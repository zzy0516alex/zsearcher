package com.example.helloworld;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BTSearchThread extends Thread {
    private String basic_url="https://www.btdad.info/search-XXX-0-0-1.html";
    private String key_word;
    private Object ResultList;

    public BTSearchThread(String key_word) {
        this.key_word = key_word;
    }

    private int counter=0;
    private Object ERROR;
    @Override
    public void run() {
        super.run();
        String url=basic_url.replace("XXX",key_word);
        ArrayList<String> title_list=new ArrayList<>();
        ArrayList<String> file_size=new ArrayList<>();
        ArrayList<String> magnet=new ArrayList<>();
        Map<String,ArrayList<String>> result_list=new HashMap<>();
        try {
            Document document= Jsoup.connect(url).get();
            Element pager=document.select("div.pager").get(0);
            String pages=pager.select("span").first().text();
            int num_of_pages = getPage(pages);
            if(num_of_pages>6)num_of_pages=6;
            for (int i = 1; i <=num_of_pages; i++) {
                try {
                    ArrayList<String>current_titlelist;
                    ArrayList<String>current_filesize;
                    ArrayList<String>current_filesize_reformed=new ArrayList<>();
                    ArrayList<String>current_magnet;
                    String newurl=url.replace("1",String.valueOf(i));
                    Document doc= Jsoup.connect(newurl).get();
                    Element element=doc.select("div.tbox").get(2);
                    Elements ele=element.select("div.title");
                    Elements ele2=element.select("div.sbar").select("span");
                    current_titlelist= (ArrayList<String>) ele.eachText();
                    current_filesize= (ArrayList<String>) ele2.eachText();
                    current_magnet= (ArrayList<String>) ele2.select("a").eachAttr("href");
                    for (int j = 2; j < current_filesize.size() ; ) {
                        current_filesize_reformed.add(current_filesize.get(j));
                        j=j+5;
                    }
                    title_list.addAll(current_titlelist);
                    file_size.addAll(current_filesize_reformed);
                    magnet.addAll(current_magnet);
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d("search","skip"+i);
                    continue;
                }
                Log.d("loop",""+i);

            }
            result_list.put("TitleList",title_list);
            result_list.put("SizeList",file_size);
            result_list.put("MagnetList",magnet);
            Log.d("search","done");
            ResultList=result_list;
        } catch (IOException e) {
            e.printStackTrace();
            if(counter<5)run();
            else ERROR=true;
            counter++;
            Log.d("searchErr","retry"+counter);
        }
    }
    private int getPage(String pages) {
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(pages);
        String Sresult= m.replaceAll("").trim();
        return Integer.parseInt(Sresult);
    }
    public Object getErr(){
        try {
            this.join();
            return ERROR;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object getResultList() {
        try {
            this.join();
            return ResultList;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
