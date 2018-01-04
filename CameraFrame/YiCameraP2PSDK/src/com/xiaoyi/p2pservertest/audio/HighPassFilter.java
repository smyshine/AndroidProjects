package com.xiaoyi.p2pservertest.audio;

/**
 * Created by xin.dingfeng on 2016/4/9.
 */
public class HighPassFilter {
    static {
        System.loadLibrary("webrtc_filter"); // to load the webrtc_filter.so library.
    }

    public void init(int freq){
        nativeCreateFilterInstance(freq);
    }

    public void Process(short[] data, int length){

    }

    private static native int nativeCreateFilterInstance(int freq);
    private static native int nativeProcess(short[] data, int length);

}
