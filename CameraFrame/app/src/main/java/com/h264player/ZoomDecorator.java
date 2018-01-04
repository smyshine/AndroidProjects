package com.h264player;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;

/**
 * Created by Nat on 2017/1/30.
 */

public class ZoomDecorator extends PlayerDecorator implements View.OnTouchListener{

    private static final String TAG = "ZoomDecorator";
    private Matrix mMatrix;
    private ScaleGestureDetector scaleDetector;
    private MoveGestureDetector moveDetector;
    private GestureDetector gestureDetector;
    
    private float mScaleFactor = 1.f;
    private float windowFocusX = 0.f;
    private float windowFocusY = 0.f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float textureViewRatioX = 0f;
    private float textureViewRatioY = 0f;

    private TextureView textureView;
    private SimpleOnGestureListener simpleOnGestureListener = null;

    public interface SimpleOnGestureListener{
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
        void onViewTap(View view, float x, float y);
    }


    public ZoomDecorator(Context context, IPlayer iPlayer) {
        super(iPlayer);

        textureView = iPlayer.getTextureView();
        textureView.setOnTouchListener(this);
        init(context);
    }

    private void init(Context context) {
        mMatrix = new Matrix();
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        moveDetector = new MoveGestureDetector(context, new MoveListener());
        gestureDetector = new GestureDetector(context, new SingleTapListener());
    }


    public void setSimpleOnGestureListener(SimpleOnGestureListener simpleOnGestureListener){
        this.simpleOnGestureListener = simpleOnGestureListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        scaleDetector.onTouchEvent(motionEvent);
        moveDetector.onTouchEvent(motionEvent);
        gestureDetector.onTouchEvent(motionEvent);

        float scaledTextureViewFocusX = (textureView.getWidth() * mScaleFactor * textureViewRatioX) ;
        float scaledTextureViewFocusY = (textureView.getHeight() * mScaleFactor * textureViewRatioY) ;

        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postTranslate( getTranslateX(scaledTextureViewFocusX), getTranslateY(scaledTextureViewFocusY));
        textureView.setTransform(mMatrix);
        textureView.setAlpha(1);
        textureView.invalidate();

        return true; // indicate event was handled

    }

    private float getTranslateY(float scaledTextureViewFocusY) {

        translateY = windowFocusY - scaledTextureViewFocusY;

        if (translateY < ((1 - mScaleFactor) * textureView.getHeight())) {
            translateY = (1 - mScaleFactor) * textureView.getHeight();
            windowFocusY = translateY + scaledTextureViewFocusY;
        }

        if (translateY > 0) {
            translateY = 0;
            windowFocusY = translateY + scaledTextureViewFocusY;
        }

        return translateY;
    }

    private float getTranslateX(float scaledTextureViewFocusX) {

        translateX = windowFocusX - scaledTextureViewFocusX;

        if (translateX < ((1 - mScaleFactor) * textureView.getWidth())) {
            translateX = (1 - mScaleFactor) * textureView.getWidth();
            windowFocusX = translateX + scaledTextureViewFocusX;
        }

        if (translateX > 0) {
            translateX = 0;
            windowFocusX = translateX + scaledTextureViewFocusX;
        }

        return translateX;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            windowFocusX = detector.getFocusX();
            windowFocusY = detector.getFocusY();

            textureViewRatioX =  (windowFocusX - translateX) / (textureView.getWidth() * mScaleFactor);
            textureViewRatioY = (windowFocusY - translateY) / (textureView.getHeight() * mScaleFactor);

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4.0f));

            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {

        @Override
        public boolean onMove(MoveGestureDetector detector) {

            PointF d = detector.getFocusDelta();
            windowFocusX += d.x;
            windowFocusY += d.y;
            return true;
        }
    }

    private class SingleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (simpleOnGestureListener != null && mScaleFactor <= 1.0 )  {
                return simpleOnGestureListener.onFling(e1, e2, velocityX, velocityY);
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (simpleOnGestureListener != null ){
                simpleOnGestureListener.onViewTap(textureView, e.getX(), e.getY());
                return true;
            }

            return super.onSingleTapConfirmed(e);
        }
    }
}
