package com.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by SMY on 2017/10/10.
 */

public class CustomViewGroup extends ViewGroup {

    public CustomViewGroup(Context context){
        this(context, null, 0);
    }

    public CustomViewGroup(Context context, AttributeSet attributeSet){
        this(context, attributeSet, 0);
    }

    public CustomViewGroup(Context context, AttributeSet attributeSet, int def){
        super(context, attributeSet, def);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0, height = 0;
        int childCount = getChildCount();
        int childWidth = 0, childHeight = 0;
        MarginLayoutParams childParams = null;

        int lHeight = 0, rHeight = 0;
        int tWidth = 0, bWidth = 0;

        for (int i = 0; i < childCount; ++i){
            View childView = getChildAt(i);
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();
            childParams = (MarginLayoutParams) childView.getLayoutParams();
            if (i == 0 || i == 1){
                tWidth += childWidth + childParams.leftMargin + childParams.rightMargin;
            }
            if (i == 2 || i == 3){
                bWidth += childWidth + childParams.leftMargin + childParams.rightMargin;
            }
            if (i == 0 || i == 2){
                lHeight += childHeight + childParams.topMargin + childParams.bottomMargin;
            }
            if (i == 1 || i == 3){
                rHeight += childHeight + childParams.topMargin + childParams.bottomMargin;
            }
        }

        width = Math.max(tWidth, bWidth);
        height = Math.max(lHeight, rHeight);

        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? widthSize : width,
                (heightMode == MeasureSpec.EXACTLY) ? heightSize : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int childWidth = 0, childHeight = 0;
        MarginLayoutParams childParams = null;

        for (int i = 0; i < childCount; ++i){
            View childView = getChildAt(i);
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();
            childParams = (MarginLayoutParams) childView.getLayoutParams();

            int cl = 0, cr = 0, ct = 0, cb = 0;
            switch (i){
                case 0:
                    cl = childParams.leftMargin;
                    ct = childParams.topMargin;
                    break;
                case 1:
                    cl = getWidth() - childWidth - childParams.leftMargin - childParams.rightMargin;
                    ct = childParams.topMargin;
                    break;
                case 2:
                    cl = childParams.leftMargin;
                    ct = getHeight() - childHeight - childParams.bottomMargin;
                    break;
                case 3:
                    cl = getWidth() - childWidth - childParams.leftMargin - childParams.rightMargin;
                    ct = getHeight() - childHeight - childParams.bottomMargin;
                    break;
            }
            cr = cl + childWidth;
            cb = ct + childHeight;
            childView.layout(cl, ct, cr, cb);
        }
    }
}
