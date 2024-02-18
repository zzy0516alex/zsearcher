package com.Z.NovelReader.Objects.beans;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(tableName = "novel_source",indices = {@Index(value = {"book_source_url"},
        unique = true)})
public class NovelRequire implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "book_source_name")
    private String bookSourceName;

    @ColumnInfo(name = "book_source_url")
    private String bookSourceUrl;

    @ColumnInfo(name = "search_url")
    private String searchUrl;

    @ColumnInfo(name = "enabled")
    private boolean enabled;

    @ColumnInfo(name = "respond_time")
    private double respondTime;//单位ms

    @Embedded
    private ruleSearch ruleSearch;

    @Embedded
    private ruleBookInfo ruleBookInfo;

    @Embedded
    private ruleToc ruleToc;

    @Embedded
    private ruleContent ruleContent;

    @Ignore
    private Set<MainAnalyzer.RuleType> availableTypes;

    public NovelRequire() {
        this.availableTypes = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookSourceName() {
        return bookSourceName!=null?bookSourceName:"";
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getRespondTime() {
        return respondTime;
    }

    public void setRespondTime(double respondTime) {
        this.respondTime = respondTime;
    }

    public com.Z.NovelReader.Objects.beans.ruleSearch getRuleSearch() {
        return ruleSearch;
    }

    public void setRuleSearch(com.Z.NovelReader.Objects.beans.ruleSearch ruleSearch) {
        this.ruleSearch = ruleSearch;
    }

    public com.Z.NovelReader.Objects.beans.ruleBookInfo getRuleBookInfo() {
        return ruleBookInfo;
    }

    public void setRuleBookInfo(com.Z.NovelReader.Objects.beans.ruleBookInfo ruleBookInfo) {
        this.ruleBookInfo = ruleBookInfo;
    }

    public com.Z.NovelReader.Objects.beans.ruleToc getRuleToc() {
        return ruleToc;
    }

    public void setRuleToc(com.Z.NovelReader.Objects.beans.ruleToc ruleToc) {
        this.ruleToc = ruleToc;
    }

    public com.Z.NovelReader.Objects.beans.ruleContent getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(com.Z.NovelReader.Objects.beans.ruleContent ruleContent) {
        this.ruleContent = ruleContent;
    }

    public Set<MainAnalyzer.RuleType> getAvailableTypes() {
        return availableTypes;
    }

    public void setAvailableTypes(Set<MainAnalyzer.RuleType> availableTypes) {
        this.availableTypes = availableTypes;
    }

    public void judgeAvailableTypes(){
        if(!this.availableTypes.isEmpty())return;
        judgeAvailableTypes(this.searchUrl);
        Field[] fields = this.ruleSearch.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);//设置允许通过反射访问私有变量
            try {
                String rule = (String) field.get(this.ruleSearch);
                if(rule==null)continue;
                judgeAvailableTypes(rule);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        fields = this.ruleBookInfo.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);//设置允许通过反射访问私有变量
            try {
                String rule = (String) field.get(this.ruleBookInfo);
                if(rule==null)continue;
                judgeAvailableTypes(rule);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        fields = this.ruleToc.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);//设置允许通过反射访问私有变量
            try {
                String rule = (String) field.get(this.ruleToc);
                if(rule==null)continue;
                judgeAvailableTypes(rule);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        fields = this.ruleContent.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);//设置允许通过反射访问私有变量
            try {
                String rule = (String) field.get(this.ruleContent);
                if(rule==null)continue;
                judgeAvailableTypes(rule);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void judgeAvailableTypes(String rule){
        if(this.availableTypes==null)this.availableTypes = new HashSet<>();
        boolean default_type = !this.availableTypes.contains(MainAnalyzer.RuleType.Jsoup);//是否是jsoup类型
        if(rule.contains("@css:")) {
            this.availableTypes.add(MainAnalyzer.RuleType.JsoupCss);
            default_type = false;
        }
        if(rule.contains("@json:")||rule.startsWith("$.")) {
            this.availableTypes.add(MainAnalyzer.RuleType.Json);
            default_type = false;
        }
        if(rule.contains("@XPath:")||rule.startsWith("//")) {
            this.availableTypes.add(MainAnalyzer.RuleType.XPath);
            default_type = false;
        }
        if(rule.contains("<js>")||rule.contains("@js:"))this.availableTypes.add(MainAnalyzer.RuleType.JavaScript);
        if(rule.contains("##"))this.availableTypes.add(MainAnalyzer.RuleType.Regex);
        if(default_type)this.availableTypes.add(MainAnalyzer.RuleType.Jsoup);//默认类型jsoup
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public static String toJsonList(List<NovelRequire> novelRequires){
        return new Gson().toJson(novelRequires);
    }

    public static NovelRequire getNovelRequireBean(String json){
        return new Gson().fromJson(json, NovelRequire[].class)[0];
    }
    public static NovelRequire[] getNovelRequireBeans(String json){
        return new Gson().fromJson(json, NovelRequire[].class);
    }

    @Override
    public String toString() {
        return "书源ID：" + id +
                "\n 名称：" + bookSourceName +
                "\n 网址：" + bookSourceUrl +
                "\n 书籍封面：" +((ruleBookInfo!=null)?ruleBookInfo.getCoverUrl():"") +
                "\n 搜索规则：" +
                "\n    搜索链接：" + searchUrl +
                "\n    结果列表：" + ruleSearch.getBookList() +
                "\n    书名：" + ruleSearch.getName() +
                "\n    作者：" + ruleSearch.getAuthor() +
                "\n 目录规则：" +
                "\n    章节列表：" + ruleToc.getChapterList() +
                "\n    章节名称：" + ruleToc.getChapterName() +
                "\n    章节连接：" + ruleToc.getChapterUrl() +
                "\n    下个目录：" + ruleToc.getNextTocUrl() +
                "\n 内容规则：" +
                "\n    内容链接：" + ruleContent.getContent() +
                "\n    下一页：" + ruleContent.getNextContentUrl() +
                "\n    替换正则：" + ruleContent.getReplaceRegex()
                ;
    }


}
