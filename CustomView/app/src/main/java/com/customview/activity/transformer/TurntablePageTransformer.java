package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class TurntablePageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position < -1){

        } else if (position < 1) {
            page.setPivotX(page.getWidth() / 2f);
            page.setPivotY(page.getHeight());
            page.setRotation(90 * position);
        }
    }
}
