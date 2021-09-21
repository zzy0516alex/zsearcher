package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Processors.BookListProcessor;
import com.Z.NovelReader.Processors.NovelSearchProcessor;
import com.Z.NovelReader.myObjects.beans.NovelSearchBean;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.SearchQuery;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class NovelSearchThread extends Thread {
    private String url;//搜书地址（完整）
    private int SourceId;//书源编号
    private SearchQuery completeSearchUrl;//搜书地址类
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private NovelRequire novelRequire;//书源规则类
    private ArrayList<NovelSearchBean>searchResult;//搜索结果
    private Handler handler;
    private Message message;
    public static final int BOOK_SEARCH_NOT_FOUND=0X1;
    public static final int BOOK_SEARCH_DONE=0X2;
    public static final int BOOK_SEARCH_NO_INTERNET=0X3;
    public static final int ILLEGAL_BOOK_SOURCE=0X4;
    public static final int BOOK_SOURCE_DIABLED=0X5;
    public enum TAG {BiQuGe,SiDaMingZhu}


    public NovelSearchThread(Context context,SearchQuery searchQuery, String key) {
        this.completeSearchUrl=new SearchQuery();
        try {
            this.completeSearchUrl = NovelSearchProcessor.getCompleteSearchUrl(searchQuery, key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.url=completeSearchUrl.getSearch_url();
        this.SourceId=completeSearchUrl.getId();
        sourceDBTools=new NovelSourceDBTools(context);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    @Override
    public void run() {
        super.run();
        searchResult=new ArrayList<>();
        //获取书源规则
        sourceDBTools.getNovelRequireById(SourceId, new NovelSourceDBTools.QueryListener() {
            @Override
            public void onResultBack(Object object) {
                novelRequire= (NovelRequire) object;
            }
        });
        try {
            //准备handler
            if (handler!=null)message=handler.obtainMessage();
            //获取document
            Connection connect = Jsoup.connect(url);
            connect.timeout(20000);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document doc=null;
            if ("GET".equals(completeSearchUrl.getMethod()))doc= connect.get();
            else if ("POST".equals(completeSearchUrl.getMethod())){
                connect.postDataCharset(completeSearchUrl.getCharset());//规定字符集
                //添加请求体
                String post_body=completeSearchUrl.getBody();
                String[] headers = post_body.split("&");
                for (String header : headers) {
                    String[] key_value = header.split("=");
                    if (key_value.length>1)connect.data(key_value[0], key_value[1]);
                }
                doc=connect.post();
            }
            //等待数据库查询结果
            int timeout=0;
            while (novelRequire==null && timeout<100){
                sleep(1);
                timeout++;
            }
            if (timeout>99)throw new TimeoutException();

            //根据书源规则处理内容
            searchResult = BookListProcessor.getSearchList(doc, novelRequire);

            if (searchResult!=null) {
                if (searchResult.size() == 0) message.what = BOOK_SEARCH_NOT_FOUND;
                else {
                    message.what = BOOK_SEARCH_DONE;
                    message.obj= searchResult;
                }
            }else message.what=ILLEGAL_BOOK_SOURCE;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err","no internet");
            if (message!=null)message.what=BOOK_SEARCH_NO_INTERNET;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e("err","no data");
            if (message!=null)message.what=BOOK_SOURCE_DIABLED;
        } catch (Exception e) {
            Log.e("novel search","书源解析错误："+novelRequire.getBookSourceName());
            e.printStackTrace();
        }

        if (message!=null)handler.sendMessage(message);
    }


    public static class NovelSearchHandler extends Handler{
        NovelSearchListener listener;
        private int search_count=0;
        private int total_search_time;
        private int num_no_internet=0;
        private int num_not_found=0;

        public NovelSearchHandler(int count,NovelSearchListener listener) {
            this.listener = listener;
            this.total_search_time=count;
        }
        public void clearAllCounters(){
            search_count=0;
            num_no_internet=0;
            num_not_found=0;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==BOOK_SEARCH_DONE)
                listener.onSearchResult((ArrayList<NovelSearchBean>) msg.obj);
            else {
                listener.onSearchError(msg.what);
                synchronized (this) {
                    switch (msg.what) {
                        case BOOK_SEARCH_NOT_FOUND:
                            num_not_found++;
                            break;
                        case BOOK_SEARCH_NO_INTERNET:
                            num_no_internet++;
                            break;
                        default:
                    }
                }
            }
            synchronized(this){
                search_count++;
                Log.d("book search", "search_count: "+search_count);
                if (search_count==total_search_time)
                    listener.onSearchFinish(total_search_time,num_no_internet,num_not_found);
            }
        }
    }
    public interface NovelSearchListener{
        void onSearchResult(ArrayList<NovelSearchBean> search_result);
        void onSearchError(int error_code);
        void onSearchFinish(int total_num,int num_no_internet,int num_not_found);
    }
}
