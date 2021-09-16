package com.Z.NovelReader.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Adapters.NovelViewAdapter;
import com.Z.NovelReader.Dialog.ViewerSettingDialog;
import com.Z.NovelReader.Global.OnReadingListener;
import com.Z.NovelReader.Global.OnSettingChangeListener;
import com.Z.NovelReader.Global.RecyclerViewItemTouchListener;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Threads.CatalogThread;
import com.Z.NovelReader.Threads.ChapGetterThread;
import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.NovelChap;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_PRIVATE;

public class NovelViewFragment extends NovelViewBasicFragment implements OnSettingChangeListener {
    private RecyclerView mRecyclerView;
    private NovelViewAdapter adapter;
    private View view;
    private String BookName;
    private int BookID;
    private NovelSearchThread.TAG BookTag;
    private String BookLink;
    private ArrayList<NovelChap> chapList;
    private NovelCatalog catalog;
    private int chap_index=0;//相对于章节缓存中的序号
    private int current_chap=0;//相对于目录的序号
    private int offset=3;
    private int offset_to_save=3;
    private int myTextSize=20;
    private boolean is_scroll_stop=true;
    private boolean auto_download=false;
    private boolean has_offset=true;
    private boolean need_save=false;
    private ExecutorService threadPool;
    private ChapGetterThread.ChapGetterHandler<FragmentActivity> last_chap_handler;
    private ChapGetterThread.ChapGetterHandler<FragmentActivity> next_chap_handler;
    private ChapGetterThread.ChapGetterHandler<FragmentActivity> jump_chap_handler;
    private CatalogThread.CatalogUpdaterHandler<FragmentActivity> chap_update_handler;
    private NovelDBTools novelDBTools;
    private SharedPreferences myInfo;
    private File Dir;
    private NovelViewAdapter.DNMod currentMod;
    private OnReadingListener readingListener;

    public void setChapList(ArrayList<NovelChap> chapList) {
        this.chapList = chapList;
    }

    public void setBookName(String bookName) {
        BookName = bookName;
    }

    public void setBookID(int bookID) {
        BookID = bookID;
    }

    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    public void setDir(File file){this.Dir=file;}

    public void setOffset(int offset) {
        this.offset = offset;
        has_offset=true;
    }

    public void setCurrentMod(NovelViewAdapter.DNMod currentMod) {
        this.currentMod = currentMod;
    }

    public void setBookLink(String bookLink) {
        BookLink = bookLink;
    }

    public void setBookTag(NovelSearchThread.TAG bookTag) {
        BookTag = bookTag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        if (context instanceof OnReadingListener) {
            readingListener = (OnReadingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ABC_Listener");
        }
        super.onAttach(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_novelview,container,false);
        mRecyclerView=view.findViewById(R.id.Novel_view);
        current_chap=chapList.get(0).getCurrent_chapter();
        //初始化数据库
        novelDBTools= ViewModelProviders.of(this).get(NovelDBTools.class);

        //初始化偏好文件
        myInfo=getActivity().getSharedPreferences("UserInfo",MODE_PRIVATE);
        myTextSize=myInfo.getInt("myTextSize",20);

        //初始化handler
        last_chap_handler=new ChapGetterThread.ChapGetterHandler<>(getActivity());
        last_chap_handler.setListener(new ChapGetterThread.ChapGetterHandler.ChapGetListener() {
            @Override
            public void onSuccess(NovelChap newChap) {
                chapList.add(0, newChap);
                adapter.updateChapList(chapList);
                int m_offset;
                if (has_offset)
                {
                    m_offset=offset;
                    has_offset=false;
                }else{
                    m_offset=3;
                }
                LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                linearLayoutManager.scrollToPositionWithOffset(chap_index+1,m_offset);
            }

            @Override
            public void onInternetError() {
                Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
                LinearLayoutManager linearLayoutManager= (LinearLayoutManager) mRecyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                linearLayoutManager.scrollToPositionWithOffset(0,offset);
            }

            @Override
            public void onGetFinish() {
                auto_download=true;
            }
        });

        next_chap_handler=new ChapGetterThread.ChapGetterHandler<>(getActivity());
        next_chap_handler.setListener(new ChapGetterThread.ChapGetterHandler.ChapGetListener() {
            @Override
            public void onSuccess(NovelChap newChap) {
                chapList.add(newChap);
                adapter.updateChapList(chapList);
            }

            @Override
            public void onInternetError() {
                Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetFinish() {
                auto_download=true;
            }
        });

        jump_chap_handler=new ChapGetterThread.ChapGetterHandler<>(getActivity());
        jump_chap_handler.setListener(new ChapGetterThread.ChapGetterHandler.ChapGetListener() {
            @Override
            public void onSuccess(NovelChap newChap) {
                chapList.clear();
                chapList.add(newChap);
                current_chap=newChap.getCurrent_chapter();
                chap_index=0;
                readingListener.onJumpToNewChap(newChap);
                adapter.updateChapList(chapList);
                LastChapDownloader(newChap);
                NextChapDownloader(newChap);
                if (current_chap==0) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                    assert linearLayoutManager != null;
                    linearLayoutManager.scrollToPositionWithOffset(chap_index, 3);
                }
            }

            @Override
            public void onInternetError() {
                Toast.makeText(getContext(), "无网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetFinish() {
                auto_download=true;
            }
        });

        //初始化线程池
        threadPool= Executors.newFixedThreadPool(2);
        final NovelChap currentChap=chapList.get(0);
        LastChapDownloader(currentChap);
        NextChapDownloader(currentChap);

        //初始化viewer
        adapter=new NovelViewAdapter(getActivity(),chapList);
        adapter.setTextSize(myTextSize);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        if (current_chap==0) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            assert linearLayoutManager != null;
            linearLayoutManager.scrollToPositionWithOffset(0, offset);
        }
        switch(currentMod){
            case DAY_MOD:switch_to_day_mod();
                break;
            case NIGHT_MOD:switch_to_night_mod();
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
                    int item_count= Objects.requireNonNull(recyclerView.getLayoutManager()).getItemCount();
                    Log.d("test", "recyclerView总共的Item个数:" +
                            item_count);

                    //获取可见的最后一个view
                    View lastChildView = recyclerView.getChildAt(
                            recyclerView.getChildCount() - 1);
                    offset_to_save=lastChildView.getTop();
                    //获取可见的最后一个view的位置
                    int lastChildViewPosition = recyclerView.getChildAdapterPosition(lastChildView);
                    if(chap_index!=lastChildViewPosition){
                        chap_index=lastChildViewPosition;
                        Log.d("viewer","item = "+chap_index);
                        current_chap=chapList.get(chap_index).getCurrent_chapter();
                        readingListener.onReadNewChap(current_chap);
                    }
                    Log.d("viewer","size= "+chapList.size());
                    if (auto_download) {
                        Log.d("viewer","index= "+chap_index);

                        if (chap_index == 0) {
                            int top= Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(0)).getTop();
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
                }
                readingListener.onScroll(is_scroll_stop);

            }
        });

        //屏幕点击识别
        GestureDetector.OnGestureListener gl = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                readingListener.onScreenTouch(e.getX(),e.getY());
                return false;
            }
        };
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemTouchListener(
                new GestureDetectorCompat(mRecyclerView.getContext(),gl)));

        return view;
    }

    private void switch_to_day_mod() {
        adapter.setDNMod(NovelViewAdapter.DNMod.DAY_MOD);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.comfortGreen));
    }

    private void switch_to_night_mod() {
        adapter.setDNMod(NovelViewAdapter.DNMod.NIGHT_MOD);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.night_background));
    }

    private void refreshChap() {
        NovelChap refresh_chap=chapList.get(chap_index);
        chapList.clear();
        chapList.add(refresh_chap);
        current_chap=refresh_chap.getCurrent_chapter();
        chap_index=0;
        readingListener.onJumpToNewChap(refresh_chap);
        adapter.updateChapList(chapList);
        LastChapDownloader(refresh_chap);
        NextChapDownloader(refresh_chap);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO:保存当前章节
        if (need_save)SaveCurrentLine();
        //saveDayNightMod();
        if (ViewerSettingDialog.brightnessObserver!=null)ViewerSettingDialog.brightnessObserver.unregister();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (need_save)SaveCurrentLine();
        saveRecoverFile(BookName);
    }

    private void SaveCurrentLine() {
        Novels novel=new Novels(BookName,catalog.getSize(),current_chap,BookLink);
        novel.setId(BookID);
        novel.setOffset(offset_to_save);
        //notice need update
        //novel.setTag_inTAG(BookTag);
        novelDBTools.updateNovels(novel);
        Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                FileIOUtils.WriteTXT(Dir,BookName,chapList.get(chap_index).getContent());
            }
        });
        thread.start();
    }

    private void LastChapDownloader(NovelChap currentChap) {
        if(currentChap.hasLastLink()){
            auto_download=false;
            NovelChap newChap=new NovelChap(catalog.getTitle().get(current_chap-1),BookTag);//章节名，标签
            newChap.setCurrent_chapter(current_chap-1);
            ChapGetterThread thread=new ChapGetterThread(currentChap.getLast_link(),newChap,last_chap_handler);
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
            ChapGetterThread thread=new ChapGetterThread(currentChap.getNext_link(),newChap,next_chap_handler);
            thread.setChapState(currentChap.getFurtherLinkType(catalog.getSize()));
            thread.setCatalog(catalog);
            threadPool.execute(thread);
        }
    }

    @Override
    public void onDNModChange(NovelViewAdapter.DNMod mod) {
        switch(mod){
            case DAY_MOD:switch_to_day_mod();
                break;
            case NIGHT_MOD:switch_to_night_mod();
                break;
            default:
        }
    }

    @Override
    public void onCatalogItemClick(int position) {
        auto_download=false;
        NovelChap newChap=new NovelChap(catalog.getTitle().get(position),BookTag);
        newChap.setCurrent_chapter(position);
        ChapGetterThread thread=new ChapGetterThread(catalog.getLink().get(position),newChap,jump_chap_handler);
        thread.setChapState(NovelChap.getLinkType(position,catalog.getSize()));
        thread.setCatalog(catalog);
        thread.start();
    }

    @Override
    public void onTextSizeChange(int size) {
        adapter.setTextSize(size);
    }

    @Override
    public void onCatalogUpdate(NovelCatalog new_catalog) {
        catalog=new_catalog;
    }
}
