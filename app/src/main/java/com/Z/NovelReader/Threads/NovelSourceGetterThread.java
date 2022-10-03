package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.google.gson.JsonSyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class NovelSourceGetterThread extends BasicHandlerThread {

    //书源地址//NOVEL_SOURCE_URL="http://yck.mumuceo.com/yuedu/shuyuan/index.html";
    private Context context;
    private String URL;
    public static final int SOURCE_TYPE_NOT_JSON = PROCESSOR_ERROR;

    public NovelSourceGetterThread(Context context,String url) {
        this.context = context;
        this.URL=url;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .followRedirects(true)
                    .timeout(50000)
                    .ignoreContentType(true).get();
            NovelRequire novelRequire = NovelRequire.getNovelRequireBean(document.text());
            NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(context);
            sourceDBTools.InsertNovelSources(novelRequire);
            callback(PROCESS_DONE,novelRequire);
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            report(SOURCE_TYPE_NOT_JSON);
        } catch (IOException e) {
            e.printStackTrace();
            report(NO_INTERNET);
        }
    }

//    public static class NSGetterHandler extends Handler{
//        NSGetterListener listener;
//
//        public NSGetterHandler(NSGetterListener listener) {
//            this.listener = listener;
//        }
//
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            if (msg.what==SOURCE_ADD_FINISH)
//                listener.onSuccess();
//            else {
//                listener.onError(msg.what);
//            }
//        }
//    }
//
//    public interface NSGetterListener{
//        void onSuccess();
//        void onError(int error_code);
//    }
}
