package com.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.customview.R;

/**
 * Created by SMY on 2017/12/18.
 */

public class StickyNavLayout extends LinearLayout implements NestedScrollingParent {

    private static final String TAG = "StickyNavLayout";

    private View mTop;
    private View mNav;
    private ViewPager mViewPager;

    private int mTopViewHeight;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private ValueAnimator mOffsetAnimator;
    private Interpolator mInterpolator;
    private int mTouchSlop;
    private int mMaximumVelocity, mMininumVelocity;
    private float mLastY;
    private boolean mDragging;

    public StickyNavLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mMininumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    private void initVelocityTracker(){
        if (mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker(){
        if (mVelocityTracker != null){
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTop = findViewById(R.id.id_stickynavlayout_topview);
        mNav = findViewById(R.id.id_stickynavlayout_indicator);
        View view = findViewById(R.id.id_stickynavlayout_viewpager);
        if (! (view instanceof ViewPager)){
            throw new RuntimeException("id_viewpager show should be used by ViewPager!");
        }
        mViewPager = (ViewPager) view;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        getChildAt(0).measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = getMeasuredHeight() - mNav.getMeasuredHeight();
        setMeasuredDimension(getMeasuredWidth(), mTop.getMeasuredHeight() + mNav.getMeasuredHeight() + mViewPager.getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTopViewHeight = mTop.getMeasuredHeight();
    }

    public void fling(int velocity){
        mScroller.fling(0, getScrollY(), 0, velocity, 0, 0, 0, mTopViewHeight);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (y < 0){
            y = 0;
        }
        if (y > mTopViewHeight){
            y = mTopViewHeight;
        }
        if (y != getScrollY()){
            super.scrollTo(x, y);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()){
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.d(TAG, "onStartNestedScroll: ");
        return true;
//        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        Log.d(TAG, "onNestedScrollAccepted: ");
//        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        Log.d(TAG, "onStopNestedScroll: ");
//        super.onStopNestedScroll(child);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.d(TAG, "onNestedScroll: ");
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d(TAG, "onNestedPreScroll: ");
        boolean hideTop = dy > 0 && getScrollY() < mTopViewHeight;
        boolean showTop = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hideTop || showTop){
            scrollBy(0, dy);
            consumed[1] = dy;
        }

//        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    private int TOP_CHILD_FLING_THRESHOLD = 3;
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.d(TAG, "onNestedFling: ");
        if (target instanceof RecyclerView && velocityY < 0){
            RecyclerView recyclerView = (RecyclerView) target;
            View firstChild = recyclerView.getChildAt(0);
            int childAdapterPostion = recyclerView.getChildAdapterPosition(firstChild);
            consumed = childAdapterPostion > TOP_CHILD_FLING_THRESHOLD;
        }
        if (!consumed){
            animateScroll(velocityY, computeDuration(0), consumed);
        } else {
            animateScroll(velocityY, computeDuration(velocityY), consumed);
        }

        return true;

//        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.d(TAG, "onNestedPreFling: ");
        return false;
//        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        Log.d(TAG, "getNestedScrollAxes: ");
        return 0;
//        return super.getNestedScrollAxes();
    }

    private int computeDuration(float velocityY){
        int distance ;
        if (velocityY > 0){
            distance = Math.abs(mTop.getHeight() - getScrollY());
        } else {
            distance = Math.abs(mTop.getHeight() - (mTop.getHeight() - getScrollY()));
        }

        int duration ;
        velocityY = Math.abs(velocityY);
        if (velocityY > 0){
            duration = 3 * Math.round(1000 * (distance / velocityY));
        } else {
            float distanceRatio = (float) distance / getHeight();
            duration = (int) (distanceRatio + 1) * 150;
        }
        return duration;
    }

    private void animateScroll(float velocityY, final int duration, boolean consumed){
        int currentOffset = getScrollY();
        int topHeight = mTop.getHeight();
        if (mOffsetAnimator == null){
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.setInterpolator(mInterpolator);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer){
                        scrollTo(0, (Integer) animation.getAnimatedValue());
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(Math.min(duration, 600));

        if (velocityY >= 0){
            mOffsetAnimator.setIntValues(currentOffset, topHeight);
            mOffsetAnimator.start();
        } else if (!consumed){
            mOffsetAnimator.setIntValues(currentOffset, 0);
            mOffsetAnimator.start();
        }
    }
}
