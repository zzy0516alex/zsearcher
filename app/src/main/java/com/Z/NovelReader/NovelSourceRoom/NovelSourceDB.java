package com.Z.NovelReader.NovelSourceRoom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.Z.NovelReader.Objects.beans.NovelRequire;

@Database(entities = {NovelRequire.class},version = 4,exportSchema = false)
public abstract class NovelSourceDB extends RoomDatabase {
    private static NovelSourceDB SOURCE_DATABASE;
    public static synchronized NovelSourceDB getDataBase(Context context){
        if (SOURCE_DATABASE == null){
            SOURCE_DATABASE= Room.databaseBuilder(context.getApplicationContext(),NovelSourceDB.class,"novel_source")
                    //.fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_3_4)
                    .build();
        }
        return SOURCE_DATABASE;
    }
    public abstract NovelSourceDao getNovelSourceDao();

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE novel_source ADD COLUMN respond_time REAL NOT NULL DEFAULT 99999");
        }
    };
}
