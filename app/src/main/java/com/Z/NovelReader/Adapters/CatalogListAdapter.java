package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.views.FlexibleRectDrawable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CatalogListAdapter extends BaseAdapter {
    private List<String> catalogItems;
    private List<Boolean> downloadStateList;
    private Context context;
    //private Map<Integer, Set<String>> color_items_map;//列表字体颜色 与 需要上色的字体的内容集合 的键值对
    private int text_size=20;
    private int basic_item_color;
    private int highlight_item_color;
    private int downloaded_color;
    private int normal_indicator_color;
    private String highlight_title;
    private enum IndicatorType{Single,Top,Bottom,Mid}
    public CatalogListAdapter(List<String> catalog_items, Context context) {
        this.catalogItems = catalog_items;
        this.context=context;
        this.downloaded_color = context.getColor(R.color.PaleGreen);
        this.normal_indicator_color = context.getColor(R.color.float_transparent);
        //this.color_items_map = new HashMap<>();
    }
    public Context getContext(){
        return context;
    }

    public void setBasicItemColor(int color){
        this.basic_item_color = color;
    }

    public void setHighlightTitle(int color, String title){
        this.highlight_item_color = color;
        this.highlight_title = title;
    }

    public void setDownloadStateList(List<Boolean> downloadStateList) {
        this.downloadStateList = downloadStateList;
        if(this.downloadStateList.size() != catalogItems.size())throw new RuntimeException("目录条目数与下载状态数不匹配");
    }

    //    public void addColoredItems(int color, Set<String> items){
//        this.color_items_map.put(color,items);
//        notifyDataSetChanged();
//    }
//
//    public void addColoredItems(int color, String...s){
//        Set<String> items = new HashSet<>();
//        items.addAll(Arrays.asList(s));
//        this.color_items_map.put(color,items);
//        notifyDataSetChanged();
//    }

    public void updateList(List<String>items,List<Boolean> isDownload){
        catalogItems = items;
        setDownloadStateList(isDownload);
        notifyDataSetChanged();
    }

    public void setTextSize(int text_size) {
        this.text_size = text_size;
    }

    @Override
    public int getCount() {
        return catalogItems.size();
    }

    @Override
    public String getItem(int position) {
        return catalogItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout Layout=null;
        if(convertView!=null) {
            Layout= (LinearLayout) convertView;
        }
        else {
            Layout= (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.catalog_list_item,null);
        }
        TextView tv_catalog_title =Layout.findViewById(R.id.catalog_title);
        LinearLayout ll_indicator = Layout.findViewById(R.id.catalog_indicator);
        String current_item_name = getItem(position);
        boolean isDownloaded = downloadStateList!=null?downloadStateList.get(position):false;

        //set title style
        if(current_item_name.equals(highlight_title)){
            tv_catalog_title.setTextColor(highlight_item_color);
            tv_catalog_title.setTextSize(text_size+2);
            tv_catalog_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv_catalog_title.setSelected(true);
        }
        else {
            tv_catalog_title.setTextColor(basic_item_color);
            tv_catalog_title.setTextSize(text_size);
            tv_catalog_title.setEllipsize(TextUtils.TruncateAt.END);
        }

        //set indicator
        if(position == 0 && isDownloaded){
            if(getCount()==1)
                ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Single));
            else ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Top));
        }
        else if(position == getCount()-1 && isDownloaded){
            //getCount == 1的情况包含在第一个分支中
            ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Bottom));
        }
        else if(isDownloaded){
            Boolean isLastDownloaded = downloadStateList.get(position-1);
            Boolean isNextDownloaded = downloadStateList.get(position+1);
            if(isLastDownloaded && isNextDownloaded){
                ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Mid));
            }
            else if(isLastDownloaded){
                ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Bottom));
            }
            else if(isNextDownloaded){
                ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Top));
            }
            else {
                ll_indicator.setBackground(createIndicatorBackground(IndicatorType.Single));
            }
        }
        else ll_indicator.setBackground(new ColorDrawable(normal_indicator_color));

        tv_catalog_title.setText(current_item_name);
        return Layout;
    }
    private FlexibleRectDrawable createIndicatorBackground(IndicatorType type){
        switch(type){
            case Top:
                return FlexibleRectDrawable.Builder.create()
                        .setCorners(ScreenUtils.dip2px(context,10),FlexibleRectDrawable.CORNER_TOP_LEFT|FlexibleRectDrawable.CORNER_TOP_RIGHT)
                        .setSolidFill(downloaded_color)
                        .build();
            case Mid:
                return FlexibleRectDrawable.Builder.create()
                        .setSolidFill(downloaded_color)
                        .build();
            case Bottom:
                return FlexibleRectDrawable.Builder.create()
                        .setCorners(ScreenUtils.dip2px(context,10),FlexibleRectDrawable.CORNER_BOTTOM_LEFT|FlexibleRectDrawable.CORNER_BOTTOM_RIGHT)
                        .setSolidFill(downloaded_color)
                        .build();
            case Single:
               return FlexibleRectDrawable.Builder.create()
                        .setCorners(ScreenUtils.dip2px(context,10),FlexibleRectDrawable.CORNER_ALL)
                        .setSolidFill(downloaded_color)
                        .build();
            default:
        }
        return null;
    }
}
