package com.example.helloworld.Threads;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.myObjects.NovelCatalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CatalogThread extends Thread {
    private String url;
    private NovelThread.TAG tag;
    private ArrayList<String> ChapList;
    private ArrayList<String> ChapLinkList;
    private NovelCatalog result;
    private boolean isOutput =false;
    private boolean isCallBack=false;
    private String BookName;
    private File Dir;
    private android.os.Handler mHandler;
    public static final int CATALOG_UPDATED=0;
    public static final int CATALOG_UPDATE_FAILED=1;
    private int reserve_count=0;
    public CatalogThread(String url, NovelThread.TAG tag,boolean isOutput,boolean isCallBack) {
        this.url=url;
        this.tag=tag;
        this.isOutput =isOutput;//是否输出到文件
        this.isCallBack=isCallBack;//是否直接回调
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setOutputParams(String bookname, File dir) {
        BookName=bookname;
        Dir=dir;
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
            switch(tag){
                case BiQuGe:
                    processor1(document);
                    break;
                case SiDaMingZhu:
                    processor2(document);
                    break;
                default:
            }
            result = new NovelCatalog(ChapList, ChapLinkList);
            if(!isOutput && isCallBack){
                //返回结果
                Message message=mHandler.obtainMessage();
                message.what = CATALOG_UPDATED;
                message.obj= result;
                mHandler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            IOtxt.WriteErrReport(Dir,e);
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
                if (mHandler!=null){
                    Message message = mHandler.obtainMessage();
                    message.what = CATALOG_UPDATE_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        }
        if(isOutput){
            StringBuilder content=new StringBuilder();
            result.completeCatalog(url,tag);
            for (int i = 0; i < result.getSize(); i++) {
                content.append(result.getTitle().get(i));
                content.append('\n');
                content.append(result.getLink().get(i));
                if(i!=ChapList.size()-1)content.append('\n');
            }
            IOtxt.WriteCatalog(Dir,BookName,content.toString());
            //成功
            if (mHandler!=null){
                Message message = mHandler.obtainMessage();
                message.what = CATALOG_UPDATED;
                mHandler.sendMessage(message);
            }
        }

    }

    private void processor1(Document document) {
        Elements elements=document.select("div.box_con");
        Elements titles=elements.get(1).select("a");
        ChapList= (ArrayList<String>) titles.eachText();
        ChapLinkList= (ArrayList<String>) titles.eachAttr("href");
    }
    private void processor2(Document document){
        Elements element=document.select("div.info_mulu");
        Elements ele_chaplist=element.get(0).select("a");
        ArrayList<String> chaps= (ArrayList<String>) ele_chaplist.eachText();
        ArrayList<String>chaplinks= (ArrayList<String>) ele_chaplist.eachAttr("href");
        ArrayList<String>link_adjusted=new ArrayList<>();
        for (String link:chaplinks) {
            link_adjusted.add(link.replace("/",""));
        }
        for (int i = 0; i < chaps.size(); i++) {
            ChapList.add(chaps.get(i)+"(1)");
            ChapList.add(chaps.get(i)+"(2)");
            ChapLinkList.add(link_adjusted.get(i));
            ChapLinkList.add(link_adjusted.get(i).replace(".html","_2.html"));
        }
    }
    public static class CatalogUpdaterHandler<T> extends Handler {
        private final WeakReference<T> mActivity;
        private int fail_counter=0;
        private int success_counter=0;
        private MyHandle myHandle;

        public CatalogUpdaterHandler(T activity) {
            mActivity = new WeakReference<T>(activity);
        }

        public void setOverride(MyHandle myHandle) {
            this.myHandle=myHandle;
        }

        @Override
        public void handleMessage(Message msg) {
            T activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case CatalogThread.CATALOG_UPDATED:
                        success_counter++;
                        break;
                    case CatalogThread.CATALOG_UPDATE_FAILED: {
                        fail_counter++;
                    }
                    break;
                    default:
                }
                myHandle.handle(msg,success_counter,fail_counter);
            }
        }
        public interface MyHandle{
            void handle(Message msg,int Success,int Fail);
        }
    }

}
