package com.lib.ble.manager;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by SMY on 2017/1/17.
 */
public class BleLogger {

    private static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String LOG_DIR = SDCARD_ROOT + "/YI360/log/";
    public static String LOG_EXTENSION = ".log";

    private static final String COMMAND_LOG_PREF = "BleLog_";
    public static final String LAST_WRITE_LOG_FILE_NAME = "ble_last_log_file_name";

    private static ExecutorService mLogThreadExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactory(){

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r,"BleWriteLog");
                    return t;
                }
            }
    );

    public static void setLogDir(String dir){
        LOG_DIR = dir;
    }

    public static void print(String tag, String msg, Object... args) {
        // if (BuildConfig.DEBUG) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.d(tag, msg != null ? msg : "");
        //  }
    }

    public static void print2File(final String tag, String msg, Object... args){
        final String logInfo;
        if (args.length > 0) {
            logInfo = String.format(msg, args);
        } else if (msg == null) {
            logInfo = "";
        } else {
            logInfo = msg;
        }
        BleLogger.print(tag, logInfo);
        mLogThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeLog(tag, logInfo);
            }
        });
    }

    private static void writeLog(String tag, String logInfo){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String strDate = formatter.format(curDate);
        File dir = new File(LOG_DIR);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName = LOG_DIR + COMMAND_LOG_PREF + strDate.substring(0,10) + LOG_EXTENSION;
        String lastFileName = BlePreferenceUtil.getInstance().getString(LAST_WRITE_LOG_FILE_NAME);
        if(!TextUtils.equals(fileName,lastFileName)){
            File file = new File(lastFileName);
            file.delete();
            BlePreferenceUtil.getInstance().putString(LAST_WRITE_LOG_FILE_NAME,fileName);
        }
        String str = strDate + tag + "   " + logInfo + "\n";
        FileOutputStream fout = null;
        try{
            fout = new FileOutputStream(fileName,true);
            byte [] bytes = str.getBytes();
            fout.write(bytes);
            fout.close();
        }
        catch(Exception e){
            e.printStackTrace();
        } finally {
            if(fout != null){
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
