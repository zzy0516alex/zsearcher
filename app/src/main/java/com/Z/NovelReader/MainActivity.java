package com.Z.NovelReader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.Z.NovelReader.Utils.StatusBarUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    //在此处添加变量
    private Button NovelSearch;
    private Button MyBookShelf;
    private Button test;
    OnClick onclick=new OnClick();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //在下面定义自己的变量
        NovelSearch =findViewById(R.id.BookSearch);
        MyBookShelf=findViewById(R.id.MyBookShelf);
        test=findViewById(R.id.btntest);
        MyBookShelf.setOnClickListener(onclick);
        NovelSearch.setOnClickListener(onclick);
        File Folder =new File(getExternalFilesDir(null),"ZsearchRes");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this,false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //true=黑色字体  false=白色
        StatusBarUtil.setStatusBarDarkTheme(this, false);

    }


    private class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent =null;
            switch (v.getId()){
                case R.id.MyBookShelf: {
                    intent=new Intent(MainActivity.this,BookShelfActivity.class);
                    break;
                }
                case R.id.BookSearch:{
                    intent=new Intent(MainActivity.this, NovelActivity.class);
                    break;
                }
                default:{}

            }
            startActivity(intent);
        }
    }

//    FileSelector.from(MainActivity.this)
//            .onlySelectFolder()  //只能选择文件夹
//                .requestCode(1) //设置返回码
//                .start();
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == 1) {
//                ArrayList<String> essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
//                StringBuilder builder = new StringBuilder();
//                for (String file :
//                        essFileList) {
//                    builder.append(file).append("\n");
//                }
//                Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}
