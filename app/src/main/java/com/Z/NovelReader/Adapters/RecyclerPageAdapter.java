package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Fragments.NovelViewBasicFragment;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelContentPage;
import com.Z.NovelReader.Objects.beans.NovelPageWindow;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.PageSplit;

import java.util.ArrayList;

public class RecyclerPageAdapter extends RecyclerView.Adapter<RecyclerPageAdapter.PageViewHolder> {

    private ArrayList<NovelContentPage> pages;
    private Context context;
    private NovelPageWindow window;
    private PageListener pageListener;
    private int text_size = 20;
    private float line_space = 1.0f;
    private NovelViewBasicFragment.DNMod Mod;

    public RecyclerPageAdapter(Context context, NovelPageWindow window){
        this.window = window;
        this.context = context;
    }

    public void setPageListener(PageListener pageListener) {
        this.pageListener = pageListener;
    }

    public void setLine_space(float line_space) {
        this.line_space = line_space;
    }

    public void setText_size(int text_size) {
        this.text_size = text_size;
    }

    public void setDNMod(NovelViewBasicFragment.DNMod mod, boolean isNotify) {
        Mod = mod;
        if (isNotify)notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PageViewHolder pageViewHolder = new PageViewHolder(LayoutInflater.from(context).inflate(R.layout.novel_text_card, parent, false));
        return pageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        //Log.d("bind view","current view: "+position);
        //Log.d(name+"|bind view","total pages: "+chap.getPageNum());
        pages = window.getPages();
        NovelContentPage novelContentPage = pages.get(position);
        holder.content.setText(novelContentPage.getPage_content());
        //holder.tv_id.setText(String.valueOf(novelContentPage.getPage_id()));
        if (novelContentPage.isTempPage()) {
            Log.d("bind view","found temp page: "+position);
            holder.content.post(()->{
                ArrayList<NovelContentPage> page = PageSplit.getPage(novelContentPage, holder.content);
                if (window.isSingleWindow())pages = page;
                else {
                    int i = pages.indexOf(novelContentPage);
                    pages.addAll(i,page);
                    pages.remove(novelContentPage);
                }
                window.setPages(pages);
                //notifyDataSetChanged();
                if (pageListener!=null)pageListener.onPageSplitDone(window,page,novelContentPage.getBelong_to_chapID());
            });
        }

        holder.content.setTextSize(text_size);
        holder.title.setTextSize(text_size+6);
        holder.content.setLineSpacing(0,line_space);
        holder.title.setLineSpacing(0,line_space);
        holder.btn_refresh.setTag(R.id.NV_refresh_btn,novelContentPage.getBelong_to_chapID());
        holder.btn_refresh.setOnClickListener(view -> {
            int pos = (int) view.getTag(R.id.NV_refresh_btn);
            if (pageListener!=null)pageListener.onRefreshClick(pos);
        });

        switch(Mod){
            case NIGHT_MOD:
            {
                holder.title.setTextColor(context.getResources().getColor(R.color.night_text,null));
                holder.content.setTextColor(context.getResources().getColor(R.color.night_text,null));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.night_background,null));
            }
            break;
            case DAY_MOD:
            {
                holder.title.setTextColor(context.getResources().getColor(R.color.day_text));
                holder.content.setTextColor(context.getResources().getColor(R.color.day_text));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.comfortGreen,null));
            }
            break;
            default:

        }

        if (novelContentPage.isFirstPage()){
            ViewGroup.LayoutParams params = holder.rl_title_box.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.rl_title_box.setLayoutParams(params);
            holder.title.setText(novelContentPage.getTitle());
            holder.rl_title_box.setVisibility(View.VISIBLE);
        }else {
            ViewGroup.LayoutParams params = holder.rl_title_box.getLayoutParams();
            params.height = 0;
            holder.rl_title_box.setLayoutParams(params);
            holder.rl_title_box.setVisibility(View.INVISIBLE);
        }

        if (novelContentPage.isFirstPage() && novelContentPage.isErrorPage()){
            holder.content.setGravity(Gravity.CENTER);
            holder.btn_refresh.setVisibility(View.VISIBLE);
        }
        else {
            holder.content.setGravity(Gravity.NO_GRAVITY);
            holder.btn_refresh.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return window.getPageNum();
    }


    class PageViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mContainer;
        private TextView title;
        private RelativeLayout rl_title_box;
        private TextView content;
        private Button btn_refresh;
        private CardView cardView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.novel_title);
            rl_title_box = itemView.findViewById(R.id.title_box);
            content = itemView.findViewById(R.id.novel_content);
            btn_refresh = itemView.findViewById(R.id.novel_refresh);
            mContainer = itemView.findViewById(R.id.card_container);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    public interface PageListener{
        void onPageSplitDone(NovelPageWindow update_chap,ArrayList<NovelContentPage> new_pages, int chapID);
        void onRefreshClick(int chapID);
    }
}
