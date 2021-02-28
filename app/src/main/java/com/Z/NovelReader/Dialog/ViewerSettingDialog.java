package com.Z.NovelReader.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.Brightness;

public class ViewerSettingDialog extends Dialog {

    private Context context;
    private Button follow_system;
    private SeekBar setTextSize;
    private SeekBar setLight;
    public static boolean isFollowing;
    private int myTextSize;
    private AdjustTextSize adjustTextSize;
    public static Brightness.BrightnessObserver brightnessObserver;
    private int brightness;

    public ViewerSettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_settings);
        init_btn_follow();
        init_skb_TextSize();
        init_skb_Light();
        brightnessObserver = new Brightness.BrightnessObserver((Activity) context,
                new Handler(Looper.getMainLooper()),
                new Brightness.BrightnessChangeListener() {
                    @Override
                    public void onChange(int brightness) {
                        setLight.setProgress(brightness);
                    }
                });
        brightnessObserver.register();
    }

    private void init_skb_Light() {
        setLight=findViewById(R.id.set_light);
        setLight.setMax(4095);
        setLight.setProgress(brightness);
        setLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!isFollowing){
                    Brightness.stopAutoBrightness((Activity) context);
                    Brightness.setSystemScreenBrightness(context,progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                follow_system(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void init_skb_TextSize() {
        setTextSize=findViewById(R.id.set_text_size);
        setTextSize.setMax(60);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setTextSize.setMin(10);
        }
        setTextSize.setProgress(myTextSize);

        setTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                myTextSize=progress;
                adjustTextSize.onChanging(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                adjustTextSize.changeDone();
            }
        });
    }

    private void init_btn_follow() {
        follow_system=findViewById(R.id.with_system);
        if (isFollowing){
            follow_system.setSelected(true);
            Brightness.startAutoBrightness((Activity) context);
        }
        else {
            follow_system.setSelected(false);
            Brightness.stopAutoBrightness((Activity) context);
        }
        follow_system.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing){
                    Brightness.stopAutoBrightness((Activity) context);
                    follow_system(false);
                }
                else {
                    Brightness.startAutoBrightness((Activity) context);
                    follow_system(true);
                }
            }
        });
    }

    private void follow_system(boolean b) {
        follow_system.setSelected(b);
        isFollowing = b;
    }

    @Override
    public void show() {
        super.show();
        Window window = this.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);

        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        int wrapContent=WindowManager.LayoutParams.WRAP_CONTENT;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        lp.height = wrapContent;
        window.setAttributes(lp);
    }

    public ViewerSettingDialog setAdjusterTextSize(AdjustTextSize adjuster) {
        this.adjustTextSize = adjuster;
        return this;
    }

    public ViewerSettingDialog setMyTextSize(int myTextSize) {
        this.myTextSize = myTextSize;
        return this;
    }

    public ViewerSettingDialog setBrightness(int brightness) {
        this.brightness=brightness;
        return this;
    }

    public interface AdjustTextSize{
        void onChanging(int progress);
        void changeDone();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
