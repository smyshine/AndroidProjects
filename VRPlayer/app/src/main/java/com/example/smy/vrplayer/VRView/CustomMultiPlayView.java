package com.example.smy.vrplayer.VRView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.smy.vrplayer.R;
import com.example.smy.vrplayer.VRListener.VRPlayListener;
import com.example.smy.vrplayer.common.VideoSeekBar;

/**
 * Created by SMY on 2016/7/13.
 */
public class CustomMultiPlayView extends FrameLayout implements VRPlayListener, View.OnClickListener,
        VideoSeekBar.OnSeekBarChangedListener, CustomMultiCardboardView.OnCardboardViewClickListener {

    private CustomMultiCardboardView mSurfaceView;
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

    private boolean showControllerLayout = true;

    private View view;

    public CustomMultiPlayView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CustomMultiPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(getContext()).inflate(R.layout.widget_multi_player, null);
        mSurfaceView = (CustomMultiCardboardView) view.findViewById(R.id.surfaceView);
        mSurfaceView.setRestoreGLStateEnabled(false);
        mSurfaceView.setSampleCount(2);
        mSurfaceView.setTransparent(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomVRPlayerView);
        boolean useSensor = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useSensor, true);
        boolean useVRMode = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useVRMode, true);
        showControllerLayout = typedArray.getBoolean(R.styleable.CustomVRPlayerView_displayController, true);

        mSurfaceView.initRender();
        mSurfaceView.setUseSensor(useSensor);
        mSurfaceView.setVRModeEnabled(useVRMode);
        //mSurfaceView.setSettingsButtonEnabled(true);眼睛中间线条下方设置按钮，reset cardboard的
        //mSurfaceView.setSampleCount(2);

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
            mSeekBar.setBackgroundColor(getResources().getColor(R.color.transparent));
            mSeekBar.setOnSeekBarChangedListener(this);
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
                    ((Activity) getContext()).finish();
                   /* if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isVideo) {
                        ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        ((Activity) getContext()).finish();
                    }*/
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

    public Bitmap convertLayoutToBitmap(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.setting_layout, null);
        //View view = inflater.inflate(R.layout.picture_setting_float, null);
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Log.d("SDBG","get measure : " + width + " ;  " + height);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, width, height);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public void setDataSource(String url)
    {
        mSurfaceView.setDataSource(url);
        mSurfaceView.setDataSource(convertLayoutToBitmap());
        mSurfaceView.setDataSource(R.id.ll_setting);
    }

    private Handler handler = new Handler();

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
    /*    if(getContext() instanceof Activity)
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
        }*/
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
