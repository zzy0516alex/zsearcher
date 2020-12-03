package com.example.helloworld.Threads;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.helloworld.Utils.IOtxt;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ContentTextThread extends Thread {
    private String url;
    private File Dir;
    private String BookName;
    private NovelThread.TAG tag;
    private Context context;
    int err_count=0;

    public ContentTextThread(String url,String book_name,File Dir) {
        this.url = url;
        this.Dir=Dir;
        this.BookName=book_name;
    }

    public void setTag(NovelThread.TAG tag) {
        this.tag = tag;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        try {
            Connection connect = Jsoup.connect(url);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();
            String content = "";
            switch(tag){
                case BiQuGe:
                    content=getContent1(document);
                    break;
                case SiDaMingZhu:
                    content=getContent2(document);
                    break;
                default:
            }
            IOtxt.WriteTXT(Dir,BookName,content);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err","text save error");
            err_count++;
            if (err_count<3)run();
            else {
                Looper.prepare();
                Toast.makeText(context, "章节缓存失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }

    }

    public static String getContent1(Document document) {
        String doc=document.select("div#content").toString();
        String temp1=doc.replace("\n","");
        String temp2=temp1.replace("&nbsp;"," ");
        String[] temp_text=temp2.split("<br>");
        StringBuilder text=new StringBuilder();
        for (int i = 0; i < temp_text.length; i=i+2) {
            text.append(temp_text[i]);
            text.append('\n');
        }
        String content=text.toString().replace("<!--over--> </div>","");
        content=content.replace("<div id=\"content\"> <!--go-->","");
        return content;
    }

    public static String getContent2(Document document){
        StringBuilder text=new StringBuilder();
        Elements main_content=document.select("div.p-content");
        List<String>paragraph=main_content.select("p").eachText();
        for (String unit:paragraph) {
            text.append("       ");
            text.append(unit);
            text.append("\n");
            text.append("\n");
        }
        return text.toString();
    }

    public void WriteTXT(String content){
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+".txt" );
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
