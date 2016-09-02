package com.example.smy.photoloading;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by SMY on 2016/9/2.
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取手机宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        final Context appContext = context.getApplicationContext();
        WindowManager manager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * 获取手机高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        final Context appContext = context.getApplicationContext();
        WindowManager manager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * 获取宽高 宽size[0] 高size[1]
     * @param context
     * @return
     */
    public static int[] getScreenSize(Context context) {
        final Context appContext = context.getApplicationContext();
        WindowManager manager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int[] size = {display.getWidth(), display.getHeight()};
        return size;
    }

    /**
     * 获取navigationBar的高度
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }
}
