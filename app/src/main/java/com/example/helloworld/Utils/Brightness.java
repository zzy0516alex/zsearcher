package com.example.helloworld.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public class Brightness {
    /*
    * 获取系统亮度
    * */
    public static int getSystemBrightness(ContentResolver cr){
        int systemBrightness=0;
        try {
            systemBrightness = Settings.System.getInt(cr,Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }

    /*
    * 更改当前界面亮度
    * */
    public static void changeAppBrightness(Activity activity,int brightness){
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        if (brightness==-1){
            layoutParams.screenBrightness=WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }else {
            layoutParams.screenBrightness=(brightness<=0 ? 1:brightness)/4095f;
        }
        window.setAttributes(layoutParams);
    }
}
