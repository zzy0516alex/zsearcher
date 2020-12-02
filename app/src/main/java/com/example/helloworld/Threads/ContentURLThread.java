package com.example.helloworld.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.helloworld.R;
import com.example.helloworld.myObjects.NovelCatalog;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class ContentURLThread extends Thread {
    private String url;
    private String  newUrl;
    private String FirstTitle;
    private Handler handler;
    private Context context;
    private NovelThread.TAG tag;
    public ContentURLThread(String url) {
        this.url=url;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setTag(NovelThread.TAG tag) {
        this.tag = tag;
    }

    @Override
    public void run() {
        super.run();
        try {
            Connection connect = Jsoup.connect(url);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();
            switch(tag){
                case BiQuGe:
                    processor1(document);
                    break;
                case SiDaMingZhu:
                    processor2(document);
                    break;
                default:
            }
            if (newUrl!=null) {
                Message message = handler.obtainMessage();
                NovelCatalog novelCatalog=new NovelCatalog();
                novelCatalog.add(FirstTitle,newUrl);
                message.obj = novelCatalog;
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processor1(Document document) {
        Elements element=document.select("div.box_con");
        Element ele_firstchap=element.get(1).select("a").first();
        String firsturl=ele_firstchap.attr("href");
        newUrl=url+firsturl;
        FirstTitle=ele_firstchap.text();
    }
    private void processor2(Document document) {
        Elements element=document.select("div.info_mulu");
        Element ele_firstchap=element.get(0).select("a").first();
        String firsturl=ele_firstchap.attr("href");
        newUrl=context.getString(R.string.book_read_base2)+firsturl;
        FirstTitle=ele_firstchap.text()+"(1)";
    }


}
