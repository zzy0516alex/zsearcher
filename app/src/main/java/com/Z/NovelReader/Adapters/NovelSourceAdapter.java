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

import androidx.lifecycle.ViewModelProvider;

import com.Z.NovelReader.R;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.suke.widget.SwitchButton;

import java.util.List;

public class NovelSourceAdapter extends BaseAdapter {

    private List<NovelRequire> novelRequireList;
    private Context context;
    private SourceViewClickListener listener;

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
            convertView.setTag(viewHolder);
        }
        viewHolder= (ViewHolder) convertView.getTag();
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

        viewHolder.tv_novelSourceName.setText(currentSource.getBookSourceName());

        return convertView;
    }


    public static class ViewHolder{
        ImageButton imb_moreINFO;
        TextView tv_novelSourceName;
        SwitchButton swb_enableControl;
    }
    public interface SourceViewClickListener{
        void onInfoClick(NovelRequire currentSource);
        void onSwitchClick(NovelRequire currentSource,boolean isEnabled);
    }
}
