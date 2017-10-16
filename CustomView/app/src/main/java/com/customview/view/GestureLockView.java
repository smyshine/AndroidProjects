package com.customview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by SMY on 2017/10/16.
 */

public class GestureLockView extends View {

    public enum Mode {
        MODE_NO_FINGER, MODE_FINGER_ON, MODE_FINGER_UP
    }

    private Mode mCurrentMode = Mode.MODE_NO_FINGER;

    private int mWidth;
    private int mHeight;
    private int mRadius;//outer circle radius
    private int mStrokeWidth = 2;

    private int mCenterX, mCenterY;
    private Paint mPaint;

    private float mArrowRate = 0.333f;
    private int mArrowDegree = -1;
    private Path mArrowPath;

    private float mInnerCircleRadiusRate = 0.3f;

    private int mColorInner, mColorOuter;
    private int mColorFingerOn, mColorFingerUp;


    public GestureLockView(Context context, int colorInner , int colorOutter , int colorFingerOn , int colorFingerUp) {
        super(context);

        this.mColorInner = colorInner;
        this.mColorOuter = colorOutter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mWidth = Math.min(mWidth, mHeight);
        mRadius = mCenterX = mCenterY = mWidth / 2;
        mRadius -= mStrokeWidth / 2;

        float arrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, mStrokeWidth + 2);
        mArrowPath.lineTo(mWidth / 2 - arrowLength, mStrokeWidth + 2 + arrowLength);
        mArrowPath.lineTo(mWidth / 2 + arrowLength, mStrokeWidth + 2 + arrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentMode){
            case MODE_FINGER_ON:
                //outer circle
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerOn);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                //inner circle
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                break;
            case MODE_FINGER_UP:
                //outer circle
                mPaint.setColor(mColorFingerUp);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                //inner circle
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);

                drawArrow(canvas);
                break;
            case MODE_NO_FINGER:
                //outer circle
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mColorOuter);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                //inner circle
                mPaint.setColor(mColorInner);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                break;
        }
    }

    private void drawArrow(Canvas canvas){
        if (mArrowDegree != -1){
            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.rotate(mArrowDegree, mCenterX, mCenterY);
            canvas.drawPath(mArrowPath, mPaint);

            canvas.restore();
        }
    }

    public void setMode(Mode mode){
        this.mCurrentMode = mode;
        invalidate();
    }

    public void setArrowDegree(int degree){
        this.mArrowDegree = degree;
    }

    public int getArrowDegree(){
        return this.mArrowDegree;
    }
}
