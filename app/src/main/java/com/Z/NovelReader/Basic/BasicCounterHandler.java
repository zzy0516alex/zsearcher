package com.Z.NovelReader.Basic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class BasicCounterHandler<T> extends Handler {
    private boolean no_error = true;
    private boolean ignore_error = false;
    private int count = 0;
    private int total_count = -1;
    private ArrayList<T> result_list;
    private BasicCounterHandlerListener<T> listener;
    public BasicCounterHandler(BasicCounterHandlerListener<T> listener) {
        this.listener = listener;
        this.count = 0;
        this.result_list = new ArrayList<>();
    }

    public void setTotal_count(int total_count, boolean ignore_error) {
        this.total_count = total_count;
        this.ignore_error = ignore_error;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (!BasicHandlerThread.isErrorOccur(msg.what)){
            if (msg.obj!=null) {
                listener.onSuccess((T) msg.obj);
                result_list.add((T) msg.obj);
            }
        }
        else {
            if (no_error)listener.onError();
            no_error = false;
        }
        synchronized(this){
            count++;
            if (count == total_count && (no_error || ignore_error)){
                listener.onAllProcessDone(result_list);
            }
        }
    }
    public interface BasicCounterHandlerListener<T>{
        void onSuccess(T result);
        void onError();
        void onAllProcessDone(ArrayList<T> results);
    }
}
