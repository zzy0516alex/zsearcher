package com.Z.NovelReader.Threads;

import android.graphics.Bitmap;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.myObjects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class GetCoverThread extends Thread {
    private String BookName;
    private File Dir;
    private String infoPageURL;
    private NovelRequire novelRequire;//书源规则类

    public GetCoverThread(Novels novel,NovelRequire novelRequire, File dir) {
        this.BookName=novel.getBookName();
        this.Dir=dir;
        this.infoPageURL=novel.getBookCatalogLink();
        this.novelRequire=novelRequire;
    }

    @Override
    public void run() {
        super.run();
        String rule_cover = novelRequire.getRuleBookInfo().getCoverUrl();
        if (rule_cover!=null) getCoverFromOriginSource(rule_cover);
        else getCoverFromQiDian();

    }

    private void getCoverFromOriginSource(String rule_cover) {
        try {
            Connection connect = Jsoup.connect(infoPageURL);
            connect.timeout(20000);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document = connect.get();
            NovelRuleAnalyzer coverSRCAnalyzer = new NovelRuleAnalyzer();
            List<String> src = coverSRCAnalyzer.getObjectFromElements(new Elements(document), rule_cover);
            if (src != null) {
                String picUrl=StringUtils.completeUrl(src.get(0),novelRequire.getBookSourceUrl());
                PictureThread thread = new PictureThread(picUrl);
                thread.start();
                Bitmap mbitmap = (Bitmap) thread.getMyBitmap();
                savePic(mbitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCoverFromQiDian() {
        String url="https://www.qidian.com/search?kw="+BookName;
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements=document.select("div.book-img-box");
            Elements ele_book_name=document.select("div.book-mid-info");
            String book_name=ele_book_name.get(0).select("a").get(0).text();
            if (StringUtils.compareStrings(book_name,BookName)>50) {
                String pic = elements.get(0).select("img").attr("src");
                String picUrl = "https:" + pic;
                PictureThread thread = new PictureThread(picUrl);
                thread.start();
                Bitmap mbitmap = (Bitmap) thread.getMyBitmap();
                savePic(mbitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePic(Bitmap mbitmap) {
        File picIn=new File(Dir+"/ZsearchRes/BookReserve/"+ BookName+"/cover.png" );
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(picIn);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        mbitmap.compress(Bitmap.CompressFormat.PNG,100,fot);
        try{
            assert fot != null;
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
