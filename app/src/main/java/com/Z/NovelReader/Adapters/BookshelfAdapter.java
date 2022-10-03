package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.R;

import java.util.ArrayList;
import java.util.List;

public class BookshelfAdapter extends BaseAdapter {
    List<Bitmap> BookCovers;
    List<Novels> Books;
    Context context=null;
    //private int item_chosen =-1;
    private ArrayList<Integer> item_select_list = new ArrayList<>();//被选中的书的索引

    public BookshelfAdapter(List<Bitmap> bookCovers, List<Novels> novels, Context context) {
        BookCovers = bookCovers;
        Books= novels;
        this.context = context;
    }

    public void setBooks(List<Novels> books) {
        Books = books;
    }

    public void setBookCovers(List<Bitmap> bookCovers) {
        BookCovers = bookCovers;
    }

    public Context getContext() {
        return context;
    }

    public void updateSelectItems(List<Integer> items) {
        this.item_select_list = (ArrayList<Integer>) items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return Books.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    @Override
    public Object getItem(int position) {
        return Books.get(position);
    }
    public Object getImage(int position){
        return BookCovers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout Layout=null;
        if(convertView!=null) {
            Layout= (RelativeLayout) convertView;
        }
        else {
            Layout= (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.book_shelf_item,null);
        }
        Novels current_book = (Novels) getItem(position);
        TextView tv_name=Layout.findViewById(R.id.BookName);
        TextView tv_writer=Layout.findViewById(R.id.Writer);
        ImageView im_cover=Layout.findViewById(R.id.BookCover);
        RelativeLayout background=Layout.findViewById(R.id.RL);
        Bitmap bookCover= (Bitmap) getImage(position);
        tv_name.setText(current_book.getBookName());
        String writer = current_book.getWriter();
        tv_writer.setText((!writer.equals(""))?writer:"佚名");
        im_cover.setImageBitmap(bookCover);

        //绘制mask
        RelativeLayout suspend_mask = Layout.findViewById(R.id.suspend_mask);
        background.measure(0,0);
        ViewGroup.LayoutParams pp =suspend_mask.getLayoutParams();
        pp.height =background.getMeasuredHeight();
        suspend_mask.setLayoutParams(pp);
        suspend_mask.setVisibility(View.INVISIBLE);
        RelativeLayout disable_mask = Layout.findViewById(R.id.disable_mask);
        ViewGroup.LayoutParams pp1 =disable_mask.getLayoutParams();
        pp1.height =background.getMeasuredHeight();
        disable_mask.setLayoutParams(pp1);
        disable_mask.setVisibility(View.INVISIBLE);

        //dynamic
        if (current_book.isRecover())
            suspend_mask.setVisibility(View.VISIBLE);
        else suspend_mask.setVisibility(View.INVISIBLE);
        if (current_book.isSpoiled())
            disable_mask.setVisibility(View.VISIBLE);
        else disable_mask.setVisibility(View.INVISIBLE);
        if(item_select_list.contains(position)){
            background.setBackgroundColor(Color.parseColor("#87CEFA"));
        }
        else{
            background.setBackgroundResource(0);
        }
        return Layout;
    }
}
