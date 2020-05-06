package com.example.helloworld.Threads;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ContentTextThread extends Thread {
    private String url;
    private File Dir;
    private String BookName;

    public ContentTextThread(String url,String book_name,File Dir) {
        this.url = url;
        this.Dir=Dir;
        this.BookName=book_name;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            String doc=document.select("div#content").toString();
            String temp1=doc.replace("\n","");
            String temp2=temp1.replace("&nbsp;"," ");
            String[] temp_text=temp2.split("<br>");
            StringBuilder text=new StringBuilder();
            for (int i = 2; i < temp_text.length; i=i+2) {
                text.append(temp_text[i]);
                text.append('\n');
            }
            String content=text.toString().replace("</div>","");
            WriteTXT(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
