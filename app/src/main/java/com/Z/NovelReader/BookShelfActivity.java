package com.Z.NovelReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.BookshelfAdapter;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Service.PrepareCatalogService;
import com.Z.NovelReader.Threads.GetCatalogThread;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.Handlers.GetCatalogHandler;
import com.Z.NovelReader.Threads.NovelUpdateThread;
import com.Z.NovelReader.Utils.BitmapUtils;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Utils.TimeUtil;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.views.Dialog.BottomSheetDialog;
import com.Z.NovelReader.views.Dialog.WaitDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.Z.NovelReader.Objects.NovelChap.getCurrentChapLink;
import static com.Z.NovelReader.Objects.NovelChap.initNovelChap;

public class BookShelfActivity extends AppCompatActivity {
    //书架数据
    private List<Bitmap> BookCover;
    private List<String> BookName;
    private List<Novels> AllNovelList;//所有书籍列表
    //外部数据源
    private SharedPreferences myInfo;//用户信息存储
    private NovelDBTools novelDBTools;//书籍信息数据库DAO
    private NovelDBUpdater novelDBUpdater;//书籍信息数据库更新DAO
    private Map<Integer,NovelRequire> novelRequireMap;//书源ID-规则对应表
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    //views
    private BookshelfAdapter adapter;//书籍grid适配器
    private GridView bookShelf;
    private SwipeRefreshLayout swipeRefresh;
    private Button delete;
    private WaitDialog waitDialog;
    private Window window;
    private ImageView menu;
    private LinearLayout searchBar;
    //环境&上下文
    Context context;
    Activity activity;
    //线程&handler
    private NovelUpdateThread.NovelUpdaterHandler<BookShelfActivity> updaterHandler;//目录更新线程 handler
    private GetCatalogHandler reloadHandler;//目录恢复 handler
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;//书籍刷新 handler
    private ExecutorService threadPool;//控制书籍更新的并发数
    //基本变量
    private Bitmap default_cover;//默认的书籍封面
    private boolean in_select_mode =false;//是否处于多选状态
    private List<Integer> item_chosen = new ArrayList<>();//被选中书籍的索引,多选
    private boolean refresh_TimeUp=false;
    private boolean refresh_Done=false;
    private Date updateTime;
    private int update_success=0;
    private int update_fail=0;
    private int bookNum = 0;
    //service
    private PrepareCatalogService service;
    private ServiceConnection connection;
    private Intent service_intent;
    //当前书籍数据
    private Novels current_book;
    private String content;//章节内容
    private NovelCatalog Catalog;//目录数据
    private NovelRequire current_rule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);

        //建立资源文件夹
        StorageUtils.createResFolders();
        //init basic params
        BookCover=new ArrayList<>();
        BookName=new ArrayList<>();
        context=this;
        activity=this;
        content="缓存读取失败";
        updateTime=new Date();
        initDefaultCover();
        //init views
        bookShelf=findViewById(R.id.BookShelf);
        delete=findViewById(R.id.delete);
        delete.setVisibility(View.INVISIBLE);
        swipeRefresh=findViewById(R.id.swipe_update);
        menu=findViewById(R.id.menu);
        searchBar=findViewById(R.id.search_bar);
        searchBar.setOnClickListener(v -> {
            Intent intent_novel_search=new Intent(BookShelfActivity.this, NovelSearchActivity.class);
            startActivity(intent_novel_search);
        });
        initMenu();
        initWaitView();
        initSwipeRefresh();
        initStatusBar();
        //init preference
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        bookNum = myInfo.getInt("bookNum",0);
        long update_time = myInfo.getLong("update_time", 0);
        updateTime.setTime(update_time);

        //init dao
        novelDBUpdater = new NovelDBUpdater(context);
        //init service
        initService();

        // init handler
        updaterHandler =new NovelUpdateThread.NovelUpdaterHandler<>(this);
        updaterHandler.setOverride(new NovelUpdateThread.NovelUpdaterHandler.NovelUpdateListener() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                update_success=Success;
                update_fail=Fail;
                if (AllNovelList.size()==Success+Fail && !refresh_TimeUp){
                    if (swipeRefresh != null)swipeRefresh.setRefreshing(false);
                    String feedback = "成功：" + update_success + "\t 失败：" + update_fail;
                    Toast.makeText(context, "章节同步完成, " + feedback, Toast.LENGTH_SHORT).show();
                    refresh_Done=true;
                }
            }

            @Override
            public void needRecoverAll(Novels novel) {
                novelDBUpdater.updateRecoverStatus(novel.getId(),true);
                service_intent.putExtra("Novel",novel);
                service_intent.putExtra("NovelRule",novelRequireMap.get(novel.getSource()));
                service_intent.putExtra("catalogPath",StorageUtils.getBookCatalogPath(novel.getBookName(),novel.getWriter()));
                service_intent.putExtra("catalogLinkPath",StorageUtils.getBookCatalogLinkPath(novel.getBookName(),novel.getWriter()));
                startService(service_intent);
            }
        });
        reloadHandler = new GetCatalogHandler(new GetCatalogHandler.GetCatalogListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onAllProcessDone(NovelCatalog catalog) {
                waitDialog.dismiss();
                startReadPage(context,content,current_book,catalog,current_rule);
            }

            @Override
            public void onFirstChapReady(NovelCatalog chap) {

            }
        });

        //thread
        threadPool = Executors.newFixedThreadPool(5);//一次更新5本书

        //init time
        final Date current_time=TimeUtil.getCurrentTimeInDate();

        //init database
        novelDBTools= new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(NovelDBTools.class);
        novelDBTools.getAllNovelsLD().observe(this, novels -> {
            AllNovelList=novels;
            SharedPreferences.Editor editor=myInfo.edit();
            editor.putInt("bookNum",novels.size()).apply();
            bookNum=novels.size();
            BookName.clear();
            BookCover.clear();
            for (Novels novel : novels) {
                //检查文件夹
                File folder = new File(StorageUtils.getBookStoragePath(novel.getBookName(),novel.getWriter()));
                if (!folder.exists())folder.mkdirs();
                //准备书架数据
                BookName.add(novel.getBookName());
                Bitmap adjustedCover = getAdjustedCover(
                        StorageUtils.getBookCoverPath(novel.getBookName(), novel.getWriter()));
                BookCover.add(adjustedCover);
            }

            if (adapter == null)
                adapter=new BookshelfAdapter(BookCover,AllNovelList,context);
            adapter.setBooks(AllNovelList);
            adapter.setBookCovers(BookCover);
            adapter.notifyDataSetChanged();
            bookShelf.setAdapter(adapter);

            if (TimeUtil.getDifference(updateTime,current_time,3)>=1){
                swipeRefresh.setRefreshing(true);
                onRefreshListener.onRefresh();
            }
        });

        //get book source
        sourceDBTools=new NovelSourceDBTools(context);
        sourceDBTools.getNovelRequireMap(object -> {
            if (object instanceof Map)
                novelRequireMap = (Map<Integer, NovelRequire>) object;
        });

        //书架点击
        bookShelf.setOnItemClickListener((parent, view, position, id) -> {
            if(!in_select_mode){
                //移除更新
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                //获取当前书本
                current_book=AllNovelList.get(position);
                Log.d("book shelf","打开书籍："+current_book.toString());
                if (current_book.isRecover()|| current_book.isSpoiled()){
                    Toast.makeText(context, "该书正在修复中", Toast.LENGTH_SHORT).show();
                    return;
                }
                //读取章节缓存
                content= FileIOUtils.read_line(
                        StorageUtils.getBookContentPath(current_book.getBookName(),current_book.getWriter()));
                //获取当前目录
                Catalog= FileIOUtils.read_catalog(
                        StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
                //获取当前书源规则
                if (novelRequireMap!=null)
                    current_rule = novelRequireMap.get(current_book.getSource());
                if (!Catalog.isEmpty() && current_rule!=null) {
                    Log.d("book shelf",current_book.getBookName()+":目录打开成功");
                    startReadPage(context, content, current_book, Catalog,current_rule);
                }
                else if (novelRequireMap!=null) {
                    if (current_rule == null) {
                        Toast.makeText(context, "该书书源被禁用，请先启用", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //重新下载目录
                        reloadCatalog();
                    }
                }
            }
            else {
                if (!isItemChosen(position))item_chosen.add(position);
                else item_chosen.remove((Integer) position);
                adapter.updateSelectItems(item_chosen);
            }
        });

        //书架长按，打开底部按钮
        bookShelf.setOnItemLongClickListener((parent, view, position, id) -> {
            if(!in_select_mode) {
                item_chosen.clear();
                item_chosen.add(position);
                adapter.updateSelectItems(item_chosen);
                in_select_mode = true;
                adapter.notifyDataSetChanged();
                delete.setVisibility(View.VISIBLE);
            }else {
                quitSelectMode();
            }
            return true;
        });

        //删除按钮点击
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove from database
                for (int i = 0; i < item_chosen.size(); i++) {
                    Novels novel_remove=AllNovelList.get(item_chosen.get(i));
                    novelDBTools.deleteNovel(novel_remove.getBookName(),novel_remove.getWriter());
                    //删除缓存的文件
                    File dir_to_delete=new File(StorageUtils.getBookStoragePath(novel_remove.getBookName(),novel_remove.getWriter()));
                    FileUtils.deleteAllFiles(dir_to_delete);
                }
                quitSelectMode();
            }
        });

    }

    //退出多选模式
    private void quitSelectMode(){
        in_select_mode = false;
        item_chosen.clear();
        adapter.updateSelectItems(item_chosen);
        delete.setVisibility(View.INVISIBLE);
    }

    private boolean isItemChosen(int index){
        return item_chosen.contains(index);
    }

    private void initDefaultCover() {
        Bitmap no_cover = BitmapFactory.decodeResource(getResources(), R.mipmap.no_book_cover);
        Bitmap resized_cover = BitmapUtils.getResizedBitmap(no_cover, 300, 400);
        default_cover = BitmapUtils.drawImageDropShadow(resized_cover);
    }

    private void reloadCatalog() {
        File catalog_link_file = new File(StorageUtils.getBookCatalogLinkPath(current_book.getBookName(),current_book.getWriter()));
        if (catalog_link_file.exists()){
            waitDialog.setTitle("目录修复中");
            waitDialog.show();
            List<String> catalog_links = FileIOUtils.read_list(catalog_link_file);
            ExecutorService catalog_thread_pool = Executors.newFixedThreadPool(15);
            reloadHandler.clearAll();
            reloadHandler.setTotal_count(catalog_links.size());
            reloadHandler.setOutput(
                    StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
            for (int i=0;i<catalog_links.size();i++) {
                GetCatalogThread catalogThread = new GetCatalogThread(catalog_links.get(i),current_rule,i);
                catalogThread.setHandler(reloadHandler);
                catalog_thread_pool.execute(catalogThread);
            }
        }else {
            Toast.makeText(context, "目录文件缺失,启动自动修复", Toast.LENGTH_SHORT).show();
            novelDBUpdater.updateRecoverStatus(current_book.getId(),true);
            adapter.notifyDataSetChanged();
            service_intent.putExtra("Novel",current_book);
            service_intent.putExtra("NovelRule",novelRequireMap.get(current_book.getSource()));
            service_intent.putExtra("catalogPath",StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
            service_intent.putExtra("catalogLinkPath",StorageUtils.getBookCatalogLinkPath(current_book.getBookName(),current_book.getWriter()));
            startService(service_intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (in_select_mode){
            quitSelectMode();
        }else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        //update book source
        sourceDBTools=new NovelSourceDBTools(context);
        sourceDBTools.getNovelRequireMap(object -> {
            if (object instanceof Map)
                novelRequireMap = (Map<Integer, NovelRequire>) object;
            Log.d("book shelf","更新 novel require");
        });
        SharedPreferences recoverInfo= context.getSharedPreferences("recoverInfo", Context.MODE_PRIVATE);
        recoverInfo.edit().putBoolean("onFront",false).apply();
        super.onResume();
    }

    private void initMenu() {
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookShelfActivity.this,SettingsActivity.class));
            }
        });
    }

    private void initStatusBar() {
        window= this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        window.setStatusBarColor(getResources().getColor(R.color.DoderBlue,null));
        StatusBarUtil.setStatusBarDarkTheme(this,false);
    }

    //初始化下拉刷新功能
    private void initSwipeRefresh() {
        onRefreshListener = () -> {
            Date current_time= TimeUtil.getCurrentTimeInDate();
            if (TimeUtil.getDifference(updateTime,current_time,0)>2 && AllNovelList.size()!=0) {
                updaterHandler.clearCounter();
                updateTime=current_time;
                Log.d("book shelf refresh","start update");
                //update catalog
                for (Novels novel : AllNovelList) {
                    if (novel.isRecover())
                        novelDBUpdater.updateRecoverStatus(novel.getId(),false);
                    if (novel.isSpoiled())
                        novelDBUpdater.updateSpoiledStatus(novel.getId(),false);
                    update_catalog(novel);
                }
                //check book cover
                recover_BookCover();

                refresh_TimeUp=false;
                timerStart(10000);
            }else if (AllNovelList.size()==0){
                Toast.makeText(context, "当前书架没有书籍，可以搜索看看", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
            else {
                Toast.makeText(context, "刷新过于频繁", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        };
        swipeRefresh.setOnRefreshListener(onRefreshListener);
    }

    //若书籍封面未加载则尝试修复
    private void recover_BookCover() {
        for (int i = 0; i < BookCover.size(); i++) {
            if (BookCover.get(i)==default_cover){
                //check file
                //String book_name=BookName.get(i);
                Novels current_novel = AllNovelList.get(i);
                File pic=new File(StorageUtils.getBookCoverPath(current_novel.getBookName(),current_novel.getWriter()));
                if (pic.exists()){
                    //update cover from file storage
                    Bitmap cover = getAdjustedCover(
                            StorageUtils.getBookCoverPath(current_novel.getBookName(),current_novel.getWriter()));
                    BookCover.set(i,cover);
                    adapter.setBookCovers(BookCover);
                    adapter.notifyDataSetChanged();
                }else if (novelRequireMap!=null){
                    //update cover from internet
                    Novels novel = AllNovelList.get(i);
                    NovelRequire novelRequire = novelRequireMap.get(novel.getSource());
                    if (novelRequire!=null) {
                        String output_path = StorageUtils.getBookCoverPath(current_novel.getBookName(),current_novel.getWriter());
                        GetCoverThread update_cover_thread = new GetCoverThread(novel,novelRequire,output_path);
                        update_cover_thread.start();
                    }
                }

            }
        }
    }

    /**
     * 下拉刷新超时处理
     * @param time
     */
    private void timerStart(int time) {
        new CountDownTimer(time + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (!refresh_Done) {
                    refresh_TimeUp=true;
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    int time_out = AllNovelList.size() - update_fail - update_success;
                    String feedback = "成功：" + update_success + "\t 失败：" + update_fail + "\t 超时：" + time_out;
                    Toast.makeText(context, "章节同步完成, " + feedback, Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor=myInfo.edit();
        editor.putLong("update_time",updateTime.getTime()).apply();
    }

    /**
     * 开启阅读界面
     * @param content 章节缓存内容
     * @param current_book 当前书本
     * @param novelCatalog 目录
     * @param novelRequire 书源规则
     */
    public static void startReadPage(Context context, String content, Novels current_book, NovelCatalog novelCatalog,NovelRequire novelRequire) {
        Intent intent=new Intent(context, NovelViewerActivity.class);
        Bundle bundle = new Bundle();
        String[] currentChap = getCurrentChapLink(current_book.getCurrentChap(), novelCatalog);
        NovelChap chap = new NovelChap(current_book);
        chap.setNovelRequire(novelRequire);
        if (content.contains("章节缓存出错"))chap.setOnError(true);
        initNovelChap(chap,content, currentChap);
        bundle.putSerializable("CurrentChap",chap);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void update_catalog(Novels novel) {
        if (novelRequireMap==null)return;
        Log.d("update catalog","start update");
        NovelRequire novelRequire = novelRequireMap.get(novel.getSource());
        NovelUpdateThread novelUpdateThread = new NovelUpdateThread(novelRequire,novel);
        novelUpdateThread.setHandler(updaterHandler);
        threadPool.execute(novelUpdateThread);
    }

    private void initService(){
        service_intent = new Intent(BookShelfActivity.this,PrepareCatalogService.class);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PrepareCatalogService.PrepareCatalogServiceBinder binder = (PrepareCatalogService.PrepareCatalogServiceBinder) iBinder;
                service = binder.getService();
                service.setListener(new PrepareCatalogService.CatalogServiceListener() {
                    @Override
                    public void onError(String info,Novels target) {
                        novelDBUpdater.updateSpoiledStatus(target.getId(),true);
                        runOnUiThread(() -> {
                            //todo adapter updata disable list
                            Toast.makeText(context, "目录修复失败:"+info, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onProcessStart(String process_name) {
                    }

                    @Override
                    public void onFirstChapReady(NovelCatalog chap) {
                    }

                    @Override
                    public void onAllProcessDone(Novels novel) {
                        novelDBUpdater.updateRecoverStatus(novel.getId(),false);
                        runOnUiThread(() -> {
                            Toast.makeText(context, "目录修复成功", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(service_intent, connection, BIND_AUTO_CREATE);
    }

    public Bitmap getAdjustedCover(String cover_path) {
        Bitmap bitmap = FileIOUtils.readBitmap(cover_path);
        if (bitmap == null)return default_cover;
        Bitmap bitmap_adjustSize=BitmapUtils.getResizedBitmap(bitmap,300,400);
        return BitmapUtils.drawImageDropShadow(bitmap_adjustSize);
    }

    public void setCatalog(NovelCatalog catalog) {
        Catalog = catalog;
    }

    public NovelCatalog getCatalog() {
        return Catalog;
    }

    public String getContent() {
        return content;
    }

    public Novels getCurrent_book() {
        return current_book;
    }

    private void initWaitView(){
        waitDialog=new WaitDialog(context,R.style.WaitDialog_black)
                .setTitle("资源预下载中");
    }
}
