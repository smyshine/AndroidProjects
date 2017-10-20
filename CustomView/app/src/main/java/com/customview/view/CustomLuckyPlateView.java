package com.customview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.customview.R;

/**
 * Created by SMY on 2017/10/20.
 */

public class CustomLuckyPlateView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "CustomLuckyPlateView";

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Thread mThread;
    private boolean isRunning;
    private String[] mTitles = new String[]{
            "Mini", "GoodLuck", "Phone", "Pad", "ComeOn", "Beauty"
    };
    private int[] mColors = new int[]{0x7F00b4ff, 0x7F00d456, 0x7F00b4ff,
            0x7F00d456, 0x7F00b4ff, 0x7F00d456};
    private int[] mImages = new int[]{R.drawable.community_like_nor, R.drawable.icon_share,
            R.drawable.community_like_hl, R.drawable.community_live_nor, R.drawable.preview_mode_panorama, R.drawable.community_live_hl};
    private Bitmap[] mBitmaps;

    private int mItemCount = mTitles.length;
    private RectF mRange = new RectF();
    private int mRadius;
    private Paint mArcPaint;
    private Paint mTextPaint;
    private double mSpeed;
    private volatile float mStartAngle = 0;
    private boolean shouldEnd;
    private int mCenter;
    private int mPadding;
    private Bitmap mBitmapBg = BitmapFactory.decodeResource(getResources(), R.drawable.profile_bg);
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

    public CustomLuckyPlateView(Context context) {
        this(context, null);
    }

    public CustomLuckyPlateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = width - getPaddingLeft() - getPaddingRight();
        mPadding = getPaddingLeft();
        mCenter = width / 2;
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffff);
        mTextPaint.setTextSize(mTextSize);

        mRange = new RectF(getPaddingLeft(), getPaddingLeft(), mRadius + getPaddingLeft(), mRadius + getPaddingLeft());
        mBitmaps = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; ++i){
            mBitmaps[i] = BitmapFactory.decodeResource(getResources(), mImages[i]);
        }
        isRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning){
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw(){
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null){
                drawBackground();
                float tmpAngle = mStartAngle;
                float sweepAngle = (float) (360 / mItemCount);
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);
                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);
                    drawText(tmpAngle, sweepAngle, mTitles[i]);
                    drawIcon(tmpAngle, i);
                    tmpAngle += sweepAngle;
                }
                mStartAngle += mSpeed;

                if (shouldEnd){
                    mSpeed -= 1;
                }
                if (mSpeed <= 0){
                    mSpeed = 0;
                    shouldEnd = false;
                }
                calculateInExactArea(mStartAngle);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawBackground(){
        mCanvas.drawColor(0xffffff);
        mCanvas.drawBitmap(mBitmapBg, null, new Rect(mPadding / 2, mPadding / 2,
                getMeasuredWidth() - mPadding / 2, getMeasuredWidth() - mPadding / 2), null);
    }

    private void drawText(float angle, float sweep, String text){
        Path path = new Path();
        path.addArc(mRange, angle, sweep);
        float textWidth = mTextPaint.measureText(text);
        float hOffset = (float) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        float vOffset = mRadius / 2 / 6;
        mCanvas.drawTextOnPath(text, path, hOffset, vOffset, mTextPaint);
    }

    private void drawIcon(float start, int i){
        int imgWidth = mRadius / 8;
        float angle = (float) ((30 + start) * (Math.PI / 180));
        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(mBitmaps[i], null, rect, null);
    }

    private void calculateInExactArea(float angle){
        if (mSpeed == 0){
            if (mListener != null){
                mListener.onResult(lastChoosenText);
            }
            return;
        }
        float rotate = angle + 90;
        rotate %= 360.0;
        for (int i = 0; i < mItemCount; i++) {
            float from = 360 - (i + 1) * (360 / mItemCount);
            float to = from + 360 - i * (360 / mItemCount);
            if (rotate > from && rotate < to){
                //Toast.makeText(getContext(), mTitles[i], Toast.LENGTH_SHORT).show();
                Log.d(TAG, "calculateInExactArea: " + mTitles[i]);
                lastChoosenText = mTitles[i];
                return;
            }
        }
    }

    public void luckyStart(int index){
        float angle = (float) (360 / mItemCount);
        float from = 270 - (index + 1) * angle;
        float to = from + angle;
        float targetFrom = 4 * 360 + from;
        float v1 = (float) (Math.sqrt(1 + 8 * targetFrom) - 1) / 2;
        float targetTo = 4 * 360 + to;
        float v2 = (float) (Math.sqrt(1 + 8 * targetTo) - 1) / 2;

        mSpeed = (float) (v1 + Math.random() * (v2 - v1));
        shouldEnd = false;
    }

    public void luckyEnd(){
        mStartAngle = 0;
        shouldEnd = true;
    }

    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return shouldEnd;
    }

    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }

    public interface LuckPlateResultListener{
        void onResult(String result);
    }

    private LuckPlateResultListener mListener;

    public void setResultListener(LuckPlateResultListener listener){
        this.mListener = listener;
    }

    private String lastChoosenText = "GoodLuck";
}
