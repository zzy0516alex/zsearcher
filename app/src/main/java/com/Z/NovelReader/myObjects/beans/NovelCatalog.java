package com.Z.NovelReader.myObjects.beans;

import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class NovelCatalog implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<String> Title;
    private ArrayList<String> Link;

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

    public void completeCatalog(String book_source_link){
        ArrayList<String>newLink=new ArrayList<>();
        for (String link : Link) {
            String l = StringUtils.completeUrl(link, book_source_link);
            newLink.add(l);
        }
        Link=newLink;
    }
    public int getSize(){
        return Title.size();
    }
}
