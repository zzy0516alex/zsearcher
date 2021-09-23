package com.Z.NovelReader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.BooklistAdapter;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Threads.CatalogURLThread;
import com.Z.NovelReader.Threads.ContentURLThread;
import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Utils.ViberateControl;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.NovelSearchBean;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.NovelSearchResult;
import com.Z.NovelReader.myObjects.beans.SearchQuery;
import com.Z.NovelReader.views.WaitDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NovelSearchActivity extends AppCompatActivity {

    //views
    private EditText search_key;//书名关键词
    private Button search_start;
    private ListView booklist;
    private ImageButton back;
    private BooklistAdapter adapter;
    private RelativeLayout loadView;//搜索等待
    private WaitDialog waitDialog;//novel show载入等待

    //basic paras
    private String input;// search_key to string
    private boolean startsearch;
    private Context context;
    private Activity activity;
    InputMethodManager manager;//软键盘设置
    boolean first_create=true;
    //datas
    private List<String>Novels;
    private List<String> Links;
    private NovelSearchResult novelSearchResult;//搜书结果view model，用于在listview中显示
    private ArrayList<NovelSearchBean> BookList;//搜书原始结果
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private List<SearchQuery> searchQueryList;//书源中的书籍搜索规则列表
    private Map<Integer, NovelRequire> novelRequireMap;//书源ID-规则对应表

    //handlers
    NovelSearchThread.NovelSearchHandler novelSearchHandler;
    ContentURLThread.ContentUrlHandler contentURL_handler;
    CatalogURLThread.CatalogUrlHandler catalogUrl_Handler;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_search);
        //init views
        search_start =findViewById(R.id.search);
        booklist=findViewById(R.id.booklist);
        loadView = (RelativeLayout) findViewById(R.id.Load);
        back=findViewById(R.id.back);

        //init 基本变量
        Novels=new ArrayList<>();
        Links =new ArrayList<>();
        context=this;
        activity= NovelSearchActivity.this;
        sourceDBTools=new NovelSourceDBTools(context);

        //初始化输入框
        initSearchKeyInput();

        //观测书籍搜索结果
        observeSearchResults();

        //初始化书源列表
        sourceDBTools.getSearchUrlList(new NovelSourceDBTools.QueryListener() {
            @Override
            public void onResultBack(Object object) {
                searchQueryList= (List<SearchQuery>) object;
            }
        });
        sourceDBTools.getNovelRequireMap(new NovelSourceDBTools.QueryListener() {
            @Override
            public void onResultBack(Object object) {
                if (object instanceof Map)
                    novelRequireMap = (Map<Integer, NovelRequire>) object;
            }
        });

        //新建本地书籍文件夹
        CreateSourcesFolder();

        loadView.setVisibility(View.GONE);//隐藏载入等待图标

        //初始化 handler
        init_ContentUrlHandler();
        init_CatalogURLHandler();

        //通过搜索按钮开始搜索
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
                activity.onBackPressed();
            }
        });

        //通过键盘enter键开始搜索
        enter_key_down(activity);

    }

    private void initSearchKeyInput() {
        search_key =findViewById(R.id.input);
        search_key.requestFocus();
        manager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    /**
     * 初始化content url handler，获取当前选择的书的指定章节的内容链接
     */
    private void init_ContentUrlHandler() {
        contentURL_handler=new ContentURLThread.ContentUrlHandler(new ContentURLThread.ContentUrlListener() {
            @Override
            public void onSuccess(NovelCatalog currentChap) {
                waitDialog.dismiss();
                launchNovelShow(currentChap);
            }

            @Override
            public void onError(int error_code,int sourceID) {
                waitDialog.dismiss();
                switch(error_code){
                    case ContentURLThread.NO_INTERNET:
                        Toast.makeText(context, "无网络", Toast.LENGTH_SHORT).show();
                        break;
                    case ContentURLThread.PROCESSOR_ERROR:
                        Toast.makeText(context, "书源规则解析出错,ID:"+sourceID, Toast.LENGTH_SHORT).show();
                        break;
                    case ContentURLThread.RULE_NEED_UPDATE:
                        Toast.makeText(context, "书源需要更新,ID:"+sourceID, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        });
    }

    private void init_CatalogURLHandler(){
        catalogUrl_Handler = new CatalogURLThread.CatalogUrlHandler(new CatalogURLThread.CatalogUrlListener() {
            @Override
            public void onSuccess(String catalog_url) {
                if (current_book!=null) {
                    current_book.setBookCatalogLink(catalog_url);
                    getContentURL(current_book);
                }else {
                    if (waitDialog!=null)waitDialog.dismiss();
                    Toast.makeText(context, "获取当前书籍失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int error_code) {
                if (waitDialog!=null)waitDialog.dismiss();
                if (current_book==null)Toast.makeText(context, "严重：获取当前书籍&目录失败", Toast.LENGTH_SHORT).show();
                switch(error_code){
                    case CatalogURLThread.PROCESSOR_ERROR:
                        Toast.makeText(context, "书源解析出错,ID:"+current_book.getSource(), Toast.LENGTH_SHORT).show();
                        break;
                    case CatalogURLThread.URL_NOT_FOUND:
                        Toast.makeText(context, "未获取到目录链接,ID:"+current_book.getSource(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        });
    }

    private void CreateSourcesFolder() {
        File Folder =new File(getExternalFilesDir(null)+"/ZsearchRes/","BookReserve");
        if(!Folder.exists()){
            Folder.mkdir();
        }
    }

    /**
     * 获取书籍搜索结果
     */
    private void observeSearchResults() {
        novelSearchResult= ViewModelProviders.of(this).get(NovelSearchResult.class);
        novelSearchResult.getMySearchResult().observe(this, new Observer<ArrayList<NovelSearchBean>>() {
            @Override
            public void onChanged(ArrayList<NovelSearchBean> bookList) {
                if (bookList.size()!=0){
                    BookList=bookList;
                    Novels.clear();
                    Links.clear();
                    for (NovelSearchBean novelSearchBean : bookList) {
                        Novels.add(novelSearchBean.getBookName());
                        Links.add(novelSearchBean.getBookInfoLink());
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
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        loadView.setVisibility(View.GONE);
    }

    /**
     * 搜索初始化及启动搜书线程
     * @param activity 用于从新载入搜索界面
     */
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
            //启动搜索限制计时器
            TimerButton();
            //启动搜书线程
            NovelSearch();
        }
        else {
            Toast.makeText(NovelSearchActivity.this, "输入的字符不能为空", Toast.LENGTH_SHORT).show();
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

    /**
     * 设置搜索间隔
     */
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

    /**
     * 书籍搜索线程
     */
    private void NovelSearch() {

        if (searchQueryList.size()!=0) {
            //init search handler
            novelSearchHandler=new NovelSearchThread.NovelSearchHandler(searchQueryList.size(),
                    new NovelSearchThread.NovelSearchListener() {
                        @Override
                        public void onSearchResult(ArrayList<NovelSearchBean> search_result) {
                            novelSearchResult.addToResult(search_result);
                        }

                        @Override
                        public void onSearchError(int error_code) {
                            switch(error_code){
                                case NovelSearchThread.ILLEGAL_BOOK_SOURCE:
                                    Toast.makeText(context, "书籍资源出错", Toast.LENGTH_SHORT).show();
                                    break;
                                case NovelSearchThread.BOOK_SOURCE_DIABLED:
                                    Toast.makeText(context, "书源被禁用", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                            }
                        }

                        @Override
                        public void onSearchFinish(int total_num, int num_no_internet, int num_not_found) {
                            if (loadView.getVisibility()!=View.GONE)
                                loadView.setVisibility(View.GONE);
                            search_key.setCursorVisible(true);
                            novelSearchHandler.clearAllCounters();
                            if (num_not_found==total_num)
                                Toast.makeText(context, "未找到", Toast.LENGTH_SHORT).show();
                            if (num_no_internet==total_num)
                                launch_internet_setting(activity);
                        }

                    });

            //start search thread
            for (SearchQuery search_query : searchQueryList) {
                NovelSearchThread T = new NovelSearchThread(context,search_query,input);
                T.setHandler(novelSearchHandler);
                T.start();
            }
        }else Toast.makeText(context, "未找到有效书源", Toast.LENGTH_SHORT).show();
    }

    /**
     * 跳转至网络连接设置界面
     * @param activity 用于跳转
     */
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

    /**
     * 初始化等待图标
     */
    private void initWaitView(){
        waitDialog = new WaitDialog(context,R.style.WaitDialog_white)
                .setWindow_paras(booklist.getWidth(),booklist.getHeight())
                .setWindowPOS((int) booklist.getX(),(int) booklist.getY())
                .setBackground_alpha(0.8f)
                .setLoadCircleColor(context,R.color.blue_gray)
                .setTitleColor(context,R.color.blue_gray);
    }

    private NovelSearchBean current_book;
    public class onItemclick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            current_book=BookList.get(position);
            Log.d("novelsearch",current_book.toString());

            initWaitView();
            if (loadView.getVisibility()!=View.GONE)
                loadView.setVisibility(View.GONE);
            //判断是否存在目录页
            NovelRequire novelRequire = novelRequireMap.get(current_book.getSource());
            String tocUrl = novelRequire.getRuleBookInfo().getTocUrl();
            if (tocUrl!=null){
                //存在目录页，获取其链接
                CatalogURLThread catalogURLThread = new CatalogURLThread(current_book.getBookInfoLink(),
                        novelRequire);
                catalogURLThread.setHandler(catalogUrl_Handler);
                catalogURLThread.start();
                waitDialog.setTitle("获取目录…");
            }else {
                //不存在额外目录页，默认使用书籍详情页链接作为目录页链接
                current_book.setBookCatalogLink(current_book.getBookInfoLink());
                getContentURL(current_book);
            }
            waitDialog.show();

        }
    }

    //跳转至novelshow界面
    private void launchNovelShow(NovelCatalog newChapter) {
        Intent intent=new Intent(NovelSearchActivity.this, NovelShowAcitivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("currentChap",newChapter);
        bundle.putSerializable("currentBook",current_book);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 获取选中的书籍的章节内容链接
     */
    private void getContentURL(final NovelSearchBean current_book){
        NovelDBTools novelDBTools= ViewModelProviders.of(this).get(NovelDBTools.class);
        novelDBTools.QueryNovelsByName(current_book.getBookNameWithoutWriter(), new NovelDBTools.QueryResultListener() {
            @Override
            public void onQueryFinish(List<com.Z.NovelReader.NovelRoom.Novels> novels) {
                //根据目录链接获取章节内容链接
                ContentURLThread thread =new ContentURLThread(current_book.getBookCatalogLink(),current_book.getSource());
                //判断所点击的书籍是否已在书架内
                if (novels.size()!=0){
                    thread.setCurrentChapIndex(novels.get(0).getCurrentChap());
                    NovelShowAcitivity.setIsInShelf(true);
                    NovelShowAcitivity.setBook_id(novels.get(0).getId());
                }else{
                    thread.setCurrentChapIndex(0);
                    NovelShowAcitivity.setIsInShelf(false);
                }
                //启动线程
                thread.setContext(context);
                thread.setHandler(contentURL_handler);
                thread.start();
                waitDialog.setTitle("获取章节…");
            }
        });
    }


}
