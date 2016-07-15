package com.example.smy.vrplayer.VRView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by SMY on 2016/7/13.
 */
public class CustomMultiPlayView extends FrameLayout implements VRPlayListener, View.OnClickListener,
        VideoSeekBar.OnSeekBarChangedListener, CustomCardboardView.OnCardboardViewClickListener {

    private CustomCardboardView mSurfaceView;
    private VRSettingView mSettingView;
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

    public CustomMultiPlayView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CustomMultiPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_multi_player, null);
        mSurfaceView = (CustomCardboardView) view.findViewById(R.id.surfaceView);
        mSettingView = (VRSettingView) view.findViewById(R.id.settingView);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomVRPlayerView);
        boolean useSensor = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useSensor, true);
        boolean useVRMode = typedArray.getBoolean(R.styleable.CustomVRPlayerView_useVRMode, true);
        showControllerLayout = typedArray.getBoolean(R.styleable.CustomVRPlayerView_displayController, true);

        mSurfaceView.initRender(true);
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
        mSettingView.getSurfaceView().setCardboardViewClickListener(this);
        mSettingView.setVisibility(VISIBLE);
        //mSettingView.getSurfaceView().setSettingsButtonEnabled(true);

        view.findViewById(R.id.llResetOrientation).setOnClickListener(this);
        addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

/*        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        mSettingView.setSystemUiVisibility(uiFlags);*/
    }

    public VRSettingView getSettingView(){
        return mSettingView;
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
                    mSettingView.getSurfaceView().setVRModeEnabled(false);
                }
                else
                {
                    vrButton.setImageResource(R.drawable.vr_screen_on);
                    mSurfaceView.setVRModeEnabled(true);
                    mSettingView.getSurfaceView().setVRModeEnabled(true);
                }
                break;
            case R.id.vrFormatBtn:
                mSurfaceView.changeWatchType();
                mSettingView.getSurfaceView().changeWatchType();
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
        View view = inflater.inflate(R.layout.picture_setting_float, null);
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

        //modify bitmap color
        /*int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int baseColor = bitmap.getPixel(0, 0);

        for(int i = 0; i < bitmapHeight; ++i){
            for (int j = 0; j < bitmapWidth; ++j){
                if (baseColor == bitmap.getPixel(j, i)){
                    bitmap.setPixel(j, i, getResources().getColor(R.color.black_30_percent));
                }
            }
        }*/

/*        Bitmap newBitmap=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);

        // 建立Paint
        Paint vPaint = new Paint();
        vPaint.setStyle( Paint.Style.FILL );
        vPaint.setAlpha( 3 );   //

        canvas.drawBitmap( newBitmap , 0, 0, vPaint );  //有透明

        return newBitmap;*/

        return bitmap;
    }

    /**
     * 利用ColorMatrix颜色矩阵更改bitmap的每个像素点的颜色
     * 比如将图片的颜色全改为红色
     */

    public Bitmap produceSpecifyColorBitmap(Bitmap oldBitmap,int specifyColor){
        Bitmap newBitmap=Bitmap.createBitmap(oldBitmap.getWidth(),oldBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);
        /**
         *
         * 一个新的齐次颜色矩阵左乘上一个原来的颜色向量矩阵即得到一个新的颜色向量矩阵
         * a1,b1,c1,d1,e1 R R1 R1=a1*R+b1*G+c1*B+d1*A+e1
         * a2,b2,c2,d2,e2 G G1 G1=a2*R+b2*G+c2*B+d2*A+e2
         * a3,b3,c3,d3,e3 * B = B1 -> B1=a3*R+b3*G+c3*B+d3*A+e3
         * a4,b4,c4,d4,e4 A A1 A1=a4*R+b4*G+c4*B+d4*A+e4
         * 1
         *
         * 如果现在我想的到透明度不变的红色图片 那么R1=255,G1=0，B1=0，A1=A
         * 那么颜色矩阵就应该是
         * 0,0,0,0,255
         * 0,0,0,0,0
         * 0,0,0,0,0
         * 0,0,0,1,0
         */
        ColorMatrix colorMatrix=new ColorMatrix(new float[]{0,0,0,0, Color.red(specifyColor),
                0,0,0,0,Color.green(specifyColor),
                0,0,0,0,Color.blue(specifyColor),
                0,0,0,1,0});
        ColorMatrixColorFilter colorMatrixColorFilter=new ColorMatrixColorFilter(colorMatrix);
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(oldBitmap,0,0,paint);

        return newBitmap;
    }

    public void setDataSource(String url)
    {
        mSurfaceView.setDataSource(url);
        //saveBitmapToFile(convertLayoutToBitmap(), "/storage/emulated/0/smy/tmp.png");
        mSettingView.setDataSource(convertLayoutToBitmap());
        //mSettingView.setDataSource(((BitmapDrawable)getResources().getDrawable(R.drawable.vr_reset_camera_orientation)).getBitmap());
    }

    public void saveBitmapToFile(Bitmap bmp, String filename){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler();

    /**
     * @param urlpath
     * @return Bitmap
     * 根据图片url获取图片对象
     */
    public static Bitmap getBitMBitmap(String urlpath) {
        Bitmap map = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * @param urlpath
     * @return Bitmap
     * 根据url获取布局背景的对象
     */
    public static Drawable getDrawable(String urlpath){
        Drawable d = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            d = Drawable.createFromStream(in, "background.jpg");
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Bitmap getBitmapFromFile(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private Runnable hideBarLayout = new Runnable() {
        @Override
        public void run() {
            mControllerBar.setVisibility(View.INVISIBLE);
        }
    };

    public void resetOrientation()
    {
        mSurfaceView.resetCameraOrientation();
        mSettingView.getSurfaceView().resetCameraOrientation();
    }


    public void handlePlayRestart()
    {
        mSeekBar.setPlayBackground(R.drawable.ic_scp_pause_nor);
        mSurfaceView.startPlay();
        //mSettingView.startPlay();
        hideControllerBar();
    }

    public void handlePlayPause()
    {
        mSeekBar.setPlayBackground(R.drawable.ic_scp_play_nor);
        handler.removeCallbacks(hideBarLayout);
        mSurfaceView.pausePlay();
        //mSettingView.pausePlay();
    }

    @Override
    public void onSeekBarChanged(int progress)
    {
        mSeekBar.setProgress(progress);
        mSurfaceView.changeMediaProgress(progress);
        //mSettingView.changeMediaProgress(progress);
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
            mSettingView.getSurfaceView().setVRModeEnabled(false);
        }
        vrButton.setImageResource(R.drawable.vr_screen_off);

        if(mSurfaceView.isUsingSensor())
        {
            mSurfaceView.changeWatchType();
            mSettingView.getSurfaceView().changeWatchType();
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
