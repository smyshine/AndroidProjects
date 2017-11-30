package smy.com.vrplayer.objects.bitmap;

import android.graphics.Bitmap;
import android.opengl.GLES20;

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

public class PanoBitmapSphere extends XYVRBaseObject {
    private FloatBuffer mSTBuffer;

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
            "precision mediump float;\n" +
                    " varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D singleTexture;\n" +
                    " void main() \n{" +
                    " gl_FragColor = texture2D(singleTexture, vTextureCoord);\n" +
                    "}\n";

    /*
     * @param nSlices how many slice in horizontal direction.
     *                The same slice for vertical direction is applied.
     *                nSlices should be > 1 and should be <= 180
     * @param x,y,z the origin of the sphere
     * @param r the radius of the sphere
     * @param viewRadius the radius of 2 picture
     * @param centerFront the center of the front camera
     * @param centerBack the center of the back camera
     *
     */
    public PanoBitmapSphere(int sphereW, int sphereH,Bitmap bitmap) {/*关键是得到这些顶点坐标和纹理坐标*/
        super(sphereW,sphereH);
        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = sphereW / step + 1;
        int jMax = sphereH / step + 1;
        int nVertices = iMax * jMax;
        int inX, inY;
        double angleStep = Math.PI / sphereH;

        int R = 500;//和native保持一致
        // 3 vertex coords + 2 texture coords
        mSTBuffer = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        /*获取顶点和纹理坐标*/
        float[] vLineBuffer = new float[iMax * 3];
        float[] vSTBuffer = new float[iMax * 2];

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
                vSTBuffer[stBase++] = (float) inX / sphereW;
                vSTBuffer[stBase++] = (float) inY / sphereH;
            }
            mVertices.put(vLineBuffer, 0, vLineBuffer.length);
            mSTBuffer.put(vSTBuffer, 0, vSTBuffer.length);
        }

        mSTBuffer.position(0);
        mVertices.position(0);

        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);
        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");

        aTextureCoordLocationFront = mShaderProgram.getAttribute("aTextureCoord");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");

        singleTextureHandle = mShaderProgram.getUniform("singleTexture");
        setSingleTex(GLHelpers.loadTexture(bitmap));

        initPanoSphereVAO(mVertices,mSTBuffer,mIndices);
    }

    public int getSTStride() {
        return 2 * VrConstant.FLOAT_SIZE;
    }


    @Override
    public void draw(float[] modelMatrix, float[] stMatrix, int eyeType) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, modelMatrix, 0);

        GLES20.glUniform1i(singleTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, singleTex);

        drawPanoSphereObject();
    }
}


