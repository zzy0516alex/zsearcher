package com.example.helloworld;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.helloworld.Adapters.BooklistAdapter;
import com.example.helloworld.Threads.ContentURLThread;
import com.example.helloworld.Threads.NovelThread;
import com.example.helloworld.Utils.ViberateControl;
import com.example.helloworld.myObjects.BookList;
import com.example.helloworld.myObjects.NovelCatalog;
import com.example.helloworld.myObjects.NovelSearchResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NovelActivity extends AppCompatActivity {

    private String url;
    private String url2;
    private EditText search_key;
    private Button search_start;
    private ListView booklist;
    private ImageButton back;
    private String input;
    private String code;
    private boolean startsearch;
    private List<String>Novels;
    private List<String> Links;
    private NovelSearchResult novelSearchResult;
    private ArrayList<BookList> BookList;
    private Context context;
    private Dialog wait_dialog;
    BooklistAdapter adapter;
    RelativeLayout loadView;
    Handler handler;
    Handler contentURL_handler;
    InputMethodManager manager;
    boolean first_create=true;
    int not_find_count =0;
    int find_count=0;
    int internet_err=0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel);
        search_key =findViewById(R.id.input);
        search_key.requestFocus();
        search_start =findViewById(R.id.search);
        booklist=findViewById(R.id.booklist);
        loadView = (RelativeLayout) findViewById(R.id.Load);
        back=findViewById(R.id.back);
        Novels=new ArrayList<>();
        Links =new ArrayList<>();
        context=this;
        final Activity activity= NovelActivity.this;
        manager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        url=this.getString(R.string.book_search_base1);
        url2=this.getString(R.string.book_search_base2);

        novelSearchResult= ViewModelProviders.of(this).get(NovelSearchResult.class);
        novelSearchResult.getMySearchResult().observe(this, new Observer<ArrayList<BookList>>() {
            @Override
            public void onChanged(ArrayList<BookList> bookLists) {
                if (bookLists.size()!=0){
                    BookList=bookLists;
                    Novels.clear();
                    Links.clear();
                    for (BookList bookList: bookLists) {
                        Novels.add(bookList.getBookName());
                        Links.add(bookList.getBookLink());
                    }
                    adapter=new BooklistAdapter(Novels,context,false,"");
                    if (first_create){
                        first_create=false;
                        booklist.setAdapter(adapter);
                        booklist.setOnItemClickListener(new onItemclick());
                    }else {
                        adapter.updateList(Novels);
                    }
                }
            }
        });

        File Folder =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookCovers");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        File Folder2 =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookContents");
        if(!Folder2.exists()){
            Folder2.mkdir();
        }

        loadView.setVisibility(View.GONE);


        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Log.d("search_result",""+msg.what);
                switch(msg.what){
                    case NovelThread.BOOK_SEARCH_NOT_FOUND:
                        not_find_count++;
                        if (not_find_count ==2) Toast.makeText(context, "未找到", Toast.LENGTH_SHORT).show();
                        break;
                    case NovelThread.BOOK_SEARCH_NO_INTERNET:
                        internet_err++;
                        if (internet_err==2)launch_internet_setting(activity);
                        break;
                    case NovelThread.BOOK_SEARCH_DONE:
                        find_count++;
                        novelSearchResult.addToResult((ArrayList<BookList>) msg.obj);
                        break;
                    default:
                }
                if (find_count+not_find_count+internet_err==2){
                    loadView.setVisibility(View.GONE);
                    find_count=0;
                    not_find_count=0;
                    internet_err=0;
                }
                search_key.setCursorVisible(true);
            }
        };
        contentURL_handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //loadView.setVisibility(View.GONE);
                wait_dialog.dismiss();
                launchNovelShow((NovelCatalog) msg.obj);
            }
        };

        search_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_btn_down(activity);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViberateControl.Vibrate(activity,15);
                back.setImageResource(R.drawable.backarrow_onclick);
                activity.onBackPressed();
            }
        });
        enter_key_down(activity);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        loadView.setVisibility(View.GONE);
    }

    private void search_btn_down(Activity activity) {
        loadView.setVisibility(View.VISIBLE);
        if((!search_key.getText().toString().equals(""))){
            input= search_key.getText().toString();
            startsearch=true;
        }
        else startsearch=false;
        if (startsearch){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(search_key.getWindowToken(), 0) ;
            search_key.setCursorVisible(false);
            if (adapter!=null){
                BookList.clear();
                Novels.clear();
                Links.clear();
                novelSearchResult.clear();
            }
            TimerButton();
            NovelSearch();
        }
        else {
            Toast.makeText(NovelActivity.this, "输入的字符不能为空", Toast.LENGTH_SHORT).show();
        restartActivity(activity);
        }
    }
    private void enter_key_down(final Activity activity){
        search_key.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN){
                    search_btn_down(activity);
                }
                return false;
            }
        });
    }

    private void TimerButton() {
        search_start.setEnabled(false);
        new CountDownTimer(5000 + 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                search_start.setText(millisUntilFinished/1000+"s");
            }

            @Override
            public void onFinish() {
                search_start.setEnabled(true);
                search_start.setText("搜索");
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
            code=URLEncoder.encode(input,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Log.e("test",code);
        NovelThread T =new NovelThread(url,code, NovelThread.TAG.BiQuGe);
        T.setHandler(handler);
        T.start();
        NovelThread T2=new NovelThread(url2,code, NovelThread.TAG.SiDaMingZhu);
        T2.setHandler(handler);
        T2.start();
    }

    private void launch_internet_setting(final Activity activity) {
            Toast.makeText(context,"请先开启网络链接", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("无网络")
                .setMessage("网络连接失败，是否前往设置？")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                        intent.putExtra("extra_prefs_show_button_bar", true);//是否显示button bar
                        intent.putExtra("extra_prefs_set_next_text", "完成");
                        intent.putExtra("extra_prefs_set_back_text", "返回");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartActivity(activity);
                    }
                }).setCancelable(false).show();
    }

    private void initWaitView(){
        final View view = LayoutInflater.from(this).inflate(R.layout.wait_dialog1, null);
        wait_dialog = new Dialog(this, R.style.WaitDialog);
        wait_dialog.setContentView(view);
        wait_dialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams lp = wait_dialog.getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = booklist.getHeight();
        lp.x= (int) booklist.getX();
        lp.y= (int) booklist.getY();
        lp.alpha = 0.8f;
        wait_dialog.getWindow().setAttributes(lp);
    }

    private String CatalogUrl;
    private String BookName;
    private NovelThread.TAG current_tag;
    public class onItemclick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BookList current_book=BookList.get(position);
            CatalogUrl=Links.get(position);
            BookName=current_book.getBookNameWithoutWriter();
            current_tag=current_book.getTag();
            Log.e("book_chose", CatalogUrl);
            ContentURLThread thread =new ContentURLThread(CatalogUrl);
            thread.setContext(context);
            thread.setHandler(contentURL_handler);
            thread.setTag(BookList.get(position).getTag());
            thread.start();
            //loadView.setVisibility(View.VISIBLE);
            initWaitView();
            wait_dialog.show();

        }
    }

    private void launchNovelShow(NovelCatalog newNovel) {
        Intent intent=new Intent(NovelActivity.this, NovelShowAcitivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("url",newNovel.getLink().get(0));
        bundle.putString("currentTitle",newNovel.getTitle().get(0));
        bundle.putString("CatalogUrl",CatalogUrl);
        bundle.putString("BookName",BookName);
        bundle.putSerializable("tag",current_tag);
        intent.putExtras(bundle);
        NovelShowAcitivity.setFloatButtonShow(true);
        NovelShowAcitivity.setFirst_load(true);
        startActivity(intent);
    }


}
