package com.example.helloworld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NovelShowAcitivity extends AppCompatActivity {
    public static Activity Novalshow;
    private WebView webView;
    private SeekBar seekBar;
    private Button past;
    private Button next;
    private Button catalog;
    private ImageButton AddBook;
    private SharedPreferences myInfo;
    private int myProgress;
    WebSettings webSettings;
    String url;
    int firstChap;
    int lastChap;
    String nextUrl;
    String pastUrl;
    String catalogUrl;
    String currentTitle;
    String BookName;
    List<String>ChapList;
    List<String>ChapLinkList;
    private boolean istouch=false;
    private static boolean isFloatButtonShow=true;
    private static boolean isInShelf=false;
    private static int book_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_show_acitivity);
        Novalshow=this;
        final Bundle bundle=this.getIntent().getExtras();
        url=bundle.getString("url");
        firstChap=bundle.getInt("firstChap");
        lastChap=bundle.getInt("lastChap");
        webView=findViewById(R.id.novelpage);
        seekBar=findViewById(R.id.zoom);
        past=findViewById(R.id.past);
        next=findViewById(R.id.next);
        catalog=findViewById(R.id.catalog);
        AddBook=findViewById(R.id.AddBook);
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        //debug
        // myInfo.edit().clear().apply();
        myProgress=myInfo.getInt("Progress",10);
        //
        if(getCurrentChap(url)==firstChap){
            past.setEnabled(false);
        }
        if(getCurrentChap(url)==lastChap){
            next.setEnabled(false);
        }
        if(!isFloatButtonShow){
            AddBook.setVisibility(View.INVISIBLE);
            AddBook.setEnabled(false);
        }
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
                restartActivity(NovelShowAcitivity.this,pastUrl,firstChap,lastChap);
                if(isInShelf){ ;
                    SharedPreferences.Editor editor=myInfo.edit();
                    editor.putString("BookUrl"+book_id,pastUrl);
                    editor.apply();
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartActivity(NovelShowAcitivity.this,nextUrl,firstChap,lastChap);
                if(isInShelf){
                    SharedPreferences.Editor editor=myInfo.edit();
                    editor.putString("BookUrl"+book_id,nextUrl);
                    editor.apply();
                }
            }
        });
        catalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCatalog();
                Intent intent=new Intent(NovelShowAcitivity.this,CatalogActivity.class);
                Bundle bundle1=new Bundle();
                bundle.putString("url",catalogUrl);
                bundle.putInt("firstChap",firstChap);
                bundle.putInt("lastChap",lastChap);
                bundle.putString("currentTitle",currentTitle);
                bundle.putStringArrayList("ChapList", (ArrayList<String>) ChapList);
                bundle.putStringArrayList("ChapLinkList", (ArrayList<String>) ChapLinkList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient());
        webSettings=webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(myProgress+150);
        PastAndNext();
        if(isFloatButtonShow)is_in_shelf();
    }


    private void is_in_shelf() {
        int num = myInfo.getInt("bookNum", 0);
        if (num != 0) {
            for (int i = 1; i <= num; i++) {
                if (myInfo.getString("BookName" + i, "").equals(BookName)) {
                    setFloatButtonShow(false);
                    isInShelf = true;
                    book_id = i;
                    AddBook.setEnabled(false);
                    AddBook.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }
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
                        int bookNum;
                        bookNum=myInfo.getInt("bookNum",0);
                        SharedPreferences.Editor editor=myInfo.edit();
                        editor.putString("BookUrl"+(bookNum+1),url);
                        editor.putString("BookName"+(bookNum+1),BookName);
                        editor.putInt("FirstChap"+(bookNum+1),firstChap);
                        editor.putInt("LastChap"+(bookNum+1),lastChap);
                        editor.putInt("bookNum",bookNum+1);
                        editor.apply();
                        setBook_id(bookNum+1);
                        AddBookThread thread=new AddBookThread(BookName,getExternalFilesDir(null));
                        thread.start();

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
        CatalogThread Cthread=new CatalogThread(catalogUrl);
        Cthread.start();
        if (Cthread.getChapList()!=null) ChapList= (List<String>) Cthread.getChapList();
        if(Cthread.getChapLinkList()!=null)ChapLinkList= (List<String>) Cthread.getChapLinkList();
    }

    private void PastAndNext() {
        BottomThread thread=new BottomThread(url);
        thread.start();
        if (thread.getNextURL()!=null) nextUrl= (String) thread.getNextURL();
        if(thread.getPastURL()!=null) pastUrl= (String) thread.getPastURL();
        if(thread.getCatalog()!=null) catalogUrl= (String) thread.getCatalog();
        if(thread.getCTitle()!=null) currentTitle= (String) thread.getCTitle();
        if(thread.getBookName()!=null) BookName= (String) thread.getBookName();
    }

    public static void restartActivity(Activity act,String newUrl,int firstChap,int lastChap){

        Intent intent=new Intent();
        intent.setClass(act, act.getClass());
        Bundle bundle=new Bundle();
        bundle.putString("url",newUrl);
        bundle.putInt("firstChap",firstChap);
        bundle.putInt("lastChap",lastChap);
        intent.putExtras(bundle);
        if(isInShelf){
            setIsInShelf(true);
            setBook_id(book_id);
        }
        act.startActivity(intent);
        act.finish();

    }
    public int getCurrentChap(String url){
        String tail=url.split("\\/")[4];
        String target=tail.split("\\.")[0];
        return Integer.parseInt(target);
    }
}
