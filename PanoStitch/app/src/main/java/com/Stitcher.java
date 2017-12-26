package com;

import com.panostitch.CombineParams;

/**
 * Created by SMY on 2017/12/7.
 */

public class Stitcher {

    static {
        System.loadLibrary("imageStitch");
    }

    private static Stitcher instance;
    private Stitcher(){}

    public static Stitcher getInstance(){
        if (instance == null){
            synchronized (Stitcher.class){
                if (instance == null){
                    instance = new Stitcher();
                }
            }
        }
        return instance;
    }

    public native void imageStitch(String src, String dst, CombineParams params, String datPath);

}
