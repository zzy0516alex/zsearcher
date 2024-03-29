package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.StorageUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovelUpdateThread extends BasicHandlerThread {

    private NovelRequire novelRequire;
    private Novels novel;

    private List<String> catalogLink;
    private ExecutorService threadPool;

//    private Context context;
//    private String broadcast_action;

    public NovelUpdateThread(NovelRequire novelRequire, Novels novel) {
        this.novelRequire = novelRequire;
        this.novel = novel;
        threadPool = Executors.newFixedThreadPool(15);
        catalogLink = new ArrayList<>();
    }
//    public void setCatalogLinkCallback(Context context,String action){
//        this.context = context;
//        this.broadcast_action = action;
//    }

    @Override
    public void run() {
        super.run();
        if (novelRequire==null){
            Log.d("novel update thread","novel require is null");
            report(ERROR_OCCUR);
            return;
        }
        File catalog_link_file = new File(StorageUtils.getBookCatalogLinkPath(novel.getBookName(),novel.getWriter()));
        if (!catalog_link_file.exists()){
            //目录链接缺失，重新获取
            Log.d("novel update thread:"+novel.getBookName(),"重新获取目录链接");
            String subTocUrl = novelRequire.getRuleToc().getNextTocUrl();
            if (subTocUrl!=null && !"".equals(subTocUrl)) {
                //目录全部缺失，重新下载
                callback(WAITING_KEY_FILES,novel);
            }else {
                catalogLink.add(novel.getBookCatalogLink());
                FileIOUtils.WriteList(
                        StorageUtils.getBookCatalogLinkPath(novel.getBookName(),novel.getWriter()),
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
                        novelRequire, novel,true);
                subCatalogLinkThread.setOutputParams(
                        StorageUtils.getBookCatalogLinkPath(novel.getBookName(),novel.getWriter()));
                subCatalogLinkThread.start();
            }

            //2.更新catalog
            checkFileAndUpdateCatalog();
        }
    }

    private void checkFileAndUpdateCatalog() {
        File temp_catalogs = new File(StorageUtils.getSubCatalogDir(novel.getBookName(),novel.getWriter()));
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
        File Folder =new File(StorageUtils.getSubCatalogDir(novel.getBookName(),novel.getWriter()));
        if(!Folder.exists()){
            Folder.mkdir();
        }

        CountDownLatch countDownLatch = new CountDownLatch(total_count-start_sequence);
        JoinCatalogThread joinCatalogThread = new JoinCatalogThread(total_count,
                StorageUtils.getSubCatalogDir(novel.getBookName(),novel.getWriter()),
                StorageUtils.getBookCatalogPath(novel.getBookName(),novel.getWriter()),false);
        joinCatalogThread.setHandler(getHandler());
        joinCatalogThread.setCountDownLatch(countDownLatch);
        joinCatalogThread.start();
        for (int i = start_sequence; i < total_count; i++) {
            GetCatalogThread catalogThread = new GetCatalogThread(catalogLink.get(i),novelRequire,novel,i);
            catalogThread.setOutput(StorageUtils.getSubCatalogPath(novel.getBookName(),novel.getWriter(),i));
            catalogThread.setCountDownLatch(countDownLatch);
            threadPool.execute(catalogThread);
        }
    }

    public static class NovelUpdaterHandler extends Handler {
        private int fail_counter=0;
        private int success_counter=0;
        private NovelUpdateListener novelUpdateListener;

        public NovelUpdaterHandler(){}

        public void setOverride(NovelUpdateListener novelUpdateListener) {
            this.novelUpdateListener = novelUpdateListener;
        }
        public void clearCounter(){
            fail_counter=0;
            success_counter=0;
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                switch (msg.what) {
                    case NovelUpdateThread.PROCESS_DONE:
                        Log.d("NovelUpdaterHandler", "成功");
                        success_counter++;
                        break;
                    case WAITING_KEY_FILES:
                        fail_counter++;
                        novelUpdateListener.needRecoverAll((Novels) msg.obj);
                        break;
                    case NovelUpdateThread.ERROR_OCCUR:
                        fail_counter++;
                        break;
                    default:
                }
            }
            novelUpdateListener.handle(msg,success_counter,fail_counter);
        }
        public interface NovelUpdateListener {
            void handle(Message msg,int Success,int Fail);
            void needRecoverAll(Novels novel);
        }
    }
}
