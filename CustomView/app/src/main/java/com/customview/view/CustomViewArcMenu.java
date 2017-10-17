package com.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.customview.R;

/**
 * Created by SMY on 2017/10/17.
 */

public class CustomViewArcMenu extends ViewGroup implements View.OnClickListener{

    private Position mPostion = Position.LEFT_TOP;

    private int mRadius = 100;
    private View mButton;
    private Status mCurrentStatus = Status.CLOSE;
    private OnMenuItemClickListener onMenuItemClickListener;


    public enum Status {
        OPEN, CLOSE
    }

    public enum Position {
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    public CustomViewArcMenu(Context context) {
        this(context, null);
    }

    public CustomViewArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomViewArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomViewArcMenu, defStyleAttr, 0);
        setPosition(typedArray.getInt(R.styleable.CustomViewArcMenu_position, 0));
        mRadius = typedArray.getDimensionPixelSize(R.styleable.CustomViewArcMenu_radius, mRadius);
        typedArray.recycle();
    }

    private void setPosition(int pos){
        switch (pos){
            case 0:
                mPostion = Position.LEFT_TOP;
                break;
            case 1:
                mPostion = Position.RIGHT_TOP;
                break;
            case 2:
                mPostion = Position.LEFT_BOTTOM;
                break;
            case 3:
                mPostion = Position.RIGHT_BOTTOM;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; ++i){
            getChildAt(i).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            layoutButton();
            int count = getChildCount();
            for (int i = 0; i < count - 1; ++i){
                View child = getChildAt(i + 1);
                child.setVisibility(GONE);

                int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
                int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));
                int witdh = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                if (mPostion == Position.LEFT_BOTTOM || mPostion == Position.RIGHT_BOTTOM){
                    ct = getMeasuredHeight() - height - ct;
                }
                if (mPostion == Position.RIGHT_TOP || mPostion == Position.RIGHT_BOTTOM){
                    cl = getMeasuredWidth() - witdh - cl;
                }
                child.layout(cl, ct, cl + witdh, ct + height);
            }
        }
    }

    @Override
    public void onClick(View v) {
        mButton = findViewById(R.id.id_button);
        if (mButton == null){
            mButton = getChildAt(0);
        }
        rotateView(mButton, 0f, 270f, 300);
        toggleMenu(300);
    }

    private void layoutButton(){
        View cButton = getChildAt(0);
        cButton.setOnClickListener(this);

        int l = 0, t = 0;
        int width = cButton.getMeasuredWidth();
        int height = cButton.getMeasuredHeight();
        switch (mPostion){
            case LEFT_TOP:
                l = 0;
                t = 0;
                break;
            case LEFT_BOTTOM:
                l = 0;
                t = getMeasuredHeight() - height;
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - width;
                t = 0;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() - height;
                break;
        }
        cButton.layout(l, t, l + width, t + height);
    }

    private void rotateView(View view, float from, float to, int duration){
        RotateAnimation animation = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    private void toggleMenu(int duration){
        int count = getChildCount();
        for (int i = 0; i < count - 1; ++i){
            final View childView = getChildAt(i + 1);
            childView.setVisibility(VISIBLE);
            int xFlag = 1, yFlag = 1;
            if (mPostion == Position.LEFT_TOP || mPostion == Position.LEFT_BOTTOM){
                xFlag = -1;
            }
            if (mPostion == Position.LEFT_TOP || mPostion == Position.RIGHT_TOP){
                yFlag = -1;
            }
            int l = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
            int t = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));

            AnimationSet animationSet = new AnimationSet(true);
            Animation animation;
            if (mCurrentStatus == Status.CLOSE){
                animationSet.setInterpolator(new OvershootInterpolator(2f));
                animation = new TranslateAnimation(xFlag * l, 0, yFlag * t, 0);
                childView.setClickable(true);
                childView.setFocusable(true);
            } else {
                animation = new TranslateAnimation(0, xFlag * l, 0, yFlag * t);
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCurrentStatus == Status.CLOSE){
                        childView.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setFillAfter(true);
            animation.setDuration(duration);
            animation.setStartOffset((i * 100) / (count - 1));
            RotateAnimation rotateAnimation = new RotateAnimation(0, 720,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(duration);
            rotateAnimation.setFillAfter(true);
            animationSet.addAnimation(rotateAnimation);
            animationSet.addAnimation(animation);
            childView.startAnimation(animationSet);
            final int index = i + 1;
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onMenuItemClickListener != null){
                        onMenuItemClickListener.onClick(childView, index - 1);
                    }
                    menuItemAnimation(index - 1);
                    changeStatus();
                }
            });
        }

        changeStatus();
    }

    private void menuItemAnimation(int pos){
        for (int i = 0; i < getChildCount() - 1; ++i){
            View child = getChildAt(i + 1);
            if (i == pos){
                child.startAnimation(scaleBig(300));
            } else {
                child.startAnimation(scaleSmall(300));
            }
            child.setClickable(false);
            child.setFocusable(false);
        }
    }

    private Animation scaleBig(int duration){
        AnimationSet animationSet = new AnimationSet(true);
        Animation scale = new ScaleAnimation(1.0f, 4.0f, 1.0f, 4.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alpha = new AlphaAnimation(1, 0);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(alpha);
        animationSet.setDuration(duration);
        animationSet.setFillAfter(true);
        return animationSet;
    }

    private Animation scaleSmall(int duration){
        Animation scale = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(duration);
        scale.setFillAfter(true);
        return scale;
    }

    private void changeStatus(){
        mCurrentStatus = mCurrentStatus == Status.CLOSE ? Status.OPEN : Status.CLOSE;
    }

    public interface OnMenuItemClickListener {
        void onClick(View view, int pos);
    }

    public void setItemClickListener(OnMenuItemClickListener listener){
        this.onMenuItemClickListener = listener;
    }
}
