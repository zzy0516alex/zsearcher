package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Global.MyApplication;
import com.Z.NovelReader.NovelRoom.NovelDBTools;
import com.Z.NovelReader.NovelRoom.NovelParams;
import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.OpenEntities.CustomDataHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class JavaScriptEngine {
    private Context ctx;
    private Scriptable scope;
    private NovelRequire rule;
    private Novels novel;
    private Java java;
    private Book book;

    public JavaScriptEngine(NovelRequire rule, Novels novel) {
        ctx = Context.enter();
        scope = ctx.initStandardObjects();
        ctx.setOptimizationLevel(-1);
        java = new Java(rule.getId(), novel!=null?novel.getId():-1);
        book = new Book(rule);
        ScriptableObject.putProperty(scope,"java",java);
        ScriptableObject.putProperty(scope,"book",book);
        this.rule = rule;
    }
    public void preDefine(String key, String value){
        scope.put(key,scope,value);
    }

    public String runScript(String jsCode) throws Exception {
        Object result = ctx.evaluateString(scope, jsCode, null, 0,null);
        java.done();//更新部分需要记忆的参数
        if (result instanceof String) return (String) result;
        else if (result instanceof NativeJavaObject) {
            return (String) ((NativeJavaObject) result).getDefaultValue(String.class);
        } else if (result instanceof NativeObject) {
            return (String) ((NativeObject) result).getDefaultValue(String.class);
        } else throw new Exception("只接受结果为string的js脚本");
    }

    //用户自定义java函数
    public static class Java{
        private int source_id;//当前调用js的书源id
        private int novel_id;
        private NovelDBTools dbTools;
        private Map<String,String> custom_params;
        private boolean new_item = false;//是否是新创建的书籍参数
        /**
         * @param source_id 书源规则的ID
         * @param novel_id 当前书籍的ID，不存在则为-1
         */
        public Java(int source_id, int novel_id) {
            this.source_id = source_id;
            this.novel_id = novel_id;
            if(novel_id==-1){
                //-1表示临时书，不在数据库中
                if(CustomDataHolder.tempParam!=null) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<HashMap<String, String>>() {
                    }.getType();
                    custom_params = gson.fromJson(CustomDataHolder.tempParam.getCustomParam(), type);
                }
                else custom_params = new HashMap<>();
                return;
            }
            dbTools = new NovelDBTools(MyApplication.getAppContext());
            dbTools.getNovelParamsByIds(novel_id, source_id, result -> {
                if(result==null){custom_params = new HashMap<>();this.new_item = true;return;}
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                custom_params = gson.fromJson(result.getCustomParam(), type);
            });
        }
        public void done(){
            String param_json = new Gson().toJson(custom_params);
            NovelParams novelParams = new NovelParams(novel_id, source_id, param_json);
            if(novel_id==-1)
                CustomDataHolder.tempParam = novelParams;
            else if(new_item)
                dbTools.operateNovelParams(NovelDBTools.DBMethods.Insert, novelParams);
            else dbTools.operateNovelParams(NovelDBTools.DBMethods.Update, novelParams);
        }
        public String ajax(String url){
            ConnectThread t = new ConnectThread(url);
            t.start();
            try {
                return t.getResult();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

        public void put(String key,String value){
            custom_params.put(key,value);
        }
        public String get(String key){
            if(!custom_params.containsKey(key))throw new RuntimeException("找不到书源自定义变量，书源解析异常");
            return custom_params.get(key);
        }
    }
    //用户自定义Book类
    public static class Book{
        public String origin;

        public Book(NovelRequire rules){
            this.origin = rules.getBookSourceUrl();
        }
    }

    public static class ConnectThread extends Thread{
        String url = "";
        String result = "";

        public ConnectThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            super.run();
            try {
                Document document= Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
                        .ignoreHttpErrors(true)
                        .followRedirects(true)
                        .timeout(80000)
                        .ignoreContentType(true).get();
                result = document.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getResult() throws InterruptedException {
            join();
            return result;
        }
    }
}
