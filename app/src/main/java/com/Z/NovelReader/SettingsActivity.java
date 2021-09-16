package com.Z.NovelReader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        quit=findViewById(R.id.quit_settings);
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        DownloadPath=myInfo.getString("DownloadPath","/download/ZsearcherDownloads");
        sourceDBTools=new NovelSourceDBTools(context);

        initQuitButton();

        initStoragePathSelector();

        initSetUpNovelSources();

        initStatusBar();
    }

    private void initSetUpNovelSources() {
        SetNovelSource=findViewById(R.id.setup_novel_source);
        SetNovelSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceDBTools.DeleteAllSource();
                NovelSourceGetterThread t1=new NovelSourceGetterThread(context,"http://www.yckceo.com/d/urU8x");//笔趣阁
                NovelSourceGetterThread t2=new NovelSourceGetterThread(context,"http://www.yckceo.com/d/2jdqx");//独步小说
                NovelSourceGetterThread t3=new NovelSourceGetterThread(context,"http://yck.mumuceo.com/d/f1HG3");//E小说
                t1.start();
                t2.start();
                t3.start();

                Log.d("source","书源已添加");
            }
        });
    }

    private void initStoragePathSelector() {
        StoragePathChange=findViewById(R.id.select_storage_path);
        StoragePathChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadPath=myInfo.getString("DownloadPath","/download/ZsearcherDownloads");
                RelativeLayout PathSelectorLayout= (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.storage_path_select_popup,null);
                TextView currentPath=PathSelectorLayout.findViewById(R.id.current_path);
                ProgressBar storageAvailable=PathSelectorLayout.findViewById(R.id.storage);
                TextView SizeAvailable=PathSelectorLayout.findViewById(R.id.available);
                TextView SizeTotal=PathSelectorLayout.findViewById(R.id.total);
                Button change=PathSelectorLayout.findViewById(R.id.change_path);
                currentPath.setText("根目录"+DownloadPath);
                SizeAvailable.setText(StorageUtils.getFileSize(StorageUtils.getROMAvailableSize(context)));
                SizeTotal.setText(StorageUtils.getFileSize(StorageUtils.getROMTotalSize(context)));
                float progress=StorageUtils.getROMAvailableSize(context)*100.0f/StorageUtils.getROMTotalSize(context);
                storageAvailable.setProgress((int) progress);
                final PopupWindow PathSelector=new PopupWindow(PathSelectorLayout,780, ViewGroup.LayoutParams.WRAP_CONTENT);
                PathSelector.setFocusable(true);
                PathSelector.setBackgroundDrawable(getDrawable(R.drawable.popwindow_white));
                BackGroundAlpha(0.6f);
                PathSelector.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                PathSelector.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        BackGroundAlpha(1.0f);
                    }
                });
                change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFileSelector();
                        PathSelector.dismiss();
                    }
                });
            }
        });
    }

    private void initQuitButton() {
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

    public void BackGroundAlpha(float f) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = f;
        activity.getWindow().setAttributes(layoutParams);
    }


    private void initStatusBar() {
        window= this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        window.setStatusBarColor(getResources().getColor(R.color.white));
        StatusBarUtil.setStatusBarDarkTheme(this,true);
    }
}
