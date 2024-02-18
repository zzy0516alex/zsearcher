package com.Z.NovelReader.NovelRoom;

import android.app.Application;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.Z.NovelReader.Objects.beans.NovelRequire;

@Database(entities = {Novels.class, NovelParams.class},version = 14,exportSchema = false)
public abstract class NovelDataBase extends RoomDatabase {
    private static NovelDataBase NOVEL_DATABASE;
    public static synchronized NovelDataBase getDataBase(Context context){
        if (NOVEL_DATABASE == null){
            NOVEL_DATABASE= Room.databaseBuilder(context.getApplicationContext(),NovelDataBase.class,"Novel_DB")
                    .fallbackToDestructiveMigration()
                    //.addMigrations(MIGRATION_11_12)
                    .build();
        }
        return NOVEL_DATABASE;
    }
    public abstract NovelDao getNovelDao();
    public abstract NovelParamDao getNovelParamDao();
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

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //添加新表到数据库
            String createTableSql = "CREATE TABLE IF NOT EXISTS 'NovelParams'('novel_id' INTEGER NOT NULL PRIMARY KEY DEFAULT -1, 'source_id' INTEGER NOT NULL, 'custom_param' TEXT)";
            //添加索引到表
            String createIndexSql = "CREATE INDEX 'idx_novels_name_writer' ON 'Novels'('book_name','writer')";
            database.execSQL(createTableSql);
            database.execSQL(createIndexSql);
        }
    };
}
