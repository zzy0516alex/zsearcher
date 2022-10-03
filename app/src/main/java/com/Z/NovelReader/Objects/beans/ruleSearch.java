package com.Z.NovelReader.Objects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.io.Serializable;

@Entity
public class ruleSearch implements Serializable {
    //此类为“搜索书籍”处理方案
    @ColumnInfo(name = "search_author")
    private String author;

    @ColumnInfo(name = "search_bookName")
    private String name;

    @ColumnInfo(name = "search_bookList")
    private String bookList;//其他成员均基于此元素

    @ColumnInfo(name = "search_bookUrl")
    private String bookUrl;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBookList() {
        return bookList;
    }

    public void setBookList(String bookList) {
        this.bookList = bookList;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }
    
}
