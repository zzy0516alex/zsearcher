package com.Z.NovelReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.BookshelfAdapter;
import com.Z.NovelReader.NovelRoom.NovelDBLiveData;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.beans.BookShelfItem;
import com.Z.NovelReader.Service.PrepareCatalogService;
import com.Z.NovelReader.Threads.GetCatalogThread;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.Handlers.GetCatalogHandler;
import com.Z.NovelReader.Threads.NovelUpdateThread;
import com.Z.NovelReader.Utils.BitmapUtils;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileOperateUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Utils.TimeUtil;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.views.Dialog.WaitDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.Z.NovelReader.Objects.NovelChap.getCurrentChapLink;
import static com.Z.NovelReader.Objects.NovelChap.initNovelChap;

public class BookShelfActivity extends AppCompatActivity {
    //书架数据
    private List<BookShelfItem> shelfItems = new ArrayList<>();//用于交互与展示的书架书籍类集合
    private Map<Integer,Novels> NovelMap = new HashMap<>();//所有书籍以ID为索引的map
    //外部数据源
    private SharedPreferences myInfo;//用户信息存储
    private NovelDBLiveData novelDBLiveData;//书籍信息数据库DAO
    private NovelDBTools novelDBTools;//书籍信息数据库更新DAO
    private Map<Integer,NovelRequire> novelRequireMap;//书源ID-规则对应表
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    //views
    private BookshelfAdapter adapter;//书籍grid适配器
    private RecyclerView bookShelf;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout bottom_controller;
    private Button btn_delete_book;
    private Button btn_cancel_selection;
    private Button btn_download_all;
    private WaitDialog waitDialog;
    private Window window;
    private ImageView menu;
    private LinearLayout searchBar;
    //环境&上下文
    Context context;
    Activity activity;
    //线程&handler
    private NovelUpdateThread.NovelUpdaterHandler updaterHandler;//目录更新线程 handler
    private GetCatalogHandler reloadHandler;//目录恢复 handler
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;//书籍刷新 handler
    private ExecutorService threadPool;//控制书籍更新的并发数
    //基本变量
    private Bitmap default_cover;//默认的书籍封面
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
        context=this;
        activity=this;
        content="缓存读取失败";
        updateTime=new Date();
        initDefaultCover();
        //init views
        bookShelf=findViewById(R.id.BookShelf);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        bookShelf.setLayoutManager(layoutManager);
        initSearchBar();
        initMenu();
        initWaitView();
        initSwipeRefresh();
        initStatusBar();
        initBottomController();
        //init preference
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        bookNum = myInfo.getInt("bookNum",0);
        long update_time = myInfo.getLong("update_time", 0);
        updateTime.setTime(update_time);

        //init dao
        novelDBTools = new NovelDBTools(context);
        //init service
        initService();

        // init handler
        //updaterHandler =new NovelUpdateThread.NovelUpdaterHandler<>(this);
        updaterHandler =new NovelUpdateThread.NovelUpdaterHandler();
        updaterHandler.setOverride(new NovelUpdateThread.NovelUpdaterHandler.NovelUpdateListener() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                update_success=Success;
                update_fail=Fail;
                if (NovelMap.size()==Success+Fail && !refresh_TimeUp){
                    if (swipeRefresh != null)swipeRefresh.setRefreshing(false);
                    String feedback = "成功：" + update_success + "\t 失败：" + update_fail;
                    Toast.makeText(context, "章节同步完成, " + feedback, Toast.LENGTH_SHORT).show();
                    refresh_Done=true;
                }
            }

            @Override
            public void needRecoverAll(Novels novel) {
                novelDBTools.updateRecoverStatus(novel.getId(),true);
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
                waitDialog.dismiss();
                novelDBTools.updateSpoiledStatus(current_book.getId(),true);
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
        novelDBLiveData = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(NovelDBLiveData.class);
        novelDBLiveData.getAllNovelsLD().observe(this, novels -> {
            SharedPreferences.Editor editor=myInfo.edit();
            editor.putInt("bookNum",novels.size()).apply();
            bookNum=novels.size();
            NovelMap.clear();
            List<BookShelfItem> newItemList = new ArrayList<>();
            for (Novels novel : novels) {
                //检查文件夹
                StorageUtils.createBookFolders(novel.getBookName(),novel.getWriter());
                //准备书架数据
                NovelMap.put(novel.getId(),novel);
                Bitmap adjustedCover = getAdjustedCover(
                        StorageUtils.getBookCoverPath(novel.getBookName(), novel.getWriter()));
                BookShelfItem item = new BookShelfItem(novel,adjustedCover);
                newItemList.add(item);
            }
            if (adapter == null){
                shelfItems = newItemList;
                adapter=new BookshelfAdapter(context,shelfItems);
                initViewClickListener();
                bookShelf.setAdapter(adapter);
            }
            else{
                updateAdapterByDiff(newItemList);
            }

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

    }

    private void initViewClickListener() {
        //书架交互
        adapter.setClickListener(v -> {
            int position = bookShelf.getChildAdapterPosition(v);
            if(!adapter.isSelectionMode()){
                BookShelfItem item = adapter.getItem(position);
                //移除更新
                if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                //获取当前书本
                current_book= NovelMap.get(item.getId());
                if(current_book==null){
                    Toast.makeText(context, "该书不存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("book shelf","打开书籍："+current_book.toString());
                if (current_book.isRecover()|| current_book.isSpoiled()){
                    Toast.makeText(context, "该书正在修复中", Toast.LENGTH_SHORT).show();
                    return;
                }
                //读取章节缓存
                content= FileIOUtils.readContent(
                        StorageUtils.getBookContentPath(current_book.getBookName(),current_book.getWriter()));
                //获取当前目录
                Catalog = new NovelCatalog();
                try {
                    Catalog= FileIOUtils.readCatalog(
                            StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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
                adapter.reverseSelection(position);
            }
        });

        adapter.setLongClickListener(v -> {
            int position = bookShelf.getChildAdapterPosition(v);
            if (!adapter.isSelectionMode()){
                adapter.setSelectionMode(true);
                adapter.select(position,true);
                bottom_controller.setVisibility(View.VISIBLE);
            }
            return true;
        });
    }

    private void initSearchBar() {
        searchBar=findViewById(R.id.search_bar);
        searchBar.setOnClickListener(v -> {
            Intent intent_novel_search=new Intent(BookShelfActivity.this, NovelSearchActivity.class);
            startActivity(intent_novel_search);
        });
    }

    //退出多选模式
    private void quitSelectMode(){
        adapter.setSelectionMode(false);
        bottom_controller.setVisibility(View.INVISIBLE);
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
                GetCatalogThread catalogThread = new GetCatalogThread(catalog_links.get(i),current_rule,current_book,i);
                catalogThread.setHandler(reloadHandler);
                catalog_thread_pool.execute(catalogThread);
            }
        }else {
            Toast.makeText(context, "目录文件缺失,启动自动修复", Toast.LENGTH_SHORT).show();
            novelDBTools.updateRecoverStatus(current_book.getId(),true);
            //adapter.notifyDataSetChanged();
            service_intent.putExtra("Novel",current_book);
            service_intent.putExtra("NovelRule",novelRequireMap.get(current_book.getSource()));
            service_intent.putExtra("catalogPath",StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
            service_intent.putExtra("catalogLinkPath",StorageUtils.getBookCatalogLinkPath(current_book.getBookName(),current_book.getWriter()));
            startService(service_intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter!=null && adapter.isSelectionMode()){
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
        menu=findViewById(R.id.menu);
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

    private void initBottomController(){
        bottom_controller = findViewById(R.id.bookshelf_bottom_control);
        bottom_controller.setVisibility(View.INVISIBLE);
        btn_delete_book =findViewById(R.id.bookshelf_delete);
        btn_cancel_selection =findViewById(R.id.bookshelf_cancel);
        //删除按钮点击
        btn_delete_book.setOnClickListener(v -> {
            //remove from database
            for (BookShelfItem item:adapter.getAllSelectedItem()) {
                novelDBTools.deleteNovelByShelfHash(item.getHash());
                //删除缓存的文件
                File dir_to_delete=new File(StorageUtils.getBookStoragePath(item.getBookName(),item.getWriter()));
                FileOperateUtils.deleteAllFiles(dir_to_delete);
            }
        });
        //取消按钮点击(通过系统返回按钮亦可退出)
        btn_cancel_selection.setOnClickListener(v -> {
            quitSelectMode();
        });
    }

    //初始化下拉刷新功能
    private void initSwipeRefresh() {
        swipeRefresh=findViewById(R.id.swipe_update);
        onRefreshListener = () -> {
            Date current_time= TimeUtil.getCurrentTimeInDate();
            if (TimeUtil.getDifference(updateTime,current_time,0)>2 && NovelMap.size()!=0) {
                updaterHandler.clearCounter();
                updateTime=current_time;
                Log.d("book shelf refresh","start update");
                //update catalog
                for (Novels novel : NovelMap.values()) {
                    if (novel.isRecover())
                        novelDBTools.updateRecoverStatus(novel.getId(),false);
                    if (novel.isSpoiled())
                        novelDBTools.updateSpoiledStatus(novel.getId(),false);
                    update_catalog(novel);
                }
                //check book cover
                try {
                    recover_BookCover();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                refresh_TimeUp=false;
                timerStart(10000);
            }else if (NovelMap.size()==0){
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
    private void recover_BookCover() throws Exception {
        List<BookShelfItem> newShelfItems = new ArrayList<>();
        for (int i = 0; i < shelfItems.size(); i++) {
            BookShelfItem current_item = (BookShelfItem) shelfItems.get(i).clone();
            if (current_item.getBookCover() == default_cover){
                //check file
                File pic = new File(StorageUtils.getBookCoverPath(current_item.getBookName(),current_item.getWriter()));
                if (pic.exists()){
                    //update cover from file storage
                    Bitmap cover = getAdjustedCover(
                            StorageUtils.getBookCoverPath(current_item.getBookName(),current_item.getWriter()));
                    current_item.setBookCover(cover);

                }else if (novelRequireMap!=null){
                    //update cover from internet
                    Novels novel = NovelMap.get(current_item.getId());
                    NovelRequire novelRequire = novelRequireMap.get(novel.getSource());
                    if (novelRequire!=null) {
                        String output_path = StorageUtils.getBookCoverPath(current_item.getBookName(),current_item.getWriter());
                        GetCoverThread update_cover_thread = new GetCoverThread(novel,novelRequire,output_path);
                        update_cover_thread.start();
                    }
                }
                newShelfItems.add(current_item);
            }
            else newShelfItems.add(current_item);
        }
        updateAdapterByDiff(newShelfItems);
    }

    private void updateAdapterByDiff(List<BookShelfItem> newShelfItems) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new BookShelfItem.BookShelfItemDiff(shelfItems, newShelfItems), false);
        adapter.setItems(newShelfItems);
        diff.dispatchUpdatesTo(adapter);
        shelfItems = newShelfItems;
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
                    int time_out = NovelMap.size() - update_fail - update_success;
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
                        novelDBTools.updateSpoiledStatus(target.getId(),true);
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
                        novelDBTools.updateRecoverStatus(novel.getId(),false);
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
