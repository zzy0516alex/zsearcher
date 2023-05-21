package com.Z.NovelReader.views.Dialog;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Fragments.NovelViewFragmentFactory;
import com.Z.NovelReader.Global.OnSettingChangeListener;
import com.Z.NovelReader.Objects.beans.NovelViewTheme;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.Brightness;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.views.BrightnessSlider;
import com.Z.NovelReaderKT.slider.NiftySlider;

import java.util.Locale;

public class ViewerSettingDialog extends Dialog {

    private Context context;
    private Button follow_system;
    private Button btn_text_size_inc;
    private Button btn_text_size_dec;
    private TextView tv_text_size_show;
    private Button btn_line_width_inc;
    private Button btn_line_width_dec;
    private TextView tv_line_width_show;
    private BrightnessSlider slider_brightness_control;
    private RadioGroup rg_mode_select;
    public boolean isFollowSystemBrightness;
    private int currentTextSize;
    private float currentLineWidth;
    private NovelViewFragmentFactory.ViewMode currentViewMode;
    private ParentSettingChangeListener parentListener;
    private OnSettingChangeListener settingChangeListener;
    private NovelViewTheme novelViewTheme;
    private SharedPreferences myInfo;
    public static Brightness.BrightnessObserver brightnessObserver;
    private float brightness;

    public static final int MAX_TEXT_SIZE = 35;
    public static final int MIN_TEXT_SIZE = 15;
    public static final float MAX_LINE_WIDTH = 2.5f;
    public static final float MIN_LINE_WIDTH = 0.5f;

    //与activity相关的setting变动
    public interface ParentSettingChangeListener{
        void onViewModeChange(NovelViewFragmentFactory.ViewMode mode);
    }

    public ViewerSettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
        myInfo = context.getSharedPreferences("UserInfo",MODE_PRIVATE);
        this.novelViewTheme = new NovelViewTheme();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dialog);
        init_btn_follow();
        init_adj_TextSize();
        init_adj_LineWidth();
        init_skb_Light();
        rg_mode_select = findViewById(R.id.mode_select_group);
        String initViewMode = myInfo.getString("myViewMode","vertical");
        switch(initViewMode){
            case "vertical":
                final RadioButton btn_mode_vertical = findViewById(R.id.mode_vertical);
                btn_mode_vertical.setChecked(true);
                currentViewMode = NovelViewFragmentFactory.ViewMode.VERTICAL;
                break;
            case "horizon":
                final RadioButton btn_mode_horizon = findViewById(R.id.mode_horizon);
                btn_mode_horizon.setChecked(true);
                currentViewMode = NovelViewFragmentFactory.ViewMode.HORIZONTAL;
                break;
            case "vivid":
                final RadioButton btn_mode_vivid = findViewById(R.id.mode_vivid);
                btn_mode_vivid.setChecked(true);
                break;
            default:
                break;
        }

        rg_mode_select.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.mode_vertical:{
                        currentViewMode = NovelViewFragmentFactory.ViewMode.VERTICAL;
                    }
                        break;
                    case R.id.mode_horizon:{
                        currentViewMode = NovelViewFragmentFactory.ViewMode.HORIZONTAL;
                    }
                        break;
                    case R.id.mode_vivid:{
                        Toast.makeText(context, "当前还不支持仿真模式", Toast.LENGTH_SHORT).show();
                    }
                        break;
                    default:
                        break;
                }
                String viewModeInString = NovelViewFragmentFactory.getViewModeInString(currentViewMode);
                myInfo.edit().putString("myViewMode",viewModeInString).apply();
                if (parentListener!=null)parentListener.onViewModeChange(currentViewMode);
            }
        });

        brightnessObserver = new Brightness.BrightnessObserver((Activity) context,
                new Handler(Looper.getMainLooper()),
                (brightness) -> {
                if(isFollowSystemBrightness)
                    slider_brightness_control.setProgressSmooth(brightness*100f);
        });
        brightnessObserver.register();
    }

    private void init_skb_Light() {
        slider_brightness_control =findViewById(R.id.brightness_control);
        slider_brightness_control.setValueFrom(0f);//百分比进度条
        slider_brightness_control.setValueTo(100f);//百分比进度条
        slider_brightness_control.setValue(brightness*100f);
        slider_brightness_control.setOnValueChangeListener(new NiftySlider.OnValueChangeListener() {
            @Override
            public void onValueChange(@NonNull NiftySlider slider, float value, boolean fromUser) {
                if (fromUser){
                    Brightness.stopAutoBrightness((Activity) context);
                    Brightness.changeActivityBrightness((Activity) context,value/100f);
                    setFollowSystemBrightness(false);
                }
            }
        });
    }

    private void init_adj_TextSize() {
        btn_text_size_inc = findViewById(R.id.text_size_inc);
        btn_text_size_dec = findViewById(R.id.text_size_dec);
        tv_text_size_show = findViewById(R.id.current_text_size);

        currentTextSize = myInfo.getInt("myTextSize",20);
        tv_text_size_show.setText(String.valueOf(currentTextSize));
        if (currentTextSize == MAX_TEXT_SIZE)btn_text_size_inc.setEnabled(false);
        if (currentTextSize == MIN_TEXT_SIZE)btn_text_size_dec.setEnabled(false);

        btn_text_size_inc.setOnClickListener(v -> {
            btn_text_size_dec.setEnabled(true);
            if (currentTextSize >= MAX_TEXT_SIZE){
                currentTextSize = MAX_TEXT_SIZE;
                return;
            }
            currentTextSize++;
            if (currentTextSize == MAX_TEXT_SIZE)btn_text_size_inc.setEnabled(false);
            commitTextSizeChange();
        });

        btn_text_size_dec.setOnClickListener(v -> {
            btn_text_size_inc.setEnabled(true);
            if (currentTextSize <= MIN_TEXT_SIZE){
                currentTextSize = MIN_TEXT_SIZE;
                return;
            }
            currentTextSize--;
            if (currentTextSize == MIN_TEXT_SIZE)btn_text_size_dec.setEnabled(false);
            commitTextSizeChange();
        });
    }

    private void commitTextSizeChange() {
        tv_text_size_show.setText(String.valueOf(currentTextSize));
        //changeListener.onChangeTextSize(currentTextSize);
        novelViewTheme.setTextSize(currentTextSize);
        settingChangeListener.onViewThemeChange(novelViewTheme,NovelViewTheme.THEME_TYPE_TEXT_SIZE);
        SharedPreferences.Editor editor=myInfo.edit();
        editor.putInt("myTextSize",currentTextSize);
        editor.apply();
    }

    private void init_adj_LineWidth() {
        btn_line_width_inc = findViewById(R.id.line_width_inc);
        btn_line_width_dec = findViewById(R.id.line_width_dec);
        tv_line_width_show = findViewById(R.id.current_line_width);

        currentLineWidth = myInfo.getFloat("myLineSpace",1.0f);
        String show = String.format(Locale.CHINA, "%.1f ×", currentLineWidth);
        tv_line_width_show.setText(show);
        if (currentLineWidth == MAX_LINE_WIDTH)btn_line_width_inc.setEnabled(false);
        if (currentLineWidth == MIN_LINE_WIDTH)btn_line_width_dec.setEnabled(false);

        btn_line_width_inc.setOnClickListener(v -> {
            btn_line_width_dec.setEnabled(true);
            if (currentLineWidth >= MAX_LINE_WIDTH){
                currentLineWidth = MAX_LINE_WIDTH;
                return;
            }
            currentLineWidth+=0.1;
            if (Math.abs(currentLineWidth-MAX_LINE_WIDTH) < 0.1)btn_line_width_inc.setEnabled(false);
            commitLineWidthChange();
        });

        btn_line_width_dec.setOnClickListener(v -> {
            btn_line_width_inc.setEnabled(true);
            if (currentLineWidth <= MIN_LINE_WIDTH){
                currentLineWidth = MIN_LINE_WIDTH;
                return;
            }
            currentLineWidth-=0.1;
            if (Math.abs(currentLineWidth-MIN_LINE_WIDTH) < 0.1)btn_line_width_dec.setEnabled(false);
            commitLineWidthChange();
        });
    }

    private void commitLineWidthChange() {
        String show = String.format(Locale.CHINA, "%.1f ×", currentLineWidth);
        tv_line_width_show.setText(show);
        //changeListener.onChangeLineWidth(currentLineWidth);
        novelViewTheme.setLineSpaceMulti(currentLineWidth);
        settingChangeListener.onViewThemeChange(novelViewTheme,NovelViewTheme.THEME_TYPE_LINE_SPACE_MULTI);
        SharedPreferences.Editor editor=myInfo.edit();
        editor.putFloat("myLineSpace",currentLineWidth);
        editor.apply();
    }

    private void init_btn_follow() {
        follow_system=findViewById(R.id.with_system);
        if (isFollowSystemBrightness){
            Brightness.startAutoBrightness((Activity) context);
        }
        else {
            Brightness.stopAutoBrightness((Activity) context);
        }
        setFollowSystemBrightness(isFollowSystemBrightness);
        follow_system.setOnClickListener(v -> {
            if (isFollowSystemBrightness){
                Brightness.stopAutoBrightness((Activity) context);
                setFollowSystemBrightness(false);
            }
            else {
                Brightness.changeActivityBrightness((Activity) context,-1);
                Brightness.startAutoBrightness((Activity) context);
                setFollowSystemBrightness(true);
            }
        });
    }

    public ViewerSettingDialog setFollowSystemBrightness(boolean b) {
        int selected_color = context.getColor(R.color.DoderBlue);
        int unselected_color = Color.WHITE;
        if(follow_system!=null)
            follow_system.setTextColor(b?selected_color:unselected_color);
        isFollowSystemBrightness = b;
        return this;
    }

    public boolean isFollowSystemBrightness() {
        return isFollowSystemBrightness;
    }

    @Override
    public void show() {
        super.show();
        Window window = this.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        setFollowSystemBrightness(isFollowSystemBrightness);
        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        int wrapContent=WindowManager.LayoutParams.WRAP_CONTENT;
        int screen_height = ScreenUtils.getScreenHeight(context);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        //lp.height = screen_height/3;
        lp.height = wrapContent;
        window.setAttributes(lp);
    }

    //直接与view相关的setting变动
    public ViewerSettingDialog setChangeListener(OnSettingChangeListener changeListener) {
        this.settingChangeListener = changeListener;
        return this;
    }

    public ViewerSettingDialog setParentListener(ParentSettingChangeListener parentListener) {
        this.parentListener = parentListener;
        return this;
    }

    public ViewerSettingDialog setBrightness(float brightness) {
        this.brightness=brightness;
        return this;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
