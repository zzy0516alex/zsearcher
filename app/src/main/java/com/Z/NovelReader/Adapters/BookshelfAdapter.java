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

import com.Z.NovelReader.R;

import java.util.List;

public class BookshelfAdapter extends BaseAdapter {
    List<Bitmap> BookCovers;
    List<String> BookNames;
    Context context=null;
    private int item_chosen =-1;

    public BookshelfAdapter(List<Bitmap> bookCovers, List<String> bookNames, Context context) {
        BookCovers = bookCovers;
        BookNames = bookNames;
        this.context = context;
    }

    public void setBookNames(List<String> bookNames) {
        BookNames = bookNames;
    }

    public void setBookCovers(List<Bitmap> bookCovers) {
        BookCovers = bookCovers;
    }

    public Context getContext() {
        return context;
    }

    public void setItem_chosen(int item_chosen) {
        this.item_chosen = item_chosen;
    }

    @Override
    public int getCount() {
        return BookNames.size();
    }

    @Override
    public Object getItem(int position) {
        return BookNames.get(position);
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
            Layout= (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.griditem,null);
        }
        TextView name=Layout.findViewById(R.id.BookName);
        ImageView img=Layout.findViewById(R.id.BookCover);
        RelativeLayout background=Layout.findViewById(R.id.RL);
        String bookName= (String) getItem(position);
        Bitmap bookCover= (Bitmap) getImage(position);
        name.setText(bookName);
        img.setImageBitmap(bookCover);
        if(item_chosen==position){
            background.setBackgroundColor(Color.parseColor("#87CEFA"));
        }
        else{
            background.setBackgroundResource(0);
        }
        return Layout;
    }
}
