package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/19.
 */

public class CardFlipoverPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        if (position <= -1){
            //页面在屏幕左侧且不可视
            page.setClickable(false);
            page.setAlpha(0);
        } else if (position <= 0){
            //页面从左侧进入或者向左侧滑出
            page.setClickable(false);
            page.setAlpha(1);
            if (position <= -0.5){
                page.setAlpha(0);
            }
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight() / 2);
            page.setTranslationX(position * -page.getWidth());
            page.setCameraDistance(10000);
            page.setRotationY(position * 180);
        } else if (position <= 1){
            //页面从右侧进入或向右侧滑出
            page.setAlpha(0);
            if (position <= 0.5){
                page.setAlpha(1);
            }
            page.setPivotX(page.getWidth() / 2);
            page.setPivotY(page.getHeight() / 2);
            page.setTranslationX(position * -page.getWidth());
            page.setCameraDistance(10000);
            page.setRotationY(-180 - (1 - position) * 180);
        } else if (position >= 1){
            //页面在屏幕右侧且不可视
        }
    }
}
