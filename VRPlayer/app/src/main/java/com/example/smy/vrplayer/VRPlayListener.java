package com.example.smy.vrplayer;

/**
 * Created by SMY on 2016/6/22.
 */
public interface VRPlayListener {
    void onVideoInit(int length);
    void listenTime(int time);
    void onVideoStartPlay();
    void onErrorPlaying(int errorType, int errorID);
    void onBufferingUpdate(int percent);
}
