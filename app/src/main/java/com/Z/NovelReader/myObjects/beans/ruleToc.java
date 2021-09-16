package com.Z.NovelReader.myObjects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity
public class ruleToc {
    //此类为“书籍目录”处理方案
    @ColumnInfo(name = "chapter_list")
    private String chapterList;

    @ColumnInfo(name = "chapter_name")
    private String chapterName;

    @ColumnInfo(name = "chapter_url")
    private String chapterUrl;

    @ColumnInfo(name = "next_chapterList")
    private String nextTocUrl;

    public String getChapterList() {
        return chapterList;
    }

    public void setChapterList(String chapterList) {
        this.chapterList = chapterList;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public String getNextTocUrl() {
        return nextTocUrl;
    }

    public void setNextTocUrl(String nextTocUrl) {
        this.nextTocUrl = nextTocUrl;
    }
}
