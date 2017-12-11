package smy.com.vrplayer.objects.video;

import android.opengl.GLES20;

import smy.com.vrplayer.R;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.objects.base.BasePanoRectObject;


/**
 * Created by hzb on 17-6-10.
 */

public class PanoVideoRectMatrix extends BasePanoRectObject {
    private final int[] sUseSTMatrix = new int[]{1};
    public PanoVideoRectMatrix(int screenW, int screenH){
        super(screenW,screenH);
        String fragShader = ShaderProgram.fetchShader(R.raw.pano_rect_frag_shader);
        fragShader = fragShader.replace("sampler2D","samplerExternalOES");
        fragShader = "#extension GL_OES_EGL_image_external : require\n" + fragShader;
        String vertShader = ShaderProgram.fetchShader(R.raw.rect_vert_shader);
        createProgram(vertShader,fragShader);
    }

    @Override
    protected void initUniformHandler(){
        super.initUniformHandler();
        singleTextureHandle = mShaderProgram.getUniform("singleTexture");
    }

    @Override
    protected void setUniform(float[] modelMatrix, float[] stMatrix){
        super.setUniform(modelMatrix, stMatrix);
        GLES20.glUniform1iv(mUseSTMatrixHandler,1,sUseSTMatrix,0);
        GLES20.glUniformMatrix4fv(mSTMatrixHandler,1,false,stMatrix,0);
    }

    @Override
    protected void activeTexture() {
        GLES20.glUniform1i(singleTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, singleTex);
    }
}
