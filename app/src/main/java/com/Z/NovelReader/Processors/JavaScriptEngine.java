package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaScriptEngine {
    private Context ctx;
    private Scriptable scope;
    private NovelRequire rule;
    private Java java;
    private Book book;

    public JavaScriptEngine(NovelRequire rule) {
        ctx = Context.enter();
        scope = ctx.initStandardObjects();
        ctx.setOptimizationLevel(-1);
        java = new Java();
        book = new Book(rule);
        ScriptableObject.putProperty(scope,"java",java);
        ScriptableObject.putProperty(scope,"book",book);
        this.rule = rule;
        preDefine();
    }
    public void preDefine(){
        scope.put("basicUrl",scope,rule.getBookSourceUrl());
        //scope.put("baseUrl",scope,rule.getBookSourceUrl());
        //scope.put("result",scope,result);
    }
    public void preDefine(String key, String value){
        scope.put(key,scope,value);
    }

    public String runScript(String jsCode) throws Exception {
        Object result = ctx.evaluateString(scope, jsCode, null, 0,null);
        if (result instanceof String) return (String) result;
        else if (result instanceof NativeJavaObject) {
            return (String) ((NativeJavaObject) result).getDefaultValue(String.class);
        } else if (result instanceof NativeObject) {
            return (String) ((NativeObject) result).getDefaultValue(String.class);
        } else throw new Exception("只接受结果为string的js脚本");
    }

    //用户自定义java函数
    public static class Java{
        Map<String,String> info = new HashMap<>();
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
            info.put(key,value);
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
