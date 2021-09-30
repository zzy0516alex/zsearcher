package com.Z.NovelReader;

import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.PictureThread;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String html = "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml;charset=utf-8\" />\n" +
                "    <title>三寸人间最新章节_耳根-顶点笔趣阁手机站</title>\n" +
                "    <meta name=\"generator\" content=\"顶点笔趣阁手机站\" />\n" +
                "    <meta id=\"ctl00_metaKeywords\" name=\"keywords\" content=\"三寸人间\"/>\n" +
                "    <meta id=\"ctl00_metaDescription\" name=\"description\" content=\"三寸人间无弹窗最新章节由网友提供，《三寸人间》情节跌宕起伏、扣人心弦，是一本情节与文笔俱佳的玄幻小说，顶点笔趣阁免费提供三寸人间最新清爽干净的文字章节在线阅读。\" />\n" +
                "    <meta http-equiv=\"Cache-Control\" content=\"no-siteapp\" />\n" +
                "    <meta http-equiv=\"Cache-Control\" content=\"no-transform\" />\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no\" />\n" +
                "    <meta name=\"format-detection\" content=\"telephone=no\" />\n" +
                "    <meta name=\"apple-mobile-web-app-capable\" content=\"yes\" />\n" +
                "    <meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black-translucent\" />\n" +
                "    <meta name=\"author\" content=\"耳根\" />\n" +
                "    <meta property=\"og:type\" content=\"novel\" />\n" +
                "    <meta property=\"og:title\" content=\"三寸人间\"/>\n" +
                "    <meta property=\"og:url\" content=\"https://qq.hehuamei.com/niyusnhdyua/5583/\" />\n" +
                "    <meta property=\"og:description\" content=\"举头三尺无神明，掌心三寸是人间。这是耳根继《仙逆》《求魔》《我欲封天》《一念永恒》后，创作的第五部长篇小说《三寸人间》。\" />\n" +
                "    <meta property=\"og:image\" content=\"https://app.hehuamei.com/files/article/image/5/5583/5583s.jpg\"/>\n" +
                "    <meta property=\"og:novel:category\" content=\"玄幻小说\"/>\n" +
                "    <meta property=\"og:novel:status\" content=\"连载中\"/>\n" +
                "    <meta property=\"og:novel:author\" content=\"耳根\"/>\n" +
                "    <meta property=\"og:novel:book_name\" content=\"三寸人间\"/>\n" +
                "    <meta property=\"og:novel:read_url\" content=\"https://qq.hehuamei.com/niyusnhdyua/5583/\"/>\n" +
                "    <meta property=\"og:novel:update_time\" content=\"2021-08-27 13:05:39\" />\n" +
                "    <meta property=\"og:novel:latest_chapter_name\" content=\"第一四五七章 终是一场虚幻灭\" />\n" +
                "    <meta property=\"og:novel:latest_chapter_url\" content=\"https://qq.hehuamei.com/niyusnhdyua/5583/82426220.html\" />\n" +
                "    <link rel=\"stylesheet\" href=\"/css/reset.css\" />\n" +
                "    <link rel=\"stylesheet\" href=\"/css/bookinfo.css\" />\n" +
                " <script type=\"application/ld+json\">\n" +
                "        {\n" +
                "            \"@context\": \"https://ziyuan.baidu.com/contexts/cambrian.jsonld\",\n" +
                "            \"@id\": \"https://qq.hehuamei.com/niyusnhdyua/5583/\",\n" +
                "            \"appid\": \"1601328506958234\",\n" +
                "            \"title\": \"三寸人间最新章节_耳根-顶点笔趣阁手机站\",\n" +
                "            \"images\": [\n" +
                "                \"https://app.hehuamei.com/files/article/image/5/5583/5583s.jpg\"\n" +
                "            ], //请在此处添加希望在搜索结果中展示图片的url，可以添加0个、1个或3个url\n" +
                "            \"pubDate\": \"2021-08-27CST13:05:39\" // 需按照yyyy-mm-ddThh:mm:ss格式编写时间，字母T不能省去\n" +
                "        }\n" +
                "    </script>\n" +
                "    <script id='mob' type='text/javascript' charset='utf-8' src='/gg/app.js'></script>\n" +
                "</head>";
//        String s1 = "https://www.babayu.com/kanshu/a";
//        String s2 = "https://www.babayu.com/kanshu/b";
        String s1 = "https://www.babayu.com/";
        String s2 = "https://www.babayu.com/kanshu/";
        String s3 = "/kanshu/61651.html";
        String s4 = "65161.html";
        String s5 = "www.babayu.com/kanshu/65161.html";
        String s6 = "https://www.babayu.com/kanshu/65161.html";
        String ss1 = StringUtils.completeUrl(s3, s2);
        System.out.println("ss1 = " + ss1);
        String ss2 = StringUtils.completeUrl(s4, s2);
        System.out.println("ss2 = " + ss2);
        String ss3 = StringUtils.completeUrl(s5, s2);
        System.out.println("ss3 = " + ss3);
        String ss4 = StringUtils.completeUrl(s6, s2);
        System.out.println("ss4 = " + ss4);
//        Pattern pattern = Pattern.compile("^-(.+)");
//        Matcher matcher = pattern.matcher(t);
//        boolean matches = matcher.matches();
//        System.out.println("matches = " + matches);
//        System.out.println(matcher.group(1));
    }
}