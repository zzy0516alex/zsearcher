package com.Z.NovelReader.NovelRoom;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NovelDBTools extends AndroidViewModel {
    private NovelDao Noveldao;
    private LiveData<List<Novels>> allNovelsLD;
    public NovelDBTools(@NonNull Application application) {
        super(application);
        NovelDataBase novelDataBase=NovelDataBase.getDataBase(application);
        Noveldao=novelDataBase.getNovelDao();
        allNovelsLD=Noveldao.getAllNovels();
    }

    public void insertNovels(Novels...novels){
        new InsertAsyncTask(Noveldao).execute(novels);
    }
    public void updateNovels(Novels...novels){
        new UpdateAsyncTask(Noveldao).execute(novels);
    }
    public void deleteNovels(Novels...novels){
        new DeleteAsyncTask(Noveldao).execute(novels);
    }
    public void deleteNovel(String bookName,String writer){
        new DeleteNovelAsyncTask(Noveldao,bookName,writer).execute();
    }
    public void deleteAll(){
        new DeleteAllAsyncTask(Noveldao).execute();
    }
    public void QueryNovelsByID(int id, QueryResultListener resultListener){
        new QueryByIDAsyncTask(Noveldao, id, resultListener).execute();
    }
    public void findSameBook(String bookName, String writer, QueryResultListener resultListener){
        new FindSameBookAsyncTask(Noveldao,resultListener,bookName,writer).execute();
    }

    public LiveData<List<Novels>> getAllNovelsLD() {
        return allNovelsLD;
    }
    public List<Novels> getNovelList(){
        return allNovelsLD.getValue();
    }

    static class InsertAsyncTask extends AsyncTask<Novels,Void,Void>{
        private NovelDao novelDao;

        public InsertAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Novels... novels) {
            novelDao.InsertNovels(novels);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Novels,Void,Void>{
        private NovelDao novelDao;

        public UpdateAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Novels... novels) {
            novelDao.UpdateNovels(novels);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Novels,Void,Void>{
        private NovelDao novelDao;

        public DeleteAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Novels... novels) {
            novelDao.DeleteNovels(novels);
            return null;
        }
    }
    static class DeleteNovelAsyncTask extends AsyncTask<Void,Void,Void>{
        private NovelDao novelDao;
        private String bookName;
        private String writer;

        public DeleteNovelAsyncTask(NovelDao novelDao,String bookName,String writer) {
            this.novelDao = novelDao;
            this.bookName = bookName;
            this.writer = writer;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.DeleteNovel(bookName,writer);
            return null;
        }
    }

    static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void>{
        private NovelDao novelDao;

        public DeleteAllAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.DeleteAll();
            return null;
        }
    }

    static class QueryByIDAsyncTask extends AsyncTask<Void,Void,List<Novels>>{
        private NovelDao novelDao;
        private QueryResultListener resultListener;
        private int id;

        public QueryByIDAsyncTask(NovelDao novelDao, int id, QueryResultListener resultListener) {
            this.novelDao = novelDao;
            this.resultListener=resultListener;
            this.id = id;
        }

        @Override
        protected List<Novels> doInBackground(Void... voids) {
            return novelDao.getNovelByID(id);
        }

        @Override
        protected void onPostExecute(List<Novels> novels) {
            resultListener.onQueryFinish(novels);
        }
    }

    static class FindSameBookAsyncTask extends AsyncTask<Void,Void,List<Novels>>{
        private NovelDao novelDao;
        private QueryResultListener resultListener;
        private String novel_name;
        private String writer;

        public FindSameBookAsyncTask(NovelDao novelDao, QueryResultListener resultListener, String novel_name, String writer) {
            this.novelDao = novelDao;
            this.resultListener = resultListener;
            this.novel_name = novel_name;
            this.writer = writer;
        }

        @Override
        protected List<Novels> doInBackground(Void... voids) {
            return novelDao.getNovelListUnique(novel_name,writer);
        }

        @Override
        protected void onPostExecute(List<Novels> novels) {
            resultListener.onQueryFinish(novels);
        }
    }


    public interface QueryResultListener{
        void onQueryFinish(List<Novels> novels);
    }

}
