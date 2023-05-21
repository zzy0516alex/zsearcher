package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUrlProcessor {
    public synchronized static String getUrl(Document document, NovelRequire novelRequire, String specific_rule) throws Exception {
        MainAnalyzer urlAnalyzer = new MainAnalyzer();
        urlAnalyzer.setEngine(new JavaScriptEngine(novelRequire));
        String url = urlAnalyzer.analyze(document, specific_rule).asString();
        return StringUtils.completeUrl(url,novelRequire.getBookSourceUrl());
    }

    public synchronized static List<String> getUrls(Document document, NovelRequire novelRequire, String specific_rule) throws Exception {
        MainAnalyzer urlAnalyzer = new MainAnalyzer();
        urlAnalyzer.setEngine(new JavaScriptEngine(novelRequire));
        List<String> urls = urlAnalyzer.analyze(document, specific_rule).asStringList();
        if(urls!=null && !urls.isEmpty()){
            urls = urls.stream().map(url -> StringUtils.completeUrl(url, novelRequire.getBookSourceUrl())).collect(Collectors.toList());
        }
        return urls;
    }
}
