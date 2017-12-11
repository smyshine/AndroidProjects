package smy.com.vrplayer.objects.video;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.objects.base.XYVRBaseObject;

/**
 * Created by LRW on 2017/5/16.
 */

@Deprecated
/*
* @deprecated since 2017-06-28
* use {@link PanoVideoRectMatrix}
* */
public class PanoVideoRectAngle extends XYVRBaseObject {

    protected int mMVPMatrixHandle;

    private static float[] squareVertices = {
            -1.0f, -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f, 1.0f
    }; // fullscreen

    private static float[] coordVertices = {
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 0.0f
    };// whole-texture

    private ShaderProgram mShaderProgram;

    private ByteBuffer mVertexBuffer;
    private ByteBuffer mTextureBuffer;

    private int aPositionLocation;
    private int aTextureCoordLocation;



    private final String mVertexShader =
                    "attribute vec4 vPosition;                         		\n" +
                    "attribute vec2 TexCoordIn;                       		\n" +
                    "varying vec2 TexCoordOut;                        		\n" +
                    "void main(void)                                  		\n" +
                    "{                                                		\n" +
                    "    gl_Position = vPosition ;              \n" +
                    "    TexCoordOut = TexCoordIn;                   		\n" +
                    "}                                               		\n";


    private static final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 TexCoordOut;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, TexCoordOut);\n" +
                    "}\n";

    public PanoVideoRectAngle(){
        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);
        createBuffers(squareVertices,coordVertices);
        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);

        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        aTextureCoordLocation = mShaderProgram.getAttribute("TexCoordIn");
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
    public void draw(float[] modelMatrix, float[] stMatrix,  int eyeType) {
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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, singleTex);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordLocation);
    }
}
