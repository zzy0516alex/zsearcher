package com.Z.NovelReader.myObjects;

import com.Z.NovelReader.Threads.NovelThread;

import java.util.ArrayList;

public class NovelCatalog {
    private ArrayList<String> Title;
    private ArrayList<String> Link;
    //NOTICE:sting value here
    private final String BASIC2="https://www.sidamingzhu.org";

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
    public void completeCatalog(String book_link, NovelThread.TAG tag){
        ArrayList<String>newLink=new ArrayList<>();
        for (String link : Link) {
            newLink.add(addBasicURL(book_link,link,tag));
        }
        Link=newLink;
    }
    public int getSize(){
        return Title.size();
    }
    private String addBasicURL(String book_link, String current_chapLink, NovelThread.TAG tag){
        String newUrl="";
        if (current_chapLink.contains("http")){
            return current_chapLink;
        }else {
            switch (tag) {
                case BiQuGe:
                    newUrl = book_link + current_chapLink;
                    break;
                case SiDaMingZhu:
                    newUrl = BASIC2 + "/" + current_chapLink;
                    break;
                default:
            }
            return newUrl;
        }
    }
}
