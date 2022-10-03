package com.Z.NovelReader.Threads;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.Objects.MapElement;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 考虑多目录页情况，获取所有目录页链接
 * 以输入的链接为起始，循环迭代获取其他链接
 */
public class SubCatalogLinkThread extends IterationThread {

    private ArrayList<String> subCatalogLinkList = new ArrayList<>();
    private NovelRequire novelRequire;
    private String catalogLinkPath;
    private String newLink = "";
    private boolean isUpdate = false;//是否是更新模式，即：取已有链接的最后一项进行迭代

    /**
     *
     * @param startLink 开始迭代的链接
     * @param novelRequire 书源
     */
    public SubCatalogLinkThread(String startLink, NovelRequire novelRequire,boolean isUpdate) {
        super(startLink);
        this.novelRequire = novelRequire;
        this.isUpdate = isUpdate;
        if (!isUpdate)subCatalogLinkList.add(startLink);
        else subCatalogLinkList.add("");
    }

    /**
     * @param catalog_link_path 目录链接文件输出路径
     */
    public void setOutputParams(String catalog_link_path){
        this.catalogLinkPath = catalog_link_path;
    }

    @Override
    public void resultProcess() {
        subCatalogLinkList.add(newLink);
    }

    @Override
    public Object preProcess(Document document) throws Exception {
        NovelRuleAnalyzer subCatalogAnalyzer = new NovelRuleAnalyzer();
        List<String> subLinks = subCatalogAnalyzer.getObjectFromElements(new Elements(document),
                    novelRequire.getRuleToc().getNextTocUrl());
        return subLinks;
    }

    @Override
    public boolean canBreakIterate(Object o) {
        List<String> subLinks;
        if (o instanceof List) subLinks = (List<String>) o;
        else throw new IllegalArgumentException("object 不是list类型");
        if (subLinks.size()!=0)newLink = StringUtils.completeUrl(subLinks.get(0),
                    novelRequire.getBookSourceUrl());
        if (newLink.equals(getStartLink()) || subCatalogLinkList.contains(newLink)) return true;
        return "".equals(newLink);
    }

    @Override
    public String updateStartLink() {
        return newLink;
    }

    @Override
    public void onIterativeFinish() {
        FileIOUtils.WriteList(catalogLinkPath,subCatalogLinkList,isUpdate);
        MapElement element = new MapElement(novelRequire.getId(),subCatalogLinkList);
        callback(PROCESS_DONE,element);
    }

    @Override
    public void onErrorOccur(int event,Exception e) {

    }

    public static class SubCatalogHandler extends Handler{
        SubCatalogListener listener;

        public SubCatalogHandler(SubCatalogListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==PROCESS_DONE)
                listener.onSuccess((MapElement) msg.obj);
            else {
                listener.onError();
            }
        }
    }
    public interface SubCatalogListener{
        void onSuccess(MapElement result);
        void onError();
    }
}
