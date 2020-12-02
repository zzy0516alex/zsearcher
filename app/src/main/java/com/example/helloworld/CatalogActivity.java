package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.helloworld.Adapters.BooklistAdapter;
import com.example.helloworld.NovelRoom.NovelDBTools;
import com.example.helloworld.NovelRoom.Novels;
import com.example.helloworld.Threads.ContentTextThread;
import com.example.helloworld.Threads.NovelThread;

import java.util.ArrayList;
import java.util.List;


public class CatalogActivity extends AppCompatActivity {

    private ListView chapList;

    private String url;
    private List<String> ChapList;
    private List<String>ChapLinkList;
    private String currentTitle;
    private NovelThread.TAG tag;
    private int locate;
    Context context;
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
                CatalogActivity.this.setResult(0);
                CatalogActivity.this.finish();
            }
        });
        //
        final Bundle bundle=this.getIntent().getExtras();
        assert bundle != null;
        url=bundle.getString("url");
        ChapList=bundle.getStringArrayList("ChapList");
        ChapLinkList=bundle.getStringArrayList("ChapLinkList");
        currentTitle=bundle.getString("currentTitle");
        tag= (NovelThread.TAG) bundle.getSerializable("tag");
        BooklistAdapter chapListAdapter=new BooklistAdapter(ChapList,context,true,currentTitle);
        chapList.setAdapter(chapListAdapter);
        for(int i=0;i<ChapList.size();i++){
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
                String newUrl="";
                String current_chapLink=ChapLinkList.get(position);
                switch(tag){
                    case BiQuGe:
                        newUrl=url+current_chapLink;
                        break;
                    case SiDaMingZhu:
                        newUrl=context.getString(R.string.book_read_base2)+"/"+current_chapLink;
                        break;
                    default:
                }
                Intent intent=new Intent();
                Bundle bundle_back=new Bundle();
                bundle_back.putString("url",newUrl);
                intent.putExtras(bundle_back);
                CatalogActivity.this.setResult(1,intent);
                CatalogActivity.this.finish();
            }
        });
    }
}
