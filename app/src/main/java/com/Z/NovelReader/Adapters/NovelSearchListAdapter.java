package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.R;

import java.util.ArrayList;

public class NovelSearchListAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<NovelSearchBean> mSearchList;
    private OnItemClickListener onItemClickListener;

    public NovelSearchListAdapter(Context c, ArrayList<NovelSearchBean> beans) {
        context = c;
        this.mSearchList = beans;
    }

    public void setData(ArrayList<NovelSearchBean> beans){
        this.mSearchList = beans;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.novel_search_list_item, parent,false);
        return new ViewHolder(item_view,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NovelSearchBean searchBean = mSearchList.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.tv_bookName.setText(searchBean.getBookNameWithoutWriter());
        String writer = searchBean.getWriter();
        vh.tv_writer.setText(writer.equals("")?"未知":writer);
        String bookSourceName = searchBean.getNovelRule().getBookSourceName();
        vh.tv_source.setText(bookSourceName.equals("")?"未知":bookSourceName);
    }

    @Override
    public int getItemCount() {
        return mSearchList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_bookName;
        private TextView tv_writer;
        private TextView tv_source;

        private OnItemClickListener listener;

        public ViewHolder(@NonNull final View itemView,OnItemClickListener clickListener) {
            super(itemView);
            tv_bookName = itemView.findViewById(R.id.search_name);
            tv_writer = itemView.findViewById(R.id.search_writer);
            tv_source = itemView.findViewById(R.id.search_source);
            this.listener = clickListener;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            listener.onItemClick(view,getAdapterPosition());
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
}
