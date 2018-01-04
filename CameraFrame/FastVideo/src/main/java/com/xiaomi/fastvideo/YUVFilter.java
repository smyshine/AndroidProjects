
package com.xiaomi.fastvideo;

import android.content.Context;
import android.opengl.GLES20;

public class YUVFilter extends GlslFilter {
    private final String YUV_FRAGMENT_SHADER_STRING =
            "precision mediump float;\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform sampler2D y_tex;\n" +
                    "uniform sampler2D u_tex;\n" +
                    "uniform sampler2D v_tex;\n" +
                    "void main() {\n" +
                    // CSC according to http://www.fourcc.org/fccyvrgb.php
                    "  float y = texture2D(y_tex, textureCoordinate).r;\n" +
                    "  float u = texture2D(u_tex, textureCoordinate).r - 0.5;\n" +
                    "  float v = texture2D(v_tex, textureCoordinate).r - 0.5;\n" +
                    "  gl_FragColor = vec4(y + 1.403 * v, " +
                    "                      y - 0.344 * u - 0.714 * v, " +
                    "                      y + 1.77 * u, 1.0);\n" +
                    "}\n";

    int texYHandle;
    int texUHandle;
    int texVHandle;
    
    int[] textures;

    public YUVFilter(Context context) {
        super(context);
    }

    @Override
    public String fragmentShader() {
        return YUV_FRAGMENT_SHADER_STRING;
    }

    @Override
    protected void prepareParams() {
        super.prepareParams();
        
        texYHandle= GLES20.glGetUniformLocation(shaderProgram, "y_tex");
        texUHandle= GLES20.glGetUniformLocation(shaderProgram, "u_tex");
        texVHandle= GLES20.glGetUniformLocation(shaderProgram, "v_tex");
    }
    @Override
    protected void updateParams() {
        super.updateParams();
        checkGlError("setYuvTextures");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(texYHandle, 0);
        checkGlError("glBindTexture y");
        // /
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(texUHandle, 1);
        checkGlError("glBindTexture u");
        //
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(texVHandle, 2);
        checkGlError("glBindTexture v");
        
//        GLES20.glUniform1i(texYHandle, 0);
//        GLES20.glUniform1i(texUHandle, 1);
//        GLES20.glUniform1i(texVHandle, 2);
//        checkGlError("glBindTexture y");
    }

    public void setYuvTextures(int mYUVTextures[]) {
        textures = mYUVTextures;
    }
}
