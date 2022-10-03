package com.Z.NovelReader.Basic;

import static com.Z.NovelReader.Basic.BasicHandlerThread.PROCESS_DONE;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class BasicHandler<T> extends Handler {
    BasicHandlerListener<T> listener;

    public BasicHandler (BasicHandlerListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what==PROCESS_DONE)
            listener.onSuccess((T) msg.obj);
        else {
            listener.onError(msg.what);
        }
    }
    public interface BasicHandlerListener<T>{
        void onSuccess(T result);
        void onError(int error_code);
    }
}
