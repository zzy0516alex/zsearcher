package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.SearchQuery;
import com.google.gson.JsonSyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class NovelSourceGetterThread extends Thread {

    //书源地址//NOVEL_SOURCE_URL="http://yck.mumuceo.com/yuedu/shuyuan/index.html";
    private Context context;
    private String URL;
    private Handler handler;
    private Message message;
    public static final int SOURCE_ADD_FINISH=0x0;
    public static final int SOURCE_TYPE_NOT_JSON=0x1;
    public static final int NO_INTERNET=0x2;

    public NovelSourceGetterThread(Context context,String url) {
        this.context = context;
        this.URL=url;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        if (handler!=null)message=handler.obtainMessage();
        try {
            Document document= Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .followRedirects(true)
                    .timeout(50000)
                    .ignoreContentType(true).get();
            NovelRequire novelRequire = NovelRequire.getNovelRequireBean(document.text());
            NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(context);
            sourceDBTools.InsertNovelSources(novelRequire);
            if (message!=null){
                message.what = SOURCE_ADD_FINISH;
                handler.sendMessage(message);
            }
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            if (message!=null){
                message.what=SOURCE_TYPE_NOT_JSON;
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            message.what=NO_INTERNET;
            handler.sendMessage(message);
        }
    }

    public static class NSGetterHandler extends Handler{
        NSGetterListener listener;

        public NSGetterHandler(NSGetterListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==SOURCE_ADD_FINISH)
                listener.onSuccess();
            else {
                listener.onError(msg.what);
            }
        }
    }

    public interface NSGetterListener{
        void onSuccess();
        void onError(int error_code);
    }
}
