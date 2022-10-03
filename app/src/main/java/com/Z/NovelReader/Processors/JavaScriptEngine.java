package com.Z.NovelReader.Processors;

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
import java.util.List;

public class JavaScriptEngine {
    private Context ctx;
    private Scriptable scope;

    public JavaScriptEngine() {
        ctx = Context.enter();
        scope = ctx.initStandardObjects();
        ctx.setOptimizationLevel(-1);
        ScriptableObject.putProperty(scope,"java",new Java());
    }
    public void preDefine(String basicUrl, String result){
        scope.put("basicUrl",scope,basicUrl);
        scope.put("result",scope,result);
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
