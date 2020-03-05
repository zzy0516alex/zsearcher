package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JsoupActivity extends AppCompatActivity {
    private Button mbtn;
    private TextView result;
    private EditText input;
    private ImageView pic;
    private String search;
    private Bitmap mybitmap;
    private boolean startsearch;
    Elements elements;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsoup);
        mbtn=findViewById(R.id.showxml);
        pic=findViewById(R.id.pic);
        result=findViewById(R.id.text_xml);
        input=findViewById(R.id.input);
        mbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input.getText().toString().equals("")){
                    String textIn=input.getText().toString();
                    search="/sk/"+textIn;
                    startsearch=true;
                }
                else startsearch=false;
                if (startsearch) {
                    classificationSearch();
                }
                else Toast.makeText(JsoupActivity.this, "请先输入内容", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void classificationSearch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String outputResults;
                    Document doc= Jsoup.connect("https://lajifenleiapp.com/"+search).get();
                    elements =doc.select("div.row");
                    final String results=elements.get(3).select("h1").text();
                    String url="https://lajifenleiapp.com"+elements.get(3).select("img").attr("src");
                    if(!results.equals("")) {
                        outputResults = results.replace(" ", "");
                        PictureThread T=new PictureThread(url);
                        T.start();
                        if(T.getMyBitmap()!=null)mybitmap= (Bitmap) T.getMyBitmap();
                        //
//                        File Folder =new File(getExternalFilesDir(null),"ZsearchRes");
//                        if(!Folder.exists()){
//                            Folder.mkdir();
//                            boolean isFolderMade=Folder.mkdirs();
//                            if(isFolderMade)Log.d("folder","success");
//                            else Log.d("folder","fail");
//                        }
//                        File pic=new File(getExternalFilesDir(null)+"/ZsearchRes/"+ "鸡蛋"+".png" );
//                        FileOutputStream fot=null;
//                        try {
//                            fot=new FileOutputStream(pic);
//                        }catch (FileNotFoundException e){
//                            e.printStackTrace();
//                        }
//                        mybitmap.compress(Bitmap.CompressFormat.PNG,100,fot);
//                        try{
//                            fot.flush();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }finally {
//                            if(fot!=null){
//                                try {
//                                    fot.close();
//                                }catch (IOException e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        }

                    }else{
                        outputResults="未找到";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.append(outputResults);
                            result.append("\n");
                            pic.setImageBitmap(mybitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
