package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.ContentProcessor;
import com.Z.NovelReader.Utils.ContentFileIO;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;

import java.io.FileNotFoundException;
import java.util.List;

public class ContentThread extends IterationThread {

    private String cache_file_path;
    private String download_file_path;
    private NovelRequire novelRequire;
    private NovelContentBean novelContent;
    private String startURL;
    private String rootURL;
    private StringBuilder content = new StringBuilder();
    //private String trigger_name = "";
    private List<String> catalogLinks;
    private int current_index;
    private boolean isOutputToCache = false;
    private boolean isOutputToDownloadFile = false;

    private boolean need_update_rootURL = false;
    private Novels novel;
    private NovelDBTools updater;

    public ContentThread(String startLink, NovelRequire novelRequire, Novels novel, String root_url) {
        super(startLink, 10);
        this.startURL = startLink;
        this.novelRequire = novelRequire;
        this.novel = novel;
        this.rootURL = root_url;
    }

    public void setCatalogLinks(List<String> links){
        this.catalogLinks = links;
        this.current_index = this.catalogLinks.indexOf(startURL);
        if(current_index==-1)throw new IllegalArgumentException("章节链接异常：不在目录中");
    }

    public void setUpdateRootURL(Context context){
        need_update_rootURL = true;
        this.updater = new NovelDBTools(context);
    }

    /**
     * 设置文件输出参数
     * @param file_path "/ZsearchRes/BookReserve/" + BookName
     */
    public void setOutputToCache(String file_path){
        this.cache_file_path = file_path;
        this.isOutputToCache = true;
    }

    public void setOutputToDownloadFile(String file_path){
        this.download_file_path = file_path;
        this.isOutputToDownloadFile = true;
    }

    public String getContent() {
        return content.toString();
    }

    @Override
    public void resultProcess() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Object preProcess(Document document) throws Exception {
        if (rootURL==null||"".equals(rootURL)){
            rootURL = StringUtils.getRootUrl(startURL);
        }
        novelContent = ContentProcessor.getNovelContent(document, novelRequire, novel, rootURL);
        content.append(novelContent.getContent());
        return novelContent;
    }

    @Override
    public boolean canBreakIterate(Object o) {
        boolean noNextPage = "".equals(novelContent.getNextPageURL());
        boolean overflow = catalogLinks.contains(novelContent.getNextPageURL());
        return overflow || noNextPage;
    }

    @Override
    public String updateStartLink() {
        Log.d("content thread","获取下一页：" + novelContent.getNextPageURL());
        return novelContent.getNextPageURL();
    }

    @Override
    public void onIterativeFinish() {
        if (isOutputToCache)
            FileIOUtils.writeContent(cache_file_path, content.toString());
        if(isOutputToDownloadFile) {
            try {
                String compressed_content = StringUtils.compressInGzip(content.toString());
                ContentFileIO.writeContent(download_file_path,compressed_content,current_index);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        callback(PROCESS_DONE,content.toString());
        if (need_update_rootURL) {
            updater.updateContentRoot(novel.getId(),rootURL);
        }
    }

    @Override
    public void onErrorOccur(int event,Exception e) {
        String error_content = generateErrorContent(event, e);
        if (isOutputToCache)
            FileIOUtils.writeContent(cache_file_path, error_content);
        FileIOUtils.WriteErrReport(e,"ContentThread|onErrorOccur");
        callback(ERROR_OCCUR,error_content);
        if (need_update_rootURL){
            if (rootURL.equals(novelRequire.getBookSourceUrl()))
                novel.setContentRootLink("");
            else novel.setContentRootLink(novelRequire.getBookSourceUrl());
            updater.updateContentRoot(novel.getId(),novel.getContentRootLink());
        }
    }

    public static String generateErrorContent(int event, Exception e) {
        String error_content = "章节缓存出错:未知的错误";
        switch(event){
            case NO_INTERNET:
                error_content = "章节缓存出错:网络异常";
                break;
            case PROCESSOR_ERROR:
                error_content = "章节缓存出错:书源解析异常";
                break;
            case TARGET_NOT_FOUND:
                error_content = "章节缓存出错:章节获取失败";
                break;
            default:{
                if(e!=null)error_content = "章节缓存出错:" + e.getMessage();
                break;
            }
        }
        return error_content;
    }
}
