package com.Z.NovelReader;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.BooklistAdapter;
import com.Z.NovelReader.Adapters.NovelViewAdapter;
import com.Z.NovelReader.Dialog.ViewerSettingDialog;
import com.Z.NovelReader.Fragments.NovelViewBasicFragment;
import com.Z.NovelReader.Fragments.NovelViewFragment;
import com.Z.NovelReader.Global.OnReadingListener;
import com.Z.NovelReader.Global.OnSettingChangeListener;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Threads.NovelSearchThread;
import com.Z.NovelReader.Threads.NovelUpdateThread;
import com.Z.NovelReader.Utils.Brightness;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.ViberateControl;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.NovelChap;

import java.util.ArrayList;
import java.util.List;

import static com.Z.NovelReader.Adapters.NovelViewAdapter.DNMod;
import static com.Z.NovelReader.Adapters.NovelViewAdapter.DNMod.DAY_MOD;
import static com.Z.NovelReader.Adapters.NovelViewAdapter.DNMod.NIGHT_MOD;

public class NovelViewerActivity extends AppCompatActivity implements OnReadingListener {

    private static final String TAG = "NovelViewerActivity";
    private static NovelChap current_chap;
    private NovelCatalog catalog;
    private NovelViewFragment fragment;
    private Activity activity;
    private Window window;
    NovelDBTools novelDBTools;
    private OnSettingChangeListener settingChangeListener;
    private BooklistAdapter booklistAdapter;
    private LinearLayout BottomToolBar;
    private LinearLayout TopToolBar;
    private ImageButton BackToShelf;
    private ImageButton btnCatalog;
    private ImageButton btnSettings;
    private ImageButton Mod_btn;
    private ImageButton Refresh;
    private TextView showBookName;
    private DrawerLayout drawerLayout;
    private ListView catalogList;
    private ViewerSettingDialog settingDialog;
    private SharedPreferences myInfo;
    private DNMod currentMod;
    private NovelUpdateThread.NovelUpdaterHandler<Activity> chap_update_handler;
    private NovelDBTools.QueryResultListener queryResultListener;
    private int currentChapIndex;
    private int myTextSize;
    private boolean is_toolbar_visible=false;
    private boolean is_scroll_stop=true;
    private String BookName;
    private String BookLink;
    private NovelSearchThread.TAG BookTag;
    private int BookID;
    private int page_offset;


    public static void setCurrent_chap(NovelChap chap) {
        NovelViewerActivity.current_chap = chap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_viewer);

        init_BasicParams();

        if (savedInstanceState==null){
            init_chapParams();
            startFragment();
        }
        else {
            //TODO:recover
            startRecover();
        }

    }

    private void startRecover() {
        SharedPreferences recoverInfo= activity.getSharedPreferences("recoverInfo", Context.MODE_PRIVATE);
        if (recoverInfo.getBoolean("onFront",false)){
            String recover_book_name=recoverInfo.getString("BookName","");
            if (!"".equals(recover_book_name)){
                novelDBTools.QueryNovelsByName(recover_book_name, queryResultListener);
            }else finish();
        }else finish();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        settingChangeListener= (OnSettingChangeListener) fragment;
        NovelViewBasicFragment recovered_fragment= (NovelViewBasicFragment) fragment;
        if (!"OK".equals(recovered_fragment.getFragmentTag())){
            //notice error start up
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            Log.e(TAG, "onAttachFragment: error start up");
        }else {
            initStatusBar();
            init_TopToolBar();
            init_BottomToolBar();
        }
        super.onAttachFragment(fragment);
    }

    private void init_BasicParams() {
        activity=this;
        myInfo=activity.getSharedPreferences("UserInfo",MODE_PRIVATE);
        novelDBTools = ViewModelProviders.of(this).get(NovelDBTools.class);
        BookLink=getIntent().getStringExtra("BookLink");
        page_offset=getIntent().getIntExtra("offset",3);
        init_QueryResultListener();
        init_currentMod();
    }

    private void init_QueryResultListener() {
        queryResultListener=new NovelDBTools.QueryResultListener() {
            @Override
            public void onQueryFinish(List<Novels> novels) {
                if (novels.size()!=0) {
                    recoverCurrentChap(novels);
                    init_chapParams();
                    startFragment();
                }else finish();
            }
        };
    }

    private void recoverCurrentChap(List<Novels> novels) {
        Novels recover_novel = novels.get(0);
        String content= FileIOUtils.read_line(recover_novel.getBookName(),getExternalFilesDir(null));
        catalog= FileIOUtils.read_catalog("/ZsearchRes/BookContents/" + recover_novel.getBookName() + "_catalog.txt",
                getExternalFilesDir(null));
        current_chap= BookShelfActivity.getNovelChap(content,
                BookShelfActivity.getCurrentChapLink(
                        recover_novel.getCurrentChap(),catalog.getTitle(),catalog.getLink()));
        current_chap.setBookID(recover_novel.getId());
        current_chap.setBookName(recover_novel.getBookName());
        current_chap.setCurrent_chapter(recover_novel.getCurrentChap());
        //notice need update
        //current_chap.setTag(recover_novel.getTag_in_TAG());
        page_offset=recover_novel.getOffset();
    }

    private void init_chapParams() {
        catalog= FileIOUtils.read_catalog("/ZsearchRes/BookContents/" + current_chap.getBookName() + "_catalog.txt",
                getExternalFilesDir(null));
        BookName=current_chap.getBookName();
        BookTag=current_chap.getTag();
        BookID=current_chap.getBookID();
    }

    private void init_Refresh() {
        Refresh = findViewById(R.id.refresh);
        init_chapUpdateHandler();
        //目录刷新点击
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(activity, R.anim.rotate);
                Refresh.startAnimation(animation);
                //notice need update
//                CatalogThread catalogThread=new CatalogThread(BookLink,BookTag,true,false);
//                catalogThread.setOutputParams(BookName,getExternalFilesDir(null));
//                catalogThread.setHandler(chap_update_handler);
//                catalogThread.start();
            }
        });
    }

    private void init_chapUpdateHandler() {
        chap_update_handler=new NovelUpdateThread.NovelUpdaterHandler<>(activity);
        chap_update_handler.setOverride(new NovelUpdateThread.NovelUpdaterHandler.NovelUpdateListener() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                if (Success==1){
                    catalog= FileIOUtils.read_catalog("/ZsearchRes/BookContents/" + BookName + "_catalog.txt",getExternalFilesDir(null));
                    booklistAdapter.updateList(catalog.getTitle());
                    settingChangeListener.onCatalogUpdate(catalog);
                    Refresh.clearAnimation();
                    Toast.makeText(activity, "章节同步完成", Toast.LENGTH_SHORT).show();
                }else{
                    Refresh.clearAnimation();
                    Toast.makeText(activity, "章节同步出错", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initStatusBar() {
        //初始化状态栏
        window= getWindow();
        //window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        StatusBarLightTheme();
    }

    private void initSettings() {
        //初始化设置界面参数
        ViewerSettingDialog.isFollowing=true;
        //brightness=Brightness.getSystemBrightness(activity.getContentResolver());
        myTextSize=myInfo.getInt("myTextSize",20);
    }

    private void init_SettingBtn() {
        btnSettings=findViewById(R.id.settings_of_novelview);
        initSettings();
        //打开设置
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingDialog=new ViewerSettingDialog(activity,R.style.NoDimDialog);
                settingDialog
                        .setMyTextSize(myTextSize)
                        .setBrightness(Brightness.getSystemBrightness(activity.getContentResolver()))
                        .setAdjusterTextSize(new ViewerSettingDialog.AdjustTextSize() {

                            @Override
                            public void onChanging(int progress) {
                                myTextSize=progress;
                                settingChangeListener.onTextSizeChange(myTextSize);
                            }

                            @Override
                            public void changeDone() {
                                SharedPreferences.Editor editor=myInfo.edit();
                                editor.putInt("myTextSize",myTextSize);
                                editor.apply();
                            }
                        })
                        .show();
                SetToolBarInvisible();
            }
        });
    }

    private void StatusBarLightTheme() {
        window.setStatusBarColor(getResources().getColor(R.color.comfortGreen));
        StatusBarUtil.setStatusBarDarkTheme(activity,true);
    }
    private void StatusBarDarkTheme() {
        window.setStatusBarColor(getResources().getColor(R.color.night_background));
        StatusBarUtil.setStatusBarDarkTheme(activity,false);
    }
    private void StatusBarDeepBlue() {
        window.setStatusBarColor(getResources().getColor(R.color.deepBlue));
        StatusBarUtil.setStatusBarDarkTheme(activity,false);
    }

    private void SetToolBarInvisible() {
        if (is_toolbar_visible) {
            BottomToolBar.setVisibility(View.INVISIBLE);
            TopToolBar.setVisibility(View.INVISIBLE);
            if (currentMod == NovelViewAdapter.DNMod.DAY_MOD) StatusBarLightTheme();
            else StatusBarDarkTheme();
            is_toolbar_visible = false;
        }
    }

    private void init_TopToolBar() {
        TopToolBar=findViewById(R.id.top_tool_bar);
        TopToolBar.setAlpha(0.99f);
        TopToolBar.setVisibility(View.INVISIBLE);

        init_BackToShelf();
        init_showBookName();
        init_Mod_btn();
    }

    private void init_BottomToolBar() {
        BottomToolBar=findViewById(R.id.bottom_tool_bar);
        BottomToolBar.setAlpha(0.97f);
        BottomToolBar.setVisibility(View.INVISIBLE);

        init_catalogList();
        init_SettingBtn();
    }

    private void init_catalogList() {
        btnCatalog=findViewById(R.id.catalog_of_novelview);
        drawerLayout=findViewById(R.id.drawer_layout);
        catalogList=findViewById(R.id.catalog_list);
        //初始化目录
        booklistAdapter=new BooklistAdapter(catalog.getTitle(),activity,true,current_chap.getTitle());
        booklistAdapter.setText_size(18);
        currentChapIndex=current_chap.getCurrent_chapter();
        //打开目录
        btnCatalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catalogList.setAdapter(booklistAdapter);
                drawerLayout.openDrawer(findViewById(R.id.left_layout));
                if (currentChapIndex >7)catalogList.setSelection(currentChapIndex -6);
                else catalogList.setSelection(currentChapIndex);
            }
        });
        //目录点击
        catalogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                settingChangeListener.onCatalogItemClick(position);
            }
        });
        init_Refresh();
    }

    private void init_Mod_btn() {
        //日夜间初始化
        Mod_btn = findViewById(R.id.day_night_switch);
        switch(currentMod){
            case DAY_MOD:switch_to_dayMod();
                break;
            case NIGHT_MOD:switch_to_nightMod();
                break;
            default:
        }
        //模式切换
        Mod_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(currentMod){
                    case DAY_MOD:
                    {
                        //当前日间模式，点击切换为夜间
                        currentMod= NIGHT_MOD;

                        switch_to_nightMod();

                        settingChangeListener.onDNModChange(NIGHT_MOD);
                    }
                    break;
                    case NIGHT_MOD:
                    {
                        //当前夜间模式，点击切换为日间
                        currentMod= DAY_MOD;

                        switch_to_dayMod();

                        settingChangeListener.onDNModChange(DAY_MOD);
                    }
                    break;
                    default:
                }
                saveDayNightMod();
            }
        });
    }

    private void switch_to_dayMod() {
        ViewerSettingDialog.isFollowing=true;
        Mod_btn.setImageResource(R.drawable.day_mod);
        if (!is_toolbar_visible)StatusBarLightTheme();
        Brightness.startAutoBrightness(activity);
    }

    private void switch_to_nightMod() {
        ViewerSettingDialog.isFollowing=false;
        Mod_btn.setImageResource(R.drawable.night_mod);
        if (!is_toolbar_visible)StatusBarDarkTheme();
        Brightness.stopAutoBrightness(activity);
        //brightness=60;
        Brightness.setSystemScreenBrightness(activity,60);
    }

    private void init_currentMod() {
        //初始化日夜模式
        int myDNMod=0;
        myDNMod=myInfo.getInt("myDNMod",0);
        switch(myDNMod){
            case 0:
                currentMod= DNMod.DAY_MOD;
                break;
            case 1:
                currentMod= DNMod.NIGHT_MOD;
                break;
            default:
        }
    }

    private void saveDayNightMod() {
        if (myInfo!=null) {
            SharedPreferences.Editor editor = myInfo.edit();
            int myDNMod = 0;
            switch (currentMod) {
                case DAY_MOD:
                    myDNMod = 0;
                    break;
                case NIGHT_MOD:
                    myDNMod = 1;
                    break;
                default:
            }
            editor.putInt("myDNMod", myDNMod);
            editor.apply();
        }
    }

    private void init_showBookName() {
        //显示书名
        showBookName=findViewById(R.id.show_book_name);
        showBookName.setText(current_chap.getBookName());
    }

    private void init_BackToShelf() {
        //返回书架
        BackToShelf=findViewById(R.id.back_to_shelf);
        BackToShelf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                ViberateControl.Vibrate(activity,15);
                activity.onBackPressed();
            }
        });
    }

    private void startFragment() {
        ArrayList<NovelChap> novelChap =new ArrayList<>();
        novelChap.add(current_chap);
        Intent intent=getIntent();
        fragment=new NovelViewFragment();
        fragment.setFragmentTag("OK");
        fragment.setOffset(page_offset);
        fragment.setBookLink(BookLink);
        fragment.setChapList(novelChap);
        fragment.setBookID(BookID);
        fragment.setBookName(BookName);
        fragment.setBookTag(BookTag);
        fragment.setCatalog(catalog);
        fragment.setDir(getExternalFilesDir(null));
        fragment.setCurrentMod(currentMod);
        getSupportFragmentManager().beginTransaction().add(R.id.novel_view_container,fragment).commitAllowingStateLoss();
    }

    @Override
    public void onScreenTouch(float x, float y) {
        float clickableArea_top=TopToolBar.getLayoutParams().height;
        float clickableArea_bottom= ScreenUtils.getScreenHeight(activity)-BottomToolBar.getLayoutParams().height;
        if (is_scroll_stop){
            if (!is_toolbar_visible) {
                BottomToolBar.setVisibility(View.VISIBLE);
                TopToolBar.setVisibility(View.VISIBLE);
                is_toolbar_visible = true;
                StatusBarDeepBlue();
            }else if (y>clickableArea_top && y<clickableArea_bottom){
                SetToolBarInvisible();
            }
        }
    }

    @Override
    public void onScroll(boolean isStop) {
        is_scroll_stop=isStop;
        if (!isStop)SetToolBarInvisible();
    }

    @Override
    public void onReadNewChap(int chapIndex) {
        currentChapIndex=chapIndex;
        booklistAdapter.setItem_to_paint(catalog.getTitle().get(chapIndex));
    }

    @Override
    public void onJumpToNewChap(NovelChap newChap) {
        currentChapIndex=newChap.getCurrent_chapter();
        booklistAdapter.setItem_to_paint(newChap.getTitle());
        if (drawerLayout.isDrawerOpen(findViewById(R.id.left_layout)))drawerLayout.closeDrawer(findViewById(R.id.left_layout));
        SetToolBarInvisible();
    }

}
