package com.example.helloworld.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helloworld.R;
import com.example.helloworld.myObjects.NovelChap;

import java.util.ArrayList;

public class NovelViewAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<NovelChap> mNovelChap;
    private int text_size=20;
    public NovelViewAdapter(Context c,ArrayList<NovelChap> n) {
        context=c;
        mNovelChap =n;
    }
    public void setTextSize(int size){
        text_size=size;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview =View.inflate(context, R.layout.novel_text_item,null);
        return new ViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NovelChap chap= mNovelChap.get(position);
        ViewHolder vh= (ViewHolder) holder;
        vh.title.setText(chap.getTitle());
        vh.content.setText(chap.getContent());
        vh.content.setTextSize(text_size);

    }

    @Override
    public int getItemCount() {
        return mNovelChap.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView content;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.novel_title);
            content=itemView.findViewById(R.id.novel_content);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }

    }
}
