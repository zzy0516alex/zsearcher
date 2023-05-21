package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Objects.beans.NovelContentBean;
import com.Z.NovelReader.Objects.beans.NovelRequire;
import com.Z.NovelReader.Objects.beans.ruleContent;
import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.mozilla.javascript.tools.jsc.Main;

import java.util.ArrayList;
import java.util.List;

public class ContentProcessor {

    public static NovelContentBean getNovelContent(Document document, NovelRequire novelRequire,String root_url) throws Exception {
        ruleContent content_rule = novelRequire.getRuleContent();

        MainAnalyzer contentAnalyzer = new MainAnalyzer();
        AnalyseResult content_result = contentAnalyzer.analyze(document, content_rule.getContent());
//        NovelRuleAnalyzer contentAnalyzer=new NovelRuleAnalyzer();
//        contentAnalyzer.isContent(true);
//        List<String> content_list = contentAnalyzer.getObjectFromElements(new Elements(document),
//                novelRequire.getRuleContent().getContent());

        NovelContentBean contentBean = new NovelContentBean();
        String content = content_result.asString();
        if (!"".equals(content)) contentBean.setContent(content);
        else throw new RuleProcessorException("解析返回内容为空");

        String next_page_rule = content_rule.getNextContentUrl();
        if (next_page_rule!=null && !"".equals(next_page_rule)) {
            MainAnalyzer nextPageUrlAnalyzer = new MainAnalyzer();
            AnalyseResult nextPageResult = nextPageUrlAnalyzer.analyze(document, next_page_rule);
//            NovelRuleAnalyzer nextPageAnalyzer = new NovelRuleAnalyzer();
//            List<String> nextPage_list = nextPageAnalyzer.getObjectFromElements(new Elements(document), next_page_rule);
//            ArrayList<Elements> next_page_trigger = nextPageAnalyzer.getSelected_elements();
            if (!"".equals(nextPageResult.asString())) {
                String url = StringUtils.completeUrl(nextPageResult.asString(), root_url);
                contentBean.setNextPageURL(url);
//                if (!"".equals(nextPage_list.get(0))) {
//                    String url = StringUtils.completeUrl(nextPage_list.get(0), root_url);
//                    contentBean.setNextPageURL(url);
//                    contentBean.setTriggerName(next_page_trigger.get(0).text());
//                }
            }
        }

        String replacement_rule = novelRequire.getRuleContent().getReplaceRegex();
        if (replacement_rule!=null && !"".equals(replacement_rule)) {
            MainAnalyzer contentReplaceAnalyzer = new MainAnalyzer();
            AnalyseResult content_replaced_result = contentReplaceAnalyzer.analyze(content_result, replacement_rule);
//            ElementBean bean = new ElementBean();
//            NovelRuleAnalyzer replacementAnalyzer = new NovelRuleAnalyzer();
//            replacementAnalyzer.getContentReplacement(bean,replacement_rule);
//            String replacedContent = replacementAnalyzer.getReplacedItem(bean, contentBean.getContent());
            contentBean.setContent(content_replaced_result.asString());
        }
        //去空行优化
        contentBean.setContent(
                removeEmptyLine(contentBean.getContent()));
        //去除多余空格
        contentBean.setContent(
                removeDuplicatedSpace(contentBean.getContent()));
        //首行缩进
        contentBean.setContent(
                shrinkFirstRow(contentBean.getContent()));
        return contentBean;
    }

    /**
     * 去除大于等于3个的连续空行，替换为2个空行
     * @param origin 原始文本
     * @return 优化后的文本
     */
    public static String removeEmptyLine(String origin){
        return origin.replaceAll("(\r?\n(\\s*\r?\n){2,})", "\n\n");
    }

    /**
     * 删除各种类型的空字符，替换为空格
     * @param origin
     * @return
     */
    public static String removeDuplicatedSpace(String origin){
        //32 | -128 | -29 | &nbsp
        return origin.replaceAll("[ 　\u00a0]{3,}", "\u3000\u3000");
    }

    /**
     * 实现首行缩进
     * @param origin
     * @return
     */
    public static String shrinkFirstRow(String origin){
        String s = origin.replaceAll("\r?\n[ ]{0,2}", "\n\u3000\u3000");
        if (StringUtils.isStartWithNoSpace(s))
            s = "\u3000\u3000" + s;
        return removeDuplicatedSpace(s);
    }
}
