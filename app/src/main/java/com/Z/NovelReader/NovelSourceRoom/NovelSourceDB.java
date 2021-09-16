package com.Z.NovelReader.NovelSourceRoom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.Z.NovelReader.myObjects.beans.NovelRequire;

@Database(entities = {NovelRequire.class},version = 1,exportSchema = false)
public abstract class NovelSourceDB extends RoomDatabase {
    private static NovelSourceDB SOURCE_DATABASE;
    public static synchronized NovelSourceDB getDataBase(Context context){
        if (SOURCE_DATABASE == null){
            SOURCE_DATABASE= Room.databaseBuilder(context.getApplicationContext(),NovelSourceDB.class,"novel_source")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return SOURCE_DATABASE;
    }
    public abstract NovelSourceDao getNovelSourceDao();
}
