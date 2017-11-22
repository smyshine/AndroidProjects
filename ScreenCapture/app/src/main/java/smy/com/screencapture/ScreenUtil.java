package smy.com.screencapture;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by SMY on 2017/11/22.
 */

public class ScreenUtil {
    public static int getScreenRealWidth(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);
        return realSize.x;
    }

    public static int getScreenRealHeight(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Point realSize = new Point();
        display.getSize(size);
        display.getRealSize(realSize);
        return realSize.y;
    }
}
