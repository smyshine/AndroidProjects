package com.example.smy.vrplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by SMY on 2016/6/22.
 */
public class CustomPlayerView extends LinearLayout implements SurfaceHolder.Callback, VideoSeekBar.OnSeekBarChangedListener, View.OnClickListener {
    public static final java.lang.String TAG = CustomPlayerView.class.getName();

    private SurfaceView surfaceView;
    private VideoSeekBar seekBar;
    private ImageView imgPlayOrPause;
    private LinearLayout llVideoLoader;

    private SurfaceHolder holder;
    private OnPlayerChangeListener onPlayerChangeListener;
    private int allTime;//视频总时长
    private int currentPlayProgress;
    private int style;//播放器样式

    private MediaController controller;
    private int videoWidth;
    private int videoHeight;

    public CustomPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_media_player, null);
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        seekBar = (VideoSeekBar) view.findViewById(R.id.cusSeekBar);
        imgPlayOrPause = (ImageView) view.findViewById(R.id.imgPlayOrPause);
        llVideoLoader = (LinearLayout) view.findViewById(R.id.ll_loadVideo);
        surfaceView.getLayoutParams().height = (int) (DensityUtil.getScreenWidth(context) * 0.5625);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        seekBar.setOnSeekBarChangedListener(this);
        seekBar.setOnClickListener(this);
        imgPlayOrPause.setOnClickListener(this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomPlayerView);
        Drawable playIcon = typedArray.getDrawable(R.styleable.CustomPlayerView_playSrc);
        Drawable scaleIcon = typedArray.getDrawable(R.styleable.CustomPlayerView_scaleSrc);
        Drawable seekIcon = typedArray.getDrawable(R.styleable.CustomPlayerView_seekSrc);
        boolean isNeedScale = typedArray.getBoolean(R.styleable.CustomPlayerView_isNeedScale, true);
        int controlBarHeight = typedArray.getDimensionPixelSize(R.styleable.CustomPlayerView_controlBarHeight, R.dimen.length_33);
        style = typedArray.getInteger(R.styleable.CustomPlayerView_style, context.getResources().getInteger(R.integer.style_lay));
        setPlayBackground(playIcon);
        setScaleBackground(scaleIcon);
        setSeekBackground(seekIcon);
        setNeedScale(isNeedScale);
        setSeekBarHeight(controlBarHeight);
        setLayoutStyle(context, style);
        addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * 调整seek布局
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setLayoutStyle(Context ctx, int style) {
        RelativeLayout.LayoutParams seekParams = (RelativeLayout.LayoutParams) seekBar.getLayoutParams();
        if (style == ctx.getResources().getInteger(R.integer.style_lay)) {
            seekParams.removeRule(RelativeLayout.ALIGN_BOTTOM);
            seekParams.addRule(RelativeLayout.BELOW, surfaceView.getId());
        } else {
            seekParams.removeRule(RelativeLayout.BELOW);
            seekParams.addRule(RelativeLayout.ALIGN_BOTTOM, surfaceView.getId());
        }
        seekBar.setLayoutParams(seekParams);
    }

    public void setPlayBackground(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        seekBar.setPlayBackground(drawable);
    }

    public void setScaleBackground(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        seekBar.setScaleBackground(drawable);
    }

    public void setSeekBackground(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        seekBar.setSeekBackground(drawable);
    }

    public void setSeekBarHeight(int height) {
        seekBar.getLayoutParams().height = height;
    }

    public void setNeedScale(boolean isNeed) {
        if (isNeed) {
            seekBar.showScaleBtn();
        } else {
            seekBar.hideScaleBtn();
        }
    }

    /**
     * 设置播放控制器 需要完成view的创建后 首先调用
     *
     * @param controll
     */
    public void setMeidaControll(MediaController controll) {
        this.controller = controll;
    }

    /**
     * 初始化数据
     *
     * @param progress
     */
    public void loadData(int progress) {
        this.currentPlayProgress = progress;
        controller.setControllerListener(new MediaController.MediaControllerListener() {
            @Override
            public void onPrepared(int videoDuration) {
                allTime = videoDuration;
                seekBar.setVideoLength(allTime, currentPlayProgress);
                resetControlClick();
                llVideoLoader.setVisibility(GONE);
                if (onPlayerChangeListener != null) {
                    onPlayerChangeListener.onPrepared();
                }
            }

            @Override
            public void onComplete() {
                seekBar.setPlayBackground(R.drawable.ic_scp_play_nor);
                imgPlayOrPause.setVisibility(VISIBLE);
                currentPlayProgress = 0;
                seekBar.setProgress(currentPlayProgress);
                controller.seekTo(currentPlayProgress);
                if (onPlayerChangeListener != null) {
                    onPlayerChangeListener.onComplete();
                }
            }

            @Override
            public void onPlay() {
                setPlayUI();
                controller.seekTo(currentPlayProgress);
            }

            @Override
            public void onPause() {
                setPauseUI();
            }

            @Override
            public void onError() {
                onPlayerChangeListener.onError();
            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onBufferingUpdate(int percent) {
                checkVideoBuffer(percent, currentPlayProgress, allTime);
            }

            @Override
            public void onPlayingProgress(int progress) {
                currentPlayProgress = progress;
                seekBar.setProgress(currentPlayProgress);
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                videoWidth = width;
                videoHeight = height;
                initSurfaceLayout(width, height);
                surfaceView.requestLayout();
            }
        });
    }

    /**
     * 在 mediaplayer没有prepare完成之前，禁止用户点击操作
     */
    private void blockControlClick() {
        imgPlayOrPause.setEnabled(false);
        seekBar.setCompoundEnabled(false);
    }

    /**
     * mediaplayer prepare完成之后，恢复控件操作
     */
    private void resetControlClick() {
        imgPlayOrPause.setEnabled(true);
        seekBar.setCompoundEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgPlayOrPause) {
            onPlayClick();
        }
    }

    /**
     * 设置是否自动播放，默认手动
     */
    public void setIsAutoPlay(boolean isAuto) {
        controller.setAutoPlay(isAuto);
    }

    /**
     * set video res
     *
     * @param source
     */
    public void setDataSource(final String source) {
        if (IsUtils.isNullOrEmpty(source)) {
            return;
        }
        blockControlClick();
        controller.setDataSource(source);
    }

    public void play() {
        controller.play();
        setPlayUI();
    }

    private void setPlayUI() {
        seekBar.setPlayBackground(R.drawable.ic_scp_pause_nor);
        imgPlayOrPause.setVisibility(GONE);
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onPlay();
        }
    }

    public void pause() {
        controller.pause();
        setPauseUI();
    }

    private void setPauseUI() {
        seekBar.setPlayBackground(R.drawable.ic_scp_play_nor);
        imgPlayOrPause.setVisibility(VISIBLE);
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onPause();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        controller.setHolder(holder);
        controller.seekTo(currentPlayProgress);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Logger.print(TAG, "surfaceChanged width = " + width + " height = " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 动态改变surface的宽高，防止被拉伸
     *
     * @param videoWidth
     * @param videoHeiht
     */
    private void initSurfaceLayout(int videoWidth, int videoHeiht) {
        int screenWidth = DensityUtil.getScreenWidth(getContext());
        int screenHeight = DensityUtil.getScreenHeight(getContext());
        float videoRatio = videoHeiht / (float) videoWidth;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        float wRatio;
        float hRatio;
        float ratio;
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenWidth += DensityUtil.getNavigationBarHeight(getContext());
            //Logger.print(TAG, "initSurfaceLayout -- ORIENTATION_LANDSCAPE");
            if (videoWidth > screenWidth || videoHeiht > screenHeight) {
                wRatio = videoWidth / (float) screenWidth;
                hRatio = videoHeiht / (float) screenHeight;
                ratio = Math.max(wRatio, hRatio);
                videoWidth = (int) Math.ceil(videoWidth / ratio);
                videoHeiht = (int) Math.ceil(videoHeiht / ratio);
                params.height = videoHeiht;
                params.width = videoWidth;
            } else {
                params.height = screenHeight;
                params.width = (int) (screenHeight * (videoWidth / (float) videoHeiht));
            }
        } else {
            //Logger.print(TAG, "initSurfaceLayout -- ORIENTATION_PORT");
            if (videoWidth > screenWidth || videoHeiht > screenWidth * 0.75f) {
                wRatio = videoWidth / (float) screenWidth;
                hRatio = videoHeiht / (screenWidth * 0.75f);
                ratio = Math.max(wRatio, hRatio);
                videoWidth = (int) Math.ceil(videoWidth / ratio);
                videoHeiht = (int) Math.ceil(videoHeiht / ratio);
                params.height = videoHeiht;
                params.width = videoWidth;
            } else {
                params.width = screenWidth;
                params.height = (int) (screenWidth * videoRatio);
            }
        }
    }

    @Override
    public void onSeekBarChanged(int progress) {
        controller.seekTo(progress);
        seekBar.setProgress(progress);
        currentPlayProgress = progress;
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onSeek(progress);
        }
    }

    /**
     * 获取当前播放时间进度，当播放结束后，将直接返回视频的总时间而不采取getCurrentPosition方式，因为
     * 该方式在这种情况下 获取的数据不准确
     */
    public int getCurrentPlayProgress() {
        return currentPlayProgress;
    }

    @Override
    public void onPlayClick() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void onScaleClick() {
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onScale();
        }
    }

    public void stop() {
        controller.stop();
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onStop();
        }
    }

    public void destroy() {
        controller.destory();
        if (onPlayerChangeListener != null) {
            onPlayerChangeListener.onDestory();
        }
    }

    public void setOnPlayerChangeListener(OnPlayerChangeListener sreenChange) {
        onPlayerChangeListener = sreenChange;
    }

    /**
     * 横竖屏切换，恢复上次状态
     *
     * @param currentPlayProgress 上次播放的进度
     */
    public void setPlayProgress(int currentPlayProgress) {
        this.currentPlayProgress = currentPlayProgress;
        //Logger.print(TAG, "setPlayProgress currentPlayProgress = " + currentPlayProgress + " allTime = " + allTime);
    }

    /**
     * 判断当前是否处于播放状态
     */
    public boolean isPlaying() {
        return controller.isPlaying();
    }

    /**
     * 隐藏或显示播放进度控制栏
     */
    public void hideOrShowControlBar() {
        if (seekBar.getVisibility() == View.INVISIBLE) {
            seekBar.setVisibility(VISIBLE);
        } else {
            seekBar.setVisibility(INVISIBLE);
        }
    }

    /**
     * 获取播放器样式
     */
    public int getStyle() {
        return style;
    }

    /**
     * 横竖屏切换 调整布局
     */
    public void notifyScreenChange() {
        if (videoWidth != 0 && videoHeight != 0) {
            initSurfaceLayout(videoWidth, videoHeight);
            surfaceView.requestLayout();
        }
    }

    /**
     * 监听播放器控制改变
     */
    public interface OnPlayerChangeListener {
        void onPrepared();

        void onPlay();

        void onPause();

        void onStop();

        void onComplete();

        void onError();

        void onScale();

        void onSeek(int progress);

        void onDestory();
    }

    /**
     * 检测视频缓冲流
     */
    private void checkVideoBuffer(int bufferPercent, int currentPlayProgress, int allTime) {
        if (allTime == 0) {
            return;
        }
        int cp = (int) ((currentPlayProgress / (float) allTime) * 100);
        if (cp > 100) cp = 100;
        int currentLoadPercent = cp;
        //Logger.print(TAG, "VIDEO_BUFFER_PERCENT : " + bufferPercent + " current_progress = " + currentPlayProgress + " allTime = " + allTime + " currentLoadPercent = " + currentLoadPercent);
        if (currentLoadPercent > bufferPercent) {
            //Logger.print(TAG, "checkVideoBuffer - PAUSE");
            llVideoLoader.setVisibility(VISIBLE);
        } else {
            //Logger.print(TAG, "checkVideoBuffer - PLAY");
            llVideoLoader.setVisibility(GONE);
        }
    }
}
