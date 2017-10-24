package com.customview.view.game2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SMY on 2017/10/24.
 */

public class Game2048Item extends View {

    private int mNumber;
    private String mNumberVal;
    private Paint mPaint;
    private Rect mBound;

    public Game2048Item(Context context) {
        this(context, null);
    }

    public Game2048Item(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Game2048Item(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
    }

    public int getmNumber() {
        return mNumber;
    }

    public void setmNumber(int mNumber) {
        this.mNumber = mNumber;
        this.mNumberVal = mNumber + "";
        mPaint.setTextSize(30.0f);
        mBound = new Rect();
        mPaint.getTextBounds(mNumberVal, 0, mNumberVal.length(), mBound);
        invalidate();
    }

    private void drawText(Canvas canvas){
        mPaint.setColor(Color.BLACK);
        float x = (getWidth() - mBound.width()) / 2;
        float y = (getHeight() + mBound.height()) / 2;
        canvas.drawText(mNumberVal, x, y, mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String bgColor = "#000000";
        switch (mNumber){
            case 0:
                bgColor = "#ccc0b3";
                break;
            case 2:
                bgColor = "#EEE4DA";
                break;
            case 4:
                bgColor = "#EDE0C8";
                break;
            case 8:
                bgColor = "#F2B179";
                break;
            case 16:
                bgColor = "#F49563";
                break;
            case 32:
                bgColor = "#F5794D";
                break;
            case 64:
                bgColor = "#F55D37";
                break;
            case 128:
                bgColor = "#EEE863";
                break;
            case 256:
                bgColor = "#EDB04D";
                break;
            case 512:
                bgColor = "#ECB04D";
                break;
            case 1024:
                bgColor = "#EB9437";
                break;
            case 2048:
                bgColor = "#EA7821";
                break;
            case 4096:
                bgColor = "#E97023";
                break;
            default:
                bgColor = "#ccc0b3";
                break;
        }
        mPaint.setColor(Color.parseColor(bgColor));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        if (mNumber != 0){
            drawText(canvas);
        }
    }
}
