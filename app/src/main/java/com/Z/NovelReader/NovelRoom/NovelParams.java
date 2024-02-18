package com.Z.NovelReader.NovelRoom;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {@ForeignKey(entity = Novels.class,parentColumns = {"id"},childColumns = {"novel_id"},onDelete = ForeignKey.CASCADE)})
public class NovelParams {

    @PrimaryKey
    @ColumnInfo(name = "novel_id", defaultValue = "-1")
    private int novelID;//-1表示是临时书籍
    @ColumnInfo(name = "source_id")
    private int sourceID;
    @ColumnInfo(name = "custom_param")
    private String customParam;

    public NovelParams() {
    }

    public NovelParams(int novelID, int sourceID, String customParam) {
        this.novelID = novelID;
        this.sourceID = sourceID;
        this.customParam = customParam;
    }

    public int getNovelID() {
        return novelID;
    }

    public void setNovelID(int novelID) {
        this.novelID = novelID;
    }

    public int getSourceID() {
        return sourceID;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    public String getCustomParam() {
        return customParam;
    }

    public void setCustomParam(String customParam) {
        this.customParam = customParam;
    }
}
