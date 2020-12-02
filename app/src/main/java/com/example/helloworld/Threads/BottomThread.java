package com.example.helloworld.Threads;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class BottomThread extends Thread {
    private String url;
    private Object pastURL;
    private Object nextURL;
    private Object catalog;
    private Object CTitle;
    private Object BookName;
    public BottomThread(String url) {
        this.url=url;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            Elements elements=document.select("div.bottem");
            Elements ele1=elements.get(0).select("a").eq(1);
            Elements ele2=elements.get(0).select("a").eq(3);
            Elements ele3=elements.get(0).select("a").eq(2);
            Elements booknameElement=document.select("div.bookname");
            Elements booknameEle=booknameElement.get(0).select("h1");
            Elements top=document.select("div.con_top");
            Elements name_list=top.select("a");
            Elements name=name_list.eq(name_list.size()-1);
            String book_name=name.text();
            String currentTitle=booknameEle.text();
            String urlPast=ele1.attr("href");
            String urlNext=ele2.attr("href");
            String urlCatalog=ele3.attr("href");
            pastURL=urlPast;
            nextURL=urlNext;
            catalog=urlCatalog;
            CTitle=currentTitle;
            BookName=book_name;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Object getPastURL(){
        try {
            this.join();
            return pastURL;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
    public Object getNextURL(){
        try {
            this.join();
            return nextURL;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
    public Object getCatalog(){
        try {
            this.join();
            return catalog;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
    public Object getCTitle(){
        try {
            this.join();
            return CTitle;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Object getBookName(){
        try {
            this.join();
            return BookName;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
