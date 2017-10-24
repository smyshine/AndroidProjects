package com.customview.view.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.customview.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by SMY on 2017/10/20.
 */

public class CustomGamePuzzleView extends RelativeLayout implements View.OnClickListener {

    private int mColumn = 3;
    private int mWidth;
    private int mPadding;
    private ImageView[] mGamePuzzleItems;
    private int mItemWidth;
    private int mMargin = 1;
    private Bitmap mBitmap;
    private List<ImagePiece> mItemBitmaps;
    private boolean once;

    public CustomGamePuzzleView(Context context) {
        this(context, null);
    }

    public CustomGamePuzzleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomGamePuzzleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMargin, getResources().getDisplayMetrics());
        mPadding = Math.min(Math.min(getPaddingLeft(), getPaddingRight()), Math.min(getPaddingTop(), getPaddingBottom()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
        if (!once){
            initBitmap();
            initItem();
        }
        once = true;
        setMeasuredDimension(mWidth, mWidth);
    }

    private void initBitmap(){
        if (mBitmap == null){
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.puzzle);
        }
        mItemBitmaps = ImageSplitter.split(mBitmap, mColumn);
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece o1, ImagePiece o2) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    private void initItem(){
        int childWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        mItemWidth = childWidth;
        mGamePuzzleItems = new ImageView[mColumn * mColumn];
        for (int i = 0; i < mGamePuzzleItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).bitmap);
            mGamePuzzleItems[i] = item;
            item.setId(i + 1);
            item.setTag(i + "_" + mItemBitmaps.get(i).index);

            RelativeLayout.LayoutParams layoutParams = new LayoutParams(mItemWidth, mItemWidth);
            if ((i + 1) % mColumn != 0){
                layoutParams.rightMargin = mMargin;
            }
            if (i % mColumn != 0){
                layoutParams.addRule(RelativeLayout.RIGHT_OF, mGamePuzzleItems[i - 1].getId());
            }
            if ((i + 1) > mColumn){
                layoutParams.topMargin = mMargin;
                layoutParams.addRule(RelativeLayout.BELOW, mGamePuzzleItems[i - mColumn].getId());
            }
            addView(item, layoutParams);
        }
    }

    private ImageView mFirst;
    private ImageView mSecond;

    @Override
    public void onClick(View v) {
        if (isAnimating){
            return;
        }

        if (mFirst == v){
            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }
        if (mFirst == null){
            mFirst = (ImageView) v;
            mFirst.setColorFilter(Color.parseColor("#22ee0000"));
        } else {
            mSecond = (ImageView) v;
            //exchangeView();
            exchangeViewWithAnimation();
        }
    }

    private void exchangeView(){
        mFirst.setColorFilter(null);
        String firstTag = (String) mFirst.getTag();
        String secondTag = (String) mSecond.getTag();
        String[] firstImageIndex = firstTag.split("_");
        String[] secondImageIndex = secondTag.split("_");
        mFirst.setImageBitmap(mItemBitmaps.get(Integer.parseInt(secondImageIndex[0])).bitmap);
        mSecond.setImageBitmap(mItemBitmaps.get(Integer.parseInt(firstImageIndex[0])).bitmap);
        mFirst.setTag(secondTag);
        mSecond.setTag(firstTag);
        mFirst = mSecond = null;
    }

    private boolean isAnimating;
    private RelativeLayout mAnimLayout;

    private void exchangeViewWithAnimation(){
        mFirst.setColorFilter(null);
        setUpAnimLayout();

        ImageView first = new ImageView(getContext());
        first.setImageBitmap(mItemBitmaps.get(getImageIndexByTag((String) mFirst.getTag())).bitmap);
        LayoutParams layoutParams = new LayoutParams(mItemWidth, mItemWidth);
        layoutParams.leftMargin = mFirst.getLeft() - mPadding;
        layoutParams.topMargin = mFirst.getTop() - mPadding;
        first.setLayoutParams(layoutParams);
        mAnimLayout.addView(first);

        ImageView second = new ImageView(getContext());
        second.setImageBitmap(mItemBitmaps.get(getImageIndexByTag((String) mSecond.getTag())).bitmap);
        LayoutParams layoutParams2 = new LayoutParams(mItemWidth, mItemWidth);
        layoutParams2.leftMargin = mSecond.getLeft() - mPadding;
        layoutParams2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(layoutParams2);
        mAnimLayout.addView(second);

        TranslateAnimation animation = new TranslateAnimation(0,
                mSecond.getLeft() - mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
        animation.setDuration(300);
        animation.setFillAfter(true);
        first.startAnimation(animation);

        TranslateAnimation animationSecond = new TranslateAnimation(0,
                mFirst.getLeft() - mSecond.getLeft(), 0, mFirst.getTop() - mSecond.getTop());
        animationSecond.setDuration(300);
        animationSecond.setFillAfter(true);
        second.startAnimation(animationSecond);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
                mFirst.setVisibility(INVISIBLE);
                mSecond.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String) mFirst.getTag();
                String secondTag = (String) mSecond.getTag();
                String[] firstImageIndex = firstTag.split("_");
                String[] secondImageIndex = secondTag.split("_");
                mFirst.setImageBitmap(mItemBitmaps.get(Integer.parseInt(secondImageIndex[0])).bitmap);
                mSecond.setImageBitmap(mItemBitmaps.get(Integer.parseInt(firstImageIndex[0])).bitmap);
                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);
                mFirst.setVisibility(VISIBLE);
                mSecond.setVisibility(VISIBLE);
                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();
                checkSuccess();
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void setUpAnimLayout(){
        if (mAnimLayout == null){
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    private int getImageIndexByTag(String tag){
        String[] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    private int getIndexByTag(String tag)
    {
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }

    private void checkSuccess(){
        boolean isSuccess = true;
        for (int i = 0; i < mGamePuzzleItems.length; i++) {
            ImageView first = mGamePuzzleItems[i];
            if (getIndexByTag((String) first.getTag()) != i){
                isSuccess = false;
            }
        }
        if (isSuccess){
            Toast.makeText(getContext(), "Success, Level Up!", Toast.LENGTH_SHORT).show();
            nextLevel();
        }
    }

    private void nextLevel(){
        this.removeAllViews();
        mAnimLayout = null;
        ++mColumn;
        initBitmap();
        initItem();
    }
}
