package com.Z.NovelReader.NovelRoom;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class NovelDBTools {
    public enum DBMethods{Insert,Update,DeleteSingle,DeleteAll}
    public enum NovelColumns{book_name,writer,total_chapter,current_chap,book_catalog_link,book_info_link}
    private NovelDao novelDao;
    private NovelParamDao novelParamDao;
    public NovelDBTools(Context context){
        novelDao = NovelDataBase.getDataBase(context).getNovelDao();
        novelParamDao = NovelDataBase.getDataBase(context).getNovelParamDao();
    }

    //更新书籍的当前章节
    public void updateTotalChap(int bookID, int total_chap){
        Log.d("update current chap","ID="+bookID+",chap="+total_chap);
        new UpdateTotalChapAsyncTask(novelDao, bookID,total_chap).execute();
    }
    //更新书籍的当前章节
    public void updateCurrentChap(int bookID, int current_chap){
        Log.d("update current chap","ID="+bookID+",chap="+current_chap);
        new UpdateCurrentChapAsyncTask(novelDao, bookID,current_chap).execute();
    }
    //更新书籍文本根链接
    public void updateContentRoot(int bookID, String ContentRoot){
        Log.d("update content root link","ID="+bookID+",link="+ContentRoot);
        new UpdateContentRootAsyncTask(novelDao,bookID,ContentRoot).execute();
    }
    //更新书籍的阅读位置
    public void updateChapOffset(int id,double progress){
        new UpdateChapOffsetAsyncTask(novelDao,progress,id).execute();
    }
    //更新书籍状态
    //修复中
    public void updateRecoverStatus(int id,boolean isRecovering){
        new UpdateRecoverStatusAsyncTask(novelDao,isRecovering,id).execute();
    }
    //已损坏
    public void updateSpoiledStatus(int id,boolean isSpoiled){
        new UpdateSpoiledStatusAsyncTask(novelDao,isSpoiled,id).execute();
    }
    //使用中
    public void updateUsedStatus(int id,boolean isUsed){
        new UpdateUsedStatusAsyncTask(novelDao,isUsed,id).execute();
    }
    //可用性
    public void updateAllUsedStatus(String bookName,String writer,boolean isUsed){
        new UpdateAllUsedStatusAsyncTask(novelDao,isUsed,bookName,writer).execute();
    }

    public void updateColumn(NovelColumns column, String update_data, int id){
        new CommonNovelUpdateTask(novelDao,column,update_data,id).execute();
    }
    public void queryNovelByID(int id, QueryListener<List<Novels>> listener){
        new QueryNovelsByIDTask(novelDao,listener).execute(id);
    }
    //根据书源号查询书籍
    public void queryNovelsBySource(int source, QueryListener<List<Novels>> listener) {
        new QueryNovelsBySourceTask(novelDao,listener).execute(source);
    }
    public void queryNovelsByHash(int hash_value, QueryListener<List<Novels>> listener){
        new QueryNovelsByShelfHash(novelDao,listener).execute(hash_value);
    }
    public void queryNovelBySourceAndHash(int source_id, int hash_value, QueryListener<List<Novels>> listener){
        new QueryNovelBySourceAndHash(source_id,hash_value,novelDao,listener).execute();
    }
    public void queryNovelsByNameAndWriter(String bookName, String writer, QueryListener<List<Novels>> listener){
        new QueryNovelsByNameAndWriter(bookName,writer,novelDao,listener).execute();
    }
    //新增书籍
    public void insertNovels(QueryListener<Novels> insertListener, Novels novel){
        new InsertNovelAsyncTask(novelDao, insertListener).execute(novel);
    }
    //删除书籍
    public void deleteNovels(Novels...novels){
        new DeleteNovelAsyncTask(novelDao).execute(novels);
    }
    //通过hash删除书籍
    public void deleteNovelByShelfHash(int hash){
        new DeleteNovelByShelfHashTask(novelDao,hash).execute();
    }

    /**更新书籍*/
    public void updateNovels(Novels...novels){
        new UpdateNovelAsyncTask(novelDao).execute(novels);
    }

    /**书籍参数增删改查
     * @param methods 操作方法
     * @param novelParams 指定条目，若为DeleteAll则无需传参
     */
    public void operateNovelParams(DBMethods methods, NovelParams...novelParams){
        new BasicNovelParamsTask(novelParamDao, methods).execute(novelParams);
    }

    /**查找书籍参数*/
    public void getNovelParamsByIds(int novelID, int sourceID, QueryListener<NovelParams> queryListener){
        new QueryNovelParamTask(novelParamDao,novelID,sourceID,queryListener).execute();
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

    static class CommonNovelUpdateTask extends AsyncTask<Void,Void,Void>{
        private NovelDao novelDao;
        private NovelColumns column;
        private String update_data;
        private int id;

        public CommonNovelUpdateTask(NovelDao novelDao, NovelColumns column, String update_data, int id) {
            this.novelDao = novelDao;
            this.column = column;
            this.update_data = update_data;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            switch(column){
                case book_name:
                    break;
                case writer:
                    break;
                case total_chapter:
                    novelDao.UpdateTotalChap(Integer.parseInt(update_data),id);
                    break;
                case current_chap:
                    novelDao.UpdateCurrentChap(Integer.parseInt(update_data),id);
                    break;
                case book_catalog_link:
                    novelDao.UpdateCatalogLink(update_data,id);
                    break;
                case book_info_link:
                    novelDao.UpdateInfoLink(update_data,id);
                    break;
                default:
            }
            return null;
        }
    }

    static class DeleteNovelAsyncTask extends AsyncTask<Novels,Void,Void>{
        private NovelDao novelDao;

        public DeleteNovelAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Novels... novels) {
            novelDao.DeleteNovels(novels);
            return null;
        }
    }

    static class DeleteNovelByShelfHashTask extends AsyncTask<Void,Void,Void>{
        private NovelDao novelDao;
        private int hash;

        public DeleteNovelByShelfHashTask(NovelDao novelDao, int hash) {
            this.novelDao = novelDao;
            this.hash = hash;
        }

        @Override
        protected Void doInBackground(Void... Void) {
            novelDao.DeleteNovelByShelfHash(hash);
            return null;
        }
    }

    static class QueryNovelsByNameAndWriter extends AsyncTask<Void,Void,List<Novels>>{
        String bookName;
        String writer;
        private NovelDao novelDao;
        private QueryListener<List<Novels>> queryListener;

        public QueryNovelsByNameAndWriter(String bookName, String writer, NovelDao novelDao, QueryListener<List<Novels>> queryListener) {
            this.bookName = bookName;
            this.writer = writer;
            this.novelDao = novelDao;
            this.queryListener = queryListener;
        }

        @Override
        protected List<Novels> doInBackground(Void... voids) {
            return novelDao.getNovelListUnique(bookName,writer);
        }

        @Override
        protected void onPostExecute(List<Novels> novelsList) {
            queryListener.onResultBack(novelsList);
        }
    }

    static class QueryNovelsByIDTask extends AsyncTask<Integer,Void,List<Novels>>{
        private NovelDao novelDao;
        private QueryListener<List<Novels>> queryListener;

        public QueryNovelsByIDTask(NovelDao novelDao, QueryListener<List<Novels>> queryListener) {
            this.novelDao = novelDao;
            this.queryListener = queryListener;
        }

        @Override
        protected List<Novels> doInBackground(Integer... id) {
            return novelDao.getNovelByID(id[0]);
        }

        @Override
        protected void onPostExecute(List<Novels> novelsList) {
            queryListener.onResultBack(novelsList);
        }
    }

    static class QueryNovelsBySourceTask extends AsyncTask<Integer,Void,List<Novels>>{

        private NovelDao novelDao;
        private QueryListener<List<Novels>> queryListener;

        public QueryNovelsBySourceTask(NovelDao novelDao, QueryListener<List<Novels>> queryListener) {
            this.novelDao = novelDao;
            this.queryListener = queryListener;
        }

        @Override
        protected List<Novels> doInBackground(Integer...source_id) {
            return novelDao.getNovelListBySource(source_id[0]);
        }

        @Override
        protected void onPostExecute(List<Novels> novels) {
            queryListener.onResultBack(novels);
        }
    }

    static class QueryNovelsByShelfHash extends AsyncTask<Integer,Void,List<Novels>>{
        private NovelDao novelDao;
        private QueryListener<List<Novels>> queryListener;

        public QueryNovelsByShelfHash(NovelDao novelDao, QueryListener<List<Novels>> queryListener) {
            this.novelDao = novelDao;
            this.queryListener = queryListener;
        }

        @Override
        protected List<Novels> doInBackground(Integer... hash_value) {
            return novelDao.getNovelListByShelfHash(hash_value[0]);
        }

        @Override
        protected void onPostExecute(List<Novels> novels) {
            queryListener.onResultBack(novels);
        }
    }

    static class QueryNovelBySourceAndHash extends AsyncTask<Void,Void,List<Novels>>{

        int source_id;
        int hash_value;
        private NovelDao novelDao;
        private QueryListener<List<Novels>> queryListener;

        public QueryNovelBySourceAndHash(int source_id, int hash_value, NovelDao novelDao, QueryListener<List<Novels>> queryListener) {
            this.source_id = source_id;
            this.hash_value = hash_value;
            this.novelDao = novelDao;
            this.queryListener = queryListener;
        }

        @Override
        protected List<Novels> doInBackground(Void... voids) {
            return novelDao.getNovelListBySourceAndHash(source_id,hash_value);
        }

        @Override
        protected void onPostExecute(List<Novels> novels) {
            queryListener.onResultBack(novels);
        }
    }

    static class InsertNovelAsyncTask extends AsyncTask<Novels,Void,Novels>{
        private NovelDao novelDao;
        private QueryListener<Novels> insertListener;

        public InsertNovelAsyncTask(NovelDao novelDao, QueryListener<Novels> insertListener) {
            this.novelDao = novelDao;
            this.insertListener = insertListener;
        }

        @Override
        protected Novels doInBackground(Novels... novels) {
            if(novels.length!=1)throw new RuntimeException("插入书籍数量异常");
            int id = (int) novelDao.InsertNovel(novels[0]);
            novels[0].setId(id);
            return novels[0];
        }

        @Override
        protected void onPostExecute(Novels novels) {
            insertListener.onResultBack(novels);
        }
    }
    static class UpdateNovelAsyncTask extends AsyncTask<Novels,Void,Void>{
        private NovelDao novelDao;

        public UpdateNovelAsyncTask(NovelDao novelDao) {
            this.novelDao = novelDao;
        }

        @Override
        protected Void doInBackground(Novels... novels) {
            novelDao.UpdateNovels(novels);
            return null;
        }
    }

    static class BasicNovelParamsTask extends AsyncTask<NovelParams,Void,Void>{

        private NovelParamDao novelParamDao;
        private DBMethods methods;

        public BasicNovelParamsTask(NovelParamDao novelParamDao, DBMethods methods) {
            this.novelParamDao = novelParamDao;
            this.methods = methods;
        }

        @Override
        protected Void doInBackground(NovelParams... novelParams) {
            switch(methods){
                case Insert:
                    novelParamDao.insertNovelParams(novelParams);
                    break;
                case Update:
                    novelParamDao.updateNovelParams(novelParams);
                    break;
                default:
            }
            return null;
        }
    }

    static class QueryNovelParamTask extends AsyncTask<Void,Void,NovelParams>{

        NovelParamDao novelParamDao;
        int novel_id;
        int source_id;
        QueryListener<NovelParams> listener;

        public QueryNovelParamTask(NovelParamDao novelParamDao, int novel_id, int source_id, QueryListener<NovelParams> listener) {
            this.novelParamDao = novelParamDao;
            this.novel_id = novel_id;
            this.source_id = source_id;
            this.listener = listener;
        }

        @Override
        protected NovelParams doInBackground(Void... voids) {
            return novelParamDao.getNovelParamByID(novel_id,source_id);
        }

        @Override
        protected void onPostExecute(NovelParams novelParams) {
            listener.onResultBack(novelParams);
        }
    }

    public interface QueryListener<T>{
        void onResultBack(T result);
    }
}
