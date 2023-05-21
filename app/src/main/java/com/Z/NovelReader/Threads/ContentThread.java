package com.Z.NovelReader.Threads;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.ContentProcessor;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContentThread extends IterationThread {

    private String file_path;
    private NovelRequire novelRequire;
    private NovelContentBean novelContent;
    private String startURL;
    private String rootURL;
    private StringBuilder content = new StringBuilder();
    //private String trigger_name = "";
    private List<String> catalogLinks;

    private boolean need_update_rootURL = false;
    private Novels novel;
    private NovelDBUpdater updater;

    public ContentThread(String startLink, NovelRequire novelRequire,String root_url) {
        super(startLink, 10);
        this.startURL = startLink;
        this.novelRequire = novelRequire;
        this.rootURL = root_url;
    }

    public void setCatalogLinks(List<String> links){
        this.catalogLinks = links;
    }

    public void setUpdateRootURL(Novels novel,Context context){
        need_update_rootURL = true;
        this.novel = novel;
        this.updater = new NovelDBUpdater(context);
    }

    /**
     * 设置文件输出参数
     * @param file_path "/ZsearchRes/BookReserve/" + BookName
     */
    public void setOutputParams(String file_path){
        this.file_path = file_path;
    }

    public String getContent() {
        return content.toString();
    }

    @Override
    public void resultProcess() {
        //trigger_name = novelContent.getTriggerName();
        //content.append(novelContent.getContent());
        Log.d("content thread","获取下一页");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Object preProcess(Document document) throws Exception {
        if (rootURL==null||"".equals(rootURL)){
            //need_update_rootURL = true;
            rootURL = StringUtils.getRootUrl(startURL);
        }
        novelContent = ContentProcessor.getNovelContent(document, novelRequire,rootURL);
//        if ("".equals(trigger_name))trigger_name = novelContent.getTriggerName();
        content.append(novelContent.getContent());
        return novelContent;
    }

    @Override
    public boolean canBreakIterate(Object o) {
        boolean noNextPage = "".equals(novelContent.getNextPageURL());
        //boolean duplicateContent = !trigger_name.equals(novelContent.getTriggerName());
        boolean overflow = catalogLinks.contains(novelContent.getNextPageURL());
        return overflow || noNextPage;
    }

    @Override
    public String updateStartLink() {
        return novelContent.getNextPageURL();
    }

    @Override
    public void onIterativeFinish() {
        FileIOUtils.WriteTXT(file_path, content.toString());
        callback(PROCESS_DONE,content.toString());
        if (need_update_rootURL) {
            updater.updateContentRoot(novel.getId(),rootURL);
        }
    }

    @Override
    public void onErrorOccur(int event,Exception e) {
        String error_content = generateErrorContent(event);
        FileIOUtils.WriteTXT(file_path, error_content);
        FileIOUtils.WriteErrReport(e,"ContentThread|onErrorOccur");
        //callback(ERROR_OCCUR,error_content);
        if (need_update_rootURL){
            if (rootURL.equals(novelRequire.getBookSourceUrl()))
                novel.setContentRootLink("");
            else novel.setContentRootLink(novelRequire.getBookSourceUrl());
            updater.updateContentRoot(novel.getId(),novel.getContentRootLink());
        }
    }

    public static String generateErrorContent(int event) {
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
            default:
        }
        return error_content;
    }
}
