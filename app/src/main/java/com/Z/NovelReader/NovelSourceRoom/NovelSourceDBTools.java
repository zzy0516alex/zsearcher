package com.Z.NovelReader.NovelSourceRoom;

import android.content.Context;
import android.os.AsyncTask;

import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.SearchQuery;

import java.util.List;

public class NovelSourceDBTools {
    private NovelSourceDao Dao;
    public NovelSourceDBTools(Context context){
        Dao=NovelSourceDB.getDataBase(context).getNovelSourceDao();
    }

    public void InsertNovelSources(NovelRequire...sources){
        new InsertAsyncTask(Dao).execute(sources);
    }
    public void DeleteNovelSources(NovelRequire...sources){
        new DeleteAsyncTask(Dao).execute(sources);
    }
    public void DeleteAllSource(){
        new DeleteAllAsyncTask(Dao).execute();
    }
    public void getSearchUrlList(QueryListener listener){
        new SearchQueryAsyncTask(Dao,listener).execute();
    }
    public void getNovelRequireById(int id,QueryListener listener){
        new RulesQueryAsyncTask(Dao,id,listener).execute();
    }
    public void UpdateSourceVisibility(int id,boolean IsEnabled){
        new UpdateVisibilityAsyncTask(Dao,id,IsEnabled).execute();
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

    public interface QueryListener{
        void onResultBack(Object object);
    }
}
