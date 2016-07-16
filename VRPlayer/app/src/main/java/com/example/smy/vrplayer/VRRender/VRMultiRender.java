package com.example.smy.vrplayer.VRRender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.example.smy.vrplayer.VRListener.VRPlayListener;
import com.google.vrtoolkit.cardboard.HeadTransform;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;

import java.io.IOException;

/**
 * Created by SMY on 2016/7/15.
 */
public class VRMultiRender extends BaseVRRender implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener {

    private String videoPath;
    private VRPlayListener listener;

    private MediaPlayer mMediaPlayer;
    private StreamingTexture mVideoTexture;
    //private Texture mSettingTexture;
    private MediaPlayer mSettingPlayer;
    private StreamingTexture mSettingTexture;

    private boolean mHasInitScreen;

    public VRMultiRender(Context context)
    {
        super(context.getApplicationContext());
    }

    private void initMediaPlayer(Uri dataSource){

        if (this.mMediaPlayer != null ) {
            try {
                this.mMediaPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.mMediaPlayer.release();
            }
            this.mMediaPlayer = null;
        }

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getContext(),dataSource);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("VRRender", "init MediaPlayer failed and excetpion :" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notifyVideoInit(mMediaPlayer.getDuration());
        resetCameraOrientation();
        mMediaPlayer.start();
        if(listener != null)
        {
            listener.onVideoStartPlay();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("Media Player Status", "Completed");
        mp.stop();
        mp.release();
        mp = null;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if(listener != null)
        {
            listener.onBufferingUpdate(percent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(listener != null)
        {
            listener.onErrorPlaying(what,extra);
        }
        return true;
    }

    @Override
    protected void initScene() {
        if(!TextUtils.isEmpty(videoPath))
        {
            initMediaPlayer(Uri.parse(videoPath));
        }

        mVideoTexture = new StreamingTexture("video", mMediaPlayer);
        mVideoTexture.setWrapType(StreamingTexture.WrapType.REPEAT);
        mVideoTexture.enableOffset(true);

        //initMediaPlayer(Uri.parse("/storage/emulated/0/smy/xiaomi.mp4"));
        mSettingTexture = new StreamingTexture("video", mMediaPlayer);
        mSettingTexture.setWrapType(StreamingTexture.WrapType.REPEAT);
        mSettingTexture.enableOffset(true);

        Material material = new Material();
        material.setColorInfluence(0);
        try {
            //DiffuseTextureFragmentShaderFragment.java 添加texture时，相同type的才行，不同type的在watchType发生变化时会导致数组溢出---
            material.addTexture(mVideoTexture);
            //material.addTexture(mSettingTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        initScene(material);
    }

    private Bitmap mPicture;

    public void setBitmap(Bitmap bitmap){
        mPicture = bitmap;
    }

    public void setVideoPath(String videoPath)
    {
        this.videoPath = videoPath;
        if(mSphere != null && !mMediaPlayer.isPlaying()){
            //initMediaPlayer(Uri.parse(videoPath));
        }
    }

    public void setVideoPlayListener(VRPlayListener listener) {
        this.listener = listener;
    }

    public void notifyVideoInit(int length) {
        if(listener != null)
        {
            listener.onVideoInit(length);
        }
    }

    public void notifyTime(int time) {
        if(listener != null)
        {
            listener.listenTime(time);
        }
    }

    public void setMediaPlayerSeekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        mVideoTexture.update();
        //mSettingTexture.update();
        notifyTime(mMediaPlayer.getCurrentPosition());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null)
            mMediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMediaPlayer != null && mMediaPlayer.getVideoHeight() != 0 && mMediaPlayer.getVideoWidth() != 0 && !mMediaPlayer.isPlaying())
            mMediaPlayer.start();
    }



    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }



    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }


    @Override
    public void onRenderSurfaceDestroyed(final SurfaceTexture surface) {
        onDestroy();
    }

    @Override
    public void onRendererShutdown() {
        onDestroy();
    }

    public void onDestroy(){
        super.onRenderSurfaceDestroyed(null);
        if (this.mVideoTexture != null) {
            this.mVideoTexture = null;
        }
        if (this.mMediaPlayer != null ) {
            try {
                this.mMediaPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.mMediaPlayer.release();
            }
            this.mMediaPlayer = null;
        }
        listener = null;
    }
}
