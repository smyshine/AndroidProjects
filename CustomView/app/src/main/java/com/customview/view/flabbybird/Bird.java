package com.customview.view.flabbybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by SMY on 2017/10/20.
 */

public class Bird {
    private static final float RADIO_POS_HEIGHT = 2 / 3f;
    private static final int BIRD_SIZE = 30;

    private int x, y;
    private int mWidth, mHeight;

    private Bitmap bitmap;

    private RectF rect = new RectF();

    public Bird(Context context, int width, int height, Bitmap bitmap) {
        this.bitmap = bitmap;

        this.x = width / 2 - bitmap.getWidth() / 2;
        this.y = (int)(height * RADIO_POS_HEIGHT);
        this.mWidth = dp2px(context, BIRD_SIZE);
        this.mHeight = (int) (mWidth * 1.0f / bitmap.getWidth() * bitmap.getHeight());
    }

    public static int dp2px(Context context, int dp){
        return (int) context.getResources().getDisplayMetrics().density * dp;
    }

    public void draw(Canvas canvas){
        rect.set(x, y, x + mWidth, y + mHeight);
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }
}
