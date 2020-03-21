package com.example.helloworld.Threads;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class CatalogThread extends Thread {
    private String url;
    private Object ChapList;
    private Object ChapLinkList;
    public CatalogThread(String url) {
        this.url=url;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            Elements elements=document.select("div.box_con");
            Elements titles=elements.get(1).select("a");
            List<String>Chaps=titles.eachText();
            List<String>ChapsLink=titles.eachAttr("href");
            ChapList=Chaps;
            ChapLinkList=ChapsLink;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getChapList(){
        try {
            this.join();
            return ChapList;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Object getChapLinkList(){
        try {
            this.join();
            return ChapLinkList;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
