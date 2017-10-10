package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.customview.R;

/**
 * Created by SMY on 2017/10/10.
 */

public class CustomViewVolume extends View{

    private int mFirstColor;
    private int mSecondColor;
    private int mCircleWidth;
    private int mDotCount;
    private int mSplitSize;
    private Bitmap mBg;

    private int mCurrentCount = 0;

    private Rect mRect;
    private Paint mPaint;

    public CustomViewVolume(Context context){
        this(context, null, 0);
    }

    public CustomViewVolume(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public CustomViewVolume(Context context, AttributeSet attributeSet, int def){
        super(context, attributeSet, def);

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CustomViewVolume, def, 0);
        mFirstColor = typedArray.getColor(R.styleable.CustomViewVolume_firstColor, Color.GREEN);
        mSecondColor = typedArray.getColor(R.styleable.CustomViewVolume_secondColor, Color.YELLOW);
        mCircleWidth = typedArray.getDimensionPixelSize(R.styleable.CustomViewVolume_circleWidth, 20);
        mDotCount = typedArray.getInt(R.styleable.CustomViewVolume_dotCount, 20);
        mSplitSize = typedArray.getInt(R.styleable.CustomViewVolume_splitSize, 20);
        mBg = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.CustomViewVolume_bg, 0));
        typedArray.recycle();

        mPaint = new Paint();
        mRect = new Rect();
    }

    public void startUp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCurrentCount < mDotCount){
                    ++mCurrentCount;
                    postInvalidate();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAntiAlias(true);//消除锯齿
        mPaint.setStrokeWidth(mCircleWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        int center = getWidth() / 2;
        int radius = center - mCircleWidth / 2;
        drawOval(canvas, center, radius);

        //计算内切正方形放bg图片
        int relRadius = radius - mCircleWidth / 2;
        mRect.left = (int) (relRadius - Math.sqrt(2) * 1.0f / 2 * relRadius) + mCircleWidth;
        mRect.right = (int) (mRect.left + Math.sqrt(2) * relRadius);
        mRect.top = (int) (relRadius - Math.sqrt(2) * 1.0f / 2 * relRadius) + mCircleWidth;
        mRect.bottom = (int) (mRect.left + Math.sqrt(2) * relRadius);

        if (mBg.getWidth() < Math.sqrt(2) * relRadius){
            mRect.left = (int) (mRect.left + Math.sqrt(2) * 1.0f / 2 * relRadius - mBg.getWidth() * 1.0f / 2);
            mRect.right = mRect.left + mBg.getWidth();
            mRect.top = (int) (mRect.top + Math.sqrt(2) * 1.0f / 2 * relRadius - mBg.getHeight() * 1.0f / 2);
            mRect.bottom = mRect.top + mBg.getHeight();
        }

        canvas.drawBitmap(mBg, null, mRect, mPaint);
    }

    //画每个小块块
    private void drawOval(Canvas canvas, int center, int radius){
        float itemSize = (360 * 1.0f - mDotCount * mSplitSize) / mDotCount;
        RectF oval = new RectF(center - radius, center - radius, center + radius, center + radius);

        mPaint.setColor(mFirstColor);
        for (int i = 0; i < mDotCount; ++i){
            canvas.drawArc(oval, i * (itemSize + mSplitSize), itemSize, false, mPaint);
        }

        mPaint.setColor(mSecondColor);
        for (int i = 0; i < mCurrentCount; ++i){
            canvas.drawArc(oval, i * (itemSize + mSplitSize), itemSize, false, mPaint);
        }
    }

    private void doUp(){
/*        if (mCurrentCount >= mDotCount){
            return;
        }
        ++mCurrentCount;
        postInvalidate();*/
        startUp();
    }

    private void doDown(){
        if (mCurrentCount <= 0){
            return;
        }
        --mCurrentCount;
        postInvalidate();
    }

    private int xDown, xUp;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                xUp = (int) event.getY();
                if (xUp > xDown){
                    doDown();
                } else {
                    doUp();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                xDown = (int) event.getY();
                break;
        }
        return true;
    }
}
