package com.Z.NovelReader.Objects.beans;

public class NovelContentPage {
    private int start_pos;
    private int end_pos;
    private int page_id;
    private String page_content;
    private boolean isTempPage;//是否是还未分页的临时页
    private int belong_to_chapID;//当前页属于哪个章节，仅在isTempPage = true 时适用
    private boolean isFirstPage;//当前页是否属于本章的首页
    private String title;//首页所包含的标题，仅在isFirstPage = true 时适用
    private boolean isErrorPage;//当前页是否为出错警示页，仅在isFirstPage = true 时适用

    public int getStart_pos() {
        return start_pos;
    }

    public void setStart_pos(int start_pos) {
        this.start_pos = start_pos;
    }

    public int getEnd_pos() {
        return end_pos;
    }

    public void setEnd_pos(int end_pos) {
        this.end_pos = end_pos;
    }

    public int getPage_id() {
        return page_id;
    }

    public void setPage_id(int page_id) {
        this.page_id = page_id;
    }

    public String getPage_content() {
        return page_content;
    }

    public void setPage_content(String page_content) {
        this.page_content = page_content;
    }

    public boolean isTempPage() {
        return isTempPage;
    }

    public void setTempPage(boolean tempPage) {
        isTempPage = tempPage;
    }

    public int getBelong_to_chapID() {
        return belong_to_chapID;
    }

    public void setBelong_to_chapID(int belong_to_chapID) {
        this.belong_to_chapID = belong_to_chapID;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isErrorPage() {
        return isErrorPage;
    }

    public void setErrorPage(boolean errorPage) {
        isErrorPage = errorPage;
    }

    @Override
    public String toString() {
        return page_content;
    }
}
