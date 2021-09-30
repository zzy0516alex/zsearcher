package com.Z.NovelReader.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.Z.NovelReader.Basic.IterationThread;
import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Utils.FileIOUtils;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 考虑多目录页情况，获取所有目录页链接
 * 以输入的链接为起始，循环迭代获取其他链接
 */
public class SubCatalogLinkThread extends IterationThread {

    private ArrayList<String> subCatalogLinkList = new ArrayList<>();
    private NovelRequire novelRequire;
    private File Dir;
    private String sub_path;
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

    public void setOutputParams(String sub_path){
        this.sub_path = sub_path;
        Dir = MyApplication.getExternalDir();
    }

    @Override
    public void resultProcess() {
        subCatalogLinkList.add(newLink);
    }

    @Override
    public Object firstProcess(Document document) throws Exception {
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
        if (newLink.equals(getStartLink())) return true;
        return "".equals(newLink);
    }

    @Override
    public String updateStartLink() {
        return newLink;
    }

    @Override
    public void onIterativeFinish() {
        FileIOUtils.WriteList(Dir,"/ZsearchRes"+sub_path,subCatalogLinkList,isUpdate);
        callback(PROCESS_DONE,subCatalogLinkList);
    }

    @Override
    public void onErrorOccur(int event) {

    }

//    public void setHandler(Handler handler) {
//        this.handler = handler;
//    }

//    @Override
//    public void run() {
//        super.run();
//        if (handler!=null && message==null)message=handler.obtainMessage();
//        try {
//            Connection connect = Jsoup.connect(startLink);
//            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
//            Document document= connect.get();
//
//            NovelRuleAnalyzer subCatalogAnalyzer = new NovelRuleAnalyzer();
//            List<String> subLinks = subCatalogAnalyzer.getObjectFromElements(new Elements(document),
//                    novelRequire.getRuleToc().getNextTocUrl());
//            if (subLinks==null) throw new RuntimeException("SubCatalogLinkThread：返回结果为null");
//            String new_link="";
//            if (subLinks.size()!=0)new_link = StringUtils.completeUrl(subLinks.get(0),
//                    novelRequire.getBookSourceUrl());
//            if (new_link.equals(startLink))safe_counter=0;
//            if (safe_counter!=0 && !"".equals(new_link)){
//                startLink = new_link;
//                subCatalogLinkList.add(startLink);
//                safe_counter--;
//                Log.d("SubCatalogLinkThread","sub catalog:"+(500-safe_counter));
//                run();
//            }else{
//                //迭代结束
//                FileIOUtils.WriteList(Dir,"/ZsearchRes"+sub_path,subCatalogLinkList,isUpdate);
//                if (message!=null){
//                    message.obj=subCatalogLinkList;
//                    message.what=ALL_LINK_GET_DONE;
//                    handler.sendMessage(message);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d("SubCatalogLinkThread","无网络");
//            if (message!=null){
//                message.what=ERROR;
//                handler.sendMessage(message);
//            }
//        } catch (RuntimeException e){
//            Log.d("SubCatalogLinkThread","返回结果为null");
//            if (message!=null){
//                message.what=ERROR;
//                handler.sendMessage(message);
//            }
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d("SubCatalogLinkThread","processor 错误");
//            if (message!=null){
//                message.what=ERROR;
//                handler.sendMessage(message);
//            }
//        }
//    }

    public static class SubCatalogHandler extends Handler{
        SubCatalogListener listener;

        public SubCatalogHandler(SubCatalogListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what==PROCESS_DONE)
                listener.onSuccess((ArrayList<String>) msg.obj);
            else {
                listener.onError();
            }
        }
    }
    public interface SubCatalogListener{
        void onSuccess(ArrayList<String> result);
        void onError();
    }
}
