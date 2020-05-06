package com.example.helloworld.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helloworld.Adapters.NovelViewAdapter;
import com.example.helloworld.R;
import com.example.helloworld.myObjects.NovelChap;

import java.util.ArrayList;

public class NovelViewFragment extends Fragment {
    RecyclerView mRecyclerView;
    NovelViewAdapter adapter;
    private ArrayList<NovelChap> chapList;
    private int chap_index=-1;

    public void setChapList(ArrayList<NovelChap> chapList) {
        this.chapList = chapList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_novelview,container,false);
        mRecyclerView=view.findViewById(R.id.Novel_view);
        adapter=new NovelViewAdapter(getActivity(),chapList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE/**滑动停止**/) {
                    Log.d("test", "recyclerView总共的Item个数:" +
                            String.valueOf(recyclerView.getLayoutManager().getItemCount()));
                    Log.d("test", "recyclerView可见的Item个数:" +
                            String.valueOf(recyclerView.getChildCount()));
                    //获取可见的最后一个view
                    View lastChildView = recyclerView.getChildAt(
                            recyclerView.getChildCount() - 1);

                    //获取可见的最后一个view的位置
                    int lastChildViewPosition = recyclerView.getChildAdapterPosition(lastChildView);
                    if(chap_index!=lastChildViewPosition){
                        chap_index=lastChildViewPosition;
                        Log.d("viewer","item = "+chap_index);
                    }
                }
            }
        });
        return view;
    }
}
