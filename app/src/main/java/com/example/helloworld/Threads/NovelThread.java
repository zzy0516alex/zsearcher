package com.example.helloworld.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.helloworld.myObjects.BookList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NovelThread extends Thread {
    private String url;
    private String code;
    private ArrayList<BookList>searchResult;
    private boolean isError=false;
    private boolean Found=false;
    private Handler handler;
    private TAG tag;
    private String search_link;
    public static final int BOOK_SEARCH_NOT_FOUND=0X1;
    public static final int BOOK_SEARCH_DONE=0X2;
    public static final int BOOK_SEARCH_NO_INTERNET=0X3;
    public enum TAG {BiQuGe,SiDaMingZhu}


    public NovelThread(String url, String code, TAG tag ) {
        this.url=url;
        this.code=code;
        this.tag=tag;
        switch(tag){
            case BiQuGe:
                search_link="/modules/article/search.php?searchkey=";
                break;
            case SiDaMingZhu:
                search_link="/search?searchkey=";
                break;
            default:
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    @Override
    public void run() {
        super.run();
        searchResult=new ArrayList<>();
        try {
            Connection connect = Jsoup.connect(url+search_link+code);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document doc= connect.get();
            switch(tag){
                case BiQuGe: {
                    Found=processor1(doc);
                }
                    break;
                case SiDaMingZhu:
                    Found=processor2(doc);
                    break;
                default:
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("err","no internet");
            isError=true;
        }
        Message message=handler.obtainMessage();
        if (isError)message.what=BOOK_SEARCH_NO_INTERNET;
        else if (!Found)message.what=BOOK_SEARCH_NOT_FOUND;
        else message.what=BOOK_SEARCH_DONE;
        message.obj= searchResult;
        handler.sendMessage(message);
    }

    private boolean processor1(Document doc) {
        Elements table =doc.select("tr").not("[align]");
        if(table.size()!=0) {
            for (Element trs:table) {
                int i_td=0;
                BookList current_book=new BookList();
                current_book.setTag(TAG.BiQuGe);
                Elements tds = trs.select("td");
                for (Element td : tds) {
                    switch(i_td){
                        case 0:{
                            current_book.setBookName(td.text());
                            current_book.setBookLink(url+td.select("a").attr("href"));
                            break;
                        }
                        case 2:
                            current_book.setWriter(td.text());
                            break;
                        default:
                    }
                    i_td++;
                }
                searchResult.add(current_book);
            }
            return true;
        }
        else {
            return false;
        }
    }

    private boolean processor2(Document doc){
        Elements book_list = doc.select("div.content_list");
        Elements book_list_used=book_list.select("h3");
        Elements writer_list=book_list.select("span.writer");
        List<String>writer=writer_list.eachText();
        List<String> book_name=book_list_used.eachText();
        List<String>book_link=new ArrayList<>();
        for (Element li : book_list_used) {
            book_link.add(url +li.select("a").attr("href"));
        }
        if (book_name.size()!=0) {
            for (int i = 0; i < book_name.size(); i++) {
                BookList current_book = new BookList();
                current_book.setBookName(book_name.get(i));
                current_book.setBookLink(book_link.get(i));
                current_book.setWriter(writer.get(i));
                current_book.setTag(TAG.SiDaMingZhu);
                searchResult.add(current_book);
            }
            return true;
        }else {
            return false;
        }
    }


    public boolean getError(){
            return isError;
    }
    public boolean isFound(){
            return Found;
    }
    public ArrayList<BookList> getNovelResults(){
            return searchResult;
    }
}
