package com.example.helloworld.Threads;

import android.graphics.Bitmap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddBookThread extends Thread {
    private String BookName;
    private File Dir;
    private Bitmap mbitmap;
    public AddBookThread(String BookName,File dir) {
        this.BookName=BookName;
        this.Dir=dir;
    }

    @Override
    public void run() {
        super.run();
        String url="https://www.qidian.com/search?kw="+BookName;
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements=document.select("div.book-img-box");
            String pic=elements.get(0).select("img").attr("src");
            String picUrl="https:"+pic;
            PictureThread thread=new PictureThread(picUrl);
            thread.start();
            mbitmap= (Bitmap) thread.getMyBitmap();
            File picIn=new File(Dir+"/ZsearchRes/BookCovers/"+ BookName+".png" );
                        FileOutputStream fot=null;
                        try {
                            fot=new FileOutputStream(picIn);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                        mbitmap.compress(Bitmap.CompressFormat.PNG,100,fot);
                        try{
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
