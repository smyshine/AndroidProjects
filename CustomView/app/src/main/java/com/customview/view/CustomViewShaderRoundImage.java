package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.customview.R;

/**
 * Created by SMY on 2017/10/18.
 */

public class CustomViewShaderRoundImage extends AppCompatImageView{

    private int type;
    private static final int TYPE_CIRCLE = 0;
    private static final int TYPE_ROUND = 1;

    private int mRadius;
    private Paint mBitmapPaint;
    private int mBorderRadius;
    private Matrix mMatrix;

    private BitmapShader mBitmapShader;

    private int mWidth;
    private RectF mRoundRect;

    public CustomViewShaderRoundImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewRoundImage);
        mBorderRadius = typedArray.getDimensionPixelSize(R.styleable.CustomViewRoundImage_radius, 10);
        type = typedArray.getInt(R.styleable.CustomViewRoundImage_type, TYPE_CIRCLE);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (type == TYPE_CIRCLE){
            mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
            mRadius = mWidth / 2;
            setMeasuredDimension(mWidth, mWidth);
        }
    }

    private void setUpShader(){
        Drawable drawable = getDrawable();
        if (drawable == null){
            return;
        }

        Bitmap bitmap = drawableToBitmap(drawable);
        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = 1.0f;
        if (type == TYPE_CIRCLE){
            int bSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
            scale = mWidth * 1.0f / bSize;
        } else if (type == TYPE_ROUND){
            scale = Math.max(getWidth() * 1.0f / bitmap.getWidth(),
                    getHeight() * 1.0f / bitmap.getHeight());
        }
        mMatrix.setScale(scale, scale);
        mBitmapShader.setLocalMatrix(mMatrix);
        mBitmapPaint.setShader(mBitmapShader);
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        if (drawable instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null){
            return;
        }
        setUpShader();

        if (type == TYPE_ROUND){
            canvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius, mBitmapPaint);
        } else {
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (type == TYPE_ROUND){
            mRoundRect = new RectF(0, 0, getWidth(), getHeight());
        }
    }

}
