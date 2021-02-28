package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Z.NovelReader.myObjects.NovelCatalog;
import com.Z.NovelReader.myObjects.NovelChap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.Z.NovelReader.Threads.ContentTextThread.getContent1;
import static com.Z.NovelReader.Threads.ContentTextThread.getContent2;


public class ChapGetterThread extends Thread {
    private boolean has_lastChap;
    private boolean has_nextChap;
    private int LinkType;
    private String url;
    private NovelCatalog catalog;
    private String chap_title;
    private String chap_content;
    private int current_chap=-1;
    private NovelChap chap;
    private Handler mHandler;
    private int err_counter=0;
    public final static int GET_SUCCEED=0;
    public final static int INTERNET_ERROR=1;

    public ChapGetterThread(String url, NovelChap newChap ,Handler handler) {
        this.url = url;
        mHandler=handler;
        this.chap=newChap;
    }

    public void setChapState(int link_type) {
        switch(link_type){
            case NovelChap.BOTH_LINK_AVAILABLE:{
                this.has_lastChap = true;
                this.has_nextChap = true;
            }
                break;
            case NovelChap.LAST_LINK_ONLY:{
                this.has_lastChap = true;
                this.has_nextChap = false;
            }
                break;
            case NovelChap.NEXT_LINK_ONLY:{
                this.has_lastChap = false;
                this.has_nextChap = true;
            }
            default:
        }
        LinkType=link_type;

    }


    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public void run() {
        super.run();
        try {
            Document document= Jsoup.connect(url).get();
            grabContent(document,chap.getTag());
            chap.setContent(chap_content);
            if (has_lastChap){
                String last_link = catalog.getLink().get(chap.getCurrent_chapter() - 1);
                chap.setLast_link(last_link);
            }
            if (has_nextChap){
                String next_link = catalog.getLink().get(chap.getCurrent_chapter() + 1);
                chap.setNext_link(next_link);
            }
            Message message=mHandler.obtainMessage();
            message.obj=chap;
            message.what=GET_SUCCEED;
            mHandler.sendMessage(message);
        }catch (IOException e){
            e.printStackTrace();
            Message err_message;
            err_counter++;
            if (err_counter<10)run();
            else {
                err_message=mHandler.obtainMessage();
                err_message.what=INTERNET_ERROR;
                mHandler.sendMessage(err_message);
            }

        }

    }

    private void grabContent(Document document, NovelThread.TAG tag) {
        switch(tag){
            case BiQuGe:
                chap_content=getContent1(document);
                break;
            case SiDaMingZhu:
                chap_content=getContent2(document);
                break;
            default:
        }
    }

    public static class ChapGetterHandler<T> extends Handler{
        private final WeakReference<T> mActivity;
        private ChapGetListener listener;

        public ChapGetterHandler(T activity) {
            mActivity = new WeakReference<T>(activity);
        }

        public void setListener(ChapGetListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            T activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case ChapGetterThread.GET_SUCCEED:
                        listener.onSuccess((NovelChap) msg.obj);
                        break;
                    case ChapGetterThread.INTERNET_ERROR:
                        listener.onInternetError();
                    break;
                    default:
                }
                listener.onGetFinish();
            }
        }
        public interface ChapGetListener{
            void onSuccess(NovelChap newChap);
            void onInternetError();
            void onGetFinish();
        }
    }
}
