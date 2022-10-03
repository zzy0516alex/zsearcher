package com.Z.NovelReader.Basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

abstract public  class BasicUpdaterBroadcast extends BroadcastReceiver {

    private Context context;

    public static String CATALOG_NOVELSHOW = "Z.NovelShow.intent.CATALOG_READY";
    public static String CATALOGLINK_BOOKSHELF = "Z.BookShelf.intent.CATALOGLINK_READY";
    public static String CATALOGLINK_NOVELVIEWER = "Z.NovelViewer.intent.CATALOGLINK_READY";

    public BasicUpdaterBroadcast(Context context) {
        this.context = context;
    }

    public void register(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(CATALOG_NOVELSHOW);
        filter.addAction(CATALOGLINK_BOOKSHELF);
        filter.addAction(CATALOGLINK_NOVELVIEWER);
        context.registerReceiver(this, filter);
    }
}
