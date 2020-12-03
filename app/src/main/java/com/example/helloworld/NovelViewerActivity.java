package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.helloworld.Fragments.NovelViewFragment;
import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.Utils.StatusBarUtil;
import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelChap;

import java.util.ArrayList;
import java.util.HashMap;

public class NovelViewerActivity extends AppCompatActivity {

    private static NovelChap current_chap;
    private NovelCatalog catalog;
    private ArrayList<String>ChapName;
    private ArrayList<String>ChapLink;

    public static void setCurrent_chap(NovelChap chap) {
        NovelViewerActivity.current_chap = chap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_viewer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ArrayList<NovelChap> novelChap =new ArrayList<>();
        novelChap.add(current_chap);

        NovelViewFragment fragment=new NovelViewFragment();

        catalog= IOtxt.read_catalog(current_chap.getBookName(),getExternalFilesDir(null));
        ChapName=catalog.getTitle();
        ChapLink=catalog.getLink();

        Intent intent=getIntent();
        fragment.setOffset(intent.getIntExtra("offset",3));
        fragment.setBookLink(intent.getStringExtra("BookLink"));

        fragment.setChapList(novelChap);
        fragment.setBookID(current_chap.getBookID());
        fragment.setBookName(current_chap.getBookName());
        fragment.setBookTag(current_chap.getTag());
        //fragment.setChapName(ChapName);
        //fragment.setChapLink(ChapLink);
        fragment.setCatalog(catalog);
        fragment.setDir(getExternalFilesDir(null));
        getSupportFragmentManager().beginTransaction().add(R.id.novel_view_container,fragment).commitAllowingStateLoss();
    }
}
