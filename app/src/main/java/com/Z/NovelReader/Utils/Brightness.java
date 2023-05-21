package com.Z.NovelReader.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public class Brightness {
    public static final int MAX_SYSTEM_BRIGHTNESS = 255;
    /*
    * 获取系统亮度
    * (0~1f)
    * */
    public static float getSystemBrightness(ContentResolver cr){
        float systemBrightness = -1;
        try {
            int brightness = Settings.System.getInt(cr,Settings.System.SCREEN_BRIGHTNESS);
            systemBrightness = brightness*1.0f/MAX_SYSTEM_BRIGHTNESS;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }


    /**
     * 更改当前界面亮度
     * @param activity 要更改亮度的界面
     * @param brightness 亮度值(0.0~1.0f)
     */
    public static void changeActivityBrightness(Activity activity,float brightness){
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        if (brightness==-1){
            layoutParams.screenBrightness=WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }else {
            layoutParams.screenBrightness = brightness;
        }
        window.setAttributes(layoutParams);
    }

    /**
     * 更改系统亮度,尽量不使用
     * @param context
     * @param systemBrightness
     * @return
     */
    public static boolean setSystemScreenBrightness(Context context, int systemBrightness) {
        return Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,systemBrightness);
    }

    /**
     * 开启自动亮度调节
     * @param activity
     */
    public static void startAutoBrightness(Activity activity) {
        CheckAutoBrightnessPermission(activity);
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        activity.getContentResolver().notifyChange(uri, null);
    }

    /**
     * 关闭自动亮度调节
     * @param activity
     */
    public static void stopAutoBrightness(Activity activity) {
        CheckAutoBrightnessPermission(activity);
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    public static class BrightnessObserver extends ContentObserver{

        private BrightnessChangeListener listener;
        private Activity mActivity;

        public BrightnessObserver(Activity activity,Handler handler,BrightnessChangeListener brightnessChangeListener) {
            super(handler);
            this.mActivity=activity;
            this.listener=brightnessChangeListener;
        }

        public void setOnBrightnessChangeListener(BrightnessChangeListener listener) {
            this.listener = listener;
        }
        public void register(){
            Uri brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            mActivity.getContentResolver().registerContentObserver(brightnessUri,
                    true,this);
        }
        public void unregister(){
            mActivity.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            float systemBrightness = getSystemBrightness(mActivity.getContentResolver());
            if(systemBrightness!=-1)listener.onChange(systemBrightness);
        }
    }
    public interface BrightnessChangeListener{
        /**
         * @param brightness (0.0~1.0f) 系统亮度
         */
        void onChange(float brightness);
    }

    public static void CheckAutoBrightnessPermission(Activity activity) {
        if (!Settings.System.canWrite(activity)){
            Uri selfPackageUri = Uri.parse("package:"
                    + activity.getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    selfPackageUri);
            activity.startActivity(intent);
        }
    }
}
