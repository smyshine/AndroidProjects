package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.customview.R;

/**
 * Created by SMY on 2017/10/11.
 */

public class CustomViewChangeColorIcon extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;

    private int mColor = 0x00b4ff;
    private float mAlpha = 0f;
    private Bitmap mIconBitmap;
    private Rect mIconRect;

    private String mText = "smy";
    private int mTextSize = 20;
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();

    public CustomViewChangeColorIcon(Context context){
        super(context);
    }

    public CustomViewChangeColorIcon(Context context, AttributeSet attributeSet){
        super(context, attributeSet);

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ChangeColorIconView);
        mIconBitmap = ((BitmapDrawable) typedArray.getDrawable(R.styleable.ChangeColorIconView_icon)).getBitmap();
        mColor = typedArray.getColor(R.styleable.ChangeColorIconView_color, 0x00b4ff);
        mText = typedArray.getString(R.styleable.ChangeColorIconView_text);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.ChangeColorIconView_text_size, 30);
        typedArray.recycle();

        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(0xff555555);
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int bitmapWidth = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mTextRect.height());

        int left = getMeasuredWidth() / 2 - bitmapWidth / 2;
        int top = getMeasuredHeight() / 2 - mTextRect.height() / 2 - bitmapWidth / 2;

        mIconRect = new Rect(left, top, left + bitmapWidth, top + bitmapWidth);
    }

    private void setTargetBitmap(int alpha){
        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setAlpha(alpha);
        mCanvas.drawRect(mIconRect, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setAlpha(255);
        mCanvas.drawBitmap(mIconBitmap, null, mIconRect, mPaint);
    }

    private void drawSourceText(Canvas canvas, int alpha){
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(0xff333333);
        mTextPaint.setAlpha(255-alpha);
        canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2 - mTextRect.width() / 2,
                mIconRect.bottom + mTextRect.height(), mTextPaint);
    }

    private void drawTargetText(Canvas canvas, int alpha){
        mTextPaint.setColor(mColor);
        mTextPaint.setAlpha(alpha);
        canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2 - mTextRect.width() / 2,
                mIconRect.bottom + mTextRect.height(), mTextPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = (int) Math.ceil(255 * mAlpha);
        canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
        setTargetBitmap(alpha);
        drawSourceText(canvas, alpha);
        drawTargetText(canvas, alpha);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void invalidateView(){
        if (Looper.getMainLooper() == Looper.myLooper()){
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void setIconAlpha(float alpha){
        this.mAlpha = alpha;
        invalidateView();
    }
}
