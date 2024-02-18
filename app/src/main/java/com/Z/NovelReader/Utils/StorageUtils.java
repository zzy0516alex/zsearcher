package com.Z.NovelReader.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.Z.NovelReader.Global.MyApplication;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

public class StorageUtils {

    public static final int STORAGE_RQS_CODE = 51600;

    private static final String EXPORT_DIR = "/ZReader ExportFiles/";
    private static final String FIRST_LEVEL_DIR = "/ZsearchRes/";
    private static final String SECOND_LEVEL_DIR = "/BookReserve/";
    private static final String BACKUP_SOURCE_DIR = "/BackupSource/";
    private static final String DOWNLOAD_CONTENT_DIR = "/DownloadContent/";
    private static final String TEMP_CATALOG_DIR = "/temp_catalogs/";

    private static final String CATALOG_LINK_FILE_NAME = "catalog_link.txt";
    private static final String CATALOG_FILE_NAME = "catalog";
    private static final String CACHE_CONTENT_FILE_NAME = "content.txt";//缓存的章节内容文件
    private static final String COVER_FILE_NAME = "cover.png";
    private static final String SUB_CATALOG_FILE_PATTERN = "%d";
    public static final String CATALOG_FILE_SUFFIX = ".csv";
    private static final String DOWNLOAD_CONTENT_FILE_PATTERN = "%d-%d";
    public static final String DOWNLOAD_CONTENT_FILE_SUFFIX = ".zrc";//z-reader contents
    private static final String SOURCE_FILE_PATTERN = "Source%s";
    public static final String SOURCE_FILE_SUFFIX = ".zrs";//z-reader sources

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
        return MyApplication.getExternalDir() + FIRST_LEVEL_DIR;
    }

    //3.book reserve path
    public static String getBookReservePath(){
        return getResPath() + SECOND_LEVEL_DIR;
    }

    //4.unique book storage path
    public static String getBookStoragePath(String book_name,String writer){
        String sub_path = String.format("/%s-%s/", book_name, writer);
        return getBookReservePath() + sub_path;
    }

    //5.0.book catalog link path
    public static String getBookCatalogLinkPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + CATALOG_LINK_FILE_NAME;
    }

    //5.1.book catalog path
    public static String getBookCatalogPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + CATALOG_FILE_NAME + CATALOG_FILE_SUFFIX;
    }

    //5.2.book content path
    public static String getBookContentPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + CACHE_CONTENT_FILE_NAME;
    }

    //5.3.book cover path
    public static String getBookCoverPath(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + COVER_FILE_NAME;
    }

    //6.sub catalog directory
    public static String getSubCatalogDir(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + TEMP_CATALOG_DIR;
    }

    //7.sub catalog path
    public static String getSubCatalogPath(String book_name,String writer,int sequence){
        String sub_path = String.format(Locale.CHINA,SUB_CATALOG_FILE_PATTERN + CATALOG_FILE_SUFFIX, sequence);
        return getSubCatalogDir(book_name,writer) + sub_path;
    }
    public static String getSubCatalogPath(String subCatalogDir,int sequence){
        String sub_path = String.format(Locale.CHINA,SUB_CATALOG_FILE_PATTERN + CATALOG_FILE_SUFFIX, sequence);
        return subCatalogDir + sub_path;
    }

    //8.backup source directory
    public static String getBackupDir(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + BACKUP_SOURCE_DIR;
    }

    //9.backup source sub directory
    public static String getBackupSubDir(String book_name,String writer,int source_id){
        String sub_path = String.format(Locale.CHINA,"/%d/", source_id);
        return getBackupDir(book_name,writer) + sub_path;
    }

    //10.0. backup source sub catalog directory
    public static String getBackupSourceSubCatalogDir(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + TEMP_CATALOG_DIR;
    }

    //10.1. backup source sub catalog path
    public static String getBackupSourceSubCatalogPath(String book_name,String writer,int source_id,int sequence){
        String sub_path = String.format(Locale.CHINA,SUB_CATALOG_FILE_PATTERN + CATALOG_FILE_SUFFIX, sequence);
        return getBackupSourceSubCatalogDir(book_name,writer,source_id) + sub_path;
    }

    //10.2.backup source catalog link path
    public static String getBackupSourceCatalogLinkPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + CATALOG_LINK_FILE_NAME;
    }

    //10.3.backup source catalog path
    public static String getBackupSourceCatalogPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + CATALOG_FILE_NAME + CATALOG_FILE_SUFFIX;
    }

    //10.4.backup source cover path
    public static String getBackupSourceCoverPath(String book_name,String writer,int source_id){
        return getBackupSubDir(book_name,writer,source_id) + COVER_FILE_NAME;
    }

    //11.temp catalog storage
    public static String getTempCatalogPath(){
        return getResPath() + CATALOG_FILE_NAME + CATALOG_FILE_SUFFIX;
    }

    //12.temp catalog link storage
    public static String getTempCatalogLinkPath(){
        return getResPath() + CATALOG_LINK_FILE_NAME;
    }

    //13.0. download content directory
    public static String getDownloadContentDir(String book_name,String writer){
        return getBookStoragePath(book_name,writer) + DOWNLOAD_CONTENT_DIR;
    }
    //13.1. download content file path
    public static String getDownloadContentPath(String book_name,String writer,int chapIndex){
        int i = chapIndex / 50;
        //example: 0-49 , 50-99, 100-149,...
        String path = String.format(Locale.CHINA, DOWNLOAD_CONTENT_FILE_PATTERN + DOWNLOAD_CONTENT_FILE_SUFFIX, i * 50, i * 50 + 49);
        return getDownloadContentDir(book_name,writer)+path;
    }

    //14. SD卡根目录 /storage/...
    public static String getSDCardRootPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    //14.0. export directory (source export,...)
    public static String getExportPath(){
        return getSDCardRootPath() + EXPORT_DIR;
    }

    public static String getSourceExportPath(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss",Locale.US); //设置时间格式
        Date curDate = new Date(System.currentTimeMillis()); //获取当前时间
        String createDate = formatter.format(curDate);   //格式转换
        String path = String.format(Locale.CHINA, SOURCE_FILE_PATTERN + SOURCE_FILE_SUFFIX, createDate);
        return getExportPath()+path;
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

        File ExportDir =new File(getExportPath());
        if(!ExportDir.exists()){
            ExportDir.mkdir();
        }
    }

    public static void createBookFolders(String bookName, String writer){
        File book_parent_folder = new File(getBookStoragePath(bookName, writer));
        if(!book_parent_folder.exists())
            book_parent_folder.mkdir();

        File backup_folder = new File(getBackupDir(bookName,writer));
        if(!backup_folder.exists())
            backup_folder.mkdir();

        File download_folder = new File(getDownloadContentDir(bookName,writer));
        if(!download_folder.exists())
            download_folder.mkdir();
    }
}
