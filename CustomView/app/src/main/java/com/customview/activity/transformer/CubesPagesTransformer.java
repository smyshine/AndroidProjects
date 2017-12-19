package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class CubesPagesTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position <= -1){

        } else if (position < 1){
            page.setCameraDistance(100000);
            page.setPivotX(0);
            page.setPivotY(page.getHeight() / 2f);
            if (position <= 0){
                page.setPivotX(page.getMeasuredWidth());
                page.setPivotY(page.getMeasuredHeight() / 2f);
            }
            page.setRotationY(90 * position);
        }
    }
}
