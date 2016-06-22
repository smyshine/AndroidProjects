package com.example.smy.progressbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by SMY on 2016/6/21.
 */
public class PowerImageView2 extends ImageView{

    private Movie mMovie;

    private long mMovieStart;

    private int mImageWidth;

    private int mImageHeight;


    public PowerImageView2(Context context) {
        super(context);
    }

    public PowerImageView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerImageView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 当资源id不等于0时，就去获取该资源的流
        InputStream is = getResources().openRawResource(R.raw.loudou);
        // 使用Movie类对流进行解码
        mMovie = Movie.decodeStream(is);
        if (mMovie != null) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mImageWidth = bitmap.getWidth();
            mImageHeight = bitmap.getHeight();
            bitmap.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            // mMovie等于null，说明是张普通的图片，则直接调用父类的onDraw()方法
            super.onDraw(canvas);
        } else {
            // mMovie不等于null，说明是张GIF图片
            // 如果允许自动播放，就调用playMovie()方法播放GIF动画
            playMovie(canvas);
            invalidate();

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMovie != null) {
            // 如果是GIF图片则重写设定PowerImageView的大小
            setMeasuredDimension(mImageWidth, mImageHeight);
        }
    }

    /**
     * 开始播放GIF动画，播放完成返回true，未完成返回false。
     *
     * @param canvas
     * @return 播放完成返回true，未完成返回false。
     */
    private boolean playMovie(Canvas canvas) {
        long now = SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int duration = mMovie.duration();
        if (duration == 0) {
            duration = 1000;
        }
        int relTime = (int) ((now - mMovieStart) % duration);
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        if ((now - mMovieStart) >= duration) {
            mMovieStart = 0;
            return true;
        }
        return false;
    }
}
