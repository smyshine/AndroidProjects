package smy.com.vrplayer.objects.base;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.VrConstant;

/**
 * Created by HanZengbo on 2017/3/18.
 */
public abstract class XYVRBaseObject {

    int mSphereWidth;
    int mSphereHeight;

    protected int mTotalIndices;

    protected FloatBuffer mVertices;
    protected ShortBuffer mIndices;

    protected int[] mObjectVAO;

    protected ShaderProgram mShaderProgram;

    protected int aPositionLocation;
    protected int aTextureCoordLocationFront;
    protected int aTextureCoordLocationBack;

    protected int mMVPMatrixHandle;
    protected int mSTMatrixHandler;
    protected int mUseSTMatrixHandler;
    protected int singleTextureHandle;
    protected int singleTex;

    public XYVRBaseObject(){

    }

    public XYVRBaseObject(int width, int height){
        mSphereWidth = width;
        mSphereHeight = height;
        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = mSphereWidth / step + 1;
        int jMax = mSphereHeight / step + 1;
        int nVertices = iMax * jMax;
        mTotalIndices = (mSphereWidth / step) * (mSphereHeight / step) * 6;//194400
        mVertices = ByteBuffer.allocateDirect(nVertices * 3 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mIndices = ByteBuffer.allocateDirect(mTotalIndices * VrConstant.SHORT_SIZE)
                .order(ByteOrder.nativeOrder()).asShortBuffer();

        short[] indexBuffer = new short[mTotalIndices];

        int index = 0;
        for (int i = 0; i < mSphereHeight / step; i++) {
            for (int j = 0; j < mSphereWidth / step; j++) {
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

    public void setSingleTex(int singleTex){
        this.singleTex = singleTex;
    }

    protected void initPanoSphereVAO(FloatBuffer verticesBuffer, FloatBuffer textureBuffer,
                         ShortBuffer indicesBuffer){
        //gen vertice, texture, and indice buffer on gpu memory and release the cpu memory.
        mObjectVAO = new int[3];
        GLES20.glGenBuffers(3,mObjectVAO,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * VrConstant.FLOAT_SIZE,
                verticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer.capacity() * VrConstant.FLOAT_SIZE,
                textureBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[2]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * VrConstant.SHORT_SIZE,
                indicesBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        verticesBuffer = null;
        textureBuffer = null;
        indicesBuffer = null;
    }

    protected void drawPanoSphereObject(){
         /*顶点坐标*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[0]);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, getVerticesStride(), 0);
        /*纹理坐标*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordLocationFront);
        GLES20.glVertexAttribPointer(aTextureCoordLocationFront, 2,
                GLES20.GL_FLOAT, false, getSTStride(), 0);
        /*索引坐标*/
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[2]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTotalIndices, GLES20.GL_UNSIGNED_SHORT,
                0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocationFront);
    }

    protected void initDualSphereVAO(FloatBuffer verticesBuffer, FloatBuffer textureBuffer1,
                                     FloatBuffer textureBuffer2, ShortBuffer indicesBuffer){
        //gen vertice, texture, and indice buffer on gpu memory and release the cpu memory.
        mObjectVAO = new int[4];
        GLES20.glGenBuffers(4,mObjectVAO,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * VrConstant.FLOAT_SIZE,
                verticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer1.capacity() * VrConstant.FLOAT_SIZE,
                textureBuffer1, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mObjectVAO[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer2.capacity() * VrConstant.FLOAT_SIZE,
                textureBuffer2, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[3]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * VrConstant.SHORT_SIZE,
                indicesBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);

        verticesBuffer = null;
        textureBuffer1 = null;
        textureBuffer2 = null;
        indicesBuffer = null;
    }

    protected void drawDualSphereObject(){
         /*顶点坐标*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[0]);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, getVerticesStride(), 0);
        /*纹理坐标1*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordLocationFront);
        GLES20.glVertexAttribPointer(aTextureCoordLocationFront, 2,
                GLES20.GL_FLOAT, false, getSTStride(), 0);
        /*纹理坐标2*/
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mObjectVAO[2]);
        GLES20.glEnableVertexAttribArray(aTextureCoordLocationBack);
        GLES20.glVertexAttribPointer(aTextureCoordLocationBack, 2,
                GLES20.GL_FLOAT, false, getSTStride(), 0);
        /*索引坐标*/
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mObjectVAO[3]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTotalIndices, GLES20.GL_UNSIGNED_SHORT,
                0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocationFront);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocationBack);
    }

    protected int getVerticesStride() {
        return 3 * VrConstant.FLOAT_SIZE;
    }

    protected int getSTStride() {
        return 2 * VrConstant.FLOAT_SIZE;
    }

    protected FloatBuffer getVertices() {
        return mVertices;
    }

    protected ShortBuffer getIndices() {
        return mIndices;
    }

    public abstract void draw(float[] modelMatrix,float[] stMatrix, int eyeType);

    public void draw(float[] matrix, ByteBuffer yBuffer, ByteBuffer uBuffer,
                              ByteBuffer vBuffer, int width, int height){

    }

}
