package com.Z.NovelReader.NovelRoom;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NovelParamDao {
 @Insert
 void insertNovelParams(NovelParams...novelParams);
 @Update(entity = NovelParams.class)
 void updateNovelParams(NovelParams...novelParams);
 @Query("DELETE FROM NovelParams WHERE novel_id=:novel_id AND source_id=:source_id")
 void deleteNovelParamByID(int novel_id, int source_id);
 @Query("SELECT * FROM NovelParams WHERE novel_id=:novel_id AND source_id=:source_id")
 NovelParams getNovelParamByID(int novel_id, int source_id);
}
