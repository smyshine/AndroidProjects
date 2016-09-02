package com.example.smy.photoloading;

import android.os.Environment;

/**
 * Created by SMY on 2016/9/2.
 */
public class AppConstants {
    private static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String MEDIA_NAME = "/DCIM/";
    public static String MEDIA_DIR = SDCARD_ROOT + MEDIA_NAME;
}
