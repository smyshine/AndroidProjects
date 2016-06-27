package com.example.smy.vrplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by SMY on 2016/6/22.
 */
public class CustomVRPlayerView extends FrameLayout implements VRPlayListener, View.OnClickListener,
        VideoSeekBar.OnSeekBarChangedListener, CustomCardboardView.OnCardboardViewClickListener{

    //public static final String url = "rtsp://192.168.42.1/live";

    private CustomCardboardView mSurfaceView;
    private VideoSeekBar mSeekBar;
    private LinearLayout mLoadLayout;
    private RelativeLayout mTopBar;
    private RelativeLayout mControllerBar;
    private ImageButton vrButton;
    private ImageButton vrFormatButton;
    private ImageButton backButton;

    private int mVideoLength;
    private int mCurrentPlayTime;

    private final int HIDDEN_BAR_TIME = 5000;

    private final int DOWNLOAD_PICTURE_SUCCESS = 0;
    private final int DOWNLOAD_PICTURE_FAILED = 1;

    private Bitmap mPicture;



    private boolean isVideo = true;
    private boolean showControllerLayout = true;

    public CustomVRPlayerView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CustomVRPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_vr_player, null);
        mSurfaceView = (CustomCardboardView) view.findViewById(R.id.surfaceView);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomVRPlayerView);
        isVideo = typedArray.getBoolean(R.styleable.CustomVRPlayerView_videoType, true);
        boolean useSensor = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useSensor, true);
        boolean useVRMode = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useVRMode, true);
        showControllerLayout = typedArray.getBoolean(R.styleable.CustomVRPlayerView_displayController, true);
        mSurfaceView.initRender(isVideo);
        mSurfaceView.setUseSensor(useSensor);
        mSurfaceView.setVRModeEnabled(useVRMode);

        typedArray.recycle();
        mSeekBar = (VideoSeekBar) view.findViewById(R.id.seekBar);

        mLoadLayout = (LinearLayout)view.findViewById(R.id.ll_loadVideo);
        mTopBar = (RelativeLayout)view.findViewById(R.id.top_bar);
        mControllerBar = (RelativeLayout)view.findViewById(R.id.player_controller);

        backButton = (ImageButton) view.findViewById(R.id.backBtn);
        backButton.setOnClickListener(this);
        vrButton = (ImageButton) view.findViewById(R.id.vrBtn);
        vrButton.setOnClickListener(this);
        vrFormatButton = (ImageButton) view.findViewById(R.id.vrFormatBtn);
        vrFormatButton.setOnClickListener(this);

        if(!showControllerLayout){
            mControllerBar.setVisibility(View.INVISIBLE);
        } else {
            if (isVideo) {
                mSeekBar.setBackgroundColor(getResources().getColor(R.color.transparent));
                mSeekBar.setOnSeekBarChangedListener(this);
            } else {
                mSeekBar.setVisibility(View.INVISIBLE);
                updateVRModeAndSensorButton(true);
            }
        }

        mSurfaceView.setRenderListener(this);
        mSurfaceView.setCardboardViewClickListener(this);
        view.findViewById(R.id.llResetOrientation).setOnClickListener(this);
        addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.backBtn:
                if (getContext() instanceof Activity) {
                    //if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isVideo) {
                    //    ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    //} else {
                        ((Activity) getContext()).finish();
                    //}
                }
                break;
            case R.id.llResetOrientation:
                resetOrientation();
                break;
            case R.id.vrBtn:
                if (mSurfaceView.getVRMode())
                {
                    vrButton.setImageResource(R.drawable.vr_screen_off);
                    mSurfaceView.setVRModeEnabled(false);
                }
                else
                {
                    vrButton.setImageResource(R.drawable.vr_screen_on);
                    mSurfaceView.setVRModeEnabled(true);
                }
                break;
            case R.id.vrFormatBtn:
                mSurfaceView.changeWatchType();
                if(mSurfaceView.isUsingSensor())
                {
                    vrFormatButton.setImageResource(R.drawable.vr_sensor_open);
                }
                else
                {
                    vrFormatButton.setImageResource(R.drawable.vr_sensor_close);
                }
                break;
        }
    }

    public void setDataSource(String url)
    {
        if(isVideo) {
            mSurfaceView.setDataSource(url);
        } else {
            //loadBitmap(url);
        }
    }

    public void setDataSource(Bitmap bitmap)
    {
        mSurfaceView.setDataSource(bitmap);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PICTURE_SUCCESS:
                    mLoadLayout.setVisibility(View.GONE);
                    mSurfaceView.setDataSource(mPicture);
                    break;
                case DOWNLOAD_PICTURE_FAILED:
                    mLoadLayout.setVisibility(View.GONE);
                    //Toast.makeText(getContext(), R.string.pic_edit_filter_load_sticker_failed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*private void loadBitmap(final String url){
        new Thread() {
            @Override
            public void run() {
                //mPicture = Bitmap.createBitmap(YiImageLoader.syncLoadYiBitmap(getContext(), url));
                if (mPicture == null) {
                    handler.sendEmptyMessage(DOWNLOAD_PICTURE_FAILED);
                    return;
                }

                handler.sendEmptyMessage(DOWNLOAD_PICTURE_SUCCESS);
            }
        }.start();
    }*/


    private Runnable hideBarLayout = new Runnable() {
        @Override
        public void run() {
            mControllerBar.setVisibility(View.INVISIBLE);
        }
    };

    public void resetOrientation()
    {
        mSurfaceView.resetCameraOrientation();
    }


    public void handlePlayRestart()
    {
        mSeekBar.setPlayBackground(R.drawable.ic_scp_pause_nor);
        mSurfaceView.startPlay();
        hideControllerBar();
    }

    public void handlePlayPause()
    {
        mSeekBar.setPlayBackground(R.drawable.ic_scp_play_nor);
        handler.removeCallbacks(hideBarLayout);
        mSurfaceView.pausePlay();
    }

    @Override
    public void onSeekBarChanged(int progress)
    {
        mSeekBar.setProgress(progress);
        mSurfaceView.changeMediaProgress(progress);
    }

    @Override
    public void onPlayClick()
    {
        if (mSurfaceView.isPlaying())
        {
            handlePlayPause();
        }
        else
        {
            handlePlayRestart();
        }
    }

    @Override
    public void onScaleClick()
    {
        if(getContext() instanceof Activity)
        {
            Activity activity = (Activity)getContext();
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else
            {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    public void onVideoInit(int length)
    {
        mVideoLength = length;
        mCurrentPlayTime = 0;
        mSeekBar.setVideoLength(length, 0);
    }

    @Override
    public void listenTime(int time)
    {
        mCurrentPlayTime = time;
        post(new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(mCurrentPlayTime);
            }
        });
    }

    @Override
    public void onVideoStartPlay()
    {
        hideControllerBar();
        mSeekBar.setPlayBackground(R.drawable.ic_scp_pause_nor);
        mLoadLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onErrorPlaying(int errorType, int errorID)
    {
        Toast.makeText(getContext(), "play video failed.type = " + errorType + " errorID = " + errorID, Toast.LENGTH_SHORT).show();
        mCurrentPlayTime = 0;
    }

    @Override
    public void onBufferingUpdate(int percent)
    {
        if (percent == 100) {
            mLoadLayout.setVisibility(View.INVISIBLE);
            return;
        }
        int currentBufferTime = mVideoLength * percent / 100;
        mSeekBar.setSecondaryProgress(currentBufferTime);
        if(mCurrentPlayTime < currentBufferTime)
        {
            mLoadLayout.setVisibility(View.INVISIBLE);
        }
        else
        {
            mLoadLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateVRModeAndSensorButton(boolean visible){
        if(visible){
            vrButton.setVisibility(VISIBLE);
            vrFormatButton.setVisibility(VISIBLE);
            if (mSurfaceView.getVRMode()) {
                vrButton.setImageResource(R.drawable.vr_screen_on);
            }
            else {
                vrButton.setImageResource(R.drawable.vr_screen_off);
            }
            if(mSurfaceView.isUsingSensor()) {
                vrFormatButton.setImageResource(R.drawable.vr_sensor_open);
            }
            else {
                vrFormatButton.setImageResource(R.drawable.vr_sensor_close);
            }
        } else {
            vrButton.setVisibility(GONE);
            vrFormatButton.setVisibility(GONE);
        }
    }
    public void onScreenChangeOrientation(boolean landScreen)
    {
        if(!showControllerLayout){
            return;
        }
        mControllerBar.setVisibility(View.VISIBLE);
        if(landScreen)
        {
            updateVRModeAndSensorButton(true);
            mSeekBar.setScaleBackground(getResources().getDrawable(R.drawable.ic_scale_close_all_screen));
            hideControllerBar();
        }
        else
        {
            vrButton.setVisibility(GONE);
            vrFormatButton.setVisibility(GONE);
            mSeekBar.setScaleBackground(getResources().getDrawable(R.drawable.ic_scale_open_all_screen));
            handler.removeCallbacks(hideBarLayout);
        }

        if(mSurfaceView.getVRMode())
        {
            mSurfaceView.setVRModeEnabled(false);
        }
        vrButton.setImageResource(R.drawable.vr_screen_off);

        if(mSurfaceView.isUsingSensor())
        {
            mSurfaceView.changeWatchType();
        }
        vrFormatButton.setImageResource(R.drawable.vr_sensor_close);
    }

    @Override
    public void onCardboardViewOnClicked()
    {
        if(showControllerLayout) {
            if (mControllerBar.getVisibility() == VISIBLE) {
                mControllerBar.setVisibility(GONE);
                handler.removeCallbacks(hideBarLayout);
            } else {
                mControllerBar.setVisibility(VISIBLE);
                hideControllerBar();
            }
        }
    }

    private void hideControllerBar()
    {
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            return;
        }
        handler.postDelayed(hideBarLayout,HIDDEN_BAR_TIME);
    }
}
