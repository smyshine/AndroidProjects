package com.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.customview.R;

/**
 * Created by SMY on 2017/10/19.
 */

public class CustomCircleMenuLayout extends ViewGroup {

    private int mRadius;
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;
    private static final float RADIO_DEFAUL_CENTER_ITEM_DIMENSION = 1 / 3f;
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;
    private static final int FINGABLE_VALUE = 300;
    private static final int NOCLICK_VALUE = 3;

    private int mFlingableValue = FINGABLE_VALUE;
    private float mPadding;
    private double mStartAngle = 0;
    private float mTmpAngle;
    private long mDownTime;
    private boolean isFling;

    private float mLastX, mLastY;
    private AutoFlingRunnable mFlingRunnable;

    private int[] mItemImages;
    private String[] mItemTexts;
    private int mMenuItemCount;
    private onMenuItemClickListener mMenuItemClickListener;

    public CustomCircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 0;
        int resHeight = 0;

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY){
            resWidth = getSuggestedMinimumWidth();
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            resWidth = resHeight = Math.min(width, height);
        }

        setMeasuredDimension(resWidth, resHeight);

        mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());

        final int count = getChildCount();
        int childSize = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        int childMode = MeasureSpec.EXACTLY;

        for (int i = 0; i < count; ++i){
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE){
                continue;
            }
            int makeMeasureSpec = -1;
            if (child.getId() == R.id.id_circle_menu_item_center){
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mRadius * RADIO_DEFAUL_CENTER_ITEM_DIMENSION), childMode);
            } else {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, childMode);
            }
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
        mPadding = RADIO_PADDING_LAYOUT * mRadius;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutRadius = mRadius;
        final int childCount = getChildCount();

        int left, top;
        int cWidth = (int) (layoutRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        float angleDelay = 360 / (childCount - 1);

        for (int i = 0; i < childCount; ++i){
            View child = getChildAt(i);
            if (child.getId() == R.id.id_circle_menu_item_center){
                continue;
            }
            mStartAngle %= 360;
            float tmp = layoutRadius / 2f - cWidth / 2 - mPadding;
            left = layoutRadius / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            top = layoutRadius / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            child.layout(left, top, left +cWidth, top + cWidth);
            mStartAngle += angleDelay;
        }

        View centerView = findViewById(R.id.id_circle_menu_item_center);
        if (centerView != null){
            centerView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMenuItemClickListener != null){
                        mMenuItemClickListener.itemCenterClick(v);
                    }
                }
            });
        }
        int centerLeft = layoutRadius / 2 - centerView.getMeasuredWidth() / 2;
        int centerRight = centerLeft + centerView.getMeasuredWidth();
        centerView.layout(centerLeft, centerLeft, centerRight, centerRight);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                if (isFling){
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float start = getAngle(mLastX, mLastY);
                float end = getAngle(x, y);
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4){
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else {
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }
                requestLayout();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                float anglePerSecond = mTmpAngle * 1000 / (System.currentTimeMillis() - mDownTime);

                if (Math.abs(anglePerSecond) > mFlingableValue && !isFling){
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));
                    return true;
                }
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE){
                    return true;
                }

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private int getDefaultWidth(){
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return Math.min(metrics.widthPixels, metrics.heightPixels);
    }

    private float getAngle(float xTouch, float yTouch){
        double x = xTouch - (mRadius / 2d);
        double y = yTouch - (mRadius / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 100 / Math.PI);
    }

    private int getQuadrant(float x, float y){
        int tmpX = (int) (x - mRadius / 2);
        int tmpY = (int) (y - mRadius / 2);
        if (tmpX >= 0){
            return tmpY >= 0 ? 4 : 1;
        }
        return tmpY >= 0 ? 3 : 2;
    }

    public void setMenuItemIconsAndTexts(int[] resIds, String[] texts){
        mItemImages = resIds;
        mItemTexts = texts;
        if (resIds == null && texts == null){
            throw new IllegalArgumentException("At least one of image and text should be set");
        }
        mMenuItemCount = resIds == null ? texts.length : resIds.length;

        if (resIds != null && texts != null){
            mMenuItemCount = Math.min(resIds.length, texts.length);
        }
        addMenuItems();
    }

    private void addMenuItems(){
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < mMenuItemCount; ++i){
            final int j = i;
            View view = inflater.inflate(R.layout.circle_menu_layout, this, false);
            ImageView iv = (ImageView) view.findViewById(R.id.circle_menu_item_image);
            TextView tv = (TextView) view.findViewById(R.id.circle_menu_item_text);

            if (iv != null){
                iv.setVisibility(VISIBLE);
                iv.setImageResource(mItemImages[i]);
                iv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMenuItemClickListener != null){
                            mMenuItemClickListener.itemClick(v, j);
                        }
                    }
                });
            }
            if (tv != null){
                tv.setVisibility(VISIBLE);
                tv.setText(mItemTexts[i]);
            }

            addView(view);
        }
    }

    public interface onMenuItemClickListener {
        void itemClick(View v, int postion);
        void itemCenterClick(View view);
    }

    public void setOnMenuItemClickListener(onMenuItemClickListener listener){
        this.mMenuItemClickListener = listener;
    }


    private class AutoFlingRunnable implements Runnable{

        private float anglePerSecond;

        public AutoFlingRunnable(float velocity){
            this.anglePerSecond = velocity;
        }

        @Override
        public void run() {
            if ((int) Math.abs(anglePerSecond) < 20){
                isFling = false;
                return;
            }
            isFling = true;
            mStartAngle += (anglePerSecond / 30);
            anglePerSecond /= 1.0666f;
            postDelayed(this, 30);
            requestLayout();
        }
    }
}
