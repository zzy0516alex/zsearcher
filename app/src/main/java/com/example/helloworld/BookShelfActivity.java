package com.example.helloworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.helloworld.Adapters.BookshelfAdapter;
import com.example.helloworld.NovelRoom.NovelDBTools;
import com.example.helloworld.NovelRoom.Novels;
import com.example.helloworld.Threads.CatalogThread;
import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelChap;

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
import java.util.concurrent.TimeUnit;

public class BookShelfActivity extends AppCompatActivity {
    List<Bitmap> BookCover;
    List<String> BookName;
    List<Novels> AllNovelList;
    NovelCatalog Catalog;
    ArrayList<String>ChapName;
    ArrayList<String>ChapLink;
    private SharedPreferences myInfo;
    private NovelDBTools novelDBTools;
    private BookshelfAdapter adapter;
    private GridView bookShelf;
    private Button delete;
    private RelativeLayout update;
    private Dialog wait_dialog;
    Context context;
    Activity activity;
    private CatalogUpdaterHandler updaterHandler;
    private CatalogReloadHandler reloadHandler;
    private boolean is_item_chosen=false;
    private int item_chosen=-1;
    private Novels current_book;
    String content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);
        File Folder =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookCovers");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        //init basic params
        BookCover=new ArrayList<>();
        BookName=new ArrayList<>();
        context=this;
        activity=this;
        updaterHandler =new CatalogUpdaterHandler(this);
        reloadHandler=new CatalogReloadHandler(this);
        content="";
        //init views
        bookShelf=findViewById(R.id.BookShelf);
        delete=findViewById(R.id.delete);
        delete.setVisibility(View.INVISIBLE);
        update=(RelativeLayout) findViewById(R.id.wait_update);
        update.setVisibility(View.GONE);
        initWaitView();
        //init preference
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        final int num = myInfo.getInt("bookNum",0);
        final String update_time = myInfo.getString("UpdateTime","");
        //get current time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date currentDate = new Date(System.currentTimeMillis());
        final String current_time = formatter.format(currentDate);
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
                updaterHandler.setCounter(novels.size());
                for (Novels novel : novels) {
                    BookName.add(novel.getBookName());
                    BookCover.add(getImage(novel.getBookName()));
                    if (!update_time.equals(current_time)){
                        update.setVisibility(View.VISIBLE);
                        update_catalog(novel);
                    }
                }
                SharedPreferences.Editor editor2=myInfo.edit();
                editor2.putString("UpdateTime",current_time).apply();
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
                            CatalogThread Cthread=new CatalogThread(current_book.getBookLink(),current_book.getTag_in_TAG());
                            Cthread.setmHandler(reloadHandler);
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
        CatalogThread catalogThread=new CatalogThread(novel.getBookLink(), novel.getTag_in_TAG());
        catalogThread.setIf_output(true,novel.getBookName(),getExternalFilesDir(null));
        catalogThread.need_update(true,context, updaterHandler);
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

    public static class CatalogUpdaterHandler extends Handler {
        private final WeakReference<BookShelfActivity> mActivity;
        private int counter;

        public CatalogUpdaterHandler(BookShelfActivity activity) {
            mActivity = new WeakReference<BookShelfActivity>(activity);
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public void handleMessage(Message msg) {
            BookShelfActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case CatalogThread.CATALOG_UPDATED:
                        counter--;
                        break;
                    case CatalogThread.CATALOG_UPDATE_FAILED: {
                        activity.update.setVisibility(View.GONE);
                        Toast.makeText(activity, "章节同步出错", Toast.LENGTH_SHORT).show();
                    }
                        break;
                    default:
                }
                if (counter==0){
                    activity.update.setVisibility(View.GONE);
                    Toast.makeText(activity, "章节同步完成", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public static class CatalogReloadHandler extends Handler {
        private final WeakReference<BookShelfActivity> mActivity;
        private int counter;

        public CatalogReloadHandler(BookShelfActivity activity) {
            mActivity = new WeakReference<BookShelfActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BookShelfActivity activity = mActivity.get();
            if (msg.what==CatalogThread.CATALOG_GAIN_FAILED)
                Toast.makeText(activity, "无网络", Toast.LENGTH_SHORT).show();
            if (activity != null && msg.obj!=null) {
                NovelCatalog catalog= (NovelCatalog) msg.obj;
                catalog.completeCatalog(activity.getCurrent_book().getBookLink(),activity.getCurrent_book().getTag_in_TAG());
                IOtxt.WriteCatalog(activity.getExternalFilesDir(null),activity.getCurrent_book().getBookName(),catalog);
                activity.setCatalog(catalog);
                activity.startReadPage(activity.getContent(),activity.getCurrent_book(),catalog.getTitle(),catalog.getLink());
            }
            activity.wait_dialog.dismiss();
        }
    }
}
