package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class CascadeZoomPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1){

        } else if (position <= 0){
            page.setAlpha(1 + position);
        } else if (position <= 1){
            page.setTranslationX(page.getWidth() * -position);
            page.setScaleX(1 - position * 0.5f);
            page.setScaleY(1 - position * 0.5f);
            page.setAlpha(1 - position);
        }
    }
}
