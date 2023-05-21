package com.Z.NovelReader.views.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.Z.NovelReader.R;
import com.Z.NovelReader.SettingsActivity;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.z.fileselectorlib.FileSelectorSettings;
import com.z.fileselectorlib.FileSelectorTheme;
import com.z.fileselectorlib.Objects.BasicParams;
import com.z.fileselectorlib.Objects.FileInfo;

import java.io.File;
import java.util.ArrayList;

public class PathSelectDialog extends PopupWindow {

    private String downloadPath;
    private Context context;
    private Activity activity;

    public PathSelectDialog(Context context, String initPath) {
        super(context);
        //总布局
        RelativeLayout pathSelectorLayout= (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.storage_path_select_popup,null);
        setContentView(pathSelectorLayout);
        this.downloadPath = initPath;
        this.context = context;
        this.activity = (Activity) context;

        //初始化视图
        TextView currentPath = pathSelectorLayout.findViewById(R.id.current_path);
        ProgressBar storageAvailable = pathSelectorLayout.findViewById(R.id.storage);
        TextView SpaceTotal = pathSelectorLayout.findViewById(R.id.total);
        Button change = pathSelectorLayout.findViewById(R.id.change_path);
        //第一行标题
        currentPath.setText("当前: Root" + downloadPath);
        //第二行存储容量
        long space_available= StorageUtils.getROMAvailableSize(context);
        long space_total=StorageUtils.getROMTotalSize(context);
        String storage_info=String.format(context.getString(R.string.storage_info),
                StorageUtils.getFileSize(space_available),
                StorageUtils.getFileSize(space_total));
        SpaceTotal.setText(storage_info);
        float progress=(space_total-space_available)*100.0f/StorageUtils.getROMTotalSize(context);
        storageAvailable.setProgress((int) progress);
        //弹窗设置
        this.setFocusable(true);
        this.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.popwindow_white));
        this.setOnDismissListener(() -> ScreenUtils.BackGroundAlpha(activity,1.0f));
        //第三行按键设置
        change.setBackground(getCorneredSelector(0,0,15,15));
        change.setOnClickListener(v -> {
            showFileSelector();
            dismiss();
        });
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        ScreenUtils.BackGroundAlpha(activity,0.6f);
    }

    private void showFileSelector() {
        FileSelectorTheme theme = new FileSelectorTheme();
        theme.setThemeColor("#1E90FF");

        FileSelectorSettings settings=new FileSelectorSettings();
        settings.setRootPath(FileSelectorSettings.getSystemRootPath() + downloadPath)
                .setMaxFileSelect(1)
                .setTitle("请选择文件夹")
                .setTheme(theme)
                .setFileTypesToSelect(FileInfo.FileType.Folder)
                .setMoreOptions(new String[]{"新建文件夹", "删除文件"},
                        (activity, currentPath, FilePathList, FilePathSelected) -> {
                            File Folder =new File(currentPath,"新文件夹");
                            if(!Folder.exists()){
                                Folder.mkdir();
                            }
                        }, (activity, currentPath, FilePathList, FilePathSelected) -> {
                            if (FilePathSelected!=null){
                                for (String path : FilePathSelected) {
                                    File delFile=new File(path);
                                    delFile.delete();
                                }
                            }
                        })
                .show(activity);
    }

    private GradientDrawable getCorneredBackground(float radius_top_left, float radius_top_right, float radius_bottom_right, float radius_bottom_left) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        float[] radii = new float[]{
                radius_top_left, radius_top_left,
                radius_top_right, radius_top_right,
                radius_bottom_right, radius_bottom_right,
                radius_bottom_left, radius_bottom_left
        };
        background.setCornerRadii(radii);
        return background;
    }

    private StateListDrawable getCorneredSelector(float radius_top_left, float radius_top_right, float radius_bottom_right, float radius_bottom_left){
        GradientDrawable pressed = getCorneredBackground(radius_top_left, radius_top_right, radius_bottom_right, radius_bottom_left);
        pressed.setColor(0xFFe6e6e6);
        GradientDrawable normal = getCorneredBackground(radius_top_left, radius_top_right, radius_bottom_right, radius_bottom_left);
        normal.setColor(0xFFFFFFFF);
        StateListDrawable background = new StateListDrawable();
        int state_pressed =  android.R.attr.state_pressed;
        background.addState(new int[]{state_pressed}, pressed);
        background.addState(new int[]{-state_pressed}, normal);
        return background;
    }
}
