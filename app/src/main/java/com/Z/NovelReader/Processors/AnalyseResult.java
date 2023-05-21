package com.Z.NovelReader.Processors;

import static com.Z.NovelReader.Utils.CollectionUtils.castList;

import com.Z.NovelReader.Processors.Analyzers.MainAnalyzer;
import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.google.gson.Gson;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AnalyseResult {
    public enum ResultType{STRING,ELEMENT,JSON_STRING,STRING_LIST,ELEMENT_LIST,JSON_STRING_LIST}
    public Object result;
    public ResultType type;

    public AnalyseResult(Object result, MainAnalyzer.RuleType ruleType){
        this.result = result;
        try {
            this.type = judgeType(result,ruleType);
        } catch (RuleProcessorException e) {
            e.printStackTrace();
        }
    }

    public AnalyseResult(Object result, ResultType type) {
        this.result = result;
        this.type = type;
    }

    public static ResultType judgeType(Object result, MainAnalyzer.RuleType ruleType) throws RuleProcessorException {
        switch(ruleType){
            case Jsoup:
                if (result instanceof String)return ResultType.STRING;
                if (result instanceof Element)return ResultType.ELEMENT;
                if (result instanceof Elements)return ResultType.ELEMENT_LIST;
                if (result instanceof List)return ResultType.STRING_LIST;
                break;
            case Json:
                if (result instanceof String){
                    if (isJson((String) result))return ResultType.JSON_STRING;
                    else return ResultType.STRING;
                }
                if (result instanceof List){
                    List<String> string_list = castList(result, String.class);
                    if (string_list.size()==0)return ResultType.STRING_LIST;//无意义，返回默认值
                    if (isJson(string_list.get(0)))return ResultType.JSON_STRING_LIST;
                    else return ResultType.STRING_LIST;
                }
                break;
        }
        throw new RuleProcessorException("未知的返回值类型");
    }

    public boolean isList(){
        return type.ordinal() > 2;
    }

    public static boolean isJson(String s) {
        Gson gson = new Gson();
        try {
            gson.fromJson(s, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    public List<Object> toObjectList(){
        if (isList())return castList(result,Object.class);
        return null;
    }

    public String asString(){
        if (!(result instanceof String))return null;
        return (String) result;
    }
    public List<String> asStringList(){
        if (result instanceof List)
            return castList(result,String.class);
        else if (result instanceof String){
            List<String> single_result = new ArrayList<>();
            single_result.add(result.toString());
            return single_result;
        }
        else return null;

    }
    public Elements asElements(){
        if (result instanceof Elements)return (Elements) result;
        if (result instanceof List){
            List<Element> elements = castList(result, Element.class);
            return new Elements(elements);
        }
        return null;
    }
    public List<Element> asElementList(){
        if (result instanceof Elements) return castList(result, Element.class);
        if (result instanceof List)return castList(result,Element.class);
        return null;
    }
}
