package com.Z.NovelReader.Objects.beans;

import com.Z.NovelReader.NovelRoom.Novels;

import java.io.Serializable;

public class NovelSearchBean implements Serializable {
    private String BookName;
    private String writer;
    private String BookInfoLink;
    private String BookCatalogLink;
    private NovelRequire novelRule;
    private double resultScore;//搜索结果与实际输入内容的匹配度
    private int source;//书源编号

    public NovelSearchBean(String bookName, String writer, String bookInfoLink) {
        BookName = bookName;
        this.writer = writer;
        BookInfoLink = bookInfoLink;
    }

    public NovelSearchBean(Novels novel,NovelRequire novelRule) {
        BookName = novel.getBookName();
        this.writer = novel.getWriter();
        BookInfoLink = novel.getBookInfoLink();
        BookCatalogLink = novel.getBookCatalogLink();
        this.novelRule = novelRule;
        this.source = novel.getSource();
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

    public String getWriter() {
        return writer!=null?writer:"";
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

    public NovelRequire getNovelRule() {
        return novelRule;
    }

    public void setNovelRule(NovelRequire novelRule) {
        this.novelRule = novelRule;
    }

    public double getResultScore() {
        return resultScore;
    }

    public void setResultScore(double resultScore) {
        this.resultScore = resultScore;
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
