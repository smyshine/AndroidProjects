package com.customview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by SMY on 2017/10/26.
 */

public class ClipImageBorderView extends View {

    private int mHorizontalPadding = 40;
    private int mVerticalPadding;
    private int mWidth;
    private int mBorderColor = Color.parseColor("#ffffff");
    private int mBorderWidth = 1;
    private Paint mPaint;

    public ClipImageBorderView(Context context) {
        this(context, null);
    }

    public ClipImageBorderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipImageBorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources().getDisplayMetrics());
        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources().getDisplayMetrics());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth() - 2 * mHorizontalPadding;
        mVerticalPadding = (getHeight() - mWidth) / 2;

        mPaint.setColor(Color.parseColor("#aa000000"));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0,
                mHorizontalPadding, getHeight(), mPaint);
        canvas.drawRect(mHorizontalPadding, 0,
                getWidth() - mHorizontalPadding, mVerticalPadding, mPaint);
        canvas.drawRect(getWidth() - mHorizontalPadding, 0,
                getWidth(), getHeight(), mPaint);
        canvas.drawRect(mHorizontalPadding, getHeight() - mVerticalPadding,
                mHorizontalPadding + mWidth, getHeight(), mPaint);

        mPaint.setColor(mBorderColor);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mHorizontalPadding, mVerticalPadding,
                mHorizontalPadding + mWidth, mVerticalPadding + mWidth, mPaint);
    }
}
