package com.Z.NovelReader.Processors;

import android.util.Log;

import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;

public class CatalogProcessor {
    public synchronized static NovelCatalog getCatalog(Document document, NovelRequire novelRequire) throws Exception {
        String catalogListRule = novelRequire.getRuleToc().getChapterList();
//        Matcher matcher = StringUtils.match_needReverseFlag(CatalogListRule);
//        boolean need_reverse = matcher.matches();
//        if (need_reverse)CatalogListRule = matcher.group(1);

        String chapterNameRule = novelRequire.getRuleToc().getChapterName();
        String chapterUrlRule = novelRequire.getRuleToc().getChapterUrl();

        ArrayList<String> chapTitle = new ArrayList<>();
        ArrayList<String> chapLink = new ArrayList<>();

        MainAnalyzer catalogListAnalyser = new MainAnalyzer();
        AnalyseResult catalogListResult = catalogListAnalyser.analyze(document, catalogListRule);

        MainAnalyzer catalogTitleAnalyser = new MainAnalyzer();
        AnalyseResult chapTitleResult = catalogTitleAnalyser.analyze(catalogListResult, chapterNameRule);
        chapTitle = (ArrayList<String>) chapTitleResult.asStringList();

        MainAnalyzer catalogUrlAnalyser = new MainAnalyzer();
        AnalyseResult chapUrlResult = catalogUrlAnalyser.analyze(catalogListResult, chapterUrlRule);
        chapLink = (ArrayList<String>) chapUrlResult.asStringList();

//        NovelRuleAnalyzer CatalogListAnalyser=new NovelRuleAnalyzer();
//        boolean useJson = CatalogListAnalyser.checkJsonUsage(CatalogListRule);
//        if(useJson){
//            String json = document.body().text();
//            Configuration configuration = Configuration.defaultConfiguration();
//            JsonArray catalogElements = JsonPath.using(configuration).parse(json).read(CatalogListRule);
//            for (int i = 0;i< catalogElements.size();i++) {
//                String element_json = catalogElements.get(i).toString();
//                JsonPrimitive title_json = JsonPath.using(configuration).parse(element_json).read(chapterNameRule);
//                JsonPrimitive link_json = JsonPath.using(configuration).parse(element_json).read(chapterUrlRule);
//                chapTitle.add(title_json.getAsString());
//                chapLink.add(link_json.getAsString());
//            }
//        }
//        else {
//            Elements catalogElement = CatalogListAnalyser.getElementsByRules(document, CatalogListRule);
//
//            NovelRuleAnalyzer ChapTitleAnalyser = new NovelRuleAnalyzer();
//            chapTitle = (ArrayList<String>) ChapTitleAnalyser.getObjectFromElements(catalogElement, chapterNameRule);
//            if (need_reverse) Collections.reverse(chapTitle);
//
//            NovelRuleAnalyzer ChapLinkAnalyser = new NovelRuleAnalyzer();
//            chapLink = (ArrayList<String>) ChapLinkAnalyser.getObjectFromElements(catalogElement, chapterUrlRule);
//            if (need_reverse) Collections.reverse(chapLink);
//        }
        NovelCatalog catalog_result=new NovelCatalog();
        catalog_result.setTitle(chapTitle);
        catalog_result.setLink(chapLink);
        catalog_result.completeCatalog(novelRequire.getBookSourceUrl());
        return catalog_result;
    }
}
