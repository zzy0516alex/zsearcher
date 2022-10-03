package com.Z.NovelReader.Utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    //获取字符串匹配度
    public static double compareStrings(String str1, String str2) {
        str1 = deleteSpaceInStart(str1);
        str2 = deleteSpaceInStart(str2);
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
        //处理https协议问题
        newurl1=http2https(url1);
        newurl2=http2https(url2);
        if (newurl1.equals(newurl2))return true;
        else {
            //处理移动设备问题
            newurl1 = newurl1.replace("https://m.","https://www.");
            newurl2 = newurl2.replace("https://m.","https://www.");
            return newurl1.equals(newurl2);
        }
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
                temp = add_to_url(bookSourceUrl,url);
            }
        } else temp = url;
        return temp;
    }

    public static String add_to_url(String root,String adder){
        StringBuilder result = new StringBuilder(root);
        boolean flag = false;//是否以 / 结尾
        if (adder.endsWith("/"))flag = true;
        String[] url_parts = adder.split("/");
        if (url_parts.length == 0)return root+adder;
        for (int i = 0; i < url_parts.length; i++) {
            if (!root.contains("/"+url_parts[i]+"/")){
                result.append(url_parts[i]);
                if (i!=url_parts.length-1 || flag)result.append("/");
            }
        }
        return result.toString();
    }

    /**
     * 通过正则表达式判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
        // 通过Matcher进行字符串匹配
        Matcher m = pattern.matcher(str);
        // 如果正则匹配通过 m.matches() 方法返回 true ，反之 false
        return m.matches();
    }

    public static String styleHTML_to_styleTXT(String html){
        Document document = Jsoup.parse(html);
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        document.outputSettings(outputSettings);
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        //document.select("p").append("\\n");
        String newHtml = document.html().replaceAll("\\\\n", "\n");
        String plainText = Jsoup.clean(newHtml, "", Whitelist.none(), outputSettings);
        return StringEscapeUtils.unescapeHtml4(plainText.trim());
    }

    /**
     * 是否符合匹配meta类的情况(首尾都是[])
     * @param rule
     * @return
     */
    public static boolean match_meta(String rule){
        Pattern pattern = Pattern.compile("^\\[.+\\]$");
        Matcher matcher = pattern.matcher(rule);
        return matcher.matches();
    }

    /**
     * 判断书源正则规则是替换还是匹配，即是否存在 $1 在正则表达式中
     * @param s
     * @return
     */
    public static boolean match_regexMatch(String s){
        Pattern pattern = Pattern.compile("(.*)(\\$\\d)(.*)");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    /**
     * 根据书源规则判断列表是否需要翻转，即规则开头为 -
     * @param rule
     * @return 一个matcher 对象
     */
    public static Matcher match_needReverseFlag(String rule){
        Pattern pattern = Pattern.compile("^-(.+)");
        Matcher matcher = pattern.matcher(rule);
        return matcher;
    }

    public static String deleteSpaceInStart(String origin){
        return origin.replaceAll("^ ","");
    }

    /**
     * 根据两个url链接，获取他们公共的父链接
     * @param str1 url
     * @param str2 url
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getSharedURL(String str1, String str2)
    {
        String shared_string;
        if(str1.length()>str2.length())
            shared_string = getSharedString(str2,str1);
        else
            shared_string = getSharedString(str1,str2);
        return getRootUrl(shared_string);
    }

    /**
     * 获取一个链接的父链接
     * @param url 任意链接
     * @return root url
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getRootUrl(String url) {
        String shared_url = "";
        if (url.endsWith("/")) return url;
        String[] url_parts = url.split("/");
        String[] url_parts_new = new String[url_parts.length-1];
        if (url_parts.length - 1 >= 0)
            System.arraycopy(url_parts, 0, url_parts_new, 0, url_parts.length - 1);
        shared_url = String.join("/",url_parts_new);
        return shared_url;
    }

    /**
     * 获取两个字符串的最大公共子串，要求str2.len > str1.len
     * @param str1
     * @param str2
     * @return
     */
    public static String getSharedString(String str1,String str2)
    {
        String[] arr=new String[100000];
        boolean flag=false;
        String max="";
        int k=0;
        for (int j=0;j<str1.length();j++)
        {
            for(int i=j+1;i<=str1.length();i++)
                arr[k++]=str1.substring(j,i);
        }

        for(int i=0;i<k;i++)
        {
            if(str2.contains(arr[i]))
            {
                flag=true;
                if(max.length()<arr[i].length())
                {
                    max=arr[i];
                }
            }
        }
        return flag?max:"";
    }

    /**
     * 是否是以0~2个空格开头的字符串
     * @param s
     * @return
     */
    public static boolean isStartWithNoSpace(String s){
        Pattern pattern = Pattern.compile("^[ ]{0,2}[^ ]+(.|\\n|\\r)*");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public static String simplifyChapName(String origin){
        return origin.replaceAll("第.+?章","");
    }

//    public static String handleEscapeCharacterInUrl(String url){
//       return url.replace("#","%23");
//    }
}
