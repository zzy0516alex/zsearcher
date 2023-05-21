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
import com.Z.NovelReader.views.Dialog.PathSelectDialog;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.FileSelectorTheme;
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
        SetNovelSource.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this,
                NovelSourceManageActivity.class)));
    }

    private void initStoragePathSelector() {
        StoragePathChange=findViewById(R.id.select_storage_path);
        StoragePathChange.setOnClickListener(v -> {
            DownloadPath=myInfo.getString("DownloadPath","/download/ZsearcherDownloads");
            PathSelectDialog pathSelectDialog = new PathSelectDialog(context,DownloadPath);
            pathSelectDialog.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        });
    }

    private void initQuitButton() {
        quit=findViewById(R.id.quit_settings);
        quit.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==FileSelectorSettings.FILE_LIST_REQUEST_CODE && resultCode==FileSelectorSettings.BACK_WITH_SELECTIONS){
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


    private void initStatusBar() {
        window= this.getWindow();
        StatusBarUtil.setStatusBarTransparent(window);
        StatusBarUtil.setStatusBarDarkTheme(activity,true);
    }
}
