package com.Z.NovelReader.Objects.beans;

public class NovelContentBean {

    private String content="";
    private String nextPageURL="";

    public NovelContentBean(String content, String nextPageURL) {
        this.content = content;
        this.nextPageURL = nextPageURL;
    }

    public NovelContentBean() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNextPageURL() {
        return nextPageURL;
    }

    public void setNextPageURL(String nextPageURL) {
        this.nextPageURL = nextPageURL;
    }
}
