package com.Z.NovelReader.NovelSourceRoom;

import android.content.Context;
import android.os.AsyncTask;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.SearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NovelSourceDBTools {
    private NovelSourceDao Dao;
    public NovelSourceDBTools(Context context){
        Dao=NovelSourceDB.getDataBase(context).getNovelSourceDao();
    }
    //插入书源
    public void InsertNovelSources(NovelRequire...sources){
        new InsertAsyncTask(Dao).execute(sources);
    }
    //删除指定书源
    public void DeleteNovelSources(NovelRequire...sources){
        new DeleteAsyncTask(Dao).execute(sources);
    }
    //删除所有书源
    public void DeleteAllSource(){
        new DeleteAllAsyncTask(Dao).execute();
    }
    //获取所有书源的搜索规则
    public void getSearchUrlList(QueryListener listener){
        new SearchQueryAsyncTask(Dao,listener).execute();
    }
    public List<SearchQuery> getSearchUrlList(){
        GetAllQueryThread getAllQueryThread = new GetAllQueryThread(Dao);
        getAllQueryThread.start();
        return getAllQueryThread.getSearchQueries();
    }
    //获取指定ID的书源规则
    public void getNovelRequireById(int id,QueryListener listener){
        new RulesQueryAsyncTask(Dao,id,listener).execute();
    }
    //更新书源的启用情况
    public void UpdateSourceVisibility(int id,boolean IsEnabled){
        new UpdateVisibilityAsyncTask(Dao,id,IsEnabled).execute();
    }
    //覆盖更新书源响应时间
    public void UpdateSourceRespondTime(int id,double respond_time){
        new UpdateRespondTimeAsyncTask(Dao,id,respond_time).execute();
    }
    //迭代更新书源响应时间
    public void IterativeUpdateSourceRespondTime(int id,double respond_time){
        new IterateRespondTimeAsyncTask(Dao,id,respond_time).execute();
    }
    //获取所有书源的 (id-规则) 对应表
    public void getNovelRequireMap(QueryListener listener){
        new RulesMapAsyncTask(Dao,listener).execute();
    }

    public Map<Integer,NovelRequire> getNovelRequireMap(){
        GetAllQueryThread getAllQueryThread = new GetAllQueryThread(Dao);
        getAllQueryThread.start();
        return getAllQueryThread.getRuleMap();
    }

    static class InsertAsyncTask extends AsyncTask<NovelRequire,Void,Void> {
        private NovelSourceDao novelSourceDao;

        public InsertAsyncTask(NovelSourceDao dao) {
            this.novelSourceDao = dao;
        }

        @Override
        protected Void doInBackground(NovelRequire... sources) {
            novelSourceDao.InsertSources(sources);
            return null;
        }
    }
    static class DeleteAsyncTask extends AsyncTask<NovelRequire,Void,Void> {
        private NovelSourceDao novelSourceDao;

        public DeleteAsyncTask(NovelSourceDao dao) {
            this.novelSourceDao = dao;
        }

        @Override
        protected Void doInBackground(NovelRequire... sources) {
            novelSourceDao.DeleteSources(sources);
            return null;
        }
    }

    static class SearchQueryAsyncTask extends AsyncTask<Void,Void, List<SearchQuery>>{

        private NovelSourceDao novelSourceDao;
        private QueryListener listener;

        public SearchQueryAsyncTask(NovelSourceDao novelSourceDao,QueryListener listener) {
            this.novelSourceDao = novelSourceDao;
            this.listener=listener;
        }

        @Override
        protected List<SearchQuery> doInBackground(Void... voids) {
            return novelSourceDao.getSearchUrls();
        }

        @Override
        protected void onPostExecute(List<SearchQuery> searchQueries) {
            listener.onResultBack(searchQueries);
        }
    }

    static class RulesQueryAsyncTask extends AsyncTask<Void,Void, NovelRequire>{

        private NovelSourceDao novelSourceDao;
        private QueryListener listener;
        private int id;

        public RulesQueryAsyncTask(NovelSourceDao novelSourceDao,int id,QueryListener listener) {
            this.novelSourceDao = novelSourceDao;
            this.listener=listener;
            this.id=id;
        }

        @Override
        protected NovelRequire doInBackground(Void... voids) {
            return novelSourceDao.getSourcesByID(id);
        }

        @Override
        protected void onPostExecute(NovelRequire novelRequire) {
            listener.onResultBack(novelRequire);
        }
    }

    static class UpdateVisibilityAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelSourceDao novelSourceDao;
        private int id;
        private boolean IsEnabled;

        public UpdateVisibilityAsyncTask(NovelSourceDao dao,int id,boolean IsEnabled) {
            this.novelSourceDao = dao;
            this.id=id;
            this.IsEnabled=IsEnabled;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            novelSourceDao.UpdateVisibility(IsEnabled,id);
            return null;
        }
    }
    static class UpdateRespondTimeAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelSourceDao novelSourceDao;
        private int id;
        private double respondTime;

        public UpdateRespondTimeAsyncTask(NovelSourceDao dao,int id,double respond_time) {
            this.novelSourceDao = dao;
            this.id=id;
            this.respondTime = respond_time;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            novelSourceDao.UpdateRespondTime(respondTime,id);
            return null;
        }
    }
    static class IterateRespondTimeAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelSourceDao novelSourceDao;
        private int id;
        private double respondTime;

        public IterateRespondTimeAsyncTask(NovelSourceDao dao,int id,double new_time) {
            this.novelSourceDao = dao;
            this.id=id;
            this.respondTime = new_time;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            novelSourceDao.IterativeUpdateRespondTime(respondTime,id);
            return null;
        }
    }
    static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelSourceDao novelSourceDao;

        public DeleteAllAsyncTask(NovelSourceDao dao) {
            this.novelSourceDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            novelSourceDao.DeleteAll();
            return null;
        }
    }

    static class RulesMapAsyncTask extends AsyncTask<Void,Void, Map<Integer,NovelRequire>>{

        private NovelSourceDao novelSourceDao;
        private QueryListener listener;

        public RulesMapAsyncTask(NovelSourceDao novelSourceDao,QueryListener listener) {
            this.novelSourceDao = novelSourceDao;
            this.listener=listener;
        }

        @Override
        protected Map<Integer,NovelRequire> doInBackground(Void... voids) {
            List<NovelRequire> sources = novelSourceDao.getSources();
            Map<Integer,NovelRequire> ruleMap=new HashMap<>();
            for (NovelRequire source : sources) {
                ruleMap.put(source.getId(),source);
            }
            return ruleMap;
        }

        @Override
        protected void onPostExecute(Map<Integer,NovelRequire> ruleMap) {
            listener.onResultBack(ruleMap);
        }
    }

    static class GetAllQueryThread extends Thread{
        private NovelSourceDao novelSourceDao;
        private Map<Integer,NovelRequire> ruleMap;
        private List<SearchQuery> searchQueries;

        public GetAllQueryThread(NovelSourceDao novelSourceDao) {
            this.novelSourceDao = novelSourceDao;
        }

        @Override
        public void run() {
            super.run();
            //rule map
            List<NovelRequire> sources = novelSourceDao.getSources();
            ruleMap=new HashMap<>();
            for (NovelRequire source : sources) {
                ruleMap.put(source.getId(),source);
            }
            //search query
            searchQueries = novelSourceDao.getSearchUrls();
        }

        public List<SearchQuery> getSearchQueries() {
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return searchQueries;
        }

        public Map<Integer, NovelRequire> getRuleMap() {
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ruleMap;
        }
    }

    public interface QueryListener{
        void onResultBack(Object object);
    }
}
