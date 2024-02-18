package com.Z.NovelReader.Threads.Handlers;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Threads.ContentThread;

import java.lang.ref.WeakReference;

public class ChapContentHandler<T> extends Handler {
    private final WeakReference<T> mActivity;
    private ChapContentListener listener;

    public ChapContentHandler(T activity) {
        mActivity = new WeakReference<T>(activity);
    }

    public void setListener(ChapContentListener listener) {
        this.listener = listener;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        T activity = mActivity.get();
        if (activity != null) {
            switch (BasicHandlerThread.mergeEvents(msg.what)) {
                case BasicHandlerThread.PROCESS_DONE:
                    listener.onSuccess((String) msg.obj);
                    break;
                case BasicHandlerThread.ERROR_OCCUR:
                    listener.onError((String) msg.obj);
                    break;
                default:
            }
            listener.onFinish();
        }
    }
    public interface ChapContentListener{
        void onSuccess(String chap_content);
        void onError(String error_content);
        void onFinish();
    }
}
