package smy.com.vrplayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.SensorEventListener;
import android.view.MotionEvent;
import android.view.View;

import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.common.XYGLHandler;
import smy.com.vrplayer.common.XYMainHandler;
import smy.com.vrplayer.model.XYPinchConfig;
import smy.com.vrplayer.render.AbstractRenderer;
import smy.com.vrplayer.render.RenderObjectHelper;
import smy.com.vrplayer.strategy.interactive.InteractiveModeManager;


/**
 * Created by HanZengbo on 2017/3/24.
 */
public class XYVRPlayer {

    public interface INotSupportCallback{
        void onNotSupport(int mode);
    }

    public interface ICardboardViewGestureListener {
        void onCardboardViewClick(MotionEvent e);
        void onCardboardViewDrag(float distanceX, float distanceY);
        void onCardboardViewFling(float velocityX, float velocityY);
        void onCardboardViewPinch(float scale);
        void onCardboardSensorChanged(int headDegree);
    }

    private XYGLHandler mGLHandler;
    private InteractiveModeManager mInteractiveModeManager;
    //用来标记进入vr模式前是否已经打开陀螺仪开关
    private boolean mShouldUseSensor = false;
    private XYTouchHelper mXYTouchHelper;
    private AbstractRenderer mVRRender;
    private ICardboardViewGestureListener mGestureListener;

    public static Builder with(Activity activity){
        return new Builder(activity);
    }

    private XYVRPlayer(Builder builder) {
        // init main handler
        XYMainHandler.init();
        // init gl handler
        mGLHandler = new XYGLHandler();

        mVRRender = builder.vrRender;
        mVRRender.setGLHandler(mGLHandler);
        ShaderProgram.sContext = builder.activity.getApplicationContext();
        mGestureListener = builder.gestureListener;
        initModeManager(builder);

        initTouchHelper(builder);

    }

    private void initModeManager(Builder builder) {
        // init InteractiveModeManager
        InteractiveModeManager.Params interactiveManagerParams = new InteractiveModeManager.Params();
        interactiveManagerParams.mVRRender = mVRRender;
        //interactiveManagerParams.mMotionDelay = builder.motionDelay;
        interactiveManagerParams.mSensorListener = new InteractiveModeManager.SensorHeadViewChanged() {
            @Override
            public void onSensorHeadViewChanged(int headerDegree) {
                if(mGestureListener != null){
                    mGestureListener.onCardboardSensorChanged(headerDegree);
                }
            }
        };
        mInteractiveModeManager = new InteractiveModeManager(builder.interactiveMode, mGLHandler, interactiveManagerParams);
        mInteractiveModeManager.prepare(builder.activity, builder.notSupportCallback);
    }

    private void initTouchHelper(Builder builder){
        mXYTouchHelper = new XYTouchHelper(builder.activity);
        mXYTouchHelper.setPinchEnabled(builder.pinchEnabled);
        final UpdatePinchRunnable updatePinchRunnable = new UpdatePinchRunnable();
        mXYTouchHelper.setGestureListener(new ICardboardViewGestureListener() {
            @Override
            public void onCardboardViewClick(MotionEvent e) {
                if(mGestureListener != null){
                    mGestureListener.onCardboardViewClick(e);
                }
            }

            @Override
            public void onCardboardViewDrag(float distanceX, float distanceY) {
                mInteractiveModeManager.handleDrag(distanceX,distanceY);
                if (mGestureListener != null) {
                    mGestureListener.onCardboardViewDrag(distanceX,
                            distanceY);
                }
            }

            @Override
            public void onCardboardViewFling(float velocityX, float velocityY) {
                mInteractiveModeManager.handleFling(velocityX,velocityY);
                if (mGestureListener != null) {
                    mGestureListener.onCardboardViewFling(velocityX, velocityY);
                }
            }

            @Override
            public void onCardboardViewPinch(final float distance) {
                updatePinchRunnable.setScale(distance);
                mGLHandler.post(updatePinchRunnable);
            }

            @Override
            public void onCardboardSensorChanged(int headDegree) {

            }

        });
        mXYTouchHelper.setPinchConfig(builder.pinchConfig);
        builder.glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mXYTouchHelper.handleTouchEvent(event);
            }
        });
    }

    public void onResume(Context context){
        if(mVRRender != null){
            mVRRender.onResume();
        }
        mInteractiveModeManager.onResume(context);
    }

    public void onPause(Context context){
        if(mVRRender != null){
            mVRRender.onPause();
        }
        mInteractiveModeManager.onPause(context);
    }

    private class UpdatePinchRunnable implements Runnable{
        private float distance;

        public void setScale(float distance) {
            this.distance = distance;
        }

        @Override
        public void run() {
            if(mVRRender != null){
                mVRRender.onZoomGesture(distance);
            }
        }
    }
    private class ResetCameraAngleRunnable implements Runnable {
        @Override
        public void run() {
            mInteractiveModeManager.onResetCameraAngle();
            if(mVRRender != null){
                mVRRender.resetCameraOrientation();
            }
        }
    }

    public void onOrientationChanged(Activity activity) {
        mInteractiveModeManager.onOrientationChanged(activity);
        mVRRender.onOrientationChange(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void resetVRViewAngle(){
        final ResetCameraAngleRunnable resetCameraAngleRunnable = new ResetCameraAngleRunnable();
        mGLHandler.post(resetCameraAngleRunnable);
    }

    public void changeRenderMode(final Activity activity, final int renderMode){
        if (mVRRender != null) {
            final int preMode = mVRRender.getRenderMode();
            final boolean usingSensor= mInteractiveModeManager.isUsingSensor();
            mVRRender.setRenderMode(renderMode, new RenderObjectHelper.ChangeRenderListener() {
                @Override
                public void changeStart() {
                    if (usingSensor){
                        useSensor(activity, false);
                    }
                }

                @Override
                public void changeEnd() {
                    if (usingSensor){
                        useSensor(activity, true);
                    }

                    if (renderMode == VrConstant.RENDER_MODE_VR) {
                        enterVRMode(activity);
                    } else if (preMode == VrConstant.RENDER_MODE_VR) {
                        exitVRMode(activity);
                    }
                }
            });
        }
    }

    private void enterVRMode(Activity activity){
        mShouldUseSensor = mInteractiveModeManager.isUsingSensor();
        //开启vr模式应该自动打开陀螺仪
        if(!mShouldUseSensor){
            useSensor(activity, true);
        }
    }

    private void exitVRMode(Activity activity){
        if(!mShouldUseSensor){
            useSensor(activity,false);
        }
    }

    public void useSensor(Activity activity, boolean use){
        if(use){
            mInteractiveModeManager.switchMode(activity, VrConstant.INTERACTIVE_MODE_CARDBOARD_MOTION_WITH_TOUCH);
        } else {
            mInteractiveModeManager.switchMode(activity, VrConstant.INTERACTIVE_MODE_TOUCH);
        }
    }


    public static class Builder {
        private XYPinchConfig pinchConfig = new XYPinchConfig();
        private SensorEventListener sensorListener;
        private Activity activity;
        private INotSupportCallback notSupportCallback;
        private ICardboardViewGestureListener gestureListener;
        private boolean pinchEnabled = true;
        private int interactiveMode = VrConstant.INTERACTIVE_MODE_TOUCH;
        private View glSurfaceView ;
        private AbstractRenderer vrRender;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        /**
         * gesture listener, e.g.
         * onClick
         *
         * @param listener listener
         * @return builder
         */
        public Builder listenGesture(ICardboardViewGestureListener listener) {
            gestureListener = listener;
            return this;
        }

        public Builder sensorCallback(SensorEventListener callback){
            this.sensorListener = callback;
            return this;
        }

        public Builder pinchConfig(XYPinchConfig config){
            this.pinchConfig = config;
            return this;
        }

        public Builder glSurfaceView(View view){
            this.glSurfaceView = view;
            return this;
        }

        public Builder vrRender(AbstractRenderer renderer){
            this.vrRender = renderer;
            return this;
        }


        public XYVRPlayer build(){
            return new XYVRPlayer(this);
        }
    }

    public interface ContentType{
        int VIDEO = 0;
        int BITMAP = 1;
        int DEFAULT = VIDEO;
    }

}
