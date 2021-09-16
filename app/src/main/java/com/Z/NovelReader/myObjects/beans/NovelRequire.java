package com.Z.NovelReader.myObjects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

@Entity(tableName = "novel_source")
public class NovelRequire {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "book_source_name")
    private String bookSourceName;

    @ColumnInfo(name = "book_source_url")
    private String bookSourceUrl;

    @ColumnInfo(name = "search_url")
    private String searchUrl;

    @ColumnInfo(name = "enabled")
    private boolean enabled;

    @Embedded
    private ruleSearch ruleSearch;

    @Embedded
    private ruleBookInfo ruleBookInfo;

    @Embedded
    private ruleToc ruleToc;

    @Embedded
    private ruleContent ruleContent;

    public NovelRequire() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookSourceName() {
        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public com.Z.NovelReader.myObjects.beans.ruleSearch getRuleSearch() {
        return ruleSearch;
    }

    public void setRuleSearch(com.Z.NovelReader.myObjects.beans.ruleSearch ruleSearch) {
        this.ruleSearch = ruleSearch;
    }

    public com.Z.NovelReader.myObjects.beans.ruleBookInfo getRuleBookInfo() {
        return ruleBookInfo;
    }

    public void setRuleBookInfo(com.Z.NovelReader.myObjects.beans.ruleBookInfo ruleBookInfo) {
        this.ruleBookInfo = ruleBookInfo;
    }

    public com.Z.NovelReader.myObjects.beans.ruleToc getRuleToc() {
        return ruleToc;
    }

    public void setRuleToc(com.Z.NovelReader.myObjects.beans.ruleToc ruleToc) {
        this.ruleToc = ruleToc;
    }

    public com.Z.NovelReader.myObjects.beans.ruleContent getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(com.Z.NovelReader.myObjects.beans.ruleContent ruleContent) {
        this.ruleContent = ruleContent;
    }

    public static NovelRequire getNovelRequireBean(String json){
        return new Gson().fromJson(json, NovelRequire[].class)[0];
    }
}