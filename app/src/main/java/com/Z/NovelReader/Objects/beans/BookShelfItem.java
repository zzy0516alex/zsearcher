package com.Z.NovelReader.Objects.beans;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.Z.NovelReader.NovelRoom.Novels;

import java.util.List;

public class BookShelfItem implements Cloneable{
    private int id;
    private String bookName;
    private String writer;
    private int hash;
    private Bitmap bookCover;
    private boolean isRecovering;
    private boolean isSpoiled;
    private boolean isSelected;

    public BookShelfItem() {
    }

    public BookShelfItem(Novels novel, Bitmap cover){
        this.id = novel.getId();
        this.bookName = novel.getBookName();
        this.writer = novel.getWriter();
        this.hash = novel.getShelfHash();
        this.isRecovering = novel.isRecover();
        this.isSpoiled = novel.isSpoiled();
        this.bookCover = cover;
        this.isSelected = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public Bitmap getBookCover() {
        return bookCover;
    }

    public void setBookCover(Bitmap bookCover) {
        this.bookCover = bookCover;
    }

    public boolean isRecovering() {
        return isRecovering;
    }

    public void setRecovering(boolean recovering) {
        isRecovering = recovering;
    }

    public boolean isSpoiled() {
        return isSpoiled;
    }

    public void setSpoiled(boolean spoiled) {
        isSpoiled = spoiled;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static class BookShelfItemDiff extends DiffUtil.Callback{
        private List<BookShelfItem> old_list, new_list;

        public BookShelfItemDiff(List<BookShelfItem> old_list, List<BookShelfItem> new_list) {
            this.old_list = old_list;
            this.new_list = new_list;
        }

        @Override
        public int getOldListSize() {
            return old_list.size();
        }

        @Override
        public int getNewListSize() {
            return new_list.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            BookShelfItem old_item = this.old_list.get(oldItemPosition);
            BookShelfItem new_item = this.new_list.get(newItemPosition);
            if(old_item ==null || new_item ==null)
                return false;
            return old_item.getId()==new_item.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            BookShelfItem old_item = this.old_list.get(oldItemPosition);
            BookShelfItem new_item = this.new_list.get(newItemPosition);
            return (old_item.bookName.equals(new_item.bookName))&&(old_item.writer.equals(new_item.writer))
                    &&(old_item.bookCover == new_item.bookCover)&&(old_item.isRecovering==new_item.isRecovering)
                    &&(old_item.isSpoiled==new_item.isSpoiled)&&(old_item.isSelected==new_item.isSelected);
        }
    }
}
