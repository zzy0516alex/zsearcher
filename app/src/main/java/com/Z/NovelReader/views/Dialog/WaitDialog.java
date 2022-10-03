package com.Z.NovelReader.views.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.Z.NovelReader.R;
import com.github.ybq.android.spinkit.SpinKitView;

public class WaitDialog extends Dialog {

    //paras
    private int window_width=400;
    private int window_height=420;
    private float background_alpha=0.6f;
    private int windowPOS_X;
    private int windowPOS_Y;
    //contents
    private TextView title;
    private SpinKitView loadCircle;

    /**
     *
     * @param context 上下文
     * @param themeResId 风格
     */
    public WaitDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.wait_dialog_titled, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);
        title = view.findViewById(R.id.wait_dialog_title);
        loadCircle = view.findViewById(R.id.Load).findViewById(R.id.load_circle);
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = window_width;
        lp.height = window_height;
        lp.alpha = background_alpha;
        this.getWindow().setAttributes(lp);
    }

    public WaitDialog setWindow_paras(int window_width,int window_height) {
        this.window_width=window_width;
        this.window_height=window_height;
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.height = window_height;
        lp.width = window_width;
        this.getWindow().setAttributes(lp);
        return this;
    }

    public WaitDialog setBackground_alpha(float background_alpha) {
        this.background_alpha=background_alpha;
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = background_alpha;
        this.getWindow().setAttributes(lp);
        return this;
    }

    public WaitDialog setTitle(String title) {
        this.title.setText(title);
        return this;
    }

    public WaitDialog setTitleColor(Context context, int colorID){
        this.title.setTextColor(ContextCompat.getColor(context,colorID));
        return this;
    }

    public WaitDialog setWindowPOS(int X,int Y){
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.x = X;
        lp.y = Y;
        this.getWindow().setAttributes(lp);
        return this;
    }

    public WaitDialog setLoadCircleColor(Context context, int colorResId){
        loadCircle.setColor(ContextCompat.getColor(context,colorResId));
        return this;
    }
}
