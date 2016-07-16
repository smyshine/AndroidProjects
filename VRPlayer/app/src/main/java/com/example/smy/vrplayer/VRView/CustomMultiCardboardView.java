package com.example.smy.vrplayer.VRView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.smy.vrplayer.VRListener.GestureListener;
import com.example.smy.vrplayer.VRListener.VRPlayListener;
import com.example.smy.vrplayer.VRRender.VRMultiRender;
import com.example.smy.vrplayer.common.ScreenUtils;

import org.rajawali3d.vr.surface.VRSurfaceView;

/**
 * Created by SMY on 2016/7/15.
 */
public class CustomMultiCardboardView extends VRSurfaceView {

    public interface OnCardboardViewClickListener{
        void onCardboardViewOnClicked();
    }

    VRMultiRender mRenderer;
    OnCardboardViewClickListener mCardboardViewClickListener;

    public CustomMultiCardboardView(Context context)
    {
        super(context);
        init();
    }

    public CustomMultiCardboardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        setSettingsButtonEnabled(false);
        setVRModeEnabled(false);
        GestureListener gestureListener= new GestureListener();
        gestureListener.setOnEventGesture(mOnEventGesture);
        gestureListener.setToucheMode(GestureListener.TOUCHEMODE.MODE_ZOOM_AND_MOVE);
        setOnTouchListener(gestureListener);
    }

    public void initRender(){
        mRenderer = new VRMultiRender(getContext());
        setRenderer(mRenderer);
    }

    public void setDataSource(String url)
    {
        mRenderer.setVideoPath(url);
    }

    public void setDataSource(Bitmap bitmap){
        mRenderer.setBitmap(bitmap);
    }

    public void setDataSource(int rid){
        mRenderer.setResourceId(rid);
    }

    public void setCardboardViewClickListener(OnCardboardViewClickListener listener)
    {
        mCardboardViewClickListener = listener;
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        mRenderer.onDestroy();
    }

    private GestureListener.OnEventGesture mOnEventGesture = new GestureListener.OnEventGesture() {
        @Override
        public void onZoomGesture(View v, float newDist, float oldDist) {
            if (!getVRMode())
            {
                mRenderer.onZoomGesture(newDist, oldDist);
            }
        }

        @Override
        public void onMoveGesture(View v, MotionEvent currentMoveEvent, float mTouchStartX, float mTouchStartY) {
            if(!getVRMode())
            {
                mRenderer.addGestureRotateAngle(
                        ScreenUtils.rawY2Angle((Activity)getContext(), currentMoveEvent.getRawY() - mTouchStartY),
                        ScreenUtils.rawX2Angle((Activity)getContext(), currentMoveEvent.getRawX() - mTouchStartX)
                );
            }
        }

        @Override
        public void onClick(View v, MotionEvent currentMoveEvent) {
            if(mCardboardViewClickListener != null)
            {
                mCardboardViewClickListener.onCardboardViewOnClicked();
            }
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    public void resetCameraOrientation()
    {
        if(null != mRenderer) {
            mRenderer.resetCameraOrientation();
        }
    }

    public void changeMediaProgress(int progress)
    {
        mRenderer.setMediaPlayerSeekTo(progress);
    }

    public boolean isPlaying()
    {
        return mRenderer.isPlaying();
    }

    public void pausePlay()
    {
        mRenderer.onPause();
    }

    public void startPlay()
    {
        mRenderer.onResume();
    }

    public void setRenderListener(VRPlayListener listener)
    {
        mRenderer.setVideoPlayListener(listener);
    }

    @Override
    public void onPause()
    {
        if(mRenderer != null) {
            mRenderer.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
        if(mRenderer != null) {
            mRenderer.onResume();
        }
        super.onResume();
    }

    public void changeWatchType()
    {
        if(mRenderer != null) {
            mRenderer.changeWatchType();
        }
    }

    public boolean isUsingSensor()
    {
        if(mRenderer != null) {
            return mRenderer.mUseSensor;
        }
        return false;
    }

    public void setUseSensor(boolean enable){
        mRenderer.mUseSensor = enable;
    }
}
