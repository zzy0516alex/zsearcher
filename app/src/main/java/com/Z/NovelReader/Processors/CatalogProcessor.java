package com.Z.NovelReader.Processors;

import com.Z.NovelReader.NovelRoom.Novels;
import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Objects.beans.NovelCatalog;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Processors.InnerEntities.AnalyseResult;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class CatalogProcessor {
    public synchronized static NovelCatalog getCatalog(Document document, @NotNull NovelRequire novelRequire, Novels novel) throws Exception {
        String catalogListRule = novelRequire.getRuleToc().getChapterList();

        String chapterNameRule = novelRequire.getRuleToc().getChapterName();
        String chapterUrlRule = novelRequire.getRuleToc().getChapterUrl();

        ArrayList<String> chapTitle;
        ArrayList<String> chapLink;

        JavaScriptEngine engine = new JavaScriptEngine(novelRequire, novel);

        MainAnalyzer catalogListAnalyser = new MainAnalyzer();
        catalogListAnalyser.setEngine(engine);
        AnalyseResult catalogListResult = catalogListAnalyser.analyze(document, catalogListRule);

        MainAnalyzer catalogTitleAnalyser = new MainAnalyzer();
        catalogTitleAnalyser.setEngine(engine);
        AnalyseResult chapTitleResult = catalogTitleAnalyser.analyze(catalogListResult, chapterNameRule);
        chapTitle = (ArrayList<String>) chapTitleResult.asStringList();

        MainAnalyzer catalogUrlAnalyser = new MainAnalyzer();
        catalogUrlAnalyser.setEngine(engine);
        AnalyseResult chapUrlResult = catalogUrlAnalyser.analyze(catalogListResult, chapterUrlRule);
        chapLink = (ArrayList<String>) chapUrlResult.asStringList();

        if(chapTitle.size()!=chapLink.size())throw new RuntimeException("目录条目与链接数不一致");
        NovelCatalog catalog_result=new NovelCatalog();
        for (int i = 0; i < chapTitle.size(); i++) {
            NovelCatalog.CatalogItem item = new NovelCatalog.CatalogItem();
            item.Title = chapTitle.get(i);
            item.Link = chapLink.get(i);
            item.isDownloaded = false;
            catalog_result.add(item);
        }
        catalog_result.completeCatalog(novelRequire.getBookSourceUrl());
        return catalog_result;
    }
}
