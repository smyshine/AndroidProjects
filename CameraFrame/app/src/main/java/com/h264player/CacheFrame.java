package com.h264player;

/**
 * Created by Nat on 2017/2/11.
 */

public class CacheFrame {
    public final byte[] data;
    public final long timestampMS;
    public final int isKeyFrame;
    public CacheFrame(byte[] data, long timestampMS, int isKeyFrame){
        this.data = data;
        this.timestampMS = timestampMS;
        this.isKeyFrame = isKeyFrame;
    }
}
