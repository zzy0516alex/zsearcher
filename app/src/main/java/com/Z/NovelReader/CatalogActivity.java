package com.Z.NovelReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.Z.NovelReader.Adapters.BooklistAdapter;
import com.Z.NovelReader.Threads.NovelSearchThread;

import java.util.List;


public class CatalogActivity extends AppCompatActivity {

    private ListView chapList;

    private String url;
    private List<String> ChapList;
    private List<String>ChapLinkList;
    private String currentTitle;
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
        toolbar.setNavigationIcon(R.mipmap.backarrow);       //加载图标
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity.this.setResult(0);
                CatalogActivity.this.finish();
            }
        });

        //获取数据
        final Bundle bundle=this.getIntent().getExtras();
        assert bundle != null;
        url=bundle.getString("url");
        ChapList=bundle.getStringArrayList("ChapList");
        ChapLinkList=bundle.getStringArrayList("ChapLinkList");
        currentTitle=bundle.getString("currentTitle");

        //加载目录
        BooklistAdapter chapListAdapter=new BooklistAdapter(ChapList,context,true,currentTitle);
        chapList.setAdapter(chapListAdapter);

        //控制列表滚动到当前章节附近
        for(int i=0;i<ChapList.size();i++){
            if(currentTitle.equals(ChapList.get(i))){
                locate=i;
                break;
            }
        }
        if(locate>7)chapList.setSelection(locate-6);
        else chapList.setSelection(locate);

        //目录点击
        chapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String current_chapLink=ChapLinkList.get(position);
                Intent intent=new Intent();
                Bundle bundle_back=new Bundle();
                bundle_back.putString("url",current_chapLink);
                intent.putExtras(bundle_back);
                CatalogActivity.this.setResult(1,intent);
                CatalogActivity.this.finish();
            }
        });
    }
}
