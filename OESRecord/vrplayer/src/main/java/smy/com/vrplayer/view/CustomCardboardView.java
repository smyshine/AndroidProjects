package smy.com.vrplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.vr.sdk.base.GvrView;

import smy.com.vrplayer.XYVRPlayer;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.listener.VRPlayListener;
import smy.com.vrplayer.listener.ViewDirectionChangeListener;
import smy.com.vrplayer.objects.base.CombineParams;
import smy.com.vrplayer.render.AbstractRenderer;
import smy.com.vrplayer.render.VRPictureRenderer;
import smy.com.vrplayer.render.VRVideoRender;

/**
 * Created by SMY on 2017/7/8.
 */

public class CustomCardboardView extends GvrView implements
        XYVRPlayer.ICardboardViewGestureListener {

    private ViewDirectionListener mViewDirectionListener;

    public enum RenderType {
        VRPicture, VRVideo, DualVRVideo, TBVRVideo
    }

    public interface OnCardboardViewClickListener {
        void onCardboardViewOnClicked();
    }

    AbstractRenderer mVRRenderer;
    RenderType mRenderType;
    OnCardboardViewClickListener mCardboardViewClickListener;

    XYVRPlayer mVRPlayerLib;
    private int mCustomRenderMode = VrConstant.RENDER_MODE_DEFAULT;

    private Activity mActivity;
    private float mGestureAngleX;
    private int mHeadDegree = 0;
    private int mOldCompassDegree = 0;

    public CustomCardboardView(Context context) {
        super(context);
        init();
    }

    public CustomCardboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setStereoModeEnabled(false);
    }

    public void initRender(RenderType renderType, int playerType, Activity activity){
        initRender(renderType, playerType, activity, "", "", "", "", "");
    }

    public void initRender(RenderType renderType, int playerType, Activity activity,
                           String panoParams,
                           String frontOcamModel, String backOcamModel,
                           String frontParams, String backParams) {
        mActivity = activity;
        mRenderType = renderType;
        CombineParams params = new CombineParams(VrConstant.FULL_VIDEO_WIDTH,
                VrConstant.FULL_VIDEO_HEIGHT, VrConstant.SPHERE_SAMPLE_STEP,
                panoParams,
                frontOcamModel,
                backOcamModel,
                frontParams,
                backParams);
        if (mRenderType == RenderType.VRVideo) {
            mVRRenderer = new VRVideoRender(getContext().getApplicationContext(),
                    VrConstant.SOURCE_FILE_TYPE_PANO, playerType, params);
        } else if (mRenderType == RenderType.VRPicture) {
            mVRRenderer = new VRPictureRenderer(params);
        } else if (mRenderType == RenderType.DualVRVideo) {
            mVRRenderer = new VRVideoRender(getContext().getApplicationContext(),
                    VrConstant.SOURCE_FILE_TYPE_DUAL, playerType, params);
        } else if (mRenderType == RenderType.TBVRVideo){
            mVRRenderer = new VRVideoRender(getContext().getApplicationContext(),
                    VrConstant.SOURCE_FILE_TYPE_STERE, playerType, params);
        }
        mVRRenderer.setViewDirectionChangeListener(new ViewDirectionChangeListener() {
            @Override
            public void onViewDirectionChange(float x) {
                onCardboardViewDrag(x, 0);
            }
        });
        setRenderer(mVRRenderer);
        mVRPlayerLib = createVRPlayer(activity);
    }

    protected XYVRPlayer createVRPlayer(Activity activity) {
        return XYVRPlayer.with(activity)
                .glSurfaceView(this)
                .vrRender(mVRRenderer)
                .listenGesture(this)
                .build();
    }

    @Override
    public void onCardboardViewClick(MotionEvent e) {
        if(mCardboardViewClickListener != null){
            mCardboardViewClickListener.onCardboardViewOnClicked();
        }
    }

    @Override
    public void onCardboardViewDrag(float distanceX, float distanceY) {
        mGestureAngleX -= distanceX;
        onViewDirectionChange();
    }

    @Override
    public void onCardboardViewFling(float velocityX, float velocityY) {

    }

    @Override
    public void onCardboardViewPinch(float scale) {

    }

    @Override
    public void onCardboardSensorChanged(int headDegree) {
        mHeadDegree = headDegree;
        onViewDirectionChange();
    }

    private void onViewDirectionChange(){
        float angleView = mHeadDegree - mGestureAngleX;
        if (angleView > VrConstant.GESTURE_X_ANGLE_NORMAL_MAX || angleView < VrConstant.GESTURE_X_ANGLE_NORMAL_MIN){
            mGestureAngleX = mHeadDegree;
            angleView = 0;
        }
        applyDirectionToView((int)angleView);
    }

    public void setDataSource(final String url) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mVRRenderer.setVideoPath(url);
            }
        });
    }

    public void setDataSource(final Bitmap bitmap) {
        if (mVRRenderer instanceof VRPictureRenderer) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mVRRenderer.initBitmap(bitmap);
                }
            });
        }
    }

    public void setCardboardViewClickListener(OnCardboardViewClickListener listener) {
        mCardboardViewClickListener = listener;
    }

    public void setViewDirectionListener(ViewDirectionListener viewDirectionListener) {
        this.mViewDirectionListener = viewDirectionListener;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        super.shutdown();
        if(mVRRenderer != null) {
            mVRRenderer.onDestroy();
        }
        if(mVRPlayerLib != null) {
            mVRPlayerLib.onPause(getContext());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    public void onStartPlayVideo(){
        resetCameraOrientation();
    }

    public void resetCameraOrientation() {
        if(mVRPlayerLib != null){
            mVRPlayerLib.resetVRViewAngle();
        }
        mHeadDegree = 0;
        mGestureAngleX = 0;
        applyDirectionToView(0);
    }

    public void changeMediaProgress(int progress) {
        mVRRenderer.setMediaPlayerSeekTo(progress);
    }

    public void pausePlay() {
        mVRRenderer.pausePlay();
    }

    public void startPlay() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mVRRenderer.startPlay();
            }
        });

    }

    public void stopPlay() {
        mVRRenderer.onStopPlay();
    }

    public void releasePlayer() {
        mVRRenderer.releasePlayer();
    }

    public void setRenderListener(VRPlayListener listener) {
        mVRRenderer.setVideoPlayListener(listener);
    }

    @Override
    public void onPause() {
        if(mVRPlayerLib != null) {
            mVRPlayerLib.onPause(getContext());
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if(mVRPlayerLib != null) {
            mVRPlayerLib.onResume(getContext());
        }
        super.onResume();
    }

    public void onOrientationChanged(){
        if(mVRPlayerLib != null) {
            mVRPlayerLib.onOrientationChanged(mActivity);
        }
    }

    public void changeSensor(boolean use){
        if(mVRPlayerLib != null) {
            mVRPlayerLib.useSensor(mActivity, use);
        }
    }

    public int getCustomRenderMode() {
        return mCustomRenderMode;
    }

    public void setCustomRenderMode(final int renderMode) {
        if (mCustomRenderMode == renderMode) {
            return;
        }
        mCustomRenderMode = renderMode;
        switch (renderMode) {
            case VrConstant.RENDER_MODE_SPHERE:
            case VrConstant.RENDER_MODE_PLANET:
            case VrConstant.RENDER_MODE_SPHERE_OUTSIDE:
            case VrConstant.RENDER_MODE_OVERALL:
                if(getStereoModeEnabled()) {
                    setStereoModeEnabled(false);
                }
                break;
            case VrConstant.RENDER_MODE_VR:
                if(!getStereoModeEnabled()) {
                    setStereoModeEnabled(true);
                }
                break;
        }
        mVRPlayerLib.changeRenderMode(mActivity,renderMode);

    }

    private void applyDirectionToView(int direction) {
        if(direction == mOldCompassDegree){
            return;
        }
        mOldCompassDegree = direction;
        if (mViewDirectionListener != null) {
            mViewDirectionListener.onViewDirection(direction);
        }
    }
}
