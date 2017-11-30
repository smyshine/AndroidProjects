package smy.com.vrplayer.objects.yuv;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

import smy.com.vrplayer.R;
import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.objects.base.BasePanoRectObject;

/**
 * Created by hzb on 17-6-10.
 */

public class PanoYUVRectObject extends BasePanoRectObject {
    protected int[] id_y = new int[1];
    protected int[] id_u = new int[1];
    protected int[] id_v = new int[1];

    protected ByteBuffer bb_y;
    protected ByteBuffer bb_u;
    protected ByteBuffer bb_v;

    protected int mTextureUniformY, mTextureUniformU, mTextureUniformV;
    private int mFrameWidth, mFrameHeight;

    public PanoYUVRectObject(int screenW, int screenH){
        super(screenW,screenH);
        String fragShader = ShaderProgram.fetchShader(R.raw.pano_yuv_rect_frag_shader);
        String vertShader = ShaderProgram.fetchShader(R.raw.rect_vert_shader);
        createProgram(vertShader,fragShader);
        GLHelpers.initTexture(id_y, id_u, id_v);
    }

    @Override
    protected void initUniformHandler(){
        super.initUniformHandler();
        mTextureUniformY = mShaderProgram.getUniform("tex_y");
        mTextureUniformU = mShaderProgram.getUniform("tex_u");
        mTextureUniformV = mShaderProgram.getUniform("tex_v");
    }

    @Override
    protected void activeTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE0");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_y[0]);
        GLHelpers.checkGlError("glBindTexture id_y");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                mFrameWidth, mFrameHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_y);
        GLHelpers.checkGlError("glTexImage2D bb_y");
        GLES20.glUniform1i(mTextureUniformY, 0);
        GLHelpers.checkGlError("glUniform1i mTextureUniformY");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE1");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_u[0]);
        GLHelpers.checkGlError("glBindTexture id_u");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                mFrameWidth / 2, mFrameHeight / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_u);
        GLES20.glUniform1i(mTextureUniformU, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLHelpers.checkGlError("glActiveTexture GL_TEXTURE2");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_v[0]);
        GLHelpers.checkGlError("glBindTexture id_v");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                mFrameWidth / 2, mFrameHeight / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, bb_v);
        GLES20.glUniform1i(mTextureUniformV, 2);
    }

    @Override
    public void draw(float[] matrix, ByteBuffer yBuffer, ByteBuffer uBuffer, ByteBuffer vBuffer,
                     int frameWidth, int frameHeight) {
        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;
        bb_y = yBuffer;
        bb_u = uBuffer;
        bb_v = vBuffer;
        bb_y.position(0);
        bb_u.position(0);
        bb_v.position(0);
        super.draw(matrix,null, 0);
    }
}
