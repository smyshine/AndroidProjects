package smy.com.vrplayer.objects.yuv;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.VrConstant;

/**
 * Created by HanZengbo on 2017/3/20.
 */
public class PanoYUVSphere extends AbstractYUVObject {

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
                    "precision mediump float;                                     \n" +
                    "varying lowp vec2 vTextureCoord;                               \n" +
                    "uniform sampler2D tex_y;                                  \n" +
                    "uniform sampler2D tex_u;                                  \n" +
                    "uniform sampler2D tex_v;                                  \n" +
                    "void main(void)                                              \n" +
                    "{                                                            \n" +
                    "    mediump vec4 yuv;                                        \n" +
                    "    vec4 rgba;                                               \n" +
                    "    yuv = vec4(texture2D(tex_y, vTextureCoord).r - 0.0625,  \n" +
                    "               texture2D(tex_u, vTextureCoord).r - 0.5000,  \n" +
                    "               texture2D(tex_v, vTextureCoord).r - 0.5000,  \n" +
                    "               1.0);                                         \n" +
                    "    rgba = mat4(1.164,    1.164,    1.164,    0.0,           \n" +
                    "                0.0,     -0.392,    2.018,    0.0,           \n" +
                    "                1.59,    -0.813,    0.0,      0.0,           \n" +
                    "                0.0,      0.0,      0.0,      1.0) * yuv;    \n" +
                    "    gl_FragColor = rgba;                                     \n" +
                    "}                                                            \n";

    private FloatBuffer mSTBuffer;

    public PanoYUVSphere(int width, int height){
        super(width,height);

        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = width / step + 1;
        int jMax = height / step + 1;
        int nVertices = iMax * jMax;
        int inX, inY;
        double angleStep = Math.PI / height;

        int R = 500;//和native保持一致
        // 3 vertex coords + 2 texture coords
        mSTBuffer = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        /*获取顶点和纹理坐标*/
        float[] vLineBuffer = new float[iMax * 3];
        float[] vSTBuffer = new float[iMax * 2];

        for (inY = 0; inY <= height; inY += step) {
            int vertexBase = 0;
            int stBase = 0;
            for (inX = 0; inX <= width; inX += step) {
                float sinY = (float) Math.sin(angleStep * inY);
                float sinX = (float) Math.sin(angleStep * inX);
                float cosY = (float) Math.cos(angleStep * inY);
                float cosX = (float) Math.cos(angleStep * inX);
                // vertex x,y,z
                vLineBuffer[vertexBase++] = R * sinY * sinX;
                vLineBuffer[vertexBase++] = R * sinY * cosX;
                vLineBuffer[vertexBase++] = R * cosY;
                // texture s,t
                vSTBuffer[stBase++] = (float) inX / width;
                vSTBuffer[stBase++] = (float) inY / height;
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

        mTextureUniformY = mShaderProgram.getUniform("tex_y");
        mTextureUniformU = mShaderProgram.getUniform("tex_u");
        mTextureUniformV = mShaderProgram.getUniform("tex_v");

        GLHelpers.initTexture(id_y, id_u, id_v);

        initPanoSphereVAO(mVertices,mSTBuffer,mIndices);
    }

    @Override
    public void draw(float[] matrix, ByteBuffer yBuffer, ByteBuffer uBuffer, ByteBuffer vBuffer, int width, int height) {
        bb_y = yBuffer;
        bb_u = uBuffer;
        bb_v = vBuffer;

        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);

        bb_y.position(0);
        bb_u.position(0);
        bb_v.position(0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE0");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_y[0]);
        GLHelpers.checkGlError("glBindTexture id_y");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                width, height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_y);
        GLHelpers.checkGlError("glTexImage2D bb_y");
        GLES20.glUniform1i(mTextureUniformY, 0);
        GLHelpers.checkGlError("glUniform1i mTextureUniformY");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE1");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_u[0]);
        GLHelpers.checkGlError("glBindTexture id_u");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                width / 2, height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_u);
        GLES20.glUniform1i(mTextureUniformU, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE2");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_v[0]);
        GLHelpers.checkGlError("glBindTexture id_v");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                width / 2, height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_v);
        GLES20.glUniform1i(mTextureUniformV, 2);

        drawPanoSphereObject();
    }

    public FloatBuffer getSTBuffer() {
        return mSTBuffer;
    }
}
