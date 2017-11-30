package smy.com.vrplayer;

import android.content.Context;
import android.content.res.Resources;
import android.view.GestureDetector;
import android.view.MotionEvent;

import smy.com.vrplayer.model.XYPinchConfig;


/**
 * Created by hzqiujiadi on 16/5/6.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * reference
 * https://github.com/boycy815/PinchImageView/blob/master/pinchimageview/src/main/java/com/boycy815/pinchimageview/PinchImageView.java
 */
public class XYTouchHelper {

    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;

    private static final float sDamping = 0.2f;

    private XYVRPlayer.ICardboardViewGestureListener mGestureListener;
    private GestureDetector mGestureDetector;
    private int mCurrentMode = 0;
    private PinchInfo mPinchInfo = new PinchInfo();
    private boolean mPinchEnabled;
    private float minScale;
    private float maxScale;
    private float mSensitivity;
    private float defaultScale;
    private float mGlobalScale;

    private static final int MODE_INIT = 0;
    private static final int MODE_PINCH = 1;

    public XYTouchHelper(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mCurrentMode == MODE_PINCH) return false;
                if(mGestureListener != null){
                    mGestureListener.onCardboardViewClick(e);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mCurrentMode == MODE_PINCH) return false;

                if (mGestureListener != null)
                    mGestureListener.onCardboardViewDrag(distanceX / sDensity * sDamping,
                            distanceY / sDensity * sDamping);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (mCurrentMode == MODE_PINCH) return false;

                if (mGestureListener != null){
                    mGestureListener.onCardboardViewFling(velocityX, velocityY);
                }
                return true;
            }
        });
    }

    public boolean handleTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mCurrentMode == MODE_PINCH) {
                reset();
                // end anim
            }
            mCurrentMode = MODE_INIT;
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            // one point up
            if (mCurrentMode == MODE_PINCH) {
                // more than 2 pointer
                if (event.getPointerCount() > 2) {
                    if (event.getAction() >> 8 == 0) {
                        // 0 up
                        markPinchInfo(event.getX(1), event.getY(1), event.getX(2), event.getY(2));
                    } else if (event.getAction() >> 8 == 1) {
                        // 1 up
                        markPinchInfo(event.getX(0), event.getY(0), event.getX(2), event.getY(2));
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            // >= 2 pointer
            mCurrentMode = MODE_PINCH;
            markPinchInfo(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
        } else if (action == MotionEvent.ACTION_MOVE) {
                // >= 2 pointer
                if (mCurrentMode == MODE_PINCH && event.getPointerCount() > 1) {
                    markPinchInfo(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    handlePinch();
                }
        }

        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void reset(){
        mPinchInfo.reset();
    }

    private void handlePinch() {
        if (mPinchEnabled){
            setScaleInner(mPinchInfo.pinch());
        }
    }

    private void setScaleInner(float scale){
        if (mGestureListener != null) {
            mGestureListener.onCardboardViewPinch(scale);
        }
        mGlobalScale = scale;
    }

    private void markPinchInfo(float x1, float y1, float x2, float y2) {
        mPinchInfo.mark(x1, y1, x2, y2);
    }

    private static float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public void setGestureListener(XYVRPlayer.ICardboardViewGestureListener listener) {
        this.mGestureListener = listener;
    }

    public void setPinchEnabled(boolean mPinchEnabled) {
        this.mPinchEnabled = mPinchEnabled;
    }

    public void setPinchConfig(XYPinchConfig pinchConfig) {
        this.minScale = pinchConfig.getMin();
        this.maxScale = pinchConfig.getMax();
        this.mSensitivity = pinchConfig.getSensitivity();
        this.defaultScale = pinchConfig.getDefaultValue();
        this.defaultScale = Math.max(minScale, this.defaultScale);
        this.defaultScale = Math.min(maxScale, this.defaultScale);
        setScaleInner(this.defaultScale);
    }

    private class PinchInfo{
        private float newDistance;
        private float lastDistance;

        public void mark(float x1, float y1, float x2, float y2){
            newDistance = calDistance(x1, y1, x2, y2);
            if(lastDistance < 0.1f){
                lastDistance = newDistance;
            }
        }

        public float pinch() {
            float result = newDistance - lastDistance;
            lastDistance = newDistance;
            return result;
        }

        public void reset(){
            newDistance = 0.0f;
            lastDistance = 0.0f;
        }
    }
}
