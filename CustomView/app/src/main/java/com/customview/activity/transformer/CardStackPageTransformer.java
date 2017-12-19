package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class CardStackPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position <= -1){

        } else if (position <= 0){

        } else {
            page.setAlpha((float) (1 - position * 0.1));
            page.setPivotX(page.getWidth() / 2f);
            page.setPivotY(page.getHeight() / 2f);
            page.setScaleX((float) Math.pow(0.9f, position));
            page.setScaleY((float) Math.pow(0.9f, position));
            page.setTranslationX(position * -page.getWidth());
            page.setTranslationY(-position * 70);
        }
    }
}
