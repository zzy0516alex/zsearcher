package com.Z.NovelReader.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//本类用于文件操作(非读写)
public class FileOperateUtils {

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
                Log.e("copyFile", "oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("copyFile", "oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("copyFile", "oldFile cannot read.");
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

    public static List<String> getAllFileNames(String path){
        List<String> fileList = new ArrayList<>();

        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                String fileName = tempList[i].getName();
                fileList.add(fileName);
            }
        }

        return fileList;
    }

    public static List<String> getAllDirNames(String path){
        List<String> fileList = new ArrayList<>();

        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList==null)return fileList;
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isDirectory()) {
                String fileName = tempList[i].getName();
                System.out.println("文件夹:" + fileName);
                fileList.add(fileName);
            }
        }

        return fileList;
    }

}
