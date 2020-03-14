package com.example.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultBoxAdapter extends BaseAdapter {

    private ArrayList<String>title_list;
    private ArrayList<String>size_list;
    private ArrayList<String>type_list;
    private Context context=null;

    public ResultBoxAdapter(ArrayList<String> title_list, ArrayList<String> size_list, ArrayList<String> type_list, Context context) {
        this.title_list = title_list;
        this.size_list = size_list;
        this.type_list = type_list;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    @Override
    public int getCount() {
        return title_list.size();
    }

    @Override
    public Object getItem(int position) {
        return title_list.get(position);
    }
    public String getType(int position){
        return type_list.get(position);
    }
    public String getSize(int position){
        return size_list.get(position);
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
            Layout= (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.listitem_for_bt,null);
        }
        TextView title=Layout.findViewById(R.id.BTtitle);
        TextView size=Layout.findViewById(R.id.filesize);
        ImageView type=Layout.findViewById(R.id.filetype);
        String Title= (String) getItem(position);
        String Size=getSize(position);

        return Layout;
    }
}
