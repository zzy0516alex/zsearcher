package com.example.helloworld.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helloworld.Adapters.BooklistAdapter;
import com.example.helloworld.Adapters.NovelViewAdapter;
import com.example.helloworld.BookShelfActivity;
import com.example.helloworld.NovelRoom.NovelDBTools;
import com.example.helloworld.NovelRoom.Novels;
import com.example.helloworld.R;
import com.example.helloworld.Threads.CatalogThread;
import com.example.helloworld.Threads.ChapGetterThread;
import com.example.helloworld.Threads.NovelThread;
import com.example.helloworld.Utils.Brightness;
import com.example.helloworld.Utils.IOtxt;
import com.example.helloworld.Utils.ScreenSize;
import com.example.helloworld.Utils.StatusBarUtil;
import com.example.helloworld.Utils.ViberateControl;
import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelChap;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_PRIVATE;

public class NovelViewFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private NovelViewAdapter adapter;
    private LinearLayout BottomToolBar;
    private LinearLayout TopToolBar;
    private ImageButton BackToShelf;
    private ImageButton btnCatalog;
    private ImageButton btnSettings;
    private ImageButton Mod_btn;
    private ImageButton Refresh;
    private TextView showBookName;
    private DrawerLayout drawerLayout;
    private ListView catalogList;
    private BooklistAdapter booklistAdapter;
    private View view;
    private View pop_view;
    private PopupWindow popSettings;
    private SeekBar setTextSize;
    private SeekBar setLight;
    private String BookName;
    private int BookID;
    private NovelThread.TAG BookTag;
    private String BookLink;
    private ArrayList<NovelChap> chapList;
    private ArrayList<String>chapLink;
    private ArrayList<String>chapName;
    private NovelCatalog catalog;
    private int chap_index=0;
    private int current_chap=0;
    private int offset=3;
    private int offset_to_save=3;
    private int myTextSize=20;
    private boolean is_scroll_stop=true;
    private boolean is_toolbar_visible=false;
    private boolean auto_download=false;
    private boolean has_offset=true;
    private boolean need_save=false;
    private ExecutorService threadPool;
    private Handler LastChapHandler;
    private Handler NextChapHandler;
    private Handler JumpChapHandler;
    private Handler chap_update_handler;
    private NovelDBTools novelDBTools;
    private SharedPreferences myInfo;
    private File Dir;
    private NovelViewAdapter.DNMod currentMod;
    private Window window;

    public void setChapList(ArrayList<NovelChap> chapList) {
        this.chapList = chapList;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
    }

    public void setBookID(int bookID) {
        BookID = bookID;
    }

    public void setChapName(ArrayList<String> chapName) {
        this.chapName = chapName;
    }

    public void setChapLink(ArrayList<String> chapLink) {
        this.chapLink = chapLink;
    }

    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    public void setDir(File file){this.Dir=file;}

    public void setOffset(int offset) {
        this.offset = offset;
        has_offset=true;
    }

    public void setBookLink(String bookLink) {
        BookLink = bookLink;
    }

    public void setBookTag(NovelThread.TAG bookTag) {
        BookTag = bookTag;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_novelview,container,false);
        mRecyclerView=view.findViewById(R.id.Novel_view);
        BottomToolBar=view.findViewById(R.id.bottom_tool_bar);
        TopToolBar=view.findViewById(R.id.top_tool_bar);
        BottomToolBar.setAlpha(0.97f);
        TopToolBar.setAlpha(0.99f);
        BottomToolBar.setVisibility(View.INVISIBLE);
        TopToolBar.setVisibility(View.INVISIBLE);
        BackToShelf=view.findViewById(R.id.back_to_shelf);
        btnCatalog=view.findViewById(R.id.catalog_of_novelview);
        btnSettings=view.findViewById(R.id.settings_of_novelview);
        showBookName=view.findViewById(R.id.show_book_name);
        showBookName.setText(BookName);
        drawerLayout=view.findViewById(R.id.drawer_layout);
        catalogList=view.findViewById(R.id.catalog_list);
        current_chap=chapList.get(0).getCurrent_chapter();
        Mod_btn = view.findViewById(R.id.day_night_switch);
        Refresh = view.findViewById(R.id.refresh);

        //初始化pop window
        pop_view=getLayoutInflater().inflate(R.layout.pop_settings,null);
        popSettings=new PopupWindow(pop_view,ViewGroup.LayoutParams.MATCH_PARENT,300);

        //初始化状态栏
        window= getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        window.setStatusBarColor(getResources().getColor(R.color.comfortGreen));

        //初始化数据库
        novelDBTools= ViewModelProviders.of(this).get(NovelDBTools.class);

        //初始化偏好文件
        myInfo=getActivity().getSharedPreferences("UserInfo",MODE_PRIVATE);
        myTextSize=myInfo.getInt("myTextSize",20);

        //初始化设置条
        initSettings();

        //初始化handler
        LastChapHandler=new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case ChapGetterThread.GET_SUCCEED:
                    {
                        NovelChap newChap= (NovelChap) msg.obj;
                        //newChap.setCurrent_chapter(current_chap-1);
                        chapList.add(0, newChap);
                        adapter.updateChapList(chapList);
                        int m_offset=0;
                        if (has_offset)
                        {
                            m_offset=offset;
                            has_offset=false;
                        }else{
                            m_offset=3;
                        }
                        LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                        linearLayoutManager.scrollToPositionWithOffset(chap_index+1,m_offset);
                    }
                        break;
                    case ChapGetterThread.INTERNET_ERROR:
                    {
                        Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
                        LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                        linearLayoutManager.scrollToPositionWithOffset(0,offset);
                    }
                        break;
                    default:
                }
                auto_download=true;
            }
        };
        NextChapHandler=new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case ChapGetterThread.GET_SUCCEED:
                    {
                        NovelChap newChap= (NovelChap) msg.obj;
                        //newChap.setCurrent_chapter(current_chap+1);
                        chapList.add(newChap);
                        adapter.updateChapList(chapList);
                    }
                        break;
                    case ChapGetterThread.INTERNET_ERROR:
                    {
                        Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
                    }
                        break;
                    default:
                }
                auto_download=true;
            }
        };
        JumpChapHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case ChapGetterThread.GET_SUCCEED:
                    {
                        NovelChap newChap= (NovelChap) msg.obj;
                        drawerLayout.closeDrawer(view.findViewById(R.id.left_layout));
                        BottomToolBar.setVisibility(View.INVISIBLE);
                        TopToolBar.setVisibility(View.INVISIBLE);
                        chapList.clear();
                        chapList.add(newChap);
                        current_chap=newChap.getCurrent_chapter();
                        chap_index=0;
                        booklistAdapter.setItem_to_paint(newChap.getTitle());
                        adapter.updateChapList(chapList);
                        LastChapDownloader(newChap);
                        NextChapDownloader(newChap);
                        if (current_chap==0) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                            linearLayoutManager.scrollToPositionWithOffset(chap_index, 3);
                        }
                    }
                        break;
                    case ChapGetterThread.INTERNET_ERROR:
                    {
                        Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
                    }
                        break;
                    default:
                }
                auto_download=true;
            }
        };

        chap_update_handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case CatalogThread.CATALOG_UPDATED:
                    {
                        //NovelCatalog catalog;
                        catalog= IOtxt.read_catalog(BookName,Dir);
                        //chapName=catalog.getTitle();
                        //chapLink=catalog.getLink();
                        booklistAdapter.updateList(catalog.getTitle());
                        Refresh.clearAnimation();
                        Toast.makeText(getActivity(), "章节同步完成", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case CatalogThread.CATALOG_UPDATE_FAILED:{
                        Refresh.clearAnimation();
                        Toast.makeText(getActivity(), "章节同步出错", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    default:
                }
            }
        };

        //初始化线程池
        threadPool= Executors.newFixedThreadPool(2);
        final NovelChap currentChap=chapList.get(0);
        LastChapDownloader(currentChap);
        NextChapDownloader(currentChap);

        booklistAdapter=new BooklistAdapter(catalog.getTitle(),getContext(),true,chapList.get(0).getTitle());
        booklistAdapter.setText_size(18);
        adapter=new NovelViewAdapter(getActivity(),chapList);
        adapter.setTextSize(myTextSize);
        adapter.setDNMod(currentMod);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        if (current_chap==0) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            linearLayoutManager.scrollToPositionWithOffset(0, offset);
        }
        //用户偏好初始化
        int myDNMod=0;
        myDNMod=myInfo.getInt("myDNMod",0);
        switch(myDNMod){
            case 0:
                currentMod= NovelViewAdapter.DNMod.DAY_MOD;
                switch_to_day_mod();
                break;
            case 1:
                currentMod= NovelViewAdapter.DNMod.NIGHT_MOD;
                switch_to_night_mod();
                break;
            default:
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE/*滑动停止*/) {
                    is_scroll_stop=true;
                    need_save=true;
                    int item_count=recyclerView.getLayoutManager().getItemCount();
                    Log.d("test", "recyclerView总共的Item个数:" +
                            String.valueOf(item_count));

                    //获取可见的最后一个view
                    View lastChildView = recyclerView.getChildAt(
                            recyclerView.getChildCount() - 1);
                    offset_to_save=lastChildView.getTop();
                    //获取可见的最后一个view的位置
                    int lastChildViewPosition = recyclerView.getChildAdapterPosition(lastChildView);
                    if(chap_index!=lastChildViewPosition){
                        chap_index=lastChildViewPosition;
                        Log.d("viewer","item = "+chap_index);
                        booklistAdapter.setItem_to_paint(chapList.get(chap_index).getTitle());
                        current_chap=chapList.get(chap_index).getCurrent_chapter();
                    }
                    Log.d("viewer","size= "+chapList.size());
                    if (auto_download) {
                        Log.d("viewer","index= "+chap_index);

                        if (chap_index == 0) {
                            int top=recyclerView.getLayoutManager().getChildAt(0).getTop();
                            int Y=recyclerView.getScrollY();
                            if(top==Y){
                                Log.d("viewer","reach");
                                LastChapDownloader(chapList.get(chap_index));
                            }
                        }else if (chap_index==chapList.size()-1){
                            NextChapDownloader(chapList.get(chap_index));
                        }
                    }

                    Log.d("viewer","cur_chap= "+current_chap);

                    if (item_count>12){
                        boolean remove=true;
                        if (chap_index>6)remove=false;
                        int Cut_min=chap_index-3;
                        int Cut_max=chap_index+3;
                        if (Cut_max > chapList.size()-1) Cut_max=chapList.size()-1;
                        if (Cut_min < 0) Cut_min=0;
                        if (remove) {
                            for (int i = chapList.size() - 1; i >= 0; i--) {
                                if (i < Cut_min || i > Cut_max) {
                                    chapList.remove(i);
                                }
                            }
                        }
                    }
                    if (item_count>50){
                        int new_item_top=recyclerView.getLayoutManager().getChildAt(recyclerView.getChildCount()-1).getTop();
                        if (new_item_top < 0){
                            Log.d("top", "new item reach top: offset = "+new_item_top);
                            has_offset=true;
                            offset=new_item_top;
                            refreshChap();
                        }
                    }
                }
                else {
                    is_scroll_stop=false;
                    if (is_toolbar_visible){
                        BottomToolBar.setVisibility(View.INVISIBLE);
                        TopToolBar.setVisibility(View.INVISIBLE);
                        is_toolbar_visible=false;
                    }
                }

            }
        });

        //返回书架
        BackToShelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViberateControl.Vibrate(getActivity(),15);
                BackToShelf.setImageResource(R.drawable.backarrow_onclick);
                getActivity().onBackPressed();
            }
        });

        //日夜间模式切换
        Mod_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(currentMod){
                    case DAY_MOD:
                    {
                        //当前日间模式，点击切换为夜间
                        currentMod= NovelViewAdapter.DNMod.NIGHT_MOD;
                        switch_to_night_mod();
                    }
                        break;
                    case NIGHT_MOD:
                    {
                        //当前夜间模式，点击切换为日间
                        currentMod= NovelViewAdapter.DNMod.DAY_MOD;
                        switch_to_day_mod();
                    }
                        break;
                    default:
                }

            }
        });

        //打开目录
        btnCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catalogList.setAdapter(booklistAdapter);
                drawerLayout.openDrawer(view.findViewById(R.id.left_layout));
                if (current_chap>7)catalogList.setSelection(current_chap-6);
                else catalogList.setSelection(current_chap);
            }
        });

        //打开设置
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int[] windowPos = getPosition(v, pop_view);
                popSettings.showAtLocation(v, Gravity.TOP|Gravity.START,windowPos[0],windowPos[1]+8);
                popSettings.setOutsideTouchable(true);
                BottomToolBar.setVisibility(View.INVISIBLE);

            }
        });

        //目录点击
        catalogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                auto_download=false;
                NovelChap newChap=new NovelChap(catalog.getTitle().get(position),BookTag);
                newChap.setCurrent_chapter(position);
                ChapGetterThread thread=new ChapGetterThread(catalog.getLink().get(position),newChap,JumpChapHandler);
                thread.setChapState(NovelChap.getLinkType(position,catalog.getSize()));
                thread.setCatalog(catalog);
                thread.start();
            }
        });
        //目录刷新点击
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                Refresh.startAnimation(animation);
                CatalogThread catalogThread=new CatalogThread(BookLink,BookTag);
                catalogThread.setIf_output(true,BookName,Dir);
                catalogThread.need_update(true,getContext(),chap_update_handler);
                catalogThread.start();
            }
        });

        //设置栏显示
        adapter.setRecycleItemClickListener(new NovelViewAdapter.OnRecycleItemClickListener() {
            @Override
            public void onClick(View view) {
                if (is_scroll_stop){
                    if (!is_toolbar_visible) {
                        BottomToolBar.setVisibility(View.VISIBLE);
                        TopToolBar.setVisibility(View.VISIBLE);
                        is_toolbar_visible = true;
                        window.setStatusBarColor(getResources().getColor(R.color.deepBlue));
                    }else {
                        BottomToolBar.setVisibility(View.INVISIBLE);
                        TopToolBar.setVisibility(View.INVISIBLE);
                        is_toolbar_visible=false;
                        if (currentMod == NovelViewAdapter.DNMod.DAY_MOD)
                            window.setStatusBarColor(getResources().getColor(R.color.comfortGreen));
                        else window.setStatusBarColor(getResources().getColor(R.color.night_background));
                    }
                    if (popSettings.isShowing())popSettings.dismiss();
                }

            }
        });

        return view;
    }

    private void switch_to_day_mod() {
        adapter.setDNMod(NovelViewAdapter.DNMod.DAY_MOD);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.comfortGreen));
        Brightness.changeAppBrightness(getActivity(),Brightness.getSystemBrightness(getActivity().getContentResolver()));
        setLight.setProgress(Brightness.getSystemBrightness(getActivity().getContentResolver()));
        Mod_btn.setImageResource(R.drawable.night_mod);
        if (!is_toolbar_visible)window.setStatusBarColor(getResources().getColor(R.color.comfortGreen));
    }

    private void switch_to_night_mod() {
        adapter.setDNMod(NovelViewAdapter.DNMod.NIGHT_MOD);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.night_background));
        Brightness.changeAppBrightness(getActivity(),120);
        setLight.setProgress(120);
        Mod_btn.setImageResource(R.drawable.day_mod);
        window.setStatusBarColor(getResources().getColor(R.color.deepBlue));
    }

    private void refreshChap() {
        NovelChap refresh_chap=chapList.get(chap_index);
        chapList.clear();
        chapList.add(refresh_chap);
        current_chap=refresh_chap.getCurrent_chapter();
        chap_index=0;
        booklistAdapter.setItem_to_paint(refresh_chap.getTitle());
        adapter.updateChapList(chapList);
        LastChapDownloader(refresh_chap);
        NextChapDownloader(refresh_chap);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO:保存当前章节
        if (need_save)SaveCurrentLine();
        SharedPreferences.Editor editor=myInfo.edit();
        int myDNMod=0;
        switch(currentMod){
            case DAY_MOD:
                myDNMod=0;
                break;
            case NIGHT_MOD:
                myDNMod=1;
                break;
            default:
        }
        editor.putInt("myDNMod", myDNMod);
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (need_save)SaveCurrentLine();
    }

    private void SaveCurrentLine() {
        Novels novel=new Novels(BookName,catalog.getSize(),current_chap,BookLink);
        novel.setId(BookID);
        novel.setOffset(offset_to_save);
        novel.setTag_inTAG(BookTag);
        novelDBTools.updateNovels(novel);
        Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                IOtxt.WriteTXT(Dir,BookName,chapList.get(chap_index).getContent());
            }
        });
        thread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initSettings() {
        setTextSize=pop_view.findViewById(R.id.set_text_size);
        setLight=pop_view.findViewById(R.id.set_light);

        setTextSize.setMax(60);
        setTextSize.setMin(10);
        setTextSize.setProgress(myTextSize);

        setTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adapter.setTextSize(progress);
                myTextSize=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor=myInfo.edit();
                editor.putInt("myTextSize",myTextSize);
                editor.apply();
            }
        });

        setLight.setMax(4095);
        setLight.setProgress(Brightness.getSystemBrightness(getActivity().getContentResolver()));
        setLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Brightness.changeAppBrightness(getActivity(),progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int[] getPosition(View v, View pop_view) {
        final int[] windowPos = new int[2];
        final int[] anchorLoc = new int[2];
        v.getLocationOnScreen(anchorLoc);
        final int anchorHeight = v.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = ScreenSize.getScreenHeight(v.getContext());
        final int screenWidth = ScreenSize.getScreenWidth(v.getContext());
        pop_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算popView的高宽
        final int windowHeight = pop_view.getMeasuredHeight();
        final int windowWidth = pop_view.getMeasuredWidth();
        windowPos[0] = screenWidth - windowWidth;
        windowPos[1] = anchorLoc[1] - windowHeight;
        return windowPos;
    }


    private void LastChapDownloader(NovelChap currentChap) {
        if(currentChap.hasLastLink()){
            auto_download=false;
            NovelChap newChap=new NovelChap(catalog.getTitle().get(current_chap-1),BookTag);//章节名，标签
            newChap.setCurrent_chapter(current_chap-1);
            ChapGetterThread thread=new ChapGetterThread(currentChap.getLast_link(),newChap,LastChapHandler);
            thread.setChapState(currentChap.getFurtherLinkType(catalog.getSize()));
            thread.setCatalog(catalog);
            threadPool.execute(thread);
        }
    }
    private void NextChapDownloader(NovelChap currentChap) {
        if(currentChap.hasNextLink()){
            auto_download=false;
            NovelChap newChap=new NovelChap(catalog.getTitle().get(current_chap+1),BookTag);//章节名，标签
            newChap.setCurrent_chapter(current_chap+1);
            ChapGetterThread thread=new ChapGetterThread(currentChap.getNext_link(),newChap,NextChapHandler);
            thread.setChapState(currentChap.getFurtherLinkType(catalog.getSize()));
            thread.setCatalog(catalog);
            threadPool.execute(thread);
        }
    }

}
