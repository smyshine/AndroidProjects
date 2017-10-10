package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.customview.R;

/**
 * Created by SMY on 2017/10/10.
 */

public class CustomViewProgress extends View {

    private int mFirstColor;
    private int mSecondColor;
    private int mCircleWidth;
    private int mSpeed;

    private Paint mPaint;
    private int mProgress = 0;
    private boolean isNext = false;


    public CustomViewProgress(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public CustomViewProgress(Context context){
        this(context, null, 0);
    }

    public CustomViewProgress(Context context, AttributeSet attributeSet, int def){
        super(context, attributeSet, def);

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CustomViewProgress, def, 0);
        mFirstColor = typedArray.getColor(R.styleable.CustomViewProgress_firstColor, Color.YELLOW);
        mSecondColor = typedArray.getColor(R.styleable.CustomViewProgress_secondColor, Color.GREEN);
        mCircleWidth = typedArray.getDimensionPixelSize(R.styleable.CustomViewProgress_circleWidth, 40);
        mSpeed = typedArray.getInt(R.styleable.CustomViewProgress_speed, 20);
        typedArray.recycle();

        mPaint = new Paint();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    ++mProgress;
                    if (mProgress == 360){
                        mProgress = 0;
                        isNext = !isNext;
                    }
                    postInvalidate();
                    try {
                        Thread.sleep(mSpeed);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int center = getWidth() / 2;
        int radius = center - mCircleWidth / 2;
        mPaint.setStrokeWidth(mCircleWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        RectF oval = new RectF(center - radius, center - radius, center + radius, center + radius);
        if (!isNext){
            mPaint.setColor(mFirstColor);
            canvas.drawCircle(center, center, radius, mPaint);
            mPaint.setColor(mSecondColor);
            canvas.drawArc(oval, -90, mProgress, false, mPaint);
        } else {
            mPaint.setColor(mSecondColor);
            canvas.drawCircle(center, center, radius, mPaint);
            mPaint.setColor(mFirstColor);
            canvas.drawArc(oval, -90, mProgress, false, mPaint);
        }
    }
}
