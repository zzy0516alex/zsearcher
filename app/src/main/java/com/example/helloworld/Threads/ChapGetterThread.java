package com.example.helloworld.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelChap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static com.example.helloworld.Threads.ContentTextThread.getContent1;
import static com.example.helloworld.Threads.ContentTextThread.getContent2;


public class ChapGetterThread extends Thread {
    private boolean has_lastChap;
    private boolean has_nextChap;
    private int LinkType;
    private String url;
    private NovelCatalog catalog;
    private String chap_title;
    private String chap_content;
    private int current_chap=-1;
    private NovelChap chap;
    private Handler mHandler;
    private int err_counter=0;
    public final static int GET_SUCCEED=0;
    public final static int INTERNET_ERROR=1;

    public ChapGetterThread(String url, NovelChap newChap ,Handler handler) {
        this.url = url;
        mHandler=handler;
        this.chap=newChap;
    }

    public void setChapState(int link_type) {
        switch(link_type){
            case NovelChap.BOTH_LINK_AVAILABLE:{
                this.has_lastChap = true;
                this.has_nextChap = true;
            }
                break;
            case NovelChap.LAST_LINK_ONLY:{
                this.has_lastChap = true;
                this.has_nextChap = false;
            }
                break;
            case NovelChap.NEXT_LINK_ONLY:{
                this.has_lastChap = false;
                this.has_nextChap = true;
            }
            default:
        }
        LinkType=link_type;

    }


    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            grabContent(document,chap.getTag());
            chap.setContent(chap_content);
            if (has_lastChap){
                String last_link = catalog.getLink().get(chap.getCurrent_chapter() - 1);
                chap.setLast_link(last_link);
            }
            if (has_nextChap){
                String next_link = catalog.getLink().get(chap.getCurrent_chapter() + 1);
                chap.setNext_link(next_link);
            }
            Message message=mHandler.obtainMessage();
            message.obj=chap;
            message.what=GET_SUCCEED;
            mHandler.sendMessage(message);
        }catch (IOException e){
            e.printStackTrace();
            Message err_message;
            err_counter++;
            if (err_counter<10)run();
            else {
                err_message=mHandler.obtainMessage();
                err_message.what=INTERNET_ERROR;
                mHandler.sendMessage(err_message);
            }

        }

    }

    private void grabContent(Document document, NovelThread.TAG tag) {
        switch(tag){
            case BiQuGe:
                chap_content=getContent1(document);
                break;
            case SiDaMingZhu:
                chap_content=getContent2(document);
                break;
            default:
        }
    }
}
