package smy.com.vrplayer.objects.bitmap;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import smy.com.vrplayer.R;
import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.objects.base.BasePanoRectObject;


/**
 * Created by hzb on 17-6-10.
 */

public class PanoBitmapRectMatrix extends BasePanoRectObject {

    public PanoBitmapRectMatrix(int screenW, int screenH,
                                Bitmap bitmap){
        super(screenW,screenH);
        String fragShader = ShaderProgram.fetchShader(R.raw.pano_rect_frag_shader);
        String vertShader = ShaderProgram.fetchShader(R.raw.rect_vert_shader);
        createProgram(vertShader,fragShader);
        singleTex = GLHelpers.loadTexture(bitmap);
    }

    @Override
    protected void initUniformHandler(){
        super.initUniformHandler();
        singleTextureHandle = mShaderProgram.getUniform("singleTexture");
    }

    @Override
    protected void activeTexture() {
        GLES20.glUniform1i(singleTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, singleTex);
    }

}
