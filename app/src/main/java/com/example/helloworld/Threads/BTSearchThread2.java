package com.example.helloworld.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class BTSearchThread2 extends Thread {
    //http://www.comicat.org/search.php?keyword=

    private String Keyword;
    private Handler handler;
    private boolean outFlow=false;
    private boolean Error=false;
    private HashMap<String,ArrayList<String>> ResultList;

    private int SEARCH_DONE=0X3;
    private int SEARCH_ERROR_LINK_FAIL=0X10;
    private int SEARCH_ERROR_NOT_FOUND=0X11;

    private String BasicUrl="http://www.comicat.org/search.php?keyword=";

    public BTSearchThread2(String keyWord) {
        try {
            this.Keyword= URLEncoder.encode(keyWord,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    int counter=0;
    @Override
    public void run() {
        super.run();
        String url=BasicUrl+Keyword;
        ArrayList<String> title_list=new ArrayList<>();
        ArrayList<String> file_size=new ArrayList<>();
        ArrayList<String> file_type=new ArrayList<>();
        ArrayList<String> magnet=new ArrayList<>();
        HashMap<String,ArrayList<String>> result_list=new HashMap<>();
        try {
            Document document= Jsoup.connect(url).get();
            List<String> pages=new ArrayList<>();
            try {
                 Element element = document.select("div.pages.clear").get(0);//遇到class里有空格时，用"."代替
                 pages = element.select("a").eachText();
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
                Log.e("search","T2-NotFound");
                Message e_message=handler.obtainMessage();
                e_message.what=SEARCH_ERROR_NOT_FOUND;
                handler.sendMessage(e_message);
                return;
            }
            int max_page=0;
            for(String s:pages){
                if(isNumeric(s)){
                    if(Integer.parseInt(s)>max_page){
                        max_page=Integer.parseInt(s);
                    }
                }
            }
            Log.d("maxpage",""+max_page);
            if(max_page>6){
                max_page=6;
                outFlow=true;
            }
            String deep_url=url+"&page=1";
            for (int p = 1; p <= max_page; p++) {
                String current_url=deep_url.replace("=1","="+p);
                try {
                    ArrayList<String>current_type=new ArrayList<>();
                    ArrayList<String>current_size=new ArrayList<>();
                    ArrayList<String>current_title=new ArrayList<>();
                    ArrayList<String>current_magnet;
                    ArrayList<String>current_magnet_reformed=new ArrayList<>();
                    Document doc = Jsoup.connect(current_url).get();
                    Elements ele_main=doc.select("tbody#data_list");
                    Elements ele_info=ele_main.select("td").not("[nowrap]");
                    List<String> info=ele_info.eachText();
                    int I_info=0;
                    for (String s:info) {
                        int index=I_info%4;
                        switch(index){
                            case 0:
                                current_type.add(s);
                                break;
                            case 1:
                                current_title.add(s);
                                break;
                            case 2:
                                current_size.add("大小："+s);
                                break;
                            default:
                        }
                        I_info++;
                    }
                    Elements ele_href=ele_info.select("[style]");
                    current_magnet= (ArrayList<String>) ele_href.select("a").eachAttr("href");
                    for (String m : current_magnet) {
                        String []reformer=m.split("\\.|-");
                        current_magnet_reformed.add("magnet:?xt=urn:btih:"+reformer[1]);
                    }
                    title_list.addAll(current_title);
                    file_size.addAll(current_size);
                    magnet.addAll(current_magnet_reformed);
                    file_type.addAll(current_type);
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d("search","skip"+p);
                    continue;
                }
                Log.d("T2-loop",""+p);
            }
            result_list.put("TitleList",title_list);
            result_list.put("SizeList",file_size);
            result_list.put("MagnetList",magnet);
            result_list.put("TypeList",file_type);
            Log.d("T2-search","done");
            ResultList=result_list;
        } catch (IOException e) {
            e.printStackTrace();
            counter++;
            Log.d("T2-searchErr","retry"+counter);
            if(counter<5)run();
            else Error=true;
        }
        Message message=handler.obtainMessage();
        if(!Error)message.what=SEARCH_DONE;
        else message.what=SEARCH_ERROR_LINK_FAIL;
        handler.sendMessage(message);
    }
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public HashMap<String, ArrayList<String>> getResultList() {
        return ResultList;
    }

    public boolean isError() {
        return Error;
    }
}
