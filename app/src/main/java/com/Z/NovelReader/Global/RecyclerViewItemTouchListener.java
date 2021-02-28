package com.Z.NovelReader.Global;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {
    private GestureDetectorCompat mGestureDetectorCompat;

    public RecyclerViewItemTouchListener(GestureDetectorCompat mGestureDetectorCompat) {
        this.mGestureDetectorCompat = mGestureDetectorCompat;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return mGestureDetectorCompat.onTouchEvent(e);
    }
}
