package com.example.smy.photoloading;

import java.io.Serializable;

/**
 * Created by SMY on 2016/9/2.
 */
public class LocalMediaInfo implements Serializable {

    public static final String LOCAL_MEDIA_INFO = "local_media_info";

    public static final int LEFT=-1;
    public static final int RIGHT=1;

    private static final long serialVersionUID = 1L;
    public static final int TYPE_OTHER = -1;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_PICTURE = 1;
    public static final int TYPE_GIF = 2;
    public int type; // 0 video, 1 picture, 2 gif

    public String fileName;
    public String filePath;
    public int id;
    public long time;
    public boolean checked;
    public long size;
    public int duration;
    public int width_height[] = new int[2];
    public long headerId;
    public int gridPos;

    //以下参数特用于发布，这块处理不太合理 后续会抽出来
    public int origin;
    public String name;
    public String thumb;
    public String title;
    public String content;

    public static int getFileType(String fileName) {
        int fileType = LocalMediaInfo.TYPE_OTHER;

        if (fileName == null) {
            return LocalMediaInfo.TYPE_OTHER;
        }

        if (fileName.endsWith("mp4") || fileName.endsWith("MP4")) {
            fileType = LocalMediaInfo.TYPE_VIDEO;
        } else if (fileName.endsWith("jpg") || fileName.endsWith("JPG")
                || fileName.endsWith("png") || fileName.endsWith("PNG")) {
            fileType = LocalMediaInfo.TYPE_PICTURE;
        } else if (fileName.endsWith("gif") || fileName.endsWith("GIF")) {
            fileType = LocalMediaInfo.TYPE_GIF;
        }

        return fileType;
    }
}
