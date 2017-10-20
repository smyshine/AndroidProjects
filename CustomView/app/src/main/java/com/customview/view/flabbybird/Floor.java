package com.customview.view.flabbybird;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by SMY on 2017/10/20.
 */

public class Floor {
    private static final float FLOOR_Y_POS_RADIO = 5 / 6f;

    private int x, y;
    private BitmapShader mFloorShader;
    private int mWidth, mHeight;

    public Floor(int width, int height, Bitmap bitmap) {
        this.mWidth = width;
        this.mHeight = height;
        y = (int) (height * FLOOR_Y_POS_RADIO);
        this.mFloorShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
    }

    public void draw(Canvas canvas, Paint paint){
        if (-x > mWidth){
            x = x % mWidth;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(x, y);
        paint.setShader(mFloorShader);
        canvas.drawRect(x, 0, -x + mWidth, mHeight - y, paint);
        canvas.restore();
        paint.setShader(null);
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
}
