package smy.com.vrplayer.objects.video;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.google.vr.sdk.base.Eye;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.objects.base.XYVRBaseObject;

/**
 * 用途：视频详情页，播放全景视频
 * 取代Rajawali
 * 顶点坐标和纹理坐标直接从java层计算了，没必要用native调用
 * Created by hzb on 16-10-10.
 */

public class StereoPanoVideoSphere extends XYVRBaseObject {
    private FloatBuffer mSTBufferLeft;
    private FloatBuffer mSTBufferRight;

    private ShaderProgram mShaderProgram;
    /*1,转换为YUV分量
    * 2，在球体xyz中的y坐标-50~+50区间内，做YUV分量的线性缩减*/
    private final String mVertexShader =
            "precision mediump float;\n" +
                    "attribute vec4 vPosition;\n" +
                    "uniform mat4 mvpMatrix;\n" + /*MVP矩阵*/
                    "attribute vec2 aTextureCoord;\n" +/*材质坐标*/
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "gl_Position =  mvpMatrix * vPosition ;\n" +
                    "vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    " varying vec2 vTextureCoord;\n" +
                    " uniform samplerExternalOES sTexture;\n" +
                    " void main() \n{" +
                    " gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    private int aPositionLocation;
    private int aTextureCoordLocation;

    private int mMVPMatrixHandle;

    public StereoPanoVideoSphere(int sphereW, int sphereH) {
        super(sphereW,sphereH);
        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = sphereW / step + 1;
        int jMax = sphereH / step + 1;
        int nVertices = iMax * jMax;
        int inX, inY;
        double angleStep = Math.PI / sphereH;

        int R = 500;//和native保持一致

        mSTBufferLeft = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mSTBufferRight = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        /*获取顶点和纹理坐标*/
        float[] vLineBuffer = new float[iMax * 3];
        float[] vSTBufferLeft = new float[iMax * 2];
        float[] vSTBufferRight = new float[iMax * 2];

        for (inY = 0; inY <= sphereH; inY += step) {
            int vertexBase = 0;
            int stBase = 0;
            for (inX = 0; inX <= sphereW; inX += step) {
                float sinY = (float) Math.sin(angleStep * inY);
                float sinX = (float) Math.sin(angleStep * inX);
                float cosY = (float) Math.cos(angleStep * inY);
                float cosX = (float) Math.cos(angleStep * inX);
                // vertex x,y,z
                vLineBuffer[vertexBase++] = R * sinY * sinX;
                vLineBuffer[vertexBase++] = R * sinY * cosX;
                vLineBuffer[vertexBase++] = R * cosY;
                // texture s,t
                vSTBufferLeft[stBase] = (float) inX / sphereW;
                vSTBufferRight[stBase] = (float) inX / sphereW;
                stBase++;

                float ft = (float) inY / sphereH / 2;
                vSTBufferLeft[stBase] = ft;
                vSTBufferRight[stBase] = ft + 0.5f;
                stBase++;
            }
            mVertices.put(vLineBuffer, 0, vLineBuffer.length);
            mSTBufferLeft.put(vSTBufferLeft, 0, vSTBufferLeft.length);
            mSTBufferRight.put(vSTBufferRight,0,vSTBufferRight.length);
        }

        mVertices.position(0);
        mSTBufferLeft.position(0);
        mSTBufferRight.position(0);

        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);
        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");

        aTextureCoordLocation = mShaderProgram.getAttribute("aTextureCoord");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");

        singleTextureHandle = mShaderProgram.getUniform("sTexture");
    }

    public void draw(float[] modelMatrix, float[] stMatrix, int eyeType){
        if(eyeType == Eye.Type.RIGHT){
            drawLeftEye(modelMatrix,false);
        } else {
            drawLeftEye(modelMatrix,true);
        }
    }

    public void drawLeftEye(float[] matrix, boolean left) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        /*顶点坐标*/
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, getVerticesStride(), getVertices());

        GLHelpers.checkGlError("glVertexAttribPointer");

        /*前半球纹理坐标*/
        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");
        if(left) {
            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                    GLES20.GL_FLOAT, false, getSTStride(),
                    mSTBufferLeft);
        } else {
            GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                    GLES20.GL_FLOAT, false, getSTStride(),
                    mSTBufferRight);
        }
        GLHelpers.checkGlError("glVertexAttribPointer");


        GLES20.glUniform1i(singleTextureHandle, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, singleTex);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTotalIndices, GLES20.GL_UNSIGNED_SHORT,
                getIndices());
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocation);
    }
}


