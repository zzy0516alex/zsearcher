package com.example.helloworld.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.helloworld.R;

public class MagnetDialog extends Dialog implements View.OnClickListener {
    private TextView title;
    private TextView magnet;
    private TextView copy_link;
    private TextView open_with_thunder;
//    private LinearLayout copy_layout;
//    private LinearLayout open_layout;

    private String myTitle;
    private String myMagnet;

    private OnCopyListener copyListener;
    private OnOpenListener openListener;

    public MagnetDialog(@NonNull Context context) {
        super(context);
    }

    public MagnetDialog setMyTitle(String myTitle) {
        this.myTitle = myTitle;
        return this;
    }

    public MagnetDialog setMyMagnet(String myMagnet) {
        this.myMagnet = myMagnet;
        return this;
    }

    public MagnetDialog setCopyListener(OnCopyListener copyListener) {
        this.copyListener = copyListener;
        return this;
    }

    public MagnetDialog setOpenListener(OnOpenListener openListener) {
        this.openListener = openListener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_magnet);
        title=findViewById(R.id.d_title);
        magnet=findViewById(R.id.magnet);
        copy_link=findViewById(R.id.copy_link);
        open_with_thunder=findViewById(R.id.open_with_thunder);
//        copy_layout=findViewById(R.id.copy_layout);
//        open_layout=findViewById(R.id.open_layout);
        if (!TextUtils.isEmpty(myTitle)){
            title.setText(myTitle);
        }
        if (!TextUtils.isEmpty(myMagnet)){
            magnet.setText(myMagnet);
        }
        copy_link.setOnClickListener(this);
        open_with_thunder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.copy_link: {
                if (copyListener!=null){
                    copyListener.OnCopy(this);
                    //copy_layout.setBackgroundColor(Color.parseColor("#DCDCDC"));
                }
            }
                break;
            case R.id.open_with_thunder:
                if (openListener!=null){
                    openListener.OnOpen(this);
                    //open_layout.setBackgroundColor(Color.parseColor("#DCDCDC"));
                }
                break;
            default:
        }
    }

    public interface OnCopyListener{
        void OnCopy(MagnetDialog dialog);
    }
    public interface OnOpenListener{
        void OnOpen(MagnetDialog dialog);
    }
}
