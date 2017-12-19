package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class ZoomInPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position <= 0){
            page.setScaleX((float) (1 + position * 0.1));
            page.setScaleY((float) (1 + position * 0.1));
        } else {
            page.setScaleX((float) (1 - position * 0.1));
            page.setScaleY((float) (1 - position * 0.1));
        }
    }
}
