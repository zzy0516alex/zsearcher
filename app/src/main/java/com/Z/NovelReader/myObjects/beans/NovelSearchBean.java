package com.Z.NovelReader.myObjects.beans;

import com.Z.NovelReader.Threads.NovelSearchThread;

import java.io.Serializable;

public class NovelSearchBean implements Serializable {
    private String BookName;
    private String writer;
    private String BookLink;
    private int source;//书源编号

    public NovelSearchBean(String bookName, String writer, String bookLink) {
        BookName = bookName;
        this.writer = writer;
        BookLink = bookLink;
    }

    public NovelSearchBean(String bookName) {
        BookName = bookName;
    }
    public NovelSearchBean(){}

    public String getBookName() {
        return writer!=null ? "《"+BookName+"》\t"+writer:"《"+BookName+"》";
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

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "NovelSearchBean{" +
                "BookName='" + BookName + '\'' +
                ", writer='" + writer + '\'' +
                ", BookLink='" + BookLink + '\'' +
                ", source=" + source +
                '}';
    }
}
