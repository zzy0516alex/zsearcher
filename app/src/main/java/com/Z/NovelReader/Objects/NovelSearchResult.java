package com.Z.NovelReader.Objects;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.Z.NovelReader.Objects.beans.NovelSearchBean;

import java.util.ArrayList;

public class NovelSearchResult extends ViewModel {
    private MutableLiveData<ArrayList<NovelSearchBean>> mySearchResult;

    public MutableLiveData<ArrayList<NovelSearchBean>> getMySearchResult() {
        if (mySearchResult==null){
            ArrayList<NovelSearchBean> initList=new ArrayList<>();
            mySearchResult=new MutableLiveData<>();
            mySearchResult.setValue(initList);
        }
        return mySearchResult;
    }
    public void addToResult(NovelSearchBean novelSearchBean){
        ArrayList<NovelSearchBean> new_list=mySearchResult.getValue();
        if (new_list!=null)new_list.add(novelSearchBean);
        mySearchResult.setValue(new_list);
    }
    public void addToResult(ArrayList<NovelSearchBean> bookList){
        ArrayList<NovelSearchBean> new_list=mySearchResult.getValue();
        if (new_list!=null)new_list.addAll(bookList);
        mySearchResult.setValue(new_list);
    }
    public void clear(){
        ArrayList<NovelSearchBean>emptyList=new ArrayList<>();
        mySearchResult.setValue(emptyList);
    }
}
