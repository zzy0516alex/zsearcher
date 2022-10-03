package com.Z.NovelReader.Objects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.io.Serializable;

@Entity
public class ruleContent implements Serializable {
    //此类为文章内容的处理方案
    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "nextContentUrl")
    private String nextContentUrl;

    @ColumnInfo(name = "content_replaceRegex")
    private String replaceRegex;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNextContentUrl() {
        return nextContentUrl;
    }

    public void setNextContentUrl(String nextContentUrl) {
        this.nextContentUrl = nextContentUrl;
    }

    public String getReplaceRegex() {
        return replaceRegex;
    }

    public void setReplaceRegex(String replaceRegex) {
        this.replaceRegex = replaceRegex;
    }
}
