package com.Z.NovelReader.Threads;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.CatalogProcessor;
import com.Z.NovelReader.Utils.FileIOUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

public class GetCatalogThread extends BasicHandlerThread {

    private String url;
    private NovelRequire novelRequire;
    private Novels novel;
    private int sub_sequence = 0;
    private MapElement sub_catalog;//enabled only sequense!=-1
    private NovelCatalog catalog;
    private String sub_catalog_path;
    private boolean needOutput = false;
    private boolean isSuccess = false;
    private CountDownLatch countDownLatch;
    private int MaxReconnectNum = 10;
    private int reconnectCounter = 0;

    public GetCatalogThread(String url, NovelRequire novelRequire, Novels novel, int sequence) {
        this.url = url;
        this.novelRequire = novelRequire;
        this.novel = novel;
        this.sub_sequence = sequence;
    }

    /**
     * 设置输出路径
     * @param output_path ="$extDir$/ZsearchRes/$book name$/catalog.txt"
     * {@link com.Z.NovelReader.Utils.StorageUtils#getBookCatalogPath(String, String)}
     */
    public void setOutput(String output_path){
        this.sub_catalog_path = output_path;
        needOutput = true;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        try {
            LocalDateTime beginTime = LocalDateTime.now();
            Document document= Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .timeout(80000)
                    .ignoreContentType(true).get();
            long cost_time = Duration.between(beginTime, LocalDateTime.now()).toMillis();
            catalog = CatalogProcessor.getCatalog(document, novelRequire, novel);
            catalog.setRespondingTime(cost_time);
            if (catalog.getSize()!=0)isSuccess = true;
            sub_catalog = new MapElement(sub_sequence,catalog);
            callback(PROCESS_DONE,sub_catalog);
        }
        catch (SocketException e){
            if (e.getMessage().contains("reset")){
                if (reconnectCounter<MaxReconnectNum) {
                    run();reconnectCounter++;
                }else {
                    report(NO_INTERNET);
                }
            }
        }
        catch (SocketTimeoutException e){
            e.printStackTrace();
            report(TARGET_NOT_FOUND);
            FileIOUtils.WriteErrReport(e,url);
        }
        catch (IOException e){
            e.printStackTrace();
            report(NO_INTERNET);
        }
        catch (Exception e){
            e.printStackTrace();
            report(PROCESSOR_ERROR);
        }

        if (needOutput && isSuccess){
            FileIOUtils.writeCatalog(sub_catalog_path,catalog);
        }
        if (countDownLatch!=null)countDownLatch.countDown();
    }
}
