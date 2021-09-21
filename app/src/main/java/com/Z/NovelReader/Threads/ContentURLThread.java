package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Processors.CatalogProcessor;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.NovelSearchBean;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class ContentURLThread extends Thread {
    private String catalogURL;
    private int sourceID;
    private NovelRequire novelRequire;//书源规则类
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private Handler handler;
    private Message message;
    private Context context;
    private int currentChapIndex;
    public static final int CONTENT_GRASP_DONE=0X0;
    public static final int BOOK_SOURCE_DIABLED=0X1;
    public static final int NO_INTERNET=0X2;
    public static final int PROCESSOR_ERROR=0X3;
    public static final int RULE_NEED_UPDATE=0X4;

    public ContentURLThread(String catalogURL, int sourceID) {
        this.catalogURL = catalogURL;
        this.sourceID=sourceID;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setCurrentChapIndex(int currentChapIndex) {
        this.currentChapIndex = currentChapIndex;
    }

    @Override
    public void run() {
        super.run();
        if (context!=null)sourceDBTools=new NovelSourceDBTools(context);
        sourceDBTools.getNovelRequireById(sourceID, new NovelSourceDBTools.QueryListener() {
            @Override
            public void onResultBack(Object object) {
                novelRequire= (NovelRequire) object;
            }
        });
        try {
            if (handler!=null)message=handler.obtainMessage();
            Connection connect = Jsoup.connect(catalogURL);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();

            NovelCatalog catalog = CatalogProcessor.getCatalog(document, novelRequire);

            if (!catalog.isEmpty()&&message!=null) {
                message.what=CONTENT_GRASP_DONE;
                //将目录写入临时文件
                FileIOUtils.WriteCatalog(context.getExternalFilesDir(null),
                        "/ZsearchRes/temp_catalog.txt",catalog);
                //返回当前需要的内容链接
                NovelCatalog currentChap = new NovelCatalog();
                currentChap.add(catalog.getTitle().get(currentChapIndex),catalog.getLink().get(currentChapIndex));
                message.obj=currentChap;
            }else {
                if (message!=null){
                    message.what = RULE_NEED_UPDATE;
                    message.obj=sourceID;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (message!=null){
                message.what=NO_INTERNET;
                message.obj=sourceID;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            if (message!=null){
                message.what=BOOK_SOURCE_DIABLED;
                message.obj=sourceID;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (message!=null){
                message.what=PROCESSOR_ERROR;
                message.obj=sourceID;
            }
        }
        if (handler!=null && message!=null)handler.sendMessage(message);
    }

    public static class ContentUrlHandler extends Handler{
        ContentUrlListener listener;

        public ContentUrlHandler(ContentUrlListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==CONTENT_GRASP_DONE)
                listener.onSuccess((NovelCatalog) msg.obj);
            else {
                listener.onError(msg.what, (Integer) msg.obj);
            }
        }
    }
    public interface ContentUrlListener{
        void onSuccess(NovelCatalog currentChap);
        void onError(int error_code,int sourceID);
    }

}
