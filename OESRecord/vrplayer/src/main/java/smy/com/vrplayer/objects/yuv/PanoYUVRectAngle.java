package smy.com.vrplayer.objects.yuv;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;

/**
 * Created by HanZengbo on 2017/3/20.
 */
public class PanoYUVRectAngle extends AbstractYUVObject{

    private int aPositionLocation;
    private int aTextureCoordLocation;

    private ByteBuffer mVertexBuffer;
    private ByteBuffer mTextureBuffer;

    protected int mTextureUniformY, mTextureUniformU, mTextureUniformV;

    private static float[] squareVertices = {
            -1.0f, -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f, 1.0f
    }; // fullscreen

    private static float[] coordVertices = {
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f
    };// whole-texture

    private ShaderProgram mShaderProgram;

    private final String mVertexShader =
            "attribute vec4 vPosition;                         		\n" +
            "attribute vec2 TexCoordIn;                       		\n" +
            "varying vec2 TexCoordOut;                        		\n" +
            "void main(void)                                  		\n" +
            "{                                                		\n" +
            "    gl_Position = vPosition;                           \n" +
            "    TexCoordOut = TexCoordIn;                   		\n" +
            "}                                               		\n";

    private final String mFragmentShader =
            "precision mediump float;                                     \n" +
            "varying lowp vec2 TexCoordOut;                               \n" +
            "uniform sampler2D tex_y;                                  \n" +
            "uniform sampler2D tex_u;                                  \n" +
            "uniform sampler2D tex_v;                                  \n" +
            "void main(void)                                              \n" +
            "{                                                            \n" +
            "    mediump vec4 yuv;                                        \n" +
            "    vec4 rgba;                                               \n" +
            "    yuv = vec4(texture2D(tex_y, TexCoordOut).r - 0.0625,  \n" +
            "               texture2D(tex_u, TexCoordOut).r - 0.5000,  \n" +
            "               texture2D(tex_v, TexCoordOut).r - 0.5000,  \n" +
            "               1.0);                                         \n" +
            "    rgba = mat4(1.164,    1.164,    1.164,    0.0,           \n" +
            "                0.0,     -0.392,    2.018,    0.0,           \n" +
            "                1.59,    -0.813,    0.0,      0.0,           \n" +
            "                0.0,      0.0,      0.0,      1.0) * yuv;    \n" +
            "    gl_FragColor = rgba;                                     \n" +
            "}                                                            \n";


    public PanoYUVRectAngle(int width, int height){
        createBuffers(squareVertices,coordVertices);
        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);

        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        aTextureCoordLocation = mShaderProgram.getAttribute("TexCoordIn");

        mTextureUniformY = mShaderProgram.getUniform("tex_y");
        mTextureUniformU = mShaderProgram.getUniform("tex_u");
        mTextureUniformV = mShaderProgram.getUniform("tex_v");

        GLHelpers.initTexture(id_y, id_u, id_v);
    }

    /**
     * these two buffers are used for holding vertices, screen vertices and texture vertices.
     */
    private void createBuffers(float[] vert, float[] coord) {
        if(mVertexBuffer == null) {
            mVertexBuffer = ByteBuffer.allocateDirect(vert.length * 4);
            mVertexBuffer.order(ByteOrder.nativeOrder());
            mVertexBuffer.asFloatBuffer().put(vert);
            mVertexBuffer.position(0);
        }

        if (mTextureBuffer == null) {
            mTextureBuffer = ByteBuffer.allocateDirect(coord.length * 4);
            mTextureBuffer.order(ByteOrder.nativeOrder());
            mTextureBuffer.asFloatBuffer().put(coord);
            mTextureBuffer.position(0);
        }
    }

    @Override
    public void draw(float[] matrix, ByteBuffer yBuffer, ByteBuffer uBuffer, ByteBuffer vBuffer, int width, int height) {
        bb_y = yBuffer;
        bb_u = uBuffer;
        bb_v = vBuffer;

        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glVertexAttribPointer(aPositionLocation, 2,
                GLES20.GL_FLOAT, false, 8, mVertexBuffer);
        GLHelpers.checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");


        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, 8,
                mTextureBuffer);
        GLHelpers.checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        GLHelpers.checkGlError("glEnableVertexAttribArray");

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

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocation);
    }
}
