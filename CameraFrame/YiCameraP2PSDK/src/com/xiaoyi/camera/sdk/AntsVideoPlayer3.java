package com.xiaoyi.camera.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.decoder.util.H264Decoder;
import com.tutk.IOTC.AVFrame;
import com.xiaomi.fastvideo.AndroidH264DecoderUtil;
import com.xiaomi.fastvideo.HardDecodeExceptionCallback;
import com.xiaomi.fastvideo.PhotoView;
import com.xiaomi.fastvideo.VideoFrame;
import com.xiaomi.fastvideo.VideoGlSurfaceView;
import com.xiaomi.fastvideo.VideoGlSurfaceViewFactory;
import com.xiaomi.fastvideo.VideoGlSurfaceViewGPU;
import com.xiaomi.fastvideo.VideoGlSurfaceViewGPULollipop;
import com.xiaomi.fastvideo.VideoGlSurfaceViewGPULollipop2;
import com.xiaoyi.camera.util.AntsUtil;
import com.xiaoyi.log.AntsLog;

import java.io.FileOutputStream;
import java.util.LinkedList;

public class AntsVideoPlayer3 extends FrameLayout
        implements OnTouchListener, PhotoView.OnScreenWindowChangedListener {


    private static final String TAG = "AntsVideoPlayer3";

    public static final float FULL_SCREEN_MIN_SCALE = 1.0f;
    public static final float NOT_FULL_MIN_SCALE = 0.60f;
    public static final float MAX_SCALE = 4.0f;
    public static final float SURFACE_SCALE = 1.5638f;

    static final int MAX_FRAMEBUF = 1280 * 720 * 3 / 2 + 1000;

    private VideoGlSurfaceView mGlSurface = null;
    // private Renderer mRenderer = null;

    private static final int INVALID_POINTER_ID = -1;

    public LinkedList<AVFrame> videoFrameQueue = new LinkedList<AVFrame>();

    private ThreadDecodeVideo mThreadDecodeVideo;

    private final Object mWaitObject = new Object();

    private final Object OBJECT = new Object();


    protected static final int SURFACE_CREATED = 0;
    protected static final int SURFACE_CHANGED = 1;

    protected static final int NEW_FRAME = 2;

    private static final int SURFACE_SIZE = 3;

    private AVFrame lastAvframe = null;

    private H264Decoder h264dec = null;
    final long AV_NOPTS_VALUE = 0x8000000000000000L;

    private int mSurfaceWidth;

    private int mSurfaceHeight;
    private int screenWidth;
    private int screenHeight;

    private GestureDetector mGestureDetector;

    private int mScaledTouchSlop;

    private ScaleGestureDetector mScaleDetector;

    private boolean isFullScreen = false;
    private boolean isMoveState = true;

    private float surfaceScale = SURFACE_SCALE;

    public void setSurfaceScale(float surfaceScale) {
        this.surfaceScale = surfaceScale;
    }

    private AndroidH264DecoderUtil.DecoderProperties mDecoderProperties;

    public AntsVideoPlayer3(Context context) {
        super(context);
    }

    public AntsVideoPlayer3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AntsVideoPlayer3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private float mScaleFactor = FULL_SCREEN_MIN_SCALE;
    private float mLastFactor = FULL_SCREEN_MIN_SCALE;

    boolean mIsSensorScroll = false;
    boolean hasSeneorScrolled = false;

    float mLastOrientationZ = 0.0f;
    float mLastOrientationY = 0f;
    float mLastOrientationX = 0f;
    private SensorManager mSensorManager = null;
    boolean mSensorEventListenerRegistered = false;
    Interpolator mSensorScrollInterpolator = new LinearInterpolator();
    private ThreadCalculatingRate mThreadCalculatingRate;

    /**
     * 初始化播放器
     * @param context
     * @param useHardDecode
     * @param callback
     */
    public void init(Context context, boolean useHardDecode, HardDecodeExceptionCallback callback) {

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        // 初始化播放器，useHardDecode表示是否使用硬解码
        mGlSurface = VideoGlSurfaceViewFactory.createVideoGlSurfaceView(context, callback, useHardDecode);

        mGestureDetector = new GestureDetector(context, new GestureListener());

        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGlSurface.getScale();
        // configure glsurface
//        mGlSurface.setEGLConfigChooser(new EGLConfigChooser(8, 8, 8, 8, 0, 0));
//        mGlSurface.setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory());
//        mGlSurface.setEGLContextFactory(new EGLContextFactory());
//        mGlSurface.getHolder().setFormat(PixelFormat.OPAQUE);
//        mGlSurface.setEGLContextClientVersion(2);

//        mRenderer = new Renderer();
//        mRenderer.setScale(mScaleFactor);
//        mGlSurface.setRenderer(mRenderer);
//        mGlSurface.setDebugFlags(GLSurface.DEBUG_CHECK_GL_ERROR | GLSurface.DEBUG_LOG_GL_CALLS);
//        mGlSurface.setRenderMode(GLSurface.RENDERMODE_WHEN_DIRTY);

//        mRenderer.setListener(new Renderer.FrameListener() {
//
//            public void onNewFrame() {
//                mGlSurface.requestRender();
//                // mHandler.sendEmptyMessage(NEW_FRAME);
//            }
//
//            @Override
//            public void onSurfaceCreate() {
//                // mHandler.sendEmptyMessage(SURFACE_CREATED);
//            }
//
//            @Override
//            public void onSurfaceChanged() {
//                // mHandler.sendEmptyMessage(SURFACE_CHANGED);
//            }
//        });
        mGlSurface.setOnTouchListener(this);
        mGlSurface.setOnScreenWindowChangedListener(this);
        addView(mGlSurface);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        if (screenHeight < screenWidth) {
            int tmp = screenHeight;
            screenHeight = screenWidth;
            screenWidth = tmp;
        }

        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layOutLandscape();
        } else {
            layOutPortrait();
        }

        this.h264dec = new H264Decoder(H264Decoder.COLOR_FORMAT_YUV420);

        mThreadDecodeVideo = new ThreadDecodeVideo();
        mThreadDecodeVideo.start();

        mThreadCalculatingRate = new ThreadCalculatingRate();
        mThreadCalculatingRate.start();
    }

    public final static int FRAME_FLOW_PATTERN_ASAP = 1;
    public final static int FRAME_FLOW_PATTERN_BUFFER = 5;

    private int mBufferQueueSize = FRAME_FLOW_PATTERN_ASAP;
    public void setFrameFlowPattern(int frameFlowPattern){
        if(frameFlowPattern == FRAME_FLOW_PATTERN_ASAP){
            mBufferQueueSize = FRAME_FLOW_PATTERN_ASAP;
        }else if(frameFlowPattern == FRAME_FLOW_PATTERN_BUFFER){
            mBufferQueueSize = FRAME_FLOW_PATTERN_BUFFER;
        }else{
            // by default
            mBufferQueueSize = FRAME_FLOW_PATTERN_ASAP;
        }
    }

    public void clearView(){
        removeView(mGlSurface);
        mThreadDecodeVideo.stopThread();
        mThreadCalculatingRate.stopThread();
    }

    public void addWartermarkView(int waterMarkresId, int bottomMarginResource, int rightMargin) {
        ImageView ivWartermark = new ImageView(this.getContext());
        ivWartermark.setImageResource(waterMarkresId);
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.RIGHT);

        lp.bottomMargin = this.getResources().getDimensionPixelOffset(
                bottomMarginResource);
        lp.rightMargin = this.getResources()
                .getDimensionPixelOffset(rightMargin);

        addView(ivWartermark, lp);
    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION
                    && (mOrientation == Configuration.ORIENTATION_PORTRAIT)) {
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];

                // 缩放时不触发重力感应
                if (mZooming) {
                    return;
                }

                if (mScaleFactor < 1.0) {
                    return;
                }

                // Log.d(TAG, "orientation x:" + x + " y:" + y + " z:" + z);
                if (y < -60) {
                    if (!mIsSensorScroll) {
                        mIsSensorScroll = true;
                        mLastOrientationX = x;
                        // mOrientationIndicatorView.setVisibility(View.VISIBLE);
                        // mOrientationIndicatorView.startAnimation(AnimationUtils
                        // .loadAnimation(getContext(),
                        // R.anim.ftue_fade_in));
                    }
                    // if (mGuideView != null && mGuideView.isShown()) {
                    // mGuideView.setVisibility(View.GONE);
                    // mGuideView.startAnimation(AnimationUtils.loadAnimation(
                    // getContext(), R.anim.ftue_fade_out));
                    // }
                } else {
                    if (mIsSensorScroll) {
                        mIsSensorScroll = false;
                        // mOrientationIndicatorView.setVisibility(View.INVISIBLE);
                        // mOrientationIndicatorView.startAnimation(AnimationUtils
                        // .loadAnimation(getContext(),
                        // R.anim.ftue_fade_out));
                    }
                }
                if (mIsSensorScroll) {
                    // mLastOrientationZ = z;
                    // int offset = (int) ((mSurfaceWidth - mVideoVisibleWidth)
                    // / 2 - (mSurfaceWidth - mVideoVisibleWidth)
                    // * z * 3.0 / 90);
                    // //moveto(offset, 0, true);

                    //int offx = (int) ((mLastOrientationX - x) * 10 * mScaleFactor);
                    int offx = (int) (mLastOrientationX - x);
                    if (Math.abs(offx) > 0) {
                        if (Math.abs(offx) < 90 && mGlSurface.getPhotoWith() > 0 && mGlSurface.getPhotoHeight() > 0) {
                            //change degree to pixel
                            float degreeToPx =  (mSurfaceHeight*mGlSurface.getPhotoWith())/(mGlSurface.getPhotoHeight()*90);
                            float fTranslateX = (float) offx * mScaleFactor * degreeToPx;
                            // Log.d("movex", mAccTransX + "");
                            mGlSurface.move(fTranslateX, 0, false);
                            mGlSurface.requestRender();
                            hasSeneorScrolled = true;
                        }
                    }
                    mLastOrientationX = x;
                }
            } else {
                if (mOrientation != Configuration.ORIENTATION_PORTRAIT) {
                    // mOrientationIndicatorView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };
    public boolean mZooming = false;

    private void doScale(float x, float y) {
        isMoveState = false;
        mEdge = mScaleFactor - 1.0f;
        mAccTransX = -1 * ((x - getWidth() / 2) / getWidth() * mScaleFactor);
        mAccTransY = -1 * ((y - getHeight() / 2) / getHeight() * mScaleFactor);
        resetXYToEdge();
        // resetRenderParams(true);
        // mAccTransX = -mSurfacediff / 2;
        // mAccTransY = 0.0f;
        mGlSurface.move(mAccTransX, -mAccTransY, false);
        mGlSurface.setScale(mScaleFactor, false);
        mGlSurface.requestRender();

        mLastFactor = mScaleFactor;

        if (onDataRateChangedListener != null){
            Message msg = Message.obtain();
            msg.what = ON_SCREEN_SCALE_CHANGED;
            Bundle bundleData = new Bundle();
            bundleData.putFloat("scale", mLastFactor);
            msg.setData(bundleData);
            mHandler.sendMessage(msg);
        }
    }


    private void doScale(float scale, boolean animal){
        mGlSurface.setScale(scale, animal);
        mGlSurface.requestRender();
        AntsLog.d(TAG,"doScale onDataRateChangedListener="+onDataRateChangedListener);
        if (onDataRateChangedListener != null){
            Message msg = Message.obtain();
            msg.what = ON_SCREEN_SCALE_CHANGED;
            Bundle bundleData = new Bundle();
            bundleData.putFloat("scale", scale);
            msg.setData(bundleData);
            mHandler.sendMessage(msg);
        }

    }

    public void setViewToMini() {
        doScale(mGlSurface.getMiniScale(),true);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getmSurfaceHeight() {
        return mSurfaceHeight;
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mZooming = true;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

//            if (mZooming) {
//                return true;
//            } else {
            float scale = mGlSurface.getScale() * detector.getScaleFactor();
            mScaleFactor = Math.max(mGlSurface.getMiniScale(), Math.min(scale, 4.0F));
            doScale(mScaleFactor, false);
//            }

//            mScaleFactor *= detector.getScaleFactor();
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

//            float px = detector.getFocusX();
//            float py = detector.getFocusY();


//            if (isFullScreen) {
//                // 第一次进全屏时不能再缩小
//                if (mScaleFactor < FULL_SCREEN_MIN_SCALE) {
//                    mScaleFactor = FULL_SCREEN_MIN_SCALE;
//                }
//            } else {
//                if (mScaleFactor < NOT_FULL_MIN_SCALE) {
//                    mScaleFactor = NOT_FULL_MIN_SCALE;
//                }
//            }
//            if (mScaleFactor > MAX_SCALE) {
//                mScaleFactor = MAX_SCALE;
//            }
//
//            // TODO mScaleFactor是缩放倍数，比率变动后做放大缩小操作
//            doScale(px, py);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            // mSurfaceWidth = (int) (screenWidth * mScaleFactor);
            // mSurfaceHeight = mSurfaceWidth * 9 / 16;
            // FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            // mSurfaceWidth, mSurfaceHeight);
            // mGlSurface.setLayoutParams(params);
            // animator.animate(mGlSurface);
        }
    }

    public void showSurfaceView() {
        // if (mGlSurface != null
        // && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
        // // int diff = (int) (mSurfaceWidth * (SCALE - 1) / 2);
        // mGlSurface.layout(0, 0, mSurfaceWidth, mSurfaceHeight);
        // // mGlSurface.layout(0, 0, mGlSurface.getWidth(),
        // // mGlSurface.getHeight());
        // }
    }

    public void hideSurfaceView() {
        // if (mGlSurface != null
        // && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
        // mGlSurface.layout(-1 * mGlSurface.getWidth() * 2, 0, -1
        // * mGlSurface.getWidth(), mGlSurface.getHeight());
        // }
    }

    public void addYUVData(YUVData yuvData) {
        mLastYUVData = yuvData;
//        if (mLastYUVData != null && mRenderer != null) {
//            mRenderer.setFrame(mLastYUVData.yuvbuf, mLastYUVData.width, mLastYUVData.height);
//        }
    }

    private int mlastX;

    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

    private float mLastTouchX;

    private float mLastTouchY;

    private int mActivePointerId = INVALID_POINTER_ID;

    public interface  OnPizJumpListener{
        void jumpToPosition(int x, int y);
    }

    public interface OnMotionClickListener{
        void onMotionClick(View view, MotionEvent event);
    }

    public OnPizJumpListener mOnPizJumpListener;
    public OnMotionClickListener mOnMotionClickListener;

    private static final int MAX_DELAY_FRAME = 100;

    /**
     * 加入需要解码的AVFrame
     *
     * @param avFrame
     */
    public void addAvFrame(AVFrame avFrame) {
        synchronized (OBJECT) {
            // if (videoFrameQueue.size() > MAX_DELAY_FRAME && avFrame.isIFrame()) {
            // clearNonIFrame();
            // }
            videoFrameQueue.add(avFrame);
        }

        synchronized (mWaitObject) {
            mWaitObject.notifyAll();
        }
    }

    /**
     * 保留queue中最后一个I帧以及此I帧之后的所有P帧，删除之前的所有帧
     */
    public void clearBufferUntilLastIFrame() {
        synchronized (OBJECT) {

            int preQueueSize = videoFrameQueue.size();

            boolean isLastIFrame = false;
            for(int i=videoFrameQueue.size()-1; i>=0; i--){
                if(isLastIFrame){
                    videoFrameQueue.remove(i);
                }else if(!isLastIFrame && videoFrameQueue.get(i).isIFrame()){
                    isLastIFrame = true;
                }
            }

            AntsLog.d(TAG, "clearBufferUntilLastIFrame preQueue:" + preQueueSize
                    + ", curQueue:" + videoFrameQueue.size());

            //videoFrameQueue.clear();
        }
    }

//    private void clearNonIFrame() {
//        AntsLog.d(TAG, "clearNonIFrame");
//        videoFrameQueue.clear();
//        /**
//         * for (int i = videoFrameQueue.size() - 1; i > 0; i--) { AVFrame avFrame =
//         * videoFrameQueue.get(i); if (!avFrame.isIFrame()) { videoFrameQueue.remove(i); } }
//         **/
//    }


    public void setOnPizJumpListener(OnPizJumpListener mOnPizJumpListener) {
        this.mOnPizJumpListener = mOnPizJumpListener;
    }

    public void setOnMotionClickListener(OnMotionClickListener mOnMotionClickListener){
        this.mOnMotionClickListener = mOnMotionClickListener;
    }

    private boolean isDoubleTapScaled = false;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mOnMotionClickListener != null){
                mOnMotionClickListener.onMotionClick(AntsVideoPlayer3.this, e);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // mLastFactor = mScaleFactor;
            if(mOnPizJumpListener != null){
                Pair<Integer,Integer> position = calcJumpPosition(e);
                mOnPizJumpListener.jumpToPosition(position.first,position.second);
                return true;
            }
            if (isFullScreen) {
                if (mGlSurface.getScale() > 1.0D) {
                    mScaleFactor = 1.0F;
                } else if (mGlSurface.getScale() < 0.9D) {
                    mScaleFactor = 1.0F;
                } else {
                    mScaleFactor = 2.0F;
                }
            } else {
                if (mGlSurface.getScale() > 1.0F) {
                    mScaleFactor = 1.0F;
                } else if (mGlSurface.getScale() > 0.9F) {
                    mScaleFactor = mGlSurface.getMiniScale();
                }else{
                    mScaleFactor = 1.0F;
                }
            }
            doScale(mScaleFactor, true);
            return true;
        }


    }


    private Pair<Integer,Integer> calcJumpPosition(MotionEvent e){

        float photoRate = mGlSurface.getPhotoWith() * 1.0f/ mGlSurface.getPhotoHeight();
        float realPhotoWidth = mGlSurface.getHeight() * photoRate * mGlSurface.getScale();
        float realPhotoHeight = mGlSurface.getHeight() * 1.0f * mGlSurface.getScale();

        float diffWidth = (realPhotoWidth - mGlSurface.getWidth()) / 2;
        float diffHeight= (realPhotoHeight - mGlSurface.getHeight()) / 2;

//        Log.d("Pos", "realPhotoWidth:"+ realPhotoWidth);
//        Log.d("Pos", "realPhotoHeight:"+ realPhotoHeight);
//
//        Log.d("Pos", "diffWidth:"+ diffWidth);
//        Log.d("Pos", "diffHeight:"+ diffHeight);
//
//
//        Log.d("Pos", "getScale:"+ mGlSurface.getScale());

        float x = e.getX() +  diffWidth - mGlSurface.getOffsetX()/2;
        float y = e.getY() +  mGlSurface.getOffsetY()/2 + diffHeight;

//
//        Log.d("Pos","x:"+x);
//        Log.d("Pos","y:"+y);

        int percentX = (int) (x * 100 / realPhotoWidth);
        int percentY = (int) (y * 100 / realPhotoHeight);

        if(percentY < 0){
            percentY = 0;
        }

        if(percentY > 100){
            percentY = 100;
        }

        AntsLog.d("Pos","percentX:" + percentX + ", percentY:" + percentY);

        return new Pair<>(percentX,percentY);

    }

    /**
     * 结束解码
     */
    public void release() {
        if (mThreadDecodeVideo != null) {
            mThreadDecodeVideo.stopThread();
            // mThreadDecodeVideo.interrupt();
            mThreadDecodeVideo = null;
        }

        if (mThreadCalculatingRate != null) {
            mThreadCalculatingRate.interrupt();
            mThreadCalculatingRate.stopThread();
            mThreadCalculatingRate = null;
        }

//        if (null != mRenderer) {
//            mRenderer.setListener(null);
//        }

//        if (null != mGlSurface) {
//            mGlSurface.destroyDrawingCache();
//
//            mGlSurface.queueEvent(new Runnable() {
//
//                public void run() {
//                    if (null == mRenderer) {
//                        return;
//                    }
//                    mRenderer.setListener(null);
//                    mRenderer.destroy();
//                }
//            });
//        }
    }

    public void resume() {
        if (mGlSurface != null) {
            mGlSurface.onResume();
        }
        if (!mSensorEventListenerRegistered) {
            mSensorEventListenerRegistered = true;
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            mSensorManager.registerListener(mSensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void addBitmap(Bitmap bitmap){
        if (bitmap != null) {
            mGlSurface.setFirstBitmap(bitmap);
        }
    }

    public void pause() {
        if (mGlSurface != null) {
            mGlSurface.onPause();
        }

        if (mSensorEventListenerRegistered) {
            mSensorEventListenerRegistered = false;
            mSensorManager.unregisterListener(mSensorEventListener);
        }
    }

    private YUVData mLastYUVData;
    private float mAccTransX;
    private float mAccTransY;
    private float mEdge;
    private float mSurfacediff;

    public YUVData getLastYUVData() {
        // return null;
        return mLastYUVData;
    }

    public AVFrame getLastAvframe(){
        return lastAvframe;
    }

    private Object LOCK = new Object();

    public interface OnDataRateChangedListener {
        void onDataRateChanged(int dataRate, int bytes);

        void onLongTimeNoDataHappened();

        void onLongTimeDecodeErrorHappened();

        void onFrameBufferIsFull();

        void onScreenScaleChanged(float factor);

        void onScreenWindowChanged(boolean isFinger, int width, int height, int x1, int y1, int x2, int y2);

        void onFrameRateCalculate(int framePerSecond);
    }

    private long lastCalculateTime;
    private long bitcount;

    private OnDataRateChangedListener onDataRateChangedListener;

    public void setOnDataRateChangedListener(OnDataRateChangedListener onDataRateChangedListener) {
        this.onDataRateChangedListener = onDataRateChangedListener;
    }

    private static final int ON_DATA_RATE_CHANGED = 111;
    private static final int ON_LONG_TIME_NO_DATA = 222;
    private static final int ON_LONG_TIME_DECODE_ERROR = 333;
    private static final int ON_FRAME_BUFFER_IS_FULL = 444;
    private static final int ON_SCREEN_SCALE_CHANGED = 555;
    private static final int ON_SCREEN_WINDOW_CHANGED = 666;
    private static final int ON_FRAME_RATE_CALCULATE = 777;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ON_DATA_RATE_CHANGED:
                    if (onDataRateChangedListener != null) {
                        onDataRateChangedListener.onDataRateChanged(msg.arg1, msg.arg2);
                    }
                    break;
                case ON_LONG_TIME_NO_DATA:
                    if (onDataRateChangedListener != null) {
                        onDataRateChangedListener.onLongTimeNoDataHappened();
                    }
                    break;
                case ON_LONG_TIME_DECODE_ERROR:
                    if (onDataRateChangedListener != null) {
                        onDataRateChangedListener.onLongTimeDecodeErrorHappened();
                    }
                    break;
                case ON_FRAME_BUFFER_IS_FULL:
                    if (onDataRateChangedListener != null){
                        onDataRateChangedListener.onFrameBufferIsFull();
                    }
                    break;
                case ON_SCREEN_SCALE_CHANGED:
                    if (onDataRateChangedListener != null){
                        float scale = msg.getData().getFloat("scale", 1L);
                        onDataRateChangedListener.onScreenScaleChanged(scale);
                    }
                    break;
                case ON_SCREEN_WINDOW_CHANGED:
                    if (onDataRateChangedListener != null){
                        boolean isFinger = msg.getData().getBoolean("isFinger");
                        int width = msg.getData().getInt("width");
                        int height = msg.getData().getInt("height");
                        int x1 = msg.getData().getInt("x1");
                        int y1 = msg.getData().getInt("y1");
                        int x2 = msg.getData().getInt("x2");
                        int y2 = msg.getData().getInt("y2");
                        onDataRateChangedListener.onScreenWindowChanged(isFinger, width, height, x1, y1, x2, y2);
                    }
                    break;
                case ON_FRAME_RATE_CALCULATE:
                    if(onDataRateChangedListener != null){
                        int frameRate = msg.getData().getInt("frameRate", 0);
                        onDataRateChangedListener.onFrameRateCalculate(frameRate);
                    }
                    break;
                default:
                    break;
            }
        }

        ;

    };

    private class ThreadCalculatingRate extends Thread {
        // 记录连续多少秒没有数据
        private int second = 0;
        private int dataCount;
        private boolean isRunning;

        @Override
        public void run() {
            isRunning = true;
            while (isRunning) {
                synchronized (LOCK) {
                    long now = System.currentTimeMillis();
                    int duration = (int) (now - lastCalculateTime);
                    int kbps = (int) Math.ceil((bitcount * 1.0 / 1024 * 8 / duration * 1000));
                    int bytes = (int) bitcount;
                    lastCalculateTime = now;
                    bitcount = 0;
                    dataCount += kbps;
                    second += 2;

                    // 5秒没有数据，通知上层处理
                    if (second == 6) {
                        int avgKbps = dataCount / 6;
                        if (avgKbps != 0 && avgKbps < 30) {
                            mHandler.sendEmptyMessage(ON_LONG_TIME_NO_DATA);
                        }
                        second = 0;
                        dataCount = 0;
                    }

                    // Log.d("kbps", kbps + "");
                    Message msg = Message.obtain();
                    msg.what = ON_DATA_RATE_CHANGED;
                    msg.arg1 = kbps;
                    msg.arg2 = bytes;
                    mHandler.sendMessage(msg);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
                // AntsLog.d("ThreadCalculatingRate", System.currentTimeMillis()+"");
            }
        }

        public void stopThread() {
            isRunning = false;
        }
    }

    private class ThreadDecodeVideo extends Thread {

        public boolean isRunning = true;

        private long prets = 0;

        private long firstPlayTime = -1;
        private long lastPlayTime = -1;

        private int preFramePerSecond = 0; // last second, frames per second
        private int curFrameTimestamp = 0; // current second timestamp
        private int curFramePerSecond = 0; // current second, frames per second

        private boolean ctrlFrameFlowRate(long prepareTime, long intervalTime, AVFrame frame) {

            if(firstPlayTime == -1){
                firstPlayTime = System.currentTimeMillis();
            }

            long curts = frame.getTimestamp_ms();


            long waitTime = 0;
            long dTime = 0;

            if(System.currentTimeMillis() - firstPlayTime < 2000){
                // 前两秒钟不进行流控
                waitTime = 0;
            }else{

                dTime = curts - prets;
                if(dTime < 0 && (dTime+1000) > 0){
                    // h21摄像机timestamp_ms只传毫秒数，如果为负需要补1秒钟数来减，数据保护一下
                    dTime = dTime + 1000;
                }

                /**
                 * intervalTime是实际帧率算出来时间差，ms (0, 1000]
                 * dTime是两两相邻帧时间戳的时间差，ms
                 * 取两者之间的合理值
                 * */
                long realIntervalTime = dTime;
                if(dTime < 0 || dTime > 1000 || Math.abs(dTime - intervalTime) > (intervalTime*0.25)){
                    realIntervalTime = intervalTime;
                }

                if(prepareTime > realIntervalTime || videoFrameQueue.size() > 10){
                    waitTime = 0;
                }else{

                    waitTime = realIntervalTime - prepareTime;

                    if(videoFrameQueue.size() < mBufferQueueSize){
                        waitTime += 25;
                    }else if(videoFrameQueue.size() > mBufferQueueSize){
                        waitTime -= 5;
                    }

                    // 保护waitTime计算错误情况下使用[0, 150]范围内的值
                    if(waitTime < 2){
                        waitTime = 0;
                    }
                    if(waitTime > 150){
                        waitTime = 150;
                    }

                }


            }

            prets = curts;


            AntsLog.d(TAG, "bufferSize:" + videoFrameQueue.size()
                    + ", dTime:" + dTime
                    + ", waitTime:" + waitTime
                    + ", prepareTime:" + prepareTime
                    + ", intervalTime:" + intervalTime
                    + ", lastFPS:" + preFramePerSecond
                    + ", curFrame:" + frame.toFrameString());


            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        public void run() {
            while (isRunning) {
                if (videoFrameQueue.size() > 0) {
                    AVFrame avFrame = null;
                    synchronized (OBJECT) {
                        avFrame = videoFrameQueue.poll();
                    }

                    if (avFrame == null) continue;

                    // 计算码率
                    synchronized (LOCK) {
                        bitcount += avFrame.getFrmSize();
                    }

                    if(lastAvframe != null && avFrame.getVideoWidth() != lastAvframe.getVideoWidth()){
                        AntsLog.d(TAG, "[" + lastAvframe.getVideoWidth() + "," + lastAvframe.getVideoHeight()
                                + "]-->"
                                + "[" + avFrame.getVideoWidth() + "," + avFrame.getVideoHeight() + "]"
                                + ", useCount:" + avFrame.useCount
                                + ", frame:" + avFrame.toFrameString());
                        if(!avFrame.isIFrame()){
                            continue;
                        }
                    }

                    mGlSurface.drawVideoFrame(new VideoFrame(avFrame.frmData,
                            avFrame.getFrmNo(), avFrame.getFrmSize(),
                            avFrame.getVideoWidth(), avFrame.getVideoHeight(),
                            avFrame.getTimeStamp(), avFrame.isIFrame()));

                    if(curFrameTimestamp == avFrame.getTimeStamp()){
                        curFramePerSecond++;
                    }else{
                        preFramePerSecond = curFramePerSecond;
                        curFrameTimestamp = avFrame.getTimeStamp();
                        curFramePerSecond = 0;

                        if (onDataRateChangedListener != null){
                            Message msg = Message.obtain();
                            msg.what = ON_FRAME_RATE_CALCULATE;
                            Bundle bundleData = new Bundle();
                            bundleData.putInt("frameRate", preFramePerSecond);
                            msg.setData(bundleData);
                            mHandler.sendMessage(msg);
                        }
                    }

                    AntsLog.d(TAG, "add frame to queue "
                            + avFrame.toFrameString()
                            + ", buffer size:" + videoFrameQueue.size()
                            + ", last FPS:" + preFramePerSecond);

                    // 控制流速
                    long prepareTime = System.currentTimeMillis() - lastPlayTime;
                    long intervalTime = (preFramePerSecond==0) ? 1000 : (1000/preFramePerSecond);
                    ctrlFrameFlowRate(prepareTime, intervalTime, avFrame);


                    lastPlayTime = System.currentTimeMillis();


                    if (avFrame.isIFrame() && avFrame.frmData.length > 36) {
                        lastAvframe = avFrame;
                    }

                    // 如果buffer里面累计了太多frame，清除buffer里面的frame
                    if(videoFrameQueue.size() > MAX_DELAY_FRAME){
                        AntsLog.d(TAG, "frame buffer is full");
                        mHandler.sendEmptyMessage(ON_FRAME_BUFFER_IS_FULL);
                        clearBufferUntilLastIFrame();
                    }

                } else {
                    if (mWaitObject != null) {
                        synchronized (mWaitObject) {
                            try {
                                mWaitObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            if (h264dec != null) {
                h264dec.nativeDestroy();
                h264dec = null;
            }
        }

        public void stopThread() {
            isRunning = false;
            synchronized (mWaitObject) {
                mWaitObject.notifyAll();
            }
        }
    }

    private void resetXYToEdge() {
        if (isMoveState) {
            if (mAccTransX > mEdge) {
                mAccTransX = mEdge;
            }
            if (mAccTransX < -(mEdge + mSurfacediff)) {
                mAccTransX = -(mEdge + mSurfacediff);
            }
            if (mAccTransY > mEdge) {
                mAccTransY = mEdge;
            }
            if (mAccTransY < -mEdge) {
                mAccTransY = -mEdge;
            }
        } else {
            mAccTransX = -mSurfacediff / 2;
            mAccTransY = 0.0f;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        mGestureDetector.onTouchEvent(ev);
        mScaleDetector.onTouchEvent(ev);

        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            /**
             * layout(l,t,r,b) l Left position, relative to parent t Top position, relative to parent r
             * Right position, relative to parent b Bottom position, relative to parent
             * */
            case MotionEvent.ACTION_MOVE: {
                isMoveState = true;
                if (!mZooming) {
//                    if (mActivePointerId == INVALID_POINTER_ID) {
//                        return true;
//                    }
                    final float x = ev.getX();
                    final float y = ev.getY();
                    // if (mOrientation != Configuration.ORIENTATION_LANDSCAPE)
                    // {
                    float dx = (x - mLastTouchX);
                    float dy = -(y - mLastTouchY);

                    mGlSurface.move(dx, dy, true);
                    mGlSurface.requestRender();
                    mLastTouchX = x;
                    mLastTouchY = y;


                    // Log.d("dxdy", "dx :" + dx + "dy :" + dy);

//                    if (Math.abs(dx) > mScaledTouchSlop / 10 || Math.abs(dy) > mScaledTouchSlop / 10) {
//                        float fTranslateX = (float) dx * mScaleFactor / (float) (getWidth());
//                        float fTranslateY = (float) dy * mScaleFactor / (float) (getHeight());
//                        mAccTransX += fTranslateX;
//                        mAccTransY += fTranslateY;
//                        resetXYToEdge();
//                        if (mScaleFactor < FULL_SCREEN_MIN_SCALE) {
//                            mAccTransY = 0.0f;
//                        }
                    // Log.d("mEdge", "mEdge :" + mEdge + "mAccTransX :" +
                    // mAccTransX
                    // + "mAccTransY :" + mAccTransY);

                    // Log.d("mEdge", "mEdge1 :" + mEdge + "mAccTransX :"
                    // + mAccTransX + "mAccTransY :" + mAccTransY);

                    //mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);
                    //mRenderer.refresh();

//                        mLastTouchX = x;
//                        mLastTouchY = y;
//                    }
                    return true;
                }
            }
            // }

            break;

            case MotionEvent.ACTION_UP: {
                mZooming = false;
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mZooming = false;
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                mZooming = false;
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public void onScreenWindowChanged(boolean isFinger, int width, int height, int x1, int y1, int x2, int y2) {

        if (onDataRateChangedListener != null){
            Message msg = Message.obtain();
            msg.what = ON_SCREEN_WINDOW_CHANGED;
            Bundle bundleData = new Bundle();
            bundleData.putBoolean("isFinger", isFinger);
            bundleData.putInt("width", width);
            bundleData.putInt("height", height);
            bundleData.putInt("x1", x1);
            bundleData.putInt("y1", y1);
            bundleData.putInt("x2", x2);
            bundleData.putInt("y2", y2);
            msg.setData(bundleData);
            mHandler.sendMessage(msg);
        }

    }

    // 横屏
    public void layOutLandscape() {
        isFullScreen = true;
        mSurfacediff = 0.0f;
        mSurfaceWidth = screenHeight;
        mSurfaceHeight = mSurfaceWidth * 9 / 16;
        LayoutParams params = new LayoutParams(mSurfaceWidth,
                mSurfaceHeight);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mGlSurface.setLayoutParams(params);
        // resetRenderParams(false);
        mOrientation = Configuration.ORIENTATION_LANDSCAPE;
    }

    // 纵向
    public void layOutPortrait() {
        isFullScreen = false;
        mSurfaceWidth = (int) (screenWidth);
        mSurfaceHeight = (int) (mSurfaceWidth * 9 / 16 * surfaceScale);

//        mSurfaceWidth = screenWidth;
//        mSurfaceHeight = mSurfaceHeight /2;
        LayoutParams params = new LayoutParams(mSurfaceWidth,
                mSurfaceHeight);
        //params.gravity = Gravity.CENTER;
        mGlSurface.setLayoutParams(params);
        //resetRenderParams(true);
        mOrientation = Configuration.ORIENTATION_PORTRAIT;
    }

    private void resetRenderParams(boolean isCenter) {
        if (isCenter) {
            mAccTransX = -mSurfacediff / 2;
        } else {
            mAccTransX = 0.0f;
        }
        mAccTransY = 0.0f;
        mScaleFactor = 1.0f;
        mEdge = mScaleFactor - 1.0f;
        mGlSurface.setScale(mScaleFactor, false);
        //mGlSurface.move(mAccTransX, -mAccTransY);
        //mRenderer.setScale(mScaleFactor);
        //mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void capture(String fileName) {
        if (mLastYUVData == null) {
            return;
        }
        YuvImage yuvImage = new YuvImage(AntsUtil.yuv420pToyuv420sp(mLastYUVData.yuvbuf,
                mLastYUVData.width, mLastYUVData.height), ImageFormat.NV21, mLastYUVData.width,
                mLastYUVData.height, null);
        FileOutputStream bos = null;
        try {
            bos = new FileOutputStream(fileName);
            yuvImage.compressToJpeg(new Rect(0, 0, mLastYUVData.width, mLastYUVData.height), 100,
                    bos);
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void snap(PhotoView.PhotoSnapCallback callback) {
        if(lastAvframe == null){
            callback.onSnap(null);
        }else {
            mGlSurface.snap(callback);
        }
    }

    public boolean isHardDecodPlaying() {
        return mGlSurface instanceof VideoGlSurfaceViewGPU
                || mGlSurface instanceof VideoGlSurfaceViewGPULollipop
                || mGlSurface instanceof VideoGlSurfaceViewGPULollipop2;
    }

    public boolean getHasSensorScrolled(){
        return hasSeneorScrolled;
    }

    
}
