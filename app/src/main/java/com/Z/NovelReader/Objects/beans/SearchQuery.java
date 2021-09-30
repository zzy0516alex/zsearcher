package com.Z.NovelReader.Objects.beans;

import androidx.room.Ignore;

public class SearchQuery {
    private int id;
    private String search_url;
    private String book_source_url;
    @Ignore
    private String charset="UTF-8";
    @Ignore
    private String method="GET";
    @Ignore
    private String body;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSearch_url() {
        return search_url;
    }

    public void setSearch_url(String search_url) {
        this.search_url = search_url;
    }

    public String getBook_source_url() {
        return book_source_url;
    }

    public void setBook_source_url(String book_source_url) {
        this.book_source_url = book_source_url;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean hasCharset(){
        return !"UTF-8".equals(this.charset);
    }
    public boolean hasMethod(){
        return !"GET".equals(this.method);
    }

}
