package smy.com.vrplayer.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.common.XYGLHandler;
import smy.com.vrplayer.render.AbstractRenderer;
import smy.com.vrplayer.strategy.ModeManager;


/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class InteractiveModeManager extends ModeManager<AbsInteractiveStrategy> implements IInteractiveMode {

    private boolean mIsResumed;

    private static int[] sModes = {VrConstant.INTERACTIVE_MODE_MOTION,
            VrConstant.INTERACTIVE_MODE_TOUCH,
            VrConstant.INTERACTIVE_MODE_MOTION_WITH_TOUCH,
            VrConstant.INTERACTIVE_MODE_CARDBOARD_MOTION,
            VrConstant.INTERACTIVE_MODE_CARDBOARD_MOTION_WITH_TOUCH
    };

    public interface SensorHeadViewChanged{
        public void onSensorHeadViewChanged(int headerDegree);
    }

    public static class Params{
        public int mMotionDelay = SensorManager.SENSOR_DELAY_GAME;
        public SensorHeadViewChanged mSensorListener;
        public AbstractRenderer mVRRender;
       // public ProjectionModeManager projectionModeManager;
        public XYGLHandler glHandler;
    }

    private Params mParams;

    public InteractiveModeManager(int mode, XYGLHandler handler, Params params) {
        super(mode, handler);
        mParams = params;
        mParams.glHandler = getGLHandler();
    }

    @Override
    protected int[] getModes() {
        return sModes;
    }

    @Override
    protected AbsInteractiveStrategy createStrategy(int mode) {
        if(mode == VrConstant.INTERACTIVE_MODE_CARDBOARD_MOTION_WITH_TOUCH) {
            return new CardboardMTStrategy(mParams);
        }
        return new TouchStrategy(mParams);
    }

    public boolean isUsingSensor(){
        return getMode() != VrConstant.INTERACTIVE_MODE_TOUCH;

    }

    private UpdateDragRunnable updateDragRunnable = new UpdateDragRunnable();
    private UpdateFlingRunnable updateFlingRunnable = new UpdateFlingRunnable();

    /**
     * handle touch touch to rotate the model
     *
     * @param distanceX x
     * @param distanceY y
     * @return true if handled.
     */
    @Override
    public boolean handleDrag(final float distanceX, final float distanceY) {
        updateDragRunnable.handleDrag(distanceX, distanceY);
        getGLHandler().post(updateDragRunnable);
        return false;
    }

    @Override
    public boolean handleFling(float velocityX, float velocityY) {
        updateFlingRunnable.handleFling(velocityX, velocityY);
        getGLHandler().post(updateFlingRunnable);
        return false;
    }

    @Override
    public void onOrientationChanged(final Activity activity) {
        getGLHandler().post(new Runnable() {
            @Override
            public void run() {
                getStrategy().onOrientationChanged(activity);
            }
        });
    }

    @Override
    public void onResetCameraAngle() {
        getStrategy().onResetCameraAngle();
    }

    private class UpdateDragRunnable implements Runnable {
        private float distanceX;
        private float distanceY;

        private void handleDrag(float distanceX, float distanceY){
            this.distanceX = distanceX;
            this.distanceY = distanceY;
        }

        @Override
        public void run() {
            getStrategy().handleDrag(distanceX, distanceY);
        }
    }

    private class  UpdateFlingRunnable implements Runnable {
        private float velocityX;
        private float velocityY;

        private void handleFling(float velocityX, float velocityY){
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        @Override
        public void run() {
            getStrategy().handleFling(velocityX, velocityY);
        }
    }

    public void onResume(Context context) {
        mIsResumed = true;
        if (getStrategy().isSupport((Activity)context)){
            getStrategy().onResume(context);
        }
    }

    @Override
    public void on(Activity activity) {
        super.on(activity);

        if (mIsResumed){
            onResume(activity);
        }
    }

    public void onPause(Context context) {
        mIsResumed = false;
        if (getStrategy().isSupport((Activity)context)){
            getStrategy().onPause(context);
        }
    }
}
