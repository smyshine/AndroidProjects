package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.customview.R;

import java.util.Random;

/**
 * Created by SMY on 2017/10/10.
 */

public class CustomViewText extends View{

    private String mText;
    private int mTextColor;
    private int mTextSize;

    private Rect mBound;
    private Paint mPaint;

    public CustomViewText(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public CustomViewText(Context context){
        this(context, null);
    }

    public CustomViewText(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewText, defStyle, 0);
        mText = typedArray.getString(R.styleable.CustomViewText_titleText);
        mTextColor = typedArray.getColor(R.styleable.CustomViewText_titleTextColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.CustomViewText_titleTextSize, 36);
        typedArray.recycle();

        //获取绘制文本的宽高
        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        mBound = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), mBound);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mText = randomText();
                if (mText.length() > 5){
                    mText = mText.substring(0, 5);
                }
                postInvalidate();
            }
        });
    }

    private String randomText(){
        Random random = new Random();
        return random.nextInt() + "";
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;

        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        } else {
            mPaint.setTextSize(mTextSize);
            mPaint.getTextBounds(mText, 0, mText.length(), mBound);
            width = getPaddingLeft() + mBound.width() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else {
            mPaint.setTextSize(mTextSize);
            mPaint.getTextBounds(mText, 0, mText.length(), mBound);
            height = getPaddingTop() + mBound.height() + getPaddingBottom();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.YELLOW);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        mPaint.setColor(mTextColor);
        canvas.drawText(mText, getWidth() / 2 - mBound.width() / 2, getHeight() / 2 + mBound.height() / 2, mPaint);
    }
}
