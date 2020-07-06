package com.example.helloworld.myObjects;

public class NovelChap {
    private String title;
    private String content;
    private int current_chapter;
    private int ttl_chapter;
    private int BookID;
    private String BookName;
    private String last_link="";
    private String next_link="";
    public static final int BOTH_LINK_AVAILABLE=0;
    public static final int LAST_LINK_ONLY=1;
    public static final int NEXT_LINK_ONLY=2;
    public static final int NO_LINK_AVAILABLE=3;

    public NovelChap(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public NovelChap(String title, String content, int link_type,String ...link) {
        this.title = title;
        this.content = content;
        switch(link_type){
            case BOTH_LINK_AVAILABLE:{
                if(link.length==2) {
                    this.last_link = link[0];
                    this.next_link = link[1];
                }
            }
                break;
            case LAST_LINK_ONLY:{
                this.last_link=link[0];
            }
                break;
            case NEXT_LINK_ONLY:{
                this.next_link=link[0];
            }
                break;
            case NO_LINK_AVAILABLE:
                break;
            default:
        }
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

    public int getBookID() {
        return BookID;
    }

    public void setBookID(int bookID) {
        BookID = bookID;
    }

    public String getBookName() {
        return BookName;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
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

    public String getLast_link() {
        return last_link;
    }

    public void setLast_link(String last_link) {
        this.last_link = last_link;
    }

    public boolean hasLastLink(){
        return !last_link.equals("");
    }
    public boolean hasNextLink(){
        return !next_link.equals("");
    }
    public int getFurtherLinkType(int ttl_chap){
        if (current_chapter==1){
            return NEXT_LINK_ONLY;
        }else if (current_chapter==ttl_chap-2){
            return LAST_LINK_ONLY;
        }else return BOTH_LINK_AVAILABLE;
    }
    public static int getLinkType(int current_chap,int ttl_chap){
        if (current_chap==0){
            return NEXT_LINK_ONLY;
        }else if (current_chap==ttl_chap-1){
            return LAST_LINK_ONLY;
        }else return BOTH_LINK_AVAILABLE;
    }

}
