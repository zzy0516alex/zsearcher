package com.Z.NovelReader.Global;

import android.app.Application;

import java.io.File;

/**
 * in AndroidManifest.xml
 * android:name=".Global.MyApplication"
 */
public class MyApplication extends Application {
    private static File ExternalDir;
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        ExternalDir = getExternalFilesDir(null);
    }

    public static File getExternalDir() {
        return ExternalDir;
    }
}
