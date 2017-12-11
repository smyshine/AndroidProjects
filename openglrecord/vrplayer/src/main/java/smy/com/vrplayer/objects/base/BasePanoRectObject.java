package smy.com.vrplayer.objects.base;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by hzb on 17-6-9.
 */

public abstract class BasePanoRectObject extends BaseRectObject {
    public BasePanoRectObject(int screenW, int screenH) {
        super(screenW,screenH);
    }

    protected void initUniformHandler(){
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");
        mUserRotationMtxHandler = mShaderProgram.getUniform("userRotationMatrix");
        mSTMatrixHandler = mShaderProgram.getUniform("stMatrix");
        mUseSTMatrixHandler = mShaderProgram.getUniform("useSTMatrix");
        Matrix.setIdentityM(mMVPMtx,0);
    }

    protected void setUniform(float[] modelMatrix, float[] stMatrix){
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMtx, 0);
        GLES20.glUniformMatrix4fv(mUserRotationMtxHandler, 1, false, modelMatrix, 0);
    }

    @Override
    public void draw(float[] modelMatrix, float[] stMatrix,  int eyeType) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());
        setUniform(modelMatrix, stMatrix);
        activeTexture();
        drawUseVBO();
    }

    private void drawUseVBO(){
              /*顶点坐标*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[0]);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 2,
                GLES20.GL_FLOAT, false, getVerticesStride(), 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[1]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,mTotalIndices,
                GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
    }
}
