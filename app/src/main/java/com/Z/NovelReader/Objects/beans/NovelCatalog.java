package com.Z.NovelReader.Objects.beans;

import com.Z.NovelReader.Utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelCatalog implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<String> Titles;
    private ArrayList<String> Links;
    private ArrayList<Boolean> isDownload;

    public static class CatalogItem{
        public int index;
        public String Title;
        public String Link;
        public boolean isDownloading;
        public boolean isDownloaded;
        public boolean isSelected;
    }

    private long respondingTime;//获取目录的响应时间

    public NovelCatalog(ArrayList<String> titles, ArrayList<String> links) {
        Titles = titles;
        Links = links;
    }

    public NovelCatalog() {
        Titles =new ArrayList<>();
        Links =new ArrayList<>();
        isDownload = new ArrayList<>();
    }

    public ArrayList<String> getTitleList() {
        return Titles;
    }

    public ArrayList<String> getLinkList() {
        return Links;
    }

    public ArrayList<Boolean> getIsDownload() {
        return isDownload;
    }

    public void setDownloaded(int id, boolean isDownloaded){
        this.isDownload.set(id,isDownloaded);
    }

    public boolean isDownloaded(int id){
        return this.isDownload.get(id);
    }

    public void setDownloadedChaps(ArrayList<Integer> ids) {
        for (Integer id : ids) {
            setDownloaded(id,true);
        }
    }

    public void add(CatalogItem item){
        item.index = Titles.size();
        Titles.add(item.Title);
        Links.add(item.Link);
        isDownload.add(item.isDownloaded);
    }

    public void addAll(NovelCatalog sub_catalog){
        this.Links.addAll(sub_catalog.getLinkList());
        this.Titles.addAll(sub_catalog.getTitleList());
        this.isDownload.addAll(sub_catalog.getIsDownload());
    }

    public CatalogItem get(int i) throws IndexOutOfBoundsException{
        CatalogItem item = new CatalogItem();
        item.index = i;
        item.Title = this.Titles.get(i);
        item.Link = this.Links.get(i);
        item.isDownloaded = this.isDownload.get(i);
        return item;
    }

    public boolean isEmpty(){
        return Titles.isEmpty()|| Links.isEmpty();
    }

    public void setRespondingTime(long respondingTime) {
        this.respondingTime = respondingTime;
    }

    public void completeCatalog(String book_source_link){
        ArrayList<String>newLink = new ArrayList<>();
        ArrayList<String>newTitle = new ArrayList<>();
        for (int i = 0; i < Links.size(); i++) {
            String link = Links.get(i);
            String title = Titles.get(i);
            if (link.equals("") || title.equals("")){
                continue;
            }
            link = StringUtils.completeUrl(link, book_source_link);
            newLink.add(link);
            newTitle.add(title);
        }
        Links = newLink;
        Titles = newTitle;
    }

    public int findMatch(String ref_title){
        double top_score = 0;
        int top_index = -1;
        for (int i = 0; i < Links.size(); i++) {
            String s = StringUtils.simplifyChapName(Titles.get(i));
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
        if(Titles.size() != Links.size())throw new RuntimeException("目录标题与链接不对应");
        return Titles.size();
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
        return String.format("[%s] %s", Titles.get(index), Links.get(index));
    }
}
