package com.Z.NovelReader.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.Z.NovelReader.Objects.beans.NovelCatalog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FileIOUtils {


    public static NovelCatalog readCatalog(String catalog_path) throws FileNotFoundException {
        File csvFile = new File(catalog_path);
        // 如果文件不存在
        if (!csvFile.exists()) throw new FileNotFoundException();
        NovelCatalog catalog = new NovelCatalog();
        // 将流写在try里面，当try执行完之后，流会自动关闭
        try (InputStream inputStream = Files.newInputStream(csvFile.toPath());
             InputStreamReader fileReader = new InputStreamReader(inputStream);
             BufferedReader buffReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = buffReader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length<3)throw new RuntimeException("目录文件格式不正确");
                NovelCatalog.CatalogItem item = new NovelCatalog.CatalogItem();
                item.Title = parts[0];
                item.Link = parts[1];
                item.isDownloaded = Boolean.parseBoolean(parts[2]);//"true"
                catalog.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catalog;
    }
    /**
     * 从目录文件读取目录链接
     * @param catalog_path 目录文件地址
     * @return NovelCatalog
     */
    public static NovelCatalog read_catalog(String catalog_path) {
        File txtDir=new File(catalog_path);
        FileInputStream fis=null;
        int counter=0;
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
     * 从文件读取文本内容
     * @param content_path ="/ZsearchRes/BookReserve/$book name$/content.txt"
     * @return 文本
     */
    public static String readContent(String content_path){
        File txtDir=new File(content_path);
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
     * 读取文本文件内容，不定后缀(按字节数组读取)
     * @param file_path 路径
     * @return 读取的内容
     */
    public static String readTextFile(String file_path){
        File file=new File(file_path);
        if(!file.exists())return "";
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = inputStream.read(buff)) != -1) {
                result.write(buff, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 写入章节内容
     * @param path 章节文件地址 /$book reserve$/$book name$/content.txt
     * @param content 内容
     */
    public static void writeContent(String path, String content){
        File mk_txt=new File(path);
        writeTextFile(content, mk_txt,false);
    }

    /**
     * 普通文本文件输出(覆盖)
     * @param content 内容
     * @param file_path 路径
     */
    public static void writeTextFile(String content, String file_path){
        File file = new File(file_path);
        writeTextFile(content,file,false);
    }

    /**
     * 输出到文本文件
     * @param content 内容
     * @param file 文件名
     * @param append 是否追加
     */
    public static void writeTextFile(String content, File file, boolean append) {
        FileOutputStream fot = null;
        try {
            fot = new FileOutputStream(file,append);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            fot.write(content.getBytes());
            fot.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.e("write file","地址不存在");
        } finally {
            try {
                fot.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 以novelCatalog格式写入目录
     * @param catalog_path 输出目录的文件路径
     * @param novelCatalog 类
     */
    public synchronized static void WriteCatalog(String catalog_path,NovelCatalog novelCatalog){
        StringBuilder content=new StringBuilder();
        for (int i = 0; i < novelCatalog.getLinkList().size(); i++) {
            content.append(novelCatalog.getTitleList().get(i));
            content.append('\n');
            content.append(novelCatalog.getLinkList().get(i));
            if(i!=novelCatalog.getSize()-1)content.append('\n');
        }
        String content_string=content.toString();
        File mk_txt=new File(catalog_path);
        writeTextFile(content_string, mk_txt,false);

    }

    public synchronized static void writeCatalog(String catalog_path,NovelCatalog novelCatalog){
        StringBuilder all_catalog = new StringBuilder();
        for (int i = 0; i < novelCatalog.getSize(); i++) {
            NovelCatalog.CatalogItem catalogItem = novelCatalog.get(i);
            if(catalogItem.Title.contains(","))
                catalogItem.Title = catalogItem.Title.replace(",","，");//防止影响csv识别
            all_catalog.append(String.format(Locale.CHINA,"%s,%s,%b",catalogItem.Title,catalogItem.Link,catalogItem.isDownloaded))
            .append('\r').append('\n');
        }
        File mk_csv = new File(catalog_path);
        writeTextFile(all_catalog.toString(),mk_csv,false);
    }

    /**
     * 将一个列表写入文件
     * @param list_path list文件的路径
     * @param list 列表
     */
    public static void WriteList(String list_path, List<String> list,boolean append){
        StringBuilder content=new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            content.append(list.get(i));
            if(i!=list.size()-1)content.append('\n');
        }
        String content_string=content.toString();
        File mk_txt=new File(list_path);
        writeTextFile(content_string, mk_txt,append);
        Log.d("file io utils","已输出链接到文件");
    }

    public static Bitmap readBitmap(String path){
            Bitmap bitmap=null;
            File picDir=new File(path);
            FileInputStream fis=null;
            try{
                fis=new FileInputStream(picDir);
                bitmap= BitmapFactory.decodeStream(fis);
            } catch (Exception e) {
                System.out.println("bitmap not found,name:"+path);
                if (picDir.exists())picDir.delete();
            }finally {
                if (fis!=null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
    }

    public static void saveBitmap(String path, Bitmap mbitmap) {
        File picIn=new File(path);
        FileOutputStream fot=null;
        try {
            fot=new FileOutputStream(picIn);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        mbitmap.compress(Bitmap.CompressFormat.PNG,100,fot);
        try{
            assert fot != null;
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

    /**
     * 错误报告
     * @param e 错误
     * @param from_where 出处
     */
    public static void WriteErrReport(Throwable e,String ...from_where){

        String errorLogFileDir = StorageUtils.getErrorLogFilePath();
        File Folder =new File(errorLogFileDir);
        if(!Folder.exists()){
            Folder.mkdir();
        }
        File mk_txt=new File(errorLogFileDir+TimeUtil.getCurrentDateInString()+"_crashLog.txt" );
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
        writeTextFile(stringBuilder.toString(), mk_txt,true);
    }

    public static List<String> read_list(File file){
        List<String> list = new ArrayList<>();
       if (!file.exists()) {
           Log.e("file io read list","list file_not_found");
           return list;
       }
        FileInputStream fis=null;
        try {
            fis = new FileInputStream(file);
            InputStreamReader i_reader=new InputStreamReader(fis);
            BufferedReader b_reader=new BufferedReader(i_reader);
            String temp="";
            while ((temp=b_reader.readLine())!=null) {
                list.add(temp);
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Log.e("file io read list","list file_not_found");
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

        return list;
    }
}
