package com.Z.NovelReader.Threads;

import android.content.Context;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.SearchQuery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class NovelSourceGetterThread extends Thread {

    public static final String NOVEL_SOURCE_URL="http://yck.mumuceo.com/yuedu/shuyuan/index.html";
    private Context context;
    private String URL;

    public NovelSourceGetterThread(Context context,String url) {
        this.context = context;
        this.URL=url;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .followRedirects(true)
                    .timeout(50000)
                    .ignoreContentType(true).get();
            NovelRequire novelRequire = NovelRequire.getNovelRequireBean(document.text());
            NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(context);
            sourceDBTools.InsertNovelSources(novelRequire);
//            sourceDBTools.UpdateSourceVisibility(3,true);
//            sourceDBTools.getSearchUrlList(new NovelSourceDBTools.QueryListener() {
//                @Override
//                public void onResultBack(Object object) {
//                    List<SearchQuery>searchList= (List<SearchQuery>) object;
//                    System.out.println("searchList = " + searchList);
//                }
//            });
//            sourceDBTools.getNovelRequireById(1, new NovelSourceDBTools.QueryListener() {
//                @Override
//                public void onResultBack(Object object) {
//                    NovelRequire require= (NovelRequire) object;
//                    System.out.println("require = " + require);
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
