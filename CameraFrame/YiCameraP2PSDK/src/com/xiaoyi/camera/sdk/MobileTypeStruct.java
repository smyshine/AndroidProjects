package com.xiaoyi.camera.sdk;

/**
 * Created by Administrator on 2016/7/4.
 */
public class MobileTypeStruct {

    public int mAecDealy;
    public int mAgcPlayGain;
    public int mAgcPlayLimit;
    public int mAgcRecGain;
    public int mAgcRecLimit;
    public MobileTypeStruct(int delay, int agcPlayGain, int agcPlayLimit, int agcRecGain, int agcRecLimit)
    {
        mAecDealy = delay;
        mAgcPlayGain = agcPlayGain;
        mAgcPlayLimit = agcPlayLimit;
        mAgcRecGain = agcRecGain;
        mAgcRecLimit = agcRecLimit;
    }
}
