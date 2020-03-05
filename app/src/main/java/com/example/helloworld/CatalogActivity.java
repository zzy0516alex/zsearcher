package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


public class CatalogActivity extends AppCompatActivity {

    private ListView chapList;

    private String url;
    private List<String> ChapList;
    private List<String>ChapLinkList;
    private ArrayList<String>Chaps;
    private int firstChap;
    private int lastChap;
    private String currentTitle;
    private int locate;
    Context context;
    private SharedPreferences myInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        context=this;
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        chapList=findViewById(R.id.ChapList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayShowTitleEnabled(false);//隐藏标题
        toolbar.setNavigationIcon(R.drawable.backarrow);       //加载图标
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //
        final Bundle bundle=this.getIntent().getExtras();
        url=bundle.getString("url");
        ChapList=bundle.getStringArrayList("ChapList");
        ChapLinkList=bundle.getStringArrayList("ChapLinkList");
        firstChap=bundle.getInt("firstChap");
        lastChap=bundle.getInt("lastChap");
        currentTitle=bundle.getString("currentTitle");
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        BooklistAdapter chapListAdapter=new BooklistAdapter(ChapList,context,true,currentTitle);
        chapList.setAdapter(chapListAdapter);
        for(int i=0;i<=ChapList.size();i++){
            if(currentTitle.equals(ChapList.get(i))){
                locate=i;
                break;
            }
        }
        if(locate>7)chapList.setSelection(locate-6);
        else chapList.setSelection(locate);
        chapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NovelShowAcitivity.Novalshow.finish();
                String newUrl=url+ChapLinkList.get(position);
                Intent intent=new Intent(CatalogActivity.this,NovelShowAcitivity.class);
                Bundle bundle1=new Bundle();
                bundle1.putString("url",newUrl);
                bundle1.putInt("firstChap",firstChap);
                bundle1.putInt("lastChap",lastChap);
                intent.putExtras(bundle1);
                if(NovelShowAcitivity.getIsInShelf()){
                    int book_id=NovelShowAcitivity.getBook_id();
                    SharedPreferences.Editor editor=myInfo.edit();
                    editor.putString("BookUrl"+book_id,newUrl);
                    editor.apply();
                    NovelShowAcitivity.setIsInShelf(true);
                    NovelShowAcitivity.setBook_id(book_id);
                }
                startActivity(intent);
                CatalogActivity.this.finish();
            }
        });
    }
}
