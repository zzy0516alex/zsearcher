package com.Z.NovelReader.Objects.beans;

import com.Z.NovelReader.Utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelCatalog implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<String> Title;
    private ArrayList<String> Link;

    private long respondingTime;//获取目录的响应时间

    public NovelCatalog(ArrayList<String> title, ArrayList<String> link) {
        Title = title;
        Link = link;
    }

    public NovelCatalog() {
        Title=new ArrayList<>();
        Link=new ArrayList<>();
    }

    public ArrayList<String> getTitle() {
        return Title;
    }

    public void setTitle(ArrayList<String> title) {
        Title = title;
    }

    public ArrayList<String> getLink() {
        return Link;
    }

    public void setLink(ArrayList<String> link) {
        Link = link;
    }

    public void add(String title,String link){
        Title.add(title);
        Link.add(link);
    }
    public boolean isEmpty(){
        return Title.isEmpty()||Link.isEmpty();
    }

    public void setRespondingTime(long respondingTime) {
        this.respondingTime = respondingTime;
    }

    public void completeCatalog(String book_source_link){
        ArrayList<String>newLink = new ArrayList<>();
        ArrayList<String>newTitle = new ArrayList<>();
        for (int i = 0; i < Link.size(); i++) {
            String link = Link.get(i);
            String title = Title.get(i);
            if (link.equals("") || title.equals("")){
                continue;
            }
            link = StringUtils.completeUrl(link, book_source_link);
            newLink.add(link);
            newTitle.add(title);
        }
        Link = newLink;
        Title = newTitle;
    }

    public int findMatch(String ref_title){
        double top_score = 0;
        int top_index = -1;
        for (int i = 0; i < Link.size(); i++) {
            String s = StringUtils.simplifyChapName(Title.get(i));
            String ref = StringUtils.simplifyChapName(ref_title);
            if ("".equals(s))
                continue;
            double score = StringUtils.compareStrings(s, ref);
            if (top_score < score) {
                top_score = score;
                top_index = i;
            }
        }
        if (top_score < 50)return -1;
        else return top_index;
    }

    public int getSize(){
        return Title.size();
    }

    public void addCatalog(NovelCatalog sub_catalog){
        this.Link.addAll(sub_catalog.getLink());
        this.Title.addAll(sub_catalog.getTitle());
    }

    /**
     * 计算获取5000章目录的耗时
     * @param isMultiCatalog 是否具有多个子目录
     * @param hasExtraPage 是否具有每章分页
     */
    public long getUniRespondTime(boolean isMultiCatalog, boolean hasExtraPage){
        if (this.getSize() == 0)return 99999L;
        int pages = 5000/this.getSize();
        if (pages<=1 || isMultiCatalog)pages = 1;
        long total_time = pages * respondingTime;
        if (hasExtraPage)
            total_time *= 2;
        return total_time;
    }

    public String toString(int index){
        return String.format("[%s] %s",Title.get(index),Link.get(index));
    }
}
