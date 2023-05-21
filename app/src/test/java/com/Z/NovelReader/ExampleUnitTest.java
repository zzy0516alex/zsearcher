package com.Z.NovelReader;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.AnalyseResult;
import com.Z.NovelReader.Processors.JavaScriptEngine;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.PictureThread;
import com.Z.NovelReader.Utils.StringUtils;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        WebClient browser = new WebClient(BrowserVersion.CHROME);

        browser.getOptions().setTimeout(20000);
        browser.getOptions().setThrowExceptionOnFailingStatusCode(false);
        browser.getOptions().setThrowExceptionOnScriptError(false);
        browser.getOptions().setCssEnabled(false);
        browser.getOptions().setJavaScriptEnabled(true);
        browser.getOptions().setRedirectEnabled(true);
        browser.getCookieManager().setCookiesEnabled(false);

        browser.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        browser.addRequestHeader("Accept-Encoding", "gzip, deflate, br");
        browser.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        browser.addRequestHeader("Cache-Control", "max-age=0");
        browser.addRequestHeader("Connection", "keep-alive");
        browser.addRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61");
        //browser.addRequestHeader("referer","https://m.qxzx8.com");
        //https://m.bimoks.com/search/,{
        try {
            WebRequest webRequest = new WebRequest(new URL("https://m.bimoks.com/search/"), HttpMethod.POST);
            webRequest.setCharset(Charset.forName("UTF-8"));
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new NameValuePair("keyword", "三国"));
            webRequest.setRequestParameters(nameValuePairs);
            //webRequest.setAdditionalHeader("keyword","三国");
            HtmlPage page = browser.getPage(webRequest);
            //设置等待js的加载时间
            browser.waitForBackgroundJavaScript(3000);
            //使用xml的方式解析获取到jsoup的document对象
            Document doc = Jsoup.parse(page.asXml());
            System.out.println(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}