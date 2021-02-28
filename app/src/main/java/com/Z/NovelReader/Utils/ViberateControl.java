package com.Z.NovelReader.Utils;

import android.app.Activity;
import android.app.Service;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.RequiresApi;

public class ViberateControl {
    /*
    震动控制
    milliseconds 震动时长ms
    amplitude 震动强度
    pattern 自定义震动模式【静止时长；震动时长；静止时长......】
    isRepeat -1不重复
     */

    //单次震动
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Vibrate(final Activity activity, long milliseconds){
        Vibrator vibrator= (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        VibrationEffect effect=VibrationEffect.createOneShot(milliseconds,VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(effect);
    }

    //强度可调
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Vibrate(final Activity activity, long milliseconds,int amplitude){
        Vibrator vibrator= (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        VibrationEffect effect=VibrationEffect.createOneShot(milliseconds,amplitude);
        vibrator.vibrate(effect);
    }

    //波形震动
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Vibrate(final Activity activity, long []pattern, int isRepeat){
        Vibrator vibrator= (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        VibrationEffect effect=VibrationEffect.createWaveform(pattern,isRepeat);
        vibrator.vibrate(effect);
    }
}
