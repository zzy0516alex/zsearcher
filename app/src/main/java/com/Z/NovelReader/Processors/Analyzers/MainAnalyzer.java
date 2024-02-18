package com.Z.NovelReader.Processors.Analyzers;

import static com.Z.NovelReader.Utils.CollectionUtils.castList;

import com.Z.NovelReader.Processors.InnerEntities.AnalyseResult;
import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.Z.NovelReader.Processors.JavaScriptEngine;
import com.google.gson.JsonArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainAnalyzer {
    //Jsoup表示通过jsoup解析的网页标签规范(默认)，JsoupCss表示以@css开头的网页风格规范，Json表示以$.开头的JsonPath规范，类型间不独立
    public enum RuleType {Jsoup,JsoupCss,Json,JavaScript,XPath,Regex }
    //only表示有且仅有js代码，follow表示需要对上一步的结果(result变量进行js脚本处理)，fail表示js脚本格式不识别或执行出错
    public enum JSType {SCRIPT_ONLY, SCRIPT_FOLLOW, PARSE_FAIL}
    //NORMAL表示仅利用##后面的内容替换前面的，REGEX MATCH表示使用正则匹配(需要有$匹配符)，REGEX REPLACE表示使用正则替换
    public enum RegexType {NORMAL_REPLACE,REGEX_MATCH,REGEX_REPLACE}
    //FIRST表示仅取多个规则的第一个，MERGE表示将多个规则的结果合并，ROTATE表示轮流选取各规则的结果
    public enum JointType {FIRST,MERGE,ROTATE}

    private RuleType type;

    private boolean needReverseList = false;

    private boolean hasJS;//包含JavaScript脚本 由 <js> 或 @js 作为标识符
    private JSType jsType;
    private String jsCode;
    private JavaScriptEngine engine;

    private boolean hasRegex;//包含正则表达式
    private RegexType regexType;
    private String[] regex_pattern = new String[2];

    private boolean hasJointRule;//包含多个规则的连接符
    private JointType jointType;

    private String[] subRules;//统一将规则装入数组中，若规则不带分隔符，则数组大小为1
    private AnalyseResult analyse_result;

    public void setEngine(JavaScriptEngine engine) {
        this.engine = engine;
    }

    public AnalyseResult analyze(Document doc, String rule) throws Exception {
        AnalyseResult context = new AnalyseResult(doc, AnalyseResult.ResultType.DOCUMENT);
        return this.analyze(context, rule);
    }

    public AnalyseResult analyze(AnalyseResult context, String rule) throws Exception {
        //pre-process
        String treated_rule = parseRule(rule);
        //处理仅js脚本的情况
        if (hasJS && jsType == JSType.SCRIPT_ONLY){
            if (engine == null)throw new RuleProcessorException("js脚本引擎未设置");
            try {
                String js_result = engine.runScript(jsCode);
                return new AnalyseResult(js_result, AnalyseResult.ResultType.STRING);
            }catch (Exception e){
                throw new RuleProcessorException("js脚本解析失败: "+e.getMessage());
            }
        }
        if(context.isNull())return new AnalyseResult(null,type);//结果为空
        //main process
        List<AnalyseResult> result_list = new ArrayList<>();
        for (String sub_rule : subRules) {
            Object result;
            switch(type){
                case Jsoup:
                    if (context.type== AnalyseResult.ResultType.DOCUMENT ||
                            context.type == AnalyseResult.ResultType.ELEMENT_LIST) {
                        Elements elements = context.asElements();
                        JsoupAnalyzer jsoupAnalyzer = new JsoupAnalyzer();
                        result = jsoupAnalyzer.getObjectFromElements(elements, sub_rule);
                    }
                    else throw new RuleProcessorException("输入类型与规则类型不匹配");
                    break;
                case Json:
                    JsonAnalyzer jsonAnalyzer = new JsonAnalyzer();
                    if (context.type == AnalyseResult.ResultType.JSON_STRING_LIST) {
                        List<String> json_list = context.asStringList();
                        result = jsonAnalyzer.getObjectFromJsonPath(json_list, sub_rule);
                    }
                    else if(context.type == AnalyseResult.ResultType.DOCUMENT ||
                            context.type == AnalyseResult.ResultType.JSON_STRING){
                        String json_string = context.asString();
                        result = jsonAnalyzer.getObjectFromJsonPath(json_string,sub_rule);
                    }
                    else throw new RuleProcessorException("输入类型与规则类型不匹配");
                    break;
                case Regex:
                    result = context.asString();//这里先不处理，在后面post process中处理
                    break;
                default:
                    throw new RuleProcessorException("未处理的规则类型");
            }
            //pack results
            analyse_result = new AnalyseResult(result,type);

            //post process（正则+js 处理）
            if (analyse_result.type == AnalyseResult.ResultType.STRING){
                analyse_result.result = handleTextOnlyResult(analyse_result.asString());
                return analyse_result;
            }
            else if (analyse_result.type == AnalyseResult.ResultType.STRING_LIST){
                analyse_result.result =
                        analyse_result.asStringList().stream().map(this::handleTextOnlyResult).collect(Collectors.toList());
            }
            //collect processed results
            result_list.add(analyse_result);
        }

        //处理列表反转的情况
        if (analyse_result.isList() && needReverseList){
            List<Object> reverseList = analyse_result.toObjectList();
            Collections.reverse(reverseList);
            analyse_result.result = reverseList;
        }
        //处理多结果合并的情况
        if (hasJointRule){
            analyse_result.result = handleJoinedListResult(result_list);
        }
        return analyse_result;
    }

//    public AnalyseResult analyze(Elements elements, String rule) throws Exception {
//        //pre-process
//        String treated_rule = parseRule(rule);
//        if (hasJS && jsType == JSType.SCRIPT_ONLY){
//            if (engine == null)throw new RuleProcessorException("js脚本引擎未设置");
//            try {
//                String js_result = engine.runScript(jsCode);
//                return new AnalyseResult(js_result, AnalyseResult.ResultType.STRING);
//            }catch (Exception e){
//                throw new RuleProcessorException("js脚本解析失败: "+e.getMessage());
//            }
//        }
//        List<AnalyseResult> result_list = new ArrayList<>();
//        for (String sub_rule : subRules) {
//            //main process
//            Object result = new Object();
//            switch(type){
//                case Jsoup:
//                    JsoupAnalyzer jsoupAnalyzer = new JsoupAnalyzer();
//                    result = jsoupAnalyzer.getObjectFromElements(elements, sub_rule);
//                    break;
//                case Json:
//                    Document document = Jsoup.parse(elements.html());
//                    JsonAnalyzer jsonAnalyzer = new JsonAnalyzer();
//                    result = jsonAnalyzer.getObjectFromJsonPath(document,sub_rule);
//                    break;
//                default:
//            }
//            //pack results
//            analyse_result = new AnalyseResult(result,type);
//
//            //post process
//            if (analyse_result.type == AnalyseResult.ResultType.STRING){
//                analyse_result.result = handleTextOnlyResult(analyse_result.asString());
//                return analyse_result;
//            }
//            else if (analyse_result.type == AnalyseResult.ResultType.STRING_LIST){
//                analyse_result.result =
//                analyse_result.asStringList().stream().map(this::handleTextOnlyResult).collect(Collectors.toList());
//            }
//
//            //collect processed results
//            result_list.add(analyse_result);
//        }
//        if (analyse_result.isList() && needReverseList){
//            List<Object> reverseList = analyse_result.toObjectList();
//            Collections.reverse(reverseList);
//            analyse_result.result = reverseList;
//        }
//        if (!hasJointRule)return analyse_result;
//        else{
//            //joint
//            analyse_result.result = handleJoinedListResult(result_list);
//            return analyse_result;
//        }
//    }

    private List<Object> handleJoinedListResult(List<AnalyseResult> result_list) {
        List<Object> result = new ArrayList<>();
        switch (jointType) {
            case FIRST:
                result = result_list.get(0).toObjectList();
                break;
            case MERGE:
                List<Object> merge_list = new ArrayList<>();
                for (AnalyseResult analyseResult : result_list) {
                    List<Object> list = analyseResult.toObjectList();
                    if (list!=null)merge_list.addAll(list);
                }
                result = merge_list;
                break;
            case ROTATE:
                List<Object> rotate_list = new ArrayList<>();
                int i = 0;
                boolean out_range = false;
                while (!out_range) {
                    for (AnalyseResult analyseResult : result_list) {
                        List<Object> list = analyseResult.toObjectList();
                        if (list==null)continue;
                        if (i >= list.size() - 1) {
                            out_range = true;
                            break;
                        }
                        rotate_list.add(list.get(i));
                    }
                    i++;
                }
                result = rotate_list;
                break;
            default:
        }
        return result;
    }

    private String parseRule(String raw_rule) throws Exception {
        String treatedRule;
        treatedRule = raw_rule.replace("\n","");//trim
        treatedRule = checkListReverse(treatedRule);
        treatedRule = checkJsUsage(treatedRule);
        if (hasJS && jsType == JSType.SCRIPT_ONLY)return treatedRule;
        //以下进入正式规则部分
        checkJointRules(treatedRule);
        checkJsonUsage(treatedRule);
        if (type == RuleType.Json)return treatedRule;
        treatedRule = checkRegexUsage(treatedRule);
        if(treatedRule.isEmpty()){type=RuleType.Regex;return treatedRule;}
        type = RuleType.Jsoup;//都不是，就是默认规则
        return treatedRule;
    }

    private String checkListReverse(String rule){
        Pattern pattern = Pattern.compile("^-(.+)");
        Matcher matcher = pattern.matcher(rule);
        if (matcher.matches()) {
            needReverseList = true;
            return matcher.group(1);
        }
        else return rule;
    }

    private String checkJsUsage(String rule) throws Exception {
        if (rule.contains("<js>")){
            if (!rule.contains("</js>"))throw new RuleProcessorException("js脚本不完整");
            hasJS = true;
            int startJS = rule.indexOf("<js>");
            int endJS = rule.indexOf("</js>");
            String rule_withoutJS = rule.substring(0, startJS) + rule.substring(endJS+5);
            jsCode = rule.substring(startJS+4,endJS);
            if (startJS == 0)
                jsType = JSType.SCRIPT_ONLY;
            else
                jsType = JSType.SCRIPT_FOLLOW;
            return rule_withoutJS;
        }
        else return rule;
    }

    public void checkJsonUsage(String rule){
        Pattern pattern = Pattern.compile("^\\$\\.(.)+");
        Matcher matcher = pattern.matcher(rule);
        if (matcher.matches() || rule.contains("@json:"))
            type = RuleType.Json;
    }

    /**
     * 规则格式：其他规则##正则表达式##替换内容###
     * @param rule 规则
     * @return 剔除正则部分的其他规则
     * @throws Exception 格式错误
     */
    public String checkRegexUsage(String rule) throws Exception {
        regex_pattern[0]="";
        regex_pattern[1]="";
        Pattern pattern_normal = Pattern.compile("^([^#]*)##([^#]*)$");
        Pattern pattern_regex = Pattern.compile("^([^#]*)##([^#]*)##([^#]*)###$");
        Pattern pattern_match = Pattern.compile("(.*)(\\$\\d)(.*)");
        Matcher matcher_normal = pattern_normal.matcher(rule);
        Matcher matcher_regex = pattern_regex.matcher(rule);
        if (matcher_normal.find()){
            hasRegex = true;
            if (matcher_normal.groupCount() < 2)throw new RuleProcessorException("普通替换的格式不正确");
            regex_pattern[0] = matcher_normal.group(2);
            regexType = RegexType.NORMAL_REPLACE;
            return matcher_normal.group(1);
        }
        else if (matcher_regex.find()){
            hasRegex = true;
            int group_size = matcher_regex.groupCount();
            String treatedRule = "";
            if (group_size < 2)
                throw new RuleProcessorException("正则替换/匹配的格式不正确");
            regex_pattern[0] = matcher_regex.group(group_size-1);
            regex_pattern[1] = matcher_regex.group(group_size);
            if (group_size==3)treatedRule = matcher_regex.group(group_size-2);
            if (pattern_match.matcher(regex_pattern[1]).matches())
                regexType = RegexType.REGEX_MATCH;
            else regexType = RegexType.REGEX_REPLACE;
            return treatedRule;
        }
        else return rule;//不包含正则
    }

    public void checkJointRules(String rule){
        String[] joint = new String[]{"\\|\\|","&&","%%"};
        for (int i = 0; i < joint.length; i++) {
            if (Pattern.compile(joint[i]).matcher(rule).find()) {
                hasJointRule = true;
                subRules = rule.split(joint[i]);
                jointType = JointType.values()[i];
                break;
            }
        }
        if (!hasJointRule){
            subRules = new String[1];
            subRules[0] = rule;
            jointType = JointType.FIRST;
        }
    }

    private String handleTextOnlyResult(String result) {
        String text = (String) result;
        if (hasRegex) {
            if (regexType == RegexType.REGEX_MATCH){
                Pattern pattern = Pattern.compile(regex_pattern[0]);
                Matcher matcher = pattern.matcher(result);
                if (matcher.find() && matcher.groupCount()>0)
                    text = text.replaceAll(regex_pattern[0], regex_pattern[1]);
                else text = null;
            }
            else text = text.replaceAll(regex_pattern[0], regex_pattern[1]);
        }
        if (hasJS){
            engine.preDefine("result",text);
            try {
                text = engine.runScript(jsCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    private boolean isObjectTextConvertible(Object obj){
        if (obj instanceof String)return true;
        if (obj instanceof List){
            List<Object> list = castList(obj,Object.class);
            return list.size()>0 && (list.get(0) instanceof String);
        }
        return false;
    }

    private boolean isObjectInCollection(Object obj){
        return (obj instanceof List)|| (obj instanceof JsonArray);
    }
}
