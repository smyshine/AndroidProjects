package smy.com.vrplayer.objects.base;


import android.opengl.GLES20;
import android.opengl.Matrix;


public abstract class BaseDualRectObject extends BaseRectObject {

    private final float JOINT_FACTOR = 0.06f;
    private int mInvpolLengthHander;
    private int mFrontInvpolArrayHandler;
    private int mBackInvpolArrayHandler;
    private int mUVfrontCenterHandler;
    private int mUVbackCenterHandler;
    private int mFrontAffineParamHandler;
    private int mBackAffineParamHandler;
    private int mExtFrontRotationMtxHandler;
    private int mExtFrontTranslateMtxHandler;
    private int mExtBackRotationMtxHandler;
    private int mExtBackTranslateMtxHandler;

    private int mRadiusHandler;
    private int mUVWidthHandler;
    private int mJoinValueHandler;
    private int mRevertXHandler;
    private boolean mRevertX;

    private CombineParams mParams;


    public BaseDualRectObject(int screenW, int screenH, CombineParams params) {/*关键是得到这些顶点坐标和纹理坐标*/
        super(screenW,screenH);
        mParams = params;
    }

    protected void initUniformHandler(){
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");
        mUserRotationMtxHandler = mShaderProgram.getUniform("userRotationMatrix");

        mInvpolLengthHander = mShaderProgram.getUniform("invpolLength");
        mFrontInvpolArrayHandler = mShaderProgram.getUniform("frontInvpols");
        mBackInvpolArrayHandler = mShaderProgram.getUniform("backInvpols");
        mFrontAffineParamHandler = mShaderProgram.getUniform("frontAffineParam");
        mBackAffineParamHandler = mShaderProgram.getUniform("backAffineParam");
        mRadiusHandler = mShaderProgram.getUniform("radius");
        mUVWidthHandler = mShaderProgram.getUniform("uvWidth");
        mJoinValueHandler = mShaderProgram.getUniform("joinValue");
        mRevertXHandler = mShaderProgram.getUniform("revertX");

        mUVfrontCenterHandler = mShaderProgram.getUniform("frontCenter");
        mExtFrontRotationMtxHandler = mShaderProgram.getUniform("extFrontRotationMatrix");
        mExtFrontTranslateMtxHandler = mShaderProgram.getUniform("extFrontTranslateVector");

        mExtBackRotationMtxHandler = mShaderProgram.getUniform("extBackRotationMatrix");
        mExtBackTranslateMtxHandler = mShaderProgram.getUniform("extBackTranslateVector");
        mUVbackCenterHandler = mShaderProgram.getUniform("backCenter");

        mSTMatrixHandler = mShaderProgram.getUniform("stMatrix");
        mUseSTMatrixHandler = mShaderProgram.getUniform("useSTMatrix");

        Matrix.setIdentityM(mMVPMtx,0);
    }

    public void drawBitmap(float[] matrix, int eyeType, boolean revertY){
        mRevertX = revertY;
        draw(matrix,null,eyeType);
    }

    protected void setUniform(float[] modelMatrix, float[] stMatrix){
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMtx, 0);
        GLES20.glUniformMatrix4fv(mUserRotationMtxHandler, 1, false, modelMatrix, 0);
        GLES20.glUniform1i(mInvpolLengthHander, mParams.front_invpol.length);
        GLES20.glUniform1fv(mFrontInvpolArrayHandler, mParams.front_invpol.length, mParams.front_invpol, 0);
        GLES20.glUniform3fv(mFrontAffineParamHandler, 1, mParams.front_affine_param, 0);
        GLES20.glUniform1fv(mBackInvpolArrayHandler, mParams.back_invpol.length, mParams.back_invpol, 0);
        GLES20.glUniform3fv(mBackAffineParamHandler, 1, mParams.back_affine_param, 0);
        GLES20.glUniform1f(mRadiusHandler,mParams.sphere_radius);
        GLES20.glUniform1f(mUVWidthHandler,mParams.fish_eye_width);
        GLES20.glUniform1f(mJoinValueHandler,mParams.sphere_radius * JOINT_FACTOR);
        GLES20.glUniform1i(mRevertXHandler,mRevertX ? 1:0);

        GLES20.glUniform2fv(mUVfrontCenterHandler,1,mParams.front_camera_center,0);
        GLES20.glUniformMatrix3fv(mExtFrontRotationMtxHandler,1,false,mParams.front_camera_rotation,0);
        GLES20.glUniform3fv(mExtFrontTranslateMtxHandler,1,mParams.front_camera_translation,0);

        GLES20.glUniform2fv(mUVbackCenterHandler,1,mParams.back_camera_center,0);
        GLES20.glUniformMatrix3fv(mExtBackRotationMtxHandler,1,false,mParams.back_camera_rotation,0);
        GLES20.glUniform3fv(mExtBackTranslateMtxHandler,1,mParams.back_camera_translation,0);
    }

    @Override
    public void draw(float[] mvpMatrix, float[] stMatrix, int eyeType) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());
        setUniform(mvpMatrix, stMatrix);
        activeTexture();
        drawUseVBO();
    }

    private void drawUseVBO(){
             /*顶点坐标*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[0]);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 2,
                GLES20.GL_FLOAT, false, getVerticesStride(), 0);

        // GLES20.glDrawArrays(GLES20.GL_LINES,0,mTotalVertices);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[1]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,mTotalIndices,
                GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
    }
}
