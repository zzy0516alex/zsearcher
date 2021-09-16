package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.myObjects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContentTextThread extends Thread {
    private String url;
    private File Dir;
    private String BookName;
    private NovelRequire novelRequire;
    private String content;
    int err_count=0;
    boolean needWriteFile=true;

    public ContentTextThread(String url,String book_name,File Dir,boolean needWriteFile) {
        this.url = url;
        this.Dir=Dir;
        this.BookName=book_name;
        this.needWriteFile=needWriteFile;
    }

    public void setNovelRequire(NovelRequire novelRequire) {
        this.novelRequire = novelRequire;
    }


    @Override
    public void run() {
        super.run();
        try {
            Connection connect = Jsoup.connect(url);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();
            NovelRuleAnalyzer contentAnalyzer=new NovelRuleAnalyzer();
            List<String> content_list;
            if (novelRequire!=null)content_list = contentAnalyzer.getObjectFromElements(new Elements(document),
                    novelRequire.getRuleContent().getContent());
            else throw new RuntimeException("novel require is null");
            if (content_list!=null) {
                content=content_list.get(0);
                if (needWriteFile)
                    FileIOUtils.WriteTXT(Dir, "/ZsearchRes/BookReserve/" + BookName, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err","text save error");
            err_count++;
            if (err_count<3)run();
            else {
                content="章节缓存出错:IO异常";
                if (needWriteFile)
                    FileIOUtils.WriteTXT(Dir,"/ZsearchRes/BookReserve/"+BookName,content);
            }
        } catch (RuntimeException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            content="章节缓存出错:书源解析异常";
            if (needWriteFile)
                FileIOUtils.WriteTXT(Dir,"/ZsearchRes/BookReserve/"+BookName,content);
        }

    }

    public String getContent() {
        try {
            this.join();
            return content;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
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

}
