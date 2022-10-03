package com.Z.NovelReader.Objects.beans;

import com.Z.NovelReader.Objects.NovelChap;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class NovelPageWindow {
    private ArrayList<NovelContentPage> pages;
    private boolean isSingleWindow = true;
    private int chapID;//仅single window有效

    public ArrayList<NovelContentPage> getPages() {
        return pages;
    }

    public void setPages(ArrayList<NovelContentPage> pages) {
        this.pages = pages;
    }

    public int getPageNum(){
        return pages!=null?pages.size():1;
    }

    public boolean isSingleWindow() {
        return isSingleWindow;
    }

    public void setSingleWindow(boolean singleWindow) {
        isSingleWindow = singleWindow;
    }

    public int getChapID() {
        return chapID;
    }

    public void setChapID(int chapID) {
        this.chapID = chapID;
    }

    public void init(NovelChap chap){
        pages = new ArrayList<>();
        NovelContentPage temp_page = new NovelContentPage();
        temp_page.setPage_id(0);
        temp_page.setPage_content(chap.getContent());
        temp_page.setTempPage(true);
        temp_page.setBelong_to_chapID(chap.getCurrentChap());
        temp_page.setFirstPage(true);
        temp_page.setErrorPage(chap.isOnError());
        temp_page.setTitle(chap.getTitle());
        pages.add(temp_page);
        chapID = chap.getCurrentChap();
        isSingleWindow = true;
    }

    public void mergePages(){
        String content = StringUtils.join(pages,"");
        content = StringUtils.strip(content, "[]");
        String title = pages.get(0).getTitle();
        pages = new ArrayList<>();
        NovelContentPage temp_page = new NovelContentPage();
        temp_page.setPage_id(0);
        temp_page.setPage_content(content);
        temp_page.setTempPage(true);
        temp_page.setBelong_to_chapID(chapID);
        temp_page.setFirstPage(true);
        temp_page.setTitle(title);
        pages.add(temp_page);
        isSingleWindow = true;
    }
}
