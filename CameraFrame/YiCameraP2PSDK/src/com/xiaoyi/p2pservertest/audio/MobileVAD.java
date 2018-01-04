package com.xiaoyi.p2pservertest.audio;

/**
 * Created by xin.dingfeng on 2016/4/8.
 */
public class MobileVAD {
  //  static {
 //       System.loadLibrary("webrtc_vad"); // to load the libwebrtc_aecm.so library.
 //   }

    private int mVadHandler = -1;   // the handler of NS instance.

    public void init()
    {
        mVadHandler = nativeCreateVADInstance();
        int ret = nativeInitializeVADInstance(mVadHandler);
    }

    public void setPolicyMode(int mode)
    {
        int ret = nativeSetVADMode(mVadHandler, mode);
    }

    public int VADProcess(short []speechFrame, int length, int freq)
    {
        int ret = nativeVADProcess(mVadHandler, speechFrame, length, freq);
        return ret;
    }

    public void close(){
        nativeVADclose(mVadHandler);
    }

    private static native int nativeCreateVADInstance();
    private static native int nativeInitializeVADInstance(int VadHandler);
    private static native int nativeSetVADMode(int VadHandler, int mode);
    private static native int nativeVADProcess(int VadHandler, short [] speechFrame, int length, int freq);
    private static native int nativeVADclose(int VadHandler);

}
