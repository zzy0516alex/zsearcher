package com.example.helloworld.myObjects;

public class NovelChap {
    private String title;
    private String content;
    private int current_chapter;
    private int ttl_chapter;
    private String next_link;

    public NovelChap(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public NovelChap(String title, String content, String link) {
        this.title = title;
        this.content = content;
        this.next_link=link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCurrent_chapter() {
        return current_chapter;
    }

    public void setCurrent_chapter(int current_chapter) {
        this.current_chapter = current_chapter;
    }

    public int getTtl_chapter() {
        return ttl_chapter;
    }

    public void setTtl_chapter(int ttl_chapter) {
        this.ttl_chapter = ttl_chapter;
    }

    public String getNext_link() {
        return next_link;
    }

    public void setNext_link(String next_link) {
        this.next_link = next_link;
    }
    public boolean isFirstChap(){
        return current_chapter == 1;
    }
    protected boolean isLastChap(){
        return current_chapter==ttl_chapter;
    }
}
