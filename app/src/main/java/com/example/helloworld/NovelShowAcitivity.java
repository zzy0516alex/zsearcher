package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.helloworld.NovelRoom.NovelDBTools;
import com.example.helloworld.NovelRoom.Novels;
import com.example.helloworld.Threads.GetCoverThread;
import com.example.helloworld.Threads.CatalogThread;
import com.example.helloworld.Threads.ContentTextThread;
import com.example.helloworld.Threads.NovelThread;
import com.example.helloworld.myObjects.NovelCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NovelShowAcitivity extends AppCompatActivity {
    public static Activity Novalshow;
    private WebView webView;
    private SeekBar seekBar;
    private Button past;
    private Button next;
    private Button catalog;
    private ImageButton web_refresh;
    private ImageButton AddBook;
    private SharedPreferences myInfo;
    private NovelDBTools novelDBTools;
    //NovelDao novelDao;
    private int myProgress;
    WebSettings webSettings;
    private Handler mHandler;
    String currentURL;
    String BaseURL;
    int ttlChap;
    int currentChap=0;
    String nextUrl;
    String pastUrl;
    String catalogUrl;
    String currentTitle;
    String BookName;
    List<String>ChapList;
    List<String>ChapLinkList;
    List<Novels> AllNovels;
    NovelThread.TAG tag;
    private boolean istouch=false;
    private static boolean first_load=true;
    private static boolean isFloatButtonShow=true;
    private static boolean isInShelf=false;
    private static int book_id;
    private Context context;
    @SuppressLint({"HandlerLeak", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_show_acitivity);
        Novalshow=this;
        //get bundle
        final Bundle bundle=this.getIntent().getExtras();
        assert bundle != null;
        currentURL =bundle.getString("url");
        currentTitle=bundle.getString("currentTitle");
        catalogUrl=bundle.getString("CatalogUrl");
        BookName=bundle.getString("BookName");
        tag= (NovelThread.TAG) bundle.getSerializable("tag");

        BaseURL=getBaseUrl(currentURL,tag);
        context=this;

        //get view
        webView=findViewById(R.id.novelpage);
        seekBar=findViewById(R.id.zoom);
        past=findViewById(R.id.past);
        next=findViewById(R.id.next);
        catalog=findViewById(R.id.catalog);
        web_refresh=findViewById(R.id.web_refresh);
        AddBook=findViewById(R.id.AddBook);

        //get preferences
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);

        myProgress=myInfo.getInt("Progress",10);

        //get database
        novelDBTools= ViewModelProviders.of(this).get(NovelDBTools.class);
        novelDBTools.getAllNovelsLD().observe(this, new Observer<List<Novels>>() {
            @Override
            public void onChanged(List<Novels> novels) {
                AllNovels=novels;
                SharedPreferences.Editor editor=myInfo.edit();
                editor.putInt("bookNum",novels.size()).apply();
                is_in_shelf();
            }
        });
//        NovelDataBase dataBase=NovelDataBase.getDataBase(this);
//        novelDao=dataBase.getNovelDao();

        //debug
        //novelDBTools.deleteAll();

        //init handler
        mHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                NovelCatalog result_back= (NovelCatalog) msg.obj;
                ChapList=result_back.getTitle();
                ChapLinkList=result_back.getLink();
                ttlChap=ChapList.size();
                PastAndNext();
                catalog.setEnabled(true);
                if (currentChap!=ChapList.size()-1)next.setEnabled(true);
                if (currentChap!=0)past.setEnabled(true);
                webView.reload();
            }
        };

        //initiate button
        //ChangeButtonCondition();
        next.setEnabled(false);
        past.setEnabled(false);

        if(!isFloatButtonShow){
            AddBook.setVisibility(View.INVISIBLE);
            AddBook.setEnabled(false);
        }
        catalog.setEnabled(false);
        next.setEnabled(false);
        //
        seekBar.setMax(150);
        seekBar.setProgress(myProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                webSettings.setTextZoom(progress+150);
                myProgress=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor=myInfo.edit();
                editor.putInt("Progress",myProgress);
                editor.apply();
            }
        });
        past.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(pastUrl);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(nextUrl);
            }
        });
        catalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getCatalog();
                Intent intent=new Intent(NovelShowAcitivity.this,CatalogActivity.class);
                Bundle bundle_catalog=new Bundle();
                bundle_catalog.putString("url",catalogUrl);
                bundle_catalog.putString("currentTitle",currentTitle);
                bundle_catalog.putStringArrayList("ChapList", (ArrayList<String>) ChapList);
                bundle_catalog.putStringArrayList("ChapLinkList", (ArrayList<String>) ChapLinkList);
                bundle_catalog.putSerializable("tag",tag);
                intent.putExtras(bundle_catalog);
                startActivityForResult(intent,1);
            }
        });

        //设置浮标
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels - 50;
        AddBook.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int ea = event.getAction();
                Log.i("TAG", "Touch:" + ea);
                switch (ea) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标   
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        istouch=true;
//                  MotionEvent cancelEvent = MotionEvent.obtain(event);
//                  cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex()<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
//                  onTouchEvent(cancelEvent);
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        int l = v.getLeft() + dx;
                        int b = v.getBottom() + dy;
                        int r = v.getRight() + dx;
                        int t = v.getTop() + dy;
                        // 下面判断移动是否超出屏幕   
                        if (l < 0) {
                            l = 0;
                            r = l + v.getWidth();
                        }
                        if (t < 0) {
                            t = 0;
                            b = t + v.getHeight();
                        }
                        if (r > screenWidth) {
                            r = screenWidth;
                            l = r - v.getWidth();
                        }
                        if (b > screenHeight) {
                            b = screenHeight;
                            t = b - v.getHeight();
                        }
                        v.layout(l, t, r, b);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        v.postInvalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }

        });
        AddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBook.setImageResource(R.mipmap.shelfclick);
                if(!istouch){
                    IfAddToShelf();
                }
                if(istouch){
                    istouch=false;
                    AddBook.setImageResource(R.mipmap.addtoshelf);
                }
            }
        });

        //
        webView.loadUrl(currentURL);
        webSettings=webView.getSettings();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if(first_load && !isInShelf) {
                    Toast.makeText(NovelShowAcitivity.this, "当前为预览模式，加入书架后可优化阅读", Toast.LENGTH_SHORT).show();
                    first_load=false;
                }
                super.onPageFinished(view, url);

                //阅读新章节时：
                if (!url.equals(currentURL)){
                    currentURL=url;
                    ChangeCurrentCondition();
                    PastAndNext();
                    if (isInShelf)update_bookshelfINFO();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(myProgress+150);

        //获取目录
        getCatalog();

        //刷新界面
        web_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(Novalshow, R.anim.rotate_limittime);
                web_refresh.startAnimation(animation);
                webView.reload();
            }
        });
        //debug ContentTextThread t=new ContentTextThread(url,BookName,getExternalFilesDir(null));
        //t.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==1){
            String newURL="";
            assert data != null;
            Bundle bundle=data.getExtras();
            assert bundle != null;
            newURL=bundle.getString("url");
            webView.loadUrl(newURL);
        }
    }

    private void ChangeCurrentCondition() {
        next.setEnabled(false);
        past.setEnabled(false);
        currentChap=ChapLinkList.indexOf(currentURL.replace(BaseURL,""));
        currentTitle=ChapList.get(currentChap);
        if (currentChap!=ChapLinkList.size()-1)next.setEnabled(true);
        if (currentChap!=0)past.setEnabled(true);
    }

    private void is_in_shelf(){
        if(AllNovels.size()!=0) {
            for (Novels novel : AllNovels) {
                if (novel.getBookName().equals(BookName)) {
                    if(first_load) {
                        Toast.makeText(Novalshow, "该书已在书架中，建议转至书架阅读", Toast.LENGTH_SHORT).show();
                        first_load=false;
                    }
                    setFloatButtonShow(false);
                    isInShelf = true;
                    book_id = novel.getId();
                    AddBook.setEnabled(false);
                    AddBook.setVisibility(View.INVISIBLE);
                    break;
                } else isInShelf = false;
            }
        }else {
            isInShelf=false;
        }
    }

    public static void setFirst_load(boolean isFirst) {
        first_load = isFirst;
    }

    public static void setFloatButtonShow(boolean floatButtonShow) {
        isFloatButtonShow = floatButtonShow;
    }

    public static void setIsInShelf(boolean isInShelf) {
        NovelShowAcitivity.isInShelf = isInShelf;
    }

    public static void setBook_id(int book_id) {
        NovelShowAcitivity.book_id = book_id;
    }

    public static boolean getIsInShelf(){
        return isInShelf;
    }
    public static int getBook_id() {
        return book_id;
    }

    public String getBookName() {
        return BookName;
    }

    private void IfAddToShelf() {
        AlertDialog.Builder builder=new AlertDialog.Builder(Novalshow);
        builder.setTitle("加入书架")
                .setMessage("是否将本书加入书架")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(NovelShowAcitivity.this, "已放入书架", Toast.LENGTH_SHORT).show();
                        AddBook.setEnabled(false);
                        AddBook.setVisibility(View.INVISIBLE);
                        setFloatButtonShow(false);
                        //TODO: add to shelf
                        isInShelf=true;
                        Novels novel=new Novels(BookName,ttlChap,currentChap,catalogUrl);
                        novel.setTag_inTAG(tag);
                        novelDBTools.insertNovels(novel);
                        GetCoverThread thread_cover=new GetCoverThread(BookName,getExternalFilesDir(null));
                        thread_cover.start();
                        ContentTextThread thread_text=new ContentTextThread(getCurrentURL(),BookName,getExternalFilesDir(null));
                        thread_text.setTag(tag);
                        thread_text.setContext(context);
                        thread_text.start();
                        CatalogThread thread_catalog=new CatalogThread(catalogUrl,tag);
                        thread_catalog.setIf_output(true,BookName,getExternalFilesDir(null));
                        thread_catalog.start();

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddBook.setImageResource(R.mipmap.addtoshelf);
                    }
                }).setCancelable(false).show();
    }


    private void getCatalog() {
        CatalogThread Cthread=new CatalogThread(catalogUrl,tag);
        Cthread.setmHandler(mHandler);
        Cthread.start();
    }

    private void PastAndNext() {
        if (currentChap!=ChapLinkList.size()-1)nextUrl=BaseURL+ChapLinkList.get(currentChap+1);
        if (currentChap!=0)pastUrl=BaseURL+ChapLinkList.get(currentChap-1);
    }

    private void update_bookshelfINFO(){
            Novels novel=new Novels(BookName,ttlChap,currentChap,catalogUrl);
            novel.setId(book_id);
            novelDBTools.updateNovels(novel);
            ContentTextThread t=new ContentTextThread(getCurrentURL(),BookName,getExternalFilesDir(null));
            t.setTag(tag);
            t.start();
    }

    private String getCurrentURL(){
        String result="";
        switch(tag){
            case BiQuGe:
                result=currentURL;
                break;
            case SiDaMingZhu:
                result=context.getString(R.string.book_search_base2)+"/"+currentURL.split("\\/")[3];
                break;
            default:
        }

        return result;
    }
    public String getBaseUrl(String url, NovelThread.TAG tag){
        String base="";
        switch(tag){
            case BiQuGe:{
                String []unit=url.split("\\/");
                base=unit[0]+"//"+unit[2]+"/"+unit[3]+"/";
            }
                break;
            case SiDaMingZhu:{
                String []unit=url.split("\\/");
                base=unit[0]+"//"+unit[2]+"/";
            }
                break;
            default:
        }

        return base;
    }
}
