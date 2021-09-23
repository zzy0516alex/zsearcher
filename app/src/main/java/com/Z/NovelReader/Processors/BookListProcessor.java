package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Utils.StringUtils;
import com.Z.NovelReader.myObjects.beans.NovelSearchBean;
import com.Z.NovelReader.myObjects.beans.NovelRequire;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class BookListProcessor {
    public synchronized static ArrayList<NovelSearchBean> getSearchList(Document document, NovelRequire novelRequire) throws Exception {
        NovelRuleAnalyzer SearchListAnalyser=new NovelRuleAnalyzer();
        Elements searchListElement = SearchListAnalyser.getElementsByRules(document, novelRequire.getRuleSearch().getBookList());

        NovelRuleAnalyzer BookNameAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>BookNames = (ArrayList<String>) BookNameAnalyser.getObjectFromElements(searchListElement, novelRequire.getRuleSearch().getName());

        NovelRuleAnalyzer BookLinkAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>BookLinks = (ArrayList<String>) BookLinkAnalyser.getObjectFromElements(searchListElement, novelRequire.getRuleSearch().getBookUrl());

        NovelRuleAnalyzer BookAuthorAnalyser=new NovelRuleAnalyzer();
        ArrayList<String>authors = (ArrayList<String>) BookAuthorAnalyser.getObjectFromElements(searchListElement, novelRequire.getRuleSearch().getAuthor());

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
