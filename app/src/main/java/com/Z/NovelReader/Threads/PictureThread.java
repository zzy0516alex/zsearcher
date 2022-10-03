package com.Z.NovelReader.Threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
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
    public Bitmap returnBitMap(String url) {
        URL myFileUrl;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            InputStream is = null;
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            }
            else if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM){
                String redirected_url = conn.getHeaderField("Location");
                return returnBitMap(redirected_url);
            }else Log.e("Picture Thread","unexpected response code:"+conn.getResponseCode());

            if (is == null){
                Log.d("Picture Thread","input stream is null");
            }
            else {
                try {
                    byte[] data=readStream(is);
                    if(data!=null){
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        return bitmap;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                is.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    /*
     * 得到图片字节流 数组大小
     * */
    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
