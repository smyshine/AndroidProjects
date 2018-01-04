package com.xiaoyi.camera.sdk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by shi.lei on 2016/5/27.
 */
public class SharedPreferencesUtil {

    private static Context context;

    public static final String DEVICE = "device";
    public static final String IS_NEED_TEST = "isneedtest";
    public static final String DEVICE_DELAYTIME = "delaytime";
    public static final String VOLUME_TYPE = "volume_type";

    private static SharedPreferencesUtil instance;
    private SharedPreferences sharedPreferences;

//    初始化 context

    public static void init(Context mcontext){
        context= mcontext;
    }

    private SharedPreferencesUtil(){
        sharedPreferences = context.getSharedPreferences(DEVICE , Context.MODE_PRIVATE);
    }

    public static SharedPreferencesUtil getInstance(){
        if(instance ==null){
            synchronized (SharedPreferencesUtil.class){
                instance = new SharedPreferencesUtil();
            }
        }
        return instance;
    }

    public int getDelayTime(){
        return sharedPreferences.getInt(DEVICE_DELAYTIME, 150);
    }

    public void putDelayTime(int time){
        sharedPreferences.edit().putInt(DEVICE_DELAYTIME , time).commit();
    }

    public boolean getIsNeedTest(){
        return sharedPreferences.getBoolean(IS_NEED_TEST , true);
    }

    public void putIsNeedTest(boolean isNeed){
        sharedPreferences.edit().putBoolean(IS_NEED_TEST , isNeed).commit();
    }

    public void putVolumeType(int type){
        sharedPreferences.edit().putInt(VOLUME_TYPE, type);
    }

    public int getVolumeType(){
        return sharedPreferences.getInt(VOLUME_TYPE, 4);
    }

}
