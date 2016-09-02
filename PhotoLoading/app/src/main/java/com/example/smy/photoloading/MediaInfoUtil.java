package com.example.smy.photoloading;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2016/9/2.
 */
public class MediaInfoUtil {
    public static List<FileInfo> getAllMediaInfo(Context context, String path) {
        List<FileInfo> fileInfos = new ArrayList<FileInfo>();

        String[] projection = {MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE, MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Video.Media.DATA + " like '" + path + "%'", null, null);
        if (cursor == null) {
            return fileInfos;
        }

        while (cursor.moveToNext()) {
            FileInfo mediaInfo = new FileInfo();
            mediaInfo.fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            mediaInfo.filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            // mediaInfo.thumb = cursor.getString(cursor
            // .getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC));
            mediaInfo.time = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)) ;
            mediaInfo.day = Long.valueOf(DateUtil.formatYYYYMMDD(mediaInfo.time * 1000L));
            mediaInfo.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
            mediaInfo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            mediaInfo.width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
            mediaInfo.height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
            fileInfos.add(mediaInfo);
        }
        cursor.close();

        String[] imageProjection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
        Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection, MediaStore.Images.Media.DATA + "  like '" + path + "%'", null, null);
        while (imageCursor.moveToNext()) {
            FileInfo mediaInfo = new FileInfo();
            mediaInfo.fileName = imageCursor
                    .getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            mediaInfo.filePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            mediaInfo.time = imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            mediaInfo.day = Long.valueOf(DateUtil.formatYYYYMMDD(mediaInfo.time * 1000l));
            mediaInfo.id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            fileInfos.add(mediaInfo);
        }
        imageCursor.close();

        return fileInfos;
    }

    /**
     * 删除指定的视频
     *
     * @param context
     * @param videoPath
     * @return 删除成功返回true，失败返回false。
     */
    public static boolean deleteVideoMediaInfo(Context context, String videoPath) {
        int row = 0;
        row = context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.DATA + " = '" + videoPath + "'", null);
        if (row > 0) {
            return true;
        }
        return false;
    }

    /**
     * 删除指定的视频
     *
     * @param context
     * @param picPath
     * @return 删除成功返回true，失败返回false。
     */
    public static boolean deletePicMediaInfo(Context context, String picPath) {
        int row = 0;
        row = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " = '" + picPath + "'", null);
        if (row > 0) {
            return true;
        }
        return false;
    }

    public static class FileInfo implements Serializable {
        public String filePath;
        public String fileName;
        public int id;
        public long time;
        public long size;
        public int width;
        public int height;

        public long day;
    }
}
