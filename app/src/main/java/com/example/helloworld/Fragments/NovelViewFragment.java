package com.example.helloworld.Fragments;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helloworld.Adapters.BooklistAdapter;
import com.example.helloworld.Adapters.NovelViewAdapter;
import com.example.helloworld.NovelViewerActivity;
import com.example.helloworld.R;
import com.example.helloworld.Threads.ChapGetterThread;
import com.example.helloworld.Threads.ContentTextThread;
import com.example.helloworld.Utils.Brightness;
import com.example.helloworld.Utils.ScreenSize;
import com.example.helloworld.myObjects.NovelChap;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovelViewFragment extends Fragment {
    RecyclerView mRecyclerView;
    NovelViewAdapter adapter;
    LinearLayout BottomToolBar;
    LinearLayout TopToolBar;
    ImageButton BackToShelf;
    ImageButton btnCatalog;
    ImageButton btnSettings;
    TextView showBookName;
    DrawerLayout drawerLayout;
    ListView catalogList;
    BooklistAdapter booklistAdapter;
    View view;
    View pop_view;
    PopupWindow popSettings;
    SeekBar setTextSize;
    SeekBar setLight;
    private String BookName;
    private int BookID;
    private ArrayList<NovelChap> chapList;
    private ArrayList<String>chapLink;
    private ArrayList<String>chapName;
    private int chap_index=0;
    private int current_chap=0;
    private boolean is_scroll_stop=true;
    private boolean is_toolbar_visible=false;
    private boolean auto_download=false;
    private ExecutorService threadPool;
    private Handler LastChapHandler;
    private Handler NextChapHandler;
    private Handler JumpChapHandler;

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

        //初始化pop window
        pop_view=getLayoutInflater().inflate(R.layout.pop_settings,null);
        popSettings=new PopupWindow(pop_view,ViewGroup.LayoutParams.MATCH_PARENT,300);

        initSettings();

        //初始化handler
        LastChapHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                NovelChap newChap= (NovelChap) msg.obj;
                newChap.setCurrent_chapter(current_chap-1);
                chapList.add(0, newChap);
                adapter.updateChapList(chapList);
                LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                linearLayoutManager.scrollToPositionWithOffset(chap_index+1,3);
                auto_download=true;
            }
        };
        NextChapHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                NovelChap newChap= (NovelChap) msg.obj;
                newChap.setCurrent_chapter(current_chap+1);
                chapList.add(newChap);
                adapter.updateChapList(chapList);
                //LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                //linearLayoutManager.scrollToPositionWithOffset(1,0);
                auto_download=true;
            }
        };
        JumpChapHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
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
                auto_download=true;
                LastChapDownloader(newChap);
                NextChapDownloader(newChap);
                if (current_chap==0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                    linearLayoutManager.scrollToPositionWithOffset(chap_index, 3);
                }
            }
        };

        //初始化线程池
        threadPool= Executors.newFixedThreadPool(2);
        NovelChap currentChap=chapList.get(0);
        LastChapDownloader(currentChap);
        NextChapDownloader(currentChap);

        booklistAdapter=new BooklistAdapter(chapName,getContext(),true,chapList.get(0).getTitle());
        booklistAdapter.setText_size(18);
        adapter=new NovelViewAdapter(getActivity(),chapList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE/**滑动停止**/) {
                    is_scroll_stop=true;
                    int item_count=recyclerView.getLayoutManager().getItemCount();
                    Log.d("test", "recyclerView总共的Item个数:" +
                            String.valueOf(item_count));

                    //获取可见的最后一个view
                    View lastChildView = recyclerView.getChildAt(
                            recyclerView.getChildCount() - 1);

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
                BackToShelf.setImageResource(R.drawable.backarrow_onclick);
                getActivity().onBackPressed();
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
                ChapGetterThread thread=new ChapGetterThread(chapLink.get(position),JumpChapHandler);
                thread.setCurrent_chap(position);
                thread.setChapState(NovelChap.getLinkType(position,chapLink.size()));
                thread.start();
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
                    }else {
                        BottomToolBar.setVisibility(View.INVISIBLE);
                        TopToolBar.setVisibility(View.INVISIBLE);
                        is_toolbar_visible=false;
                    }
                    if (popSettings.isShowing())popSettings.dismiss();
                }

            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initSettings() {
        setTextSize=pop_view.findViewById(R.id.set_text_size);
        setLight=pop_view.findViewById(R.id.set_light);

        setTextSize.setMax(60);
        setTextSize.setMin(10);
        setTextSize.setProgress(20);
        setTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adapter.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
            ChapGetterThread thread=new ChapGetterThread(currentChap.getLast_link(),LastChapHandler);
            thread.setChapState(currentChap.getFurtherLinkType(chapName.size()));
            threadPool.execute(thread);
        }
    }
    private void NextChapDownloader(NovelChap currentChap) {
        if(currentChap.hasNextLink()){
            auto_download=false;
            ChapGetterThread thread=new ChapGetterThread(currentChap.getNext_link(),NextChapHandler);
            thread.setChapState(currentChap.getFurtherLinkType(chapName.size()));
            threadPool.execute(thread);
        }
    }
}
