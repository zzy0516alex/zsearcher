package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.helloworld.Threads.BTSearchThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BtSearchActivity extends AppCompatActivity {

    private HashMap<String,ArrayList<String>>resultBox1;

    private EditText searchTarget;
    private ImageButton BTsearch;
    private RelativeLayout loading;
    Context context;
    BTSearchThread thread;
    Handler handler;
    final int SEARCH_DONE=0X1;
    final int SEARCH_DONE2=0X3;
    private int search_counter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_search);
        searchTarget=findViewById(R.id.searchTarget);
        BTsearch=findViewById(R.id.BTsearch);
        loading=findViewById(R.id.Load);
        loading.setVisibility(View.GONE);
        context=this;
        //com.xunlei.downloadprovider
        //doStartApplicationWithPackageName("com.xunlei.downloadprovider");
        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what==SEARCH_DONE){
                    get_result();
                    loading.setVisibility(View.GONE);
                    searchTarget.setCursorVisible(true);
                    search_counter++;
                    Log.e("search","DONE_NO.1");
                }
                if(msg.what==SEARCH_DONE2){

                }
                if(search_counter==1){
                    Intent intent=new Intent(BtSearchActivity.this,BtResultActivity.class);
                    BtResultActivity.setResultBox1(resultBox1);
                    search_counter=0;
                    startActivity(intent);
                }
            }
        };
        BTsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String keyword=searchTarget.getText().toString();
                if(!keyword.equals("")){
                    loading.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchTarget.getWindowToken(), 0) ;
                    searchTarget.setCursorVisible(false);
                    BtSearch(keyword);

                }else{
                    Toast.makeText(BtSearchActivity.this, "请先输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void BtSearch(String keyword) {

        thread=new BTSearchThread(keyword);
        thread.setMhandler(handler);
        thread.start();
    }
    private void get_result(){
        if (thread.getErr()) {
            Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
        } else {
            if (thread.getResultList() != null) {
                resultBox1=thread.getResultList();
            }
        }
    }

    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            Toast.makeText(this, "请先安装迅雷", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }
}
