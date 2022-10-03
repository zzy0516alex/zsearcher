package com.Z.NovelReader.NovelRoom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.Z.NovelReader.Objects.beans.NovelRequire;

@Database(entities = {Novels.class},version = 11,exportSchema = false)
public abstract class NovelDataBase extends RoomDatabase {
    private static NovelDataBase NOVEL_DATABASE;
    public static synchronized NovelDataBase getDataBase(Context context){
        if (NOVEL_DATABASE == null){
            NOVEL_DATABASE= Room.databaseBuilder(context.getApplicationContext(),NovelDataBase.class,"Novel_DB")
                    .fallbackToDestructiveMigration()
                    //.addMigrations(MIGRATION_8_9)
                    //.addMigrations(MIGRATION_9_10)
                    .build();
        }
        return NOVEL_DATABASE;
    }
    public abstract NovelDao getNovelDao();
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Novels ADD COLUMN writer TEXT");
        }
    };
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //database.execSQL("ALTER TABLE Novels ADD COLUMN is_recover BOOL DEFAULT 0");
            database.execSQL("ALTER TABLE Novels ADD COLUMN is_spoiled BOOL DEFAULT 0");
            database.execSQL("ALTER TABLE Novels ADD COLUMN is_used BOOL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //无法删除列，弃用
            database.execSQL("ALTER TABLE Novels ADD COLUMN progress REAL");
        }
    };
}
