package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class FlipRotationPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1){

        } else if (position <= 1){
            page.setAlpha(1 - position);
            if (position <= 0){
                page.setAlpha(1 + position);
            }
            page.setCameraDistance(60000);
            page.setTranslationX(page.getWidth() * -position);
            page.setPivotX(0);
            page.setRotationY(position * 90);
        }
    }
}
