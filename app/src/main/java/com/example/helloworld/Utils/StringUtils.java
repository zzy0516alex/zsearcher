package com.example.helloworld.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
}
