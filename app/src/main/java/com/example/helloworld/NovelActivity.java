package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NovelActivity extends AppCompatActivity {

    private String url="https://www.52bqg.com/modules/article/search.php?searchkey=";
    private EditText editText;
    private Button button;
    private ListView booklist;
    private String input;
    private String code;
    private boolean startsearch;
    private List<String>Novels;
    private List<String>Contents;
    private Context context;
    BooklistAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel);
        editText=findViewById(R.id.input);
        button=findViewById(R.id.search);
        booklist=findViewById(R.id.booklist);
        Novels=new ArrayList<>();
        Contents=new ArrayList<>();
        context=this;
        final Activity activity= NovelActivity.this;

        File Folder =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookCovers");
        if(!Folder.exists()){
            Folder.mkdir();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((!editText.getText().toString().equals(""))&&(editText.getText().toString().length()>1)){
                    input=editText.getText().toString();
                    startsearch=true;
                }
                else startsearch=false;
                if (startsearch){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0) ;
                    TimerButton();
                    NovelSearch();
                }
                else {Toast.makeText(NovelActivity.this, "输入的字符必须大于二", Toast.LENGTH_SHORT).show();
                restartActivity(activity);
                }
                //Log.e("test2",Contents.get(0));
                adapter=new BooklistAdapter(Novels,context,false,"");
                booklist.setAdapter(adapter);
                booklist.setOnItemClickListener(new onItemclick());
            }
        });
    }

    private void TimerButton() {
        button.setEnabled(false);
        new CountDownTimer(30000 + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                button.setText(millisUntilFinished/1000+"s");
            }

            @Override
            public void onFinish() {
                button.setEnabled(true);
                button.setText("搜索");
            }
        }.start();
    }
    public static void restartActivity(Activity act){

        Intent intent=new Intent();
        intent.setClass(act, act.getClass());
        act.startActivity(intent);
        act.finish();

    }

    private void NovelSearch() {
        try {
            code=URLEncoder.encode(input,"gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Log.e("test",code);
        NovelThread T =new NovelThread(url,code);
        T.start();
        if(T.getError()){
            Log.e("err","getErr1");
            Toast.makeText(context,"请先开启网络链接", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.putExtra("extra_prefs_show_button_bar", true);//是否显示button bar
            intent.putExtra("extra_prefs_set_next_text", "完成");
            intent.putExtra("extra_prefs_set_back_text", "返回");
            startActivity(intent);
            //NovelActivity.this.finish();
        }else {
            if (T.isFound()) {
                if (T.getNovelnames() != null) Novels = (List<String>) T.getNovelnames();
                if (T.getNovelcontents() != null) Contents = (List<String>) T.getNovelcontents();
            } else {
                Toast.makeText(context, "未找到", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String newUrl;
    private int lastChap;
    private int firstChap;
    public class onItemclick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e("test2",Contents.get(position));
            ContentThread thread =new ContentThread(Contents.get(position));
            thread.start();
            if(thread.getContentUrl()!=null) newUrl= (String) thread.getContentUrl();
            if(thread.getFirstchap()!=null) firstChap= (int) thread.getFirstchap();
            if(thread.getLastchap()!=null) lastChap= (int) thread.getLastchap();
            Intent intent=new Intent(NovelActivity.this,NovelShowAcitivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("url",newUrl);
            bundle.putInt("firstChap",firstChap);
            bundle.putInt("lastChap",lastChap);
            intent.putExtras(bundle);
            NovelShowAcitivity.setFloatButtonShow(true);
            startActivity(intent);
        }
    }



}
