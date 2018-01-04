package com.tnpclient;

import android.content.Context;
import android.media.MediaFormat;
import android.view.TextureView;

import com.h264player.DecodePlayer;
import com.h264player.G711UCodec;
import com.h264player.IDataCache;
import com.h264player.IPlayer;
import com.h264player.SnapshotDecorator;
import com.h264player.ZoomDecorator;

import java.io.IOException;

/**
 * Created by SMY on 2018/1/3.
 */

public class ClientPlayer implements DecodePlayer.OnDecodePlayerPlaybackListener {

    private static final String TAG = "DemoPlayer";
    private SnapshotDecorator player;

    public ClientPlayer(Context context, TextureView textureView){
        DecodePlayer decodePlayer = new DecodePlayer(textureView);
        decodePlayer.setOnDecodePlayerPlaybackListener(this);
        ZoomDecorator zoomDecorator = new ZoomDecorator(context, decodePlayer);
        player = new SnapshotDecorator(zoomDecorator);
    }

    public void setupCache(IDataCache cache){
        player.setupCache(cache);
    }
    public void stop(){
        player.stop();
    }
    public void dataFinish(){
        player.finishAddAVFrame();
    }

    public void setup(String mineType, MediaFormat format) throws IOException {
        player.setupVideoDecoder(mineType, format);
    }

    public void pause(){ player.pause();}

    public void resume(){ player.resume();}

    public void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode){
        player.setupPCM(streamType, sampleRateInHz, channelConfig, audioFormat, mode);
    }

    public void setupPCM(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode, G711UCodec g711){
        player.setupPCM(streamType, sampleRateInHz, channelConfig, audioFormat, mode, g711);
    }

    public void addAVFrame(IPlayer.AVFRAME_TYPE type, byte[] data, long timestampMS, int isKeyFrame){
        player.addAVFrame(type, data, timestampMS, isKeyFrame);
    }

    public void snapshot(String savedPath){
        player.snapshot(savedPath);
    }

    public void seekTo(float progress){
        player.seekTo(progress);
    }

    @Override
    public void onDidFinishPlay() {
        player.stop();
    }

    @Override
    public void onDidPlay() {

    }

}
