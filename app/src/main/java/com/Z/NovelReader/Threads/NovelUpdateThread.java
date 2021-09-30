package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovelUpdateThread extends BasicHandlerThread {

    private File Dir;
    private NovelRequire novelRequire;
    private Novels novel;

    private List<String> catalogLink;
    private ExecutorService threadPool;

    public NovelUpdateThread(NovelRequire novelRequire, Novels novel) {
        Dir = MyApplication.getExternalDir();
        this.novelRequire = novelRequire;
        this.novel = novel;
        threadPool = Executors.newFixedThreadPool(15);
        catalogLink = new ArrayList<>();
    }

    @Override
    public void run() {
        super.run();
        if (novelRequire==null){
            Log.d("novel update thread","novel require is null");
            report(ERROR_OCCUR);
            return;
        }
        File catalog_link_file = new File(Dir+"/ZsearchRes/BookReserve/"+
                novel.getBookName()+"/catalog_link.txt");
        if (!catalog_link_file.exists()){
            //目录链接缺失，重新获取
            Log.d("novel update thread:"+novel.getBookName(),"重新获取目录链接");
            String subTocUrl = novelRequire.getRuleToc().getNextTocUrl();
            if (subTocUrl!=null && !"".equals(subTocUrl)) {
                //存在子目录
                SubCatalogLinkThread subCatalogLinkThread = new SubCatalogLinkThread(novel.getBookCatalogLink(),
                        novelRequire,false);
                subCatalogLinkThread.setOutputParams("/BookReserve/"+novel.getBookName()+"/catalog_link.txt");
                subCatalogLinkThread.start();
                report(ERROR_OCCUR);
            }else {
                catalogLink.add(novel.getBookCatalogLink());
                FileIOUtils.WriteList(Dir,
                        "/ZsearchRes/BookReserve/"+novel.getBookName()+"/catalog_link.txt",
                        catalogLink,false);
                //继续更新目录
                checkFileAndUpdateCatalog();
            }
        }else {
            //目录链接存在，同时更新catalog link 和catalog文件
            //1.更新catalog link
            Log.d("novel update thread:"+novel.getBookName(),"更新目录链接");
            catalogLink = FileIOUtils.read_list(catalog_link_file);
            if (catalogLink.size() == 0){
                Log.d("novel update thread","catalog link is empty");
                report(ERROR_OCCUR);
                return;
            }
            String subTocUrl = novelRequire.getRuleToc().getNextTocUrl();
            if (subTocUrl!=null && !"".equals(subTocUrl)) {
                SubCatalogLinkThread subCatalogLinkThread = new SubCatalogLinkThread(catalogLink.get(catalogLink.size() - 1),
                        novelRequire,true);
                subCatalogLinkThread.setOutputParams("/BookReserve/" + novel.getBookName() + "/catalog_link.txt");
                subCatalogLinkThread.start();
            }

            //2.更新catalog
            checkFileAndUpdateCatalog();
        }
    }

    private void checkFileAndUpdateCatalog() {
        File temp_catalogs = new File(Dir+"/ZsearchRes/BookReserve/"+
                novel.getBookName()+"/temp_catalogs");
        File[] files = temp_catalogs.listFiles();
        if (!temp_catalogs.exists()){
            //目录临时文件不存在，全部目录重新下载
            Log.d("novel update thread:"+novel.getBookName(),"更新全部目录");
            updateCatalog(0,catalogLink.size());
        }else if (files == null || files.length==0){
            Log.d("novel update thread:"+novel.getBookName(),"更新全部目录");
            updateCatalog(0,catalogLink.size());
        }else {
            //临时文件存在，按缺少的数量进行下载
            int exist_files = files.length;
            if (exist_files <= catalogLink.size()){
                //补上缺少的目录
                Log.d("novel update thread:"+novel.getBookName(),"补全目录");
                updateCatalog(exist_files-1,catalogLink.size());
            }else {
                Log.d("novel update thread","there is too many catalog temp files");
                report(ERROR_OCCUR);
            }
        }
    }

    private void updateCatalog(int start_sequence,int total_count) {
        CountDownLatch countDownLatch = new CountDownLatch(total_count-start_sequence);
        JoinCatalogThread joinCatalogThread = new JoinCatalogThread(total_count,
                "/BookReserve/"+novel.getBookName(),Dir,false);
        joinCatalogThread.setHandler(getHandler(),PROCESS_DONE,novel.getBookName());
        joinCatalogThread.setCountDownLatch(countDownLatch);
        joinCatalogThread.start();
        for (int i = start_sequence; i < total_count; i++) {
            CatalogThread catalogThread = new CatalogThread(catalogLink.get(i),
                    novelRequire);
            catalogThread.setOutputParams("/BookReserve/"+novel.getBookName(),i);
            catalogThread.setCountDownLatch(countDownLatch);
            threadPool.execute(catalogThread);
        }
    }

    public static class NovelUpdaterHandler<T> extends Handler {
        private final WeakReference<T> mActivity;
        private int fail_counter=0;
        private int success_counter=0;
        private NovelUpdateListener novelUpdateListener;

        public NovelUpdaterHandler(T activity) {
            mActivity = new WeakReference<T>(activity);
        }

        public void setOverride(NovelUpdateListener novelUpdateListener) {
            this.novelUpdateListener = novelUpdateListener;
        }
        public void clearCounter(){
            fail_counter=0;
            success_counter=0;
        }

        @Override
        public void handleMessage(Message msg) {
            T activity = mActivity.get();
            if (activity != null) {
                synchronized (this) {
                    switch (BasicHandlerThread.mergeEvents(msg.what)) {
                        case NovelUpdateThread.PROCESS_DONE:
                            Log.d("NovelUpdaterHandler", "成功：" + msg.obj);
                            success_counter++;
                            break;
                        case NovelUpdateThread.ERROR_OCCUR:
                            fail_counter++;
                            break;
                        default:
                    }
                }
                novelUpdateListener.handle(msg,success_counter,fail_counter);
            }
        }
        public interface NovelUpdateListener {
            void handle(Message msg,int Success,int Fail);
        }
    }
}
