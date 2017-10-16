package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.customview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2017/10/16.
 */

public class CustomViewGroupGestureLock extends RelativeLayout {

    private GestureLockView[] mGestureLockViews;

    private int mCount = 3;
    private int[] mAnswer = {0,1,2,5,8};

    private List<Integer> mChoose = new ArrayList<>();
    private Paint mPaint;

    private int mMarginBetweenLockView = 30;
    private int mGestureLockViewWidth;

    private int mInnerCircleColor = 0xff939090;
    private int mOuterCircleColor = 0xffe0dbdb;
    private int mFingerOnColor = 0xff378fc9;
    private int mFingerUpColor = 0xffff0000;

    private int mWidth, mHeight;
    private Path mPath;
    private int mLastPathX, mLastPathY;
    private Point mTmpTarget = new Point();

    private int mRetryCount = 4;

    private GestureLockViewListener mListener;

    public CustomViewGroupGestureLock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomViewGroupGestureLock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomGestureLockViewGroup, defStyleAttr, 0);
        mInnerCircleColor = typedArray.getColor(R.styleable.CustomGestureLockViewGroup_color_inner_circle, mInnerCircleColor);
        mOuterCircleColor = typedArray.getColor(R.styleable.CustomGestureLockViewGroup_color_outer_circle, mOuterCircleColor);
        mFingerOnColor = typedArray.getColor(R.styleable.CustomGestureLockViewGroup_color_finger_on, mFingerOnColor);
        mFingerUpColor = typedArray.getColor(R.styleable.CustomGestureLockViewGroup_color_finger_up, mFingerUpColor);
        mCount = typedArray.getInt(R.styleable.CustomGestureLockViewGroup_count, mCount);
        mRetryCount = typedArray.getInt(R.styleable.CustomGestureLockViewGroup_tryCount, mRetryCount);
        typedArray.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = mWidth = Math.min(mWidth, mHeight);

        if (mGestureLockViews == null){
            mGestureLockViews = new GestureLockView[mCount * mCount];
            mGestureLockViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));
            mMarginBetweenLockView = (int) (mGestureLockViewWidth * 0.25);
            mPaint.setStrokeWidth(mGestureLockViewWidth * 0.29f);

            for (int i = 0; i < mGestureLockViews.length; ++i){
                mGestureLockViews[i] = new GestureLockView(getContext(),
                        mInnerCircleColor, mOuterCircleColor, mFingerOnColor, mFingerUpColor);
                mGestureLockViews[i].setId(i + 1);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        mGestureLockViewWidth, mGestureLockViewWidth);

                //not the first of every line
                if (i % mCount != 0){
                    layoutParams.addRule(RelativeLayout.RIGHT_OF, mGestureLockViews[i - 1].getId());
                }
                if (i > mCount - 1){
                    layoutParams.addRule(RelativeLayout.BELOW, mGestureLockViews[i - mCount].getId());
                }
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMargin = 0;
                int topMargin = 0;

                if (i >= 0 && i < mCount){
                    topMargin = mMarginBetweenLockView;
                }
                if (i % mCount == 0){
                    leftMargin = mMarginBetweenLockView;
                }
                layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                mGestureLockViews[i].setMode(GestureLockView.Mode.MODE_NO_FINGER);
                addView(mGestureLockViews[i], layoutParams);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(50);
                GestureLockView view = getChildByPosition(x, y);
                if (view != null){
                    int id = view.getId();
                    if (!mChoose.contains(id)){
                        mChoose.add(id);
                        view.setMode(GestureLockView.Mode.MODE_FINGER_ON);
                        if (mListener != null){
                            mListener.onBlockSelected(id);
                        }
                        mLastPathX = view.getLeft() / 2 + view.getRight() / 2;
                        mLastPathY = view.getTop() / 2 + view.getBottom() / 2;
                        if (mChoose.size() == 1){
                            mPath.moveTo(mLastPathX, mLastPathY);
                        } else {
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                    }
                }
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
                mPaint.setColor(mFingerUpColor);
                mPaint.setAlpha(50);
                --this.mRetryCount;
                if (mListener != null){
                    mListener.onGestureResult(checkAnswer());
                    if (this.mRetryCount == 0){
                        mListener.onUnmatchExceed();
                    }
                }

                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;
                changeItemMode();

                for (int i = 0; i < mChoose.size() - 1; ++i){
                    int id = mChoose.get(i);
                    int nextId = mChoose.get(i + 1);

                    GestureLockView start = (GestureLockView) findViewById(id);
                    GestureLockView end = (GestureLockView) findViewById(nextId);

                    int dx = end.getLeft() - start.getLeft();
                    int dy = end.getTop() - start.getTop();

                    int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
                    start.setArrowDegree(angle);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mPath != null){
            canvas.drawPath(mPath, mPaint);
        }
        if (mChoose.size() > 1){
            if (mLastPathX != 0 && mLastPathY != 0){
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x, mTmpTarget.y, mPaint);
            }
        }
    }

    private void changeItemMode(){
        for (GestureLockView view : mGestureLockViews){
            if (mChoose.contains(view.getId())){
                view.setMode(GestureLockView.Mode.MODE_FINGER_UP);
            }
        }
    }

    private void reset(){
        mChoose.clear();
        mPath.reset();
        for (GestureLockView view : mGestureLockViews){
            view.setMode(GestureLockView.Mode.MODE_NO_FINGER);
            view.setArrowDegree(-1);
        }
    }

    private boolean checkAnswer(){
        if (mAnswer.length != mChoose.size()){
            return false;
        }

        for (int i = 0; i < mAnswer.length; ++i){
            if (mAnswer[i] != mChoose.get(i)){
                return false;
            }
        }
        return true;
    }

    private boolean checkPositionInChild(View child, int x, int y){
        int padding = (int) (mGestureLockViewWidth * 0.15);

        return x >= child.getLeft() + padding && x <= child.getRight() - padding &&
                y >= child.getTop() + padding && y <= child.getBottom() - padding;
    }

    private GestureLockView getChildByPosition(int x, int y){
        for (GestureLockView view : mGestureLockViews){
            if (checkPositionInChild(view, x, y)){
                return view;
            }
        }

        return null;
    }

    public interface GestureLockViewListener {
        void onBlockSelected(int id);
        void onGestureResult(boolean matched);
        void onUnmatchExceed();
    }

    public void setGestureLockViewListener(GestureLockViewListener listener){
        this.mListener = listener;
    }

    public void setAnswer(int[] answer){
        this.mAnswer = answer;
    }

    public void setMaxRetryCount(int count){
        this.mRetryCount = count;
    }


}
