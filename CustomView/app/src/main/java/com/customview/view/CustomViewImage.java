package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.customview.R;

/**
 * Created by SMY on 2017/10/10.
 */

public class CustomViewImage extends View {

    private String mText;
    private int mTextColor;
    private int mTextSize;
    private Bitmap mImage;
    private int mImageScale;

    private int mWidth, mHeight;

    private Rect mTextBound;
    private Paint mPaint;
    private Rect mRect;

    public CustomViewImage(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public CustomViewImage(Context context){
        this(context, null);
    }

    public CustomViewImage(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewImage, defStyle, 0);
        mText = typedArray.getString(R.styleable.CustomViewImage_titleText);
        mTextColor = typedArray.getColor(R.styleable.CustomViewImage_titleTextColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.CustomViewImage_titleTextSize, 36);
        mImage = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.CustomViewImage_image, 0));
        mImageScale = typedArray.getInt(R.styleable.CustomViewImage_imageScaleType, 0);
        typedArray.recycle();

        //获取绘制文本的宽高
        mPaint = new Paint();
        mRect = new Rect();
        mPaint.setTextSize(mTextSize);
        mTextBound = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY){
            mWidth = specSize;
        } else {
            int desireByImg = getPaddingLeft() + getPaddingRight() + mImage.getWidth();
            int desireByText = getPaddingLeft() + getPaddingRight() + mTextBound.width();
            if (specMode == MeasureSpec.AT_MOST){
                mWidth = Math.min(Math.max(desireByImg, desireByText), specSize);
            }
        }

        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY){
            mHeight = specSize;
        } else {
            int desireByImg = getPaddingTop() + getPaddingBottom() + mImage.getHeight();
            int desireByText = getPaddingTop() + getPaddingBottom() + mTextBound.height();
            if (specMode == MeasureSpec.AT_MOST){
                mHeight = Math.min(Math.max(desireByImg, desireByText), specSize);
            }
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.YELLOW);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);

        mRect.left = getPaddingLeft();
        mRect.right = mWidth - getPaddingRight();
        mRect.top = getPaddingTop();
        mRect.bottom = mHeight - getPaddingBottom();

        mPaint.setColor(mTextColor);
        mPaint.setStyle(Paint.Style.FILL);

        if (mTextBound.width() > mWidth){
            TextPaint paint = new TextPaint(mPaint);
            String msg = TextUtils.ellipsize(mText, paint, (float) mWidth - getPaddingLeft() - getPaddingRight(),
                    TextUtils.TruncateAt.END).toString();
            canvas.drawText(msg, getPaddingLeft(), mHeight - getPaddingBottom(), mPaint);
        } else {
            canvas.drawText(mText, mWidth / 2 - mTextBound.width() * 1.0f / 2, mHeight - getPaddingBottom(), mPaint);
        }

        mRect.bottom -= mTextBound.height();

        if (mImageScale == 0){//fit xy
            canvas.drawBitmap(mImage, null, mRect, mPaint);
        } else {
            mRect.left = mWidth / 2 - mImage.getWidth() / 2;
            mRect.right = mWidth / 2 + mImage.getWidth() / 2;
            mRect.top = (mHeight - mTextBound.height()) / 2 - mImage.getHeight() / 2;
            mRect.bottom = (mHeight - mTextBound.height()) / 2 + mImage.getHeight() / 2;
            canvas.drawBitmap(mImage, null, mRect, mPaint);
        }
    }
}
