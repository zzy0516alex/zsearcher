package com.example.helloworld.Utils;

import android.util.Log;

import com.example.helloworld.myObjects.NovelCatalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class IOtxt {


    public static NovelCatalog read_catalog(String bookname, File Dir /*getExternalFilesDir*/) {
        File txtDir=new File(Dir+"/ZsearchRes/BookContents/" + bookname + "_catalog.txt");
        FileInputStream fis=null;
        int counter=0;
        HashMap<String,ArrayList<String>> result=new HashMap<>();
        ArrayList<String> ChapName = new ArrayList<>();
        ArrayList<String> ChapLink = new ArrayList<>();
        try {
            fis = new FileInputStream(txtDir);
            InputStreamReader i_reader=new InputStreamReader(fis);
            BufferedReader b_reader=new BufferedReader(i_reader);
            String temp="";

            while ((temp=b_reader.readLine())!=null) {
                int s=counter % 2;
                switch(s){
                    case 0:
                    {
                        ChapName.add(temp);
                    }
                    break;
                    case 1:
                    {
                        ChapLink.add(temp);
                    }
                    break;
                    default:
                }
                counter++;
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e("error","book_catalog_not_found");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new NovelCatalog(ChapName,ChapLink);
    }

    public static String read_line(String bookname,File Dir){
        File txtDir=new File(Dir+"/ZsearchRes/BookContents/" + bookname + ".txt");
        FileInputStream fis=null;
        StringBuilder sb = new StringBuilder("");
        try {
            fis = new FileInputStream(txtDir);
            InputStreamReader i_reader=new InputStreamReader(fis);
            BufferedReader b_reader=new BufferedReader(i_reader);
            String temp="";
            while ((temp=b_reader.readLine())!=null) {
                sb.append(temp);
                sb.append("\n");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e("error","book_content_not_found");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
    public static void WriteTXT(File Dir,String BookName,String content){
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+".txt" );
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(mk_txt);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        try{
            fot.write(content.getBytes());
            fot.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fot!=null){
                try {
                    fot.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }

    public static void WriteCatalog(File Dir,String BookName,String content){
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+"_catalog.txt" );
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(mk_txt);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        try{
            fot.write(content.getBytes());
            fot.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fot!=null){
                try {
                    fot.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
    public static void WriteCatalog(File Dir,String BookName,NovelCatalog novelCatalog){
        StringBuilder content=new StringBuilder();
        for (int i = 0; i < novelCatalog.getLink().size(); i++) {
            content.append(novelCatalog.getTitle().get(i));
            content.append('\n');
            content.append(novelCatalog.getLink().get(i));
            if(i!=novelCatalog.getSize()-1)content.append('\n');
        }
        String content_string=content.toString();
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+"_catalog.txt" );
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(mk_txt);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        try{
            fot.write(content_string.getBytes());
            fot.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fot!=null){
                try {
                    fot.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
