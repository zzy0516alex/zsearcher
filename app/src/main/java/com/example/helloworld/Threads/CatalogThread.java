package com.example.helloworld.Threads;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.myObjects.NovelCatalog;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CatalogThread extends Thread {
    private String url;
    private NovelThread.TAG tag;
    private ArrayList<String> ChapList;
    private ArrayList<String> ChapLinkList;
    NovelCatalog result;
    private boolean if_output=false;
    private boolean need_update=false;
    private String BookName;
    private File Dir;
    private android.os.Handler mHandler;
    private Context context;
    public static final int CATALOG_UPDATED=0;
    public static final int CATALOG_UPDATE_FAILED=1;
    public static final int CATALOG_GAIN_FAILED=2;
    int reserve_count=0;
    public CatalogThread(String url, NovelThread.TAG tag) {
        this.url=url;
        this.tag=tag;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setIf_output(boolean if_output, String bookname, File dir) {
        this.if_output = if_output;
        BookName=bookname;
        Dir=dir;
    }
    public void need_update(boolean need_update, Context context,Handler handler){
        this.need_update=need_update;
        this.context=context;
        mHandler = handler;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        ChapList=new ArrayList<>();
        ChapLinkList=new ArrayList<>();
        try {
            Document document=Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .timeout(100000)
                    .ignoreContentType(true).get();
            switch(tag){
                case BiQuGe:
                    processor1(document);
                    break;
                case SiDaMingZhu:
                    processor2(document);
                    break;
                default:
            }
            result = new NovelCatalog(ChapList, ChapLinkList);
            if(!if_output){
                Message message=mHandler.obtainMessage();
                message.obj= result;
                mHandler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reserve_count++;
            if (reserve_count<3)
                run();
            else {
                if (if_output)if_output=false;
                if (mHandler!=null && !need_update){
                    Message message = mHandler.obtainMessage();
                    message.what = CATALOG_GAIN_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        }
        if(if_output){
            StringBuilder content=new StringBuilder();
            result.completeCatalog(url,tag);
            for (int i = 0; i < result.getSize(); i++) {
                content.append(result.getTitle().get(i));
                content.append('\n');
                content.append(result.getLink().get(i));
                if(i!=ChapList.size()-1)content.append('\n');
            }
            IOtxt.WriteCatalog(Dir,BookName,content.toString());
        }
        if (need_update){
            if (if_output) {
                Message message = mHandler.obtainMessage();
                message.what = CATALOG_UPDATED;
                mHandler.sendMessage(message);
            }else {
                Message message = mHandler.obtainMessage();
                message.what = CATALOG_UPDATE_FAILED;
                mHandler.sendMessage(message);
            }
        }
    }

    private void processor1(Document document) {
        Elements elements=document.select("div.box_con");
        Elements titles=elements.get(1).select("a");
        ChapList= (ArrayList<String>) titles.eachText();
        ChapLinkList= (ArrayList<String>) titles.eachAttr("href");
    }
    private void processor2(Document document){
        Elements element=document.select("div.info_mulu");
        Elements ele_chaplist=element.get(0).select("a");
        ArrayList<String> chaps= (ArrayList<String>) ele_chaplist.eachText();
        ArrayList<String>chaplinks= (ArrayList<String>) ele_chaplist.eachAttr("href");
        ArrayList<String>link_adjusted=new ArrayList<>();
        for (String link:chaplinks) {
            link_adjusted.add(link.replace("/",""));
        }
        for (int i = 0; i < chaps.size(); i++) {
            ChapList.add(chaps.get(i)+"(1)");
            ChapList.add(chaps.get(i)+"(2)");
            ChapLinkList.add(link_adjusted.get(i));
            ChapLinkList.add(link_adjusted.get(i).replace(".html","_2.html"));
        }
    }

}
