package com.Z.NovelReader.Processors.Analyzers;

import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.Z.NovelReader.Processors.InnerEntities.JsoupSelectorBean;
import com.Z.NovelReader.Utils.StringUtils;

import com.Z.NovelReader.Processors.InnerEntities.JsoupSelectorBean.SelectorType;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsoupAnalyzer {
    private JsoupSelectorBean[] beans;

    static class SelectedElements{
        public Elements elements;
        public int bean_left;

        public SelectedElements(Elements elements, int bean_left) {
            this.elements = elements;
            this.bean_left = bean_left;
        }
        public boolean isEmpty(){
            return elements.size()==0;
        }
    }

    /**
     * 通过原始规则输入构建bean，格式化规则 (注意已经剥离正则和js)
     * @param raw_rule 原始Jsoup selector规则
     * @throws Exception 抛出无法解析规则的异常
     */
    private void splitLayers(String raw_rule) throws Exception {
        String[] layers;
        if (raw_rule.startsWith("@"))
            raw_rule = raw_rule.substring(1);
        layers=raw_rule.split("@");
        beans=new JsoupSelectorBean[layers.length];
        for (int i = 0; i < layers.length; i++) {
            String currentlayer=layers[i];
            JsoupSelectorBean b=new JsoupSelectorBean();
            //set type
            setSelectorType(currentlayer,b);
            //set exclusions
            currentlayer = setElementExclusions(b,currentlayer);
            //set content
            setRuleContent(b,currentlayer);
            beans[i]=b;
        }
    }

    /**
     * @param rule 已经按@分离后的段(layer)
     * @return selector类型
     */
    private void setSelectorType(String rule, JsoupSelectorBean bean)throws Exception{
        if (rule.contains("@"))
            throw new RuleProcessorException("查询语句分离不完全");
        if (rule.contains(".")){
            if (rule.contains("class."))bean.setType(SelectorType.CLASS);
            else if (rule.contains("tag."))bean.setType(SelectorType.TAG);
            else if (rule.contains("id."))bean.setType(SelectorType.ID);
            else if (rule.contains("text."))bean.setType(SelectorType.MATCH_TEXT);//匹配包含特定文本的元素
            else bean.setType(SelectorType.OMIT_TAG);//缺省TAG标志的tag类型
        }else if (match_meta(rule)){
            bean.setType(SelectorType.META);//包含[property=...] 的 <meta> 标签类型
        } else if (match_attr(rule)) {
            bean.setType(SelectorType.ATTR);
        }
        else {
            if (rule.contains("textNodes"))bean.setType(SelectorType.TEXT_NODES);
            else if (rule.contains("text"))bean.setType(SelectorType.TEXT);
            else if (rule.contains("html"))bean.setType(SelectorType.HTML);
            else bean.setType(SelectorType.OMIT_TAG);
        }
    }

    //是否符合匹配meta标签的情况(首尾都是[])
    private boolean match_meta(String rule){
        Pattern pattern = Pattern.compile("^\\[.+\\]$");
        Matcher matcher = pattern.matcher(rule);
        return matcher.matches();
    }

    private boolean match_attr(String rule){
        String[] label_keys = new String[]{"href","alt","src","content"};
        boolean match = false;
        for (String label_key : label_keys) {
            if (rule.equals(label_key)) {
                match = true;break;
            }
        }
        return match;
    }

    private String setElementExclusions(JsoupSelectorBean bean, String layer) throws Exception {
        List<Integer> exclusions=new ArrayList<>();
        if (!layer.contains("!"))return layer;
        String[] parts = layer.split("!");
        if (parts.length!=2)throw new RuleProcessorException("layer 格式错误");
        for (String p:parts[1].split("[|:]")) {
            exclusions.add(Integer.parseInt(p));
        }
        bean.setExclusions(exclusions);
        return parts[0];
    }

    /**
     * 添加主Selector文本
     * @param bean 查询容器
     * @param layer 原文
     * @throws Exception 原文格式错误
     */
    private void setRuleContent(JsoupSelectorBean bean, String layer) throws Exception {
        switch(bean.getType()){
            case CLASS: {
                String[] parts_class = layer.split("\\.");
                String rule_content = layer.replace("class.", "");
                if (parts_class.length > 2) {
                    rule_content = rule_content.split("\\.")[0];
                    bean.setElement_index(Integer.parseInt(parts_class[2]));
                }
                bean.setBound(".");
                if (rule_content.contains(" ")) rule_content = rule_content.replace(" ", ".");
                bean.setRuleContent(rule_content);
            }
            break;
            case TAG:{
                String[] parts_tag = layer.split("\\.");
                if (parts_tag.length<=1||parts_tag.length>3)throw new RuleProcessorException("layer 格式错误");
                bean.setRuleContent(parts_tag[1]);
                bean.setBound("");
                if (parts_tag.length==3){
                    if (StringUtils.isNumber(parts_tag[2]))
                        bean.setElement_index(Integer.parseInt(parts_tag[2]));
                    else bean.setRuleContent(parts_tag[1]+"."+parts_tag[2]);
                }
            }
            break;
            case MATCH_TEXT:{
                String[] parts_text_own = layer.split("\\.");
                if (parts_text_own.length != 2)throw new RuleProcessorException("layer 格式错误");
                bean.setRuleContent(String.format(":containsOwn(%1$s)",
                        parts_text_own[1]));
                bean.setBound("");
            }
            break;
            case ID:{
                bean.setRuleContent(layer.replace("id.",""));
                bean.setBound("#");
            }
            break;
            case ATTR:{
                bean.setAttr(layer);
            }
            break;
            case OMIT_TAG:{
                // 1. span.0
                // 2. p
                // 3. div.content
                String content = layer;
                if (layer.contains(".")) {
                    String[] parts_omit_tag = layer.split("\\.");
                    if (StringUtils.isNumber(parts_omit_tag[1])) {
                        //bean.setRuleContent(parts_omit_tag[0]);
                        bean.setElement_index(Integer.parseInt(parts_omit_tag[1]));
                        content = parts_omit_tag[0];
                    }
                }
                bean.setRuleContent(content);
                bean.setBound("");
            }
            break;
            case META:{
                bean.setBound("meta");
                bean.setRuleContent(layer);
            }
            break;
            default:
        }
    }

    /**
     * 根据规则选择子element，直到规则不能再被解析为element
     * @param root 父elements
     * @return 解析到的最后一层element & 剩余未使用的规则的索引
     * @throws Exception
     */
    private SelectedElements getElementsFromBean(Elements root) throws Exception {
        Elements elements = root;
        int last_bean_index = -1;
        //第一层
//        if (beans[0].isTypeOfElement())elements = selectElements(root,beans[0]);
//        else last_bean_index=0;
        //后续的层
        for (int i = 0; i < beans.length; i++) {
            JsoupSelectorBean bean = beans[i];
            if (bean.isTypeOfElement()){//仍能返回element类型
                elements = selectElements(elements,bean);
            }else {//已经不是可以select的element
                last_bean_index = i;
                break;
            }
        }
        //若到最后一层还是能解析出element，则last_bean_index 为 -1
        return new SelectedElements(elements, last_bean_index);
    }

    /**
     * 根据jsoup select规则从一系列标签中选择所需的elements
     * @param elements 父标签
     * @param bean 规则集合
     * @return 选择出的子标签
     */
    private Elements selectElements(Elements elements, JsoupSelectorBean bean) {
        String selection=bean.getBound()+bean.getRuleContent();
        elements = elements.select(selection);
        if (bean.hasExclusions() && elements.size()!=0)
            elements=getElementsAfterExclusion(bean.getExclusions(),elements);
        if (bean.hasIndex() && elements.size()!=0)
            elements=getElementsByIndex(bean.getElement_index(),elements);
        return elements;
    }

    //根据筛选规则排除 element
    private Elements getElementsAfterExclusion(List<Integer> excludes,Elements elements) {
        List<Element>elementList=new ArrayList<>();
        //处理倒数序号的情况
        for (int i = 0; i < excludes.size(); i++) {
            Integer exclude_index = excludes.get(i);
            if (exclude_index<0)
                excludes.set(i, elements.size()+exclude_index);
        }
        //按序号排除element
        for (int i = 0; i < elements.size(); i++) {
            if (excludes.contains(i))continue;
            elementList.add(elements.get(i));
        }
        return new Elements(elementList);
    }

    //根据选择规则选中 element
    private Elements getElementsByIndex(int index,Elements elements){
        if (index>=0){
            return new Elements(elements.get(index));
        }else {
            int i=elements.size()+index;
            return new Elements(elements.get(i));
        }
    }

    /**
     * 从elements中根据规则提取字符串列表
     * @param eles 一组元素
     * @param raw_rule 原始规则
     * @return 可能为Elements类型，可能为String类型，也可能为List<>类型
     * @throws Exception
     */
    public Object getObjectFromElements(Elements eles, String raw_rule) throws Exception{
        Object object;
        if (eles.size()==0)
            return null;
        if (raw_rule.equals("")){
            if (eles.size()==1)return eles.get(0).text();
            else return eles.stream().map(Element::text).collect(Collectors.toList());
        }
        splitLayers(raw_rule);
        if (beans.length<=0)throw new RuleProcessorException("beans 生成错误");
        if (eles.size()==1){
            //为一整块的内容，可以解析出
            SelectedElements selectedElements = getElementsFromBean(eles);
            int last_bean_index=selectedElements.bean_left;
            if (selectedElements.bean_left == -1){
                object = selectedElements.elements;
                return object;//返回Elements类型
            }
            else {
                object = getTextContentFromElement(selectedElements, beans[last_bean_index]);
                return object;//返回string类型
            }
        }
        List<String>item_list = new ArrayList<>();
        for (Element ele : eles) {
            SelectedElements selectedElements = getElementsFromBean(new Elements(ele));
            //selected_elements.add(selectedElements.elements);
            int last_bean_index=selectedElements.bean_left;
            if (last_bean_index!=-1 && !selectedElements.isEmpty()){
                String current_item = getTextContentFromElement(selectedElements, beans[last_bean_index]);
                item_list.add(current_item);
            }
        }
        object = item_list;
        return object;//返回string list类型
    }

    /**
     * 从element中提取文本信息
     * @param selectedElements 已筛选的element
     * @param bean 规则集合
     * @return 从element中提取的文本
     */
    private String getTextContentFromElement(SelectedElements selectedElements, JsoupSelectorBean bean) throws RuleProcessorException {
        String current_item = "null:null";
        switch(bean.getType()){
            case TEXT: {
                current_item = selectedElements.elements.text();
            }
            break;
            case ATTR: {
                current_item = selectedElements.elements.attr(bean.getAttr());
            }
            break;
            case TEXT_NODES:{
                Element content_element = selectedElements.elements.get(0);
                List<TextNode> paras = content_element.textNodes();
                StringBuilder content=new StringBuilder();
                for (TextNode para:paras) {
                    content.append(para.text());
                    content.append("\n");
                }
                current_item = content.toString();
            }
            break;
            case HTML:{
                StringBuilder content=new StringBuilder();
                for (Element para : selectedElements.elements) {
                    content.append(para.html());
                    content.append("\n");
                }
                current_item = StringUtils.styleHTML_to_styleTXT(content.toString());
            }
            break;
        }
        if (current_item.equals("null:null"))throw new RuleProcessorException("unhandled element type");
        return current_item;
    }
}
