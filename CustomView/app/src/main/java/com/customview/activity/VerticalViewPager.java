package com.customview.activity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by SMY on 2017/12/20.
 */

public class VerticalViewPager extends ViewPager {

    public VerticalViewPager(Context context) {
        super(context);
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private MotionEvent swapTouchEvent(MotionEvent event){
        float width = getWidth();
        float height = getHeight();
        event.setLocation(-(event.getY() / height) * width, (event.getX() / width) * height);
        return event;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean interrupt =  super.onInterceptTouchEvent(swapTouchEvent(MotionEvent.obtain(ev)));
        swapTouchEvent(MotionEvent.obtain(ev));
        return interrupt;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(swapTouchEvent(MotionEvent.obtain(ev)));
    }
}
