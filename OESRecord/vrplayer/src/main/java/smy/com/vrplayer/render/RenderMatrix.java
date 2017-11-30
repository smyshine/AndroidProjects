package smy.com.vrplayer.render;

import android.opengl.Matrix;
import android.os.SystemClock;

import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.listener.ViewDirectionChangeListener;


/**
 * Created by SMY on 2017/5/18.
 */

public class RenderMatrix {
    public float[] mProjectionMatrix = new float[16];
    public float[] mViewMatrix = new float[16];
    public float[] mMVPMatrix = new float[16];
    public volatile float[] mSensorMatrix = new float[16];
    public float[] mSTMatrix = new float[16];

    public int mRenderMode = VrConstant.RENDER_MODE_SPHERE;
    public float mFieldOfView = VrConstant.INIT_FIELD_VIEW;

    //手势滑动参数
    public float mGestureXAngle = 0;
    public float mGestureYAngle = 0;
    public float mFlingVelocityX = 0;
    public float mFlingVelocityY = 0;

    //绘制区域参数
    public int screenWidth = 0;
    public int screenHeight = 0;
    public int mFrameWidth;
    public int mFrameHeight;

    //手机屏幕方向参数
    public boolean mIsDeviceLandscape = false;

    public float mEyeY = VrConstant.INIT_POSITION;

    public RenderMatrix setFieldOfView(float view){
        this.mFieldOfView = view;
        return this;
    }

    public RenderMatrix setEyeY(float eye){
        this.mEyeY = eye;
        return this;
    }

    public RenderMatrix setGestureX(float x){
        this.mGestureXAngle = x;
        return this;
    }

    public RenderMatrix setGestureY(float y){
        this.mGestureYAngle = y;
        return this;
    }

    public void setViewDirectionChangeListener(ViewDirectionChangeListener listener){
        this.viewDirectionChangeListener = listener;
    }

    public ViewDirectionChangeListener viewDirectionChangeListener;


    public void identitySensor(){
        Matrix.setIdentityM(mSensorMatrix, 0);
    }

    public void setSensorMatrix(float[] matrixS) {
        System.arraycopy(matrixS, 0, mSensorMatrix, 0, mSensorMatrix.length);
    }

    public void setSTMatrix(float[] matrix){
        System.arraycopy(matrix,0,mSTMatrix,0,matrix.length);
    }

    public void initFieldOfView(){
        if (mRenderMode == VrConstant.RENDER_MODE_PLANET){
            mFieldOfView = VrConstant.INIT_FIELD_VIEW_PLANET;
        } else if (mRenderMode == VrConstant.RENDER_MODE_SPHERE_OUTSIDE){
            if (mIsDeviceLandscape){
                mFieldOfView = VrConstant.INIT_FIELD_VIEW_SOUTSIDE_LANDSCAPE;
            } else {
                mFieldOfView = VrConstant.INIT_FIELD_VIEW_SOUTSIDE;
            }
        } else {
            mFieldOfView = VrConstant.INIT_FIELD_VIEW;
        }
    }

    public void onOrientationChange(boolean landscape){
        mIsDeviceLandscape = landscape;
        initFieldOfView();
    }

    public void onZoomGesture(float scale) {
        float originFieldView = mFieldOfView;
        float fieldOfViewMin = (mRenderMode == VrConstant.RENDER_MODE_PLANET) ?
                VrConstant.FIELD_OF_VIEW_PLANET_MIN : VrConstant.FIELD_OF_VIEW_NORMAL_MIN;
        float fieldOfViewMax = (mRenderMode == VrConstant.RENDER_MODE_PLANET)
                ? VrConstant.FIELD_OF_VIEW_PLANET_MAX : VrConstant.FIELD_OF_VIEW_NORMAL_MAX;
        mFieldOfView = originFieldView - (scale) / 10;
        if(mFieldOfView < fieldOfViewMin){
            mFieldOfView = fieldOfViewMin;
        } else if(mFieldOfView > fieldOfViewMax){
            mFieldOfView = fieldOfViewMax;
        }
    }

    public void resetOrientation() {
        initFieldOfView();
        mGestureXAngle = 0.0f;
        mGestureYAngle = 0.0f;
    }

    public void setGestureRotateAngle(float rotateXAngle, float rotateYAngle) {
        mGestureXAngle -= rotateXAngle;
        mGestureYAngle -= rotateYAngle;
        mGestureXAngle %= VrConstant.GESTURE_X_ANGLE_NORMAL_MAX;
        mGestureYAngle %= VrConstant.GESTURE_X_ANGLE_NORMAL_MAX;
    }

    public void setFlingVelocity(float velocityX, float velocityY){
        //传入的velocity单位是像素/秒，
        mFlingVelocityX = velocityX / 30;
        mFlingVelocityY = velocityY / 30;
    }

    public void setRenderMode(int renderMode) {
        mRenderMode = renderMode;
        identitySensor();
    }

    public void setScreenSize(int width, int height){
        screenWidth = width;
        screenHeight = height;
    }

    public void setFrameSize(int width, int height){
        mFrameWidth = width;
        mFrameHeight = height;
    }

    private long mPrevTime = -1;

    /**
     * 递减旋转速度
     */
    private void decreaseRotateSpeed() {
        if (mFlingVelocityX > 0) {
            mFlingVelocityX -= VrConstant.ROTATE_DAMPER;
            if (mFlingVelocityX < 0) {
                mFlingVelocityX = 0;
            }
        } else if (mFlingVelocityX < 0) {
            mFlingVelocityX += VrConstant.ROTATE_DAMPER;
            if (mFlingVelocityX > 0) {
                mFlingVelocityX = 0;
            }
        }

        if (mFlingVelocityY > 0) {
            mFlingVelocityY -= VrConstant.ROTATE_DAMPER;
            if (mFlingVelocityY < 0) {
                mFlingVelocityY = 0;
            }
        } else if (mFlingVelocityY < 0) {
            mFlingVelocityY += VrConstant.ROTATE_DAMPER;
            if (mFlingVelocityY > 0) {
                mFlingVelocityY = 0;
            }
        }
    }

    public void decrease(){
        long curTime = SystemClock.uptimeMillis();
        decreaseRotateSpeed();
        if (mPrevTime < 0) mPrevTime = curTime;
        double delta = (curTime - mPrevTime) / 1000.0f;
        mPrevTime = curTime;
        mGestureXAngle += delta * mFlingVelocityX;
        mGestureYAngle += delta * mFlingVelocityY;
        mGestureXAngle %= VrConstant.GESTURE_X_ANGLE_NORMAL_MAX;
        mGestureYAngle %= VrConstant.GESTURE_X_ANGLE_NORMAL_MAX;

        if (viewDirectionChangeListener != null){
            viewDirectionChangeListener.onViewDirectionChange((float) -delta * mFlingVelocityX);
        }
    }
}
