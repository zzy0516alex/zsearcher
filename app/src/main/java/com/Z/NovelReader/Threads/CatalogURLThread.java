package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * 通过book info link 获取book catalog link
 */
public class CatalogURLThread extends BasicHandlerThread {

    private String bookInfoLink;
    private String bookCatalogLink;
    private NovelRequire novelRequire;

    public CatalogURLThread(String bookInfoLink, NovelRequire novelRequire) {
        this.bookInfoLink = bookInfoLink;
        this.novelRequire = novelRequire;
    }


    @Override
    public void run() {
        super.run();
        try {
            Connection connect = Jsoup.connect(bookInfoLink);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();

            NovelRuleAnalyzer catalogUrlAnalyzer=new NovelRuleAnalyzer();
            List<String> urls = catalogUrlAnalyzer.getObjectFromElements(new Elements(document),
                    novelRequire.getRuleBookInfo().getTocUrl());
            if (urls!=null && urls.size()!=0) {
                bookCatalogLink = StringUtils.completeUrl(urls.get(0),
                        novelRequire.getBookSourceUrl());
                Log.d("catalog url thread","目录链接："+bookCatalogLink);
                callback(PROCESS_DONE,bookCatalogLink);
            }else {
                report(TARGET_NOT_FOUND);
            }
        } catch (IOException e) {
            e.printStackTrace();
            report(NO_INTERNET);
        } catch (Exception e){
            e.printStackTrace();
            report(PROCESSOR_ERROR);
        }

    }

    public static class CatalogUrlHandler extends Handler{
        CatalogUrlListener listener;

        public CatalogUrlHandler(CatalogUrlListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==PROCESS_DONE)
                listener.onSuccess((String) msg.obj);
            else {
                listener.onError(msg.what);
            }
        }
    }
    public interface CatalogUrlListener{
        void onSuccess(String catalog_url);
        void onError(int error_code);
    }
}
