package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    //在此处添加变量
    private Button GarbageClassificationSearch;
    private Button NovelSearch;
    private Button MyBookShelf;
    private Button BtSearch;
    OnClick onclick=new OnClick();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //在下面定义自己的变量
        GarbageClassificationSearch = findViewById(R.id.btn02);
        NovelSearch =findViewById(R.id.btn03);
        MyBookShelf=findViewById(R.id.MyBookShelf);
        BtSearch=findViewById(R.id.BTlink);
        MyBookShelf.setOnClickListener(onclick);
        GarbageClassificationSearch.setOnClickListener(onclick);
        NovelSearch.setOnClickListener(onclick);
        BtSearch.setOnClickListener(onclick);
        File Folder =new File(getExternalFilesDir(null),"ZsearchRes");
        if(!Folder.exists()){
            Folder.mkdir();
        }
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
                case R.id.btn02: {
                    intent=new Intent(MainActivity.this,JsoupActivity.class);
                    break;
                }
                case R.id.btn03:{
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
