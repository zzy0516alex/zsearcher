package com.Z.NovelReader.Utils;

import android.util.Log;

import com.Z.NovelReader.myObjects.beans.NovelCatalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FileIOUtils {


    /**
     * 从目录文件读取目录链接
     * @param catalog_sub_path 书名
     * @param Dir 根目录
     * @return NovelCatalog
     */
    public static NovelCatalog read_catalog(String catalog_sub_path, File Dir /*getExternalFilesDir*/) {
        File txtDir=new File(Dir+catalog_sub_path);
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

    /**
     * 从文件读取章节内容
     * @param bookname 书名
     * @param Dir 根目录
     * @return 文本
     */
    public static String read_line(String bookname,File Dir){
        File txtDir=new File(Dir+"/ZsearchRes/BookReserve/" + bookname + "/content.txt");
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

    /**
     * 写入章节内容
     * @param Dir 根目录
     * @param sub_path 子目录需带有书名"/ZsearchRes/BookReserve/bookname"
     * @param content 内容
     */
    public static void WriteTXT(File Dir,String sub_path,String content){
        File mk_txt=new File(Dir+sub_path+"/content.txt");
        writeToFile(content, mk_txt,false);
    }

    /**
     * 写入目录
     * @param Dir 根目录
     * @param BookName 书名
     * @param content 内容
     */
    public static void WriteCatalog(File Dir,String BookName,String content){
        File mk_txt=new File(Dir+"/ZsearchRes/BookContents/"+ BookName+"_catalog.txt" );
        writeToFile(content, mk_txt,false);
    }

    /**
     * 输出到TXT
     * @param content 内容
     * @param mk_txt 文件名
     * @param append 是否追加
     */
    private static void writeToFile(String content, File mk_txt,boolean append) {
        FileOutputStream fot = null;
        try {
            fot = new FileOutputStream(mk_txt,append);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert fot != null;
            fot.write(content.getBytes());
            fot.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fot != null) {
                try {
                    fot.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 以novelCatalog格式写入目录
     * @param Dir 根目录
     * @param sub_path 书名
     * @param novelCatalog 类
     */
    public static void WriteCatalog(File Dir,String sub_path,NovelCatalog novelCatalog){
        StringBuilder content=new StringBuilder();
        for (int i = 0; i < novelCatalog.getLink().size(); i++) {
            content.append(novelCatalog.getTitle().get(i));
            content.append('\n');
            content.append(novelCatalog.getLink().get(i));
            if(i!=novelCatalog.getSize()-1)content.append('\n');
        }
        String content_string=content.toString();
        File mk_txt=new File(Dir + sub_path);
        writeToFile(content_string, mk_txt,false);

    }

    /**
     * 错误报告
     * @param Dir 根目录
     * @param e 错误
     * @param from_where 出处
     */
    public static void WriteErrReport(File Dir,Throwable e,String ...from_where){
        File Folder =new File(Dir,"Errors");
        if(!Folder.exists()){
            Folder.mkdir();
        }
        File mk_txt=new File(Dir+"/Errors/"+TimeUtil.getCurrentDateInString()+"_crashLog.txt" );
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\n');
        stringBuilder.append(TimeUtil.getCurrentTimeInString());
        stringBuilder.append('\n');
        stringBuilder.append("CAUSE=");
        stringBuilder.append(Arrays.toString(from_where));
        stringBuilder.append('\n');
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        stringBuilder.append(writer.toString());
        stringBuilder.append("-----------------------------------\n");
        writeToFile(stringBuilder.toString(), mk_txt,true);
    }
}
