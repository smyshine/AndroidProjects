package smy.com.vrplayer.strategy.interactive;

import android.app.Activity;
import android.content.Context;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class TouchStrategy extends AbsInteractiveStrategy {

    private static final String TAG = "TouchStrategy";

    public TouchStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public void onResume(Context context) {}

    @Override
    public void onPause(Context context) {}

    @Override
    public boolean handleDrag(final float distanceX, final float distanceY) {
        if(getVRRender() != null){
            getVRRender().setGestureRotateAngle(distanceX,
                    distanceY);
        }
        return false;
    }

    @Override
    public boolean handleFling(float velocityX, float velocityY) {
        if (getVRRender() != null){
            getVRRender().setFlingVelocity(velocityX, velocityY);
        }
        return false;
    }

    @Override
    public void onOrientationChanged(Activity activity) {

    }

    @Override
    public void onResetCameraAngle() {

    }

    @Override
    public void on(Activity activity) {
        /*for (MD360Director director : getDirectorList()){
            director.reset();
        }*/
    }

    @Override
    public void off(Activity activity) {
    }

    @Override
    public boolean isSupport(Activity activity) {
        return true;
    }
}
