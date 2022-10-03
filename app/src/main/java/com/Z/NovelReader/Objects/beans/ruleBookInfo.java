package com.Z.NovelReader.Objects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.io.Serializable;

@Entity
public class ruleBookInfo implements Serializable {
    //此类为“书籍信息”处理方案
    @ColumnInfo(name = "Info_coverUrl")
    private String coverUrl;

    @ColumnInfo(name = "Info_introduction")
    private String intro;

    @ColumnInfo(name = "Info_tocUrl")
    private String tocUrl;

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getTocUrl() {
        return tocUrl;
    }

    public void setTocUrl(String tocUrl) {
        this.tocUrl = tocUrl;
    }
}
