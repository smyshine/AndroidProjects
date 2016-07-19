package com.example.smy.vrplayer.VRRender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import com.example.smy.vrplayer.VRListener.VRPlayListener;
import com.google.vrtoolkit.cardboard.HeadTransform;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;

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

    private void initSettingPlayer(Uri dataSource){

        if (this.mSettingPlayer != null ) {
            try {
                this.mSettingPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.mSettingPlayer.release();
            }
            this.mSettingPlayer = null;
        }

        try {
            mSettingPlayer = new MediaPlayer();
            mSettingPlayer.setDataSource(getContext(),dataSource);
            mSettingPlayer.setLooping(true);
            mSettingPlayer.setOnPreparedListener(this);
            mSettingPlayer.setOnCompletionListener(this);
            mSettingPlayer.setOnErrorListener(this);
            mSettingPlayer.setOnBufferingUpdateListener(this);
            mSettingPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("VRRender", "init MediaPlayer failed and excetpion :" + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notifyVideoInit(mp.getDuration());
        resetCameraOrientation();
        mp.start();
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

    private boolean usePictureStreamingTexture = false;
    private Surface mSurface;
    private volatile boolean mShouldUpdateTexture;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mFrameCount;

    final Runnable mUpdateTexture = new Runnable() {
        public void run() {
            // -- Draw the view on the canvas
            final Canvas canvas = mSurface.lockCanvas(null);
            canvas.drawColor(Color.TRANSPARENT);
            Paint p = new Paint();
            //清屏
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(p);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            canvas.drawBitmap(mPicture, 0, 0, p);
            /*p.setAntiAlias(true);
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            //
            canvas.drawCircle(200, 200, 8.0f, p);
            canvas.drawCircle(800, 200, 8.0f, p);*/
            mSurface.unlockCanvasAndPost(canvas);
            // -- Indicates that the texture should be updated on the OpenGL thread.
            mShouldUpdateTexture = true;
        }
    };

    @Override
    protected void initScene() {
        if(!TextUtils.isEmpty(videoPath))
        {
            initMediaPlayer(Uri.parse(videoPath));
        }

        mVideoTexture = new StreamingTexture("video", mMediaPlayer);
        mVideoTexture.setWrapType(StreamingTexture.WrapType.REPEAT);
        mVideoTexture.enableOffset(true);

        /*initSettingPlayer(Uri.parse("/storage/emulated/0/smy/xiaomi.mp4"));
        mSettingTexture = new StreamingTexture("video", mSettingPlayer);
        mSettingTexture.setWrapType(StreamingTexture.WrapType.REPEAT);
        mSettingTexture.enableOffset(true);*/

        //picture as streaming texture
        /*usePictureStreamingTexture = true;
        mSettingTexture = new StreamingTexture("video", new StreamingTexture.ISurfaceListener() {
            @Override
            public void setSurface(Surface surface) {
                Log.d("smdbg", "setsurface");
                mSurface = surface;
                mSettingTexture.getSurfaceTexture().setDefaultBufferSize(1024, 1024);
            }
        });*/

        /*Texture photo = new Texture("photo", mPicture);
        photo.setWrapType(ATexture.WrapType.REPEAT);
        photo.enableOffset(false);*/

        Material material = new Material();
        material.setColorInfluence(0);
        //material.enableLighting(true);
        //material.setDiffuseMethod(new DiffuseMethod.Lambert());
        try {
            //DiffuseTextureFragmentShaderFragment.java 添加texture时，相同type的才行，不同type的在watchType发生变化时会导致数组溢出---
            material.addTexture(mVideoTexture);
            //material.addTexture(mSettingTexture);
            //material.addTexture(new Texture("photo", mPicture));
            //material.addTexture(new Texture("setting", resourceId));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        initScene(material);
        initScene(new Texture("photo", mPicture));
    }

    private Bitmap mPicture;

    public void setBitmap(Bitmap bitmap){
        mPicture = bitmap;
    }

    private int resourceId;

    public void setResourceId(int id){
        resourceId = id;
    }

    public void setVideoPath(String videoPath)
    {
        this.videoPath = videoPath;
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
        if (usePictureStreamingTexture){
            // -- not a really accurate way of doing things but you get the point :)
            if (mSurface != null && mFrameCount++ >= (mFrameRate * 0.25)) {
                mFrameCount = 0;
                mHandler.post(mUpdateTexture);
            }
            // -- update the texture because it is ready
            if (mShouldUpdateTexture) {
                mSettingTexture.update();
                mShouldUpdateTexture = false;
            }
        }
        else
        {
            //mSettingTexture.update();
        }

        mVideoTexture.update();
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
        if (mSettingPlayer != null && mSettingPlayer.getVideoHeight() != 0 && mSettingPlayer.getVideoWidth() != 0 && !mSettingPlayer.isPlaying())
            mSettingPlayer.start();
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
        if (this.mSettingTexture != null){
            this.mSettingTexture = null;
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
