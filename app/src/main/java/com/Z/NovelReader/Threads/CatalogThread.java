package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.Processors.CatalogProcessor;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class CatalogThread extends Thread {

    //result back
    private ArrayList<String> ChapList;
    private ArrayList<String> ChapLinkList;
    private NovelCatalog result;
    //construct params
    private String url;
    private NovelRequire novelRequire;
    private boolean isOutput =false;//是否输出到文件
    private boolean isCallBack=false;//是否直接回调
    //output params
    private String sub_path;//"/ZsearchRes"+sub_path 例如：sub_path = "/BookReserve/bookname"
    private File Dir;
    private int output_sequence = 0;

    //call back params
    private android.os.Handler mHandler;
    public static final int CATALOG_OBTAIN_SUCCESS =0X0;
    public static final int CATALOG_OBTAIN_FAILED =0X1;
    public static final int NOVEL_SOURCE_NO_FOUND=0X2;//todo 考虑书源被删除的情况
    public static final int AWAIT_INTERRUPTED=0X3;

    //concurrent param
    private CountDownLatch countDownLatch;

    private int reserve_count=0;

    public CatalogThread(String url, NovelRequire novelRequire) {
        this.url=url;
        this.novelRequire=novelRequire;
    }

    public void setHandler(@NonNull Handler mHandler) {
        this.mHandler = mHandler;
        this.isCallBack=true;
    }

    /**
     * 设置文件输出参数
     * @param sub_path "/BookReserve/"+bookname
     * @param sequence 目录序列号
     */
    public void setOutputParams(String sub_path,int...sequence) {
        this.sub_path=sub_path;
        Dir= MyApplication.getExternalDir();
        if (sequence!=null && sequence.length!=0)output_sequence = sequence[0];
        this.isOutput =true;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        super.run();
        ChapList=new ArrayList<>();
        ChapLinkList=new ArrayList<>();
        try {
            Document document=Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                    //.ignoreHttpErrors(true)
                    .followRedirects(true)
                    .timeout(50000)
                    .ignoreContentType(true).get();
            result = CatalogProcessor.getCatalog(document, novelRequire);
            if(!isOutput && isCallBack){
                //返回结果
                Message message=mHandler.obtainMessage();
                message.what = CATALOG_OBTAIN_SUCCESS;
                message.obj= result;
                mHandler.sendMessage(message);
            }
        }catch (SocketTimeoutException e){
            FileIOUtils.WriteErrReport(Dir,e,url);
            if (isOutput) isOutput =false;
            if (isCallBack){
                Message message = mHandler.obtainMessage();
                message.what = CATALOG_OBTAIN_FAILED;
                mHandler.sendMessage(message);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            FileIOUtils.WriteErrReport(Dir,e,url);
            reserve_count++;
            if (reserve_count<3) {
                try {
                    Thread.sleep(10000);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                run();//重试
            }
            else {
                if (isOutput) isOutput =false;
                //失败
                if (isCallBack){
                    Message message = mHandler.obtainMessage();
                    message.what = CATALOG_OBTAIN_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        }
        //需要输出到文件
        if(isOutput){
            isOutput=false;

            File Folder =new File(Dir+"/ZsearchRes"+sub_path+"/","temp_catalogs");
            if(!Folder.exists()){
                Folder.mkdir();
            }

            FileIOUtils.WriteCatalog(Dir,"/ZsearchRes"+sub_path+"/temp_catalogs/"+
                    output_sequence+".txt",result);
            Log.d("Catalog Thread","生成子目录："+"/ZsearchRes"+sub_path+"/temp_catalogs/"+
                    output_sequence+".txt");

            if (countDownLatch!=null)countDownLatch.countDown();
            //成功(isCallBack && isOutPut)
            if (isCallBack){
                Message message = mHandler.obtainMessage();
                if (result.getSize()!=0) {
                    message.what = CATALOG_OBTAIN_SUCCESS;
                    message.obj = result;
                    mHandler.sendMessage(message);
                }else {
                    Log.d("catalog thread","catalog is empty");
                    message.what = CATALOG_OBTAIN_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        }

    }


    public static class ContentUrlHandler extends Handler{
        ContentUrlListener listener;

        public ContentUrlHandler(ContentUrlListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==CATALOG_OBTAIN_SUCCESS){
                NovelCatalog catalog = (NovelCatalog) msg.obj;
                NovelCatalog currentChap = new NovelCatalog();
                currentChap.add(catalog.getTitle().get(0),
                        catalog.getLink().get(0));
                listener.onSuccess(currentChap);
            }
            else {
                listener.onError();
            }
        }
    }
    public interface ContentUrlListener{
        void onSuccess(NovelCatalog currentChap);
        void onError();
    }

}
