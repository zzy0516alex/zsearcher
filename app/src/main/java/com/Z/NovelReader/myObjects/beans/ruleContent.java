package com.Z.NovelReader.myObjects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity
public class ruleContent {
    //此类为文章内容的处理方案
    @ColumnInfo(name = "content")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
