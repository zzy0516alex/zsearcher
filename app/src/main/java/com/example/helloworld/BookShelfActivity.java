package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.helloworld.Adapters.BookshelfAdapter;
import com.example.helloworld.myObjects.NovelChap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookShelfActivity extends AppCompatActivity {
    List<Bitmap> BookCover;
    List<String> BookName;
    private SharedPreferences myInfo;
    private BookshelfAdapter adapter;
    private GridView bookShelf;
    private Button delete;
    Context context;
    private boolean is_item_chosen=false;
    private int item_chosen=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_shelf);
        File Folder =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookCovers");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        BookCover=new ArrayList<>();
        BookName=new ArrayList<>();
        context=this;
        bookShelf=findViewById(R.id.BookShelf);
        delete=findViewById(R.id.delete);
        delete.setVisibility(View.INVISIBLE);
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);
        final int num=myInfo.getInt("bookNum",0);
        if(num!=0){
            for (int i = 1; i <=num ; i++) {
                String bookname=myInfo.getString("BookName"+i,"");
                if(!bookname.equals("")){
                    BookName.add(bookname);
                    BookCover.add(getImage(bookname));
                }
            }
            adapter=new BookshelfAdapter(BookCover,BookName,context);
            bookShelf.setAdapter(adapter);
            bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!is_item_chosen){
                        //TODO show content
//                        Intent intent=new Intent(BookShelfActivity.this,NovelShowAcitivity.class);
//                        Bundle bundle=new Bundle();
//                        bundle.putString("url",myInfo.getString("BookUrl"+(position+1),""));
//                        bundle.putInt("firstChap",myInfo.getInt("FirstChap"+(position+1),999999));
//                        bundle.putInt("lastChap",myInfo.getInt("LastChap"+(position+1),999999));
//                        intent.putExtras(bundle);
                        //debug
                        Intent intent=new Intent(BookShelfActivity.this,NovelViewerActivity.class);
                        String content="";
                        try {
                            content=read_text(BookName.get(position));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        NovelChap chap=new NovelChap("第一章",content);
                        NovelViewerActivity.setCurrent_chap(chap);
                        //NovelShowAcitivity.setFloatButtonShow(false);
                        startActivity(intent);
                    }else {
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.INVISIBLE);
                    }
                }
            });
            bookShelf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!is_item_chosen) {
                        adapter.setItem_chosen(position);
                        is_item_chosen = true;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.VISIBLE);
                        item_chosen=position;
                    }else {
                        adapter.setItem_chosen(-1);
                        is_item_chosen = false;
                        adapter.notifyDataSetChanged();
                        delete.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //TODO remove book
                        int num;
                        int counter=0;
                        String book_remove;
                        List<String>book_name_left;
                        List<String>book_url_left;
                        int []book_firstchap_left;
                        int []book_lastchap_left;
                        int myprogress=10;
                        //
                        num=myInfo.getInt("bookNum",0);
                        book_remove = myInfo.getString("BookName" + (item_chosen + 1), "");
                        myprogress = myInfo.getInt("Progress", 10);
                        if(num!=1) {
                            book_name_left = new ArrayList<>();
                            book_url_left = new ArrayList<>();
                            book_firstchap_left = new int[num - 1];
                            book_lastchap_left = new int[num - 1];
                            for (int i = 1; i <= num; i++) {
                                String current_book_name = myInfo.getString("BookName" + i, "");
                                String current_book_url = myInfo.getString("BookUrl" + i, "");
                                int current_book_firstchap = myInfo.getInt("FirstChap" + i, 999999);
                                int current_book_lastchap = myInfo.getInt("LastChap" + i, 999999);
                                if ((!current_book_name.equals(book_remove)) && (!current_book_name.equals(""))) {
                                    book_name_left.add(current_book_name);
                                    book_url_left.add(current_book_url);
                                    book_firstchap_left[counter] = current_book_firstchap;
                                    book_lastchap_left[counter] = current_book_lastchap;
                                    counter++;
                                }
                            }
                            SharedPreferences.Editor editor = myInfo.edit();
                            editor.clear();
                            editor.apply();
                            SharedPreferences.Editor editor2 = myInfo.edit();
                            editor2.putInt("bookNum", num - 1);
                            editor2.putInt("Progress", myprogress);
                            for (int i = 1; i <= num - 1; i++) {
                                editor2.putString("BookName" + i, book_name_left.get(i - 1)).apply();
                                editor2.putString("BookUrl" + i, book_url_left.get(i - 1)).apply();
                                editor2.putInt("FirstChap" + i, book_firstchap_left[i - 1]).apply();
                                editor2.putInt("LastChap" + i, book_lastchap_left[i - 1]).apply();
                            }
                            editor2.apply();
                        }else{
                            SharedPreferences.Editor editor = myInfo.edit();
                            editor.clear();
                            editor.apply();
                            editor.putInt("bookNum",0);
                            editor.putInt("Progress",myprogress);
                            editor.apply();
                        }
                        File delefile = new File(getExternalFilesDir(null)+"/ZsearchRes/BookCovers/"+book_remove+".png");
                        if(delefile.exists() && delefile.isFile()) {
                            if(delefile.delete()){
                                Log.e("delete","success");
                            }else{
                                Log.e("delete","fail");
                            }
                        }else {
                            Log.e("delete","does not exist");
                        }
                        if(!book_remove.equals("")){
                            BookName.remove(book_remove);
                            BookCover.remove(item_chosen);
                            adapter.setItem_chosen(-1);
                            is_item_chosen = false;
                            adapter.notifyDataSetChanged();
                        }
                        //
                        TimeUnit.SECONDS.sleep(1);
                        delete.setVisibility(View.INVISIBLE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    public Bitmap getImage(String BookName) {
        Bitmap bitmap_AddShadow=null;
        File picDir=new File(getExternalFilesDir(null)+"/ZsearchRes/BookCovers/" + BookName + ".png");
        FileInputStream fis=null;
        try{
            fis=new FileInputStream(picDir);
            Bitmap bitmap= BitmapFactory.decodeStream(fis);
            Bitmap bitmap_adjustSize=getNewBitmap(bitmap,300,400);
            bitmap_AddShadow=drawImageDropShadow(bitmap_adjustSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(getResources(),R.mipmap.no_book_cover);
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap_AddShadow;
    }

    public Bitmap getNewBitmap(Bitmap bitmap, int newWidth ,int newHeight){
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }
    private Bitmap drawImageDropShadow(Bitmap originalBitmap) {

        BlurMaskFilter blurFilter = new BlurMaskFilter(1,
                BlurMaskFilter.Blur.NORMAL);
        Paint shadowPaint = new Paint();
        shadowPaint.setAlpha(50);
        shadowPaint.setColor(Color.parseColor("#FF0000"));
        shadowPaint.setMaskFilter(blurFilter);

        int[] offsetXY = new int[2];
        Bitmap shadowBitmap = originalBitmap
                .extractAlpha(shadowPaint, offsetXY);

        Bitmap shadowImage32 = shadowBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(shadowImage32);
        c.drawBitmap(originalBitmap, offsetXY[0], offsetXY[1], null);

        return shadowImage32;
    }
    public String read_text(String bookname) throws IOException {
        File txtDir=new File(getExternalFilesDir(null)+"/ZsearchRes/BookContents/" + bookname + ".txt");
        FileInputStream fis=null;
        StringBuilder sb = new StringBuilder("");
        try {
            fis = new FileInputStream(txtDir);
            InputStreamReader i_reader=new InputStreamReader(fis);
            BufferedReader b_reader=new BufferedReader(i_reader);
            String temp="";
//            byte[] buff = new byte[1024];
//            int len = 0;
            //(len = fis.read(buff)) > 0
            while ((temp=b_reader.readLine())!=null) {
                sb.append(temp);
                sb.append("\n");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e("error","book_content_not_found");
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
