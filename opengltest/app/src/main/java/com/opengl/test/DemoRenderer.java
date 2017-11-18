package com.opengl.test;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by SMY on 2017/11/18.
 */

public class DemoRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int aPostionHandler;
    private int uMatrixHandler;
    private int aTextureCoordHandler;
    private int uTextureSamplerHandler;

    private int programId;
    private int textureId;

    private final float[] vertexData = {
            0f, 0f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, -1f, 0f
    };

    private final short[] indexData = {
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 1
    };

    private final float[] textureVertexData = {
            0.5f, 0.5f,
            1f, 0f,
            0f, 0f,
            0f, 1f,
            1f, 1f
    };

    private final float[] projectionMatrix = new float[16];
    private ShortBuffer indexBuffer;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureVertexBuffer;

    public DemoRenderer(Context context) {
        this.context = context;

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indexData.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indexData);
        indexBuffer.position(0);

        uTextureSamplerHandler = GLES20.glGetUniformLocation(programId, "sTexture");
        aTextureCoordHandler = GLES20.glGetAttribLocation(programId, "aTextCoord");
        uMatrixHandler = GLES20.glGetUniformLocation(programId, "uMatrix");
        aPostionHandler = GLES20.glGetAttribLocation(programId, "aPosition");
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        String vertexShader = TextureHelper.readRawText(context, R.raw.vertex_shader);
        String fragmentShader = TextureHelper.readRawText(context, R.raw.fragment_shader);
        programId = TextureHelper.createProgram(vertexShader, fragmentShader);

        textureId = TextureHelper.loadTexture(context, R.drawable.reminiscence);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        /*float ratio = width > height?
                (float) width / height:
                (float) height / width;
        if (width > height){
            Matrix.orthoM(projectionMatrix,0,-ratio,ratio,-1f,1f,-1f,1f);
        } else {
            Matrix.orthoM(projectionMatrix,0,-1f,1f,-ratio,ratio,-1f,1f);
        }*/
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programId);
        //GLES20.glUniformMatrix4fv(uMatrixHandler, 1, false, projectionMatrix, 0);

        GLES20.glEnableVertexAttribArray(aPostionHandler);
        GLES20.glVertexAttribPointer(aPostionHandler, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        /*GLES20.glEnableVertexAttribArray(aTextureCoordHandler);
        GLES20.glVertexAttribPointer(aTextureCoordHandler, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glUniform1i(uTextureSamplerHandler, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexData.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);*/
    }
}
