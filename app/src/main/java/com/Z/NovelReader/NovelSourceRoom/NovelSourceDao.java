package com.Z.NovelReader.NovelSourceRoom;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.SearchQuery;

import java.util.List;

@Dao
public interface NovelSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertSources(NovelRequire...sources);

    @Update
    void UpdateSources(NovelRequire...sources);

    @Query("UPDATE novel_source SET enabled=:IsEnabled WHERE id=:id")
    void UpdateVisibility(boolean IsEnabled,int id);

    @Query("UPDATE novel_source SET respond_time=:time WHERE id=:id")
    void UpdateRespondTime(double time, int id);

    @Query("UPDATE novel_source SET respond_time = (respond_time+:time)/2 WHERE id=:id")
    void IterativeUpdateRespondTime(double time, int id);

    @Delete
    void DeleteSources(NovelRequire...sources);

    @Query("DELETE FROM novel_source")
    void DeleteAll();

    @Query("SELECT * FROM novel_source ORDER BY ID")
    LiveData<List<NovelRequire>> getAllSources();

    @Query("SELECT * FROM novel_source WHERE enabled=1 ORDER BY ID")
    List<NovelRequire> getSources();

    @Query("SELECT * FROM novel_source WHERE id=:id AND enabled=1")
    NovelRequire getSourcesByID(int id);

    @Query("SELECT id,search_url,book_source_url From novel_source WHERE enabled=1 ORDER BY ID")
    List<SearchQuery> getSearchUrls();
}
