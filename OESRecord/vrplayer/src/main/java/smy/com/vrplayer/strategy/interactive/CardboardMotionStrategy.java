package smy.com.vrplayer.strategy.interactive;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.google.vr.sdk.base.sensors.internal.OrientationEKF;
import com.google.vr.sdk.base.sensors.internal.Vector3d;

import java.util.concurrent.TimeUnit;

import smy.com.vrplayer.common.XYMainHandler;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class CardboardMotionStrategy extends AbsInteractiveStrategy implements SensorEventListener {

    private static final String TAG = "CardboardMotionStrategy";

    private int mDeviceRotation;

    private float[] mResultMatrix = new float[16];

    private float[] mTmpMatrix = new float[16];

    private float[] mRotateMatrix = new float[16];

    private boolean mRegistered = false;

    private Boolean mIsSupport = null;

    private final OrientationEKF mTracker = new OrientationEKF();

    private Vector3d mLatestAcc = new Vector3d();

    private long mLatestGyroEventClockTimeNs;

    private final Vector3d mGyroBias = new Vector3d();

    private final Vector3d mLatestGyro = new Vector3d();

    /**-- 以下变量在updateSensorRunnable中频繁使用，故在外部定义 ---*/
    private double secondsSinceLastGyroEvent;
    private double secondsToPredictForward;
    private double[] mat;
    private float rotation = 0;
    /**-- end ---*/

    public CardboardMotionStrategy(InteractiveModeManager.Params params) {
        super(params);
    }

    @Override
    public void onResume(Context context) {
        registerSensor(context);
    }

    @Override
    public void onPause(Context context) {
        unregisterSensor(context);
    }

    @Override
    public boolean handleDrag(float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean handleFling(float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onOrientationChanged(Activity activity) {
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void onResetCameraAngle() {
        mTracker.setHeadingDegrees(0.0f);
    }

    @Override
    public void on(Activity activity) {
        mDeviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        registerSensor(activity);
    }

    @Override
    public void off(Activity activity) {
        unregisterSensor(activity);
    }

    @Override
    public boolean isSupport(Activity activity) {
        if (mIsSupport == null){
            SensorManager mSensorManager = (SensorManager) activity
                    .getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor sensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mIsSupport = (sensor1 != null && sensor2 != null);
        }
        return mIsSupport;
    }

    protected void registerSensor(Context context){
        if (mRegistered) return;

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (sensor1 == null || sensor2 == null){
            Log.e(TAG,"TYPE_ACCELEROMETER TYPE_GYROSCOPE sensor not support!");
            return;
        }

        mSensorManager.registerListener(this, sensor1, getParams().mMotionDelay,
                XYMainHandler.sharedHandler());
        mSensorManager.registerListener(this, sensor2, getParams().mMotionDelay,
                XYMainHandler.sharedHandler());

        mRegistered = true;
    }

    protected void unregisterSensor(Context context){
        if (!mRegistered) return;

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);

        mRegistered = false;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.accuracy != 0){

            int type = event.sensor.getType();

            if (type == Sensor.TYPE_ACCELEROMETER){
                synchronized (mTracker){
                    this.mLatestAcc.set(event.values[0], event.values[1], event.values[2]);
                    this.mTracker.processAcc(this.mLatestAcc, event.timestamp);
                }

            } else if(type == Sensor.TYPE_GYROSCOPE){
                synchronized (mTracker){
                    this.mLatestGyroEventClockTimeNs = System.nanoTime();
                    this.mLatestGyro.set(event.values[0], event.values[1], event.values[2]);
                    Vector3d.sub(this.mLatestGyro, this.mGyroBias, this.mLatestGyro);
                    this.mTracker.processGyro(this.mLatestGyro, event.timestamp);
                }
            }
            if (getParams().mSensorListener != null) {
                getParams().mSensorListener.onSensorHeadViewChanged((int)(mTracker.getHeadingDegrees()));
            }
            getParams().glHandler.post(updateSensorRunnable);
        }
    }


    private Runnable updateSensorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRegistered) return;

            // mTracker will be used in multi thread.
            synchronized (mTracker){
                secondsSinceLastGyroEvent = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - mLatestGyroEventClockTimeNs);
                secondsToPredictForward = secondsSinceLastGyroEvent + 1.0/60;
                mat = mTracker.getPredictedGLMatrix(secondsToPredictForward);
                for (int i = 0; i < mat.length; i++){
                    //实际测试情况，该处有可能返回的value为nan，导致渲染失败。原因分析在cardboard的陀螺仪计算逻辑上出问题，
                    //但由于无法打印代码，暂时执行reset，重新计算一次即可正常。
                    if(Double.isNaN(mat[i])){
                        mTracker.reset();
                        return;
                    }
                    mTmpMatrix[i] = (float) mat[i];
                }
            }

            rotation = 0;
            switch (mDeviceRotation){
                case Surface.ROTATION_0:
                    rotation = 0;
                    break;
                case Surface.ROTATION_90:
                    rotation = 90.0f;
                    break;
                case Surface.ROTATION_180:
                    rotation = 180.0f;
                    break;
                case Surface.ROTATION_270:
                    rotation = 270.0f;
                    break;
            }

            Matrix.setRotateEulerM(mRotateMatrix, 0, 0.0f, 0.0f, -rotation);
            Matrix.multiplyMM(mResultMatrix, 0, mRotateMatrix, 0, mTmpMatrix, 0);
            getVRRender().setSensorMatrix(mResultMatrix);
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
