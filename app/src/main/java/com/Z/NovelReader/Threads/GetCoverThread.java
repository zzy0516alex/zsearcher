package com.Z.NovelReader.Threads;

import android.graphics.Bitmap;

import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GetCoverThread extends Thread {
    private String BookName;
    private String infoPageURL;
    private NovelRequire novelRequire;//书源规则类
    private String coverPath;

    /**
     *
     * @param novel bookName,bookInfoLink required
     * @param novelRequire
     * @param output_path 输出封面的路径
     */
    public GetCoverThread(Novels novel,NovelRequire novelRequire,String output_path) {
        this.BookName=novel.getBookName();
        this.infoPageURL=novel.getBookInfoLink();
        this.novelRequire=novelRequire;
        this.coverPath = output_path;
    }

    @Override
    public void run() {
        super.run();
        if (novelRequire.getRuleBookInfo() == null)return;
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
            if (src != null && src.size() != 0) {
                String picUrl=StringUtils.completeUrl(src.get(0),novelRequire.getBookSourceUrl());
                PictureThread thread = new PictureThread(picUrl);
                thread.start();
                Bitmap mbitmap = (Bitmap) thread.getMyBitmap();
                if (mbitmap!=null)
                    FileIOUtils.saveBitmap(coverPath,mbitmap);
            }else {
                getCoverFromQiDian();
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
                if (mbitmap!=null)
                    FileIOUtils.saveBitmap(coverPath,mbitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
