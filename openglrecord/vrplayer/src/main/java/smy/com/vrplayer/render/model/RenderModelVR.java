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

public class RenderModelVR extends AbstractRenderModel {

    public RenderModelVR() {
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
        Matrix.setLookAtM(mRenderMatrix.mViewMatrix, 0,  0.0f, getEyeY(), 1.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);
    }

    @Override
    public PropertyValuesHolder getValuesHolder(String type, RenderMatrix matrix) {
        switch (type){
            case PROPERTY_FIELD_OF_VIEW:
                return ofFloat(PROPERTY_FIELD_OF_VIEW, VrConstant.INIT_FIELD_VIEW, VrConstant.INIT_FIELD_VIEW);
            case PROPERTY_EYE_Y:
                return ofFloat(PROPERTY_EYE_Y, 0.0f, 0.0f);
            case PROPERTY_GESTURE_X:
                return ofFloat(PROPERTY_GESTURE_X, 0.0f, 0.0f);
            case PROPERTY_GESTURE_Y:
                return ofFloat(PROPERTY_GESTURE_Y, 0.0f, 0.0f);
        }
        return null;
    }

    @Override
    public int getAnimationDuration() {
        return  10;
    }
}
