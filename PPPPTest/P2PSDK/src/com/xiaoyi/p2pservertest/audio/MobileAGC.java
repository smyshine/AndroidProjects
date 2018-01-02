package com.xiaoyi.p2pservertest.audio;

/**
 * Created by xin.dingfeng on 2016/3/24.
 */
public class MobileAGC {
  //  static {
  //      System.loadLibrary("webrtc_agc"); // to load the libwebrtc_aecm.so library.
  //  }

    private int mAgcHandler = -1;
    private int minLevel = 0;
    private int maxLevel = 255;

    public void init(int SampFreq, int compressionGaindB, int targetLevelDbfs){
        mAgcHandler =  nativeCreateAgcInstance();
        int ret = nativeInitializeAgcInstance(mAgcHandler, minLevel, maxLevel, SampFreq,  compressionGaindB, targetLevelDbfs);
    }

    public void Process(short []input, int num_bands, int samples, short[] output, int inMicLevel, short echo){
        nativeProcess(mAgcHandler, input, num_bands, samples, output, inMicLevel, echo);
    }

    public void close(){
        nativeClose(mAgcHandler);
    }

    private static native int nativeCreateAgcInstance();

    private static native int nativeInitializeAgcInstance(int AgcHandler, int mMinLevel, int mMaxLevel,
                                                         int samplingFrequency, int compressionGaindB, int targetLevelDbfs);

    private static native int nativeProcess(int AgcHandler, short []input, int num_bands, int samples, short[] output, int inMicLevel, short echo);

    private static native int nativeClose(int AgcHandler);
}
