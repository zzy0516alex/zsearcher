package com.Z.NovelReader.NovelRoom;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Novels {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "book_name")
    private String BookName;
    @ColumnInfo(name = "total_chapter")
    private int ttlChap;
    @ColumnInfo(name = "current_chap")
    private int CurrentChap;
    @ColumnInfo(name="book_catalog_link")
    private String BookCatalogLink;//书籍目录链接，存在多目录页时则是目录页第一页的链接
    @ColumnInfo(name="book_info_link")
    private String BookInfoLink;//书籍信息链接
    @ColumnInfo(name = "source")
    private int source;
    @ColumnInfo(name="offset")
    private int offset=3;
    @Ignore
    private boolean isUpdating;//是否正在执行其他更新操作

    public Novels(String bookName, int ttlchap, int currentChap,String bookCatalogLink,String bookInfoLink) {
        this.BookName = bookName;
        this.ttlChap = ttlchap;
        this.CurrentChap = currentChap;
        this.BookCatalogLink = bookCatalogLink;
        this.BookInfoLink = bookInfoLink;
    }

    public Novels() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return BookName;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
    }

    public int getTtlChap() {
        return ttlChap;
    }

    public void setTtlChap(int ttlChap) {
        this.ttlChap = ttlChap;
    }

    public int getCurrentChap() {
        return CurrentChap;
    }

    public void setCurrentChap(int currentChap) {
        CurrentChap = currentChap;
    }

    public String getBookCatalogLink() {
        return BookCatalogLink;
    }

    public void setBookCatalogLink(String bookCatalogLink) {
        BookCatalogLink = bookCatalogLink;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getBookInfoLink() {
        return BookInfoLink;
    }

    public void setBookInfoLink(String bookInfoLink) {
        BookInfoLink = bookInfoLink;
    }

    @Override
    public String toString() {
        return  BookName + "：" +
                "详情页：" + BookInfoLink + ",\n" +
                "目录页：" + BookCatalogLink +
                ", 源ID：" + source;
    }
}
