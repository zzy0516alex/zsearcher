package com.Z.NovelReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Threads.NovelSourceGetterThread;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.StorageUtils;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.Objects.BasicParams;
import com.z.fileselectorlib.Objects.FileInfo;

import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private Activity activity;
    private Context context;
    private Window window;
    private RelativeLayout StoragePathChange;
    private RelativeLayout SetNovelSource;
    private PopupWindow PathSelector;
    private SharedPreferences myInfo;
    private NovelSourceDBTools sourceDBTools;
    private String DownloadPath;
    private ImageButton quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        activity=SettingsActivity.this;
        context=this;
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        DownloadPath=myInfo.getString("DownloadPath","/download/ZsearcherDownloads");
        sourceDBTools=new NovelSourceDBTools(context);

        //初始化退出按钮
        initQuitButton();

        //初始化存储路径选择对话框
        initStoragePathSelector();

        initSetUpNovelSources();

        //初始化顶部状态栏
        initStatusBar();
    }

    private void initSetUpNovelSources() {
        SetNovelSource=findViewById(R.id.setup_novel_source);
        SetNovelSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sourceDBTools.DeleteAllSource();
//                NovelSourceGetterThread t1=new NovelSourceGetterThread(context,"http://www.yckceo.com/d/urU8x");//笔趣阁
//                NovelSourceGetterThread t2=new NovelSourceGetterThread(context,"http://www.yckceo.com/d/2jdqx");//独步小说
//                NovelSourceGetterThread t3=new NovelSourceGetterThread(context,"http://yck.mumuceo.com/d/f1HG3");//E小说
//                t1.start();
//                t2.start();
//                t3.start();
                startActivity(new Intent(SettingsActivity.this,
                        NovelSourceManageActivity.class));
            }
        });
    }

    private void initStoragePathSelector() {
        StoragePathChange=findViewById(R.id.select_storage_path);
        StoragePathChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadPath=myInfo.getString("DownloadPath","/download/ZsearcherDownloads");
                //总布局
                RelativeLayout PathSelectorLayout= (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.storage_path_select_popup,null);
                //初始化视图
                TextView currentPath=PathSelectorLayout.findViewById(R.id.current_path);
                ProgressBar storageAvailable=PathSelectorLayout.findViewById(R.id.storage);
                TextView SpaceTotal=PathSelectorLayout.findViewById(R.id.total);
                Button change=PathSelectorLayout.findViewById(R.id.change_path);
                //第一行标题
                currentPath.setText("根目录"+DownloadPath);
                //第二行存储容量
                long space_available=StorageUtils.getROMAvailableSize(context);
                long space_total=StorageUtils.getROMTotalSize(context);
                String storage_info=String.format(getString(R.string.storage_info),
                        StorageUtils.getFileSize(space_available),
                        StorageUtils.getFileSize(space_total));
                SpaceTotal.setText(storage_info);
                float progress=(space_total-space_available)*100.0f/StorageUtils.getROMTotalSize(context);
                storageAvailable.setProgress((int) progress);
                //弹窗设置
                PathSelector=new PopupWindow(PathSelectorLayout,780, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                //第三行按键设置
                change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isStoragePermissionGranted()) {
                            showFileSelector();
                            PathSelector.dismiss();
                        }
                    }
                });
            }
        });
    }

    private boolean isStoragePermissionGranted() {
        int readPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermissionCheck == PackageManager.PERMISSION_GRANTED
                && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.v("storage setting", "Permission is granted");
            return true;
        } else {
            Log.v("storage setting", "Permission is revoked");
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    private void initQuitButton() {
        quit=findViewById(R.id.quit_settings);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                String path=FilePathSelected.get(0).replace(FileSelectorSettings.getSystemRootPath(),"");
                SharedPreferences.Editor editor = myInfo.edit();
                editor.putString("DownloadPath", path).apply();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //权限重新允许
            showFileSelector();
            PathSelector.dismiss();
        }
    }

    private void showFileSelector() {
        FileSelectorSettings settings=new FileSelectorSettings();
        settings.setRootPath(FileSelectorSettings.getSystemRootPath()+DownloadPath)
                .setMaxFileSelect(1)
                .setTitle("请选择文件夹")
                .setThemeColor("#1E90FF")
                .setFileTypesToSelect(FileInfo.FileType.Folder)
                .setMoreOPtions(new String[]{"新建文件夹", "删除文件"},
                        new BasicParams.OnOptionClick() {
                            @Override
                            public void onclick(Activity activity, int position, String currentPath, ArrayList<String> FilePathSelected) {
                                File Folder =new File(currentPath,"新文件夹");
                                if(!Folder.exists()){
                                    Folder.mkdir();
                                }
                            }
                        }, new BasicParams.OnOptionClick() {
                            @Override
                            public void onclick(Activity activity, int position, String currentPath, ArrayList<String> FilePathSelected) {
                                if (FilePathSelected!=null){
                                    for (String path :
                                            FilePathSelected) {
                                        File delFile=new File(path);
                                        delFile.delete();
                                    }
                                }
                            }
                        })
                .show(SettingsActivity.this);
    }

//    public void BackGroundAlpha(float f) {
//        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
//        layoutParams.alpha = f;
//        activity.getWindow().setAttributes(layoutParams);
//    }


    private void initStatusBar() {
        window= this.getWindow();
        StatusBarUtil.setStatusBarTransparent(window);
        StatusBarUtil.setStatusBarDarkTheme(activity,true);
    }
}
