package com.Z.NovelReader.NovelRoom;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity(indices = {@Index(name = "idx_novels_name_writer", value = {"book_name","writer"})})
public class Novels implements Serializable {
    @Ignore
    public static final double DEFAULT_PROGRESS = 0.001;
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "book_name")
    private String BookName;
    @ColumnInfo(name = "writer")
    private String Writer;
    @ColumnInfo(name = "shelf_hash")
    private int shelfHash;//书籍在书架中的唯一标识，不同源的同一书籍该值相同，并通过该值定义为书架上的同一本书
    @ColumnInfo(name = "total_chapter")
    private int ttlChap;//总目录数，随目录更新而更新
    @ColumnInfo(name = "current_chap")
    private int CurrentChap;//在novel中表示当前正阅读的章节，在NovelChap中表示当前章节在目录中的序号
    @ColumnInfo(name="book_catalog_link")
    private String BookCatalogLink;//书籍目录链接，存在多目录页时则是目录页第一页的链接
    @ColumnInfo(name="book_info_link")
    private String BookInfoLink;//书籍信息链接
    @ColumnInfo(name="content_root_link")
    private String ContentRootLink;//章节内容父链接
    @ColumnInfo(name = "source")
    private int source;//对应的书源
    @ColumnInfo(name="progress")
    private double progress;//章节中的阅读进度 [0,1]
    @ColumnInfo(name = "is_recover")
    private boolean isRecover=false;//是否正在修复
    @ColumnInfo(name = "is_spoiled")
    private boolean isSpoiled=false;//是否已损坏
    @ColumnInfo(name = "is_used")
    private boolean isUsed=true;//是否正在使用

    public Novels(String bookName, String writer, int ttlchap, int currentChap,String bookCatalogLink,String bookInfoLink, int source) {
        this.BookName = bookName;
        this.ttlChap = ttlchap;
        this.CurrentChap = currentChap;
        this.BookCatalogLink = bookCatalogLink;
        this.BookInfoLink = bookInfoLink;
        this.Writer = writer;
        this.source = source;
    }

    public Novels() {
    }

    public Novels(NovelSearchBean search_result){
        this.id = -1;//临时书籍，不适用于数据库插入
        this.BookName = search_result.getBookName();
        this.Writer = search_result.getWriter();
        this.shelfHash =
        this.ttlChap = 0;//待目录获取后更新
        this.CurrentChap = 0;
        this.BookCatalogLink = search_result.getBookCatalogLink();
        this.BookInfoLink = search_result.getBookInfoLink();
        this.ContentRootLink = "";//待内容获取后更新
        this.source = search_result.getSource();
        this.progress = DEFAULT_PROGRESS;
    }

    /**专为NovelChap准备的构造器*/
    public Novels(int id, String bookName, String writer, int shelfHash, int ttlChap, int currentChap, String bookCatalogLink, String bookInfoLink, String contentRootLink, int source, double progress) {
        this.id = id;
        this.BookName = bookName;
        this.Writer = writer;
        this.shelfHash = shelfHash;
        this.ttlChap = ttlChap;
        this.CurrentChap = currentChap;
        this.BookCatalogLink = bookCatalogLink;
        this.BookInfoLink = bookInfoLink;
        this.ContentRootLink = contentRootLink;
        this.source = source;
        this.progress = progress;
    }

    public void setStatus(boolean recovering,boolean spoiled,boolean used){
        this.isRecover = recovering;
        this.isSpoiled = spoiled;
        this.isUsed = used;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return BookName;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
    }

    public String getWriter() {
        return Writer!=null?Writer:"";
    }

    public void setWriter(String writer) {
        Writer = writer;
    }

    public int getShelfHash() {
        return shelfHash;
    }

    public void setShelfHash(int shelfHash) {
        this.shelfHash = shelfHash;
    }

    public void autoSetShelfHash(){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest((BookName+Writer).getBytes());
        BigInteger num = new BigInteger(1, digest);
        this.shelfHash = num.intValue();
    }

    public int getTtlChap() {
        return ttlChap;
    }

    public void setTtlChap(int ttlChap) {
        this.ttlChap = ttlChap;
    }

    public int getCurrentChap() {
        return CurrentChap;
    }

    public void setCurrentChap(int currentChap) {
        CurrentChap = currentChap;
    }

    public String getBookCatalogLink() {
        return BookCatalogLink;
    }

    public void setBookCatalogLink(String bookCatalogLink) {
        BookCatalogLink = bookCatalogLink;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getBookInfoLink() {
        return BookInfoLink;
    }

    public void setBookInfoLink(String bookInfoLink) {
        BookInfoLink = bookInfoLink;
    }

    public String getContentRootLink() {
        return ContentRootLink;
    }

    public void setContentRootLink(String contentRootLink) {
        ContentRootLink = contentRootLink;
    }

    public boolean isRecover() {
        return isRecover;
    }

    public void setRecover(boolean recovering) {
        isRecover = recovering;
    }

    public boolean isSpoiled() {
        return isSpoiled;
    }

    public void setSpoiled(boolean spoiled) {
        isSpoiled = spoiled;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    @Override
    public String toString() {
        return  BookName + " - " + Writer + "：\n" +
                "详情页：" + BookInfoLink + ",\n" +
                "目录页：" + BookCatalogLink + ",\n" +
                "源ID：" + source;
    }
}
