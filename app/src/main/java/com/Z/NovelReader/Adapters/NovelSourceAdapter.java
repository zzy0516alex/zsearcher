package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Z.NovelReader.R;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.suke.widget.SwitchButton;

import java.util.List;

public class NovelSourceAdapter extends BaseAdapter {

    private List<NovelRequire> novelRequireList;
    private Context context;
    private SourceViewClickListener listener;
    private boolean delete_mode=false;

    public NovelSourceAdapter(List<NovelRequire> novelRequires, Context context) {
        this.novelRequireList=novelRequires;
        this.context=context;
    }

    public void setViewClickListener(SourceViewClickListener listener) {
        this.listener = listener;
    }

    public void setNovelRequireList(List<NovelRequire> novelRequireList) {
        this.novelRequireList = novelRequireList;
    }

    public void setDelete_mode(boolean delete_mode) {
        this.delete_mode = delete_mode;
        notifyDataSetChanged();
    }

    public boolean isDelete_mode() {
        return delete_mode;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getCount() {
        return novelRequireList==null ? 0 : novelRequireList.size();
    }

    @Override
    public Object getItem(int position) {
        return novelRequireList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if (convertView==null){
            convertView = (RelativeLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.novel_source_item,null);
            viewHolder = new ViewHolder();
            viewHolder.imb_moreINFO=convertView.findViewById(R.id.more_info);
            viewHolder.tv_novelSourceName=convertView.findViewById(R.id.novel_source_name);
            viewHolder.swb_enableControl=convertView.findViewById(R.id.novel_source_enable);
            viewHolder.imb_sourceDelete=convertView.findViewById(R.id.novel_source_delete);
            convertView.setTag(viewHolder);
        }else viewHolder= (ViewHolder) convertView.getTag();
        //visibility
        if (delete_mode){
            viewHolder.swb_enableControl.setVisibility(View.INVISIBLE);
            viewHolder.imb_sourceDelete.setVisibility(View.VISIBLE);
        }else {
            viewHolder.swb_enableControl.setVisibility(View.VISIBLE);
            viewHolder.imb_sourceDelete.setVisibility(View.INVISIBLE);
        }

        //set listener
        NovelRequire currentSource = (NovelRequire) getItem(position);

        viewHolder.imb_moreINFO.setTag(R.id.NS_info_btn,position);
        viewHolder.imb_moreINFO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.NS_info_btn);
                Log.d("novel source adapter","info pos="+pos);
                NovelRequire currentSource = (NovelRequire) getItem(pos);
                listener.onInfoClick(currentSource);
            }
        });

        viewHolder.swb_enableControl.setTag(R.id.NS_enable_btn,position);
        viewHolder.swb_enableControl.setChecked(currentSource.isEnabled());
        viewHolder.swb_enableControl.setOnCheckedChangeListener(
                new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                int pos = (int) view.getTag(R.id.NS_enable_btn);
                Log.d("novel source adapter","enable pos="+pos);
                NovelRequire currentSource = (NovelRequire) getItem(pos);
                listener.onSwitchClick(currentSource,isChecked);
            }
        });

        viewHolder.imb_sourceDelete.setTag(R.id.NS_delete_btn,position);
        viewHolder.imb_sourceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.NS_delete_btn);
                Log.d("novel source adapter","delete pos="+pos);
                NovelRequire currentSource = (NovelRequire) getItem(pos);
                listener.onSourceDelete(currentSource);
            }
        });

        viewHolder.tv_novelSourceName.setText(currentSource.getBookSourceName());

        return convertView;
    }


    public static class ViewHolder{
        ImageButton imb_moreINFO;
        TextView tv_novelSourceName;
        SwitchButton swb_enableControl;
        ImageButton imb_sourceDelete;
    }
    public interface SourceViewClickListener{
        void onInfoClick(NovelRequire currentSource);
        void onSwitchClick(NovelRequire currentSource,boolean isEnabled);
        void onSourceDelete(NovelRequire currentSource);
    }
}
