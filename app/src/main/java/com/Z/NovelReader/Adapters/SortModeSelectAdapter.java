package com.Z.NovelReader.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.BitmapUtils;

import java.util.ArrayList;

public class SortModeSelectAdapter extends RecyclerView.Adapter<SortModeSelectAdapter.ViewHolder> {

    private Context context;
    private OnItemClickListener onItemClickListener;
    private ArrayList<String> sortModeList;

    private int selectedMode = -1;
    private boolean isReversed = false;

    public SortModeSelectAdapter(Context context, ArrayList<String> sortModeList) {
        this.context = context;
        this.sortModeList = sortModeList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setSelectedMode(int selectedMode) {
        this.selectedMode = (selectedMode-1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sort_type_item, parent,false);
        return new ViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String mode_name = sortModeList.get(position);
        holder.tv_sort_type_name.setText(mode_name);
        holder.itemView.setOnClickListener(v -> {
            if (selectedMode == holder.getAdapterPosition())
                isReversed = !isReversed;
            else {
                selectedMode = holder.getAdapterPosition();
                isReversed = false;
            }
            //holder.changeSortSequence(true,isReversed);
            notifyDataSetChanged();
            if (onItemClickListener!=null)onItemClickListener.onItemClick(selectedMode,isReversed);
        });
        holder.changeSortSequence((selectedMode == position),isReversed);
    }

    @Override
    public int getItemCount() {
        return sortModeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rl_background;
        private TextView tv_sort_type_name;
        private ImageView im_tri_up;//正三角
        private ImageView im_tri_down;//倒三角

        private boolean isSelected = false;
        private boolean isReversed = false;
        private OnItemClickListener listener;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            rl_background = itemView.findViewById(R.id.SMS_background);
            tv_sort_type_name = itemView.findViewById(R.id.SMS_type_name);
            im_tri_up = itemView.findViewById(R.id.SMS_tri_up);
            im_tri_down = itemView.findViewById(R.id.SMS_tri_down);
//            this.listener = clickListener;
//            itemView.setOnClickListener(this);
        }

        public void changeSortSequence(boolean isSelected, boolean isReversed){
            Bitmap tri_up = BitmapFactory.decodeResource(context.getResources(), R.mipmap.up_arrow_icon).copy(Bitmap.Config.ARGB_8888, true);
            Bitmap tri_down = BitmapFactory.decodeResource(context.getResources(), R.mipmap.down_arrow_icon).copy(Bitmap.Config.ARGB_8888, true);
            if (isSelected){
                rl_background.setBackground(ContextCompat.getDrawable(context,R.drawable.popwindow_blue));
                tv_sort_type_name.setTextColor(context.getColor(R.color.SelectorBlue));
                if (isReversed){
                    Bitmap bm_down = BitmapUtils.changeColor(tri_down, context.getColor(R.color.SelectorBlue));
                    im_tri_down.setImageBitmap(bm_down);
                    Bitmap bm_up = BitmapUtils.changeColor(tri_up, context.getColor(R.color.btn_click_gray));
                    im_tri_up.setImageBitmap(bm_up);
                }else {
                    Bitmap bm_down = BitmapUtils.changeColor(tri_down, context.getColor(R.color.btn_click_gray));
                    im_tri_down.setImageBitmap(bm_down);
                    Bitmap bm_up = BitmapUtils.changeColor(tri_up, context.getColor(R.color.SelectorBlue));
                    im_tri_up.setImageBitmap(bm_up);
                }
            }else {
                rl_background.setBackground(ContextCompat.getDrawable(context,R.drawable.popwindow_white));
                tv_sort_type_name.setTextColor(context.getColor(R.color.system_gray));
                Bitmap bm_down = BitmapUtils.changeColor(tri_down, context.getColor(R.color.btn_click_gray));
                im_tri_down.setImageBitmap(bm_down);
                Bitmap bm_up = BitmapUtils.changeColor(tri_up, context.getColor(R.color.btn_click_gray));
                im_tri_up.setImageBitmap(bm_up);
            }
        }

//        @Override
//        public void onClick(View view) {
//            if (isSelected){
//                isReversed = !isReversed;
//            }else isSelected = true;
//            listener.onItemClick(view,getAdapterPosition());
//        }
    }

    public interface OnItemClickListener{
        /**
         * item点击事件
         * @param isReversed 是否需要倒转列表
         * @param position 注意返回-1表示未准备完毕
         */
        void onItemClick(int position,boolean isReversed);
    }
}
