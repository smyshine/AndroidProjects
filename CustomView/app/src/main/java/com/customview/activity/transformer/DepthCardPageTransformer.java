package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class DepthCardPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1){
            page.setCameraDistance(10000);
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight());
            page.setRotationY(20);
        } else if (position <= 1){
            page.setCameraDistance(10000);
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight());
            page.setRotationY(-position * 20);
            page.setScaleY((float) Math.pow(0.91, 1 - Math.abs(position)));
        } else if (position <= 2){
            page.setCameraDistance(10000);
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight());
            page.setRotationY(-20);
        }
    }
}
