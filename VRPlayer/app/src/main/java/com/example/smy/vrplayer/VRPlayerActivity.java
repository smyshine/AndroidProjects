package com.example.smy.vrplayer;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.rajawali3d.vr.VRActivity;


public class VRPlayerActivity extends VRActivity implements VRPlayListener, View.OnClickListener, VideoSeekBar.OnSeekBarChangedListener {

    public static final String VIDEO_PATH = "vrvideopath";

    enum eVrFormat {
        VRFORMAT_2D,
        VRFORMAT_3D_LEFT_RIGHT,
        VRFORMAT_3D_UP_DOWN,
    };
    private VRVideoRender mRenderer;
    private View barLayout;
    private ImageButton vrButton, vrFormatButton;
    private ImageButton backButton;
    private LinearLayout mLoadLayout;

    private final static String TAG = "VideoActivity";
    private final int HIDDEN_BAR_TIME = 4000;

    private Handler handler = new Handler();;

    private String url;

    private int mVideoLength;
    private int mCurrentPlayTime;

    private VideoSeekBar mSeekBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getSurfaceView().setSettingsButtonEnabled(false);
        GestureListener gestureListener = new GestureListener();
        gestureListener.setOnEventGesture(mOnEventGesture);
        gestureListener.setToucheMode(GestureListener.TOUCHEMODE.MODE_ZOOM_AND_MOVE);
        getCardboardView().setOnTouchListener(gestureListener);
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mRenderer != null) {
            handlePlayPause();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mRenderer != null) {
            handlePlayRestart();
        }

    }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(hideBarLayout);
        if(mRenderer != null) {
            mRenderer.onDestroy();
        }
    }

    private Runnable hideBarLayout = new Runnable() {
        @Override
        public void run() {
            //barLayout.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onVideoInit(int length) {
        mVideoLength = length;
        mCurrentPlayTime = 0;
        mSeekBar.setVideoLength(length, 0);
    }

    @Override
    public void listenTime(final int time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentPlayTime = time;
                mSeekBar.setProgress(time);
            }
        });
    }

    @Override
    public void onVideoStartPlay() {
        handler.postDelayed(hideBarLayout, HIDDEN_BAR_TIME);
        mLoadLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onErrorPlaying(int errorType, int errorID) {
        Toast.makeText(getBaseContext(), "play video failed.type = " + errorType + " errorID = " + errorID, Toast.LENGTH_SHORT).show();
        mCurrentPlayTime = 0;
    }

    @Override
    public void onBufferingUpdate(int percent) {
        int currentBufferTime = mVideoLength * percent / 100;
        mSeekBar.setSecondaryProgress(currentBufferTime);
        if(mCurrentPlayTime < currentBufferTime){
            mLoadLayout.setVisibility(View.INVISIBLE);
        } else {
            mLoadLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCardboardTrigger() {
        int visibility;
        visibility = barLayout.getVisibility();
        if (visibility != View.VISIBLE) {
            handler.removeCallbacks(hideBarLayout);
            barLayout.setVisibility(View.VISIBLE);
            // handler.postDelayed(hideBarLayout,2000);
        } else {
            barLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void initView() {
        //setContentView(R.layout.activity_vrplayer_view);
        url = getIntent().getStringExtra(VIDEO_PATH);
        mRenderer = new VRVideoRender(this);
        setRenderer(mRenderer);
        mRenderer.setVideoPath(url);

        mRenderer.setVideoPlayListener(this);

        setConvertTapIntoTrigger(true);

        LayoutInflater layoutInflater = getLayoutInflater();
        barLayout = layoutInflater.inflate(R.layout.activity_vrplayer_view, null);//findViewById(R.id.view) ;//
        addContentView(barLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        backButton = (ImageButton) findViewById(R.id.backBtn);
        vrButton = (ImageButton) findViewById(R.id.vrBtn);
        vrFormatButton = (ImageButton) findViewById(R.id.vrFormatBtn);
        mSeekBar = (VideoSeekBar) findViewById(R.id.seekBar);
        mSeekBar.hideScaleBtn();
        mSeekBar.setBackgroundColor(getResources().getColor(R.color.transparent));

        mLoadLayout = (LinearLayout)findViewById(R.id.ll_loadVideo);
        findViewById(R.id.ll_ResetOrientation).setOnClickListener(this);

        backButton.setOnClickListener(this);

        vrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSurfaceView().getVRMode()) {
                    vrButton.setImageResource(R.drawable.vr_screen_off);
                    getSurfaceView().setVRModeEnabled(false);
                } else {
                    vrButton.setImageResource(R.drawable.vr_screen_on);
                    getSurfaceView().setVRModeEnabled(true);
                }
            }
        });

        mSeekBar.setOnSeekBarChangedListener(this);
    }



    @Override
    public void onSeekBarChanged(int progress) {
        mRenderer.setMediaPlayerSeekTo(progress);
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onPlayClick() {
        if (mRenderer.isPlaying()) {
            handlePlayPause();
        } else {
            handlePlayRestart();
        }
    }

    @Override
    public void onScaleClick() {

    }


    private void handlePlayRestart(){
        mSeekBar.setPlayBackground(R.drawable.ic_scp_pause_nor);
        mRenderer.onResume();
        handler.postDelayed(hideBarLayout,HIDDEN_BAR_TIME);
    }

    private void handlePlayPause(){
        mSeekBar.setPlayBackground(R.drawable.ic_scp_play_nor);
        handler.removeCallbacks(hideBarLayout);
        mRenderer.onPause();
    }

    public void resetOrientation() {
        if (null != mRenderer) {
            mRenderer.resetCameraOrientation();
        }
    }

    public void showFormatPopup(View v) {
        mRenderer.changeWatchType();
        if(mRenderer.mUseSensor){
            vrFormatButton.setImageResource(R.drawable.vr_sensor_open);
        } else {
            vrFormatButton.setImageResource(R.drawable.vr_sensor_close);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn)
        {
            onBackPressed();
        }
        else if (view.getId() == R.id.ll_ResetOrientation)
        {
            resetOrientation();
        }
    }

    private GestureListener.OnEventGesture mOnEventGesture = new GestureListener.OnEventGesture() {
        @Override
        public void onZoomGesture(View v, float newDist, float oldDist) {
            if(!getSurfaceView().getVRMode()){
                mRenderer.onZoomGesture(newDist,oldDist);
            }
        }

        @Override
        public void onMoveGesture(View v, MotionEvent currentMoveEvent, float mTouchStartX, float mTouchStartY) {
            if(!getSurfaceView().getVRMode()){
                mRenderer.addGestureRotateAngle(
                        ScreenUtils.rawY2Angle(VRPlayerActivity.this, currentMoveEvent.getRawY() - mTouchStartY),
                        ScreenUtils.rawX2Angle(VRPlayerActivity.this, currentMoveEvent.getRawX() - mTouchStartX)
                );
            }
        }

        @Override
        public void onClick(View v, MotionEvent currentMoveEvent) {
            onCardboardTrigger();
        }

        @Override
        public void onLeftRightGesture(View v, MotionEvent currentEventm, float startx, float starty, GestureListener.GestureState state) {

        }

        @Override
        public void onUpDownLeftGesture(View v, MotionEvent currentEventm, float startx, float starty, GestureListener.GestureState state) {

        }

        @Override
        public void onUpDownRightGesture(View v, MotionEvent currentEventm, float startx, float starty, GestureListener.GestureState state) {

        }
    };
}
