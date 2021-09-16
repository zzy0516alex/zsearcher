package com.Z.NovelReader.Processors;

import android.util.Log;

import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.myObjects.beans.NovelCatalog;
import com.Z.NovelReader.myObjects.beans.NovelRequire;
import com.Z.NovelReader.myObjects.beans.NovelSearchBean;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class CatalogProcessor {
    public synchronized static NovelCatalog getCatalog(Document document, NovelRequire novelRequire) throws Exception {
        NovelRuleAnalyzer CatalogListAnalyser=new NovelRuleAnalyzer();
        Elements catalogElement = CatalogListAnalyser.getElementsByRules(document, novelRequire.getRuleToc().getChapterList());

        NovelRuleAnalyzer ChapTitleAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>ChapTitle = (ArrayList<String>) ChapTitleAnalyser.getObjectFromElements(catalogElement, novelRequire.getRuleToc().getChapterName());

        NovelRuleAnalyzer ChapLinkAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>ChapLink = (ArrayList<String>) ChapLinkAnalyser.getObjectFromElements(catalogElement, novelRequire.getRuleToc().getChapterUrl());
        Log.d("catalog processor","size:"+ChapTitle.size());
        NovelCatalog catalog_result=new NovelCatalog();
        catalog_result.setTitle(ChapTitle);
        catalog_result.setLink(ChapLink);
        catalog_result.completeCatalog(novelRequire.getBookSourceUrl());
        return catalog_result;
    }
}
