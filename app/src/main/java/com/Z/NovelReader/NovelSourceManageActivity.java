package com.Z.NovelReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.NovelSourceAdapter;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceViewModel;
import com.Z.NovelReader.Threads.NovelSourceGetterThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.views.KeyboardPopupWindow;
import com.Z.NovelReader.views.WaitDialog;
import com.google.gson.JsonSyntaxException;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.Objects.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NovelSourceManageActivity extends AppCompatActivity {

    //views
    private Window window;
    private ListView lv_novelSourceList;
    private ImageButton im_manage;
    private PopupWindow manageOptions;
    private WaitDialog waitDialog;
    //data sources
    private NovelSourceAdapter adapter;
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private NovelSourceViewModel novelSourceViewModel;//书源数据维护
    private List<NovelRequire> novelRequireList;//书源列表
    //thread & handler
    private NovelSourceGetterThread.NSGetterHandler handler;
    //basic paras
    private Context context;
    private Activity activity;

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
        handler = new NovelSourceGetterThread.NSGetterHandler(new NovelSourceGetterThread.NSGetterListener() {
            @Override
            public void onSuccess() {
                if (waitDialog!=null)waitDialog.dismiss();
                Toast.makeText(context, "书源添加完成", Toast.LENGTH_SHORT).show();
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
        //初始化view model，启动observer
        novelSourceViewModel = ViewModelProviders.of(this).get(NovelSourceViewModel.class);
        novelSourceViewModel.getNovelSourceLiveData().observe(this, new Observer<List<NovelRequire>>() {
            @Override
            public void onChanged(List<NovelRequire> novelRequires) {
                novelRequireList=novelRequires;
                adapter.setNovelRequireList(novelRequireList);
                adapter.notifyDataSetChanged();
            }
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
                sourceDBTools.DeleteNovelSources(currentSource);
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
            showFileSelector();
            manageOptions.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==FileSelectorSettings.REQUEST_CODE && resultCode==FileSelectorSettings.BACK_WITH_SELECTIONS){
            assert data != null;
            Bundle bundle=data.getExtras();
            assert bundle != null;
            ArrayList<String> FilePathSelected
                    =bundle.getStringArrayList(FileSelectorSettings.FILE_PATH_LIST_REQUEST);
            if (FilePathSelected!=null && FilePathSelected.size()!=0) {
                String path=FilePathSelected.get(0);
                File local_source = new File(path);
                if (local_source.exists()){
                    try {
                        String sourceJSON = FileIOUtils.read_line("", local_source);
                        NovelRequire[] novelRequire = NovelRequire.getNovelRequireBeans(sourceJSON);
                        NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(context);
                        sourceDBTools.InsertNovelSources(novelRequire);
                        Toast.makeText(this, "新增：" +novelRequire.length+ "个书源", Toast.LENGTH_SHORT).show();
                    }catch (JsonSyntaxException e){
                        e.printStackTrace();
                        Toast.makeText(context, "文件不符合书源格式", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showManageOptions() {
        //加载布局
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.novel_source_manage_popup, null);
        //找到布局的控件
        final LinearLayout ll_internet_import = layout.findViewById(R.id.import_from_internet);
        final LinearLayout ll_local_import = layout.findViewById(R.id.import_from_local);
        final LinearLayout ll_delete_select = layout.findViewById(R.id.delete_select);
        final TextView delete_select_title = ll_delete_select.findViewById(R.id.delete_select_title);
        //设置属性
        if (adapter.isDelete_mode()){
            delete_select_title.setText("退出删除");
        }else {
            delete_select_title.setText("删除模式");
        }
        // 实例化popupWindow
        manageOptions = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //焦点和阴影
        manageOptions.setFocusable(true);
        manageOptions.setElevation(8);
        //设置popupWindow弹出窗体位置
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        manageOptions.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.popwindow_white));
        manageOptions.showAsDropDown(im_manage,-layout.getMeasuredWidth() + im_manage.getWidth(),15);
        //点击事件
        //网络导入
        ll_internet_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextForURL();
            }
        });
        ll_local_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StorageUtils.isStoragePermissionGranted(context,activity)) {
                    showFileSelector();
                    manageOptions.dismiss();
                }
            }
        });
        ll_delete_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!adapter.isDelete_mode()){
                    adapter.setDelete_mode(true);
                    delete_select_title.setText("退出删除");
                }else {
                    adapter.setDelete_mode(false);
                    delete_select_title.setText("删除模式");
                }
            }
        });
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
                manageOptions.dismiss();
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
        FileSelectorSettings settings=new FileSelectorSettings();
        settings.setRootPath(FileSelectorSettings.getSystemRootPath())
                .setMaxFileSelect(1)
                .setTitle("请选择书源文件")
                .setThemeColor("#1E90FF")
                .setFileTypesToSelect(FileInfo.FileType.Unknown)
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