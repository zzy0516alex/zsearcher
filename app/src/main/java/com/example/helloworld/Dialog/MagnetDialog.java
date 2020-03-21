package com.example.helloworld.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.helloworld.R;

public class MagnetDialog extends Dialog {
    private TextView title;
    private TextView magnet;

    public MagnetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_magnet);
        title=findViewById(R.id.d_title);
        magnet=findViewById(R.id.magnet);
    }
}
