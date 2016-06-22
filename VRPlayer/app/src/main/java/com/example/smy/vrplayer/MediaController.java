package com.example.smy.vrplayer;

import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by SMY on 2016/6/22.
 */
public class MediaController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener
        , MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    public static final int UPDATE_RANGE = 100;//更新播放进度幅度
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private MediaPlayer mediaPlayer;
    private int bufferPercent;
    private MediaControllerListener controllerListener;
    private android.os.Handler handler = new android.os.Handler();
    private int mCurrentState = STATE_IDLE;
    private boolean isAutoPlay = false;//设置是否自动播放

    public MediaController() {
        this.mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
    }

    public void setControllerListener(MediaControllerListener listener) {
        controllerListener = listener;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPercent = percent;
        if (mCurrentState == STATE_PLAYING) {
            controllerListener.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        if (controllerListener == null) {
            return;
        }
        controllerListener.onComplete();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        controllerListener.onError();
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentState = STATE_PREPARED;
        controllerListener.onPrepared(mp.getDuration());
        play();
        if (!isAutoPlay) {
            pause();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        controllerListener.onSeekComplete();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        controllerListener.onVideoSizeChanged(width, height);
    }

    public void setHolder(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(holder);
        }
    }

    /**
     * 设置视频链接
     *
     * @param path
     */
    public void setDataSource(String path) {
        if (mCurrentState != STATE_IDLE || mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        mCurrentState = STATE_PLAYING;
        controllerListener.onPlay();
        handler.postDelayed(checkProgress, 500);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        mCurrentState = STATE_PAUSED;
        controllerListener.onPause();
        handler.removeCallbacks(checkProgress);
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public int getCurrentPlayProgress() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getBufferingPercent() {
        return bufferPercent;
    }

    public void seekTo(int currentPlayProgress) {
        if (mCurrentState != STATE_IDLE) {
            mediaPlayer.seekTo(currentPlayProgress);
        }
    }

    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
    }

    public void destory() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    /**
     * 用于获取当前播放进度，刷新进度条
     */
    private Runnable checkProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                controllerListener.onPlayingProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(checkProgress, UPDATE_RANGE);
            }
        }
    };

    public boolean isPlaying() {
        return mediaPlayer != null ? mediaPlayer.isPlaying() : false;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.isAutoPlay = autoPlay;
    }

    public interface MediaControllerListener {
        void onPrepared(int videoDuration);

        void onComplete();

        void onPlay();

        void onPause();

        void onError();

        void onSeekComplete();

        void onPlayingProgress(int progress);

        void onVideoSizeChanged(int width, int height);

        void onBufferingUpdate(int percent);

    }
}
