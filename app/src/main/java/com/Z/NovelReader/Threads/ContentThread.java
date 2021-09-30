package com.Z.NovelReader.Threads;

import android.util.Log;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.ContentProcessor;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.FileIOUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;

public class ContentThread extends IterationThread {

    private File Dir;
    private String sub_path;
    private NovelRequire novelRequire;
    private NovelContentBean novelContent;
    private String rootURL;
    private StringBuilder content = new StringBuilder();

    public ContentThread(String startLink, NovelRequire novelRequire,String root_url) {
        super(startLink, 10);
        this.novelRequire = novelRequire;
        this.rootURL = root_url;
        Dir = MyApplication.getExternalDir();
    }

    /**
     * 设置文件输出参数
     * @param subPath "/ZsearchRes/BookReserve/" + BookName
     */
    public void setOutputParams(String subPath){
        this.sub_path = subPath;
    }

    @Override
    public void resultProcess() {
        Log.d("content thread","获取下一页");
    }

    @Override
    public Object firstProcess(Document document) throws Exception {
        novelContent = ContentProcessor.getNovelContent(document, novelRequire,rootURL);
        content.append(novelContent.getContent());
        return novelContent;
    }

    @Override
    public boolean canBreakIterate(Object o) {
        if ("".equals(novelContent.getNextPageURL()))return true;
        else return false;
    }

    @Override
    public String updateStartLink() {
        return novelContent.getNextPageURL();
    }

    @Override
    public void onIterativeFinish() {
        FileIOUtils.WriteTXT(Dir, sub_path, content.toString());
        callback(PROCESS_DONE,content.toString());
    }

    @Override
    public void onErrorOccur(int event) {
        String error_content = "出现了未被发现的错误";
        switch(event){
            case NO_INTERNET:
                error_content = "章节缓存出错:网络异常";
                break;
            case PROCESSOR_ERROR:
                error_content = "章节缓存出错:书源解析异常";
                break;
            default:
        }
        FileIOUtils.WriteTXT(Dir, sub_path, error_content);
    }
}
