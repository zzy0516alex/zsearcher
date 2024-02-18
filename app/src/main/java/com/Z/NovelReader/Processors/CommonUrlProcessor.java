package com.Z.NovelReader.Processors;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一处理URL相关规则的处理器（目录URL、后续目录URL、封面URL...）
 */
public class CommonUrlProcessor {
    public synchronized static String getUrl(Document document, NovelRequire novelRequire, Novels novel, String specific_rule) throws Exception {
        MainAnalyzer urlAnalyzer = new MainAnalyzer();
        JavaScriptEngine engine = new JavaScriptEngine(novelRequire, novel);
        engine.preDefine("baseUrl", document.baseUri());
        urlAnalyzer.setEngine(engine);
        String url = urlAnalyzer.analyze(document, specific_rule).asString();
        return StringUtils.completeUrl(url,novelRequire.getBookSourceUrl());
    }

    public synchronized static List<String> getUrls(Document document, NovelRequire novelRequire, Novels novel, String specific_rule) throws Exception {
        MainAnalyzer urlAnalyzer = new MainAnalyzer();
        JavaScriptEngine engine = new JavaScriptEngine(novelRequire, novel);
        engine.preDefine("baseUrl", document.baseUri());
        urlAnalyzer.setEngine(engine);
        List<String> urls = urlAnalyzer.analyze(document, specific_rule).asStringList();
        if(urls!=null && !urls.isEmpty()){
            urls = urls.stream().map(url -> StringUtils.completeUrl(url, novelRequire.getBookSourceUrl())).collect(Collectors.toList());
        }
        return urls;
    }
}
