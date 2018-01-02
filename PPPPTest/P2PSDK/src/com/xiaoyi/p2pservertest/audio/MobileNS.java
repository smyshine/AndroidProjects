package com.xiaoyi.p2pservertest.audio;


/**
 * Created by xin.dingfeng on 2016/3/14.
 */
public class MobileNS {
   // static {
  //      System.loadLibrary("webrtc_ns"); // to load the libwebrtc_aecm.so library.
  //  }

    private int mNsHandler = -1;   // the handler of NS instance.

    public void init(int SampFreq)
    {
        mNsHandler = nativeCreateNsInstance();
        int ret = nativeInitializeNsInstance(mNsHandler, SampFreq);
    }

    public void setPolicyMode(int mode)
    {
       int ret = nativeSetNsMode(mNsHandler, mode);
    }

    public void NsProcess(short []speechFrame, int num_bands, short []outFrame)
    {
        int ret = nativeNsProcess(mNsHandler, speechFrame, num_bands, outFrame);
    }

    public void close(){
        int ret = nativeClose(mNsHandler);
    }

    private static native int nativeCreateNsInstance();

    private static native int nativeInitializeNsInstance(int NsHandler,
                                                           int samplingFrequency);

    private static native int nativeSetNsMode(int NsHandler, int mode);

    private static native int nativeClose(int NsHandler);

    private static native int nativeNsProcess(int NsHander, short []speechFrame, int num_bands, short []outFrame);

}
