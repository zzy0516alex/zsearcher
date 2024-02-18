package com.Z.NovelReader.Fragments;

import static android.content.Context.MODE_PRIVATE;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.Z.NovelReader.Adapters.RecyclerPageAdapter;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelContentPage;
import com.Z.NovelReader.Objects.beans.NovelPageWindow;
import com.Z.NovelReader.Objects.beans.NovelViewTheme;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.views.Dialog.ViewerSettingDialog;

import java.util.ArrayList;
import java.util.Locale;

public class NovelViewHorizontalFragment extends NovelViewBasicFragment {

    private ViewPager2 pageView;
    private RelativeLayout rl_background;
    private Context context;
    private NovelPageWindow currentNovelChap;
    private ArrayList<NovelPageWindow> windowList;//包含所有缓存内章节的分页数据，所有数据均为single_window
    //windowList index 与 chap_cache_index 一致
    private int window_page_index = 0;//当前页在整个页窗口内的索引
    private int chap_page_index = 0;//当前页在当前章节内的索引
    private enum WindowType{left,right,match,both}
    private WindowType windowType;//在currentWindow中，cache相对于currentChap的位置
    private boolean after_update = false;//在更新页面后，不需要重置页面索引
    private boolean init_page = true;//在初始化未完成前，不需要重置页面索引
    private boolean hold_progress = false;//重新分页后不按现有索引，而根据当前阅读进度调整
    private boolean isScrolling = false;//是否正在滑动
    private NovelViewTheme currentTheme;//当前的字体风格
    private RecyclerPageAdapter.PageListener pageListener;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horizontal_novelview,container,false);
        pageView = view.findViewById(R.id.horizontal_novel_view);
        rl_background = view.findViewById(R.id.rl_background_h);
        context = getContext();

        MAX_CHAP_CACHE = 6;
        MIN_CHAP_CACHE = 1;

        initNovelInfo();
        initDBUpdater();
        initHandlers();
        //initCache();

        windowList = new ArrayList<>();
        NovelPageWindow first_window = new NovelPageWindow();
        first_window.init(getCurrentChap());
        windowList.add(first_window);
        currentNovelChap = first_window;

        //初始状态章节未分页，作为一个临时章节先进行加载
        //此时章节的总页数未知，无法跳转到用户之前阅读的页面
        windowType = WindowType.match;
        currentNovelChap.setSingleWindow(true);
        init_page = true;

        //分页后处理
        pageListener = new RecyclerPageAdapter.PageListener() {
            @Override
            public void onPageSplitDone(NovelPageWindow update_chap, ArrayList<NovelContentPage> pages, int chapID) {
                for (NovelPageWindow window : windowList) {
                    if (window.getChapID() == chapID){
                        window.setPages(pages);
                        break;
                    }
                }
                //根据chap_page_index 调整window_page_index
                int total_chap = currentNovelChap.getPageNum();
                int total_window = pageView.getAdapter().getItemCount();

                if (init_page || hold_progress){
                    //在初始页面分页完成后，根据用户的阅读进度，对各索引赋值
                    int page_i = (int) Math.round(getCurrent_chap_progress() * (total_chap-1));
                    if (page_i<0)page_i=0;
                    if (page_i>=total_chap)page_i=total_chap-1;
                    Log.d("init reading",String.format("saved reading progress:%.3f | start from page:%d",getCurrent_chap_progress(),page_i));
                    chap_page_index = page_i;
                    window_page_index = chap_page_index;

                    /**在初始章加载完毕后，再加载其前后章
                     * @see #cacheInitReady() 对当前章处于首页和尾页时作出特殊处理*/
                    if (init_page)initCache();
                }

                switch(windowType){
                    case left:case both:{
                        if (hasLastChap()) {
                            NovelPageWindow left_chap = windowList.get(getChapCacheIndex() - 1);
                            window_page_index = chap_page_index + left_chap.getPageNum();
                        }
                    }
                    break;
                    case right:{
                        window_page_index = chap_page_index;
                    }
                    break;
                    case match:{
                        window_page_index = chap_page_index;
                        //当当前章不是首页或尾页时，无需处理，直接跳转到该页
                        if (((chap_page_index!=0)&&(chap_page_index != (total_chap-1))) && init_page)
                            init_page = false;
                    }
                    break;
                    default:
                }
                updatePageView(update_chap,true);

                Log.d("page split",String.format("chap %d/%d",chap_page_index, total_chap -1));
                Log.d("page split",String.format("window %d/%d",window_page_index, total_window -1));
            }

            @Override
            public void onRefreshClick(int chapID) {
                SkipChapDownloader(chapID);
            }
        };

        //初始化偏好文件
        SharedPreferences myInfo=getActivity().getSharedPreferences("UserInfo",MODE_PRIVATE);
        int initTextSize=myInfo.getInt("myTextSize",20);
        float initLineSpace = myInfo.getFloat("myLineSpace",1.0f);
        currentTheme = new NovelViewTheme();
        currentTheme.setTextSize(initTextSize);
        currentTheme.setLineSpaceMulti(initLineSpace);

        //初始化view
        RecyclerPageAdapter adapter = new RecyclerPageAdapter(context,currentNovelChap);
        adapter.setPageListener(pageListener);
        adapter.setText_size(initTextSize);
        adapter.setLine_space(initLineSpace);
        manageDayNightModeChange(getInitViewMod());
        adapter.setDNMod(getInitViewMod(),false);
        pageView.setAdapter(adapter);
        pageView.setOffscreenPageLimit(2);
        //pageView.setCurrentItem(window_page_index,false);
        pageView.setPageTransformer((page, position) -> {
            if (position <= 0.0f) {
                //被滑动的那页，设置水平位置偏移量为0
                //Log.d("transformPage","pos "+position);
                page.setTranslationX(0.0f);
            } else {
                //未被滑动的页
                //Log.d("transformPage","pos "+position);
                page.setTranslationX((-page.getWidth() * position));
                page.setTranslationZ(-position);
            }
        });

        //处理页面变化
        pageView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (init_page)return;
                if (after_update){
                    after_update = false;
                    return;
                }
                int total_window_pages = pageView.getAdapter().getItemCount();
                int total_chap_pages = currentNovelChap.getPageNum();
                window_page_index = position;
                switch(windowType){
                    case left:case both:{
                        if (hasLastChap()) {
                            try {
                                NovelPageWindow left_chap = windowList.get(getChapCacheIndex() - 1);
                                chap_page_index = position - left_chap.getPages().size();
                            }
                            catch (Exception e){
                                FileIOUtils.WriteErrReport(e,"ViewerFragment|IndexError:"+String.format(Locale.CHINA,"%d/%d",getChapCacheIndex(),windowList.size()));
                                NovelPageWindow left_chap = windowList.get(getChapCacheIndex() - 1);
                                chap_page_index = position - left_chap.getPages().size();
                            }
                        }
                    }
                    break;
                    case right:case match:{
                        chap_page_index = position;
                    }
                    break;
                    default:
                }
                Log.d("page change",String.format("window %d/%d",window_page_index, total_window_pages-1));
                Log.d("page change",String.format("chap %d/%d",chap_page_index, total_chap_pages-1));
                double progress = 0;
                if (total_chap_pages>1)progress = (double) chap_page_index / (double) (total_chap_pages - 1);//仅一页时认为是 0%
                if (progress > 1)progress = 0;
                if (progress < 0)progress = 1;
                if (!hold_progress)setCurrent_chap_progress(progress);
                else hold_progress = false;
                Log.d("page change",String.format("reading progress: %f",getCurrent_chap_progress()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // System.out.println("state = " + state);
                if (state == SCROLL_STATE_IDLE){
                    isScrolling = false;
                    int total_window_pages = pageView.getAdapter().getItemCount();
                    int total_chap_pages = currentNovelChap.getPages().size();

                    if (chap_page_index == total_chap_pages){
                        Log.d("page change","start the new chap (next)"+(getChapCacheIndex()+1));
                        if (!hasNextChap())return;
                        updateReadingChap(getChapCacheIndex()+1);
                        currentNovelChap = windowList.get(getChapCacheIndex());
                        chap_page_index = 0;
                        windowType = WindowType.left;
                        if (isAuto_download() && getChapCacheIndex()==getChapCache().size()-1)
                            NextChapDownloader(getCurrentChap());
                    }
                    if (chap_page_index == -1){
                        Log.d("page change","start the new chap (last)"+(getChapCacheIndex()-1));
                        if (!hasLastChap())return;
                        updateReadingChap(getChapCacheIndex()-1);
                        currentNovelChap = windowList.get(getChapCacheIndex());
                        chap_page_index = currentNovelChap.getPages().size()-1;
                        windowType = WindowType.right;
                        if (isAuto_download() && getChapCacheIndex()==0)
                            LastChapDownloader(getCurrentChap());
                    }

                    if ((window_page_index == total_window_pages-1) && (total_chap_pages != 1)){
                        Log.d("page change","reach the last page of the window");
                        if (!hasNextChap()){
                            Log.d("page change","also the last chap of the book!");
                            return;
                        }
                        if (getChapCacheIndex()+1 >= getChapCache().size()){
                            Log.d("page change","next chap not ready!");
                            getReadingListener().waitDialogControl(true);
                            if(!isDownloaderRunning()){
                                NextChapDownloader(getCurrentChap());
                            }
                            return;
                        }
                        window_page_index = chap_page_index;
                        ArrayList<NovelContentPage> temp_pages = new ArrayList<>(currentNovelChap.getPages());
                        temp_pages.addAll(windowList.get(getChapCacheIndex()+1).getPages());//single page
                        NovelPageWindow window = new NovelPageWindow();
                        window.setPages(temp_pages);window.setSingleWindow(false);
                        after_update = true;
                        updatePageView(window,true);
                        windowType = WindowType.right;
                    }
                    if ((window_page_index == 0) && (total_chap_pages != 1)){
                        Log.d("page change","reach the first page of the window");
                        if (!hasLastChap()){
                            Log.d("page change","also the first chap of the book!");
                            return;
                        }
                        if (getChapCacheIndex()-1 < 0){
                            Log.d("page change","last chap not ready!");
                            getReadingListener().waitDialogControl(true);
                            //若章节下载线程异常未启动，则重新启动
                            if(!isDownloaderRunning()){
                                LastChapDownloader(getCurrentChap());
                            }
                            return;
                        }
                        ArrayList<NovelContentPage> temp_pages = new ArrayList<>(windowList.get(getChapCacheIndex() - 1).getPages());
                        window_page_index += temp_pages.size();
                        temp_pages.addAll(currentNovelChap.getPages());
                        NovelPageWindow window = new NovelPageWindow();
                        window.setPages(temp_pages);window.setSingleWindow(false);
                        after_update = true;
                        updatePageView(window,true);
                        //pageView.setCurrentItem(window_page_index,false);
                        windowType = WindowType.left;

                    }

                    if (((window_page_index == 0) || (window_page_index == total_window_pages-1)) && (total_chap_pages == 1)){
                        Log.d("page change","reach the only page of the chap");
                        //对于当前章节只有一页的情况
                        ArrayList<NovelContentPage> temp_pages = new ArrayList<>(currentNovelChap.getPages());
                        windowType = WindowType.match;
                        boolean added = false;
                        int add_in_front = 0;
                        //存在右页
                        if (hasNextChap() && (getChapCacheIndex()+1 < getChapCache().size())){
                            ArrayList<NovelContentPage> next_pages = windowList.get(getChapCacheIndex() + 1).getPages();
                            temp_pages.addAll(next_pages);
                            added = true;
                            windowType = WindowType.right;
                        }
                        //存在左页
                        if (hasLastChap() && (getChapCacheIndex()-1 >= 0)){
                            ArrayList<NovelContentPage> last_pages = windowList.get(getChapCacheIndex() - 1).getPages();
                            temp_pages.addAll(0,last_pages);
                            added = true;
                            add_in_front = last_pages.size();
                            if (windowType!=WindowType.match)windowType = WindowType.both;
                            else windowType = WindowType.left;
                        }
                        if (!added)return;
                        window_page_index = chap_page_index + add_in_front;
                        NovelPageWindow window = new NovelPageWindow();
                        window.setPages(temp_pages);window.setSingleWindow(false);
                        after_update = true;
                        updatePageView(window,true);
                    }

                }
                else {
                    isScrolling = true;
                }
            }
        });

        pageView.getChildAt(0).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP){
                getReadingListener().toolbarControl(isScrolling,event.getX(),event.getY());
            }
            return false;
        });

        return view;
    }

    private void updatePageView(RecyclerPageAdapter adapter, boolean isSetToCurrent) {
        //Log.d("update page view","call start");
        pageView.post(() -> {
            pageView.setAdapter(adapter);
            if (isSetToCurrent)pageView.setCurrentItem(window_page_index, false);
        });
        //Log.d("update page view","done, window size: "+update_window.getPageNum());
    }

    private void updatePageView(NovelPageWindow update_window, boolean isSetToCurrent) {
        RecyclerPageAdapter adapter = new RecyclerPageAdapter(context, update_window);
        adapter.setPageListener(pageListener);
        adapter.setText_size(currentTheme.getTextSize());
        adapter.setLine_space(currentTheme.getLineSpaceMulti());
        adapter.setDNMod(currentTheme.getDayNightMode(),false);
        updatePageView(adapter,isSetToCurrent);
    }

    @Override
    public void downloadReady(int download_type, boolean isError) {
        if (download_type == DOWNLOAD_TYPE_LAST_CHAP){
            NovelPageWindow window = new NovelPageWindow();
            window.init(getChapCache().get(0));
            windowList.add(0,window);
        }
        if (download_type == DOWNLOAD_TYPE_NEXT_CHAP){
            NovelPageWindow window = new NovelPageWindow();
            window.init(getChapCache().get(getChapCache().size()-1));
            windowList.add(window);
        }

        if (download_type == DOWNLOAD_TYPE_SKIP_CHAP){
            windowList.clear();
            NovelPageWindow skip_window = new NovelPageWindow();
            skip_window.init(getChapCache().get(0));
            windowList.add(skip_window);

            chap_page_index = 0;
            window_page_index = chap_page_index;
            windowType = WindowType.match;
            currentNovelChap = skip_window;
            currentNovelChap.setSingleWindow(true);
            updatePageView(currentNovelChap,true);
        }
    }

    @Override
    public void cacheInitReady() {
        Log.d("novel view fragment", String.format("cache init: chap_i= %d, window_i= %d",chap_page_index,window_page_index));
        int total_chap = currentNovelChap.getPageNum();
        int total_window = pageView.getAdapter().getItemCount();

        boolean isFirstPage = (chap_page_index == 0) && (window_page_index == 0);
        boolean isLastPage = (chap_page_index == (total_chap-1)) && (window_page_index == (total_window-1));

        if (isFirstPage && isLastPage && total_chap == 1){
            Log.d("cache init","start from a single page chap");
            //对于当前章节只有一页的情况
            ArrayList<NovelContentPage> temp_pages = new ArrayList<>(currentNovelChap.getPages());
            windowType = WindowType.match;
            boolean added = false;
            int add_in_front = 0;
            //存在右页
            if (hasNextChap() && (getChapCacheIndex()+1 < getChapCache().size())){
                ArrayList<NovelContentPage> next_pages = windowList.get(getChapCacheIndex() + 1).getPages();
                temp_pages.addAll(next_pages);
                added = true;
                windowType = WindowType.right;
            }
            //存在左页
            if (hasLastChap() && (getChapCacheIndex()-1 >= 0)){
                ArrayList<NovelContentPage> last_pages = windowList.get(getChapCacheIndex() - 1).getPages();
                temp_pages.addAll(0,last_pages);
                added = true;
                add_in_front = last_pages.size();
                if (windowType!=WindowType.match)windowType = WindowType.both;
                else windowType = WindowType.left;
            }
            if (!added)return;
            window_page_index = chap_page_index + add_in_front;
            NovelPageWindow window = new NovelPageWindow();
            window.setPages(temp_pages);window.setSingleWindow(false);
            after_update = true;
            updatePageView(window,true);
            if (init_page)init_page = false;
        }
        else if (isFirstPage){
            Log.d("cache init","start from the first page of a chap");
            if (hasLastChap()) {
                ArrayList<NovelContentPage> temp_pages =
                        new ArrayList<>(windowList.get(getChapCacheIndex() - 1).getPages());
                window_page_index += temp_pages.size();
                temp_pages.addAll(currentNovelChap.getPages());
                NovelPageWindow window = new NovelPageWindow();
                window.setPages(temp_pages);window.setSingleWindow(false);
                after_update = true;
                updatePageView(window,false);
                if (init_page)init_page = false;
                windowType = WindowType.left;
            }
            else {
                Log.d("cache init","start from the first page of the novel");
                windowType = WindowType.match;
                if (init_page)init_page = false;
            }
        }
        else if (isLastPage){
            Log.d("cache init","start from the last page of a chap");
            if (hasNextChap()) {
                window_page_index = chap_page_index;
                ArrayList<NovelContentPage> temp_pages = new ArrayList<>(currentNovelChap.getPages());
                temp_pages.addAll(windowList.get(getChapCacheIndex() + 1).getPages());//single page
                NovelPageWindow window = new NovelPageWindow();
                window.setPages(temp_pages);
                window.setSingleWindow(false);
                after_update = true;
                updatePageView(window, true);
                if (init_page) init_page = false;
                windowType = WindowType.right;
            }
            else {
                Log.d("cache init","start from the last page of the novel");
                windowType = WindowType.match;
                if (init_page)init_page = false;
            }
        }
    }

    @Override
    public void afterCacheCut() {
        ArrayList<NovelPageWindow>cutWindowList = new ArrayList<>();
        for (NovelPageWindow window : windowList) {
            for (NovelChap chap : getChapCache()) {
                if (window.getChapID() == chap.getCurrentChap()){
                    cutWindowList.add(window);
                    break;
                }
            }
        }
        Log.d("cache cut done",String.format("window cut %d -> %d",windowList.size(),cutWindowList.size()));
        windowList = new ArrayList<>(cutWindowList);
        Log.d("cache cut done","window cut committed: new size "+windowList.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("horizontal fragment", "on destroy called");
        SaveCurrentLine();
        if (ViewerSettingDialog.brightnessObserver!=null)ViewerSettingDialog.brightnessObserver.unregister();
    }

    @Override
    public void onPause() {
        super.onPause();
        SaveCurrentLine();
        saveRecoverFile();
    }

    @Override
    public void onDNModChange(DNMod mod) {
        manageDayNightModeChange(mod);
        RecyclerPageAdapter adapter = (RecyclerPageAdapter) pageView.getAdapter();
        adapter.setDNMod(mod,true);
    }

    private void manageDayNightModeChange(DNMod mod) {
        switch(mod){
            case DAY_MOD:{
                currentTheme.setDayNightMode(mod);
                rl_background.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.comfortGreen));
            }
                break;
            case NIGHT_MOD: {
                currentTheme.setDayNightMode(mod);
                rl_background.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.night_background));
            }
                break;
            default:
        }
    }

    @Override
    public void onViewThemeChange(NovelViewTheme theme, int change_type) {
        for (NovelPageWindow window : windowList) {
            window.mergePages();
            if (window.getChapID() == currentNovelChap.getChapID()){
                currentNovelChap = window;
                windowType = WindowType.match;
            }
        }
        switch(change_type){
            case NovelViewTheme.THEME_TYPE_LINE_SPACE_MULTI:
                currentTheme.setLineSpaceMulti(theme.getLineSpaceMulti());
                break;
            case NovelViewTheme.THEME_TYPE_TEXT_SIZE:
                currentTheme.setTextSize(theme.getTextSize());
                break;
            default:
        }
        after_update = true;
        hold_progress = true;
        updatePageView(currentNovelChap,false);
    }
}
