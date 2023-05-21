package com.Z.NovelReader.Threads;

import static com.Z.NovelReader.Objects.NovelChap.getCurrentChapLink;
import static com.Z.NovelReader.Objects.NovelChap.initNovelChap;

import android.content.Context;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Basic.BasicHandler;
import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.NovelDBUpdater;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.BackupSourceBean;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.FileUtils;
import com.Z.NovelReader.Utils.StorageUtils;
import com.Z.NovelReader.Utils.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;

public class SwitchSourceThread extends BasicHandlerThread {
    private Context context;
    private NovelDBUpdater dbUpdater;
    private BackupSourceBean backupSource;//用于替换的书籍信息和目录
    private NovelRequire backupRule;//用于替换的书源规则
    private Novels backupNovel;
    private NovelChap currentUsingChap;//要被替换的书籍信息

    public SwitchSourceThread(Context context, BackupSourceBean backupSource, NovelRequire backupRule, NovelChap currentUsingChap) {
        this.context = context;
        this.backupSource = backupSource;
        this.backupRule = backupRule;
        this.currentUsingChap = currentUsingChap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        //super.run();
        Looper.prepare();
        dbUpdater = new NovelDBUpdater(context);
        if (backupSource == null){
            report(NULL_OBJECT);
            return;
        }
        NovelCatalog catalog = backupSource.getCatalog();
        backupNovel = backupSource.getCurrent_novel();
        if (backupNovel==null){
            report(NULL_OBJECT);
            return;
        }
        backupNovel.setUsed(false);//暂时先不启用，待成功后启用
        backupNovel.setTtlChap(catalog.getSize());
        //match chap title
        int match = catalog.findMatch(currentUsingChap.getTitle());
        if (match!=-1)backupNovel.setCurrentChap(match);
        else if (currentUsingChap.getCurrentChap() < catalog.getSize())
            backupNovel.setCurrentChap(currentUsingChap.getCurrentChap());
        else backupNovel.setCurrentChap(0);

        //download content and save to file
        //提取父链接
        String contentRootUrl;
        if (catalog.getSize() > 1)contentRootUrl = StringUtils.getSharedURL(
                catalog.getLink().get(0),catalog.getLink().get(1));
        else contentRootUrl = StringUtils.getRootUrl(catalog.getLink().get(0));
        backupNovel.setContentRootLink(contentRootUrl);
        dbUpdater.updateNovels(backupNovel);//更新父链接
        //开始下载章节内容
        ContentThread contentThread = new ContentThread(catalog.getLink().get(backupNovel.getCurrentChap()),
                backupRule,contentRootUrl);
        contentThread.setCatalogLinks(catalog.getLink());
        contentThread.setUpdateRootURL(backupNovel,context);
        contentThread.setOutputParams(StorageUtils.getBookContentPath(backupNovel.getBookName(),backupNovel.getWriter()));
        BasicHandler<String> contentHandler = new BasicHandler<>(
                new BasicHandler.BasicHandlerListener<String>() {
                    @Override
                    public void onSuccess(String result) {
                        String[] currentChap = getCurrentChapLink(backupNovel.getCurrentChap(), catalog);
                        NovelChap chap = new NovelChap(backupNovel);
                        chap.setNovelRequire(backupRule);
                        initNovelChap(chap,result, currentChap);
                        //启用备选书源文件
                        String backup_catalog_path = StorageUtils.getBackupSourceCatalogPath(chap.getBookName(),chap.getWriter(),backupSource.getSourceID());
                        String backup_catalogURL_path = StorageUtils.getBackupSourceCatalogLinkPath(chap.getBookName(),chap.getWriter(),backupSource.getSourceID());
                        String backup_cover_path = StorageUtils.getBackupSourceCoverPath(chap.getBookName(),chap.getWriter(),backupSource.getSourceID());
                        String target_catalog_path = StorageUtils.getBookCatalogPath(chap.getBookName(),chap.getWriter());
                        String target_catalogURL_path = StorageUtils.getBookCatalogLinkPath(chap.getBookName(),chap.getWriter());
                        String target_cover_path = StorageUtils.getBookCoverPath(chap.getBookName(),chap.getWriter());
                        FileUtils.copyFile(backup_catalog_path,target_catalog_path);
                        FileUtils.copyFile(backup_catalogURL_path,target_catalogURL_path);
                        FileUtils.copyFile(backup_cover_path,target_cover_path);
                        callback(PROCESS_DONE,chap);
                    }

                    @Override
                    public void onError(int error_code) {
                        report(error_code);
                    }
                });
        contentThread.setHandler(contentHandler);
        contentThread.start();
        Looper.loop();
    }
}
