package com.example.helloworld.Threads;

import android.os.Handler;
import android.os.Message;

import com.example.helloworld.myObjects.NovelChap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


public class ChapGetterThread extends Thread {
    private boolean has_lastChap;
    private boolean has_nextChap;
    private int LinkType;
    private String url;
    private String last_link;
    private String next_link;
    private String chap_title;
    private String chap_content;
    private int current_chap=-1;
    private NovelChap chap;
    private Handler mHandler;

    public ChapGetterThread(String url, Handler handler) {
        this.url = url;
        mHandler=handler;
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

    public void setCurrent_chap(int current_chap) {
        this.current_chap = current_chap;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            grabContent(document);
            grabChapInfo(document);
            chap=new NovelChap(chap_title,chap_content);
            if (has_lastChap)chap.setLast_link(last_link);
            if (has_nextChap)chap.setNext_link(next_link);
            if (current_chap!=-1)chap.setCurrent_chapter(current_chap);
            Message message=mHandler.obtainMessage();
            message.obj=chap;
            mHandler.sendMessage(message);
        }catch (IOException e){
            e.printStackTrace();
            run();
        }

    }

    private void grabContent(Document document) {
            String doc=document.select("div#content").toString();
            String temp1=doc.replace("\n","");
            String temp2=temp1.replace("&nbsp;"," ");
            String[] temp_text=temp2.split("<br>");
            StringBuilder text=new StringBuilder();
            for (int i = 2; i < temp_text.length; i=i+2) {
                text.append(temp_text[i]);
                text.append('\n');
            }
            chap_content=text.toString().replace("</div>","");
    }
    private void grabChapInfo(Document document){
        Elements elements=document.select("div.bottem");
        Elements ele1=elements.get(0).select("a").eq(1);
        Elements ele2=elements.get(0).select("a").eq(3);
        Elements booknameElement=document.select("div.bookname");
        Elements booknameEle=booknameElement.get(0).select("h1");
        String currentTitle=booknameEle.text();
        if (has_lastChap)last_link=ele1.attr("href");
        if (has_nextChap)next_link=ele2.attr("href");
        chap_title=currentTitle;
    }
}
