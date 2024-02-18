package com.Z.NovelReader.Threads;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.ContentProcessor;
import com.Z.NovelReader.Utils.ContentFileIO;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;

import java.io.FileNotFoundException;
import java.util.List;

public class ContentDownloadThread extends IterationThread {

    private String download_file_path;
    private NovelRequire novelRequire;
    private Novels novel;
    private NovelContentBean novelContent;
    private String startURL;
    private String rootURL;
    private StringBuilder content = new StringBuilder();

    private List<String> catalogLinks;
    private int current_index;
    private boolean isOutputToCache = false;
    private boolean isOutputToDownloadFile = false;

    private boolean need_update_rootURL = false;

    public ContentDownloadThread(String startLink, NovelRequire novelRequire, Novels novel, String root_url) {
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
        return novelContent.getNextPageURL();
    }

    @Override
    public void onIterativeFinish() {
        if(isOutputToDownloadFile) {
            try {
                String compressed_content = StringUtils.compressInGzip(content.toString());
                ContentFileIO.writeContent(download_file_path,compressed_content,current_index);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        callback(PROCESS_DONE,current_index);
    }

    @Override
    public void onErrorOccur(int event,Exception e) {
        callback(ERROR_OCCUR,current_index);
    }
}
