package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.example.helloworld.Fragments.NovelViewFragment;
import com.example.helloworld.myObjects.NovelChap;

import java.util.ArrayList;

public class NovelViewerActivity extends AppCompatActivity {

    private static NovelChap current_chap;

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
        /*debug
        Novel chap1=new Novel("第一章","正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文");
        Novel chap2=new Novel("第二章","正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文");
        Novel chap3=new Novel("第二章","正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文\n正文正文\n正文\n正文\n正文\n正文\n正文\n正文");
        novel.add(chap1);
        novel.add(chap2);
        novel.add(chap3);
        *
         */

        NovelViewFragment fragment=new NovelViewFragment();
        fragment.setChapList(novelChap);
        getSupportFragmentManager().beginTransaction().add(R.id.novel_view_container,fragment).commitAllowingStateLoss();
    }
}
