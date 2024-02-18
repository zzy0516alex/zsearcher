package com.Z.NovelReader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.NovelSearchListAdapter;
import com.Z.NovelReader.Basic.BasicHandler;
import com.Z.NovelReader.NovelRoom.NovelDBLiveData;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Service.PrepareCatalogService;
import com.Z.NovelReader.Threads.CatalogURLThread;
import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Utils.ViberateControl;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.NovelSearchResult;
import com.Z.NovelReader.Objects.beans.SearchQuery;
import com.Z.NovelReader.views.Dialog.WaitDialog;
import com.Z.NovelReader.views.LayoutAnimationHelper;
import com.Z.NovelReader.views.PopupWindow.SearchResultSortSelector;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NovelSearchActivity extends AppCompatActivity {

    //views
    private EditText search_key;//书名关键词
    private Button search_start;
    private RecyclerView lv_book_search_result;
    private ImageButton imb_back;
    private ImageButton imb_sort_mode;
    private SearchResultSortSelector sortSelector;
    private NovelSearchListAdapter searchListAdapter;
    private RelativeLayout loadView;//搜索等待
    private WaitDialog waitDialog;//novel show载入等待

    //basic paras
    private String input;// search_key to string
    private boolean startsearch;
    private Context context;
    private Activity activity;
    private InputMethodManager manager;//软键盘设置
    private boolean first_create=true;
    private int prepare_counter = 0;
    private boolean canDoSearch = false;
    private boolean afterSort = false;//排序后事件令牌，被滚动列表动作消耗
    private int sort_type = NovelSearchResult.SORT_BY_ID;
    //datas
//    private List<String>Novels;
//    private List<String> Links;
    private NovelSearchResult novelSearchResult;//搜书结果view model，用于在listview中显示
    private ArrayList<NovelSearchBean> oldBookList;//搜书原始结果，更新前
    private ArrayList<NovelSearchBean> newBookList;//搜书原始结果，更新后
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    private List<SearchQuery> searchQueryList;//书源中的书籍搜索规则列表
    private Map<Integer, NovelRequire> novelRequireMap;//书源ID-规则对应表

    //handlers
    private NovelSearchThread.NovelSearchHandler novelSearchHandler;//处理书籍搜索结果
    private BasicHandler<MapElement> catalogUrl_Handler;//处理额外的目录页链接获取结果

    //service
    private PrepareCatalogService service;
    private ServiceConnection connection;
    private Intent service_intent;

    //consts
    private static final int PREPARE_STEPS = 2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_search);
        //init views
        search_start =findViewById(R.id.search);
        lv_book_search_result =findViewById(R.id.book_search_list);
        loadView = findViewById(R.id.Load);
        imb_back =findViewById(R.id.back);
        imb_sort_mode = findViewById(R.id.sort_mode_select);

        //init 基本变量
//        Novels=new ArrayList<>();
//        Links =new ArrayList<>();
        newBookList = new ArrayList<>();
        oldBookList = new ArrayList<>();
        context = this;
        activity = NovelSearchActivity.this;
        sourceDBTools=new NovelSourceDBTools(context);

        //初始化排序选择菜单
        initSortSelector();

        //初始化输入框
        initSearchKeyInput();

        //观测书籍搜索结果
        observeSearchResults();

        //初始化书源列表
        prepareRequiredData();

        loadView.setVisibility(View.GONE);//隐藏载入等待图标

        //初始化 handler
        init_CatalogURLHandler();
        initService();

        //通过搜索按钮开始搜索
        search_start.setOnClickListener(v -> search_btn_down(activity));

        imb_back.setOnClickListener(v -> {
            ViberateControl.Vibrate(activity,15);
            activity.onBackPressed();
        });
        imb_sort_mode.setOnClickListener(v -> {
            if (!novelSearchResult.isEmpty())sortSelector.show(imb_sort_mode);
            else Toast.makeText(context, "排序功能在有搜索结果后可用", Toast.LENGTH_SHORT).show();
        });

        //通过键盘enter键开始搜索
        enter_key_down(activity);

        Log.d("NovelSearch","UI thread"+Thread.currentThread());

    }

    private void initSortSelector() {
        sortSelector = new SearchResultSortSelector(context,NovelSearchResult.SORT_BY_ID);
        sortSelector.setListener((type, is_reversed) -> {
            afterSort = true;
            this.sort_type = type;
            novelSearchResult.sortBy(type,is_reversed);
        });
    }

    private void prepareRequiredData() {
        sourceDBTools.getSearchUrlList(object -> {
            searchQueryList = (List<SearchQuery>) object;
            checkPrepareReady();
        });
        sourceDBTools.getNovelRequireMap(object -> {
            if (object instanceof Map)
                novelRequireMap = (Map<Integer, NovelRequire>) object;
            checkPrepareReady();
        });
    }

    private void initSearchKeyInput() {
        search_key =findViewById(R.id.input);
        search_key.requestFocus();
        manager= (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private void init_CatalogURLHandler(){
        catalogUrl_Handler = new BasicHandler<>(
                new BasicHandler.BasicHandlerListener<MapElement>() {
            @Override
            public void onSuccess(MapElement element) {
                if (current_book!=null) {
                    current_book.setBookCatalogLink((String) element.value);
                    startPrepareCatalogService(current_book);
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
                    case CatalogURLThread.TARGET_NOT_FOUND:
                        Toast.makeText(context, "未获取到目录链接,ID:"+current_book.getSource(), Toast.LENGTH_SHORT).show();
                        break;
                    case CatalogURLThread.NO_INTERNET:
                        Toast.makeText(context, "无网络", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        });
    }

    private boolean first_load_data = true;
    /**
     * 获取书籍搜索结果
     */
    private void observeSearchResults() {
        novelSearchResult= new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(NovelSearchResult.class);
        novelSearchResult.getMySearchResult().observe(this, bookList -> {
            if (bookList.size()!=0 && (!first_create)){
                oldBookList = new ArrayList<>(newBookList);
                newBookList = new ArrayList<>(bookList);
//                Novels.clear();
//                Links.clear();
                for (NovelSearchBean novelSearchBean : newBookList) {
                    NovelRequire novelRequire = novelRequireMap.get(novelSearchBean.getSource());
                    if (novelRequire==null)continue;
                    novelSearchBean.setNovelRule(novelRequire);
                    novelSearchBean.setResultScore(StringUtils.compareStrings(novelSearchBean.getBookNameWithoutWriter(),input));
                }
                if (first_load_data){
                    first_load_data = false;
                    playListAnimation(LayoutAnimationHelper.getAnimationSetFromBottom(),false);
                }
            }

            if (first_create){
                first_create=false;
                searchListAdapter = new NovelSearchListAdapter(context,oldBookList);
                searchListAdapter.setOnItemClickListener(new onItemClick());
                lv_book_search_result.setAdapter(searchListAdapter);
                lv_book_search_result.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            }else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffSearchResults(oldBookList,newBookList), true);
                searchListAdapter.setData(newBookList);
                result.dispatchUpdatesTo(searchListAdapter);
                if (afterSort) {
                    afterSort = false;//消耗令牌
                    lv_book_search_result.smoothScrollToPosition(0);
                }
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        loadView.setVisibility(View.GONE);
        if (novelRequireMap==null || novelRequireMap.isEmpty() || searchQueryList==null || searchQueryList.isEmpty()){
            prepare_counter = 0;canDoSearch = false;
            prepareRequiredData();
        }
    }

    private void checkPrepareReady(){
        synchronized (this){
            if (prepare_counter >= PREPARE_STEPS){
                prepare_counter = 0;
                canDoSearch = false;
            }
            prepare_counter++;
            if (prepare_counter == PREPARE_STEPS)canDoSearch = true;
        }
    }

    /**
     * 搜索初始化及启动搜书线程
     * @param activity 用于从新载入搜索界面
     */
    private void search_btn_down(Activity activity) {
        if (!canDoSearch){
            Toast.makeText(activity, "请稍侯", Toast.LENGTH_SHORT).show();
            return;
        }
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
            if (searchListAdapter!=null){
                oldBookList.clear();
                newBookList.clear();
                first_load_data = true;
//                Novels.clear();
//                Links.clear();
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

        if (searchQueryList.size()!=0 && novelRequireMap.size()!=0) {
            //init search handler
            novelSearchHandler=new NovelSearchThread.NovelSearchHandler(searchQueryList.size(),
                    new NovelSearchThread.NovelSearchListener() {
                        @Override
                        public void onSearchResult(ArrayList<NovelSearchBean> search_result) {
                            novelSearchResult.addToResult(search_result);
                        }
                        @Override
                        public void onSearchFinish(int total_num, int num_no_internet, int num_not_found) {
                            if (loadView.getVisibility()!=View.GONE)
                                loadView.setVisibility(View.GONE);
                            Log.d("Novel Search", "onSearchFinish: not found num = "+num_not_found);
                            search_key.setCursorVisible(true);
                            novelSearchHandler.clearAllCounters();
                            if (num_not_found==total_num)
                                Toast.makeText(context, "未找到", Toast.LENGTH_SHORT).show();
                            if (num_no_internet==total_num)
                                launch_internet_setting(activity);
                            afterSort = true;
                            novelSearchResult.sortBy(sort_type,false);
                        }

                    });

            //start search thread
            for (SearchQuery search_query : searchQueryList) {
                NovelSearchThread T = new NovelSearchThread(novelRequireMap.get(search_query.getId()),search_query,input);
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
                .setPositiveButton("设置", (dialog, which) -> {
                    Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.putExtra("extra_prefs_show_button_bar", true);//是否显示button bar
                    intent.putExtra("extra_prefs_set_next_text", "完成");
                    intent.putExtra("extra_prefs_set_back_text", "返回");
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> restartActivity(activity)).setCancelable(false).show();
    }

    /**
     * 初始化等待图标
     */
    private void initWaitView(){
        waitDialog = new WaitDialog(context,R.style.WaitDialog_white)
                .setWindow_paras(lv_book_search_result.getWidth(), lv_book_search_result.getHeight())
                .setWindowPOS((int) lv_book_search_result.getX(),(int) lv_book_search_result.getY())
                .setBackground_alpha(0.8f)
                .setLoadCircleColor(context,R.color.blue_gray)
                .setTitleColor(context,R.color.blue_gray);
    }

    public void playListAnimation(Animation animation, boolean isReverse) {
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setDelay(0.1f);
        controller.setOrder(isReverse ? LayoutAnimationController.ORDER_REVERSE : LayoutAnimationController.ORDER_NORMAL);

        lv_book_search_result.setLayoutAnimation(controller);
        //searchListAdapter.notifyDataSetChanged();
        lv_book_search_result.scheduleLayoutAnimation();
    }


    private NovelSearchBean current_book;
    private NovelRequire current_novelRequire;
    private void initService(){
        service_intent = new Intent(NovelSearchActivity.this,PrepareCatalogService.class);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PrepareCatalogService.PrepareCatalogServiceBinder binder = (PrepareCatalogService.PrepareCatalogServiceBinder) iBinder;
                service = binder.getService();
                service.setListener(new PrepareCatalogService.CatalogServiceListener() {
                    @Override
                    public void onError(String info,Novels target) {
                        runOnUiThread(() -> {
                            waitDialog.dismiss();
                            Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onProcessStart(String process_name) {
                        runOnUiThread(()->{
                            waitDialog.setTitle(process_name);
                        });
                    }

                    @Override
                    public void onFirstChapReady(NovelCatalog chap) {
                        runOnUiThread(()->{
                            waitDialog.dismiss();
                        });
                        launchNovelShow(chap);
                    }

                    @Override
                    public void onAllProcessDone(com.Z.NovelReader.NovelRoom.Novels novel) {

                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(service_intent, connection, BIND_AUTO_CREATE);
    }

    public class onItemClick implements NovelSearchListAdapter.OnItemClickListener{

        @Override
        public void onItemClick(View view, int position) {
            if (position==-1)return;
            current_book = newBookList.get(position);
            Log.d("novelsearch",current_book.toString());

            initWaitView();
            if (loadView.getVisibility()!=View.GONE)
                loadView.setVisibility(View.GONE);

            current_novelRequire = novelRequireMap.get(current_book.getSource());
            if (current_novelRequire==null){
                Toast.makeText(context, "未获取到书源信息", Toast.LENGTH_SHORT).show();
                return;
            }
            current_book.setNovelRule(current_novelRequire);
            String tocUrl = "";
            if (current_novelRequire.getRuleBookInfo()!=null)
                tocUrl = current_novelRequire.getRuleBookInfo().getTocUrl();
            waitDialog.setTitle("搜索目录…");
            waitDialog.show();

            if (tocUrl!=null && !"".equals(tocUrl)){
                //存在目录页，获取其链接
                CatalogURLThread catalogURLThread = new CatalogURLThread(current_book.getBookInfoLink(),
                        current_novelRequire, null);
                catalogURLThread.setHandler(catalogUrl_Handler);
                catalogURLThread.start();
            }else {
                //不存在额外目录页，默认使用书籍详情页链接作为目录页链接
                current_book.setBookCatalogLink(current_book.getBookInfoLink());
                startPrepareCatalogService(current_book);
            }
        }
    }

    //跳转至novelshow界面
    private void launchNovelShow(final NovelCatalog newChapter) {
        NovelDBTools novelDBTools = new NovelDBTools(context);
        novelDBTools.queryNovelsByNameAndWriter(current_book.getBookNameWithoutWriter(), current_book.getWriter(),
                novels -> {
                    //判断所点击的书籍是否已在书架内
                    if (novels.size()!=0){
                        //已在书架内
                        Toast.makeText(context, "该书已在书架内，自动转到书架阅读页", Toast.LENGTH_SHORT).show();
                        Novels current_book = novels.get(0);
                        //读取章节缓存
                        String content= FileIOUtils.readContent(
                                StorageUtils.getBookContentPath(current_book.getBookName(),current_book.getWriter()));
                        //获取当前目录
                        NovelCatalog catalog= null;
                        try {
                            catalog = FileIOUtils.readCatalog(
                                    StorageUtils.getBookCatalogPath(current_book.getBookName(),current_book.getWriter()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "书籍目录丢失", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //获取当前书源规则
                        NovelRequire current_rule = null;
                        if (novelRequireMap!=null)
                            current_rule = novelRequireMap.get(current_book.getSource());
                        if (!catalog.isEmpty() && current_rule!=null) {
                            Log.d("book shelf", current_book.getBookName() + ":目录打开成功");
                            BookShelfActivity.startReadPage(context, content, current_book, catalog, current_rule);
                        }else {
                            Toast.makeText(context, "书籍信息丢失", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Intent intent=new Intent(NovelSearchActivity.this, NovelShowAcitivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("currentChap",newChapter);
                        bundle.putSerializable("currentBook",current_book);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
    }

    private void startPrepareCatalogService(final NovelSearchBean current_book){
        service_intent.putExtra("NovelSearch",current_book);
        service_intent.putExtra("catalogPath",StorageUtils.getTempCatalogPath());
        service_intent.putExtra("catalogLinkPath",StorageUtils.getTempCatalogLinkPath());
        startService(service_intent);
    }

    private class DiffSearchResults extends DiffUtil.Callback {
        private ArrayList<NovelSearchBean> old_list, new_list;

        DiffSearchResults(ArrayList<NovelSearchBean> o, ArrayList<NovelSearchBean> n) {
            this.old_list = o;
            this.new_list = n;
        }

        @Override
        public int getOldListSize() {
            return old_list.size();
        }

        @Override
        public int getNewListSize() {
            return new_list.size();
        }

        // 判断Item是否已经存在
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String old_data = old_list.get(oldItemPosition).getBookInfoLink();
            String new_data = new_list.get(newItemPosition).getBookInfoLink();
            return old_data.equals(new_data);
        }

        // 如果Item已经存在则会调用此方法，判断Item的内容是否一致
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            String old_data = old_list.get(oldItemPosition).getBookInfoLink();
            String new_data = new_list.get(newItemPosition).getBookInfoLink();
            return old_data.equals(new_data);
        }
    }
}
