package com.Z.NovelReader.Threads;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class CheckRespondThread extends BasicHandlerThread {

    private NovelRequire rule;
    private NovelSourceDBTools DBUpdater;
    public static final int RESPOND_TEST_LEVEL = 5;
    public static final String TAG = "CheckRespondThread";

    public CheckRespondThread(NovelRequire rule, NovelSourceDBTools updater) {
        this.rule = rule;
        this.DBUpdater = updater;
    }

    //按搜索5000章小说的耗时计算
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        long timeConsuming = 99999L;//ms
        long timeSum = 0L;
        int connect_count = 0;
        for (int i = 0; i < RESPOND_TEST_LEVEL; i++) {
            try {
                LocalDateTime beginTime = LocalDateTime.now();
                Connection connect = Jsoup.connect(rule.getBookSourceUrl());
                connect.timeout(2000);
                connect.ignoreHttpErrors(true);
                connect.followRedirects(true);
                connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
                Document doc= connect.get();
                timeSum += Duration.between(beginTime,LocalDateTime.now()).toMillis();
                connect_count++;
            }catch (IOException ignored){}
        }
        if (connect_count!=0)timeConsuming = timeSum/(long) connect_count;
        Log.d(TAG, rule.getBookSourceName()+ " | connect time: " +timeConsuming);

        //存在额外目录页
        if (rule.getRuleBookInfo()!=null && rule.getRuleBookInfo().getTocUrl()!=null)
            timeConsuming *= 2;
        //存在子目录分节，假定每节50章
        if (rule.getRuleToc()!=null && rule.getRuleToc().getNextTocUrl()!=null)
            timeConsuming *= 100;

        DBUpdater.UpdateSourceRespondTime(rule.getId(),timeConsuming);
        if (timeConsuming>=99999)DBUpdater.UpdateSourceVisibility(rule.getId(),false);

        callback(PROCESS_DONE,timeConsuming);
    }
}
