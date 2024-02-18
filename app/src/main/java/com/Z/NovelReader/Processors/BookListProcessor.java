package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Objects.beans.ruleSearch;
import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Processors.InnerEntities.AnalyseResult;
import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.Objects.beans.NovelSearchBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class BookListProcessor {
    public synchronized static ArrayList<NovelSearchBean> getSearchList(Document document, NovelRequire novelRequire) throws Exception {
        JavaScriptEngine engine = new JavaScriptEngine(novelRequire,null);

        ruleSearch ruleSearch = novelRequire.getRuleSearch();
        MainAnalyzer searchListAnalyser = new MainAnalyzer();
        searchListAnalyser.setEngine(engine);
        AnalyseResult result_search_list = searchListAnalyser.analyze(document, ruleSearch.getBookList());

        MainAnalyzer bookNameAnalyser = new MainAnalyzer();
        bookNameAnalyser.setEngine(engine);
        AnalyseResult result_book_name = bookNameAnalyser.analyze(result_search_list, ruleSearch.getName());
        ArrayList<String>BookNames = (ArrayList<String>) result_book_name.asStringList();

        MainAnalyzer bookLinkAnalyser = new MainAnalyzer();
        bookLinkAnalyser.setEngine(engine);
        AnalyseResult result_book_link = bookLinkAnalyser.analyze(result_search_list, ruleSearch.getBookUrl());
        ArrayList<String>BookLinks = (ArrayList<String>) result_book_link.asStringList();

        MainAnalyzer bookAuthorAnalyser = new MainAnalyzer();
        bookAuthorAnalyser.setEngine(engine);
        AnalyseResult result_book_author = bookAuthorAnalyser.analyze(result_search_list, ruleSearch.getAuthor());
        ArrayList<String>authors = (ArrayList<String>) result_book_author.asStringList();

        ArrayList<NovelSearchBean>searchResult=new ArrayList<>();
        for (int i=0;i<BookNames.size();i++) {
            NovelSearchBean current_book=new NovelSearchBean();
            current_book.setBookName(BookNames.get(i));
            String url=BookLinks.get(i);
            current_book.setBookInfoLink(StringUtils.completeUrl(url, novelRequire.getBookSourceUrl()));
            if (authors.size()!=0)current_book.setWriter(authors.get(i));
            current_book.setSource(novelRequire.getId());
            searchResult.add(current_book);
        }
        return searchResult;
    }
}
