package com.xiaomi.fastvideo;

import android.util.Log;

import com.xiaomi.h264videoplayer.BuildConfig;

/**
 * Created by Chuanlong on 2016/7/1.
 */
public class MiLog {
    private final static String TAG = "AntsLog";

    private static boolean DEBUG = BuildConfig.DEBUG;

    public final static int MI_LOG_NO_PRINT = -1;
    public final static int MI_LOG_INVALID_PARAMETER = -2;


    protected static int v(String tag, String msg) {
        if(tag == null || msg == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return Log.v(tag, msg);
        else return MI_LOG_NO_PRINT;
    }

    protected static int d(String tag, String msg) {
        if(tag == null || msg == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return Log.d(tag, msg);
        else return MI_LOG_NO_PRINT;
    }

    protected static int i(String tag, String msg) {
        if(tag == null || msg == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return Log.i(tag, msg);
        else return MI_LOG_NO_PRINT;
    }

    protected static int w(String tag, String msg) {
        if(tag == null || msg == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return Log.w(tag, msg);
        else return MI_LOG_NO_PRINT;
    }

    protected static int e(String tag, String msg) {
        if(tag == null || msg == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return Log.e(tag, msg);
        else return MI_LOG_NO_PRINT;
    }

    protected static int e(String tag, String msg, Throwable tr) {
        if(tag == null || msg == null || tr == null) return MI_LOG_INVALID_PARAMETER;
        if(DEBUG) return  Log.e(tag, msg, tr);
        else return MI_LOG_NO_PRINT;
    }


    // print it anyway.
    protected static int V(String msg){
        if(msg == null) return MI_LOG_INVALID_PARAMETER;
        return Log.v(TAG, msg);
    }

    protected static int D(String msg){
        if(msg == null) return MI_LOG_INVALID_PARAMETER;
        return Log.d(TAG, msg);
    }

    protected static int I(String msg){
        if(msg == null) return MI_LOG_INVALID_PARAMETER;
        return Log.i(TAG, msg);
    }

    protected static int W(String msg){
        if(msg == null) return MI_LOG_INVALID_PARAMETER;
        return Log.w(TAG, msg);
    }

    protected static int E(String msg){
        if(msg == null) return MI_LOG_INVALID_PARAMETER;
        return Log.e(TAG, msg);
    }

    protected static int E(String msg, Throwable tr){
        if(msg == null || tr == null) return MI_LOG_INVALID_PARAMETER;
        return Log.e(TAG, msg, tr);
    }

    public static void setDebug(boolean debug){
        DEBUG = debug;
    }

}
