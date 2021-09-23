package com.Z.NovelReader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.BookshelfAdapter;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Threads.CatalogThread;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Utils.BitmapUtils;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.TimeUtil;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.NovelChap;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.views.WaitDialog;
import com.z.fileselectorlib.FileSelectorSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BookShelfActivity extends AppCompatActivity {
    List<Bitmap> BookCover;
    List<String> BookName;
    List<Novels> AllNovelList;//所有书籍列表
    NovelCatalog Catalog;//目录数据
    private SharedPreferences myInfo;//用户信息存储
    private NovelDBTools novelDBTools;//书籍信息数据库DAO
    private Map<Integer,NovelRequire> novelRequireMap;//书源ID-规则对应表
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private BookshelfAdapter adapter;//书籍grid适配器
    //views
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
    private CatalogThread.CatalogUpdaterHandler<BookShelfActivity> updaterHandler;//目录更新线程handler
    private CatalogReloadHandler reloadHandler;//目录重新下载线程handler
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;//书籍刷新handler
    //基本变量
    private Bitmap default_cover;
    private boolean is_item_chosen=false;
    private int item_chosen=-1;
    private Novels current_book;
    private String content;
    private boolean refresh_TimeUp=false;
    private boolean refresh_Done=false;
    private Date updateTime;
    private int update_success=0;
    private int update_fail=0;
    private int bookNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);
        File Folder1 =new File(getExternalFilesDir(null),"ZsearchRes");
        if(!Folder1.exists()){
            Folder1.mkdir();
        }
        File Folder2 =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookReserve");
        if(!Folder2.exists()){
            Folder2.mkdir();
        }
        File Folder3 =new File(FileSelectorSettings.getSystemRootPath() +"/download/","ZsearcherDownloads");
        if(!Folder3.exists()){
            Folder3.mkdir();
        }
        //init basic params
        BookCover=new ArrayList<>();
        BookName=new ArrayList<>();
        context=this;
        activity=this;
        updaterHandler =new CatalogThread.CatalogUpdaterHandler<>(this);
        reloadHandler=new CatalogReloadHandler(this);
        content="缓存读取失败";
        updateTime=new Date();
        default_cover=BitmapUtils.getResizedBitmap(BitmapFactory.decodeResource(
                getResources(),R.mipmap.no_book_cover),
                300,400);
        //init views
        bookShelf=findViewById(R.id.BookShelf);
        delete=findViewById(R.id.delete);
        delete.setVisibility(View.INVISIBLE);
        swipeRefresh=findViewById(R.id.swipe_update);
        menu=findViewById(R.id.menu);
        searchBar=findViewById(R.id.search_bar);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_novel_search=new Intent(BookShelfActivity.this, NovelSearchActivity.class);
                startActivity(intent_novel_search);
            }
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

        // init handler
        updaterHandler.setOverride(new CatalogThread.CatalogUpdaterHandler.MyHandle() {
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
        });

        //init time
        final Date current_time=TimeUtil.getCurrentTimeInDate();

        //init database
        novelDBTools= ViewModelProviders.of(this).get(NovelDBTools.class);
        novelDBTools.getAllNovelsLD().observe(this, new Observer<List<Novels>>() {
            @Override
            public void onChanged(List<Novels> novels) {
                AllNovelList=novels;
                SharedPreferences.Editor editor=myInfo.edit();
                editor.putInt("bookNum",novels.size()).apply();
                bookNum=novels.size();
                BookName.clear();
                BookCover.clear();
                for (Novels novel : novels) {
                    //检查文件夹
                    File folder = new File(getExternalFilesDir(null)+"/ZsearchRes/BookReserve/"+novel.getBookName());
                    if (!folder.exists())folder.mkdirs();
                    //准备书架数据
                    BookName.add(novel.getBookName());
                    BookCover.add(getImage(novel.getBookName()));
                }
                if (TimeUtil.getDifference(updateTime,current_time,3)>=1){
                    swipeRefresh.setRefreshing(true);
                    onRefreshListener.onRefresh();
                }
                if(bookNum!=0) {
                    adapter.setBookNames(BookName);
                    adapter.setBookCovers(BookCover);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        //get book source
        sourceDBTools=new NovelSourceDBTools(context);
        sourceDBTools.getNovelRequireMap( new NovelSourceDBTools.QueryListener() {
            @Override
            public void onResultBack(Object object) {
                if (object instanceof Map)
                    novelRequireMap = (Map<Integer, NovelRequire>) object;
            }
        });

        //书架点击
        if(bookNum!=0){
            adapter=new BookshelfAdapter(BookCover,BookName,context);
            bookShelf.setAdapter(adapter);
            bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!is_item_chosen){
                        if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                        content= FileIOUtils.read_line(BookName.get(position),getExternalFilesDir(null));
                        current_book=AllNovelList.get(position);
                        Catalog= FileIOUtils.read_catalog("/ZsearchRes/BookReserve/" + current_book.getBookName() + "/catalog.txt",
                                getExternalFilesDir(null));
                        if (!Catalog.isEmpty()) {
                            ArrayList<String> ChapName = Catalog.getTitle();
                            ArrayList<String> ChapLink = Catalog.getLink();
                            startReadPage(content, current_book, ChapName, ChapLink);
                        }else if (novelRequireMap!=null){
                            //重新下载catalog
                            waitDialog.show();
                            CatalogThread catalog_reload_thread=new CatalogThread(current_book.getBookCatalogLink(),
                                    novelRequireMap.get(current_book.getSource()),true,true);
                            catalog_reload_thread.setOutputParams(current_book.getBookName(),
                                    context.getExternalFilesDir(null));
                            catalog_reload_thread.setHandler(reloadHandler);
                            catalog_reload_thread.start();
                        }else {
                            Toast.makeText(context, "书源信息丢失，目录加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.INVISIBLE);
                    }
                }
            });

            //书架长按，打开底部按钮
            bookShelf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!is_item_chosen) {
                        adapter.setItem_chosen(position);
                        is_item_chosen = true;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.VISIBLE);
                        item_chosen=position;
                    }else {
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            });

            //删除按钮点击
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //notice remove book
                        //remove from database
                        String bookname_remove=AllNovelList.get(item_chosen).getBookName();
                        Novels novel_remove=AllNovelList.get(item_chosen);
                        novelDBTools.deleteNovels(novel_remove);
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;

                        //删除缓存的文件
                        File dir_to_delete=new File(getExternalFilesDir(null)+"/ZsearchRes/BookReserve/"+bookname_remove);
                        FileUtils.deleteAllFiles(dir_to_delete);
                        //
                        TimeUnit.MILLISECONDS.sleep(500);
                        delete.setVisibility(View.INVISIBLE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    protected void onResume() {
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
        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Date current_time= TimeUtil.getCurrentTimeInDate();
                if (TimeUtil.getDifference(updateTime,current_time,0)>2 && AllNovelList.size()!=0) {
                    updaterHandler.clearCounter();
                    updateTime=current_time;
                    //update catalog
                    for (Novels novel : AllNovelList) {
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
            }
        };
        swipeRefresh.setOnRefreshListener(onRefreshListener);
    }

    //若书籍封面未加载则尝试修复
    private void recover_BookCover() {
        for (int i = 0; i < BookCover.size(); i++) {
            if (BookCover.get(i)==default_cover){
                //check file
                String book_name=BookName.get(i);
                File pic=new File(getExternalFilesDir(null)+"/ZsearchRes/BookReserve/"+book_name+"/cover.png");
                if (pic.exists()){
                    //update cover from file storage
                    Bitmap cover = getImage(book_name);
                    BookCover.set(i,cover);
                    adapter.setBookCovers(BookCover);
                    adapter.notifyDataSetChanged();
                }else if (novelRequireMap!=null){
                    //update cover from internet
                    Novels novel = AllNovelList.get(i);
                    NovelRequire novelRequire = novelRequireMap.get(novel.getSource());
                    if (novelRequire!=null) {
                        GetCoverThread update_cover_thread = new GetCoverThread(novel,novelRequire,
                                getExternalFilesDir(null));
                        update_cover_thread.start();
                    }
                }

            }
        }
    }

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

    private void startReadPage(String content, Novels current_book, ArrayList<String> chapName, ArrayList<String> chapLink) {
        Intent intent=new Intent(BookShelfActivity.this, NovelViewerActivity.class);
        intent.putExtra("offset",current_book.getOffset());
        intent.putExtra("BookLink",current_book.getBookCatalogLink());
        String[] currentChap = getCurrentChapLink(current_book.getCurrentChap(), chapName, chapLink);
        NovelChap chap;
        chap = getNovelChap(content, currentChap);
        chap.setBookID(current_book.getId());
        chap.setBookName(current_book.getBookName());
        chap.setCurrent_chapter(current_book.getCurrentChap());
        //TODO need update
        //chap.setTag(current_book.getTag_in_TAG());
        NovelViewerActivity.setCurrent_chap(chap);
        startActivity(intent);
    }

    public static NovelChap getNovelChap(String content, String[] currentChap) {
        NovelChap chap;
        if (currentChap[1].equals("")) {
            chap = new NovelChap(currentChap[0], content, NovelChap.NEXT_LINK_ONLY, currentChap[2]);
        } else if (currentChap[2].equals("")) {
            chap = new NovelChap(currentChap[0], content, NovelChap.LAST_LINK_ONLY, currentChap[1]);
        } else {
            chap = new NovelChap(currentChap[0], content, NovelChap.BOTH_LINK_AVAILABLE, currentChap[1], currentChap[2]);
        }
        return chap;
    }

    private void update_catalog(Novels novel) {
        if (novelRequireMap==null)return;
        NovelRequire novelRequire = novelRequireMap.get(novel.getSource());
        if (novelRequire==null)return;
        CatalogThread catalogThread=new CatalogThread(novel.getBookCatalogLink(),novelRequire,true,false);
        catalogThread.setOutputParams(novel.getBookName(),getExternalFilesDir(null));
        catalogThread.setHandler(updaterHandler);
        catalogThread.start();
    }


    public Bitmap getImage(String BookName) {
        Bitmap bitmap_AddShadow=null;
        File picDir=new File(getExternalFilesDir(null)+"/ZsearchRes/BookReserve/" + BookName + "/cover.png");
        FileInputStream fis=null;
        try{
            fis=new FileInputStream(picDir);
            Bitmap bitmap= BitmapFactory.decodeStream(fis);
            Bitmap bitmap_adjustSize=BitmapUtils.getResizedBitmap(bitmap,300,400);
            bitmap_AddShadow= BitmapUtils.drawImageDropShadow(bitmap_adjustSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return default_cover;
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap_AddShadow;
    }



    public static String[] getCurrentChapLink(int current_chap, ArrayList<String> chapName, ArrayList<String> chapLink) {
        String[] result=new String[3];//0:name 1:last_link 2:next_link
        result[0]= chapName.get(current_chap);
        if(current_chap>0)
            result[1]= chapLink.get(current_chap-1);
        else result[1]="";
        if(current_chap< chapLink.size()-1)
            result[2]= chapLink.get(current_chap+1);
        else result[2]="";
        return result;
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

    public static class CatalogReloadHandler extends Handler {
        //todo 考虑书源被删除的情况
        private final WeakReference<BookShelfActivity> mActivity;
        private int counter;

        CatalogReloadHandler(BookShelfActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BookShelfActivity activity = mActivity.get();
            if (msg.what==CatalogThread.CATALOG_UPDATE_FAILED)
                Toast.makeText(activity, "无网络", Toast.LENGTH_SHORT).show();
            if (activity != null && msg.obj!=null) {
                NovelCatalog catalog= (NovelCatalog) msg.obj;
                activity.setCatalog(catalog);
                activity.startReadPage(activity.getContent(),activity.getCurrent_book(),catalog.getTitle(),catalog.getLink());
            }
            assert activity != null;
            activity.waitDialog.dismiss();
        }
    }
}
