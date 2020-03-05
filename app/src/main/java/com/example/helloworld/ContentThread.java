package com.example.helloworld;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ContentThread extends Thread {
    private String url;
    private Object firstchap;
    private Object lastchap;
    private Object newUrl;
    public ContentThread(String url) {
        this.url=url;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            Elements elements=document.select("div.box_con");
            Element ele=elements.get(1).select("a").first();
            Element ele2=elements.get(1).select("a").last();
            String lasturl=ele2.attr("href");
            String contentUrl=ele.attr("href");
            String a=contentUrl.split("\\.")[0];
            String b=lasturl.split("\\.")[0];
            firstchap=Integer.parseInt(a);
            lastchap=Integer.parseInt(b);
            //firstchap= Integer.parseInt(contentUrl.split("\\.")[0]);
            //lastchap=Integer.parseInt(lasturl.split("\\.")[0]);
            newUrl=url+contentUrl;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getFirstchap(){
        try {
            this.join();
            return firstchap;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
    public Object getLastchap(){
        try {
            this.join();
            return lastchap;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
    public Object getContentUrl(){
        try {
            this.join();
            return newUrl;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
}
