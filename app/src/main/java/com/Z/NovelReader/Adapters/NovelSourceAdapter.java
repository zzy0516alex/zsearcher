package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.R;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.views.FlexibleRectDrawable;
import com.google.android.flexbox.FlexboxLayout;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;
import java.util.Map;

public class NovelSourceAdapter extends RecyclerView.Adapter<NovelSourceAdapter.ViewHolder> {

    private List<NovelRequire> novelRequireList;
    private Map<Integer,Integer> time_response_map;
    private Context context;
    private SourceViewClickListener listener;
    private boolean delete_mode=false;
    private boolean show_time = false;

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

    public void setTimeResponseList(Map<Integer, Integer> time_response_list) {
        this.time_response_map = time_response_list;
        this.show_time = true;
        notifyDataSetChanged();
    }

    private TextView createTagTextView(MainAnalyzer.RuleType ruleType) {
        String tag_name = "";
        int tag_color = 0;
        switch(ruleType){
            case Jsoup:
                tag_name = "Jsoup";
                tag_color = ContextCompat.getColor(context, R.color.BlueViolet);
                break;
            case JsoupCss:
                tag_name = "Css";
                tag_color = ContextCompat.getColor(context, R.color.Olive);
                break;
            case Json:
                tag_name = "Json";
                tag_color = ContextCompat.getColor(context, R.color.MediumBlue);
                break;
            case JavaScript:
                tag_name = "JS";
                tag_color = ContextCompat.getColor(context, R.color.FireBrick);
                break;
            case Regex:
                tag_name = "Regex";
                tag_color = ContextCompat.getColor(context, R.color.DarkCyan);
                break;
            case XPath:
                tag_name = "XPath";
                tag_color = ContextCompat.getColor(context, R.color.Peru);
                break;
            default:
        }
        TextView tagTextView = new TextView(context);
        tagTextView.setText(tag_name);
        tagTextView.setTextColor(tag_color);
        tagTextView.setTextSize(12);
        tagTextView.setPadding(5,2,5,2);

        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 12, 0);
        tagTextView.setLayoutParams(layoutParams);

        // 获取可绘制的背景资源
        FlexibleRectDrawable backgroundDrawable = FlexibleRectDrawable.Builder.create()
                .setStroke(1.5f,tag_color)
                .setCorners(8,FlexibleRectDrawable.CORNER_ALL)
                .build();
        tagTextView.setBackground(backgroundDrawable);

        // 添加其他样式设置，例如边距、字体大小等
        return tagTextView;
    }

    public Context getContext() {
        return context;
    }

    public NovelRequire getItem(int position) {
        return novelRequireList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //在此函数中主要反射item view，以及定义点击回调函数
        View view = LayoutInflater.from(context).inflate(R.layout.novel_source_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (delete_mode){
            holder.swb_enableControl.setVisibility(View.INVISIBLE);
            holder.imb_sourceDelete.setVisibility(View.VISIBLE);
        }else {
            holder.swb_enableControl.setVisibility(View.VISIBLE);
            holder.imb_sourceDelete.setVisibility(View.INVISIBLE);
        }

        //set listener
        NovelRequire currentSource = getItem(position);

        holder.imb_moreINFO.setTag(R.id.NS_info_btn, currentSource.getId());//使用position会导致删除时定位错误
        holder.imb_moreINFO.setOnClickListener(v -> {
            int id = (int) v.getTag(R.id.NS_info_btn);
            Log.d("novel source adapter","info ID="+id);
            listener.onInfoClick(id);
        });

        holder.swb_enableControl.setTag(R.id.NS_enable_btn, currentSource.getId());
        holder.swb_enableControl.setOnCheckedChangeListener(null);
        holder.swb_enableControl.setCheckedImmediatelyNoEvent(currentSource.isEnabled());
        holder.swb_enableControl.setOnCheckedChangeListener(
                (view, isChecked) -> {
                    int id = (int) view.getTag(R.id.NS_enable_btn);
                    Log.d("novel source adapter",isChecked?"enable":"disable"+" ID ="+id);
                    listener.onSwitchClick(id, isChecked);
                });

        holder.imb_sourceDelete.setTag(R.id.NS_delete_btn, currentSource.getId());
        holder.imb_sourceDelete.setOnClickListener(v -> {
            int id = (int) v.getTag(R.id.NS_delete_btn);
            Log.d("novel source adapter","delete ID="+id);
            listener.onSourceDelete(id);
        });

        holder.tv_novelSourceName.setText(currentSource.getBookSourceName());

        if(show_time && time_response_map!=null){
            int time = time_response_map.get(currentSource.getId());
            String time_s = String.format(context.getResources().getString(R.string.novel_source_respond_time),time);
            holder.tv_timeResponse.setText(time_s);
            int color;
            if(time<500) color = context.getResources().getColor(R.color.deep_rgy_g,null);
            else if(time<2000) color = context.getResources().getColor(R.color.deep_rgy_y,null);
            else color = context.getResources().getColor(R.color.deep_rgy_r,null);
            holder.tv_timeResponse.setTextColor(color);
        }

        currentSource.judgeAvailableTypes();
        holder.fbl_tagContainer.removeAllViews();  // 清除之前的标签
        for (MainAnalyzer.RuleType type : currentSource.getAvailableTypes()) {
            holder.fbl_tagContainer.addView(createTagTextView(type));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return novelRequireList==null ? 0 : novelRequireList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageButton imb_moreINFO;
        TextView tv_novelSourceName;
        SwitchButton swb_enableControl;
        ImageButton imb_sourceDelete;
        FlexboxLayout fbl_tagContainer;
        TextView tv_timeResponse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imb_moreINFO = itemView.findViewById(R.id.more_info);
            this.tv_novelSourceName = itemView.findViewById(R.id.novel_source_name);
            this.swb_enableControl = itemView.findViewById(R.id.novel_source_enable);
            this.imb_sourceDelete = itemView.findViewById(R.id.novel_source_delete);
            this.fbl_tagContainer = itemView.findViewById(R.id.novel_source_tag);
            this.tv_timeResponse = itemView.findViewById(R.id.novel_source_res_time);
        }
    }
    public interface SourceViewClickListener{
        void onInfoClick(int sourceID);
        void onSwitchClick(int sourceID, boolean isEnabled);
        void onSourceDelete(int sourceID);
    }

    public static class NovelSourceItemDiff extends DiffUtil.Callback{

        private List<NovelRequire> old_list, new_list;

        public NovelSourceItemDiff(List<NovelRequire> old_list, List<NovelRequire> new_list) {
            this.old_list = old_list;
            this.new_list = new_list;
        }

        @Override
        public int getOldListSize() {
            return old_list.size();
        }

        @Override
        public int getNewListSize() {
            return new_list.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            NovelRequire old_item = this.old_list.get(oldItemPosition);
            NovelRequire new_item = this.new_list.get(newItemPosition);
            if(old_item ==null || new_item ==null)
                return false;
            return old_item.getId()==new_item.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NovelRequire old_item = this.old_list.get(oldItemPosition);
            NovelRequire new_item = this.new_list.get(newItemPosition);
            if(old_item ==null || new_item ==null)
                return false;
            return old_item.getBookSourceName().equals(new_item.getBookSourceName()) &&
                    old_item.isEnabled()==new_item.isEnabled();
        }
    }
}
