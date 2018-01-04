package com.xiaoyi.camera.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tutk.IOTC.AVFrame;
import com.xiaoyi.camera.util.AntsUtil;
import com.xiaoyi.log.AntsLog;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by USER on 2015/7/21.
 */
public class AntsSurfacePlayer extends FrameLayout implements View.OnTouchListener, SurfaceHolder.Callback {

    public static final float FULL_SCREEN_MIN_SCALE = 1.0f;
    public static final float NOT_FULL_MIN_SCALE = 0.60f;
    public static final float MAX_SCALE = 4.0f;
    public static final float SURFACE_SCALE = 1.65f;
    public static final float SURFACE_SCALE_NEW = 1.79f;

    static final int MAX_FRAMEBUF = 1280 * 720 * 3 / 2 + 1000;

    private SurfaceView mSurface = null;

    private SurfaceHolder holder;
//private Renderer mRenderer = null;

    private static final int INVALID_POINTER_ID = -1;

    public LinkedList<AVFrame> videoFrameQueue = new LinkedList<AVFrame>();

    private ThreadDecodeVideo mThreadDecodeVideo;

    private final Object mWaitObject = new Object();

    private final Object OBJECT = new Object();

    private static final String TAG = "AntsVideoDecoder";

    protected static final int SURFACE_CREATED = 0;
    protected static final int SURFACE_CHANGED = 1;

    protected static final int NEW_FRAME = 2;

    private static final int SURFACE_SIZE = 3;

    private AVFrame lastAvframe = null;

    //    private H264Decoder h264dec = null;
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

    private MediaCodec mediaCodec;

    public AntsSurfacePlayer(Context context) {
        super(context);
        init(context);
    }

    public AntsSurfacePlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public AntsSurfacePlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private float mScaleFactor = NOT_FULL_MIN_SCALE;
    private float mLastFactor = NOT_FULL_MIN_SCALE;
    private OnScaleChangeListener mOnScaleChangeListener;

    boolean mIsSensorScroll = false;
    float mLastOrientationZ = 0.0f;
    float mLastOrientationY = 0f;
    float mLastOrientationX = 0f;
    private SensorManager mSensorManager = null;
    boolean mSensorEventListenerRegistered = false;
    Interpolator mSensorScrollInterpolator = new LinearInterpolator();
    private ThreadCalculatingRate mThreadCalculatingRate;

    public void init(Context context) {

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        mSurface = new SurfaceView(context);

        holder = mSurface.getHolder();
        holder.addCallback(this);


        mGestureDetector = new GestureDetector(context, new GestureListener());

        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // configure glsurface
//        mSurface.setEGLConfigChooser(new EGLConfigChooser(8, 8, 8, 8, 0, 0));
//        mSurface.setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory());
//        mSurface.setEGLContextFactory(new EGLContextFactory());
//        mSurface.getHolder().setFormat(PixelFormat.OPAQUE);
//        mSurface.setEGLContextClientVersion(2);
//
//        mRenderer = new Renderer();
//        mRenderer.setScale(mScaleFactor);
//        mSurface.setRenderer(mRenderer);
//        mSurface.setDebugFlags(GLSurface.DEBUG_CHECK_GL_ERROR | GLSurface.DEBUG_LOG_GL_CALLS);
//        mSurface.setRenderMode(GLSurface.RENDERMODE_WHEN_DIRTY);

//        mRenderer.setListener(new Renderer.FrameListener() {
//
//            public void onNewFrame() {
//                mSurface.requestRender();
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
        mSurface.setOnTouchListener(this);
        addView(mSurface);
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

//        this.h264dec = new H264Decoder(H264Decoder.COLOR_FORMAT_YUV420);

        mThreadDecodeVideo = new ThreadDecodeVideo();
        mThreadDecodeVideo.start();

        mThreadCalculatingRate = new ThreadCalculatingRate();
        mThreadCalculatingRate.start();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initCodec(int width, int height) {
        if (mediaCodec == null) {
            try{
                mediaCodec = MediaCodec.createDecoderByType("video/avc");
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
                mediaCodec.configure(mediaFormat, holder.getSurface(), null, 0);
                mediaCodec.start();
            }catch (Exception e){
            }
        }
    }

    public void addWartermarkView(int waterMarkresId, int bottomMarginResource, int rightMargin) {
        ImageView ivWartermark = new ImageView(this.getContext());
        ivWartermark.setImageResource(waterMarkresId);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
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

                // ??????????????????
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

                    int offx = (int) ((mLastOrientationX - x) * 10 * mScaleFactor);
                    if (Math.abs(offx) > 0) {
                        if (Math.abs(offx) < 200) {
                            float fTranslateX = (float) offx * mScaleFactor / (float) (getWidth());
                            mAccTransX += fTranslateX;
                            // Log.d("movex", mAccTransX + "");
                            resetXYToEdge();
//                            mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);
//                            mRenderer.refresh();
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

    public interface OnScaleChangeListener {
        public void onScale(float factor);
    }

    public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
        this.mOnScaleChangeListener = onScaleChangeListener;
    }

    private void doScale(float x, float y) {
        isMoveState = false;
        mEdge = mScaleFactor - 1.0f;
        mAccTransX = -1 * ((x - getWidth() / 2) / getWidth() * mScaleFactor);
        mAccTransY = -1 * ((y - getHeight() / 2) / getHeight() * mScaleFactor);
        resetXYToEdge();
        // resetRenderParams(true);
        // mAccTransX = -mSurfacediff / 2;
        // mAccTransY = 0.0f;
//        mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);
//        mRenderer.setScale(mScaleFactor);
//        mRenderer.refresh();
        mLastFactor = mScaleFactor;

        if (mOnScaleChangeListener != null) {
            mOnScaleChangeListener.onScale(mLastFactor);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mZooming = true;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float px = detector.getFocusX();
            float py = detector.getFocusY();

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            if (isFullScreen) {
                // ????谓?????????????小
                if (mScaleFactor < FULL_SCREEN_MIN_SCALE) {
                    mScaleFactor = FULL_SCREEN_MIN_SCALE;
                }
            } else {
                if (mScaleFactor < NOT_FULL_MIN_SCALE) {
                    mScaleFactor = NOT_FULL_MIN_SCALE;
                }
            }
            if (mScaleFactor > MAX_SCALE) {
                mScaleFactor = MAX_SCALE;
            }

            // TODO mScaleFactor?????????????????????????小????
            doScale(px, py);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            // mSurfaceWidth = (int) (screenWidth * mScaleFactor);
            // mSurfaceHeight = mSurfaceWidth * 9 / 16;
            // FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            // mSurfaceWidth, mSurfaceHeight);
            // mSurface.setLayoutParams(params);
            // animator.animate(mSurface);
        }
    }

    public void showSurfaceView() {
        // if (mSurface != null
        // && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
        // // int diff = (int) (mSurfaceWidth * (SCALE - 1) / 2);
        // mSurface.layout(0, 0, mSurfaceWidth, mSurfaceHeight);
        // // mSurface.layout(0, 0, mSurface.getWidth(),
        // // mSurface.getHeight());
        // }
    }

    public void hideSurfaceView() {
        // if (mSurface != null
        // && mOrientation == Configuration.ORIENTATION_PORTRAIT) {
        // mSurface.layout(-1 * mSurface.getWidth() * 2, 0, -1
        // * mSurface.getWidth(), mSurface.getHeight());
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

    public View.OnClickListener mOnClickListener;

    private static final int MAX_DELAY_FRAME = 100;

    /**
     * ????????????AVFrame
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

    private void clearNonIFrame() {
        AntsLog.d(TAG, "clearNonIFrame");
        videoFrameQueue.clear();
        /**
         * for (int i = videoFrameQueue.size() - 1; i > 0; i--) { AVFrame avFrame =
         * videoFrameQueue.get(i); if (!avFrame.isIFrame()) { videoFrameQueue.remove(i); } }
         **/
    }

    private boolean isDoubleTapScaled = false;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(AntsSurfacePlayer.this);
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // mLastFactor = mScaleFactor;
            if (mScaleFactor < 1.3f) {
                scaleTo2x(e);
            } else {
                scaleTo1x(e);
            }

            return true;
        }

        private void scaleTo1x(MotionEvent e) {
            int frate = 300 / 30;
            float step = mScaleFactor - 1.0f;
            while (mScaleFactor > 1.0f) {
                mScaleFactor -= step / 30;
                try {
                    Thread.sleep(frate);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                doScale(e.getX(), e.getY());
            }

            mScaleFactor = 1.0f;
            doScale(e.getX(), e.getY());
        }

        private void scaleTo2x(MotionEvent e) {
            int frate = 300 / 30;
            float step = 2.0f - mScaleFactor;
            while (mScaleFactor < 2.0f) {
                mScaleFactor += step / 30;
                try {
                    Thread.sleep(frate);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                doScale(e.getX(), e.getY());
            }
            mScaleFactor = 2.0f;
            doScale(e.getX(), e.getY());
        }

    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        this.mOnClickListener = l;
    }

    /**
     * ????????
     */
    public void clearBuffer() {
        synchronized (OBJECT) {
            videoFrameQueue.clear();
        }
    }

    /**
     * ????????
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
//
//        if (null != mSurface) {
//            mSurface.destroyDrawingCache();
//
//            mSurface.queueEvent(new Runnable() {
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
//        if (mSurface != null) {
//            mSurface.onResume();
//        }
        if (!mSensorEventListenerRegistered) {
            mSensorEventListenerRegistered = true;
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            mSensorManager.registerListener(mSensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void pause() {
//        if (mSurface != null) {
//            mSurface.onPause();
//        }

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

    private Object LOCK = new Object();

    public interface OnDataRateChangedListener {
        void onDataRateChanged(int dataRate);

        void onLongTimeNoDataHappened();

        void onLongTimeDecodeErrorHappened();
    }

    private long lastCalculateTime;
    private long bitcount;

    private OnDataRateChangedListener onDataRateChangedListener;

    public void setOnDataRateChangedListener(OnDataRateChangedListener onDataRateChangedListener) {
        this.onDataRateChangedListener = onDataRateChangedListener;
    }

    private static final int ON_DATARATE_CHANGED = 111;
    private static final int ON_LONG_TIME_NO_DATA = 222;
    private static final int ON_LONG_TIME_DECODE_ERROR = 333;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ON_DATARATE_CHANGED:
                    if (onDataRateChangedListener != null) {
                        onDataRateChangedListener.onDataRateChanged(msg.arg1);
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
                default:
                    break;
            }
        }

        ;

    };

    private class ThreadCalculatingRate extends Thread {
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
                    lastCalculateTime = now;
                    bitcount = 0;
                    dataCount += kbps;
                    second += 2;

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
                    msg.what = ON_DATARATE_CHANGED;
                    msg.arg1 = kbps;
                    mHandler.sendMessage(msg);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // AntsLog.d("ThreadCalculatingRate", System.currentTimeMillis()+"");
            }
        }

        public void stopThread() {
            isRunning = false;
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class ThreadDecodeVideo extends Thread {

        private static final int NEW_FRAME = 0;
        private long lastAvFrameTime;

        private long decodeFinishedTime;

        private boolean decodeLostFrame = false;

        public boolean isRunning = true;

        private byte[] yuvbuf = new byte[MAX_FRAMEBUF];

        private long prets = 0;

        private int decodeErrorCount = 0;

        private int MAX_IFRAME_DECODE_ERROR = 2;
        private long lastPlayTime;

        private boolean ctrlFrameFlowRate(long difftime, AVFrame frame) {
            long curts = 0;
            if (frame.getTimestamp_ms() > 0) {
                curts = frame.getTimestamp_ms();
            } else {
                curts = frame.getTimeStamp();
            }
            long waittime = 0;

            if (curts < prets) {
                waittime = 0;
                prets = curts;
            } else {
                long dtime = curts - prets;

                if (dtime < 40) {
                    dtime = 40;
                }

                waittime = dtime - difftime;

                if (difftime > 40 || videoFrameQueue.size() > 5) {
                    waittime = 0;
                }

                if (waittime > 30) {
                    waittime = 30;
                }

                // if (mAVFrameQueue.size() < 3 && waittime < 10) {
                // waittime = 10;
                // }

            }

            if (waittime < 2) {
                waittime = 0;
            }

            // AntsLog.d(TAG, "size:" + mAVFrameQueue.size() + " waitime:"
            // + waittime + " diffTime:" + diffTime);

            if (waittime > 0) {
                try {
                    Thread.sleep(waittime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            prets = curts;
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

                    AntsLog.d("frame",
                            "decode frame:" + avFrame.getFrmNo() + "-" + avFrame.getTimeStamp());

                    if (avFrame.isIFrame()) {
                        decodeLostFrame = false;
                    }

                    // if (decodeLostFrame) {
                    // continue;
                    // }

                    lastAvFrameTime = System.currentTimeMillis();

                    // if(isNeedCtrlFrameFlowRate(difftime,avFrame)){
                    // continue;
                    // }

                    synchronized (LOCK) {
                        bitcount += avFrame.getFrmSize();
                    }


                    if (lastAvframe != null
                            && avFrame.getVideoWidth() != lastAvframe.getVideoWidth()) {
                        AntsLog.d("decode", "video width changed " + avFrame.getVideoWidth());

//                        h264dec.nativeDestroy();
//                        h264dec = new H264Decoder(H264Decoder.COLOR_FORMAT_YUV420);
                    }

                    if (decodeErrorCount >= MAX_IFRAME_DECODE_ERROR) {
                        AntsLog.d("decode", "avFrame decode failed");
                        decodeErrorCount = 0;
                        mHandler.sendEmptyMessage(ON_LONG_TIME_DECODE_ERROR);
                        continue;
                    }

//                    if (h264dec == null) {
//                        break;
//                    }

//                    int ret = h264dec.consumeNalUnitsFromDirectBuffer(avFrame.frmData,
//                            avFrame.getFrmSize(), avFrame.getFrmNo());
//
//                    if (ret <= 0) {
//                        if (avFrame.isIFrame() && avFrame.frmData.length > 36) {
//                            decodeErrorCount++;
//                        }
//                        lastAvframe = avFrame;
//                        continue;
//                    }

                    if (avFrame.isIFrame() && avFrame.frmData.length > 36) {
                        decodeErrorCount = 0;
                    }

//                    if (yuvbuf == null) {
//                        break;
//                    }

                    initCodec(avFrame.getVideoWidth(), avFrame.getVideoHeight());

                    if (mediaCodec == null) {
                        break;
                    }

                    synchronized (mediaCodec) {
                        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            inputBuffer.put(avFrame.frmData);
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, avFrame.frmData.length, 0L, 0);
                        }

                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        byte[] outData = new byte[bufferInfo.size];
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                            outputBuffer.position(bufferInfo.offset);
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                            outputBuffer.get(outData);

                            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);


//                    int res = (int) h264dec.getYUVData(yuvbuf, yuvbuf.length);
//
//                    if (res != -1) {
                            // Log.d("VIDEO", "setFrame");
//                        mLastYUVData = new YUVData(yuvbuf, h264dec.getWidth(), h264dec.getHeight());
                            mLastYUVData = new YUVData(outData, avFrame.getVideoWidth(), avFrame.getVideoHeight());

                            decodeFinishedTime = System.currentTimeMillis();

                            long difftime = System.currentTimeMillis() - lastPlayTime;

                            ctrlFrameFlowRate(difftime, avFrame);

                            AntsLog.d(TAG, "playdiff:" + (System.currentTimeMillis() - lastPlayTime));

                            lastPlayTime = System.currentTimeMillis();

//                            if (mRenderer != null && mSurface != null) {
//                                mRenderer.setFrame(outData, 640, 360);
//                            }

//                    }
                            lastAvframe = avFrame;
                        }
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
                // AntsLog.d("ThreadDecodeVideo", System.currentTimeMillis()+"");
            }

//            if (h264dec != null) {
//                h264dec.nativeDestroy();
//                h264dec = null;
//            }

        }

        public void stopThread() {
            isRunning = false;
            yuvbuf = null;
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
                    if (mActivePointerId == INVALID_POINTER_ID) {
                        return true;
                    }
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float y = ev.getY(pointerIndex);
                    // if (mOrientation != Configuration.ORIENTATION_LANDSCAPE)
                    // {
                    int dx = (int) (x - mLastTouchX);
                    int dy = (int) (y - mLastTouchY);

                    // Log.d("dxdy", "dx :" + dx + "dy :" + dy);

                    if (Math.abs(dx) > mScaledTouchSlop / 10 || Math.abs(dy) > mScaledTouchSlop / 10) {
                        float fTranslateX = (float) dx * mScaleFactor / (float) (getWidth());
                        float fTranslateY = (float) dy * mScaleFactor / (float) (getHeight());
                        mAccTransX += fTranslateX;
                        mAccTransY += fTranslateY;
                        resetXYToEdge();
                        if (mScaleFactor < FULL_SCREEN_MIN_SCALE) {
                            mAccTransY = 0.0f;
                        }
                        // Log.d("mEdge", "mEdge :" + mEdge + "mAccTransX :" +
                        // mAccTransX
                        // + "mAccTransY :" + mAccTransY);

                        // Log.d("mEdge", "mEdge1 :" + mEdge + "mAccTransX :"
                        // + mAccTransX + "mAccTransY :" + mAccTransY);
//                        mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);
//                        mRenderer.refresh();

                        mLastTouchX = x;
                        mLastTouchY = y;
                    }
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

    // ????
    public void layOutLandscape() {
        isFullScreen = true;
        mSurfacediff = 0.0f;
        mSurfaceWidth = screenHeight;
        mSurfaceHeight = mSurfaceWidth * 9 / 16;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mSurfaceWidth,
                mSurfaceHeight);
        mSurface.setLayoutParams(params);
//        resetRenderParams(false);
        mOrientation = Configuration.ORIENTATION_LANDSCAPE;
    }

    // ????
    public void layOutPortrait() {
        isFullScreen = false;
        mSurfacediff = (SURFACE_SCALE_NEW - 1.0f);
        mSurfaceWidth = (int) (screenWidth * SURFACE_SCALE);
        mSurfaceHeight = mSurfaceWidth * 9 / 16;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mSurfaceWidth,
                mSurfaceHeight);
        mSurface.setLayoutParams(params);
//        resetRenderParams(true);
        mOrientation = Configuration.ORIENTATION_PORTRAIT;
    }

//    private void resetRenderParams(boolean isCenter) {
//        if (isCenter) {
//            mAccTransX = -mSurfacediff / 2;
//        } else {
//            mAccTransX = 0.0f;
//        }
//        mAccTransY = 0.0f;
//        mScaleFactor = 1.0f;
//        mEdge = mScaleFactor - 1.0f;
//        mRenderer.setScale(mScaleFactor);
//        mRenderer.setTranslate(mAccTransX, -mAccTransY, 0.0f);
//
//    }

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


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


}