package com.Z.NovelReader.Threads.Handlers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.BasicHandlerThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelSourceRoom.NovelSourceDBTools;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Objects.NovelChap;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.FileIOUtils;

import java.util.HashMap;
import java.util.Locale;

public class GetCatalogHandler extends Handler {
    private int count =0;
    private int total_count;
    private GetCatalogListener listener;
    private boolean no_error = true;
    private HashMap<Integer,NovelCatalog> subCatalogs;
    private NovelCatalog generalCatalog;
    private String catalog_path;
    private boolean need_output = false;
    private NovelSourceDBTools NSUpdater;
    private NovelRequire rule;

    public GetCatalogHandler(GetCatalogListener listener) {
        this.listener = listener;
        this.count = 0;
        this.subCatalogs = new HashMap<>();
        generalCatalog = new NovelCatalog();
    }
    public void clearAll(){
        count =0;
        subCatalogs = new HashMap<>();
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public void setNSUpdater(NovelSourceDBTools NSUpdater , NovelRequire novelRequire) {
        this.NSUpdater = NSUpdater;
        this.rule = novelRequire;
    }

    /**
     * 设置输出路径
     * @param output_path ="$extDir$/ZsearchRes/$book name$/catalog.txt"
     */
    public void setOutput(String output_path){
        this.catalog_path = output_path;
        need_output = true;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (!BasicHandlerThread.isErrorOccur(msg.what)){
            MapElement element = (MapElement) msg.obj;
            subCatalogs.put(element.key, (NovelCatalog) element.value);
            NovelCatalog catalog = (NovelCatalog) element.value;
            if (element.key == 0 && catalog.getSize()>0){
                if (NSUpdater!=null){
                    boolean hasExtraPage = (rule.getRuleBookInfo() != null && rule.getRuleBookInfo().getTocUrl() != null);
                    long uniRespondTime = catalog.getUniRespondTime((total_count > 1), hasExtraPage);
                    NSUpdater.IterativeUpdateSourceRespondTime(rule.getId(),uniRespondTime);
                    Log.d("updateSourceRespondTime", "rs time: "+uniRespondTime);
                }
                NovelCatalog currentChap = new NovelCatalog();
                currentChap.add(catalog.getTitle().get(0),
                        catalog.getLink().get(0));
                listener.onFirstChapReady(currentChap);
            }
        }
        else {
            if (no_error)listener.onError();
            no_error = false;
        }
        synchronized(this){
            count++;
            Log.d("Get sub_catalog", count +"/"+ total_count);
            if (count == total_count && no_error){
                //join catalog
                for (int i = 0; i < subCatalogs.size(); i++) {
                    generalCatalog.addCatalog(subCatalogs.get(i));
                }
                Log.d("CatalogHandler","process done in thread"+Thread.currentThread());
                if (need_output)FileIOUtils.WriteCatalog(catalog_path,generalCatalog);
                listener.onAllProcessDone(generalCatalog);
            }
        }
    }

    public interface GetCatalogListener{
        void onError();
        void onAllProcessDone(NovelCatalog catalog);
        void onFirstChapReady(NovelCatalog chap);
    }
}
