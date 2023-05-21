package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Processors.CommonUrlProcessor;
import com.Z.NovelReader.Processors.JavaScriptEngine;
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
    private Document document;

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
            document = connect.get();
//            NovelRuleAnalyzer catalogUrlAnalyzer=new NovelRuleAnalyzer();
//            catalogUrlAnalyzer.setEngine(createJsEngine());
//            List<String> urls = catalogUrlAnalyzer.getObjectFromElements(new Elements(document),
//                    novelRequire.getRuleBookInfo().getTocUrl());
            String catalog_url = CommonUrlProcessor.getUrl(document, novelRequire, novelRequire.getRuleBookInfo().getTocUrl());
            if (catalog_url!=null) {
                bookCatalogLink = catalog_url;
                Log.d("catalog url thread","目录链接："+bookCatalogLink);
                MapElement element = new MapElement(novelRequire.getId(),bookCatalogLink);
                callback(PROCESS_DONE,element);
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

    public JavaScriptEngine createJsEngine(){
        JavaScriptEngine engine = new JavaScriptEngine(novelRequire);
        engine.preDefine("baseUrl",bookInfoLink);
        engine.preDefine("result",document.toString());
        return engine;
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
