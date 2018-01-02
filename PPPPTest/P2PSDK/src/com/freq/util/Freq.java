package com.freq.util;

/**
 * Created by shi.lei on 2016/5/26.
 */
public class Freq {
    private static final String TAG = "Freq";
    static public  native void MyTest();
    static public  native int nativeGetFreq(short[] shArr, int len);
    static public  native int nativeInitFft(int freq, int specialFeq);
    static{
        System.loadLibrary("fftw_android21");
    }
}
