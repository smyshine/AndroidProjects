package smy.com.vrplayer.render.model;

import android.animation.PropertyValuesHolder;
import android.opengl.GLES20;
import android.opengl.Matrix;

import smy.com.vrplayer.render.RenderMatrix;


/**
 * Created by SMY on 2017/5/17.
 */

public abstract class AbstractRenderModel {

    public static final float Z_NEAR = 10f;
    public static final float Z_FAR = 550f;

    public static final String PROPERTY_FIELD_OF_VIEW = "fieldOfView";
    public static final String PROPERTY_ZNEAR = "near";
    public static final String PROPERTY_ZFAR = "far";
    public static final String PROPERTY_EYE_Y = "eyeY";
    public static final String PROPERTY_GESTURE_X = "gestureX";
    public static final String PROPERTY_GESTURE_Y = "gestureY";

    RenderMatrix mRenderMatrix = new RenderMatrix();

    protected float[] mCurrentRotation = new float[16];
    protected float[] mCurrentRotationPost = new float[16];
    protected float[] mTempMatrix = new float[16];

    protected int mWidth = 0;
    protected int mHeight = 0;


    public AbstractRenderModel(){

    }

    protected void setRenderMatrix(RenderMatrix data){
        mRenderMatrix = data;
    }

    public void decrease(){
        mRenderMatrix.decrease();
    }

    public void update(RenderMatrix data){
        decrease();
        setRenderMatrix(data);
        setProjectionMatrix();
        setViewMatrix();
        setMVPMatrix();
    }

    public void init(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    public void setViewPortYUV(boolean pano){

    }

    public void releaseViewPortYUV(boolean pano){
        if (!pano){
            GLES20.glDisable(GLES20.GL_BLEND);
        }
    }


    public abstract void setProjectionMatrix();
    public abstract void setViewMatrix();
    public abstract PropertyValuesHolder getValuesHolder(String type, RenderMatrix matrix);
    public abstract int getAnimationDuration();

    protected void setMVPMatrix(){
        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mRenderMatrix.mGestureYAngle, 1.0f, 0.0f, 0.0f);
        Matrix.setIdentityM(mCurrentRotationPost, 0);
        Matrix.rotateM(mCurrentRotationPost, 0, -mRenderMatrix.mGestureXAngle, 0.0f, 0.0f, 1.0f);

        Matrix.multiplyMM(mTempMatrix, 0, mRenderMatrix.mSensorMatrix, 0, mCurrentRotationPost, 0);
        Matrix.multiplyMM(mCurrentRotationPost, 0, mCurrentRotation, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mTempMatrix, 0, mRenderMatrix.mViewMatrix, 0, mCurrentRotationPost, 0);
        Matrix.multiplyMM(mRenderMatrix.mMVPMatrix, 0, mRenderMatrix.mProjectionMatrix, 0, mTempMatrix, 0);
    }

    protected float getFieldOfView(){
        return mRenderMatrix.mFieldOfView;
    }

    protected float getEyeY(){
        return mRenderMatrix.mEyeY;
    }
}
