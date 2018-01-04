package com.h264player;
import android.media.MediaFormat;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by Nat on 2017/1/29.
 */



public interface IPlayer {

    public enum AVFRAME_TYPE{
        AUDIO,
        VIDEO
    }

    void addAVFrame(AVFRAME_TYPE type, byte[] data, long timestampMS, int isKeyFrame);
    void finishAddAVFrame();
    void setupVideoDecoder(String mineType, MediaFormat format) throws IOException;
    void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode);
    void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode, G711UCodec g711);
    void seekTo(float progress);
    void resume();
    void pause();
    void stop();
    void setupCache(IDataCache cache);

    TextureView getTextureView();
}
