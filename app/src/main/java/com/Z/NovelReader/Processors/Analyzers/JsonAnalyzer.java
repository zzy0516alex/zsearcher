package com.Z.NovelReader.Processors.Analyzers;

import com.Z.NovelReader.Processors.Exceptions.RuleProcessorException;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class JsonAnalyzer {
    static {
        //configure jsonpath providers
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new GsonJsonProvider();
            private final MappingProvider mappingProvider = new GsonMappingProvider();
            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }
        });
    }

//    public Object getObjectFromJsonPath(Document jsoup_document, String json_path) throws RuleProcessorException {
//        String json = jsoup_document.body().text();
//        return getObjectFromJsonPath(json,json_path);
//    }

    public Object getObjectFromJsonPath(List<String> json_list, String json_path) throws RuleProcessorException {
        List<String> result = new ArrayList<>();
        for (String json_document : json_list) {
            Object object = getObjectFromJsonPath(json_document, json_path);
            if (object instanceof String)result.add((String) object);
            else throw new RuleProcessorException("json列表解析的结果仍为列表");
        }
        return result;
    }

    /**
     * 处理json格式的网页
     * @param json_document 网页的内容
     * @param json_path 筛选规则
     * @return 可能是单个字符串结果，也可能是仍具有json格式的字符串列表
     * @throws RuleProcessorException
     */
    public Object getObjectFromJsonPath(String json_document, String json_path) throws RuleProcessorException {
        Configuration configuration = Configuration.defaultConfiguration();
        Object result = JsonPath.using(configuration).parse(json_document).read(json_path);//JsonArray or JsonPrimitive
        if (result instanceof JsonPrimitive)
            return  ((JsonPrimitive) result).getAsString();//string
        else if (result instanceof JsonArray){
            JsonArray array = (JsonArray) result;
            List<String> json_array = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                json_array.add(array.get(i).toString());
            }
            return json_array;//json string list
        }
        else throw new RuleProcessorException("json 解析结果类型不正确");
    }
}
