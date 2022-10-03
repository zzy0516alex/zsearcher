package com.Z.NovelReader.views;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.Z.NovelReader.NovelSourceManageActivity;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.QRcodeUtils.Constant;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.zxing.activity.CaptureActivity;
import com.Z.NovelReader.zxing.camera.CameraManager;

public class NovelSourceMenu extends PopupWindow {

    private boolean isDeleteMode;
    private RelativeLayout layout;
    private MenuClickListener menuClickListener;

    public void setMenuClickListener(MenuClickListener menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    public NovelSourceMenu(Context context, boolean is_delete_mode) {
        super(context);
        this.isDeleteMode = is_delete_mode;
        //加载布局
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.novel_source_manage_popup, null);
        //找到布局的控件
        final LinearLayout ll_internet_import = layout.findViewById(R.id.import_from_internet);
        final LinearLayout ll_local_import = layout.findViewById(R.id.import_from_local);
        final LinearLayout ll_QR_import = layout.findViewById(R.id.import_from_QRcode);
        final LinearLayout ll_delete_select = layout.findViewById(R.id.delete_select);
        final LinearLayout ll_check_source = layout.findViewById(R.id.check_source);
        final TextView delete_select_title = ll_delete_select.findViewById(R.id.delete_select_title);
        //设置属性
        if (isDeleteMode){
            delete_select_title.setText("退出删除");
        }else {
            delete_select_title.setText("删除模式");
        }
        this.setContentView(layout);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //焦点和阴影
        this.setFocusable(true);
        this.setElevation(8);
        //设置popupWindow弹出窗体位置
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        this.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.popwindow_white));
        //点击事件
        //网络导入
        ll_internet_import.setOnClickListener(v -> {
            if (menuClickListener!=null)menuClickListener.doInternetImport();
        });
        ll_local_import.setOnClickListener(v -> {
            if (menuClickListener!=null)menuClickListener.doLocalImport();
            if (isShowing())dismiss();
        });
        ll_QR_import.setOnClickListener(v -> {
            // 二维码扫码
            if (menuClickListener!=null)menuClickListener.doQRImport();
            if (isShowing())dismiss();
        });
        ll_delete_select.setOnClickListener(v -> {
            if (!isDeleteMode){
                delete_select_title.setText("退出删除");
            }else {
                delete_select_title.setText("删除模式");
            }
            isDeleteMode = !isDeleteMode;
            if (menuClickListener!=null)menuClickListener.onDeleteModeChange(isDeleteMode);
            if (isShowing())dismiss();
        });
        ll_check_source.setOnClickListener(v -> {
            if (menuClickListener!=null)menuClickListener.doCheckSource();
            if (isShowing())dismiss();
        });
    }

    public void show(View parentView){
        this.showAsDropDown(parentView,-layout.getMeasuredWidth() + parentView.getWidth(),15);
    }

    public interface MenuClickListener{
        void doInternetImport();
        void doLocalImport();
        void doQRImport();
        void onDeleteModeChange(boolean isDeleteMode);
        void doCheckSource();
    }
}
