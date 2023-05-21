package com.Z.NovelReader.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.Z.NovelReader.Global.OnReadingListener;
import com.Z.NovelReader.Global.OnSettingChangeListener;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Threads.ContentThread;
import com.Z.NovelReader.Threads.Handlers.ChapContentHandler;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StorageUtils;

import java.util.ArrayList;
import java.util.Objects;

public abstract class NovelViewBasicFragment extends Fragment implements OnSettingChangeListener {
    //public statics
    public static final int DOWNLOAD_TYPE_LAST_CHAP = 0x01;
    public static final int DOWNLOAD_TYPE_NEXT_CHAP = 0x02;
    public static final int DOWNLOAD_TYPE_SKIP_CHAP = 0x03;
    public static final double DEFAULT_CHAP_PROGRESS = Novels.DEFAULT_PROGRESS;//默认的章节进度初值(该值不能为0，防止触发上一章节的自动下载)
    public int MAX_CHAP_CACHE = 12;//章节缓存列表需要裁剪的阈值
    public int MIN_CHAP_CACHE = 3;//章节缓存列表当前帧前(后)至少保留的大小
    public enum DNMod {
        NIGHT_MOD,
        DAY_MOD
    }
    //basic info
    private String tag;//一个 NovelViewFragment 实体的标志
    private SharedPreferences recoverInfo;//记录书籍是否停留在阅读界面
    //handlers
    private ChapContentHandler<FragmentActivity> lastChapHandler;//上一章下载完毕后处理
    private ChapContentHandler<FragmentActivity> nextChapHandler;//下一章下载完毕后处理
    private ChapContentHandler<FragmentActivity> jumpChapHandler;//第n章下载完毕后处理
    //novel info
    private Novels currentBook;//当前书籍信息集合
    private String novelName;
    private String writer;
    private int novelID;
    private String contentRoot;//正文的根地址(链接)
    private NovelCatalog catalog;//目录
    private NovelRequire novelRequire;//书源规则信息
    //basic params
    private ArrayList<NovelChap> chapCache;//章节缓存列表
    private int chap_cache_index = 0;//相对于章节缓存中的序号
    private int chap_catalog_index = 0;//相对于目录的序号
    private double current_chap_progress = DEFAULT_CHAP_PROGRESS;//当前章节的阅读进度
    private boolean auto_download = true;//是否自动下载下一章
    private int initCacheCount = 2;//检查章节缓存初始化是否完成
    private DNMod initViewMod;
    //database update
    private NovelDBUpdater updater;
    //listener
    private OnReadingListener readingListener;

    //getter & setter

    public void setChapCache(ArrayList<NovelChap> chapCache) {
        this.chapCache = chapCache;
    }

    public ArrayList<NovelChap> getChapCache() {
        return chapCache;
    }

    public NovelChap getCurrentChap(){
        return chapCache.get(chap_cache_index);
    }

    public boolean hasLastChap(){
        return chap_catalog_index-1 >= 0;
    }

    public NovelChap getLastChap(){
        if (!hasLastChap())return null;
        return chapCache.get(chap_cache_index-1);
    }

    public boolean hasNextChap(){
        return chap_catalog_index+1 < catalog.getSize();
    }

    public NovelChap getNextChap(){
        if (!hasNextChap())return null;
        return chapCache.get(chap_cache_index+1);
    }

    public void setCatalog(NovelCatalog catalog) {
        this.catalog = catalog;
    }

    public NovelCatalog getCatalog() {
        return catalog;
    }

    public int getChap_cache_index() {
        return Math.max(chap_cache_index, 0);
    }

    public void setChap_cache_index(int chap_cache_index) {
        this.chap_cache_index = chap_cache_index;
    }

    public int getChap_catalog_index() {
        return chap_catalog_index;
    }

    public void setChap_catalog_index(int chap_catalog_index) {
        this.chap_catalog_index = chap_catalog_index;
    }

    public double getCurrent_chap_progress() {
        return current_chap_progress;
    }

    public void setCurrent_chap_progress(double current_chap_progress) {
        this.current_chap_progress = current_chap_progress;
    }

    public boolean isAuto_download() {
        return auto_download;
    }

    public void setInitViewMod(DNMod initViewMod) {
        this.initViewMod = initViewMod;
    }

    public DNMod getInitViewMod() {
        return initViewMod;
    }

    public NovelRequire getNovelRequire() {
        return novelRequire;
    }

    //对可用的fragment进行标记
    public String getFragmentTag() {
        return tag;
    }

    public void setFragmentTag(String tag) {
        this.tag = tag;
    }

    public OnReadingListener getReadingListener() {
        return readingListener;
    }

    //用于及时恢复阅读界面（暂未使用）
    public void saveRecoverFile(){
        recoverInfo= Objects.requireNonNull(getActivity()).getSharedPreferences("recoverInfo", Context.MODE_PRIVATE);
        recoverInfo.edit().putString("BookName",novelName)
                .putBoolean("onFront",true)
                .apply();
    }

    //以下初始化函数均应在 onCreateView 中调用
    /**初始化书籍信息*/
    public void initNovelInfo(){
        currentBook = chapCache.get(0);
        chap_catalog_index = currentBook.getCurrentChap();
        novelName = currentBook.getBookName();
        writer = currentBook.getWriter();
        novelID = currentBook.getId();
        contentRoot = currentBook.getContentRootLink();
        current_chap_progress = currentBook.getProgress();
        novelRequire = chapCache.get(0).getNovelRequire();
    }
    /**初始化数据库更新器*/
    public void initDBUpdater(){
        updater = new NovelDBUpdater(getContext());
    }
    /**初始化章节缓存的线程回调处理器*/
    public void initHandlers(){
        lastChapHandler = new ChapContentHandler<>(getActivity());
        lastChapHandler.setListener(new ChapContentHandler.ChapContentListener() {
            @Override
            public void onSuccess(String chap_content) {
                NovelChap newChap = packChap(chap_content, chap_catalog_index - 1,false);
                chapCache.add(0, newChap);
                auto_download = true;
                chap_cache_index++;
                downloadReady(DOWNLOAD_TYPE_LAST_CHAP,false);
                checkCacheSize();
            }

            @Override
            public void onError(String error_content) {
                NovelChap newChap = packChap(error_content, chap_catalog_index - 1,true);
                chapCache.add(0, newChap);
                auto_download = false;
                chap_cache_index++;
                downloadReady(DOWNLOAD_TYPE_LAST_CHAP,true);
                checkCacheSize();
            }

            @Override
            public void onFinish() {
                checkCacheInit();
                readingListener.waitDialogControl(false);
            }
        });

        nextChapHandler = new ChapContentHandler<>(getActivity());
        nextChapHandler.setListener(new ChapContentHandler.ChapContentListener() {
            @Override
            public void onSuccess(String chap_content) {
                NovelChap newChap = packChap(chap_content, chap_catalog_index + 1,false);
                chapCache.add(newChap);
                auto_download = true;
                downloadReady(DOWNLOAD_TYPE_NEXT_CHAP,false);
                checkCacheSize();
            }

            @Override
            public void onError(String error_content) {
                NovelChap newChap = packChap(error_content, chap_catalog_index + 1,true);
                chapCache.add(newChap);
                auto_download = false;
                downloadReady(DOWNLOAD_TYPE_NEXT_CHAP,true);
                checkCacheSize();
            }

            @Override
            public void onFinish() {
                checkCacheInit();
                //readingListener.waitDialogControl(false);
            }
        });

        jumpChapHandler = new ChapContentHandler<>(getActivity());
        jumpChapHandler.setListener(new ChapContentHandler.ChapContentListener() {
            @Override
            public void onSuccess(String chap_content) {
                NovelChap newChap = packChap(chap_content, chap_catalog_index,false);
                refreshChapCache(newChap);
                auto_download = true;
            }

            @Override
            public void onError(String error_content) {
                readingListener.onJumpToNewChap(false,null);
                Toast.makeText(getContext(), error_content, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                readingListener.waitDialogControl(false);
            }

        });
    }
    /**由章节列表中第一个章节缓存前后的章节*/
    public void initCache(){
        NovelChap currentChap= chapCache.get(0);
        if (currentChap.hasLastLink() && currentChap.hasNextLink())
            initCacheCount = 2;
        else initCacheCount = 1;
        LastChapDownloader(currentChap);
        NextChapDownloader(currentChap);
    }

    public void checkCacheInit(){
        synchronized (this){
            if (initCacheCount==0)return;
            initCacheCount--;
            if (initCacheCount == 0){
                readingListener.waitDialogControl(false);
                cacheInitReady();
            }
        }
    }

    /**
     * 当章节缓存过大时，进行裁剪
     */
    public void checkCacheSize(){
        if (chapCache.size() > MAX_CHAP_CACHE){
            boolean isDown =true;//是向下翻阅的
            if (chap_cache_index < (MAX_CHAP_CACHE/2))isDown = false;
            int Cut_min = chap_cache_index-MIN_CHAP_CACHE;
            int Cut_max = chap_cache_index+MIN_CHAP_CACHE;
            if (Cut_max > chapCache.size()-1) Cut_max=chapCache.size()-1;
            if (Cut_min < 0) Cut_min=0;
            for (int i = chapCache.size() - 1; i >= 0; i--) {
                if (i < Cut_min || i > Cut_max) {
                    chapCache.remove(i);
                    if (isDown)chap_cache_index--;
                }
            }
            if (!isDown)chap_cache_index++;//此时在头部已经新增了一个章节，当前章节应该为后一个
            afterCacheCut();
        }
    }

    /**
     * 当检测到用户开始阅读新章节时，调用本函数
     * 更新章节索引
     * @param new_pos 新章节在章节缓存列表中的索引
     */
    public void updateReadingChap(int new_pos){
        chap_cache_index = new_pos;
        chap_catalog_index = chapCache.get(chap_cache_index).getCurrentChap();
        readingListener.onReadNewChap(chap_catalog_index);
        Log.d("novel view","当前章节列表："+(chap_cache_index +1) + "/"+ chapCache.size());
        Log.d("novel view","当前章节："+ chap_catalog_index);
        //Log.d("novel view","章节可见："+visible_chap_num);
    }

    /**
     * 在当前书籍的空白章节的基础上,添加标题、文本、上下章节链接,更新当前章节号
     * @param content 文本
     * @return 打包完成的章节类型数据
     */
    public NovelChap packChap(String content, int catalog_index,boolean onError){
        NovelChap newChap = new NovelChap(currentBook);
        newChap.setCurrentChap(catalog_index);
        newChap.setOnError(onError);
        String[] links = NovelChap.getCurrentChapLink(catalog_index, catalog);
        NovelChap.initNovelChap(newChap,content,links);
        return newChap;
    }

    /**
     * 缓存相对与当前章节的上一章节正文
     * @param currentChap 当前章节
     */
    public void LastChapDownloader(NovelChap currentChap) {
        chap_catalog_index = currentChap.getCurrentChap();
        if(currentChap.hasLastLink()){
            readingListener.waitDialogControl(true);
            Log.d("novel view","开始缓存上一章："+(chap_catalog_index -1));
            auto_download=false;
            ContentThread contentThread = new ContentThread(currentChap.getLast_link(),
                    novelRequire,contentRoot);
            contentThread.setCatalogLinks(getCatalog().getLink());
            contentThread.setOutputParams(StorageUtils.getBookContentPath(novelName,writer));
            contentThread.setHandler(lastChapHandler);
            contentThread.start();
        }
    }

    /**
     * 缓存相对与当前章节的下一章节正文
     * @param currentChap 当前章节
     */
    public void NextChapDownloader(NovelChap currentChap) {
        if(currentChap.hasNextLink()){
            Log.d("novel view","开始缓存下一章:"+(chap_catalog_index +1));
            auto_download=false;
            ContentThread contentThread = new ContentThread(currentChap.getNext_link(),
                    novelRequire,contentRoot);
            contentThread.setCatalogLinks(getCatalog().getLink());
            contentThread.setOutputParams(StorageUtils.getBookContentPath(novelName,writer));
            contentThread.setHandler(nextChapHandler);
            contentThread.start();
        }
    }

    /**
     * 跳转到某一章节
     * @param chap_pos 要跳转的章节的序号
     */
    public void SkipChapDownloader(int chap_pos){
        readingListener.waitDialogControl(true);
        auto_download=false;
        chap_catalog_index = chap_pos;
        Log.d("novel view","skip to chap:"+catalog.toString(chap_catalog_index));
        ContentThread contentThread = new ContentThread(catalog.getLink().get(chap_catalog_index),
                novelRequire,contentRoot);
        contentThread.setCatalogLinks(getCatalog().getLink());
        contentThread.setOutputParams(StorageUtils.getBookContentPath(novelName,writer));
        contentThread.setHandler(jumpChapHandler);
        contentThread.start();
    }

    /**
     * 保存当前章节及位置
     */
    public void SaveCurrentLine() {
        if (chapCache == null || chapCache.size()==0)return;
        final NovelChap currentChap = chapCache.get(chap_cache_index);
        Log.d("novel view",String.format("保存章节：%d|%s %.3f %%",currentChap.getCurrentChap(),
                currentChap.getTitle(),current_chap_progress * 100.0));
        updater.updateChapOffset(novelID,current_chap_progress);
        updater.updateCurrentChap(novelID,currentChap.getCurrentChap());
        Thread thread =new Thread(() -> FileIOUtils.WriteTXT(StorageUtils.getBookContentPath(novelName,writer),
                currentChap.getContent()));
        thread.start();
    }

    /**
     * 刷新整个章节缓存列表,注意刷新后需同步到 UI
     * 删除全部已有章节，仅保留一个章节
     * @param reservedChap 需要保留的章节
     */
    public void refreshChapCache(NovelChap reservedChap) {
        chapCache.clear();
        chapCache.add(reservedChap);
        chap_catalog_index =reservedChap.getCurrentChap();
        chap_cache_index =0;
        readingListener.onJumpToNewChap(true,reservedChap);
        downloadReady(DOWNLOAD_TYPE_SKIP_CHAP,false);

        LastChapDownloader(reservedChap);
        NextChapDownloader(reservedChap);
    }

    //章节缓存完成
    public abstract void downloadReady(int download_type, boolean isError);

    //初始章节的前后章缓存完成
    public abstract void cacheInitReady();

    //当发生了章节缓存裁剪时，触发
    public abstract void afterCacheCut();

    @Override
    public void onAttach(@NonNull Context context) {
        if (context instanceof OnReadingListener) {
            readingListener = (OnReadingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
        super.onAttach(context);
    }

    @Override
    public void onCatalogItemClick(int position) {
        SkipChapDownloader(position);
    }

    @Override
    public void onCatalogUpdate(NovelCatalog new_catalog) {
        catalog=new_catalog;
    }

}
