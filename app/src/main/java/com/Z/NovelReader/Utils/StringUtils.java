package com.Z.NovelReader.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    //获取字符串匹配度
    public static double compareStrings(String str1, String str2) {
        ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
        ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i=0; i<pairs1.size(); i++) {
            Object pair1=pairs1.get(i);
            for(int j=0; j<pairs2.size(); j++) {
                Object pair2=pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        return (2.0*intersection)/union*100;
    }
    private static String[] letterPairs(String str) {
        int numPairs = str.length()-1;
        String[] pairs = new String[numPairs];
        for (int i=0; i<numPairs; i++) {
            pairs[i] = str.substring(i,i+2);
        }
        return pairs;
    }

    private static ArrayList wordLetterPairs(String str) {
        ArrayList allPairs = new ArrayList();
        String[] words = str.split("\\s");
        for (int w=0; w < words.length; w++) {
            String[] pairsInWord = letterPairs(words[w]);
            for (int p=0; p < pairsInWord.length; p++) {
                allPairs.add(pairsInWord[p]);
            }
        }
        return allPairs;
    }

    /**
     * 判断URL是否相等
     * @param url1
     * @param url2
     * @return true/false
     */
    public static boolean UrlStingCompare(String url1,String url2){
        String newurl1,newurl2;
        newurl1=http2https(url1);
        newurl2=http2https(url2);
        return newurl1.equals(newurl2);
    }

    private static String http2https(String url) {
        String newurl=url;
        int i1=url.indexOf("http");
        if (i1!=-1){
            if ('s'!=url.charAt(i1+4)){
                newurl=url.replace("http","https");
            }
        }else {
            throw new IllegalArgumentException("invalid url");
        }
        return newurl;
    }

    /**
     * 统计子串在源串中出现的次数
     * @param s 源字符串
     * @param sub 子串
     * @return 重复次数
     */
    public static int filter(String s,String sub){
        int old_length=s.length();
        String replace="";
        if (s.contains(sub)){
            replace = s.replace(sub, "");//将需要查找的字符串替换为空
        }
        int new_length= replace.length();//用原来字符串的长度去减替换过的字符串就是该字符串中字符出现的次数
        int count=(old_length-new_length)/(sub.length());//因为是字符串中字符出现的次数,所以要除以字符串你的长度最后就是字符串在另一个字符串中出现的次数
        return count;
    }

    /**
     * 补全书籍链接
     * @param url 原始链接
     * @param bookSourceUrl 书源链接
     * @return 完整链接
     */
    public static String completeUrl(String url, String bookSourceUrl) {
        String temp = "";
        if (!url.contains("http")) {
            if (bookSourceUrl != null) {
                if (url.startsWith("/")) url = url.substring(1);
                if (!bookSourceUrl.endsWith("/"))bookSourceUrl=bookSourceUrl+"/";
                temp = (bookSourceUrl + url);
            }
        } else temp = url;
        return temp;
    }

    // 通过 -?[0-9]+(\\\\.[0-9]+)? 进行匹配是否为数字
    private static Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");

    /**
     * 通过正则表达式判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        // 通过Matcher进行字符串匹配
        Matcher m = pattern.matcher(str);
        // 如果正则匹配通过 m.matches() 方法返回 true ，反之 false
        return m.matches();
    }
}
