package com.Z.NovelReader.Processors;

import java.util.List;

public class JsoupSelectorBean {
    public enum SelectorType {CLASS,TAG,ID,META, MATCH_TEXT,ATTR,TEXT,TEXT_NODES,HTML,OMIT_TAG}
    private SelectorType type;
    private String ruleContent;//html中的label
    private List<Integer> exclusions;//需要剔除的元素索引,0是第1个,负数为倒数序号,-1最后一个
    private int element_index = -1;//同名元素索引,用"." 隔开，在最后一位
    private String attr = "";
    private String bound = ".";//为rule content添加结尾符，如：#或.

    public SelectorType getType() {
        return type;
    }

    public boolean isTypeOfText(){
        return this.type == SelectorType.TEXT ||
                this.type == SelectorType.ATTR ||
                this.type == SelectorType.TEXT_NODES ||
                this.type == SelectorType.HTML;
    }

    public boolean isTypeOfElement(){
        return !isTypeOfText();
    }

    public void setType(SelectorType type) {
        this.type = type;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }

    public List<Integer> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<Integer> exclusions) {
        this.exclusions = exclusions;
    }

    public int getElement_index() {
        return element_index;
    }

    public void setElement_index(int element_index) {
        this.element_index = element_index;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getBound() {
        return bound;
    }

    public void setBound(String bound) {
        this.bound = bound;
    }

    public boolean hasExclusions(){
        return exclusions!=null;
    }

    public boolean hasIndex(){
        return element_index != -1;
    }

}
