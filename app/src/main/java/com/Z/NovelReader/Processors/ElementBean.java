package com.Z.NovelReader.Processors;

import java.util.List;

public class ElementBean {
    private NovelRuleAnalyzer.RuleType type;
    private String ruleContent;//js中的label
    private List<Integer> exclusions;//需要剔除的元素索引
    private int element_index=9999;//同名元素索引,用"." 隔开，在最后一位
    private String attr = "";
    private String bound = ".";//为rulecontent添加结尾符，如：#
    private String[] replacement=new String[2];
    private boolean need_advanced_match = false;// 当存在##[patten1]##[patten2]###时，且此时bean不是匹配element的类型，且包含$，为真
    private boolean need_advanced_replace = false;// 当存在##[patten1]##[patten2]###时，且此时bean不是匹配element的类型，为真

    public NovelRuleAnalyzer.RuleType getType() {
        return type;
    }

    public void setType(NovelRuleAnalyzer.RuleType type) {
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

    public String[] getReplacement() {
        return replacement;
    }

    public void setReplacement(String[] replacement) {
        this.replacement = replacement;
    }

    public boolean hasExclusions(){
        return exclusions!=null;
    }

    public boolean hasIndex(){
        return element_index!=9999;
    }

    public boolean hasReplacement(){
        if (replacement==null||replacement[0]==null)return false;
        return !"".equals(replacement[0]);
    }

    public boolean isNeed_advanced_match() {
        return need_advanced_match;
    }

    public boolean isNeed_advanced_replace() {
        return need_advanced_replace;
    }

    public void setNeed_advanced_match(boolean need_advanced_match) {
        this.need_advanced_match = need_advanced_match;
    }

    public void setNeed_advanced_replace(boolean need_advanced_replace) {
        this.need_advanced_replace = need_advanced_replace;
    }
}
