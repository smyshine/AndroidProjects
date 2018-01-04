package com.h264player;

import android.media.MediaFormat;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by Nat on 2017/1/29.
 */

public class PlayerDecorator implements IPlayer {

    private IPlayer iPlayer;
    public PlayerDecorator(IPlayer iPlayer) {
        this.iPlayer = iPlayer;
    }

    @Override
    public void addAVFrame(AVFRAME_TYPE type, byte[] data, long timestampMS, int isKeyFrame) {
        iPlayer.addAVFrame(type, data, timestampMS, isKeyFrame);
    }

    @Override
    public void finishAddAVFrame() {
        iPlayer.finishAddAVFrame();
    }

    @Override
    public void setupVideoDecoder(String mineType, MediaFormat format) throws IOException {
        iPlayer.setupVideoDecoder(mineType, format) ;
    }

    @Override
    public void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode) {
        iPlayer.setupPCM(streamType, sampleRateInHz, channelConfig, audioFormat, mode);
    }

    @Override
    public void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode, G711UCodec g711) {
        iPlayer.setupPCM(streamType, sampleRateInHz, channelConfig, audioFormat, mode, g711);
    }

    @Override
    public void seekTo(float progress) {
        iPlayer.seekTo(progress);
    }

    @Override
    public void resume() {
        iPlayer.resume();
    }

    @Override
    public void pause() {
        iPlayer.pause();
    }

    @Override
    public void stop() {
        iPlayer.stop();
    }

    @Override
    public void setupCache(IDataCache cache) {
        iPlayer.setupCache(cache);
    }

    @Override
    public TextureView getTextureView() {
        return iPlayer.getTextureView();
    }
}
