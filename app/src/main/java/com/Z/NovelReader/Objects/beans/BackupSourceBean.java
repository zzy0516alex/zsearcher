package com.Z.NovelReader.Objects.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.R;

public class BackupSourceBean {
    private Novels current_novel;
    private int SourceID;
    private String SourceName;
    private NovelCatalog catalog;
    private Bitmap coverBrief;
    private boolean isChosen = false;

    public BackupSourceBean(Novels current_novel, int sourceID, String sourceName, NovelCatalog catalog, boolean isChosen) {
        this.current_novel = current_novel;
        SourceID = sourceID;
        SourceName = sourceName;
        this.catalog = catalog;
        this.isChosen = isChosen;
    }

    public Novels getCurrent_novel() {
        return current_novel;
    }

    public void setCurrent_novel(Novels current_novel) {
        this.current_novel = current_novel;
    }

    public int getSourceID() {
        return SourceID;
    }

    public void setSourceID(int sourceID) {
        SourceID = sourceID;
    }

    public String getSourceName() {
        return SourceName;
    }

    public void setSourceName(String sourceName) {
        SourceName = sourceName;
    }

    public NovelCatalog getCatalog() {
        return catalog;
    }

    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    public Bitmap getCoverBrief() {
        return coverBrief;
    }

    public void setCoverBrief(Bitmap coverBrief) {
        this.coverBrief = coverBrief;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }
}
