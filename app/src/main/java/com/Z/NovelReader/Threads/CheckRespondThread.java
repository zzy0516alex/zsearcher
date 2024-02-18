package com.Z.NovelReader.Threads;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.SSLAgent;

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
    public static final long DEFAULT_TIME_CONSUME_MS = 999999L;
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
        long timeConsuming = DEFAULT_TIME_CONSUME_MS;//ms
        long timeSum = 0L;
        int connect_count = 0;
        for (int i = 0; i < RESPOND_TEST_LEVEL; i++) {
            try {
                SSLAgent.getInstance().trustAllHttpsCertificates();
                LocalDateTime beginTime = LocalDateTime.now();
                Connection connect = Jsoup.connect(rule.getBookSourceUrl());
                connect.timeout(2000);
                connect.ignoreHttpErrors(true);
                connect.followRedirects(true);
                connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
                Document doc= connect.get();
                timeSum += Duration.between(beginTime,LocalDateTime.now()).toMillis();
                connect_count++;
            }catch (IOException e){}
        }
        if (connect_count!=0)timeConsuming = timeSum/(long) connect_count;
        long connection_time = timeConsuming;
        Log.d(TAG, rule.getBookSourceName()+ " | connect time: " +timeConsuming);

        //存在额外目录页
        if (rule.getRuleBookInfo()!=null && rule.getRuleBookInfo().getTocUrl()!=null)
            timeConsuming *= 2;
        //存在子目录分节，假定每节50章
        if (rule.getRuleToc()!=null && rule.getRuleToc().getNextTocUrl()!=null)
            timeConsuming *= 100;

        DBUpdater.UpdateSourceRespondTime(rule.getId(),timeConsuming);
//        if (timeConsuming>=DEFAULT_TIME_CONSUME_MS)
//            DBUpdater.UpdateSourceVisibility(rule.getId(),false);
        ResourceCheckResult result = new ResourceCheckResult()
                                    .setResourceID(rule.getId())
                                    .setRespondTime(timeConsuming)
                                    .setConnectionTime(connection_time)
                                    .setTimeout((timeConsuming >= DEFAULT_TIME_CONSUME_MS))
                                    .setEnabled(rule.isEnabled());

        callback(PROCESS_DONE,result);
    }

    public static class ResourceCheckResult{
        public int resourceID;
        public long respondTime;//考虑规则复杂度的综合响应时间
        public long connectionTime;//单次访问的响应时间，用于简单判断源网站是否还存在
        public boolean isTimeout;//是否超时
        public boolean isEnabled;//是否正在使用

        public ResourceCheckResult() {
        }

        public ResourceCheckResult setResourceID(int resourceID) {
            this.resourceID = resourceID;return this;
        }

        public ResourceCheckResult setRespondTime(long respondTime) {
            this.respondTime = respondTime;return this;
        }

        public ResourceCheckResult setConnectionTime(long connectionTime) {
            this.connectionTime = connectionTime;return this;
        }

        public ResourceCheckResult setTimeout(boolean timeout) {
            isTimeout = timeout;return this;
        }

        public ResourceCheckResult setEnabled(boolean enabled) {
            isEnabled = enabled;return this;
        }
    }
}
