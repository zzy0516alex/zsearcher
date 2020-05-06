package com.example.helloworld.Threads;

import android.os.Handler;
import android.os.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CatalogThread extends Thread {
    private String url;
    private List<String> ChapList;
    private List<String> ChapLinkList;
    private HashMap<String,List<String >> result;
    private boolean if_output=false;
    private String BookName;
    private File Dir;
    private android.os.Handler mHandler;
    public CatalogThread(String url) {
        this.url=url;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setIf_output(boolean if_output, String bookname, File dir) {
        this.if_output = if_output;
        BookName=bookname;
        Dir=dir;
    }

    @Override
    public void run() {
        super.run();
        result=new HashMap<>();
        try {
            Document document= Jsoup.connect(url).get();
            Elements elements=document.select("div.box_con");
            Elements titles=elements.get(1).select("a");
            List<String>Chaps=titles.eachText();
            List<String>ChapsLink=titles.eachAttr("href");
            ChapList=Chaps;
            ChapLinkList=ChapsLink;
            result.put("ChapName",ChapList);
            result.put("ChapLink",ChapLinkList);
            if(!if_output){
                Message message=mHandler.obtainMessage();
                message.obj=result;
                mHandler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(if_output){
            StringBuilder content=new StringBuilder();
            for (int i = 0; i < ChapList.size(); i++) {
                content.append(ChapList.get(i));
                content.append('\n');
                content.append(url+ChapLinkList.get(i));
                if(i!=ChapList.size()-1)content.append('\n');
            }
            WriteTXT(content.toString());
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

    public void WriteTXT(String content){
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+"_catalog.txt" );
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(mk_txt);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        try{
            fot.write(content.getBytes());
            fot.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fot!=null){
                try {
                    fot.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
