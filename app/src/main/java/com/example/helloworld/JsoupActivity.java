package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helloworld.Threads.PictureThread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JsoupActivity extends AppCompatActivity {
    private Button mbtn;
    private TextView result;
    private EditText input;
    private ScrollView mScroll;
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
        mScroll=findViewById(R.id.garbage_result_scroll);
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
                    elements =doc.select("div.col-md-offset-1.col-md-10.col-xs-12");
                    final String results=elements.get(1).select("h1").text();
                    String url="https://lajifenleiapp.com"+elements.get(1).select("img").attr("src");
                    if(!results.equals("")) {
                        outputResults = results.replace(" ", "");
                        PictureThread T=new PictureThread(url);
                        T.start();
                        if(T.getMyBitmap()!=null)mybitmap= (Bitmap) T.getMyBitmap();

                    }else{
                        outputResults="未找到";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.append(outputResults);
                            result.append("\n");
                            Editable editable = result.getEditableText();
                            int length = editable.length();
                            if (length > 50) {
                                editable.delete(0, length - length/2);
                            }
                            mScroll.fullScroll(View.FOCUS_DOWN);
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
