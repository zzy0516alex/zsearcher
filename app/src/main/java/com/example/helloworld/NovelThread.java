package com.example.helloworld;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class NovelThread extends Thread {
    private String url;
    private String code;
    private Object getNovels;
    private Object getcontexts;
    private boolean isError=false;
    private boolean Found=false;
    private Handler handler;


    public NovelThread(String url, String code) {
        this.url=url;
        this.code=code;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        List<String> N_novels;
        N_novels=new ArrayList<>();
        List<String> C_novels;
        C_novels=new ArrayList<>();
        try {
            Document doc= Jsoup.connect(url+code).get();
            Elements elements =doc.select("div.novelslistss");
            Elements elements2=doc.select("div.box_con");
            if (elements.size()!=0) {
                Elements lis = elements.get(0).select("li");
                if(lis.size()!=0) {
                    Elements titles = lis.select("a");
                    List<String> novelnames = titles.eachText();
                    List<String> novelcontents = titles.eachAttr("href");
                    for (int i = 0; i < novelnames.size(); ) {
                        N_novels.add(novelnames.get(i));
                        C_novels.add(novelcontents.get(i));
                        i = i + 2;
                    }
                    Log.e("内容", C_novels.get(0));
                    getNovels = N_novels;
                    getcontexts = C_novels;
                    Found = true;
                }
                else {
                    Found=false;
                }
            }
            else if(elements2.size()!=0){
                Elements siglebook=elements2.get(0).select("div.fr");
                Elements siglebook2=elements2.get(0).select("h1");
                N_novels.add(siglebook2.text());
                String link=siglebook.select("a").attr("href");
                C_novels.add(link.replace("m.",""));
                getNovels = N_novels;
                getcontexts = C_novels;
                Found = true;
            }
            else{
                Found=false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err1","no internet");
            isError=true;
        }
        Message message=handler.obtainMessage();
        message.what=0X2;
        handler.sendMessage(message);
    }


    public boolean getError(){
            return isError;
    }
    public boolean isFound(){
            return Found;
    }
    public Object getNovelnames(){
            return getNovels;
    }
    public Object getNovelcontents(){
            return  getcontexts;
    }
}
