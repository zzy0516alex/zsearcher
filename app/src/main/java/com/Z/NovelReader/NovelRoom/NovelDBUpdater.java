package com.Z.NovelReader.NovelRoom;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.Z.NovelReader.BookShelfActivity;

import java.util.List;

public class NovelDBUpdater {
    private NovelDao Dao;
    public NovelDBUpdater(Context context){
        Dao= NovelDataBase.getDataBase(context).getNovelDao();
    }

    //更新书籍的当前章节
    public void updateTotalChap(int bookID, int total_chap){
        Log.d("update current chap","ID="+bookID+",chap="+total_chap);
        new UpdateTotalChapAsyncTask(Dao, bookID,total_chap).execute();
    }
    //更新书籍的当前章节
    public void updateCurrentChap(int bookID, int current_chap){
        Log.d("update current chap","ID="+bookID+",chap="+current_chap);
        new UpdateCurrentChapAsyncTask(Dao, bookID,current_chap).execute();
    }
    //更新书籍文本根链接
    public void updateContentRoot(int bookID, String ContentRoot){
        Log.d("update content root link","ID="+bookID+",link="+ContentRoot);
        new UpdateContentRootAsyncTask(Dao,bookID,ContentRoot).execute();
    }
    //更新书籍的阅读位置
    public void updateChapOffset(int id,double progress){
        new UpdateChapOffsetAsyncTask(Dao,progress,id).execute();
    }
    //更新书籍状态
    //修复中
    public void updateRecoverStatus(int id,boolean isRecovering){
        new UpdateRecoverStatusAsyncTask(Dao,isRecovering,id).execute();
    }
    //已损坏
    public void updateSpoiledStatus(int id,boolean isSpoiled){
        new UpdateSpoiledStatusAsyncTask(Dao,isSpoiled,id).execute();
    }
    //使用中
    public void updateUsedStatus(int id,boolean isUsed){
        new UpdateUsedStatusAsyncTask(Dao,isUsed,id).execute();
    }
    public void updateAllUsedStatus(String bookName,String writer,boolean isUsed){
        new UpdateAllUsedStatusAsyncTask(Dao,isUsed,bookName,writer).execute();
    }

    //根据书源号查询书籍
    public List<Novels> QueryNovelsBySource(int source) throws InterruptedException {
        QueryBySourceThread thread = new QueryBySourceThread(source, Dao);
        thread.start();
        return thread.getNovel_found();
    }
    //新增书籍
    public void insertNovels(Novels...novels){
        new NovelDBTools.InsertAsyncTask(Dao).execute(novels);
    }
    //删除书籍
    public void deleteNovels(Novels...novels){
        new NovelDBTools.DeleteAsyncTask(Dao).execute(novels);
    }
    //更新书籍
    public void updateNovels(Novels...novels){
        new NovelDBTools.UpdateAsyncTask(Dao).execute(novels);
    }

    static class UpdateTotalChapAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private int bookID;
        private int totalChap;

        public UpdateTotalChapAsyncTask(NovelDao novelDao, int bookID, int totalChap) {
            this.novelDao = novelDao;
            this.bookID = bookID;
            this.totalChap = totalChap;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateTotalChap(totalChap,bookID);
            return null;
        }
    }

    static class UpdateCurrentChapAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private int bookID;
        private int currentChap;

        public UpdateCurrentChapAsyncTask(NovelDao novelDao, int bookID, int currentChap) {
            this.novelDao = novelDao;
            this.bookID = bookID;
            this.currentChap = currentChap;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateCurrentChap(currentChap, bookID);
            return null;
        }
    }

    static class UpdateContentRootAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private int bookID;
        private String ContentRoot;

        public UpdateContentRootAsyncTask(NovelDao novelDao, int bookID, String ContentRoot) {
            this.novelDao = novelDao;
            this.bookID = bookID;
            this.ContentRoot = ContentRoot;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateContentRoot(ContentRoot,bookID);
            return null;
        }
    }

    static class UpdateChapOffsetAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private double progress;
        private int id;

        public UpdateChapOffsetAsyncTask(NovelDao novelDao, double progress, int id) {
            this.novelDao = novelDao;
            this.progress = progress;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateChapOffset(progress,id);
            return null;
        }
    }

    static class UpdateRecoverStatusAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private boolean isRecovering;
        private int id;

        public UpdateRecoverStatusAsyncTask(NovelDao novelDao, boolean isRecovering, int id) {
            this.novelDao = novelDao;
            this.isRecovering = isRecovering;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateRecoverStatus(isRecovering,id);
            return null;
        }
    }

    static class UpdateSpoiledStatusAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private boolean isSpoiled;
        private int id;

        public UpdateSpoiledStatusAsyncTask(NovelDao novelDao, boolean isSpoiled, int id) {
            this.novelDao = novelDao;
            this.isSpoiled = isSpoiled;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateSpoiledStatus(isSpoiled,id);
            return null;
        }
    }

    static class UpdateUsedStatusAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private boolean isUsed;
        private int id;

        public UpdateUsedStatusAsyncTask(NovelDao novelDao, boolean isUsed, int id) {
            this.novelDao = novelDao;
            this.isUsed = isUsed;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateUsedStatus(isUsed,id);
            return null;
        }
    }

    static class UpdateAllUsedStatusAsyncTask extends AsyncTask<Void,Void,Void> {
        private NovelDao novelDao;
        private boolean isUsed;
        private String bookName;
        private String writer;

        public UpdateAllUsedStatusAsyncTask(NovelDao novelDao, boolean isUsed, String bookName,String writer) {
            this.novelDao = novelDao;
            this.isUsed = isUsed;
            this.bookName = bookName;
            this.writer = writer;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            novelDao.UpdateAllUsedStatus(isUsed,bookName,writer);
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

    static class QueryBySourceThread extends Thread{

        private int source_id;
        private NovelDao novelDao;
        private List<Novels> novel_found;

        public QueryBySourceThread(int source_id, NovelDao novelDao) {
            this.source_id = source_id;
            this.novelDao = novelDao;
        }

        @Override
        public void run() {
            super.run();
            novel_found = novelDao.getNovelListBySource(source_id);
        }

        public List<Novels> getNovel_found() throws InterruptedException {
            join();
            return novel_found;
        }
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
}
