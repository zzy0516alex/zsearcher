package com.Z.NovelReader.views.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.Z.NovelReader.R;
import com.Z.NovelReader.Utils.ScreenUtils;

import java.util.ArrayList;

public class BottomSheetDialog extends Dialog {
    private LinearLayout ll_items;
    private LinearLayout sheet_container;
    private ArrayList<SheetItem> items;

    public BottomSheetDialog(@NonNull Context context) {
        this(context,0);
    }

    public BottomSheetDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        items = new ArrayList<>();
    }

    public interface SheetItemClickListener{
        void onSheetItemClick();
    }

    public static class SheetItem{
        public String item_title;
        public int icon_res_id;
        public SheetItemClickListener listener;

        public SheetItem(@NonNull String item_title, int icon_res_id, SheetItemClickListener listener) {
            this.item_title = item_title;
            this.icon_res_id = icon_res_id;
            this.listener = listener;
        }
    }

    public void addSheetItem(String item_title, int icon_id, SheetItemClickListener listener){
        this.items.add(new SheetItem(item_title,icon_id,listener));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置背景透明
        setContentView(R.layout.dialog_bottom_sheet);

        final ScrollView scrollLayout = (ScrollView) findViewById(R.id.scroll_layout);
        ll_items = (LinearLayout) findViewById(R.id.llay_other);
        sheet_container = findViewById(R.id.sheet_container);
        //取消按钮
        TextView txtCancel = (TextView) findViewById(R.id.txt_cancel);
        StateListDrawable background = getCorneredSelector(15,15,15,15);
        txtCancel.setBackground(background);
        txtCancel.setOnClickListener(v -> {
                this.dismiss();
        });
        //其他按钮
        for (int i = 0; i < items.size(); i++) {
            SheetItem current_item = items.get(i);

            RelativeLayout rl_item = new RelativeLayout(getContext());
            LinearLayout ll_item = new LinearLayout(getContext());
            ll_item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ll_item.setOrientation(LinearLayout.HORIZONTAL);
            ll_item.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            StateListDrawable corneredSelector;
            if(i == 0)
                corneredSelector = getCorneredSelector(15, 15, 0, 0);
            else if (i == items.size()-1)
                corneredSelector = getCorneredSelector(0, 0, 15, 15);
            else
                corneredSelector = getCorneredSelector(0, 0, 0, 0);
            ll_item.setBackground(corneredSelector);
            int padding = (int) (10 * getContext().getResources().getDisplayMetrics().density + 0.5f);//10dp的padding转换成px
            ll_item.setPadding(0, padding, 0, padding);
            TextView textView = new TextView(getContext());
            textView.setText(current_item.item_title);
            textView.setTextSize(20);
            textView.setTextColor(0xFF000000);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(20,0,0,0);
            ImageView icon = new ImageView(getContext());
            icon.setImageResource(current_item.icon_res_id);
            ll_item.addView(icon);
            ll_item.addView(textView);
            ll_item.setOnClickListener(v -> {
                if (this.isShowing())this.dismiss();
                current_item.listener.onSheetItemClick();
            });
            View view = new View(getContext());
            view.setBackgroundColor(0xFFe6e6e6);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.setMargins(15,0,15,0);
            view.setLayoutParams(layoutParams);
            rl_item.addView(ll_item);
            if(i != items.size()-1)rl_item.addView(view);
            ll_items.addView(rl_item);
        }

        /**
         * 设置一定条数，不能再撑开，而是变成滑动
         */
        scrollLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int minNumWhenScroll = 5;//最小可滑动条数
                int childViewCount = ll_items.getChildCount();
                int scrollLayoutHeight = 0;
                int childHeight = 0;
                if (childViewCount == 0) {
                    scrollLayoutHeight = 0;
                } else {
                    childHeight = ll_items.getChildAt(0).getHeight();
                    if (childViewCount <= minNumWhenScroll) {
                        scrollLayoutHeight = childHeight * childViewCount;
                    } else {
                        scrollLayoutHeight = childHeight * minNumWhenScroll;
                    }
                }
                scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollLayoutHeight));
                scrollLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        Window window = this.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        int screenHeight = ScreenUtils.getScreenHeight(getContext());
        sheet_container.measure(0,0);
        int matchParent = WindowManager.LayoutParams.MATCH_PARENT;//父布局的宽度
        int wrapContent = WindowManager.LayoutParams.WRAP_CONTENT;//父布局的宽度
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = matchParent;
        lp.height = wrapContent;
        window.setAttributes(lp);
    }

    private GradientDrawable getCorneredBackground(float radius_top_left, float radius_top_right, float radius_bottom_right, float radius_bottom_left) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        float[] radii = new float[]{
                radius_top_left, radius_top_left,
                radius_top_right, radius_top_right,
                radius_bottom_right, radius_bottom_right,
                radius_bottom_left, radius_bottom_left
        };
        background.setCornerRadii(radii);
        return background;
    }

    private StateListDrawable getCorneredSelector(float radius_top_left, float radius_top_right, float radius_bottom_right, float radius_bottom_left){
        GradientDrawable pressed = getCorneredBackground(radius_top_left, radius_top_right, radius_bottom_right, radius_bottom_left);
        pressed.setColor(0xFFe6e6e6);
        GradientDrawable normal = getCorneredBackground(radius_top_left, radius_top_right, radius_bottom_right, radius_bottom_left);
        normal.setColor(0xFFFFFFFF);
        StateListDrawable background = new StateListDrawable();
        int state_pressed =  android.R.attr.state_pressed;
        background.addState(new int[]{state_pressed}, pressed);
        background.addState(new int[]{-state_pressed}, normal);
        return background;
    }
}
