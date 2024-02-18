package com.Z.NovelReader.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.Z.NovelReader.Basic.BasicCounterHandler;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Threads.ContentDownloadThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChapDownloadService extends Service {
    private ChapDownloadServiceBinder binder = new ChapDownloadServiceBinder();
    private ExecutorService threadPool;//线程池,用于章节下载
    private List<String> download_links;
    private NovelChap currentChap;
    private NovelCatalog currentCatalog;
    private ChapDownloadListener downloadListener;

    public ChapDownloadService() {
    }

    public class ChapDownloadServiceBinder extends Binder{
        public ChapDownloadService getService(){return ChapDownloadService.this;}
    }

    public interface ChapDownloadListener{
        void onChapDownloaded(int index);
    }

    public void setDownloadListener(ChapDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        threadPool = Executors.newFixedThreadPool(16);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        download_links = intent.getStringArrayListExtra("DownloadLinks");
        currentChap = (NovelChap) intent.getSerializableExtra("NovelChap");
        currentCatalog = (NovelCatalog) intent.getSerializableExtra("Catalog");

        if(download_links==null || currentChap==null || currentCatalog==null)
            throw new RuntimeException("必要数据缺失");

        //检查文件夹
        File download_folder = new File(StorageUtils.getDownloadContentDir(currentChap.getBookName(),currentChap.getWriter()));
        if(!download_folder.exists())download_folder.mkdir();

        BasicCounterHandler<Integer> handler = new BasicCounterHandler<>(new BasicCounterHandler.BasicCounterHandlerListener<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                currentCatalog.setDownloaded(result,true);
                downloadListener.onChapDownloaded(result);
                Log.d("Content download","章节"+result+"下载完毕");
            }

            @Override
            public void onError() {

            }

            @Override
            public void onAllProcessDone(ArrayList<Integer> results) {
                FileIOUtils.writeCatalog(StorageUtils.getBookCatalogPath(currentChap.getBookName(),currentChap.getWriter()),currentCatalog);
            }
        });
        handler.setIgnoreResult(true);
        handler.setTotal_count(download_links.size(),true);
        for (String link : download_links) {
            ArrayList<String> linkList = currentCatalog.getLinkList();
            int index = linkList.indexOf(link);
            ContentDownloadThread thread = new ContentDownloadThread(link,currentChap.getNovelRequire(), currentChap, currentChap.getContentRootLink());
            thread.setCatalogLinks(linkList);
            thread.setOutputToDownloadFile(StorageUtils.getDownloadContentPath(currentChap.getBookName(),currentChap.getWriter(),index));
            thread.setHandler(handler);
            threadPool.execute(thread);
        }

        return super.onStartCommand(intent, flags, startId);
    }
}