package com.Z.NovelReader.Processors;

import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.Z.NovelReader.Utils.StringUtils;
import com.jayway.jsonpath.JsonPath;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//书源解析类，参考规则：https://alanskycn.gitee.io/teachme/Rule/source.html
public class NovelRuleAnalyzer {
    public enum RuleType {CLASS,Tag,ID,META,TEXT_OWN,ATTR,TEXT,TEXT_NODES,HTML,OMIT_TAG,REGEX_MATCH}
    //script表示<js> </js>包裹的js代码，marker表示@js开头的js代码
    //only表示有且仅有js代码，follow表示需要对上一步的结果(result变量进行js脚本处理)，fail表示js脚本格式不识别或执行出错
    public enum JSType {SCRIPT_ONLY, SCRIPT_FOLLOW, PARSE_FAIL, MARKER_ONLY}

    private ElementBean[] beans;
    private ArrayList<Elements>selected_elements = new ArrayList<>();
    private boolean isContent = false;//如果需要获取章节内容，为真，进行特别优化
    //For javascript
    private boolean JsFlag = false;//是否有js脚本需要处理
    private JSType jsType;
    private String jsCode;
    private String jsResult;
    private JavaScriptEngine engine;
    //For jsonpath
    private boolean JsonFlag = false;//是否是

    public void isContent(boolean content) {
        isContent = content;
    }

    public void setEngine(JavaScriptEngine engine) {
        this.engine = engine;
    }

//    public void setRule(NovelRequire rule) {
//        this.rule = rule;
//        this.engine = new JavaScriptEngine(rule);
//    }

    //notice: 构建bean所需的方法：

    /**
     *
     * @param rule 已经按@分离后的段
     * @return 查询类型
     */
    private RuleType getRuleType(String rule)throws Exception{
        if (rule.contains("@"))
            throw new RuleProcessorException("查询语句分离不完全");
        if (rule.contains("##"))rule = rule.split("##")[0];//净化，防止正则污染
        if (rule.contains(".")){
            if (rule.contains("class."))return RuleType.CLASS;
            else if (rule.contains("tag."))return RuleType.Tag;
            else if (rule.contains("id."))return RuleType.ID;
            else if (rule.contains("text."))return RuleType.TEXT_OWN;//匹配包含特定文本的元素
            else return RuleType.OMIT_TAG;//缺省TAG标志的tag类型
        }else if (StringUtils.match_meta(rule)){
            return RuleType.META;
        } else {
            if (rule.contains("href"))return RuleType.ATTR;
            else if (rule.contains("alt"))return RuleType.ATTR;
            else if (rule.contains("src"))return RuleType.ATTR;
            else if (rule.contains("content"))return RuleType.ATTR;
            else if (rule.contains("textNodes"))return RuleType.TEXT_NODES;
            else if (rule.contains("text"))return RuleType.TEXT;
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
            case Tag:{
                String[] parts_tag = layer.split("\\.");
                if (parts_tag.length<=1||parts_tag.length>3)throw new RuleProcessorException("layer 格式错误");
                bean.setRuleContent(parts_tag[1]/*.split("!")[0]*/);
                bean.setBound("");
                if (parts_tag.length==3){
                    if (StringUtils.isNumber(parts_tag[2]))
                        bean.setElement_index(Integer.parseInt(parts_tag[2]));
                    else bean.setRuleContent(parts_tag[1]+"."+parts_tag[2]);
                }
            }
                break;
            case TEXT_OWN:{
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
            case REGEX_MATCH:{
                bean.setRuleContent("");
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

    private String checkJsUsage(String rule) throws Exception {
        if (rule.contains("<js>")){
            if (!rule.contains("</js>"))throw new RuleProcessorException("js脚本不完整");
            if (engine == null)throw new RuleProcessorException("js脚本引擎未设置");
            JsFlag = true;
            int startJS = rule.indexOf("<js>");
            int endJS = rule.indexOf("</js>");
            String layer_withoutJS = rule.substring(0, startJS) + rule.substring(endJS+5);
            jsCode = rule.substring(startJS+4,endJS);
            jsResult = "";
            if (startJS == 0){
                try {
                    jsResult = engine.runScript(jsCode);
                    jsType = JSType.SCRIPT_ONLY;
                }catch (Exception e){
                    e.printStackTrace();
                    jsType = JSType.PARSE_FAIL;
                    throw new RuleProcessorException("js脚本解析失败");
                }
            }
            else jsType = JSType.SCRIPT_FOLLOW;
            return layer_withoutJS;
        }
        else return rule;
    }

    public boolean checkJsonUsage(String rule){
        Pattern pattern = Pattern.compile("^\\$\\.(.)+");
        Matcher matcher = pattern.matcher(rule);
        return matcher.matches();
    }

    private String getElementExclusions(ElementBean bean, String layer) throws Exception {
        List<Integer> exclusions=new ArrayList<>();
        if (!layer.contains("!"))return layer;
        String[] parts = layer.split("!");
        if (parts.length!=2)throw new RuleProcessorException("layer 格式错误");
        for (String p:parts[1].split("[|:]")) {
            //todo add ":" detect
            exclusions.add(Integer.parseInt(p));
        }
        bean.setExclusions(exclusions);
        return parts[0];
    }

    public String getContentReplacement(ElementBean bean, String layer) throws Exception {
        String[] replacement=new String[2];
        replacement[0]="";
        replacement[1]="";
        if (!layer.contains("##"))return layer;
        String[] parts = layer.split("##");
        if (parts.length>4)throw new RuleProcessorException("layer 格式错误");
        replacement[0]=parts[1];//普通替换
        if (parts.length>=3){
            if (bean.getType()==RuleType.OMIT_TAG)bean.setType(RuleType.REGEX_MATCH);
            else if (!isTypeofElement(bean)){
                if (StringUtils.match_regexMatch(parts[2]))
                    bean.setNeed_advanced_match(true);//需利用正则匹配
                else bean.setNeed_advanced_replace(true);//需利用正则替换
            }
            replacement[1]=parts[2];//进阶正则 ## [regex] ## $n ## #
        }
        bean.setReplacement(replacement);
        return parts[0];
    }

    /**
     * 通过原始输入构建bean，格式化规则
     * @param raw_rule
     * @throws Exception
     */
    private void splitLayers(String raw_rule) throws Exception {
        String[] layers;
        if (raw_rule.contains("||")) {
            String[] split = raw_rule.split("\\|\\|");
            raw_rule = split[0];
        }
        if (raw_rule.startsWith("@"))
            raw_rule = raw_rule.substring(1);
        //set js management
        raw_rule = checkJsUsage(raw_rule);
        //check json usage
        JsonFlag = checkJsonUsage(raw_rule);
        if (!isCssRule())return;
        layers=raw_rule.split("@");
        beans=new ElementBean[layers.length];
        for (int i = 0; i < layers.length; i++) {
            String currentlayer=layers[i];
            ElementBean b=new ElementBean();
            //set type
            b.setType(getRuleType(currentlayer));
            //set replacement
            currentlayer = getContentReplacement(b,currentlayer);
            //set exclusions
            currentlayer = getElementExclusions(b,currentlayer);
            //set content
            getRuleContentByType(b,currentlayer);
            beans[i]=b;
        }
    }

    private boolean isCssRule(){
        return !((JsFlag && jsType==JSType.SCRIPT_ONLY) || (JsonFlag));
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
        if (beans.length<=0)throw new RuntimeException("beans 生成错误");
        SelectedElements elementsFromBean = getElementsFromBean(document);
        if (elementsFromBean.bean_left!=-1)throw new RuleProcessorException("应该使用getObjectFromElements");
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
        int last_bean_index=-1;
        //第一层
        if (isTypeofElement(beans[0]))elements = getSelectedElements(root,beans[0]);
        else last_bean_index=0;
        //后续的层
        for (int i = 1; i < beans.length; i++) {
            ElementBean bean = beans[i];
            if (isTypeofElement(bean)){//仍是element类型
                elements = getSelectedElements(elements,bean);
            }else {//已经不是可以select的element
                last_bean_index = i;
                break;
            }
        }
        return new SelectedElements(elements, last_bean_index);
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
        List<String> object=new ArrayList<>();
        splitLayers(raw_rule);
        if (JsFlag && jsType==JSType.SCRIPT_ONLY){
            if (!jsResult.equals(""))object.add(jsResult);
            return object;
        }
        if(JsonFlag){
            String json = new Document(eles.toString()).body().text();
            object = JsonPath.read(json, raw_rule);
            return object;
        }
        if (beans.length<=0)throw new RuleProcessorException("beans 生成错误");
        if(!isCssRule())throw new RuleProcessorException("未处理的非css规则");
        for (Element ele : eles) {
            SelectedElements selectedElements = getElementsFromBean(ele);
            selected_elements.add(selectedElements.elements);
            int last_bean_index=selectedElements.bean_left;
            if (last_bean_index!=-1 && !selectedElements.isEmpty()){
                ElementBean bean=beans[last_bean_index];
                String current_item = "null:null";
                switch(bean.getType()){
                    case TEXT: {
                        String item = selectedElements.elements.text();
                        current_item = item;
                        //object.add(getReplacedItem(bean, item));
                    }
                        break;
                    case ATTR: {
                        String item = selectedElements.elements.attr(bean.getAttr());
                        current_item = item;
                        //object.add(getReplacedItem(bean, item));
                    }
                        break;
                    case TEXT_NODES:{
                        Element content_element = selectedElements.elements.get(0);
                        List<TextNode> paras = content_element.textNodes();
                        StringBuilder content=new StringBuilder();
                        for (TextNode para:paras) {
                            if (isContent)content.append("  ");
                            content.append(para.text());
                            if (isContent)content.append("\n");
                            if (isContent)content.append("\n");
                        }
                        current_item = content.toString();
                        //object.add(getReplacedItem(bean,content.toString()));
                    }
                        break;
                    case HTML:{
                        StringBuilder content=new StringBuilder();
                        for (Element para : selectedElements.elements) {
                            content.append(para.html());
                            if (isContent)content.append("\n");
                        }
                        String txt = StringUtils.styleHTML_to_styleTXT(content.toString());
                        current_item = txt;
                        //object.add(getReplacedItem(bean,txt));
                    }
                        break;
                    case REGEX_MATCH:{
                        String result = getRegexMatchedResult(ele.html(), bean);
                        current_item = result;
                        //object.add(result);
                    }
                        break;
                    default:{
                        String item = selectedElements.elements.text();
                        object.add(getReplacedItem(bean, item));
                    }
                }
                if (current_item.equals("null:null"))throw new RuleProcessorException("unhandled element type");
                String replacedItem = getReplacedItem(bean, current_item);
                if (JsFlag && jsType == JSType.SCRIPT_FOLLOW){
                    engine.runScript(jsCode);
                }
                object.add(replacedItem);
            }
        }
        return object;
    }

    public ArrayList<Elements> getSelected_elements() {
        return selected_elements;
    }

    /**
     * 在原始字符串按正则表达式找到符合要求的字符串，并进行替换
     * @param origin 原始字符串
     * @param bean 包含正则规则的元素
     * @return 匹配并替换的字符串
     */
    private String getRegexMatchedResult(String origin, ElementBean bean) {
        Pattern pattern=Pattern.compile(bean.getReplacement()[0]);
        Matcher matcher=pattern.matcher(origin);
        String result="";
        if (matcher.find()){
            String match = matcher.group(0);
            result = match.replaceAll(bean.getReplacement()[0],
                    bean.getReplacement()[1]);
        }
        return result;
    }

    public String getReplacedItem(ElementBean bean, String item) throws Exception {
        String result=item;
        if (bean.hasReplacement() && (bean.getType()!=RuleType.REGEX_MATCH)){
            if ("".equals(bean.getReplacement()[1]))
                result = item.replaceAll(bean.getReplacement()[0],"");
            else if (bean.isNeed_advanced_match()) {
                result = getRegexMatchedResult(item,bean);
            } else if (bean.isNeed_advanced_replace()){
                result = item.replaceAll(bean.getReplacement()[0],bean.getReplacement()[1]);
            } else
                throw new Exception("layout 类型错误,应该为:REGEX_MATCH 或 源规则不符合正则替换逻辑");
        }
        return result;
    }
    //判断接下来解析的目标是否仍是elements
    private boolean isTypeofElement(ElementBean bean) {
        return bean.getType()!= RuleType.TEXT &&
                bean.getType()!= RuleType.ATTR &&
                bean.getType()!=RuleType.TEXT_NODES &&
                bean.getType()!=RuleType.HTML &&
                bean.getType()!=RuleType.REGEX_MATCH;
    }

    private Elements getSelectedElements(Elements elements, ElementBean bean) {
        String selection=bean.getBound()+bean.getRuleContent();
        elements = elements.select(selection);
        if (bean.hasExclusions() && elements.size()!=0)
            elements=getElementsAfterExclusion(bean.getExclusions(),elements);
        if (bean.hasIndex() && elements.size()!=0)
            elements=getElementsByIndex(bean.getElement_index(),elements);
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
    private Elements getElementsByIndex(int index,Elements elements){
        if (index>=0){
            return new Elements(elements.get(index));
        }else {
            int i=elements.size()+index;
            return new Elements(elements.get(i));
        }
    }

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
}
