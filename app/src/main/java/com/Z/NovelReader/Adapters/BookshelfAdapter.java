package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.BookShelfItem;
import com.Z.NovelReader.R;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.library.SmoothCheckBox;

public class BookshelfAdapter extends RecyclerView.Adapter<BookshelfAdapter.ViewHolder> {
    List<BookShelfItem> items;
    Context context;
    private View.OnLongClickListener longClickListener;
    private View.OnClickListener clickListener;
    private boolean selection_mode = false;
    //private int item_chosen =-1;
    //private ArrayList<Integer> item_select_list = new ArrayList<>();//被选中的书的索引

    public BookshelfAdapter(Context context, List<BookShelfItem> books) {
        this.items = books;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.selection_mode = isSelectionMode;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode(){return this.selection_mode;}

    public void setItems(List<BookShelfItem> books){
        this.items = books;
    }

    public void reverseSelection(int position){
        items.get(position).setSelected(!items.get(position).isSelected());
        notifyItemChanged(position);
    }

    public void select(int position, boolean selected) {
        if (items.get(position).isSelected() != selected) {
            items.get(position).setSelected(selected);
            notifyItemChanged(position);
        }
    }

    public BookShelfItem getItem(int position){
        return items.get(position);
    }

    public List<BookShelfItem> getAllSelectedItem(){
        List<BookShelfItem> selected = new ArrayList<>();
        for (BookShelfItem item : items) {
            if (item.isSelected())selected.add(item);
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_shelf_item, null);
        view.setOnLongClickListener(longClickListener);
        view.setOnClickListener(clickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookShelfItem item = getItem(position);
        holder.tv_name.setText(item.getBookName());
        String writer = item.getWriter();
        holder.tv_writer.setText((!writer.equals(""))?writer:"佚名");
        holder.im_cover.setImageBitmap(item.getBookCover());
        //确定mask绘制大小
        holder.im_cover.measure(0,0);
        int height = holder.im_cover.getMeasuredHeight();
        int width = holder.im_cover.getMeasuredWidth();
        float x = holder.im_cover.getX();
        float y = holder.im_cover.getY();
        //绘制mask，并隐藏
        ViewGroup.LayoutParams pp_suspend_mask =holder.suspend_mask.getLayoutParams();
        pp_suspend_mask.height = height;
        pp_suspend_mask.width = width;
        holder.suspend_mask.setLayoutParams(pp_suspend_mask);
        holder.suspend_mask.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams pp_disable_mask =holder.disable_mask.getLayoutParams();
        pp_disable_mask.height = height;
        pp_disable_mask.width = width;
        holder.disable_mask.setLayoutParams(pp_disable_mask);
        holder.disable_mask.setX(x);
        holder.disable_mask.setY(y);
        holder.disable_mask.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams pp_selection_mask =holder.selection_mask.getLayoutParams();
        pp_selection_mask.height = height;
        pp_selection_mask.width = width;
        holder.selection_mask.setLayoutParams(pp_selection_mask);
        holder.selection_mask.setVisibility(View.INVISIBLE);

        //if(!selection_mode)item.setSelected(false);
        //按优先级显示mask
        if(item.isSpoiled())holder.disable_mask.setVisibility(View.VISIBLE);
        else if(item.isRecovering())holder.suspend_mask.setVisibility(View.VISIBLE);
        else if(selection_mode){
            holder.selection_mask.setVisibility(View.VISIBLE);
            if (item.isSelected())holder.cb_selection.setChecked(true);
            else holder.cb_selection.setChecked(false);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_writer;
        ImageView im_cover;

        RelativeLayout suspend_mask;
        RelativeLayout disable_mask;
        RelativeLayout selection_mask;
        SmoothCheckBox cb_selection;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.BookName);
            tv_writer = itemView.findViewById(R.id.Writer);
            im_cover = itemView.findViewById(R.id.BookCover);
            //修复中
            suspend_mask = itemView.findViewById(R.id.suspend_mask);
            //已失效
            disable_mask = itemView.findViewById(R.id.disable_mask);
            //选中
            selection_mask = itemView.findViewById(R.id.select_mask);
            cb_selection = itemView.findViewById(R.id.select_book);
            cb_selection.setClickable(false);
        }
    }
}
