package com.customview.activity.transformer;

import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMY on 2017/12/19.
 */

public class TransformerHelper {

    public static List<String> getTransformerList(){
        List<String> list = new ArrayList<>();
        list.add("Card Flip over");//1
        list.add("Book Flip");//2
        list.add("Card Stack");//3
        list.add("Cascade Zoom");//4
        list.add("Flip Rotation");//5
        list.add("Turntable");//6
        list.add("Depth Card");//7
        list.add("Cubes");//8
        list.add("Zoom in");//9
        return list;
    }

    public static ViewPager.PageTransformer getTransformer(int effect){
        switch (effect){
            case 2:
                return new BookFlipPageTransformer();
            case 3:
                return new CardStackPageTransformer();
            case 4:
                return new CascadeZoomPageTransformer();
            case 5:
                return new FlipRotationPageTransformer();
            case 6:
                return new TurntablePageTransformer();
            case 7:
                return new DepthCardPageTransformer();
            case 8:
                return new CubesPagesTransformer();
            case 9:
                return new ZoomInPageTransformer();
            default:
                return new CardFlipoverPageTransformer();
        }
    }
}
