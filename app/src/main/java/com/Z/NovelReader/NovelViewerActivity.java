package com.Z.NovelReader;

import static com.Z.NovelReader.Fragments.NovelViewBasicFragment.DNMod.DAY_MOD;
import static com.Z.NovelReader.Fragments.NovelViewBasicFragment.DNMod.NIGHT_MOD;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Z.NovelReader.Adapters.CatalogListAdapter;
import com.Z.NovelReader.Basic.BasicHandler;
import com.Z.NovelReader.Basic.BasicUpdaterBroadcast;
import com.Z.NovelReader.Fragments.NovelViewFragmentFactory;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Service.AlterSourceService;
import com.Z.NovelReader.Threads.SwitchSourceThread;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Utils.ServiceUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.views.Dialog.SwitchSourceDialog;
import com.Z.NovelReader.views.Dialog.ViewerSettingDialog;
import com.Z.NovelReader.Fragments.NovelViewBasicFragment;
import com.Z.NovelReader.Global.OnReadingListener;
import com.Z.NovelReader.Global.OnSettingChangeListener;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Threads.NovelUpdateThread;
import com.Z.NovelReader.Utils.Brightness;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.ScreenUtils;
import com.Z.NovelReader.Utils.StatusBarUtil;
import com.Z.NovelReader.Utils.ViberateControl;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.views.Dialog.WaitDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NovelViewerActivity extends AppCompatActivity implements OnReadingListener {

    private static final String TAG = "NovelViewerActivity";
    //main resource
    private NovelChap CurrentChap;
    //views
    private NovelViewBasicFragment fragment;
    private CatalogListAdapter catalogListAdapter;
    private LinearLayout BottomToolBar;
    private LinearLayout TopToolBar;
    private ImageButton BackToShelf;
    private ImageButton btnCatalog;
    private ImageButton btnSettings;
    private ImageButton btnSwitchSource;
    private ImageButton Mod_btn;
    private ImageButton Refresh;
    private TextView showBookName;
    private DrawerLayout drawerLayout;
    private ListView catalogList;
    private ViewerSettingDialog settingDialog;
    private WaitDialog waitDialog;
    private SwitchSourceDialog switchSourceDialog;
    //Anims
    Animation action_bottom_up;
    Animation action_bottom_down;
    Animation action_top_up;
    Animation action_top_down;
    //handler & listener
    private NovelUpdateThread.NovelUpdaterHandler<Activity> catalog_update_handler;
    private OnSettingChangeListener settingChangeListener;
    //external data sources
    private SharedPreferences myInfo;
    private NovelDBTools novelDBTools;
    private NovelDBUpdater dbUpdater;
    //basic paras
    private Activity activity;
    private Context context;
    private Window window;
    private Intent service_intent;
    private ServiceConnection serviceConnection;
    private AlterSourceService service;
    //viewer paras
    //private int myTextSize;
    private boolean is_toolbar_visible=false;
    private boolean is_scroll_stop=true;
    private NovelViewBasicFragment.DNMod currentDNMode;
    private NovelViewFragmentFactory.ViewMode currentViewMode;
    //private int current_brightness = -1;
    //backup source params
    private ArrayList<BackupSourceBean> backupSourceList;
    //novel data
    private int currentChapIndex;//当前章节索引，用于绘制目录
    private NovelCatalog catalog;
    //novel source
    private Map<Integer,NovelRequire> novelSourceMap;
    private Map<Integer,Novels> novelBackupMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_viewer);

        init_BasicParams();

        if(savedInstanceState==null){
            startFragment();
        }else {
            startRenovate();
        }

        /** 后续步骤
         * @see NovelViewerActivity#onAttachFragment(Fragment)
         */
    }

    @Override
    protected void onPause() {
        super.onPause();
        Brightness.startAutoBrightness(activity);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Brightness.startAutoBrightness(activity);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //TODO:recover brightness
//        if (currentDNMode ==NIGHT_MOD){
//            Brightness.stopAutoBrightness(activity);
//            Brightness.setSystemScreenBrightness(activity,60);
//        }
    }

    /**
     * 初始化动画，toolbar隐藏显示动画
     */
    private void initAnimation() {
        action_bottom_up = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f);
        action_bottom_up.setDuration(200);
        action_bottom_up.setInterpolator(new DecelerateInterpolator());

        action_bottom_down = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        action_bottom_down.setDuration(200);
        action_bottom_down.setInterpolator(new AccelerateInterpolator());

        action_top_down = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f);
        action_top_down.setDuration(200);
        action_top_down.setInterpolator(new AccelerateInterpolator());

        action_top_up = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f);
        action_top_up.setDuration(200);
        action_top_up.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * 初始化等待界面
     */
    private void initWaitView(){
        waitDialog=new WaitDialog(context,R.style.WaitDialog_black)
                .setTitle("章节缓存中…");
    }

    /**
     * 活动被清除后更新current chap
     */
    private void startRenovate() {
        Log.d("novel viewer","renovate start");
        novelDBTools.QueryNovelsByID(CurrentChap.getId(),
                novels -> {
                    if (novels.size()!=0) {
                        renovateCurrentChap(novels);
                        startFragment();
                    }else finish();
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        settingChangeListener= (OnSettingChangeListener) fragment;
        NovelViewBasicFragment recovered_fragment= (NovelViewBasicFragment) fragment;
        if (!"OK".equals(recovered_fragment.getFragmentTag())){
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            Log.e(TAG, "onAttachFragment: activity been killed");
        }else {
            initAnimation();

            initWaitView();

            initStatusBar();

            init_TopToolBar();

            init_BottomToolBar();
        }
        super.onAttachFragment(fragment);
    }

    /**
     * 初始化用户数据和书籍章节信息
     */
    private void init_BasicParams() {
        activity = this;
        context = this;
        myInfo = activity.getSharedPreferences("UserInfo",MODE_PRIVATE);

        String initViewMode = myInfo.getString("myViewMode", "vertical");
        currentViewMode = NovelViewFragmentFactory.parseViewMode(initViewMode);

        novelDBTools = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(NovelDBTools.class);
        dbUpdater = new NovelDBUpdater(context);
        NovelSourceDBTools sourceDBTools=new NovelSourceDBTools(getApplicationContext());
        novelSourceMap = sourceDBTools.getNovelRequireMap();

        Bundle bundle = NovelViewerActivity.this.getIntent().getExtras();
        assert bundle != null;
        CurrentChap = (NovelChap) bundle.getSerializable("CurrentChap");

        novelBackupMap = new HashMap<>();
        novelDBTools.findSameBook(CurrentChap.getBookName(), CurrentChap.getWriter(), novels -> {
            for (Novels novel : novels) {
                novelBackupMap.put(novel.getSource(),novel);
            }
        });
        backupSourceList = new ArrayList<>();

        init_currentMod();
    }

    /**
     * 从外部更新当前章节信息
     * @param novels 数据库读取到的书籍信息
     */
    private void renovateCurrentChap(List<Novels> novels) {
        loadCatalog();
        Novels current_novel = novels.get(0);
        //读取缓存的章节
        String content= FileIOUtils.read_line(
                StorageUtils.getBookContentPath(current_novel.getBookName(),current_novel.getWriter()));

        CurrentChap.setCurrentChap(current_novel.getCurrentChap());
        CurrentChap.setProgress(current_novel.getProgress());
        String[] links = NovelChap.getCurrentChapLink(CurrentChap.getCurrentChap(), catalog);
        NovelChap.initNovelChap(CurrentChap,content,links);
    }

    /**
     * 加载目录
     */
    private void loadCatalog() {
        catalog= FileIOUtils.read_catalog(
                StorageUtils.getBookCatalogPath(CurrentChap.getBookName(),CurrentChap.getWriter()));
    }

    /**
     * 初始化目录刷新按钮
     */
    private void init_Refresh() {
        Refresh = findViewById(R.id.refresh);
        init_chapUpdateHandler();
        //目录刷新点击
        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(activity, R.anim.rotate);
                Refresh.startAnimation(animation);
                NovelUpdateThread updateThread = new NovelUpdateThread(CurrentChap.getNovelRequire(),
                        CurrentChap);
                updateThread.setHandler(catalog_update_handler);
                updateThread.setCatalogLinkCallback(context,BasicUpdaterBroadcast.CATALOGLINK_NOVELVIEWER);
                updateThread.start();
            }
        });
    }

    /**
     * 初始化目录刷新回调
     */
    private void init_chapUpdateHandler() {
        catalog_update_handler =new NovelUpdateThread.NovelUpdaterHandler<>(activity);
        catalog_update_handler.setOverride(new NovelUpdateThread.NovelUpdaterHandler.NovelUpdateListener() {
            @Override
            public void handle(Message msg, int Success, int Fail) {
                if (Success==1){
                    loadCatalog();
                    catalogListAdapter.updateList(catalog.getTitle());
                    settingChangeListener.onCatalogUpdate(catalog);
                    Refresh.clearAnimation();
                    Toast.makeText(activity, "章节同步完成", Toast.LENGTH_SHORT).show();
                }else{
                    Refresh.clearAnimation();
                    Toast.makeText(activity, "章节同步出错", Toast.LENGTH_SHORT).show();
                }
                /*if (msg.what == BasicHandlerThread.WAITING_KEY_FILES){
                    Log.d("novel viewer","目录更新出错，等待关键文件就绪");
                    waiting_key_files = true;
                }*/
            }

            @Override
            public void needRecoverAll(Novels novel) {
                Toast.makeText(activity, "目录文件损坏", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化状态栏
     */
    private void initStatusBar() {
        window= getWindow();
        StatusBarUtil.setStatusBarTransparent(window);
        StatusBarUtil.setStatusBarDarkTheme(activity,true);
    }

    private void initSettings() {
        //初始化设置界面参数

    }

    /**
     * 初始化设置：
     *      字体大小
     *      亮度调整
     */
    private void init_SettingBtn() {
        btnSettings=findViewById(R.id.settings_of_novelview);
        settingDialog = new ViewerSettingDialog(activity,R.style.NoDimDialog);
        settingDialog
                .setFollowSystemBrightness(true)
                .setBrightness(Brightness.getSystemBrightness(activity.getContentResolver()))
                .setChangeListener(settingChangeListener)
                .setParentListener(mode -> {
                    currentViewMode = mode;
                    shiftFragment();
                });
        //initSettings();
        //打开设置
        btnSettings.setOnClickListener(v -> {
            settingDialog.show();
            SetToolBarInvisible();
        });
    }

    /**
     * 隐藏上下toolbar
     */
    private void SetToolBarInvisible() {
        if (is_toolbar_visible) {
            BottomToolBar.startAnimation(action_bottom_down);
            BottomToolBar.setVisibility(View.INVISIBLE);
            TopToolBar.startAnimation(action_top_up);
            TopToolBar.setVisibility(View.INVISIBLE);

            if (currentDNMode == DAY_MOD)
                StatusBarUtil.setStatusBarDarkTheme(activity,true);
            else StatusBarUtil.setStatusBarDarkTheme(activity,false);
            is_toolbar_visible = false;
        }
    }

    /**
     * 初始化顶栏：
     *      返回书架按钮
     *      书名显示TextView
     *      日夜间模式切换按钮
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init_TopToolBar() {
        TopToolBar=findViewById(R.id.top_tool_bar);
        TopToolBar.setAlpha(0.99f);
        TopToolBar.setVisibility(View.INVISIBLE);

        init_BackToShelf();
        init_showBookName();
        init_Mod_btn();
    }

    /**
     * 初始化底栏：
     *      目录列表显示
     *      设置按钮
     */
    private void init_BottomToolBar() {
        BottomToolBar=findViewById(R.id.bottom_tool_bar);
        BottomToolBar.setAlpha(0.97f);
        BottomToolBar.setVisibility(View.INVISIBLE);

        init_catalogList();
        init_switchSourceList();
        init_SettingBtn();
    }

    private void init_switchSourceList() {
        backupSourceList.clear();
        btnSwitchSource = findViewById(R.id.switch_source_of_novelview);
        switchSourceDialog = new SwitchSourceDialog(context,R.style.NoDimDialog);
        switchSourceDialog.setListener(backupSource -> {
            //notice 换源
            //dbUpdater.updateAllUsedStatus(CurrentChap.getBookName(),CurrentChap.getWriter(),false);
            NovelRequire currentRule = novelSourceMap.get(backupSource.getSourceID());
            Novels currentNovel = novelBackupMap.get(backupSource.getSourceID());
            backupSource.setCurrent_novel(currentNovel);
            SwitchSourceThread switchSourceThread = new SwitchSourceThread(context,backupSource,currentRule,CurrentChap);
            switchSourceThread.setHandler(new BasicHandler<NovelChap>(
                    new BasicHandler.BasicHandlerListener<NovelChap>() {
                @Override
                public void onSuccess(NovelChap result) {
                    //先关闭原先使用的书源
                    dbUpdater.updateUsedStatus(CurrentChap.getId(),false);
                    //再替换上新书源,并启用
                    CurrentChap = result;
                    dbUpdater.updateUsedStatus(CurrentChap.getId(),true);
                    refreshFragment();
                    if (waitDialog!=null && waitDialog.isShowing()){
                        waitDialog.dismiss();
                    }
                }

                @Override
                public void onError(int error_code) {
                    System.out.println("error_code = " + error_code);
                    Toast.makeText(context, "换源失败", Toast.LENGTH_SHORT).show();
                    if (waitDialog!=null && waitDialog.isShowing())
                        waitDialog.dismiss();
                }
            }));
            switchSourceThread.start();
            if (drawerLayout.isDrawerOpen(findViewById(R.id.left_layout)))drawerLayout.closeDrawer(findViewById(R.id.left_layout));
            SetToolBarInvisible();
            if (switchSourceDialog!=null && switchSourceDialog.isShowing())
                switchSourceDialog.dismiss();
            if (waitDialog!=null && !waitDialog.isShowing()){
                waitDialog.setTitle("换源准备中…");
                waitDialog.show();
            }
        });
        boolean serviceRunning = ServiceUtils.isServiceRunning(context, AlterSourceService.class.getName());
        if (serviceRunning){
            bind_service();
        }else {
            //read backup source from file
            //String backup_path = "/ZsearchRes/BookReserve/" + CurrentChap.getBookName() + "/BackupSource";
            List<String> dirs = FileUtils.getAllDirNames(
                    StorageUtils.getBackupDir(CurrentChap.getBookName(),CurrentChap.getWriter()));
            for (String dir : dirs) {
                try {
                    int id = Integer.parseInt(dir);
                    NovelRequire novelRequire = novelSourceMap.get(id);
                    if (novelRequire == null) continue;
                    //String source_path = backup_path + "/" + dir;
                    NovelCatalog novelCatalog = FileIOUtils.read_catalog(
                            StorageUtils.getBackupSourceCatalogPath(CurrentChap.getBookName(),CurrentChap.getWriter(),id));
                    Bitmap cover = FileIOUtils.readBitmap(
                            StorageUtils.getBackupSourceCoverPath(CurrentChap.getBookName(),CurrentChap.getWriter(),id));
                    if (novelCatalog.getSize()!=0){
                        BackupSourceBean backupSourceBean = new BackupSourceBean(CurrentChap,novelRequire.getId(),novelRequire.getBookSourceName(),novelCatalog,(novelRequire.getId() == CurrentChap.getSource()));
                        backupSourceBean.setCoverBrief(cover);
                        backupSourceList.add(backupSourceBean);
                    }
                }catch (NumberFormatException e){
                    Log.e("NovelViewer","backup source file read error:dir name not int");
                }
            }
        }
        btnSwitchSource.setOnClickListener(view -> {
            //check chosen
            for (BackupSourceBean b : backupSourceList) {
                b.setChosen(b.getSourceID() == CurrentChap.getSource());
            }
            switchSourceDialog.setOwnerActivity(activity);
            switchSourceDialog.setCurrentChap(CurrentChap);
            switchSourceDialog.setBackupSourceList(backupSourceList);
            switchSourceDialog.show();
            SetToolBarInvisible();
        });
    }

    private void bind_service(){
        service_intent = new Intent(NovelViewerActivity.this,AlterSourceService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                AlterSourceService.AlterSourceServiceBinder binder = (AlterSourceService.AlterSourceServiceBinder) iBinder;
                service = binder.getService();
                if (service.isAllProcessDone()){
                    Map<NovelRequire, NovelCatalog> backupSourceMap = service.getBackupSourceMap();
                    setBackupSourceFromMap(backupSourceMap);
                }else {
                    service.setListener(backupSourceMap -> {
                        setBackupSourceFromMap(backupSourceMap);
                        runOnUiThread(()->{
                            if (switchSourceDialog!=null){
                                for (BackupSourceBean b : backupSourceList) {
                                    b.setChosen(b.getSourceID() == CurrentChap.getSource());
                                }
                                switchSourceDialog.setBackupSourceList(backupSourceList);
                            }
                        });
                    });
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(service_intent,serviceConnection,BIND_AUTO_CREATE);
    }

    private void setBackupSourceFromMap(Map<NovelRequire, NovelCatalog> backupSourceMap) {
        Iterator<Map.Entry<NovelRequire, NovelCatalog>> iterator = backupSourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NovelRequire, NovelCatalog> entry = iterator.next();
            NovelRequire novelRequire = entry.getKey();
            NovelCatalog novelCatalog = entry.getValue();
            Bitmap cover = FileIOUtils.readBitmap(StorageUtils.getBackupSourceCoverPath(CurrentChap.getBookName(),CurrentChap.getWriter(),novelRequire.getId()));
            BackupSourceBean backupSourceBean = new BackupSourceBean(CurrentChap, novelRequire.getId(), novelRequire.getBookSourceName(), novelCatalog, (novelRequire.getId() == CurrentChap.getSource()));
            backupSourceBean.setCoverBrief(cover);
            backupSourceList.add(backupSourceBean);
        }
    }

    /**
     * 初始化目录界面
     */
    private void init_catalogList() {
        btnCatalog=findViewById(R.id.catalog_of_novelview);
        drawerLayout=findViewById(R.id.rl_background_v);
        catalogList=findViewById(R.id.catalog_list);
        //初始化目录
        catalogListAdapter =new CatalogListAdapter(catalog.getTitle(),activity,true, CurrentChap.getTitle());
        catalogListAdapter.setText_size(18);
        currentChapIndex= CurrentChap.getCurrentChap();
        //打开目录
        btnCatalog.setOnClickListener(v -> {
            boolean isDarkTheme = (currentDNMode == NIGHT_MOD);
            catalogListAdapter.setDarkTheme(isDarkTheme);
            catalogList.setAdapter(catalogListAdapter);
            View catalog_view = findViewById(R.id.left_layout);
            catalog_view.setBackgroundColor(getResources().
                    getColor(isDarkTheme?R.color.night_background:R.color.white,null));
            TextView title = catalog_view.findViewById(R.id.title_catalog);
            title.setTextColor(getResources().
                    getColor(isDarkTheme?R.color.white:R.color.black,null));
            drawerLayout.openDrawer(findViewById(R.id.left_layout));
            if (currentChapIndex >7)catalogList.setSelection(currentChapIndex -6);
            else catalogList.setSelection(currentChapIndex);
        });
        //目录点击
        catalogList.setOnItemClickListener((parent, view, position, id) -> settingChangeListener.onCatalogItemClick(position));
        init_Refresh();
    }

    /**
     * 初始化日夜间模式按钮
     */
    private void init_Mod_btn() {
        //日夜间初始化
        Mod_btn = findViewById(R.id.day_night_switch);
        switch(currentDNMode){
            case DAY_MOD:switch_to_dayMod();
                break;
            case NIGHT_MOD:switch_to_nightMod();
                break;
            default:
        }
        //模式切换
        Mod_btn.setOnClickListener(v -> {
            switch(currentDNMode){
                case DAY_MOD:
                {
                    //当前日间模式，点击切换为夜间
                    currentDNMode = NIGHT_MOD;

                    switch_to_nightMod();

                    settingChangeListener.onDNModChange(NIGHT_MOD);
                }
                break;
                case NIGHT_MOD:
                {
                    //当前夜间模式，点击切换为日间
                    currentDNMode = DAY_MOD;

                    switch_to_dayMod();

                    settingChangeListener.onDNModChange(DAY_MOD);
                }
                break;
                default:
            }
            saveDayNightMod();
        });
    }

    private void switch_to_dayMod() {
        Mod_btn.setImageResource(R.mipmap.night_mod);
        if (!is_toolbar_visible)
            StatusBarUtil.setStatusBarDarkTheme(activity,true);
        Brightness.startAutoBrightness(activity);
    }

    private void switch_to_nightMod() {
        Mod_btn.setImageResource(R.mipmap.day_mod);
        if (!is_toolbar_visible)
            StatusBarUtil.setStatusBarDarkTheme(activity,false);
//        Brightness.stopAutoBrightness(activity);
//        Brightness.setSystemScreenBrightness(activity,60);
    }

    /**
     * 根据用户信息应用当前日夜间模式
     */
    private void init_currentMod() {
        //初始化日夜模式
        int myDNMod=0;
        myDNMod=myInfo.getInt("myDNMod",0);
        switch(myDNMod){
            case 0:
                currentDNMode = DAY_MOD;
                break;
            case 1:
                currentDNMode = NIGHT_MOD;
                break;
            default:
        }
    }

    /**
     * 保存当前日夜间模式
     */
    private void saveDayNightMod() {
        if (myInfo!=null) {
            SharedPreferences.Editor editor = myInfo.edit();
            int myDNMod = 0;
            switch (currentDNMode) {
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

    /**
     * 书名显示TextView
     */
    private void init_showBookName() {
        //显示书名
        showBookName=findViewById(R.id.show_book_name);
        showBookName.setText(CurrentChap.getBookName());
    }

    /**
     * 返回书架按钮
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init_BackToShelf() {
        //返回书架
        BackToShelf=findViewById(R.id.back_to_shelf);
        BackToShelf.setOnClickListener(v -> {
            ViberateControl.Vibrate(activity,15);
            activity.onBackPressed();
        });
    }

    /**
     * 启动章节内容显示界面
     */
    private void startFragment() {
        loadCatalog();
        try {
            setupFragment(currentViewMode);
            getSupportFragmentManager().beginTransaction().add(R.id.novel_view_container,fragment).commitAllowingStateLoss();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupFragment(NovelViewBasicFragment mFragment) throws IOException, ClassNotFoundException {
        ArrayList<NovelChap> novelChap = new ArrayList<>();
        novelChap.add(CurrentChap.deepClone());
        //mFragment = NovelViewFragmentFactory.getNovelViewFragment(viewMode);
        if (mFragment==null)throw new IllegalArgumentException("view mode invalid");
        mFragment.setFragmentTag("OK");
        mFragment.setChapCache(novelChap);
        mFragment.setCatalog(catalog);
        mFragment.setInitViewMod(currentDNMode);
    }

    private void setupFragment(NovelViewFragmentFactory.ViewMode viewMode) throws IOException, ClassNotFoundException {
        fragment = NovelViewFragmentFactory.getNovelViewFragment(viewMode);
        setupFragment(fragment);
    }

    /**
     * 切换章节内容显示界面
     */
    private void shiftFragment(){
        try {
            CurrentChap = fragment.getCurrentChap().deepClone();
            CurrentChap.setProgress(fragment.getCurrent_chap_progress());
            CurrentChap.setNovelRequire(fragment.getNovelRequire());
            NovelViewBasicFragment new_fragment = NovelViewFragmentFactory.getNovelViewFragment(currentViewMode);
            setupFragment(new_fragment);
            if (new_fragment == null)return;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.novel_view_container, new_fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            fragment = new_fragment;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void refreshFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        loadCatalog();
        try {
            setupFragment(currentViewMode);
            // 替换操作
            fragmentTransaction.replace(R.id.novel_view_container, fragment);
            fragmentTransaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void toolbarControl(boolean intercept, float x, float y) {
        float clickableArea_top=TopToolBar.getLayoutParams().height;
        float clickableArea_bottom= ScreenUtils.getScreenHeight(activity)-BottomToolBar.getLayoutParams().height;
        if (!intercept){
            if (!is_toolbar_visible) {
                BottomToolBar.startAnimation(action_bottom_up);
                BottomToolBar.setVisibility(View.VISIBLE);
                TopToolBar.startAnimation(action_top_down);
                TopToolBar.setVisibility(View.VISIBLE);
                is_toolbar_visible = true;
                StatusBarUtil.setStatusBarDarkTheme(activity,false);
            }else if (y>clickableArea_top && y<clickableArea_bottom){
                SetToolBarInvisible();
            }
        }else SetToolBarInvisible();
    }

    @Override
    public void onReadNewChap(int chapIndex) {
        currentChapIndex=chapIndex;
        CurrentChap.setCurrentChap(chapIndex);
        CurrentChap.setTitle(catalog.getTitle().get(currentChapIndex));
        catalogListAdapter.setItem_to_paint(catalog.getTitle().get(chapIndex));
    }

    @Override
    public void onJumpToNewChap(boolean success, NovelChap newChap) {
        if (success) {
            currentChapIndex = newChap.getCurrentChap();
            CurrentChap.setCurrentChap(currentChapIndex);
            CurrentChap.setTitle(catalog.getTitle().get(currentChapIndex));
            catalogListAdapter.setItem_to_paint(newChap.getTitle());
        }
//        if (waitDialog!=null && waitDialog.isShowing()) waitDialog.dismiss();
        if (drawerLayout.isDrawerOpen(findViewById(R.id.left_layout)))drawerLayout.closeDrawer(findViewById(R.id.left_layout));
        SetToolBarInvisible();
    }

    @Override
    public void waitDialogControl(boolean isShow) {
        if (waitDialog!=null){
            if (isShow && !waitDialog.isShowing()){
                waitDialog.setTitle("章节缓存中…");
                waitDialog.show();
            }
            if (!isShow && waitDialog.isShowing())
                waitDialog.dismiss();
        }
    }

}
