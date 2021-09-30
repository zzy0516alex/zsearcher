package com.Z.NovelReader.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 新文件类型.znr
 */
public class FileUtils {

    public static String getRootPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 以加密形式输出文件.znr
     * @param content 文件原始内容
     * @param file_parent_path 文件存储路径
     * @param filename 文件名
     */
    public static void outputFile(String content,String file_parent_path,String filename){
        byte[] b_content=content.getBytes();//utf-8
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream output = null;

        try {
            File file=new File(file_parent_path+File.separator+filename+".znr");
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(b_content);
            bis = new BufferedInputStream(byteInputStream);
            fos=new FileOutputStream(file);
            output = new BufferedOutputStream(fos);

            byte[] header={0x05,0x16};
            output.write(header);

            byte[] buffer=new byte[1024];
            int len=0;
            while ((len=bis.read(buffer))!=-1){
                output.write(buffer,0,len);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
                try {
                    if (bis!=null)bis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
//                try {
//                    if (fos!=null)fos.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
                try {
                    if (output!=null)output.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
        }
    }

    /**
     * 删除文件夹及其下所有文件
     * @param dir 文件夹
     */
    public static void deleteAllFiles(File dir) {
        File[] files = dir.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                        Log.e("file util","删除文件失败");
                        e.printStackTrace();
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);//file=null
                        try {
                            f.delete();
                        } catch (Exception e) {
                            Log.e("file util","删除文件失败");
                            e.printStackTrace();
                        }
                    }
                }
            }
        dir.delete();//删除文件夹本身
    }

    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     *         <code>false</code> otherwise
     */
    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /*
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
