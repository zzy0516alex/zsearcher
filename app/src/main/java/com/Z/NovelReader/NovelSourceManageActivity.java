package com.Z.NovelReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.NovelSourceAdapter;
import com.Z.NovelReader.Basic.BasicCounterHandler;
import com.Z.NovelReader.Basic.BasicHandler;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceViewModel;
import com.Z.NovelReader.Threads.CheckRespondThread;
import com.Z.NovelReader.Threads.NovelSourceGetterThread;
import com.Z.NovelReader.Utils.CollectionUtils;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Utils.QRcodeUtils.Constant;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.views.Dialog.BottomSheetDialog;
import com.Z.NovelReader.views.Dialog.SweetDialog.SweetAlertDialog;
import com.Z.NovelReader.views.KeyboardPopupWindow;
import com.Z.NovelReader.views.Dialog.WaitDialog;
import com.Z.NovelReader.views.NovelSourceMenu;
import com.Z.NovelReader.zxing.activity.CaptureActivity;
import com.Z.NovelReader.zxing.camera.CameraManager;
import com.google.gson.JsonSyntaxException;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.FileSelectorTheme;
import com.z.fileselectorlib.Objects.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NovelSourceManageActivity extends AppCompatActivity {

    //views
    private Window window;
    private ListView lv_novelSourceList;
    private ImageButton im_manage;
    //private PopupWindow manageOptions;
    private NovelSourceMenu source_manage_menu;
    private WaitDialog waitDialog;
    //data sources
    private NovelSourceAdapter adapter;
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private NovelDBUpdater novelDBUpdater;//书籍数据库DAO
    private NovelSourceViewModel novelSourceViewModel;//书源数据维护
    private List<NovelRequire> novelRequireList;//书源列表
    //thread & handler
    //private NovelSourceGetterThread.NSGetterHandler handler;
    private BasicHandler<NovelRequire> handler;
    //basic paras
    private Context context;
    private Activity activity;
    private boolean add_source_flag = false;
    private boolean intent_handling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_source_manage);
        //参数初始化
        context=this;
        activity=this;
        //view 初始化
        lv_novelSourceList = findViewById(R.id.novel_source_list);
        im_manage = findViewById(R.id.NSM_manage);
        initWaitView();
        //界面初始化
        initStatusBar();
        //init handler
        handler = new BasicHandler<>(new BasicHandler.BasicHandlerListener<NovelRequire>() {
            @Override
            public void onSuccess(NovelRequire result) {
                if (waitDialog!=null)waitDialog.dismiss();
                Toast.makeText(context, "书源添加完成", Toast.LENGTH_SHORT).show();
                CheckRespondThread thread = new CheckRespondThread(result,sourceDBTools);
                thread.start();
            }

            @Override
            public void onError(int error_code) {
                if (waitDialog!=null)waitDialog.dismiss();
                switch(error_code){
                    case NovelSourceGetterThread.NO_INTERNET:
                        Toast.makeText(context, "无网络", Toast.LENGTH_SHORT).show();
                        break;
                    case NovelSourceGetterThread.SOURCE_TYPE_NOT_JSON:
                        Toast.makeText(context, "书源链接无效:不是json", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        });

        //书源数据库DAO初始化
        sourceDBTools = new NovelSourceDBTools(this);
        novelDBUpdater = new NovelDBUpdater(this);
        //初始化view model，启动observer
        novelSourceViewModel = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(NovelSourceViewModel.class);
        novelSourceViewModel.getNovelSourceLiveData().observe(this, novelRequires -> {
            if (add_source_flag){
                String info = String.format(Locale.CHINA,"新增：%d个书源",
                        novelRequires.size()-novelRequireList.size());
                Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
                add_source_flag = false;
            }
            novelRequireList=novelRequires;
            adapter.setNovelRequireList(novelRequireList);
            adapter.notifyDataSetChanged();
        });
        //初始化adapter
        adapter=new NovelSourceAdapter(novelRequireList,this);
        adapter.setViewClickListener(new NovelSourceAdapter.SourceViewClickListener() {
            @Override
            public void onInfoClick(NovelRequire currentSource) {
                Log.d("novel source manage activity","info:"+currentSource.toString());
                showRuleDetailDialog(currentSource);
            }

            @Override
            public void onSwitchClick(NovelRequire currentSource, boolean isEnabled) {
                sourceDBTools.UpdateSourceVisibility(currentSource.getId(),isEnabled);
            }

            @Override
            public void onSourceDelete(NovelRequire currentSource) {
                try {
                    List<Novels> novels = novelDBUpdater.QueryNovelsBySource(currentSource.getId());
                    if (novels.size()!=0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("删除书源")
                                .setMessage(String.format(Locale.CHINA, "书架中将有 %d本书被一并删除", novels.size()))
                                .setPositiveButton("确定", (dialogInterface, i) -> {
                                    novelDBUpdater.deleteNovels(novels.toArray(new Novels[0]));
                                    sourceDBTools.DeleteNovelSources(currentSource);
                                })
                                .setNegativeButton("取消", ((dialogInterface, i) -> {
                                    Log.d("novel source manage", "cancel delete");
                                }))
                                .setCancelable(false).show();
                    }else{
                        sourceDBTools.DeleteNovelSources(currentSource);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        //list 显示数据
        lv_novelSourceList.setAdapter(adapter);
        //管理按钮点击
        im_manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManageOptions();
            }
        });

        if (!intent_handling)handleSourceFileFromOuter();
        System.out.println("on create");
    }

    private void handleSourceFileFromOuter() {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            intent_handling = true;
            Uri uri = intent.getData();
            String file_path = Uri.decode(uri.getEncodedPath());
            Log.d("NovelSourceManage", "open source file: "+ file_path);
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE);
            sweetAlertDialog.setTitleText("读取书源")
                    .setContentText("检测到书源文件，是否读入")
                    .setConfirmButton("确认", sweetAlertDialog1->{
                        sweetAlertDialog1.dismissWithAnimation();
                        addNovelRequireFromFile(new File(file_path));
                    })
                    .show();
            sweetAlertDialog.setOnDismissListener(dialog -> {
                intent_handling = false;
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter.isDelete_mode())adapter.setDelete_mode(false);
        else super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //权限重新允许
            if (requestCode == StorageUtils.STORAGE_RQS_CODE)
                showFileSelector();
            if (requestCode == Constant.REQ_PERM_CAMERA){
                Intent intent = new Intent(NovelSourceManageActivity.this, CaptureActivity.class);
                startActivityForResult(intent, Constant.REQ_QR_CODE);
            }
            source_manage_menu.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //本地文件导入回调
        if (requestCode==FileSelectorSettings.FILE_LIST_REQUEST_CODE && resultCode==FileSelectorSettings.BACK_WITH_SELECTIONS){
            assert data != null;
            Bundle bundle=data.getExtras();
            assert bundle != null;
            ArrayList<String> FilePathSelected
                    =bundle.getStringArrayList(FileSelectorSettings.FILE_PATH_LIST_REQUEST);
            if (FilePathSelected!=null && FilePathSelected.size()!=0) {
                String path=FilePathSelected.get(0);
                File local_source = new File(path);
                addNovelRequireFromFile(local_source);
            }
        }
        //二维码导入回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            Log.d("qr code",""+scanResult);
            if (!scanResult.contains("http")){
                Toast.makeText(context, "未读取到链接", Toast.LENGTH_SHORT).show();
                return;
            }
            NovelSourceGetterThread sourceGetterThread=new NovelSourceGetterThread(context,scanResult);
            sourceGetterThread.setHandler(handler);
            sourceGetterThread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!intent_handling)handleSourceFileFromOuter();
        System.out.println("on resume");
    }

    private void addNovelRequireFromFile(File local_source) {
        if (local_source.exists()){
            try {
                //String sourceJSON = FileIOUtils.read_line(local_source.getPath());
                String sourceJSON = FileUtils.readZnrFile(local_source.getPath());
                NovelRequire[] novelRequire = NovelRequire.getNovelRequireBeans(sourceJSON);
                List<NovelRequire> novelRequires = Arrays.asList(novelRequire);
                //CollectionUtils<NovelRequire> collectionUtil = new CollectionUtils<>();
                List<NovelRequire> diffList = CollectionUtils.diffListByStringKey(novelRequireList, novelRequires);
                if(diffList.size()>0) {
                    NovelSourceDBTools sourceDBTools = new NovelSourceDBTools(context);
                    sourceDBTools.InsertNovelSources(novelRequire);
                    //check source
                    for (NovelRequire rule : novelRequire) {
                        CheckRespondThread thread = new CheckRespondThread(rule, sourceDBTools);
                        thread.start();
                    }
                    add_source_flag = true;
                }else Toast.makeText(context, "导入的书源已存在", Toast.LENGTH_SHORT).show();
            }catch (JsonSyntaxException e){
                e.printStackTrace();
                Toast.makeText(context, "文件不符合书源格式", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showManageOptions() {
        source_manage_menu = new NovelSourceMenu(context,adapter.isDelete_mode());
        source_manage_menu.setMenuClickListener(new NovelSourceMenu.MenuClickListener() {
            @Override
            public void onImportSource() {
                BottomSheetDialog dialog = new BottomSheetDialog(context,R.style.NormalBottomDialog);
                dialog.addSheetItem("通过书源链接导入", R.mipmap.internet_icon, () -> {
                    showEditTextForURL();
                });
                dialog.addSheetItem("通过本地文件导入", R.mipmap.local_icon, () -> {
                    if (StorageUtils.isStoragePermissionGranted(context,activity)) {
                        showFileSelector();
                    }
                });
                dialog.addSheetItem("在源仓库扫码导入", R.mipmap.qr_icon, () -> {
                    if(CameraManager.checkPermission(activity,context)) {
                        Intent intent = new Intent(NovelSourceManageActivity.this, CaptureActivity.class);
                        startActivityForResult(intent, Constant.REQ_QR_CODE);
                    }
                });
                dialog.show();
            }

            @Override
            public void onExportSource() {
                BottomSheetDialog dialog = new BottomSheetDialog(context,R.style.NormalBottomDialog);
                dialog.addSheetItem("导出为本地文件", R.mipmap.local_icon, () -> {
                    String source_output = NovelRequire.toJsonList(novelRequireList);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss",Locale.US); //设置时间格式
                    Date curDate = new Date(System.currentTimeMillis()); //获取当前时间
                    String createDate = formatter.format(curDate);   //格式转换
                    boolean isOK = FileUtils.outputZnrFile(source_output,StorageUtils.getDownloadPath(),"Source"+createDate);
                    if(isOK) Toast.makeText(context, "文件已保存："+StorageUtils.getDownloadPath()+"Source"+createDate, Toast.LENGTH_SHORT).show();
                });
                dialog.addSheetItem("通过第三方分享", R.mipmap.share_icon, () -> {

                });
                dialog.show();
            }

            @Override
            public void onDeleteModeChange(boolean isDeleteMode) {
                adapter.setDelete_mode(isDeleteMode);
            }

            @Override
            public void doCheckSource() {
                checkSourceValidity();
            }
        });
        source_manage_menu.show(im_manage);
    }

    private void checkSourceValidity() {
        BasicCounterHandler<CheckRespondThread.ResourceCheckResult> counterHandler = new BasicCounterHandler<>(
                new BasicCounterHandler.BasicCounterHandlerListener<CheckRespondThread.ResourceCheckResult>() {
            @Override
            public void onSuccess(CheckRespondThread.ResourceCheckResult ignore) {}

            @Override
            public void onError() {}

            @Override
            public void onAllProcessDone(ArrayList<CheckRespondThread.ResourceCheckResult> results) {
                int disable_num = 0;
                for (CheckRespondThread.ResourceCheckResult result: results) {
                    if (result.isEnabled && (!result.isValid)){
                        disable_num++;
                        sourceDBTools.UpdateSourceVisibility(result.resourceID,false);
                    }
                    sourceDBTools.UpdateSourceRespondTime(result.resourceID,result.respondTime);
                }
                String info = "书源校验完成";
                if (disable_num!=0)info += String.format(Locale.CHINA,",%d个书源失效",disable_num);
                Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
                waitDialog.dismiss();
            }
        });
        counterHandler.setTotal_count(novelRequireList.size(),true);
        waitDialog.setTitle("书源校验中");
        waitDialog.show();
        for (NovelRequire rule : novelRequireList) {
            CheckRespondThread check = new CheckRespondThread(rule,sourceDBTools);
            check.setHandler(counterHandler);
            check.start();
        }
    }

    private void showEditTextForURL() {
        final KeyboardPopupWindow popWiw = new KeyboardPopupWindow(context,R.layout.edit_keyboard,true);
        popWiw.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popWiw.setFocusable(true);
        //根据软键盘调整大小
        popWiw.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWiw.showAtLocation(lv_novelSourceList, Gravity.BOTTOM
                | Gravity.CENTER_HORIZONTAL, 0, 0);
        popWiw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                InputMethodManager im = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                source_manage_menu.dismiss();
            }
        });
        ImageButton imb_commit = popWiw.getContentView().findViewById(R.id.ITIM_link_commit);
        final EditText edt_input = popWiw.getContentView().findViewById(R.id.ITIM_link_input);
        imb_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = edt_input.getText().toString();
                if ("".equals(link) || !link.contains("http")){
                    Toast.makeText(activity, "错误的链接", Toast.LENGTH_SHORT).show();
                }else {
                    NovelSourceGetterThread sourceGetterThread=new NovelSourceGetterThread(context,link);
                    sourceGetterThread.setHandler(handler);
                    sourceGetterThread.start();
                    popWiw.dismiss();
                    waitDialog.setTitle("书源载入中");
                    waitDialog.show();
                }
            }
        });
    }

    private void showRuleDetailDialog(final NovelRequire currentSource) {
        RelativeLayout RuleDetailLayout= (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.novel_source_detail_popup,null);
        TextView tv_content=RuleDetailLayout.findViewById(R.id.NSDP_content);
        tv_content.setText(currentSource.toString());
        //弹窗设置
        final PopupWindow PathSelector=new PopupWindow(RuleDetailLayout,780, 1300);
        PathSelector.setFocusable(true);
        PathSelector.setBackgroundDrawable(getDrawable(R.drawable.popwindow_white));
        ScreenUtils.BackGroundAlpha(activity,0.6f);
        PathSelector.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        PathSelector.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ScreenUtils.BackGroundAlpha(activity,1.0f);
            }
        });
    }

    private void showFileSelector() {
        FileSelectorTheme theme = new FileSelectorTheme();
        theme.setThemeColor("#1E90FF");

        FileSelectorSettings settings=new FileSelectorSettings();
        settings.setRootPath(FileSelectorSettings.getSystemRootPath())
                .setMaxFileSelect(1)
                .setTitle("请选择书源文件(.znr)")
                .setTheme(theme)
                .setFileTypesToSelect(FileInfo.FileType.File)
                .setCustomizedIcons(new String[]{".znr"},context,R.mipmap.book_source_file_icon)
                .setFileTypesToShow(".znr")
                .show(NovelSourceManageActivity.this);
    }

    private void initStatusBar() {
        window= this.getWindow();
        StatusBarUtil.setStatusBarTransparent(window);
        StatusBarUtil.setStatusBarDarkTheme(this,true);
    }
    private void initWaitView(){
        waitDialog=new WaitDialog(context,R.style.WaitDialog_black)
                .setTitle("书源载入中");
    }
}