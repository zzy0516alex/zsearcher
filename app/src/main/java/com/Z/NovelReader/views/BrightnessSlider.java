package com.Z.NovelReader.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.Z.NovelReader.R;
import com.Z.NovelReaderKT.slider.Effects.ITEffect;
import com.Z.NovelReaderKT.slider.NiftySlider;
import com.Z.NovelReaderKT.slider.Widgets.Utils;

import org.jetbrains.annotations.Nullable;

public class BrightnessSlider extends NiftySlider {
    private ObjectAnimator animator;
    private float progress_hold;//等待动画结束之后赋值给value

    public BrightnessSlider(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BrightnessSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BrightnessSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        int inactiveTrackColor = Utils.INSTANCE.setColorAlpha(ContextCompat.getColor(context, R.color.DoderBlue), 0.2f);
        int activeTrackColor = Utils.INSTANCE.setColorAlpha(ContextCompat.getColor(context, R.color.DoderBlue), 0.4f);
        int iconTintColor = Utils.INSTANCE.setColorAlpha(ContextCompat.getColor(context, R.color.DoderBlue), 0.7f);

        ITEffect icon_effect = new ITEffect(this);
        icon_effect.setStartIcon(R.mipmap.icon_brightness_down);
        icon_effect.setEndIcon(R.mipmap.icon_brightness_up);
        icon_effect.setStartIconSize(Utils.INSTANCE.dpToPx(10));
        icon_effect.setEndIconSize(Utils.INSTANCE.dpToPx(14));
        icon_effect.setStartTintList(ColorStateList.valueOf(iconTintColor));
        icon_effect.setEndTintList(ColorStateList.valueOf(iconTintColor));
        icon_effect.setStartPadding(Utils.INSTANCE.dpToPx(12));
        icon_effect.setEndPadding(Utils.INSTANCE.dpToPx(12));

        this.setTrackTintList(ColorStateList.valueOf(activeTrackColor));
        this.setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor));
        this.setEffect(icon_effect);

        animator = new ObjectAnimator();
        //设置动画属性
        animator.setPropertyName("value");
        //设置执行动画的View
        animator.setTarget(this);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setValue(progress_hold);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                setValue(progress_hold);
            }
        });
    }

    public void setProgressSmooth(float target_progress){
        float current_progress = getValue();
        //设置进度数组，  0 - max
        animator.setFloatValues(current_progress,target_progress);
        float delta_progress = Math.abs(current_progress - target_progress);
        float percent = delta_progress / getValueTo();
        //设置动画时间
        animator.setDuration((int)(50*percent*100));
        //动画开启
        animator.start();
        progress_hold = target_progress;
    }
}
