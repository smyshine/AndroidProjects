package com.customview.view.flabbybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Random;

/**
 * Created by SMY on 2017/10/20.
 */

public class Pipe {
    private static final float RADIO_BETWEEN_UP_DOWN = 1 / 5f;
    private static final float RADIO_MAX_HEIGHT = 2 / 5f;
    private static final float RADIO_MIN_HEIGHT = 1 / 5f;

    private int x;
    private int height;
    private int margin;
    private Bitmap mTop;
    private Bitmap mBottom;
    private static Random random = new Random();

    public Pipe(Context context, int width, int height, Bitmap top, Bitmap bottom) {
        margin = (int) (height * RADIO_BETWEEN_UP_DOWN);
        x = width;
        this.mTop = top;
        this.mBottom = bottom;
        RandomHeight(height);
    }

    private void RandomHeight(int gameHeight){
        height = random.nextInt((int) (gameHeight * (RADIO_MAX_HEIGHT - RADIO_MIN_HEIGHT)));
        height = (int) (height + gameHeight * RADIO_MIN_HEIGHT);
    }

    public void draw(Canvas canvas, RectF rectF){
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(x, -(rectF.bottom - height));
        canvas.drawBitmap(mTop, null, rectF, null);
        canvas.translate(0, rectF.bottom + margin);
        canvas.drawBitmap(mBottom, null, rectF, null);
        canvas.restore();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public boolean touchBird(Bird bird){
        return bird.getX() + bird.getmWidth() > x && (bird.getY() < height || bird.getY() + bird.getmHeight() > height + margin);
    }
}
