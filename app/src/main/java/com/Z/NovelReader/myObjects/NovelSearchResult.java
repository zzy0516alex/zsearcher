package com.Z.NovelReader.myObjects;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class NovelSearchResult extends ViewModel {
    private MutableLiveData<ArrayList<BookList>> mySearchResult;

    public MutableLiveData<ArrayList<BookList>> getMySearchResult() {
        if (mySearchResult==null){
            ArrayList<BookList> initList=new ArrayList<>();
            mySearchResult=new MutableLiveData<>();
            mySearchResult.setValue(initList);
        }
        return mySearchResult;
    }
    public void addToResult(BookList bookList){
        ArrayList<BookList> new_list=mySearchResult.getValue();
        if (new_list!=null)new_list.add(bookList);
        mySearchResult.setValue(new_list);
    }
    public void addToResult(ArrayList<BookList> bookLists){
        ArrayList<BookList> new_list=mySearchResult.getValue();
        if (new_list!=null)new_list.addAll(bookLists);
        mySearchResult.setValue(new_list);
    }
    public void clear(){
        ArrayList<BookList>emptyList=new ArrayList<>();
        mySearchResult.setValue(emptyList);
    }
}
