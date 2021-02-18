package com.example.helloworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.helloworld.Adapters.BookshelfAdapter;
import com.example.helloworld.NovelRoom.NovelDBTools;
import com.example.helloworld.NovelRoom.Novels;
import com.example.helloworld.Threads.CatalogThread;
import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.Utils.StatusBarUtil;
import com.example.helloworld.Utils.TimeUtil;
import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelChap;
import com.z.fileselectorlib.FileSelectorActivity;
import com.z.fileselectorlib.FileSelectorSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BookShelfActivity extends AppCompatActivity {
    List<Bitmap> BookCover;
    List<String> BookName;
    List<Novels> AllNovelList;
    NovelCatalog Catalog;
    private SharedPreferences myInfo;
    private NovelDBTools novelDBTools;
    private BookshelfAdapter adapter;
    private GridView bookShelf;
    private SwipeRefreshLayout swipeRefresh;
    private Button delete;
    private Dialog wait_dialog;
    private Date updateTime;
    private Window window;
    private ImageView menu;
    private LinearLayout searchBar;
    Context context;
    Activity activity;
    private CatalogThread.CatalogUpdaterHandler<BookShelfActivity> updaterHandler;
    private CatalogReloadHandler reloadHandler;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private boolean is_item_chosen=false;
    private int item_chosen=-1;
    private Novels current_book;
    String content;
    private boolean refresh_TimeUp=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);
        File Folder1 =new File(getExternalFilesDir(null),"ZsearchRes");
        if(!Folder1.exists()){
            Folder1.mkdir();
        }
        File Folder2 =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookCovers");
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
        content="";
        updateTime=new Date();
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
                Intent intent_novel_search=new Intent(BookShelfActivity.this,NovelActivity.class);
                startActivity(intent_novel_search);
            }
        });
        initMenu();
        initWaitView();
        initSwipeRefresh();
        initStatusBar();
        //init preference
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        final int num = myInfo.getInt("bookNum",0);
        long update_time = myInfo.getLong("update_time", 0);
        updateTime.setTime(update_time);

        // init handler
        updaterHandler.setOverride(new CatalogThread.CatalogUpdaterHandler.MyHandle() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                if (AllNovelList.size() == Success + Fail || refresh_TimeUp) {
                    if (swipeRefresh != null)swipeRefresh.setRefreshing(false);
                        String feedback = "成功：" + Success + "\t 失败：" + Fail;
                        Toast.makeText(context, "章节同步完成, " + feedback, Toast.LENGTH_SHORT).show();
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
                BookName.clear();
                BookCover.clear();
                for (Novels novel : novels) {
                    BookName.add(novel.getBookName());
                    BookCover.add(getImage(novel.getBookName()));
                }
                if (TimeUtil.getDifference(updateTime,current_time,3)>=1){
                    swipeRefresh.setRefreshing(true);
                    onRefreshListener.onRefresh();
                }
                if(num!=0) {
                    adapter.setBookNames(BookName);
                    adapter.setBookCovers(BookCover);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        if(num!=0){
            adapter=new BookshelfAdapter(BookCover,BookName,context);
            bookShelf.setAdapter(adapter);
            bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!is_item_chosen){
                        if (swipeRefresh.isRefreshing())swipeRefresh.setRefreshing(false);
                        //TODO show content
                        content=IOtxt.read_line(BookName.get(position),getExternalFilesDir(null));
                        current_book=AllNovelList.get(position);
                        Catalog= IOtxt.read_catalog(current_book.getBookName(),getExternalFilesDir(null));
                        if (!Catalog.isEmpty()) {
                            ArrayList<String> ChapName = Catalog.getTitle();
                            ArrayList<String> ChapLink = Catalog.getLink();
                            startReadPage(content, current_book, ChapName, ChapLink);
                        }else{
                            //重新下载catalog
                            wait_dialog.show();
                            CatalogThread Cthread=new CatalogThread(current_book.getBookLink(),current_book.getTag_in_TAG(),false,true);
                            Cthread.setHandler(reloadHandler);
                            Cthread.start();
                        }
                    }else {
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.INVISIBLE);
                    }
                }
            });
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
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //TODO remove book
                        //remove from database
                        String bookname_remove=AllNovelList.get(item_chosen).getBookName();
                        Novels novel_remove=AllNovelList.get(item_chosen);
                        novelDBTools.deleteNovels(novel_remove);
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;

                        //remove book cover
                        removeBookCover(bookname_remove);
                        //remove book contents
                        removeBookContents(bookname_remove);
                        removeBookContents(bookname_remove+"_catalog");

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

    private void initMenu() {
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    menu.setImageDrawable(getDrawable(R.mipmap.menu_onclick));
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    menu.setImageDrawable(getDrawable(R.mipmap.menu));
                    startActivity(new Intent(BookShelfActivity.this,SettingsActivity.class));
                }
                return true;
            }
        });
    }

    private void initStatusBar() {
        window= this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        window.setStatusBarColor(getResources().getColor(R.color.DoderBlue));
        StatusBarUtil.setStatusBarDarkTheme(this,false);
    }

    private void initSwipeRefresh() {
        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Date current_time= TimeUtil.getCurrentTimeInDate();
                if (TimeUtil.getDifference(updateTime,current_time,0)>30) {
                    updaterHandler.clearCounter();
                    updateTime=current_time;
                    //update catalog
                    for (Novels novel : AllNovelList) {
                        update_catalog(novel);
                    }
                    refresh_TimeUp=false;
                    timerStart(10000);
                }else {
                    Toast.makeText(context, "刷新过于频繁", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                }
            }
        };
        swipeRefresh.setOnRefreshListener(onRefreshListener);
    }

    private void timerStart(int time) {
        new CountDownTimer(time + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                refresh_TimeUp=true;
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
        intent.putExtra("BookLink",current_book.getBookLink());
        String[] currentChap = getCurrentChap(current_book.getCurrentChap(), chapName, chapLink);
        NovelChap chap;
        if (currentChap[1].equals("")) {
            chap = new NovelChap(currentChap[0], content, NovelChap.NEXT_LINK_ONLY, currentChap[2]);
        } else if (currentChap[2].equals("")) {
            chap = new NovelChap(currentChap[0], content, NovelChap.LAST_LINK_ONLY, currentChap[1]);
        } else {
            chap = new NovelChap(currentChap[0], content, NovelChap.BOTH_LINK_AVAILABLE, currentChap[1], currentChap[2]);
        }
        chap.setBookID(current_book.getId());
        chap.setBookName(current_book.getBookName());
        chap.setCurrent_chapter(current_book.getCurrentChap());
        chap.setTag(current_book.getTag_in_TAG());
        NovelViewerActivity.setCurrent_chap(chap);
        startActivity(intent);
    }

    private void update_catalog(Novels novel) {
        CatalogThread catalogThread=new CatalogThread(novel.getBookLink(), novel.getTag_in_TAG(),true,false);
        catalogThread.setOutputParams(novel.getBookName(),getExternalFilesDir(null));
        catalogThread.setHandler(updaterHandler);
        catalogThread.start();
    }

    private void removeBookCover(final String bookname_remove) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File delefile = new File(getExternalFilesDir(null)+"/ZsearchRes/BookCovers/"+bookname_remove+".png");
                if(delefile.exists() && delefile.isFile()) {
                    if(delefile.delete()){
                        Log.e("delete","success");
                    }else{
                        Log.e("delete","fail");
                    }
                }else {
                    Log.e("delete","does not exist");
                }
            }
        }).start();

    }
    private void removeBookContents(final String bookname_remove){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File delefile = new File(getExternalFilesDir(null)+"/ZsearchRes/BookContents/"+bookname_remove+".txt");
                if(delefile.exists() && delefile.isFile()) {
                    if(delefile.delete()){
                        Log.e("delete","success");
                    }else{
                        Log.e("delete","fail");
                    }
                }else {
                    Log.e("delete","does not exist");
                }
            }
        }).start();

    }

    public Bitmap getImage(String BookName) {
        Bitmap bitmap_AddShadow=null;
        File picDir=new File(getExternalFilesDir(null)+"/ZsearchRes/BookCovers/" + BookName + ".png");
        FileInputStream fis=null;
        try{
            fis=new FileInputStream(picDir);
            Bitmap bitmap= BitmapFactory.decodeStream(fis);
            Bitmap bitmap_adjustSize=getNewBitmap(bitmap,300,400);
            bitmap_AddShadow=drawImageDropShadow(bitmap_adjustSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return getNewBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.no_book_cover),300,400);
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

    public Bitmap getNewBitmap(Bitmap bitmap, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }
    private Bitmap drawImageDropShadow(Bitmap originalBitmap) {

        BlurMaskFilter blurFilter = new BlurMaskFilter(1,
                BlurMaskFilter.Blur.NORMAL);
        Paint shadowPaint = new Paint();
        shadowPaint.setAlpha(50);
        shadowPaint.setColor(Color.parseColor("#FF0000"));
        shadowPaint.setMaskFilter(blurFilter);

        int[] offsetXY = new int[2];
        Bitmap shadowBitmap = originalBitmap
                .extractAlpha(shadowPaint, offsetXY);

        Bitmap shadowImage32 = shadowBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(shadowImage32);
        c.drawBitmap(originalBitmap, offsetXY[0], offsetXY[1], null);

        return shadowImage32;
    }

    private static String[] getCurrentChap(int current_chap, ArrayList<String> chapName, ArrayList<String> chapLink) {
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
        final View view = LayoutInflater.from(this).inflate(R.layout.wait_dialog2, null);
        wait_dialog = new Dialog(this, R.style.WaitDialog2);
        wait_dialog.setContentView(view);
        wait_dialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams lp = wait_dialog.getWindow().getAttributes();
        lp.width = 400;
        lp.height = 420;
        lp.alpha = 0.6f;
        wait_dialog.getWindow().setAttributes(lp);
    }

    public static class CatalogReloadHandler extends Handler {
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
                catalog.completeCatalog(activity.getCurrent_book().getBookLink(),activity.getCurrent_book().getTag_in_TAG());
                IOtxt.WriteCatalog(activity.getExternalFilesDir(null),activity.getCurrent_book().getBookName(),catalog);
                activity.setCatalog(catalog);
                activity.startReadPage(activity.getContent(),activity.getCurrent_book(),catalog.getTitle(),catalog.getLink());
            }
            assert activity != null;
            activity.wait_dialog.dismiss();
        }
    }
}
