package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

public class ContentProcessor {

    public static NovelContentBean getNovelContent(Document document, NovelRequire novelRequire,String root_url) throws Exception {
        NovelRuleAnalyzer contentAnalyzer=new NovelRuleAnalyzer();
        contentAnalyzer.setContent(true);
        List<String> content_list = contentAnalyzer.getObjectFromElements(new Elements(document),
                novelRequire.getRuleContent().getContent());

        NovelContentBean contentBean = new NovelContentBean();
        if (content_list.size()!=0) contentBean.setContent(content_list.get(0));
        else contentBean.setContent("书源规则出错！");

        String next_page_rule = novelRequire.getRuleContent().getNextContentUrl();
        if (next_page_rule!=null && !"".equals(next_page_rule)) {
            NovelRuleAnalyzer nextPageAnalyzer = new NovelRuleAnalyzer();
            List<String> nextPage_list = nextPageAnalyzer.getObjectFromElements(new Elements(document), next_page_rule);
            if (nextPage_list.size()!=0) {
                String url = StringUtils.completeUrl(nextPage_list.get(0), root_url);
                contentBean.setNextPageURL(url);
            }
        }

        String replacement_rule = novelRequire.getRuleContent().getReplaceRegex();
        if (replacement_rule!=null && !"".equals(replacement_rule)) {
            ElementBean bean = new ElementBean();
            NovelRuleAnalyzer replacementAnalyzer = new NovelRuleAnalyzer();
            replacementAnalyzer.getContentReplacement(bean,replacement_rule);
            String replacedContent = replacementAnalyzer.getReplacedItem(bean, contentBean.getContent());
            contentBean.setContent(replacedContent);
        }
        //取空行优化
        contentBean.setContent(
                StringUtils.removeEmptyLine(contentBean.getContent()));
        return contentBean;
    }
}
