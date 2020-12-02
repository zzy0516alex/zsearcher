package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.helloworld.Utils.StatusBarUtil;
import com.example.helloworld.myObjects.BookList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //在此处添加变量
    private Button NovelSearch;
    private Button MyBookShelf;
    private Button BtSearch;
    OnClick onclick=new OnClick();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //在下面定义自己的变量
        NovelSearch =findViewById(R.id.BookSearch);
        MyBookShelf=findViewById(R.id.MyBookShelf);
        BtSearch=findViewById(R.id.BTlink);
        MyBookShelf.setOnClickListener(onclick);
        NovelSearch.setOnClickListener(onclick);
        BtSearch.setOnClickListener(onclick);
        File Folder =new File(getExternalFilesDir(null),"ZsearchRes");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this,false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //true=黑色字体  false=白色
        StatusBarUtil.setStatusBarDarkTheme(this, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String encode="";
                String base="https://www.sidamingzhu.org";
                try {
                    encode=URLEncoder.encode("三国","utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Connection connect = Jsoup.connect("https://www.sidamingzhu.org/search?searchkey="+encode);
                connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
                // 带参数结束
                try {
                    Document document = connect.get();
                    Elements book_list = document.select("div.content_list");
                    Elements book_list_used=book_list.select("h3");
                    List<String>book_name=book_list_used.eachText();
                    List<String>book_link=new ArrayList<>();
                    for (Element li : book_list_used) {
                        book_link.add(base+li.select("a").attr("href"));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent =null;
            switch (v.getId()){
                case R.id.MyBookShelf: {
                    intent=new Intent(MainActivity.this,BookShelfActivity.class);
                    break;
                }
                case R.id.BookSearch:{
                    intent=new Intent(MainActivity.this, NovelActivity.class);
                    break;
                }
                case R.id.BTlink:{
                    intent=new Intent(MainActivity.this,BtSearchActivity.class);
                    break;
                }
                default:{}

            }
            startActivity(intent);
        }
    }


}
