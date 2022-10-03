package com.Z.NovelReader.Threads;

import android.content.Context;
import android.util.Log;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.ContentProcessor;
import com.Z.NovelReader.Utils.FileIOUtils;

import org.jsoup.nodes.Document;

import java.io.File;

public class ContentThread extends IterationThread {

    private String file_path;
    private NovelRequire novelRequire;
    private NovelContentBean novelContent;
    private String rootURL;
    private StringBuilder content = new StringBuilder();
    private String trigger_name = "";

    private boolean need_update_rootURL = false;
    private Novels novel;
    private NovelDBUpdater updater;

    public ContentThread(String startLink, NovelRequire novelRequire,String root_url) {
        super(startLink, 10);
        this.novelRequire = novelRequire;
        this.rootURL = root_url;
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
        trigger_name = novelContent.getTriggerName();
        Log.d("content thread","获取下一页");
    }

    @Override
    public Object preProcess(Document document) throws Exception {
        if ("".equals(rootURL))return null;
        novelContent = ContentProcessor.getNovelContent(document, novelRequire,rootURL);
        if ("".equals(trigger_name))trigger_name = novelContent.getTriggerName();
        content.append(novelContent.getContent());
        return novelContent;
    }

    @Override
    public boolean canBreakIterate(Object o) {
        return "".equals(novelContent.getNextPageURL()) ||
                !trigger_name.equals(novelContent.getTriggerName());
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
