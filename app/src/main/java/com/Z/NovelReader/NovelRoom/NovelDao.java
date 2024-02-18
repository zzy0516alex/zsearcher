package com.Z.NovelReader.NovelRoom;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NovelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long InsertNovel(Novels novel);

    @Update(entity = Novels.class)
    void UpdateNovels(Novels...novels);

    @Delete
    void DeleteNovels(Novels...novels);

    @Query("DELETE FROM NOVELS")
    void DeleteAll();

    @Query("DELETE FROM Novels WHERE book_name=:bookName AND writer=:writer")
    void DeleteNovelByNameWithWriter(String bookName,String writer);

    @Query("DELETE FROM Novels WHERE shelf_hash=:shelf_hash")
    void DeleteNovelByShelfHash(int shelf_hash);

    @Query("SELECT * FROM NOVELS WHERE is_used=1 ORDER BY ID")
    LiveData<List<Novels>> getAllNovels();

    @Query("SELECT * FROM NOVELS ORDER BY ID")
    List<Novels> getNovelList();

    @Query("SELECT * FROM NOVELS WHERE id=:id")
    List<Novels> getNovelByID(int id);

    @Query("UPDATE Novels SET total_chapter=:totalChap WHERE id=:id")
    void UpdateTotalChap(int totalChap, int id);

    @Query("UPDATE Novels SET current_chap=:currentChap WHERE id=:id")
    void UpdateCurrentChap(int currentChap, int id);

    @Query("UPDATE Novels SET book_catalog_link=:catalog_link WHERE id=:id")
    void UpdateCatalogLink(String catalog_link, int id);

    @Query("UPDATE Novels SET book_info_link=:info_link WHERE id=:id")
    void UpdateInfoLink(String info_link, int id);

    @Query("UPDATE Novels SET content_root_link =:content_root WHERE id=:id")
    void UpdateContentRoot(String content_root, int id);

    @Query("UPDATE Novels SET `progress`=:progress WHERE id=:id")
    void UpdateChapOffset(double progress,int id);

    @Query("UPDATE Novels SET is_recover=:isRecover WHERE id=:id")
    void UpdateRecoverStatus(boolean isRecover,int id);

    @Query("UPDATE Novels SET is_spoiled=:isSpoiled,is_recover=0 WHERE id=:id")
    void UpdateSpoiledStatus(boolean isSpoiled,int id);

    @Query("UPDATE Novels SET is_used=:isUsed WHERE id=:id")
    void UpdateUsedStatus(boolean isUsed,int id);

    @Query("UPDATE Novels SET is_used=:isUsed WHERE book_name=:bookName AND writer=:writer")
    void UpdateAllUsedStatus(boolean isUsed,String bookName,String writer);

    @Query("SELECT * FROM NOVELS WHERE source=:sourceID")
    List<Novels> getNovelListBySource(int sourceID);

    @Query("SELECT * FROM NOVELS WHERE book_name=:bookName AND writer =:writer")
    List<Novels> getNovelListUnique(String bookName,String writer);
    @Query("SELECT * FROM NOVELS WHERE shelf_hash =:hash_value")
    List<Novels> getNovelListByShelfHash(int hash_value);
    @Query("SELECT * FROM NOVELS WHERE source=:source_id AND shelf_hash =:hash_value")
    List<Novels> getNovelListBySourceAndHash(int source_id, int hash_value);

}
