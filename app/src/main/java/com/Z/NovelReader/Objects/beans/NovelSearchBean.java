package com.Z.NovelReader.Objects.beans;

import java.io.Serializable;

public class NovelSearchBean implements Serializable {
    private String BookName;
    private String writer;
    private String BookInfoLink;
    private String BookCatalogLink;
    private int source;//书源编号

    public NovelSearchBean(String bookName, String writer, String bookInfoLink) {
        BookName = bookName;
        this.writer = writer;
        BookInfoLink = bookInfoLink;
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

    public String getBookInfoLink() {
        return BookInfoLink;
    }

    public void setBookInfoLink(String bookInfoLink) {
        BookInfoLink = bookInfoLink;
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

    public String getBookCatalogLink() {
        return BookCatalogLink;
    }

    public void setBookCatalogLink(String bookCatalogLink) {
        BookCatalogLink = bookCatalogLink;
    }

    @Override
    public String toString() {
        return "NovelSearchBean{" +
                "BookName='" + BookName + '\'' +
                ", writer='" + writer + '\'' +
                ", BookLink='" + BookInfoLink + '\'' +
                ", source=" + source +
                '}';
    }
}
