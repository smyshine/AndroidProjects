package smy.com.vrplayer.listener;

/**
 * Created by SMY on 2016/8/8.
 */
public interface VRPlayListener {
    void onVideoInit(int length);
    void listenTime(int time);
    void onVideoStartPlay();
    void onErrorPlaying(int errorType, int errorID);
    void onBufferingUpdate(int percent);
    void onVideoFinish();
    void onFrameAvailable();
}
