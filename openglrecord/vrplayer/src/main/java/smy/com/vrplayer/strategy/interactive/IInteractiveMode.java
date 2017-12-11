package smy.com.vrplayer.strategy.interactive;

import android.app.Activity;
import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IInteractiveMode {

    void onResume(Context context);

    void onPause(Context context);

    boolean handleDrag(float distanceX, float distanceY);

    boolean handleFling(float velocityX, float velocityY);

    void onOrientationChanged(Activity activity);

    void onResetCameraAngle();
}
