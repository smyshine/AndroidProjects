package smy.com.vrplayer.objects.base;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.VrConstant;

/**
 * Created by hzb on 17-6-10.
 */

public abstract class BaseRectObject extends XYVRBaseObject {
    protected int mTotalVertices;
    protected int mUserRotationMtxHandler;
    protected float[] mMVPMtx = new float[16];

    protected abstract void initUniformHandler();
    protected abstract void activeTexture();

    public BaseRectObject(int screenW, int screenH){
        mSphereWidth = screenW;
        mSphereHeight = screenH;
    }

    protected void createProgram(String vertShader, String fragShader){
        mShaderProgram = new ShaderProgram(vertShader, fragShader);
        initVertex(mSphereWidth,mSphereHeight);
        initUniformHandler();
        createVBO();
    }

    protected void initVertex(int width, int height){
        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = width / step + 1;
        int jMax = height / step + 1;
        mTotalVertices = iMax * jMax;
        mTotalIndices = (width / step) * (height / step) * 6;
        int inX, inY;
        mVertices = ByteBuffer.allocateDirect(mTotalVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mIndices = ByteBuffer.allocateDirect(mTotalIndices * VrConstant.SHORT_SIZE)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        for (inY = 0; inY <= height; inY += step) {
            for (inX = 0; inX <= width; inX += step) {
                mVertices.put((float)inX / height);
                mVertices.put((float)inY / height);
            }
        }

        mIndices = ByteBuffer.allocateDirect(mTotalIndices * VrConstant.SHORT_SIZE)
                .order(ByteOrder.nativeOrder()).asShortBuffer();

        short[] indexBuffer = new short[mTotalIndices];

        int index = 0;
        for (int i = 0; i < height / step; i++) {
            for (int j = 0; j < width / step; j++) {
                int i1 = i + 1;
                int j1 = j + 1;
                indexBuffer[index++] = (short) (i * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j1);
                indexBuffer[index++] = (short) (i * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j1);
                indexBuffer[index++] = (short) (i * iMax + j1);
            }
        }
        mIndices.put(indexBuffer, 0, mTotalIndices);
        mVertices.position(0);
        mIndices.position(0);
    }

    private void createVBO(){
        mObjectVAO = new int[2];
        GLES20.glGenBuffers(2,mObjectVAO,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertices.capacity() * VrConstant.FLOAT_SIZE,
                mVertices, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices.capacity() * VrConstant.SHORT_SIZE,
                mIndices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);

        mVertices = null;
        mIndices = null;
    }

    @Override
    protected int getVerticesStride() {
        return 2 * VrConstant.FLOAT_SIZE;
    }
}
