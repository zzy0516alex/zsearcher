package com.Z.NovelReader.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.Z.NovelReader.Global.MyApplication;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;

public class StorageUtils {

    public static final int STORAGE_RQS_CODE = 51600;

    /**
     *
     * @param context 当前活动引用
     * @return 总存储空间大小
     */
    public static long getROMTotalSize(final Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return blockSize * totalBlocks;

    }

    /**
     *
     * @param context 当前活动引用
     * @return 可用存储空间大小
     */
    public static long getROMAvailableSize(final Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return blockSize * availableBlocks;
    }

    //调用系统函数，字符串转换 long -String KB/MB
    public static String formateFileSize(Context context,long size){
        return Formatter.formatFileSize(context, size);
    }

    public static String getFileSize(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();

    }

    public static boolean isStoragePermissionGranted(Context context, Activity activity) {
        int readPermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermissionCheck == PackageManager.PERMISSION_GRANTED
                && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.v("storage setting", "Permission is granted");
            return true;
        } else {
            Log.v("storage setting", "Permission is revoked");
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_RQS_CODE);
            return false;
        }
    }

    //manage novel info storage
    //1.Error logger path
    public static String getErrorLogFilePath(){
        return MyApplication.getExternalDir() + "/Errors/";
    }

    //2.Res path
    public static String getResPath(){
        return MyApplication.getExternalDir() + "/ZsearchRes/";
    }

    //3.book reserve path
    public static String getBookReservePath(){
        return getResPath() + "/BookReserve/";
    }

    //4.unique book storage path
    public static String getBookStoragePath(String book_name,String writer){
        String sub_path = String.format("/%s-%s/", book_name, writer);
        return getBookReservePath() + sub_path;
    }

    //5.0.book catalog link path
    public static String getBookCatalogLinkPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/catalog_link.txt";
    }

    //5.1.book catalog path
    public static String getBookCatalogPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/catalog.txt";
    }

    //5.2.book content path
    public static String getBookContentPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/content.txt";
    }

    //5.3.book cover path
    public static String getBookCoverPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/cover.png";
    }

    //6.sub catalog dictionary
    public static String getSubCatalogDir(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/temp_catalogs/";
    }

    //7.sub catalog path
    public static String getSubCatalogPath(String book_name,String writer,int sequence){
        String sub_path = String.format(Locale.CHINA,"/%d.txt", sequence);
        return getSubCatalogDir(book_name,writer) + sub_path;
    }

    //8.backup source dictionary
    public static String getBackupDir(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + "/BackupSource/";
    }

    //9.backup source sub dictionary
    public static String getBackupSubDir(String book_name,String writer,int source_id){
        String sub_path = String.format(Locale.CHINA,"/%d/", source_id);
        return getBackupDir(book_name,writer) + sub_path;
    }

    //10.0. backup source sub catalog dictionary
    public static String getBackupSourceSubCatalogDir(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + "/temp_catalogs/";
    }

    //10.1. backup source sub catalog path
    public static String getBackupSourceSubCatalogPath(String book_name,String writer,int source_id,int sequence){
        String sub_path = String.format(Locale.CHINA,"/%d.txt", sequence);
        return getBackupSourceSubCatalogDir(book_name,writer,source_id) + sub_path;
    }

    //10.2.backup source catalog link path
    public static String getBackupSourceCatalogLinkPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + "/catalog_link.txt";
    }

    //10.3.backup source catalog path
    public static String getBackupSourceCatalogPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + "/catalog.txt";
    }

    //10.4.backup source cover path
    public static String getBackupSourceCoverPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + "/cover.png";
    }

    //11.temp catalog storage
    public static String getTempCatalogPath(){
        return getResPath()+"/catalog.txt";
    }

    //12.temp catalog link storage
    public static String getTempCatalogLinkPath(){
        return getResPath()+"/catalog_link.txt";
    }

    public static String getDownloadPath(){
        return FileUtils.getRootPath() + "/ZS Downloads/";
    }


    //create folders for book data storage
    public static void createResFolders(){
        File ResDir =new File(getResPath());
        if(!ResDir.exists()){
            ResDir.mkdir();
        }

        File ErrorLogDir = new File(getErrorLogFilePath());
        if(!ErrorLogDir.exists()){
            ErrorLogDir.mkdir();
        }

        File BookReserveDir =new File(getBookReservePath());
        if(!BookReserveDir.exists()){
            BookReserveDir.mkdir();
        }

        File DownloadDir =new File(getDownloadPath());
        if(!DownloadDir.exists()){
            DownloadDir.mkdir();
        }
    }
}
