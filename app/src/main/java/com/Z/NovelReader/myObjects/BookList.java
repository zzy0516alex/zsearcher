package com.Z.NovelReader.myObjects;

import com.Z.NovelReader.Threads.NovelThread;

public class BookList {
    private String BookName;
    private String writer;
    private String BookLink;
    private NovelThread.TAG tag;

    public BookList(String bookName, String writer, String bookLink) {
        BookName = bookName;
        this.writer = writer;
        BookLink = bookLink;
    }

    public BookList(String bookName) {
        BookName = bookName;
    }
    public BookList(){}

    public String getBookName() {
        return "《"+BookName+"》\t"+writer;
    }

    public String getBookNameWithoutWriter(){
        return BookName;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
    }

    public String getBookLink() {
        return BookLink;
    }

    public void setBookLink(String bookLink) {
        BookLink = bookLink;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public NovelThread.TAG getTag() {
        return tag;
    }

    public void setTag(NovelThread.TAG tag) {
        this.tag = tag;
    }
}
