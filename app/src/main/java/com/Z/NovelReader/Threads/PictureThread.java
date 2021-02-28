package com.Z.NovelReader.Threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PictureThread extends Thread {
    private String url;
    private Object MyBitmap;
    public PictureThread(String url) {
        this.url=url;
    }

    @Override
    public void run() {
        super.run();
        Bitmap myBitmap=returnBitMap(url);
        MyBitmap=myBitmap;
    }
    public final static Bitmap returnBitMap(String url) {
        URL myFileUrl;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Object getMyBitmap() {
        try {
            this.join();
            return MyBitmap;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
