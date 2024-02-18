package com.Z.NovelReader.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 阅读器书籍文本保存文件读写
 * [格式]：
 * Header：
 * |包含章节数(UnsignedByte 1~50)|章节i的索引(UnsignedByte 0~49)|章节i的起始byte(Integer 0~2^31)|章节i的长度byte(Integer 0~2^31)|其他章节...
 * Body：
 * |章节i String(Bytes)|章节j String(Bytes)|...对应header索引
 * 注意：header中的各章节起始位置标识是从0开始到不包含header长度的位置，实际使用中需加上header长度
 * header长度 = 1 + Header[0] * (1+4*2)
 */
public class ContentFileIO {

    public static class ContentFileHeader{
        public int pos;//在header中的索引
        public int index;//在目录中的索引
        public int begin;
        public int length;
    }

    public synchronized static void writeContent(String content_file, String zipped_content, int index) throws FileNotFoundException {
        byte[] content_bytes = zipped_content.getBytes(StandardCharsets.UTF_8);
        boolean isFileExists = (new File(content_file)).exists();
        File Dir = (new File(content_file)).getParentFile();
        RandomAccessFile file = new RandomAccessFile(content_file,"rwd");
        if(!isFileExists){
            //如果不存在，直接写入
            try {
                //写入header
                file.writeByte(1);//章节数
                file.writeByte(index);//章节索引
                file.writeInt(0);//起始位置
                file.writeInt(content_bytes.length);//章节长度
                file.write(content_bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            //先读取header
            try {
                int size = file.readUnsignedByte();
                ContentFileHeader matched_item = null;
                ContentFileHeader last_item = null;
                List<ContentFileHeader> headerList = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ContentFileHeader header_item = new ContentFileHeader();
                    header_item.pos = i;
                    header_item.index = file.readUnsignedByte();
                    header_item.begin = file.readInt();
                    header_item.length = file.readInt();
                    headerList.add(header_item);
                    if(header_item.index == index){
                        matched_item = header_item;
                    }
                    if(i==size-1)last_item = header_item;
                }
                if(matched_item!=null){
                    //若文件中已存在当前章节的内容，则更新
                    int header_length = size*9+1;
                    long total_length = file.length();
                    int delta_content_length = content_bytes.length - matched_item.length;
                    File tempFile = File.createTempFile("content", null,Dir);
                    FileOutputStream temp_output = new FileOutputStream(tempFile);
                    FileInputStream temp_input = new FileInputStream(tempFile);
                    //先保存后面的内容
                    file.seek(header_length + matched_item.begin+ matched_item.length);
                    byte[] buff = new byte[1024];
                    //用于保存实际读取的字节数
                    int hasRead = 0;
                    //使用循环方式读取插入点后的数据
                    while ((hasRead = file.read(buff)) > 0) {
                        //将读取的数据写入临时文件
                        temp_output.write(buff, 0, hasRead);
                    }
                    //把文件记录指针重新定位到pos位置
                    file.seek(header_length + matched_item.begin);
                    //追加需要插入的内容
                    file.write(content_bytes);
                    //追加临时文件中的内容
                    while ((hasRead = temp_input.read(buff)) > 0) {
                        file.write(buff, 0, hasRead);
                    }
                    //更新header
                    seekToHeaderItem(matched_item.pos,2,file);//起始位置不变，但长度可能不同
                    file.writeInt(content_bytes.length);
                    //后续所有item的起始位置都改变
                    for (int i = matched_item.pos+1; i < size; i++) {
                        seekToHeaderItem(i,1,file);
                        file.writeInt(headerList.get(i).begin + delta_content_length);
                    }
                    //更新fileLength防止最后一项内容缩短导致的长度异常
                    file.setLength(total_length+delta_content_length);
                    tempFile.delete();
                }
                else if(last_item!=null){
                    //若还不存在，则在末尾追加
                    //先改写文件头
                    file.seek(0);
                    file.writeByte(size+1);//新增了一章
                    seekToHeaderItem(size,0,file);//定位到header
                    //保存之后的body避免覆盖
                    File tempFile = File.createTempFile("content", null,Dir);
                    FileOutputStream temp_output = new FileOutputStream(tempFile);
                    FileInputStream temp_input = new FileInputStream(tempFile);
                    byte[] buff = new byte[1024];
                    int hasRead = 0;
                    //使用循环方式读取插入点后的数据
                    while ((hasRead = file.read(buff)) > 0) {
                        temp_output.write(buff, 0, hasRead);
                    }
                    seekToHeaderItem(size,0,file);//在header末尾追加
                    file.writeByte(index);//章节索引
                    file.writeInt(last_item.begin+last_item.length);//起始位置
                    file.writeInt(content_bytes.length);//章节长度
                    //补全剩余内容
                    while ((hasRead = temp_input.read(buff)) > 0) {
                        file.write(buff, 0, hasRead);
                    }
                    //在末尾追加
                    file.seek(file.length());
                    file.write(content_bytes);
                    tempFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static String readContent(String content_file, int index) throws FileNotFoundException {
        RandomAccessFile file = new RandomAccessFile(content_file,"r");
        String result = "";
        try {
            ContentFileHeader matchedHeader = null;
            int size = file.readUnsignedByte();
            int header_length = size*9+1;
            for (int i = 0; i < size; i++) {
                ContentFileHeader header_item = new ContentFileHeader();
                header_item.pos = i;
                header_item.index = file.readUnsignedByte();
                header_item.begin = file.readInt();
                header_item.length = file.readInt();
                if(header_item.index == index)matchedHeader = header_item;
            }
            if(matchedHeader==null)return result;

            byte[] buff = new byte[matchedHeader.length];
            file.seek(header_length + matchedHeader.begin);
            file.read(buff,0, matchedHeader.length);
            result = new String(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param item_pos 需要定位到的header中哪一部分
     * @param inItemIndex 需要定位到该部分中哪一字段 0-章节index字段 1-章节起始位置字段 2-章节长度字段
     * @param file 文件句柄
     * @throws IOException
     */
    private static void seekToHeaderItem(int item_pos, int inItemIndex, RandomAccessFile file) throws IOException {
        int index = 0;
        if(inItemIndex > 0)index+=1;
        if(inItemIndex > 1)index+=4;
        file.seek(item_pos* 9L + 1 + index);
    }

    /**
     * int转bytes 高位在前
     * @param value int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static List<Integer> getAllDownloadedChaps(String dir_path){
        List<String> allFileNames = FileOperateUtils.getAllFileNames(dir_path);
        List<Integer> chap_index = new ArrayList<>();
        for (String file_name : allFileNames) {
            if(!file_name.endsWith(StorageUtils.DOWNLOAD_CONTENT_FILE_SUFFIX))continue;
            String full_path = dir_path + file_name;
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(full_path,"r");
                int size = file.readUnsignedByte();
                int header_length = size*9+1;
                for (int i = 0; i < size; i++) {
                    int index = file.readUnsignedByte();
                    file.readInt();
                    file.readInt();
                    chap_index.add(index);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chap_index;
    }
}
