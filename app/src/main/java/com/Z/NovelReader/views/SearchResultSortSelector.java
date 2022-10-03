package com.Z.NovelReader.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Adapters.SortModeSelectAdapter;
import com.Z.NovelReader.Objects.NovelSearchResult;
import com.Z.NovelReader.R;

import java.util.ArrayList;

public class SearchResultSortSelector extends PopupWindow {

    private RelativeLayout layout;
    private SortModeListener listener;

    public void setListener(SortModeListener listener) {
        this.listener = listener;
    }

    public SearchResultSortSelector(Context context, int default_mode) {
        super(context);
        layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.sort_mode_select_popup, null);
        this.setContentView(layout);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //焦点和阴影
        this.setFocusable(true);
        this.setElevation(8);
        //设置popupWindow弹出窗体位置
        this.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.popwindow_white));
        RecyclerView list = layout.findViewById(R.id.mode_list);
        list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        ArrayList<String> modes = new ArrayList<>();
        modes.add("内容匹配");modes.add("响应时间");modes.add("书源新旧");
        SortModeSelectAdapter adapter = new SortModeSelectAdapter(context,modes);
        adapter.setSelectedMode(default_mode);
        adapter.setOnItemClickListener((position,isReversed) -> {
            if (position==-1)return;
            if (listener!=null)listener.onModeChange(position+1,isReversed);
        });
        list.setAdapter(adapter);
    }

    public void show(View parentView){
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        this.showAsDropDown(parentView,-layout.getMeasuredWidth() + parentView.getWidth(),15);
    }

    public interface SortModeListener{
        void onModeChange(int type,boolean is_reversed);
    }
}
