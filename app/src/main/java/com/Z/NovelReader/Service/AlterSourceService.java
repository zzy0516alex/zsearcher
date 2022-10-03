package com.Z.NovelReader.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.Z.NovelReader.Basic.BasicCounterHandler;
import com.Z.NovelReader.Basic.BasicHandler;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Objects.beans.SearchQuery;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Threads.CatalogURLThread;
import com.Z.NovelReader.Threads.GetCatalogThread;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.JoinCatalogThread;
import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Threads.SubCatalogLinkThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlterSourceService extends Service {

    private AlterSourceServiceBinder binder = new AlterSourceServiceBinder();
    private AlterSourceProcessListener listener;
    private NotificationManager notificationManager;
    private Novels currentNovel;
    private NovelDBUpdater novelDBTools;
    private ArrayList<Integer>sourceException;
    private List<SearchQuery> searchQueryList;//书源中的书籍搜索规则列表
    private Map<Integer, NovelRequire> novelRequireMap;//书源ID-规则对应表
    private ExecutorService novelSearch_threadPool;//目录获取线程池
    private ArrayList<NovelSearchBean> searchResult;//保存可用的其他书源的搜索结果
    private BasicHandler<MapElement> subCatalogHandler;//子目录获取handler
    private BasicHandler<MapElement> catalogUrlHandler;//目录页链接获取handler
    private Map<NovelRequire,NovelCatalog> backupSourceMap = new HashMap<>();//后备源
    private boolean isAllProcessDone = false;
    //for debugger
    private int catalog_url_counter = 0;
    private int sub_catalog_counter = 0;
    private int catalog_get_counter = 0;

    public AlterSourceService() {
    }

    public class AlterSourceServiceBinder extends Binder{
        public AlterSourceService getService(){
            return AlterSourceService.this;
        }
    }

    public interface AlterSourceProcessListener{
        void onAllProcessDone(Map<NovelRequire,NovelCatalog> backupSourceMap);
    }

    public void setListener(AlterSourceProcessListener listener) {
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        novelSearch_threadPool = Executors.newFixedThreadPool(15);
        searchResult = new ArrayList<>();

        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel;
        //Android8.0要求设置通知渠道
        channel = new NotificationChannel("AlterSource", "alter source service", NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(null, null);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
//        RemoteViews views = setupNotificationView(0,0);
//        notificationManager.notify(5160,getNotification(views));
    }

    private RemoteViews setupNotificationView(int max,int progress){
        RemoteViews view = new RemoteViews(getPackageName(),R.layout.alter_source_notification);
        if (max <= progress && max!=0)view.setTextViewText(R.id.asn_tip,"Zsearcher: 书源准备就绪");
        else view.setTextViewText(R.id.asn_tip,"Zsearcher: 换源书源准备中");
        view.setTextViewText(R.id.asn_value,String.format(Locale.CHINA,"%d / %d",progress,max));
        view.setProgressBar(R.id.asn_progress,max==0?1000:max,progress,false);
        return view;
    }

    private Notification getNotification(RemoteViews views){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"AlterSource");
        Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher_round)
//                .setCustomBigContentView(views)
                .setCustomContentView(views)
                .setAutoCancel(true)
                .build();
        return notification;
    }
    private void updateNotification(RemoteViews views) {
        Notification notification = getNotification(views);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(5160, notification);
    }

    @Override
    public void onDestroy() {
        Log.d("ALterSourceService","service destroy");
        Toast.makeText(getApplicationContext(), "service destroy", Toast.LENGTH_SHORT).show();
        notificationManager.cancel(5160);
        super.onDestroy();
    }

    //用于与activity通信
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //执行业务
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (notificationManager!=null){
            notificationManager.cancel(5160);
            notificationManager.notify(5160,getNotification(setupNotificationView(0,0)));
        }
        catalog_url_counter = 0;
        sub_catalog_counter = 0;
        catalog_get_counter = 0;
        backupSourceMap.clear();
        searchResult.clear();
        isAllProcessDone = false;
        //获取备选书源并保存
        currentNovel = (Novels) intent.getSerializableExtra("Novel");
        sourceException = intent.getIntegerArrayListExtra("Exception");
        if (sourceException == null)sourceException = new ArrayList<>();
        new Thread(() -> {
            Looper.prepare();
            NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(getApplicationContext());
            searchQueryList = sourceDBTools.getSearchUrlList();
            novelRequireMap = sourceDBTools.getNovelRequireMap();
            novelDBTools= new NovelDBUpdater(getApplicationContext());

            File backup_folder = new File(StorageUtils.getBackupDir(currentNovel.getBookName(),currentNovel.getWriter()));
            if (!backup_folder.exists())backup_folder.mkdirs();

            subCatalogHandler = new BasicHandler<>(
                    new BasicHandler.BasicHandlerListener<MapElement>() {
                        @Override
                        public void onSuccess(MapElement element) {
                            sub_catalog_counter++;
                            List<String> result = (List<String>) element.value;
                            NovelRequire novelRequire = novelRequireMap.get(element.key);
                            if (novelRequire == null) return;
                            String sub_catalog_dir = StorageUtils.getBackupSourceSubCatalogDir(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId());
                            File sub_catalog_folder = new File(sub_catalog_dir);
                            if (!sub_catalog_folder.exists())sub_catalog_folder.mkdirs();

                            CountDownLatch countDownLatch = new CountDownLatch(result.size());
                            ExecutorService threadPool = Executors.newFixedThreadPool(15);
                            BasicHandler<MapElement> joinCatalogHandler = new BasicHandler<>(
                                    new BasicHandler.BasicHandlerListener<MapElement>() {
                                @Override
                                public void onSuccess(MapElement result) {
                                    backupSourceMap.put(novelRequireMap.get(result.key), (NovelCatalog) result.value);
                                    checkCounter();
                                }

                                @Override
                                public void onError(int error_code) {
                                    checkCounter();
                                }
                            });
                            JoinCatalogThread joinCatalogThread = new JoinCatalogThread(result.size(),
                                    sub_catalog_dir,
                                    StorageUtils.getBackupSourceCatalogPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId()),
                                    true);
                            joinCatalogThread.setSourceID(element.key);
                            joinCatalogThread.setHandler(joinCatalogHandler);
                            joinCatalogThread.setCountDownLatch(countDownLatch);
                            joinCatalogThread.start();
                            for (int i = 0; i < result.size(); i++) {
                                GetCatalogThread catalogThread = new GetCatalogThread(result.get(i),novelRequire,i);
                                catalogThread.setOutput(
                                        StorageUtils.getBackupSourceSubCatalogPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId(),i));
                                catalogThread.setCountDownLatch(countDownLatch);
                                threadPool.execute(catalogThread);
                            }
                        }

                        @Override
                        public void onError(int error_code) {
                            checkCounter();
                        }
                    });

            catalogUrlHandler = new BasicHandler<>(
                    new BasicHandler.BasicHandlerListener<MapElement>() {
                @Override
                public void onSuccess(MapElement result) {
                    int id = result.key;
                    for (NovelSearchBean searchBean : searchResult) {
                        if (searchBean.getSource() == id) {
                            searchBean.setBookCatalogLink((String) result.value);
                            getCatalog(searchBean);
                            break;
                        }
                    }
                }

                @Override
                public void onError(int error_code) {
                    checkCounter();
                }
            });

            NovelSearchThread.NovelSearchHandler searchHandler = new NovelSearchThread.NovelSearchHandler(searchQueryList.size(),
                    new NovelSearchThread.NovelSearchListener() {
                        @Override
                        public void onSearchResult(ArrayList<NovelSearchBean> search_result) {
                            for (NovelSearchBean result : search_result) {
                                if (result.getBookNameWithoutWriter().equals(currentNovel.getBookName())
                                        && result.getWriter().equals(currentNovel.getWriter())){
                                    if (sourceException.contains(result.getSource()))continue;
                                    searchResult.add(result);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onSearchFinish(int total_num, int num_no_internet, int num_not_found) {
                            Log.d("AlterSource service","available source found:"+searchResult.size());
                            for (NovelSearchBean searchBean : searchResult) {
                                NovelRequire novelRequire = novelRequireMap.get(searchBean.getSource());
                                if (novelRequire == null) continue;
                                //获取封面
                                Novels temp_novel = new Novels(searchBean.getBookNameWithoutWriter(),searchBean.getWriter(),
                                        0,0,"",searchBean.getBookInfoLink(),searchBean.getSource());
                                String outputPath = StorageUtils.getBackupSourceCoverPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId());
                                GetCoverThread thread_cover = new GetCoverThread(temp_novel,novelRequire,outputPath);
                                thread_cover.start();
                                //获取目录
                                String tocUrl = "";
                                if (novelRequire.getRuleBookInfo()!=null)
                                    tocUrl = novelRequire.getRuleBookInfo().getTocUrl();

                                if (tocUrl!=null && !"".equals(tocUrl)){
                                    //存在目录页，获取其链接
                                    CatalogURLThread catalogURLThread = new CatalogURLThread(searchBean.getBookInfoLink(),
                                            novelRequire);
                                    catalogURLThread.setHandler(catalogUrlHandler);
                                    catalogURLThread.start();
                                }else {
                                    //不存在额外目录页，默认使用书籍详情页链接作为目录页链接
                                    searchBean.setBookCatalogLink(searchBean.getBookInfoLink());
                                    getCatalog(searchBean);
                                }
                            }
                        }
                    });

            for (SearchQuery searchQ : searchQueryList) {
                NovelSearchThread t = new NovelSearchThread(novelRequireMap.get(searchQ.getId()),searchQ,currentNovel.getBookName());
                t.setHandler(searchHandler);
                novelSearch_threadPool.execute(t);
            }

            Looper.loop();
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkCounter(){
        catalog_get_counter++;
        updateNotification(setupNotificationView(searchResult.size(),catalog_get_counter));
        Log.d("AlterSourceService",String.format("Catalog Ready:%d/%d",catalog_get_counter,searchResult.size()));
        if (catalog_get_counter == searchResult.size()){
            Log.d("AlterSourceService","All process done");
            isAllProcessDone = true;
            if (listener!=null)
                listener.onAllProcessDone(backupSourceMap);
        }
    }

    public boolean isAllProcessDone() {
        return isAllProcessDone;
    }

    public Map<NovelRequire, NovelCatalog> getBackupSourceMap() {
        return backupSourceMap;
    }

    private void getCatalog(NovelSearchBean searchBean) {
        catalog_url_counter++;

        NovelRequire novelRequire = novelRequireMap.get(searchBean.getSource());
        if (novelRequire == null) return;
        //String outputPath = "/BookReserve/"+currentNovel.getBookName()+"/BackupSource/"+novelRequire.getId();
        File source_folder = new File(StorageUtils.getBackupSubDir(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId()));
        if (!source_folder.exists())source_folder.mkdirs();
        if (searchBean.getSource()!=currentNovel.getSource()) {
            Novels novel = new Novels(searchBean.getBookNameWithoutWriter(), searchBean.getWriter(), 0, 0,
                    searchBean.getBookCatalogLink(), searchBean.getBookInfoLink(), searchBean.getSource());
            novel.setUsed(false);
            novelDBTools.insertNovels(novel);
        }

        String subTocUrl = novelRequire.getRuleToc().getNextTocUrl();
        if (subTocUrl!=null && !"".equals(subTocUrl)) {
            Log.d("AlterSourceService","get catalog with sub catalog");
            //存在子目录
            SubCatalogLinkThread subCatalogLinkThread = new SubCatalogLinkThread(searchBean.getBookCatalogLink(),
                    novelRequire,false);
            subCatalogLinkThread.setOutputParams(
                    StorageUtils.getBackupSourceCatalogLinkPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId()));
            subCatalogLinkThread.setHandler(subCatalogHandler);
            subCatalogLinkThread.start();
        }else {
            sub_catalog_counter++;
            Log.d("AlterSourceService","get catalog directly");
            ArrayList<String> subCatalogLinkList = new ArrayList<>();
            subCatalogLinkList.add(searchBean.getBookCatalogLink());
            FileIOUtils.WriteList(
                    StorageUtils.getBackupSourceCatalogLinkPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId()),
                    subCatalogLinkList,false);

            BasicHandler<MapElement> catalogHandler = new BasicHandler<>(
                    new BasicHandler.BasicHandlerListener<MapElement>() {
                @Override
                public void onSuccess(MapElement result) {
                    checkCounter();
                    backupSourceMap.put(novelRequireMap.get(result.key), (NovelCatalog) result.value);
                }

                @Override
                public void onError(int error_code) {
                    checkCounter();
                }
            });

            GetCatalogThread catalogThread = new GetCatalogThread(searchBean.getBookCatalogLink(),
                    novelRequire,novelRequire.getId());
            catalogThread.setHandler(catalogHandler);
            catalogThread.setOutput(
                    StorageUtils.getBackupSourceCatalogPath(currentNovel.getBookName(),currentNovel.getWriter(),novelRequire.getId()));
            catalogThread.start();
        }
    }
}