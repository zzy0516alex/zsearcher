package com.Z.NovelReader.Processors;

import android.util.Log;

import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;

public class CatalogProcessor {
    public synchronized static NovelCatalog getCatalog(Document document, NovelRequire novelRequire) throws Exception {
        String CatalogListRule = novelRequire.getRuleToc().getChapterList();
        Matcher matcher = StringUtils.match_needReverseFlag(CatalogListRule);
        boolean need_reverse = matcher.matches();
        if (need_reverse)CatalogListRule = matcher.group(1);

        NovelRuleAnalyzer CatalogListAnalyser=new NovelRuleAnalyzer();
        Elements catalogElement = CatalogListAnalyser.getElementsByRules(document, CatalogListRule);

        NovelRuleAnalyzer ChapTitleAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>ChapTitle = (ArrayList<String>) ChapTitleAnalyser.getObjectFromElements(catalogElement, novelRequire.getRuleToc().getChapterName());
        if (need_reverse) Collections.reverse(ChapTitle);

        NovelRuleAnalyzer ChapLinkAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>ChapLink = (ArrayList<String>) ChapLinkAnalyser.getObjectFromElements(catalogElement, novelRequire.getRuleToc().getChapterUrl());
        if (need_reverse) Collections.reverse(ChapLink);

        Log.d("catalog processor","size:"+ChapTitle.size());
        NovelCatalog catalog_result=new NovelCatalog();
        catalog_result.setTitle(ChapTitle);
        catalog_result.setLink(ChapLink);
        catalog_result.completeCatalog(novelRequire.getBookSourceUrl());
        return catalog_result;
    }
}
