package com.Z.NovelReader.NovelRoom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Novels.class},version = 5,exportSchema = false)
public abstract class NovelDataBase extends RoomDatabase {
    private static NovelDataBase NOVEL_DATABASE;
    public static synchronized NovelDataBase getDataBase(Context context){
        if (NOVEL_DATABASE == null){
            NOVEL_DATABASE= Room.databaseBuilder(context.getApplicationContext(),NovelDataBase.class,"Novel_DB")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return NOVEL_DATABASE;
    }
    public abstract NovelDao getNovelDao();
}
