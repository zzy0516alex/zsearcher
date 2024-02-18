package com.Z.NovelReader.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.WindowManager;

public class ScreenUtils {
    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    /**
     * 获取屏幕宽度(px)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int dip2px(Context context, int dip){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static void BackGroundAlpha(Activity activity,float f) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = f;
        activity.getWindow().setAttributes(layoutParams);
    }
}
