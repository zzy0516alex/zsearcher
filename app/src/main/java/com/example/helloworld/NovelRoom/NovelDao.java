package com.example.helloworld.NovelRoom;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NovelDao {
    @Insert
    void InsertNovels(Novels...novels);

    @Update
    void UpdateNovels(Novels...novels);

    @Delete
    void DeleteNovels(Novels...novels);

    @Query("DELETE FROM NOVELS")
    void DeleteAll();

    @Query("SELECT * FROM NOVELS ORDER BY ID")
    LiveData<List<Novels>> getAllNovels();

    @Query("SELECT * FROM NOVELS ORDER BY ID")
    List<Novels> getNovelList();

}
