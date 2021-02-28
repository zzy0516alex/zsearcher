package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.R;
import com.Z.NovelReader.myObjects.NovelChap;

import java.util.ArrayList;

public class NovelViewAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<NovelChap> mNovelChap;
    private int text_size = 20;
    public enum DNMod {
        NIGHT_MOD,
        DAY_MOD
    }
    private DNMod Mod;

    public NovelViewAdapter(Context c, ArrayList<NovelChap> n) {
        context = c;
        mNovelChap = n;
    }

    public void setTextSize(int size) {
        text_size = size;
        notifyDataSetChanged();
    }
    public void updateChapList(ArrayList<NovelChap>novelChaps){
        mNovelChap=novelChaps;
        notifyDataSetChanged();

    }
    public void setDNMod(DNMod mod){
        Mod=mod;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = View.inflate(context, R.layout.novel_text_item, null);
        return new ViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NovelChap chap = mNovelChap.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.title.setText(chap.getTitle());
        vh.content.setText(chap.getContent());
        vh.content.setTextSize(text_size);
        vh.title.setTextSize(text_size+6);
        switch(Mod){
            case NIGHT_MOD:
            {
                vh.title.setTextColor(context.getResources().getColor(R.color.night_text));
                vh.content.setTextColor(context.getResources().getColor(R.color.night_text));
            }
                break;
            case DAY_MOD:
            {
                vh.title.setTextColor(context.getResources().getColor(R.color.day_text));
                vh.content.setTextColor(context.getResources().getColor(R.color.day_text));
            }
                break;
            default:

        }
    }

    @Override
    public int getItemCount() {
        return mNovelChap.size();
    }

    private OnRecycleItemClickListener recycleItemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView content;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.novel_title);
            content = itemView.findViewById(R.id.novel_content);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //recycleItemClickListener.onClick(itemView);
                }
            });

        }


    }
    public void setRecycleItemClickListener(OnRecycleItemClickListener recycleItemClickListener) {
        this.recycleItemClickListener = recycleItemClickListener;
    }

    public interface OnRecycleItemClickListener {
        void onClick(View view);
    }
}
