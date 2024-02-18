package com.Z.NovelReader.NovelRoom;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NovelDBLiveData extends AndroidViewModel {
    private NovelDao Noveldao;
    private LiveData<List<Novels>> allNovelsLD;
    public NovelDBLiveData(@NonNull Application application) {
        super(application);
        NovelDataBase novelDataBase=NovelDataBase.getDataBase(application);
        Noveldao=novelDataBase.getNovelDao();
        allNovelsLD=Noveldao.getAllNovels();
    }

    public LiveData<List<Novels>> getAllNovelsLD() {
        return allNovelsLD;
    }
    public List<Novels> getNovelList(){
        return allNovelsLD.getValue();
    }

}
