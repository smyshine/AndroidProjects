package com.h264player;

/**
 * Created by starvedia on 2017/2/11.
 */


public interface IDataCache {
    CacheFrame popVideoFrame();
    CacheFrame popAudioFrame();
    boolean pushVideoFrame(CacheFrame videoFrame);
    boolean pushAudioFrame(CacheFrame audioFrame);
    void clear();
    void seekTo(float progress);
    int getCacheCount();
}
