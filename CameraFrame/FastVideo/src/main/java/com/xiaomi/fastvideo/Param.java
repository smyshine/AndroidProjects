package com.xiaomi.fastvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by livy on 16/5/16.
 */
public class Param {
    public	int handle;// gpu端句柄
    public	String name;

    public Param(String name) {
        this.name = name;
    }

    public void setParams(int program) {
        handle = GLES20.glGetUniformLocation(program, name);
    }

    public void clear() {

    }

    @Override
    public String toString() {
        return name;
    }



    public static class FloatParam extends Param {
        float value;

        public FloatParam(String name, float value) {
            super(name);
            this.value = value;
        }

        public float value() {
            return value;
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle < 0)
                return;
            GLES20.glUniform1f(handle, value);
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    public static class IntParam extends Param {
        int value;

        @Override
        public String toString() {
            return name + "=" + value;
        }

        public IntParam(String name, int value) {
            super(name);
            this.value = value;
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle < 0)
                return;
            GLES20.glUniform1i(handle, value);
        }
    }

    public static class FloatsParam extends Param {
        float value[];

        public FloatsParam(String name, float[] value) {
            super(name);
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=" + value.toString();
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle < 0)
                return;
            switch (value.length) {
                case 1:
                    GLES20.glUniform1f(handle, value[0]);
                    break;
                case 2:
                    GLES20.glUniform2fv(handle, 1, value, 0);
                    break;
                case 3:
                    GLES20.glUniform3fv(handle, 1, value, 0);
                    break;
                case 4:
                    GLES20.glUniform4fv(handle, 1, value, 0);
                    break;
                // TODO 2*2 和4冲突 个数一样
                case 9:
                    GLES20.glUniformMatrix3fv(handle, 1, false, value, 0);
                    break;
                case 16:
                    GLES20.glUniformMatrix4fv(handle, 1, false, value, 0);
                    break;
                default:
                    break;
            }
        }
    }

    public static class RectParam extends Param {
        float value[];

        public RectParam(String name, float[] value) {
            super(name);
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=" + value.toString();
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle < 0)
                return;
            GLES20.glUniform4fv(handle, value.length / 4, value, 0);
        }
    }

    public static class VarFloatParam extends FloatParam {
        float min;
        float max;

        public VarFloatParam(String name, float value, float min, float max) {
            super(name, value);
            this.min = min;
            this.max = max;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public void setProgress(int progress) {
            value = min + ((float) progress) * (max - min) / 100;
        }

        public int getProgress() {
            return (int) ((value - min) * 100 / (max - min));
        }

    }

    public static class HueFloatParam extends VarFloatParam {
        ColorSpaceMatrix mColorSpaceMatrix;
        float[] mMatrix;

        public HueFloatParam(String name, float value, float min, float max) {
            super(name, value, min, max);
            mColorSpaceMatrix = new ColorSpaceMatrix();
            mMatrix = mColorSpaceMatrix.getMatrix();
        }

        @Override
        public void setParams(int program) {
            handle = GLES20.glGetUniformLocation(program, name);

            mColorSpaceMatrix.identity();
            mColorSpaceMatrix.setHue(value);
            mMatrix = mColorSpaceMatrix.getMatrix();
            GLES20.glUniformMatrix4fv(handle, 1, false, mMatrix, 0);
        }

        @Override
        public String toString() {
            return name + "=" + mMatrix.toString();
        }
    }

    public static class TextureParam extends Param {
        Bitmap textureBitmap;
        int textureId;
        int[] texture = { 0 };

        public TextureParam(String name, Bitmap textureBitmap, int textureId) {
            super(name);
            this.textureBitmap = textureBitmap;
            this.textureId = textureId;
        }

        @Override
        public void clear() {
            super.clear();
            GLES20.glActiveTexture(textureId);
            GLES20.glDeleteTextures(1, texture, 0);
            texture[0] = 0;
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle == 0 || textureBitmap == null)
                return;
            GLES20.glActiveTexture(textureId);

            GLES20.glGenTextures(1, texture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
            GlslFilter.checkGlError("texImage2D");
            int textureIndex = 0;
            switch (textureId) {
                case GLES20.GL_TEXTURE0:
                    textureIndex = 0;
                    break;
                case GLES20.GL_TEXTURE1:
                    textureIndex = 1;
                    break;
                case GLES20.GL_TEXTURE2:
                    textureIndex = 2;
                    break;
                case GLES20.GL_TEXTURE3:
                    textureIndex = 3;
                    break;
                case GLES20.GL_TEXTURE4:
                    textureIndex = 4;
                    break;
                case GLES20.GL_TEXTURE5:
                    textureIndex = 5;
                    break;
                case GLES20.GL_TEXTURE6:
                    textureIndex = 6;
                    break;
                case GLES20.GL_TEXTURE7:
                    textureIndex = 7;
                    break;

                default:
                    break;
            }
            GLES20.glUniform1i(handle, textureIndex);
            GlslFilter.checkGlError("set texture:" + textureIndex);
        }
    }

    public static class TextureValueParam extends Param {
        int textureValue;
        int textureId;

        public TextureValueParam(String name, int textureValue, int textureId) {
            super(name);
            this.textureValue = textureValue;
            this.textureId = textureId;
        }

        @Override
        public void clear() {
            super.clear();
            GLES20.glActiveTexture(textureId);
        }

        @Override
        public void setParams(int program) {
            super.setParams(program);
            if (handle == 0 || textureValue == 0)
                return;
            GLES20.glActiveTexture(textureId);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureValue);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //
            // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
            GlslFilter.checkGlError("texImage2D");
            int textureIndex = 0;
            switch (textureId) {
                case GLES20.GL_TEXTURE0:
                    textureIndex = 0;
                    break;
                case GLES20.GL_TEXTURE1:
                    textureIndex = 1;
                    break;
                case GLES20.GL_TEXTURE2:
                    textureIndex = 2;
                    break;
                case GLES20.GL_TEXTURE3:
                    textureIndex = 3;
                    break;
                case GLES20.GL_TEXTURE4:
                    textureIndex = 4;
                    break;
                case GLES20.GL_TEXTURE5:
                    textureIndex = 5;
                    break;
                case GLES20.GL_TEXTURE6:
                    textureIndex = 6;
                    break;
                case GLES20.GL_TEXTURE7:
                    textureIndex = 7;
                    break;

                default:
                    break;
            }
            GLES20.glUniform1i(handle, textureIndex);
            GlslFilter.checkGlError("set texture:" + textureIndex);
        }
    }

    public static class TextureFileParam extends TextureParam {
        Context mContext;
        String mTextureFile;

        public TextureFileParam(String name, String textureFile,
                                Context context, int textureId) {
            super(name, null, textureId);
            mContext = context;
            mTextureFile = textureFile;
        }

        @Override
        public void setParams(int program) {
            if (textureBitmap == null) {
                InputStream is;
                try {
                    is = mContext.getAssets().open(mTextureFile);
                    textureBitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.setParams(program);
        }

    }
}
