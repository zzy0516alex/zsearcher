package com.Z.NovelReader;

import com.Z.NovelReader.Processors.JavaScriptEngine;
import com.Z.NovelReader.Processors.NovelRuleAnalyzer;
import com.Z.NovelReader.Threads.GetCoverThread;
import com.Z.NovelReader.Threads.PictureThread;
import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.net.URLEncoder;
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
        //double v = StringUtils.compareStrings("第一百八十章 章节名", "第一百七十九章 章节名");
        String s = StringUtils.deleteSpaceInStart(" 第 一 百八十章 章节名");
        System.out.println("expr=" + s);
    }
}