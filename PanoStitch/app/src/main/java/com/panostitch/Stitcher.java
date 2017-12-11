package com.panostitch;

/**
 * Created by SMY on 2017/12/7.
 */

public class Stitcher {

    static {
        System.loadLibrary("native-lib");
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void imageStitch(String src, String dst, String params);

}
