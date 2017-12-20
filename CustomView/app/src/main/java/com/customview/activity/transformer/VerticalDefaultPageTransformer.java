package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SMY on 2017/12/20.
 */

public class VerticalDefaultPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        //default
/*        float alpha = 0;
        if (position >= 0 && position <= 1){
            alpha = 1 - position;
        } else {
            alpha = 1 + position;
        }
        page.setAlpha(alpha);
        page.setTranslationX(page.getWidth() * -position);
        page.setTranslationY(page.getHeight() * position);*/

        //stack
        /*if (position >= -1 && position <= 1){
            page.setTranslationX(page.getWidth() * -position);
            page.setTranslationY(position < 0 ? position * page.getHeight() : 0f);
        }*/

        //zoom out
        /*int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();
        float alpha = 0;
        if (0 <= position && position <= 1) {
            alpha = 1 - position;
        } else if (-1 < position && position < 0) {
            float scaleFactor = Math.max(0.9f, 1 - Math.abs(position));
            float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
            float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                page.setTranslationX(horizontalMargin - verticalMargin / 2);
            } else {
                page.setTranslationX(-horizontalMargin + verticalMargin / 2);
            }

            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            alpha = position + 1;
        }

        page.setAlpha(alpha);
        page.setTranslationX(page.getWidth() * -position);
        float yPosition = position * page.getHeight();
        page.setTranslationY(yPosition);*/

        //mine
        if (position <= -1){
            page.setAlpha(0);
        } else if (position <= 0){
            page.setAlpha(1 + position);
            page.setTranslationX(-position * page.getWidth());
            page.setTranslationY(-position * page.getHeight());
        } else{
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
