package com.Z.NovelReader.Fragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Adapters.NovelVerticalViewAdapter;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelViewTheme;
import com.Z.NovelReader.views.Dialog.ViewerSettingDialog;
import com.Z.NovelReader.Global.RecyclerViewItemTouchListener;
import com.Z.NovelReader.R;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class NovelViewVerticalFragment extends NovelViewBasicFragment /*implements OnSettingChangeListener*/ {

    //views
    private RecyclerView contentView;//文本内容
    private RelativeLayout rl_background;//阅读背景
    private NovelVerticalViewAdapter adapter;
    private View view;
    //basic params
    private int myTextSize=20;//字体大小
    private float myLineSpace=1.0f;//行间距倍率
    private boolean is_scroll_stop=true;//界面停止滑动
    private int current_offset;//在垂直阅读情况下，当前章节的偏移量
    private int viewHeight;//当前章节的整体高度
    private boolean offset_located =false;//是否已经定位到当前进度
    private boolean need_save=false;//是否需要保存当前章节，若未曾滑动过界面则不保存
    //private DNMod currentMod;
    //external datasource
    private SharedPreferences myInfo;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_vertical_novelview,container,false);
        contentView =view.findViewById(R.id.Novel_view);
        rl_background = view.findViewById(R.id.rl_background_v);

        //初始化书本参数
        initNovelInfo();
        //初始化数据库
        initDBUpdater();

        //初始化偏好文件
        myInfo=getActivity().getSharedPreferences("UserInfo",MODE_PRIVATE);
        myTextSize=myInfo.getInt("myTextSize",20);
        myLineSpace = myInfo.getFloat("myLineSpace",1.0f);

        //初始化handler
        initHandlers();

        //初始章节预下载
        initCache();

        //初始化viewer
        adapter=new NovelVerticalViewAdapter(getActivity(), getChapCache());
        adapter.setTextSize(myTextSize);
        adapter.setLine_space(myLineSpace);

        contentView.setAdapter(adapter);
        contentView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        //重新载入章节
        adapter.setViewCallbackListener(new NovelVerticalViewAdapter.ViewCallbackListener() {
            @Override
            public void onRefreshClick(NovelChap chap) {
                SkipChapDownloader(chap.getCurrentChap());
            }

            @Override
            public void onViewInitReady(View item_view) {
                if (offset_located)return;
                viewHeight = item_view.getBottom() - item_view.getTop();
                if (viewHeight>5)current_offset = (-1) * (int)(viewHeight * getCurrent_chap_progress());
                //若为第一章，则直接移动，不必等待下载上一章完成
                if (getChap_catalog_index() == 0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) contentView.getLayoutManager();
                    assert linearLayoutManager != null;
                    linearLayoutManager.scrollToPositionWithOffset(0, current_offset);
                    offset_located = true;
                }
            }
        });

        switch(getInitViewMod()){
            case DAY_MOD:switch_to_day_mod();
                break;
            case NIGHT_MOD:switch_to_night_mod();
                break;
            default:
        }

        contentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE/*滑动停止*/) {
                    is_scroll_stop=true;
                    need_save=true;
                    //限制列表中缓存的章节数量
                    int item_count= Objects.requireNonNull(recyclerView.getLayoutManager()).getItemCount();
                    //屏幕中可见的章节数（正常情况下是1/2，异常章节时可能为3）
                    int visible_chap_num = recyclerView.getChildCount();
                    //Log.d("vertical view","num: "+visible_chap_num);
                    //获取可见的最后一个view
                    View lastChildView;
                    if (visible_chap_num == 2){
                        lastChildView = recyclerView.getChildAt(0);
                    }
                    else {
                        lastChildView = recyclerView.getChildAt(visible_chap_num - 1);
                    }
                    current_offset = lastChildView.getTop();
                    int bottom = lastChildView.getBottom();
                    //Log.d("vertical view","top: "+current_offset);
                    //Log.d("vertical view","bottom: "+bottom);
                    //获取当前view的高度
                    int view_height = lastChildView.getBottom() - lastChildView.getTop();
                    //Log.d("vertical view","height: "+view_height);
                    viewHeight = view_height;
                    //得到当前章节的阅读进度
                    setCurrent_chap_progress(Math.abs((double)current_offset/(double) view_height));
                    Log.d("vertical view","progress: "+getCurrent_chap_progress());

                    //获取可见的最后一个view的位置
                    int lastChildViewPosition = recyclerView.getChildAdapterPosition(lastChildView);
                    if(getChapCacheIndex() != lastChildViewPosition){
                        updateReadingChap(lastChildViewPosition);
                        Log.d("novel view","章节可见："+visible_chap_num);
                    }
                    if (isAuto_download()) {
                        if ((getChapCacheIndex()+1-visible_chap_num) == 0) {
                            int top= Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(0)).getTop();
                            int Y=recyclerView.getScrollY();
                            if(top==Y){
                                setChapCacheIndex(0);
                                Log.d("novel view","reachTop");
                                LastChapDownloader(getChapCache().get(0));
                            }
                        }else if (getChapCacheIndex() == getChapCache().size()-1){
                            NextChapDownloader(getCurrentChap());
                        }
                    }

                    //防止溢出
                    if (item_count>50){
                        int new_item_top=recyclerView.getLayoutManager().getChildAt(recyclerView.getChildCount()-1).getTop();
                        if (new_item_top < 0){
                            Log.d("outflow", "cache is too big, refresh cache"+new_item_top);
                            offset_located =true;
                            setCurrent_chap_progress(new_item_top);
                            refreshChapCache(getCurrentChap());
                            adapter.updateChapList(getChapCache());
                        }
                    }
                }
                else {
                    is_scroll_stop=false;
                    //开始滑动，则关闭toolbar
                    getReadingListener().toolbarControl(true,-1,-1);
                }

            }
        });

        //屏幕点击识别
        GestureDetector.OnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                getReadingListener().toolbarControl(!is_scroll_stop,e.getX(),e.getY());
                return false;
            }
        };
        contentView.addOnItemTouchListener(new RecyclerViewItemTouchListener(
                new GestureDetectorCompat(contentView.getContext(),gl)));

        return view;
    }

    private void switch_to_day_mod() {
        adapter.setDNMod(DNMod.DAY_MOD);
        rl_background.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.comfortGreen));
    }

    private void switch_to_night_mod() {
        adapter.setDNMod(DNMod.NIGHT_MOD);
        rl_background.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.night_background));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (need_save)SaveCurrentLine();
        if (ViewerSettingDialog.brightnessObserver!=null)ViewerSettingDialog.brightnessObserver.unregister();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (need_save)SaveCurrentLine();
        saveRecoverFile();
    }

    @Override
    public void onDNModChange(DNMod mod) {
        switch(mod){
            case DAY_MOD:switch_to_day_mod();
                break;
            case NIGHT_MOD:switch_to_night_mod();
                break;
            default:
        }
    }

    @Override
    public void onViewThemeChange(NovelViewTheme theme, int change_type) {
        switch(change_type){
            case NovelViewTheme.THEME_TYPE_TEXT_SIZE:
                adapter.setTextSize(theme.getTextSize());
                break;
            case NovelViewTheme.THEME_TYPE_LINE_SPACE_MULTI:
                adapter.setLine_space(theme.getLineSpaceMulti());
                break;
            default:
        }
    }

    @Override
    public void downloadReady(int download_type, boolean isError) {
        if (download_type == DOWNLOAD_TYPE_LAST_CHAP){
            int m_offset;
            if (!offset_located)
            {   //若还未定位到阅读位置，则移动到改位置
                m_offset = current_offset;
                offset_located =true;
            }else{
                //若已经定位，则仅做微小移动示意章节以准备完毕，防止重复触发章节缓存
                m_offset = (int) (DEFAULT_CHAP_PROGRESS * viewHeight);
            }
            LinearLayoutManager linearLayoutManager= (LinearLayoutManager) contentView.getLayoutManager();
            assert linearLayoutManager != null;
            linearLayoutManager.scrollToPositionWithOffset(getChapCacheIndex(),m_offset);
        }
        adapter.updateChapList(getChapCache());
    }

    @Override
    public void cacheInitReady() {

    }

    @Override
    public void afterCacheCut() {
        adapter.updateChapList(getChapCache());
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) contentView.getLayoutManager();
        assert linearLayoutManager != null;
        linearLayoutManager.scrollToPositionWithOffset(getChapCacheIndex(), current_offset);
    }
}
