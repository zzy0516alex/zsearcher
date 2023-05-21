package com.Z.NovelReader.Utils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HttpConnection {
    private Connection connection;
    public HttpConnection(String url){
        connection = Jsoup.connect(url);

        connection.timeout(20000);
        connection.ignoreHttpErrors(true);
        connection.followRedirects(true);

        connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61");
        connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        connection.header("Accept-Encoding", "gzip, deflate, br");
        connection.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        connection.header("Cache-Control", "max-age=0");
        connection.header("Connection", "close");
    }

    public Document get() throws IOException {
        SSLAgent.getInstance().trustAllHttpsCertificates();
        return connection.get();
    }
//    private WebClient browser;
//    private boolean isJSEnabled = false;
//
//    public HttpConnection(){
//        browser = new WebClient(BrowserVersion.CHROME);
//
//        browser.getOptions().setTimeout(20000);
//        browser.getOptions().setThrowExceptionOnFailingStatusCode(false);
//        browser.getOptions().setThrowExceptionOnScriptError(false);
//        browser.getOptions().setCssEnabled(false);
//        browser.getOptions().setJavaScriptEnabled(false);
//        browser.getOptions().setRedirectEnabled(true);
//        browser.getCookieManager().setCookiesEnabled(false);
//
//        browser.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//        browser.addRequestHeader("Accept-Encoding", "gzip, deflate, br");
//        browser.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
//        browser.addRequestHeader("Cache-Control", "max-age=0");
//        browser.addRequestHeader("Connection", "keep-alive");
//        browser.addRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61");
//    }
//
//    public void enableJavaScript(boolean enable){
//        isJSEnabled = enable;
//        browser.getOptions().setJavaScriptEnabled(enable);
//    }
//
//    public Document connectByGet(String url) throws IOException {
//        HtmlPage page = browser.getPage(url);
//        //设置等待js的加载时间
//        if(isJSEnabled) browser.waitForBackgroundJavaScript(3000);
//        //使用xml的方式解析获取到jsoup的document对象
//        return Jsoup.parse(page.asXml());
//    }
//
//    public Document connectByPost(String url, String charset_name, List<NameValuePair> params) throws IOException {
//        WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
//        webRequest.setCharset(Charset.forName(charset_name));
//        webRequest.setRequestParameters(params);
//        HtmlPage page = browser.getPage(webRequest);
//        if(isJSEnabled) browser.waitForBackgroundJavaScript(3000);
//        return Jsoup.parse(page.asXml());
//    }
}
