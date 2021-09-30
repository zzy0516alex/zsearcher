package com.Z.NovelReader.Threads;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.Z.NovelReader.BookShelfActivity;
import com.Z.NovelReader.NovelShowAcitivity;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Objects.beans.NovelCatalog;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class JoinCatalogThread extends Thread {

    private int subCatalog_num=1;
    private String subCatalog_path;
    private File Dir;
    private boolean isClearTemp=false;
    private CountDownLatch countDownLatch;

    private Context context;
    private Handler handler;
    private String intent_content;
    private int message_what;
    private String bookname;
    private NovelCatalog joined_catalog;

    public static final int NOVEL_SHOW = 0x0;
    public static final int BOOK_SHELF = 0x1;

    /**
     *
     * @param subCatalog_num 子目录数
     * @param subCatalog_path "/ZsearchRes"+subCatalog_path + "/temp_catalogs/"+ i +".txt"
     *                        "/ZsearchRes"+subCatalog_path+"/catalog.txt"
     * @param Dir 根目录
     */
    public JoinCatalogThread(int subCatalog_num, String subCatalog_path,File Dir,boolean isClearTemp) {
        this.subCatalog_num = subCatalog_num;
        this.subCatalog_path = subCatalog_path;
        this.Dir = Dir;
        joined_catalog = new NovelCatalog();
        this.isClearTemp = isClearTemp;
    }

    public void setBroadcast(Context context,int to_which) {
        this.context = context;
        switch(to_which){
            case NOVEL_SHOW:
                intent_content = NovelShowAcitivity.CATALOG_BROADCAST;
                break;
            case BOOK_SHELF:
                intent_content = BookShelfActivity.CATALOG_BROADCAST;
                break;
            default:
        }
    }

    public void setHandler(Handler handler,int what,String which) {
        this.handler = handler;
        message_what = what;
        bookname = which;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            if (countDownLatch!=null)countDownLatch.await();
            Log.d("join catalog thread","所有子目录就绪，开始生成完整目录");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.run();
        for (int i = 0; i < subCatalog_num; i++) {
            String path = "/ZsearchRes"+subCatalog_path+
                    "/temp_catalogs/"+ i +".txt";
            NovelCatalog sub_catalog = FileIOUtils.read_catalog(path, Dir);
            joined_catalog.addCatalog(sub_catalog);
        }
        FileIOUtils.WriteCatalog(Dir,"/ZsearchRes"+subCatalog_path+"/catalog.txt",joined_catalog);
        if (isClearTemp)FileUtils.deleteAllFiles(new File(Dir+"/ZsearchRes"+subCatalog_path + "/temp_catalogs"));
        Log.d("JoinCatalogThread","合并目录完成："+"/ZsearchRes"+subCatalog_path+"/catalog.txt");
        if (context!=null) {
            for (int i = 0; i < 5; i++) {
                context.sendBroadcast(new Intent(intent_content));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (handler!=null){
            Message message = handler.obtainMessage();
            message.what = message_what;
            message.obj = bookname;
            handler.sendMessage(message);
        }
    }
}
