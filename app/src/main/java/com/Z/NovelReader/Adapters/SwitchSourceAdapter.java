package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.BitmapUtils;
import com.Z.NovelReader.Utils.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

import cn.refactor.library.SmoothCheckBox;

public class SwitchSourceAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BackupSourceBean> sources;

    public SwitchSourceAdapter(Context context, ArrayList<BackupSourceBean> sources) {
        this.context = context;
        this.sources = sources;
    }

    public void updateSources(ArrayList<BackupSourceBean> sources) {
        this.sources = sources;
        notifyDataSetChanged();
    }

    public ArrayList<BackupSourceBean> getSources() {
        return sources;
    }

    @Override
    public int getCount() {
        return sources.size();
    }

    @Override
    public Object getItem(int i) {
        return sources.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.switch_source_item, null);
            holder.im_coverBrief = view.findViewById(R.id.sws_book_cover);
            holder.tv_bookName = view.findViewById(R.id.sws_book_name);
            holder.tv_sourceName = view.findViewById(R.id.sws_book_source);
            holder.tv_catalogInfo = view.findViewById(R.id.sws_catalog_info);
            holder.cb_select = view.findViewById(R.id.select_source);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        BackupSourceBean backupSourceBean = sources.get(i);
        holder.cb_select.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked){
                backupSourceBean.setChosen(true);
                for (BackupSourceBean b :
                        sources) {
                    if (b!=backupSourceBean)b.setChosen(false);
                }
                notifyDataSetChanged();
            }
        });

        holder.cb_select.setChecked(backupSourceBean.isChosen());
        Bitmap bitmap = backupSourceBean.getCoverBrief();
        Bitmap cover = bitmap!=null?bitmap: BitmapFactory.decodeResource(
                context.getResources(),R.mipmap.no_book_cover);
        Bitmap cover_brief = BitmapUtils.setRadius(BitmapUtils.getResizedBitmap(cover,150,200),10);
        holder.im_coverBrief.setImageBitmap(cover_brief);
        String bookName = String.format("%s - %s", backupSourceBean.getCurrent_novel().getBookName(), backupSourceBean.getCurrent_novel().getWriter());
        holder.tv_bookName.setText(bookName);
        holder.tv_sourceName.setText(backupSourceBean.getSourceName());
        int catalog_length = backupSourceBean.getCatalog().getSize();
        String latestChapName = StringUtils.simplifyChapName(backupSourceBean.getCatalog().getTitleList().get(catalog_length-1));
        String catalogInfo = String.format(Locale.CHINA,"共%d章 最新章节: %s",catalog_length, latestChapName);
        holder.tv_catalogInfo.setText(catalogInfo);

        return view;
    }
    class ViewHolder {
        SmoothCheckBox cb_select;
        ImageView im_coverBrief;
        TextView tv_bookName;
        TextView tv_sourceName;
        TextView tv_catalogInfo;
    }
}
