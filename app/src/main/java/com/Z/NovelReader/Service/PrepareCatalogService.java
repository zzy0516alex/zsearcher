package com.Z.NovelReader.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.Z.NovelReader.Basic.BasicUpdaterBroadcast;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Threads.CatalogURLThread;
import com.Z.NovelReader.Threads.GetCatalogThread;
import com.Z.NovelReader.Threads.Handlers.GetCatalogHandler;
import com.Z.NovelReader.Threads.SubCatalogLinkThread;
import com.Z.NovelReader.Utils.FileIOUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrepareCatalogService extends Service {

    private PrepareCatalogServiceBinder binder = new PrepareCatalogServiceBinder();
    private CatalogServiceListener listener;
    private NovelRequire novelRequire;
    private SubCatalogLinkThread.SubCatalogHandler subCatalog_Handler;//处理多目录页的链接获取结果
    private GetCatalogHandler catalogHandler;
    private boolean isAllProcessDone = false;
    private NovelCatalog generalCatalog;
    private Novels Novel;
    private String catalogPath;
    private String catalogLinkPath;
    private ExecutorService threadPool;//目录获取线程池

    public PrepareCatalogService() {
    }
    public class PrepareCatalogServiceBinder extends Binder {
        public PrepareCatalogService getService(){
            return PrepareCatalogService.this;
        }
    }
    public interface CatalogServiceListener{
        void onError(String info,Novels target);
        void onProcessStart(String process_name);
        void onFirstChapReady(NovelCatalog chap);
        void onAllProcessDone(Novels novel);
    }

    public void setListener(CatalogServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * notice: 目录获取
     * 目录获取step1：是否存在目录页-> 1.1.存在：启动catalogURL线程进行爬取{@link CatalogURLThread}
     *                             1.2.不存在：直接将详情页链接设置为目录页链接
     * 目录获取step2:生成目录链接文件
     *      判断是否存在子目录-> 2.1.存在：启动子目录搜索线程生成文件{@link SubCatalogLinkThread}
     *                        2.2.不存在：直接将当前目录链接输出到文件
     * 目录获取step3：3.1.根据目录链接文件获取目录(可能是一个或者多个) {@link GetCatalogThread}
     *              3.2.目录获取后通过{@link GetCatalogHandler}存入Map中
     *              3.3.所有目录获取完成后将目录合并{@link GetCatalogHandler}
     *              3.4.该线程将目录合并后输出到“catalog.txt”，并通过广播通知目录已准备完毕
     *
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NovelSearchBean current_book = (NovelSearchBean) intent.getSerializableExtra("NovelSearch");
        Novels novel = (Novels) intent.getSerializableExtra("Novel");
        catalogPath = intent.getStringExtra("catalogPath");
        catalogLinkPath = intent.getStringExtra("catalogLinkPath");
        if (current_book==null && novel==null){
            listener.onError("书籍数据未输入",null);
            return super.onStartCommand(intent, flags, startId);
        }
        if (current_book == null){
            NovelRequire novelRule = (NovelRequire) intent.getSerializableExtra("NovelRule");
            this.Novel = novel;
            current_book = new NovelSearchBean(novel,novelRule);
        }
        if (this.Novel == null){
            this.Novel = new Novels(-1,current_book.getBookName(),current_book.getWriter(),0,0,current_book.getBookCatalogLink(),current_book.getBookInfoLink(),"",current_book.getNovelRule().getId(),0);
        }
        novelRequire = current_book.getNovelRule();
        //初始化线程池,最高并发15线程
        threadPool = Executors.newFixedThreadPool(15);

        NovelSearchBean finalCurrent_book = current_book;
        new Thread(() -> {
            Looper.prepare();
            if (novelRequire ==null) {
                Log.d("generateCatalogLinkList", "current_novelRequire is null");
                listener.onError("未读取到书源信息",Novel);
                return;
            }
            Log.d("CatalogService","service start in thread"+Thread.currentThread());
            initHandlers();

            listener.onProcessStart("获取子目录…");
            String subTocUrl = novelRequire.getRuleToc().getNextTocUrl();
            if (subTocUrl!=null && !"".equals(subTocUrl)) {
                //存在子目录
                SubCatalogLinkThread subCatalogLinkThread = new SubCatalogLinkThread(finalCurrent_book.getBookCatalogLink(),
                        novelRequire,false);
                subCatalogLinkThread.setOutputParams(catalogLinkPath);
                subCatalogLinkThread.setHandler(subCatalog_Handler);
                subCatalogLinkThread.start();
            }else {
                ArrayList<String> subCatalogLinkList = new ArrayList<>();
                subCatalogLinkList.add(finalCurrent_book.getBookCatalogLink());
                FileIOUtils.WriteList(catalogLinkPath, subCatalogLinkList,false);
                GetCatalogThread catalogThread = new GetCatalogThread(finalCurrent_book.getBookCatalogLink(),novelRequire,0);
                catalogHandler.setTotal_count(1);
                catalogThread.setHandler(catalogHandler);
                catalogThread.start();
            }

            Looper.loop();
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    void initHandlers(){
        subCatalog_Handler = new SubCatalogLinkThread.SubCatalogHandler(new SubCatalogLinkThread.SubCatalogListener() {
            @Override
            public void onSuccess(MapElement element) {
                List<String> result = (List<String>) element.value;
                if (result.size()==0)return;
                listener.onProcessStart("生成目录…");
                catalogHandler.setTotal_count(result.size());
                for (int i = 0; i < result.size(); i++) {
                    GetCatalogThread catalogThread = new GetCatalogThread(result.get(i),novelRequire,i);
                    catalogThread.setHandler(catalogHandler);
                    threadPool.execute(catalogThread);
                }
            }

            @Override
            public void onError() {
                listener.onError("获取子目录时遇到错误",Novel);
            }
        });

        catalogHandler = new GetCatalogHandler(new GetCatalogHandler.GetCatalogListener() {
            @Override
            public void onError() {
                listener.onError("获取子目录失败",Novel);
            }

            @Override
            public void onAllProcessDone(NovelCatalog catalog) {
                isAllProcessDone = true;
                generalCatalog = catalog;
                for (int i = 0; i < 10; i++) {
                    getApplicationContext().sendBroadcast(new Intent(BasicUpdaterBroadcast.CATALOG_NOVELSHOW));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (Novel !=null & listener!=null)listener.onAllProcessDone(Novel);
                stopSelf();
            }

            @Override
            public void onFirstChapReady(NovelCatalog chap) {
                listener.onFirstChapReady(chap);
            }
        });
        catalogHandler.setOutput(catalogPath);
        catalogHandler.setNSUpdater(new NovelSourceDBTools(getApplicationContext()),novelRequire);
    }
}