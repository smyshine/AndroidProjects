package com.example.smy.photoloading;

import android.app.Application;

/**
 * Created by SMY on 2016/9/2.
 */
public class MyApplication extends Application {

    private static MyApplication sInstance = null;

    @Override
    public void onCreate(){
        super.onCreate();
        sInstance = this;
    }

    public static MyApplication getInstance(){
        return sInstance;
    }
}
