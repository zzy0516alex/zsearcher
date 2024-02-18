package com.Z.NovelReader.Threads;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Utils.ContentFileIO;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;

import java.io.FileNotFoundException;

public class ContentLoadThread extends BasicHandlerThread {
    private String downloadFilePath;
    private String cacheFilePath;
    private boolean save_to_cache;
    private int index;

    public ContentLoadThread(int index) {
        this.index = index;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public void setCacheFilePath(String cacheFilePath) {
        this.cacheFilePath = cacheFilePath;
        this.save_to_cache = true;
    }

    @Override
    public void run() {
        try {
            String zipped_content = ContentFileIO.readContent(downloadFilePath, index);
            String content = StringUtils.decompressInGzip(zipped_content);
            if (save_to_cache)
                FileIOUtils.writeContent(cacheFilePath, content);
            callback(PROCESS_DONE, content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            String error_content = "已下载的章节内容丢失";
            if (save_to_cache)
                FileIOUtils.writeContent(cacheFilePath, error_content);
            callback(ERROR_OCCUR,error_content);
        }
    }
}
