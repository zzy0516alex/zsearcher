package com.Z.NovelReader.NovelSourceRoom;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.Z.NovelReader.myObjects.beans.NovelRequire;

import java.util.List;

public class NovelSourceViewModel extends AndroidViewModel {

    private NovelSourceDao DAO;
    private LiveData<List<NovelRequire>> NovelSourceLiveData;

    public NovelSourceViewModel(@NonNull Application application) {
        super(application);
        NovelSourceDB data_base = NovelSourceDB.getDataBase(application);
        DAO=data_base.getNovelSourceDao();
        NovelSourceLiveData=DAO.getAllSources();
    }

    public LiveData<List<NovelRequire>> getNovelSourceLiveData() {
        return NovelSourceLiveData;
    }
}
