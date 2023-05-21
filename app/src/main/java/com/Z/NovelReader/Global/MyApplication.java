package com.Z.NovelReader.Global;

import android.app.Application;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

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
