package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Z.NovelReader.R;

import java.util.List;

public class CatalogListAdapter extends BaseAdapter {
    private  List<String>Novels;
    private Context context=null;
    private boolean canPaint=false;
    private String item_to_paint;
    private int text_size=20;
    private boolean isDarkTheme;
    public CatalogListAdapter(List<String> novels, Context context, boolean canPaint, String item_to_paint) {
        this.Novels=novels;
        this.context=context;
        this.canPaint=canPaint;
        this.item_to_paint=item_to_paint;
    }
    public Context getContext(){
        return context;
    }

    public void setItem_to_paint(String item_to_paint) {
        this.item_to_paint = item_to_paint;
        notifyDataSetChanged();
    }

    public void setDarkTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
    }

    public void updateList(List<String>items){
        Novels=items;
        notifyDataSetChanged();
    }

    public void setText_size(int text_size) {
        this.text_size = text_size;
    }

    @Override
    public int getCount() {
        return Novels.size();
    }

    @Override
    public Object getItem(int position) {
        return Novels.get(position);
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
            Layout= (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.listitem,null);
        }
        TextView title=Layout.findViewById(R.id.N_name);
        if(getItem(position).toString().equals(item_to_paint) && canPaint){
            title.setTextColor(Color.parseColor("#1E90FF"));
        }
        else title.setTextColor(isDarkTheme?Color.parseColor("#FFC8C8C8"):Color.parseColor("#000000"));
        String Novelname= (String) getItem(position);
        title.setTextSize(text_size);
        title.setText(Novelname);
        return Layout;
    }

}
