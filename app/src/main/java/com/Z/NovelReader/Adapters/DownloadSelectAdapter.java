package com.Z.NovelReader.Adapters;

import static com.Z.NovelReader.Utils.ScreenUtils.dip2px;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.views.FlexibleRectDrawable;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.List;

public class DownloadSelectAdapter extends RecyclerView.Adapter<DownloadSelectAdapter.ViewHolder> {

    private Context context;
    private List<NovelCatalog.CatalogItem> items;
    private View.OnLongClickListener longClickListener;
    private View.OnClickListener clickListener;
    private NovelCatalog.CatalogItem currentItem;

    private int textColorSelect = Color.BLACK;
    private int textColorNormal = Color.parseColor("#DCDCDC");

    private enum DrawableType{DOWNLOAD,NORMAL,SELECTED,CURRENT_DOWNLOAD,CURRENT_NORMAL,CURRENT_SELECTED}

    public DownloadSelectAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    private FlexibleRectDrawable createDrawable(DrawableType type){
        FlexibleRectDrawable drawable = null;
        switch(type){
            case DOWNLOAD:
               drawable = FlexibleRectDrawable.Builder.create()
                        .setSolidFill(context.getColor(R.color.downloadGreen))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            case NORMAL:
                drawable = FlexibleRectDrawable.Builder.create()
                        .setStroke(dip2px(context,2),context.getColor(R.color.SteelBlue))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            case SELECTED:
                drawable = FlexibleRectDrawable.Builder.create()
                        .setSolidFill(context.getColor(R.color.SteelBlue))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            case CURRENT_NORMAL:
                drawable = FlexibleRectDrawable.Builder.create()
                        .setStroke(dip2px(context,3), context.getColor(R.color.DoderBlue))
                        .setShadow(dip2px(context,3),context.getColor(R.color.CyanShadow))
                        .setShadowOffsetCenter(dip2px(context,1))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            case CURRENT_DOWNLOAD:
                drawable = FlexibleRectDrawable.Builder.create()
                        .setStroke(dip2px(context,3), context.getColor(R.color.DoderBlue))
                        .setSolidFill(context.getColor(R.color.downloadGreen))
                        .setShadow(dip2px(context,3),context.getColor(R.color.CyanShadow))
                        .setShadowOffsetCenter(dip2px(context,1))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            case CURRENT_SELECTED:
                drawable = FlexibleRectDrawable.Builder.create()
                        .setStroke(dip2px(context,3), context.getColor(R.color.DoderBlue))
                        .setSolidFill(context.getColor(R.color.SteelBlue))
                        .setShadow(dip2px(context,3),context.getColor(R.color.CyanShadow))
                        .setShadowOffsetCenter(dip2px(context,1))
                        .setCorners(dip2px(context,50), FlexibleRectDrawable.CORNER_ALL)
                        .build();
                break;
            default:
        }
        return drawable;
    }

    public void setCurrentItem(NovelCatalog.CatalogItem currentItem) {
        this.currentItem = currentItem;
    }

    public void addItem(NovelCatalog.CatalogItem item){
        item.index = this.items.size();
        this.items.add(item);
    }

    public void updateItem(int index, NovelCatalog.CatalogItem item){
        this.items.set(index,item);
        notifyItemChanged(index);
    }

    public void setLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void reverseSelection(int position){
        items.get(position).isSelected = (!items.get(position).isSelected);
        notifyItemChanged(position);
    }

    public void setDownloaded(int position,boolean isDownloaded){
        if(items.get(position).isDownloading){
            items.get(position).isDownloaded = isDownloaded;
            notifyItemChanged(position);
        }
    }

    public void setSelected(int position, boolean selected) {
        if (items.get(position).isSelected != selected) {
            items.get(position).isSelected = selected;
            notifyItemChanged(position);
        }
    }

    public void unSelectAll(){
        for (NovelCatalog.CatalogItem item : items) {
            item.isSelected = false;
        }
        notifyDataSetChanged();
    }

    public void selectRangeChange(int start, int end, boolean isSelected) {
        if (start < 0 || end >= items.size()) {
            return;
        }
        if (isSelected) {
            for (int i = start; i <= end; i++) {
                NovelCatalog.CatalogItem item = items.get(i);
                if (!item.isSelected && !item.isDownloaded) {
                    item.isSelected = true;
                }
            }
            notifyItemRangeChanged(start,end-start+1);
        } else {
            for (int i = start; i <= end; i++) {
                NovelCatalog.CatalogItem item = items.get(i);
                if (item.isSelected) {
                    item.isSelected = false;
                }
            }
            notifyItemRangeChanged(start,end-start+1);
        }
    }

    public void setSelectedItemToDownloadMode(){
        for (NovelCatalog.CatalogItem item : items) {
            if(item.isSelected){
                item.isDownloading = true;
                item.isSelected = false;
                notifyItemChanged(item.index);
            }
        }
    }

    public List<NovelCatalog.CatalogItem> getAllSelectedItem(){
        List<NovelCatalog.CatalogItem> selected = new ArrayList<>();
        for (NovelCatalog.CatalogItem item : items) {
            if (item.isSelected)selected.add(item);
        }
        return selected;
    }

    public List<String> getAllSelectedLinks(){
        List<String> links = new ArrayList<>();
        for (NovelCatalog.CatalogItem item : items) {
            if (item.isSelected) links.add(item.Link);
        }
        return links;
    }

    public boolean isSelected(int pos){
        return items.get(pos).isSelected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.download_select_item, parent, false);
        view.setOnLongClickListener(longClickListener);
        view.setOnClickListener(clickListener);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.parentView.setBackground(createDrawable(DrawableType.NORMAL));
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NovelCatalog.CatalogItem item = items.get(position);
        holder.loadingView.setVisibility(View.INVISIBLE);
        if (item.isDownloaded){
            if(isCurrentItem(item))
                holder.parentView.setBackground(createDrawable(DrawableType.CURRENT_DOWNLOAD));
            else holder.parentView.setBackground(createDrawable(DrawableType.DOWNLOAD));
            holder.textView.setTextColor(textColorSelect);
            addOrRemoveProperty(holder.textView,RelativeLayout.ALIGN_PARENT_START,false);
            addOrRemoveProperty(holder.textView,RelativeLayout.CENTER_HORIZONTAL,true);
        }
        else if (item.isSelected) {
            if(isCurrentItem(item)){
                holder.parentView.setBackground(createDrawable(DrawableType.CURRENT_SELECTED));
            }
            else holder.parentView.setBackground(createDrawable(DrawableType.SELECTED));
            holder.textView.setTextColor(textColorSelect);
            addOrRemoveProperty(holder.textView,RelativeLayout.ALIGN_PARENT_START,false);
            addOrRemoveProperty(holder.textView,RelativeLayout.CENTER_HORIZONTAL,true);
        }
        else if (item.isDownloading){
            holder.parentView.setBackground(createDrawable(DrawableType.NORMAL));
            holder.textView.setTextColor(textColorNormal);
            holder.loadingView.setVisibility(View.VISIBLE);
            addOrRemoveProperty(holder.textView,RelativeLayout.CENTER_HORIZONTAL,false);
            addOrRemoveProperty(holder.textView,RelativeLayout.ALIGN_PARENT_START,true);
        }
        else {
            if(isCurrentItem(item)){
                holder.parentView.setBackground(createDrawable(DrawableType.CURRENT_NORMAL));
                holder.textView.setTextColor(context.getColor(R.color.DoderBlue));
            }
            else {
                holder.parentView.setBackground(createDrawable(DrawableType.NORMAL));
                holder.textView.setTextColor(textColorNormal);
            }
            addOrRemoveProperty(holder.textView,RelativeLayout.ALIGN_PARENT_START,false);
            addOrRemoveProperty(holder.textView,RelativeLayout.CENTER_HORIZONTAL,true);
        }
        holder.textView.setText(String.format("%04d",position+1));
    }

    private boolean isCurrentItem(NovelCatalog.CatalogItem item) {
        return item.Title.equals(currentItem.Title) && item.Link.equals(currentItem.Link);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void addOrRemoveProperty(View view, int property, boolean flag){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if(flag){
            layoutParams.addRule(property);
        }
        else {
            layoutParams.removeRule(property);
        }
        view.setLayoutParams(layoutParams);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private View parentView;
        private SpinKitView loadingView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.ds_item_id);
            parentView = itemView.findViewById(R.id.ds_item_parent);
            loadingView = itemView.findViewById(R.id.ds_item_downloading);
        }
    }
}

