package com.Z.NovelReader.Basic;

import android.os.Handler;
import android.os.Message;

abstract public class BasicHandlerThread extends Thread {
    private Handler handler;
    private Message message;

    public static final int PROCESS_DONE=0X1;//执行完毕
    public static final int TARGET_NOT_FOUND=0X2;//结果未找到
    public static final int NO_INTERNET=0X3;//无网络
    public static final int PROCESSOR_ERROR=0X4;//处理器错误
    public static final int NOVEL_SOURCE_NOT_FOUND=0X5;//处理器错误
    public static final int ERROR_OCCUR=0X6;//有错误发生(包含1,3,4,5)

    public void setHandler(Handler handler) {
        this.handler = handler;
        if (handler!=null)message = handler.obtainMessage();
    }

    public Handler getHandler() {
        return handler;
    }

    public void callback(int event, Object object){
        if (event < 0x1 || event > 0x6) throw new IllegalArgumentException("不存在该event");
        if (message!=null){
            message.what = event;
            if (object!=null)message.obj = object;
            handler.sendMessage(message);
        }
    }

    public void report(int event){
        callback(event,null);
    }

    public static int mergeEvents(int event){
        if (event > 0x1) return ERROR_OCCUR;
        else return PROCESS_DONE;
    }
}
