package smy.com.vrplayer.render.model;

import android.animation.PropertyValuesHolder;
import android.opengl.GLES20;
import android.opengl.Matrix;

import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.render.RenderMatrix;

import static android.animation.PropertyValuesHolder.ofFloat;


/**
 * Created by SMY on 2017/5/17.
 */

public class RenderModelSphere extends AbstractRenderModel {

    public RenderModelSphere() {
        super();
    }

    @Override
    public void init(int width, int height) {
        super.init(width,height);
        GLES20.glViewport(0, 0, mWidth, mHeight);
    }

    @Override
    public void setProjectionMatrix() {
        Matrix.perspectiveM(mRenderMatrix.mProjectionMatrix, 0, getFieldOfView(),
                (float) (mWidth) / mHeight, Z_NEAR, Z_FAR);
    }

    @Override
    public void setViewMatrix() {
        Matrix.setLookAtM(mRenderMatrix.mViewMatrix, 0,  0.0f, getEyeY(), 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f);
    }

    @Override
    public void setViewPortYUV(boolean pano) {
        GLES20.glViewport(0, 0, mWidth, mHeight);
        if (!pano){
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        }
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glFrontFace(GLES20.GL_CCW);
    }

    @Override
    public PropertyValuesHolder getValuesHolder(String type, RenderMatrix matrix) {
        switch (type){
            case PROPERTY_FIELD_OF_VIEW:
                return ofFloat(PROPERTY_FIELD_OF_VIEW, matrix.mFieldOfView, VrConstant.INIT_FIELD_VIEW);
            case PROPERTY_EYE_Y:
                return ofFloat(PROPERTY_EYE_Y, matrix.mEyeY, VrConstant.INIT_POSITION);
            case PROPERTY_GESTURE_X:
                return ofFloat(PROPERTY_GESTURE_X, matrix.mGestureXAngle, 0.0f);
            case PROPERTY_GESTURE_Y:
                return ofFloat(PROPERTY_GESTURE_Y, matrix.mGestureYAngle, 0.0f);
        }
        return null;
    }

    @Override
    public int getAnimationDuration() {
        return  500;
    }
}
