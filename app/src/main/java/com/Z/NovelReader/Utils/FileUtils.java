package com.Z.NovelReader.Utils;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
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
}
