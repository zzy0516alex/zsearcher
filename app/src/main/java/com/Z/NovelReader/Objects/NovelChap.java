package com.Z.NovelReader.Objects;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NovelChap extends Novels implements Serializable {
    private String title;
    private String content;
    private NovelRequire novelRequire;//书源规则
    private String last_link="";
    private String next_link="";
    private boolean onError = false;

    public static final int BOTH_LINK_AVAILABLE=0;
    public static final int LAST_LINK_ONLY=1;
    public static final int NEXT_LINK_ONLY=2;
    public static final int NO_LINK_AVAILABLE=3;

    public NovelChap(Novels novels){
        super(novels.getId(),novels.getBookName(),novels.getWriter(),novels.getShelfHash(),
                novels.getTtlChap(),novels.getCurrentChap(),
                novels.getBookCatalogLink(),novels.getBookInfoLink(),
                novels.getContentRootLink(),novels.getSource(),
                novels.getProgress());
    }
    public NovelChap() {
    }

    public NovelChap deepClone() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (NovelChap) ois.readObject();
    }

    public void initChap(String title, String content, int link_type,String ...link) {
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

    public NovelRequire getNovelRequire() {
        return novelRequire;
    }

    public void setNovelRequire(NovelRequire novelRequire) {
        this.novelRequire = novelRequire;
    }

    public boolean hasLastLink(){
        return !last_link.equals("");
    }
    public boolean hasNextLink(){
        return !next_link.equals("");
    }

    public void setOnError(boolean onError) {
        this.onError = onError;
    }

    public boolean isOnError() {
        return onError;
    }

    public int getFurtherLinkType(){
        if (getCurrentChap()==1){
            return NEXT_LINK_ONLY;
        }else if (getCurrentChap()==getTtlChap()-2){
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

    public static void initNovelChap(NovelChap chap, String content, String[] currentChap) {
        if (currentChap[1].equals("")) {
            chap.initChap(currentChap[0], content, NovelChap.NEXT_LINK_ONLY, currentChap[2]);
        } else if (currentChap[2].equals("")) {
            chap.initChap(currentChap[0], content, NovelChap.LAST_LINK_ONLY, currentChap[1]);
        } else {
            chap.initChap(currentChap[0], content, NovelChap.BOTH_LINK_AVAILABLE, currentChap[1], currentChap[2]);
        }
    }

    public static String[] getCurrentChapLink(int current_chap, NovelCatalog novelCatalog) {
        String[] result=new String[3];//0:name 1:last_link 2:next_link
        result[0]= novelCatalog.getTitleList().get(current_chap);
        if(current_chap>0)
            result[1]= novelCatalog.getLinkList().get(current_chap-1);
        else result[1]="";
        if(current_chap< novelCatalog.getSize()-1)
            result[2]= novelCatalog.getLinkList().get(current_chap+1);
        else result[2]="";
        return result;
    }

}
