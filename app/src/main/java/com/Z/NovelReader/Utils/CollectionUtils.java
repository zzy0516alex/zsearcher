package com.Z.NovelReader.Utils;

import com.Z.NovelReader.Objects.beans.NovelRequire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {
    /**
     * 差集(基于java8新特性)优化解法 适用于大数据量
     * 求List1中有的但是List2中没有的元素
     */
    public static <T> List<T> diffListByStringKey(List<T> list1, List<T> list2) {
        if (list2.size() < list1.size()) {
            Map<String, T> tempMap = list2.parallelStream().collect(Collectors.toMap(T::toString, Function.identity(), (oldData, newData) -> newData));
            return list1.parallelStream().filter(obj -> {
                return !tempMap.containsKey(obj.toString());
            }).collect(Collectors.toList());
        }
        else {
            Map<String, T> tempMap = list1.parallelStream().collect(Collectors.toMap(T::toString, Function.identity(), (oldData, newData) -> newData));
            return list2.parallelStream().filter(obj -> !tempMap.containsKey(obj.toString())).collect(Collectors.toList());
        }
    }

    public static <T> List<T> diffListByHashKey(List<T> list1, List<T> list2) {
        if (list2.size() >= list1.size()) {
            Map<Integer, T> tempMap = list2.parallelStream().collect(Collectors.toMap(T::hashCode, Function.identity(), (oldData, newData) -> newData));
            return list1.parallelStream().filter(obj -> !tempMap.containsKey(obj.hashCode())).collect(Collectors.toList());
        }
        else {
            Map<Integer, T> tempMap = list1.parallelStream().collect(Collectors.toMap(T::hashCode, Function.identity(), (oldData, newData) -> newData));
            return list2.parallelStream().filter(obj -> !tempMap.containsKey(obj.hashCode())).collect(Collectors.toList());
        }
    }

    /**
     * 并集(去重) 基于Java8新特性 适用于大数据量
     * 合并list1和list2 去除重复元素
     */
    public static <T> List<T> distinctMergeList(ArrayList<T> list1, ArrayList<T> list2){
        //第一步 先求出list1与list2的差集
        List<T>diff = diffListByStringKey(list1,list2);
        //第二部 再合并list1和list2
        diff.addAll(list2);
        return diff;
    }

    /**
     * 将object对象转为list，注意object需继承arraylist
     * @param obj 列表对象的父类
     * @param clazz 列表的泛型
     * @param <T>
     * @return List对象
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz){
        List<T> result = new ArrayList<T>();
        if(obj instanceof ArrayList<?>)
        {
            for (Object o : (ArrayList<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
