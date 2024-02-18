package com.Z.NovelReader;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.Z.NovelReader.Basic.BasicUpdaterBroadcast;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Processors.OpenEntities.CustomDataHolder;
import com.Z.NovelReader.Service.AlterSourceService;
import com.Z.NovelReader.Threads.ContentThread;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.FileOperateUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.views.Dialog.SweetDialog.SweetAlertDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static java.lang.Thread.sleep;

public class NovelShowAcitivity extends AppCompatActivity {
    public Activity Novalshow;
    private WebView webView;
    private SeekBar seekBar;
    private Button past;
    private Button next;
    private Button catalog;
    private ImageButton web_refresh;
    private ImageButton AddBook;
    //外部存储的数据源
    private SharedPreferences myInfo;//用户信息（字体、书籍数量等）
    private NovelDBTools novelDBTools;//书籍数据库DAO
    private NovelRequire novelRequire;//书源规则类
    private NovelSourceDBTools sourceDBTools;//书源数据库DAO
    //用户设置
    private int myProgress;//字体大小
    private WebSettings webSettings;
    //章节信息 书本信息
    private String currentURL;//当前网页链接
    private String loadURL;//待加载的网页链接
    private int ttlChap;//总章节数
    private int currentChap=0;//当前章节索引
    private String nextUrl;
    private String pastUrl;
    private String catalogUrl;//书籍目录链接
    private String infoUrl;//书籍信息页链接
    private String contentRootUrl;//章节内容父链接
    private String currentTitle;//当前章节标题
    private String BookName;
    private String writer;
    private int sourceID;//书源编号
    //目录信息
    private List<String>ChapList;
    private List<String>ChapLinkList;
    //基本变量
    private boolean istouch=false;
    private boolean first_load=true;//首次载入，界面提示
    private boolean isFloatButtonShow=true;
    private static boolean isInShelf=false;
    private boolean isCatalogReady=false;//目录是否下载完成
    private static int book_id;//书架中的书号
    private Context context;
    //broad cast intent
    public static String CATALOG_BROADCAST = "Z.NovelShow.intent.CATALOG_READY";


    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_show_acitivity);
        Novalshow=this;
        //取出 bundle,初始化章节信息
        final Bundle bundle=this.getIntent().getExtras();
        assert bundle != null;
        NovelCatalog currentChap= (NovelCatalog) bundle.getSerializable("currentChap");
        NovelSearchBean currentBook= (NovelSearchBean) bundle.getSerializable("currentBook");
        if (currentChap!=null && currentBook!=null) {
            currentURL = currentChap.getLinkList().get(0);
            currentTitle = currentChap.getTitleList().get(0);
            catalogUrl = currentBook.getBookCatalogLink();
            infoUrl = currentBook.getBookInfoLink();
            sourceID = currentBook.getSource();
            BookName = currentBook.getBookNameWithoutWriter();
            writer = currentBook.getWriter();
        }else {
            Toast.makeText(this, "书籍初始化失败", Toast.LENGTH_SHORT).show();
            Log.d("novel show","书籍初始化失败");
            return;
        }

        context=this;

        //get book source
        sourceDBTools=new NovelSourceDBTools(context);
        sourceDBTools.getNovelRequireById(sourceID, false, object -> novelRequire = (NovelRequire) object);

        //get view
        webView=findViewById(R.id.novelpage);
        seekBar=findViewById(R.id.zoom);
        past=findViewById(R.id.past);
        next=findViewById(R.id.next);
        catalog=findViewById(R.id.catalog);
        web_refresh=findViewById(R.id.web_refresh);
        AddBook=findViewById(R.id.AddBook);

        //get preferences
        myInfo=super.getSharedPreferences("UserInfo",MODE_PRIVATE);

        myProgress=myInfo.getInt("Progress",10);

        //get database
        novelDBTools = new NovelDBTools(context);

        //initiate button
        next.setEnabled(false);
        past.setEnabled(false);

        catalog.setEnabled(false);
        next.setEnabled(false);
        //字体大小拖动条
        seekBar.setMax(150);
        seekBar.setProgress(myProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                webSettings.setTextZoom(progress+150);
                myProgress=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor=myInfo.edit();
                editor.putInt("Progress",myProgress);
                editor.apply();
            }
        });
        // 上一章/下一章按钮
        past.setOnClickListener(v -> loadURL(pastUrl));
        next.setOnClickListener(v -> loadURL(nextUrl));

        //目录按钮点击
        catalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //准备目录数据传入
                Intent intent=new Intent(NovelShowAcitivity.this,CatalogActivity.class);
                Bundle bundle_catalog=new Bundle();
                bundle_catalog.putString("currentTitle",currentTitle);
                intent.putExtras(bundle_catalog);
                startActivityForResult(intent,1);
            }
        });

        //设置浮标
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels - 50;
        AddBook.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int ea = event.getAction();
                switch (ea) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        istouch=true;
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        int l = v.getLeft() + dx;
                        int b = v.getBottom() + dy;
                        int r = v.getRight() + dx;
                        int t = v.getTop() + dy;
                        // 下面判断移动是否超出屏幕
                        if (l < 0) {
                            l = 0;
                            r = l + v.getWidth();
                        }
                        if (t < 0) {
                            t = 0;
                            b = t + v.getHeight();
                        }
                        if (r > screenWidth) {
                            r = screenWidth;
                            l = r - v.getWidth();
                        }
                        if (b > screenHeight) {
                            b = screenHeight;
                            t = b - v.getHeight();
                        }
                        v.layout(l, t, r, b);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        v.postInvalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }

        });
        AddBook.setEnabled(false);
        AddBook.setVisibility(View.INVISIBLE);
        AddBook.setOnClickListener(v -> {
            AddBook.setImageResource(R.mipmap.shelfclick);
            if(!istouch){
                IfAddToShelf();
            }
            if(istouch){
                istouch=false;
                AddBook.setImageResource(R.mipmap.addtoshelf);
            }
        });

        //网页加载与配置
        loadURL(currentURL);
        webSettings=webView.getSettings();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if(first_load) {
                    Toast.makeText(NovelShowAcitivity.this, "当前为预览模式，加入书架后可优化阅读", Toast.LENGTH_SHORT).show();
                }
                super.onPageFinished(view, url);
                //阅读新章节时：
                System.out.println("web_view load:"+url);
                if (!StringUtils.UrlStingCompare(url,loadURL)){
                    Log.d("web_view","发生重定向");
                    currentURL=loadURL;
                }else currentURL = url;

                if (!first_load)ChangeCurrentCondition();
                //duplicated：如果该书已在书架内则更新书架数据库信息
                //if (isInShelf)update_bookshelfINFO();
                first_load=false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //super.onReceivedSslError(view, handler, error);
                Log.e("novel show","onReceivedSslError sslError="+error.toString());
                if(error.getPrimaryError() == android.net.http.SslError.SSL_INVALID ){
                    handler.proceed();
                }else{
                    handler.cancel();
                }

            }

        });
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(myProgress+150);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webSettings.setDomStorageEnabled(true);

        //获取目录
        registerCatalogBroadcast();

        //刷新界面
        web_refresh.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(Novalshow, R.anim.rotate_limittime);
            web_refresh.startAnimation(animation);
            webView.reload();
        });

    }

    private void registerCatalogBroadcast() {
        BasicUpdaterBroadcast basicUpdaterBroadcast = new BasicUpdaterBroadcast(context) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BasicUpdaterBroadcast.CATALOG_NOVELSHOW)
                        && !isCatalogReady)
                    getCatalog();
            }
        };
        basicUpdaterBroadcast.register();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==1){
            String newURL="";
            assert data != null;
            Bundle bundle=data.getExtras();
            assert bundle != null;
            newURL=bundle.getString("url");
            loadURL(newURL);
        }
    }

    private void loadURL(String newURL) {
        webView.loadUrl(newURL);
        loadURL = newURL;
    }

    /**
     * 更新整个activity当前的阅读界面（数据）状态
     */
    private void ChangeCurrentCondition() {
        next.setEnabled(false);
        past.setEnabled(false);
        //更新current chap
        currentChap = ChapLinkList.indexOf(currentURL);
        //考虑list：http，current：https的情况
        if(currentChap==-1)currentChap=ChapLinkList.indexOf(currentURL.replace("https","http"));
        //考虑list：https，current：http的情况
        if (currentChap==-1)currentChap=ChapLinkList.indexOf(currentURL.replace("http","https"));
        try {
            if (currentChap!=-1)currentTitle = ChapList.get(currentChap);
            else {
                Toast.makeText(Novalshow, "更新当前章节时出错:定位章节失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            Toast.makeText(Novalshow, "更新当前章节时出错：索引溢出", Toast.LENGTH_SHORT).show();
            return;
        }
        PastAndNext();
        if (currentChap!=ChapLinkList.size()-1)next.setEnabled(true);
        if (currentChap!=0)past.setEnabled(true);
    }

    public void setFloatButtonShow(boolean floatButtonShow) {
        isFloatButtonShow = floatButtonShow;
    }

    public String getBookName() {
        return BookName;
    }

    /**
     * 弹出是否加入书架对话框
     * 将书籍加入数据库并保存当前章节内容
     */
    private void IfAddToShelf() {
        if(!isCatalogReady){
            AddBook.setImageResource(R.mipmap.addtoshelf);
            Toast.makeText(Novalshow, "请先等待目录加载完毕", Toast.LENGTH_SHORT).show();
            return;
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setTitleText("加入书架")
                .setContentText("加入书架后,可在首页点击书籍,进入优化阅读")
                .setConfirmButton("确认", sweetAlertDialog1->{
                    if (novelRequire!=null) {
                        AddBook.setEnabled(false);
                        AddBook.setVisibility(View.INVISIBLE);
                        setFloatButtonShow(false);
                        addToShelf();
                        sweetAlertDialog1.dismissWithAnimation();
                    }else Toast.makeText(Novalshow, "未查找到书源", Toast.LENGTH_SHORT).show();
                })
                .setCancelButton("取消", sweetAlertDialog1->{
                    AddBook.setImageResource(R.mipmap.addtoshelf);
                    sweetAlertDialog1.dismissWithAnimation();
                })
                .showCancelButton(true)
                .show();
    }

    private void addToShelf() {
        //插入数据库
        if (currentChap==-1)currentChap = 0;
        Novels novel = new Novels(BookName,writer,ttlChap,currentChap,catalogUrl,infoUrl,sourceID);
        novel.setUsed(true);
        novel.setProgress(Novels.DEFAULT_PROGRESS);
        novel.autoSetShelfHash();
        novelDBTools.insertNovels(result -> {
            //绑定书籍数据
            if(CustomDataHolder.tempParam!=null && CustomDataHolder.tempParam.getSourceID() != sourceID){
                Toast.makeText(context, "临时书籍数据出错，添加失败", Toast.LENGTH_SHORT).show();return;
            }
            CustomDataHolder.tempParam.setNovelID(result.getId());
            novelDBTools.operateNovelParams(NovelDBTools.DBMethods.Insert,CustomDataHolder.tempParam);
            CustomDataHolder.tempParam = null;
        }, novel);
        //启动换源服务
        Intent alter_source_intent = new Intent(NovelShowAcitivity.this, AlterSourceService.class);
        alter_source_intent.putExtra("Novel",novel);
        startService(alter_source_intent);
        //新建文件夹
        File Book_Folder = new File(StorageUtils.getBookStoragePath(BookName,writer));
        if (!Book_Folder.exists()) {
            Book_Folder.mkdir();
        }
        //获取封面图片
        String output_path = StorageUtils.getBookCoverPath(BookName,writer);
        GetCoverThread thread_cover = new GetCoverThread(novel,novelRequire,output_path);
        thread_cover.start();
        //获取当前章节文本
        ContentThread thread_content = new ContentThread(currentURL,novelRequire, novel,contentRootUrl);
        thread_content.setCatalogLinks(ChapLinkList);
        thread_content.setOutputToCache(StorageUtils.getBookContentPath(BookName,writer));
        thread_content.setUpdateRootURL(context);
        thread_content.start();
        //通过复制获取目录
        FileOperateUtils.copyFile(StorageUtils.getTempCatalogPath(),StorageUtils.getBookCatalogPath(BookName,writer));
        FileOperateUtils.copyFile(StorageUtils.getTempCatalogLinkPath(),StorageUtils.getBookCatalogLinkPath(BookName,writer));

        Toast.makeText(NovelShowAcitivity.this, "已放入书架", Toast.LENGTH_SHORT).show();
    }

    /**
     * 从临时目录中读取目录数据，初始化章节信息
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getCatalog() {
        Log.d("novel show","收到广播，开始初始化目录");
        //读取临时保存的目录
        NovelCatalog result_back = null;
        try {
            result_back = FileIOUtils.readCatalog(StorageUtils.getTempCatalogPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "目录读取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!result_back.isEmpty()) {
            ChapList = result_back.getTitleList();
            ChapLinkList = result_back.getLinkList();
            ttlChap = ChapList.size();

            if (ttlChap > 1)contentRootUrl = StringUtils.getSharedURL(
                    ChapLinkList.get(0),ChapLinkList.get(1));
            else contentRootUrl = StringUtils.getRootUrl(ChapLinkList.get(0));
            Log.d("novel show","content root url= "+contentRootUrl);
            ChangeCurrentCondition();
            catalog.setEnabled(true);
            AddBook.setEnabled(true);
            AddBook.setVisibility(View.VISIBLE);
            isCatalogReady=true;
        }else{
            Toast.makeText(this, "目录读取失败", Toast.LENGTH_SHORT).show();
            Log.d("novel show","目录读取失败");
        }
    }

    /**
     * 获取前后章节的链接
     */
    private void PastAndNext() {
        if (currentChap!=ChapLinkList.size()-1)nextUrl=ChapLinkList.get(currentChap+1);
        if (currentChap!=0)pastUrl=ChapLinkList.get(currentChap-1);
    }


}
