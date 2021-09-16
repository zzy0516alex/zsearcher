package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Utils.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NovelRuleAnalyzer {
    public enum RuleType {CLASS,Tag,ID,ATTR,TEXT,TEXT_NODES,HTML,OMIT_TAG}
    private ElementBean[] beans;
    //public String[] TAG_NAME={"a","p","div","span","li","h1","h2","h3","h4","h5","h6","em","dd"};

    //notice: 构建bean所需的方法：

    /**
     *
     * @param rule 已经按@分离后的段
     * @return 查询类型
     */
    private RuleType getRuleType(String rule){
        if (rule.contains("@"))
            throw new IllegalArgumentException("查询语句分离不完全");
        if (rule.contains(".")){
            if (rule.contains("class."))return RuleType.CLASS;
            else if (rule.contains("tag."))return RuleType.Tag;
            else if (rule.contains("id."))return RuleType.ID;
            else return RuleType.OMIT_TAG;//缺省TAG标志的tag类型
        }else {
            if (rule.contains("href"))return RuleType.ATTR;
            else if (rule.contains("alt"))return RuleType.ATTR;
            else if (rule.contains("src"))return RuleType.ATTR;
            else if (rule.contains("content"))return RuleType.ATTR;
            else if (rule.contains("text"))return RuleType.TEXT;
            else if (rule.contains("textNodes"))return RuleType.TEXT_NODES;
            else if (rule.contains("html"))return RuleType.HTML;
            else return RuleType.OMIT_TAG;
        }
    }

    /**
     * 添加主查询文本
     * @param bean 查询容器
     * @param layer 原文
     * @throws Exception 原文格式错误
     */
    private void getRuleContentByType(ElementBean bean, String layer) throws Exception {
        switch(bean.getType()){
            case CLASS:
                String[] parts_class = layer.split("\\.");
                String rule_content = layer.replace("class.", "");
                if (parts_class.length>2){
                    rule_content=rule_content.split("\\.")[0];
                    bean.setElement_index(Integer.parseInt(parts_class[2]));
                }
                bean.setBound(".");
                if (rule_content.contains(" "))rule_content=rule_content.replace(" ",".");
                bean.setRuleContent(rule_content);
                break;
            case Tag:{
                String[] parts_tag = layer.split("\\.");
                if (parts_tag.length<=1||parts_tag.length>3)throw new Exception("layer 格式错误");
                bean.setRuleContent(parts_tag[1].split("!")[0]);
                bean.setBound("");
                if (parts_tag.length==3)bean.setElement_index(Integer.parseInt(parts_tag[2]));
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
                if (layer.contains(".")) {
                    String[] parts_omit_tag = layer.split("\\.");
                    if (StringUtils.isNumber(parts_omit_tag[1])) {
                        bean.setRuleContent(parts_omit_tag[0]);
                        bean.setElement_index(Integer.parseInt(parts_omit_tag[1]));
                    }
                    else bean.setRuleContent(layer);
                }else{
                    bean.setRuleContent(layer);
                }
                bean.setBound("");
            }
                break;
            default:
        }
    }

    private List<Integer> getElementExclusions(String layer) throws Exception {
        List<Integer> result=new ArrayList<>();
        if (!layer.contains("!"))return null;
        String[] parts = layer.split("!");
        if (parts.length!=2)throw new Exception("layer 格式错误");
        for (String p:parts[1].split("\\|")) {
            result.add(Integer.parseInt(p));
        }
        return result;
    }

    private String getContentReplacement(ElementBean bean,String layer) throws Exception {
        String[] replacement=new String[2];
        replacement[0]="";
        replacement[1]="";
        if (!layer.contains("##"))return layer;
        //if (bean.getType()==RuleType.NONE)bean.setType(RuleType.TEXT);
        String[] parts = layer.split("##");
        if (parts.length>4)throw new Exception("layer 格式错误");
        replacement[0]=parts[1];
        if (parts.length>=3)replacement[1]=parts[2];
        bean.setReplacement(replacement);
        return parts[0];
    }

    /**
     * 通过原始输入构建bean，格式化规则
     * @param raw_rule
     * @throws Exception
     */
    public void splitLayers(String raw_rule) throws Exception {
        String[] layers;
        layers=raw_rule.split("@");
        beans=new ElementBean[layers.length];
        for (int i = 0; i < layers.length; i++) {
            String currentlayer=layers[i];
            ElementBean b=new ElementBean();
            //set type
            b.setType(getRuleType(currentlayer));
            //set replacement
            currentlayer=getContentReplacement(b,currentlayer);
            //set content
            getRuleContentByType(b,currentlayer);
            //set exclusions
            b.setExclusions(getElementExclusions(currentlayer));
            beans[i]=b;
        }
    }

    //notice: select element所需的方法:

    /**
     * 从根文档中提取符合要求的elements
     * @param document
     * @param raw_rule 原始规则
     * @return root elements(父元素)
     * @throws Exception
     */
    public Elements getElementsByRules(Document document, String raw_rule) throws Exception {
        splitLayers(raw_rule);
        if (beans.length<=0||beans.length>3)throw new Exception("beans 生成错误");
        SelectedElements elementsFromBean = getElementsFromBean(document);
        if (elementsFromBean.bean_left!=-1)throw new Exception("应该使用getObjectFromElements");
        return elementsFromBean.elements;
    }

    /**
     * 根据规则选择子element
     * @param root 父elements
     * @return 解析到的最后一层element & 剩余未使用的规则
     * @throws Exception
     */
    private SelectedElements getElementsFromBean(Elements root) throws Exception {
        Elements elements=root;
        int bean_left=-1;
        //第一层
        if (isTypeofElement(beans[0]))elements = getSelectedElements(root,beans[0]);
        else bean_left=0;
        //第二层
        if (beans.length>=2){
            if (isTypeofElement(beans[1]))elements = getSelectedElements(elements, beans[1]);
            else if (bean_left ==-1){
                bean_left=1;
            }else throw new Exception("rule 格式错误");
        }
        //第三层
        if (beans.length==3){
            if (isTypeofElement(beans[2]))elements = getSelectedElements(elements,beans[2]);
            else if (bean_left ==-1){
                bean_left=2;
            }else throw new Exception("rule 格式错误");
        }
        //if (elements.size()==0)bean_left=-1;//不符合要求的匹配不再加入列表中
        return new SelectedElements(elements,bean_left);
    }

    //重载函数，使之匹配单个element
    private SelectedElements getElementsFromBean(Element element) throws Exception{
        return getElementsFromBean(new Elements(element));
    }

    /**
     * 从elements中根据规则提取字符串列表
     * @param eles 一组元素
     * @param raw_rule 原始规则
     * @return 符合要求的字符串列表
     * @throws Exception
     */
    public List<String> getObjectFromElements(Elements eles, String raw_rule) throws Exception{
        splitLayers(raw_rule);
        if (beans.length<=0||beans.length>3)throw new Exception("beans 生成错误");
        List<String> object=new ArrayList<>();
        for (Element ele : eles) {
            SelectedElements selectedElements = getElementsFromBean(ele);
            int bean_left=selectedElements.bean_left;
            if (bean_left!=-1 && !selectedElements.isEmpty()){
                ElementBean bean=beans[bean_left];
                switch(bean.getType()){
                    case TEXT: {
                        String item = selectedElements.elements.text();
                        object.add(getReplacedItem(bean, item));
                    }
                        break;
                    case ATTR: {
                        String item = selectedElements.elements.attr(bean.getAttr());
                        object.add(getReplacedItem(bean, item));
                    }
                        break;
                    case TEXT_NODES:{
                        Element content_element = selectedElements.elements.get(0);
                        List<TextNode> paras = content_element.textNodes();
                        StringBuilder content=new StringBuilder();
                        for (TextNode para:paras) {
                            content.append("    ");
                            content.append(para.text());
                            content.append("\n");
                            content.append("\n");
                        }
                        object.add(getReplacedItem(bean,content.toString()));
                    }
                        break;
                    case HTML:{
                        StringBuilder content=new StringBuilder();
                        for (Element para : selectedElements.elements) {
                            content.append("    ");
                            content.append(para.text());
                            content.append("\n");
                            content.append("\n");
                        }
                        object.add(getReplacedItem(bean,content.toString()));
                    }
                        break;
                    default:{
                        String item = selectedElements.elements.text();
                        object.add(getReplacedItem(bean, item));
                    }
                }
            }
        }

        return object;
    }

    private String getReplacedItem(ElementBean bean, String item) {
        String result=item;
        if (bean.hasReplacement())
            result = item.replace(bean.getReplacement()[0],bean.getReplacement()[1]);
        return result;
    }
    //判断接下来解析的目标是否仍是elements
    private boolean isTypeofElement(ElementBean bean) {
        return bean.getType()!= RuleType.TEXT &&
                bean.getType()!= RuleType.ATTR &&
                bean.getType()!=RuleType.TEXT_NODES &&
                bean.getType()!=RuleType.HTML;
    }

    private Elements getSelectedElements(Elements elements, ElementBean bean) {
        String selection=bean.getBound()+bean.getRuleContent();
        try{
            elements = elements.select(selection);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (bean.hasExclusions() && elements.size()!=0)
            elements=getElementsAfterExclusion(bean.getExclusions(),elements);
        if (bean.hasIndex() && elements.size()!=0)
            elements=new Elements(elements.get(bean.getElement_index()));
        return elements;
    }
    private Elements getSelectedElements(Element element, ElementBean bean) {
        return getSelectedElements(new Elements(element),bean);
    }

    private Elements getElementsAfterExclusion(List<Integer> excludes,Elements elements) {
        List<Element>elementList=new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            if (excludes.contains(i))continue;
            elementList.add(elements.get(i));
        }
        return new Elements(elementList);
    }

    class SelectedElements{
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
}
