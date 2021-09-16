package com.Z.NovelReader.myObjects;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.Z.NovelReader.NovelSourceRoom.NovelSourceDB;
import com.Z.NovelReader.myObjects.beans.NovelRequire;

import java.util.List;

public class NovelSourceViewModel extends AndroidViewModel {
    LiveData<List<NovelRequire>> allNovelSources;
    public NovelSourceViewModel(@NonNull Application application) {
        super(application);
        NovelSourceDB novelSourceDB = NovelSourceDB.getDataBase(application);
        allNovelSources = novelSourceDB.getNovelSourceDao().getAllSources();
    }

    public LiveData<List<NovelRequire>> getAllNovelSources() {
        return allNovelSources;
    }
}
