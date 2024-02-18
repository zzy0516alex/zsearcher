package com.Z.NovelReader.Threads;

import android.util.Log;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileOperateUtils;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Utils.StorageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;

public class JoinCatalogThread extends BasicHandlerThread {

    private String inputPath;
    private String outputPath;
    private int subCatalog_num=1;
    private boolean isClearTemp=false;
    private CountDownLatch countDownLatch;
    private int sourceID = -1;

//    private Context context;
//    private Handler handler;
//    private String intent_content;
//    private int message_what;
//    private String bookname;
    private NovelCatalog joined_catalog;

    /**
     *
     * @param subCatalog_num 子目录数
     * @param input_path 需要读取的路径：临时子目录文件夹
     * @param output_path 需要输出的路径: 完整目录文件
     * @param isClearTemp 是否清除临时子目录文件
     */
    public JoinCatalogThread(int subCatalog_num, String input_path, String output_path, boolean isClearTemp) {
        this.subCatalog_num = subCatalog_num;
        this.inputPath = input_path;
        this.outputPath = output_path;
        joined_catalog = new NovelCatalog();
        this.isClearTemp = isClearTemp;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    //    public void setBroadcast(Context context,String action) {
//        this.context = context;
//        this.intent_content = action;
//    }
//
//    public void setHandler(Handler handler,int what,String which) {
//        this.handler = handler;
//        message_what = what;
//        bookname = which;
//    }

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
//            String sub_catalog_path = inputPath + i +".txt";
            NovelCatalog sub_catalog = null;
            try {
                sub_catalog = FileIOUtils.readCatalog(StorageUtils.getSubCatalogPath(inputPath,i));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            joined_catalog.addAll(sub_catalog);
        }
        FileIOUtils.writeCatalog(outputPath,joined_catalog);
        if (isClearTemp) FileOperateUtils.deleteAllFiles(
                new File(inputPath));
        Log.d("JoinCatalogThread","合并目录完成:"+outputPath);
        MapElement element = new MapElement(sourceID,joined_catalog);
        callback(PROCESS_DONE,element);


//        if (context!=null) {
//            for (int i = 0; i < 5; i++) {
//                context.sendBroadcast(new Intent(intent_content));
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        if (handler!=null){
//            Message message = handler.obtainMessage();
//            message.what = message_what;
//            message.obj = bookname;
//            handler.sendMessage(message);
//        }
    }
}
