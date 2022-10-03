package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Fragments.NovelViewBasicFragment;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Objects.NovelChap;

import java.util.ArrayList;

public class NovelViewAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<NovelChap> mNovelChap;
    private ViewCallbackListener viewCallbackListener;
    private int text_size = 20;
    private float line_space = 1.0f;
//    public enum DNMod {
//        NIGHT_MOD,
//        DAY_MOD
//    }
    private NovelViewBasicFragment.DNMod Mod;

    public NovelViewAdapter(Context c, ArrayList<NovelChap> n) {
        context = c;
        mNovelChap = n;
    }

    public void setViewCallbackListener(ViewCallbackListener viewCallbackListener) {
        this.viewCallbackListener = viewCallbackListener;
    }

    public void setTextSize(int size) {
        text_size = size;
        notifyDataSetChanged();
    }

    public void setLine_space(float line_space) {
        this.line_space = line_space;
        notifyDataSetChanged();
    }

    public void updateChapList(ArrayList<NovelChap>novelChaps){
        mNovelChap=novelChaps;
        notifyDataSetChanged();

    }
    public void setDNMod(NovelViewBasicFragment.DNMod mod){
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
        vh.content.setLineSpacing(0,line_space);
        vh.title.setLineSpacing(0,line_space);
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

        vh.btn_refresh.setTag(R.id.NV_refresh_btn,position);
        vh.btn_refresh.setOnClickListener(view -> {
            int pos = (int) view.getTag(R.id.NV_refresh_btn);
            NovelChap novelChap = mNovelChap.get(pos);
            viewCallbackListener.onRefreshClick(novelChap);
        });

        if (chap.isOnError()){
            vh.content.setGravity(Gravity.CENTER);
            vh.btn_refresh.setVisibility(View.VISIBLE);
        }
        else {
            vh.content.setGravity(Gravity.NO_GRAVITY);
            vh.btn_refresh.setVisibility(View.INVISIBLE);
        }

        vh.itemView.post(()->{
            int top = vh.itemView.getTop();
            int bottom = vh.itemView.getBottom();
            viewCallbackListener.onViewInitReady(vh.itemView);
        });
    }

    @Override
    public int getItemCount() {
        return mNovelChap.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView content;
        private Button btn_refresh;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.novel_title);
            content = itemView.findViewById(R.id.novel_content);
            btn_refresh = itemView.findViewById(R.id.novel_refresh);

        }


    }

    public interface ViewCallbackListener {
        void onRefreshClick(NovelChap chap);
        void onViewInitReady(View item_view);
    }
}
