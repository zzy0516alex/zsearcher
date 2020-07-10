package com.example.helloworld.NovelRoom;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
    @ColumnInfo(name="book_link")
    private String BookLink;
    @ColumnInfo(name="offset")
    private int offset=3;

    public Novels(String bookName, int ttlchap, int currentChap,String bookLink) {
        this.BookName = bookName;
        this.ttlChap = ttlchap;
        this.CurrentChap = currentChap;
        this.BookLink=bookLink;
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

    public String getBookLink() {
        return BookLink;
    }

    public void setBookLink(String bookLink) {
        BookLink = bookLink;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
