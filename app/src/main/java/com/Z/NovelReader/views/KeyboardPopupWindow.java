package com.Z.NovelReader.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.Z.NovelReader.R;

import java.util.Timer;
import java.util.TimerTask;

public class KeyboardPopupWindow extends PopupWindow {

    private View popView;

    protected Context context;

    private boolean isOkClose=true;


    protected int maxTextSize = 24;
    protected int minTextSize = 14;
    public KeyboardPopupWindow(Context context, int edit_text_Id,boolean isOpenKeyboard) {

        this.context=context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popView = inflater.inflate(edit_text_Id, null);
        // 设置SelectPicPopupWindow的View
        this.setContentView(popView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框

        /*
         * popView.setOnTouchListener(new OnTouchListener() {
         *
         * public boolean onTouch(View v, MotionEvent event) {
         *
         * int height = popView.findViewById(R.id.pop_layout).getTop(); int
         * y=(int) event.getY(); if(event.getAction()==MotionEvent.ACTION_UP){
         * if(y<height){ dismiss(); } } return true; } });
         */

        if(isOpenKeyboard){
            openKeyboard();
        }
    }


    /**
     * 打开软键盘
     */
    private void openKeyboard() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        }, 100);
    }

    public boolean isOkClose() {
        return isOkClose;
    }
    public void setOkClose(boolean isOkClose) {
        this.isOkClose = isOkClose;
    }
    public Context getContext() {
        return context;
    }
}
