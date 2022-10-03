package com.Z.NovelReader.Objects;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NovelSearchResult extends ViewModel {
    private MutableLiveData<ArrayList<NovelSearchBean>> mySearchResult;
    public static final int SORT_BY_NAME = 0X1;//按名称匹配排序
    public static final int SORT_BY_TIME = 0X2;//按响应时间排序
    public static final int SORT_BY_ID = 0X3;//按编书源号排序

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
    public boolean isEmpty(){
        ArrayList<NovelSearchBean> list = mySearchResult.getValue();
        if (list==null)return true;
        return list.isEmpty();
    }

    /**
     * 对搜索结果进行排序，默认从小到大排序
     * @param type 排序类型：内容匹配度、响应时间、ID大小顺序(书源新旧)
     */
    public void sortBy(int type, boolean isReversed){
        ArrayList<NovelSearchBean> data = mySearchResult.getValue();
        if (data == null)return;
        if (data.isEmpty())return;

        switch(type){
            case SORT_BY_NAME:{
                data.sort((o1, o2) -> {
                   return  (int) ((isReversed?-1:1) * (o2.getResultScore() - o1.getResultScore()));
                });
            }
                break;
            case SORT_BY_TIME:{
                data.sort((o1, o2) -> {
                    if (o1.getNovelRule() == null || o2.getNovelRule() == null)return 0;
                    return (int) ((isReversed?-1:1) * (o1.getNovelRule().getRespondTime() - o2.getNovelRule().getRespondTime()));
                });
            }
                break;
            case SORT_BY_ID:{
                data.sort((o1, o2) -> {
                    if (o1.getNovelRule() == null || o2.getNovelRule() == null)return 0;
                    return (int) ((isReversed?-1:1) * (o1.getNovelRule().getId() - o2.getNovelRule().getId()));
                });
            }
            default:
        }
        mySearchResult.setValue(data);
    }
}
