package com.example.helloworld;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;

import com.example.helloworld.Fragments.ResultFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class BtResultActivity extends AppCompatActivity {
    private static HashMap<String , ArrayList<String>> resultBox1;
    private static HashMap<String , ArrayList<String>> resultBox2;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Fragment []mFragments;
    private final int Fragment_NUM=2;
    private final int Fragment_SOURCE_btdad=0;
    private final int Fragment_SOURCE_animation=1;

    public static void setResultBox1(HashMap<String, ArrayList<String>> resultBox) {
        resultBox1 = resultBox;
    }

    public static void setResultBox2(HashMap<String, ArrayList<String>> resultBox) {
        resultBox2 = resultBox;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_result);
        viewPager=findViewById(R.id.pager);
        tabLayout=findViewById(R.id.tab_layout);

        mFragments=new Fragment[Fragment_NUM];
        //btdad
        ResultFragment btdad=new ResultFragment();
        btdad.setResultBox(resultBox1);
        mFragments[Fragment_SOURCE_btdad]=btdad;
        //animation
        ResultFragment animation=new ResultFragment();
        animation.setResultBox(resultBox2);
        mFragments[Fragment_SOURCE_animation]=animation;

        ViewPagerAdapter adapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#1E90FF"));

    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case Fragment_SOURCE_btdad:
                    return mFragments[Fragment_SOURCE_btdad];
                case Fragment_SOURCE_animation:
                    return mFragments[Fragment_SOURCE_animation];
                default:
                    throw new IllegalArgumentException("Invalid section: " + position);
            }
        }

        @Override
        public int getCount() {
            return Fragment_NUM;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case Fragment_SOURCE_btdad:
                    return "全局源1";
                case Fragment_SOURCE_animation:
                    return "动漫源";
                default:
                    return super.getPageTitle(position);
            }
        }
    }
}
