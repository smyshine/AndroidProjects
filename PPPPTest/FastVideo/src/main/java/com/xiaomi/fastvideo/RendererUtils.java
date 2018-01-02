
package com.xiaomi.fastvideo;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.renderscript.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;

/**
 * Utils for GL renderer.
 */
public class RendererUtils {

    public static class RenderContext {
        private int shaderProgram;
        private int texSamplerHandle;
        private int alphaHandle;
        private int texCoordHandle;
        private int posCoordHandle;
        private FloatBuffer texVertices;
        private FloatBuffer posVertices;
        private float alpha = 1f;
        private int modelViewMatHandle;
        float[] mModelViewMat = {
                1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0,
                0, 1
        };
    }

    public static class FilterContext {
        public int shaderProgram;
        public int texSamplerHandle;
        public int texCoordHandle;
        public int posCoordHandle;
        public FloatBuffer texVertices;
        public FloatBuffer posVertices;
    }

    private static int[] frame = new int[1];

    private static final float[] TEX_VERTICES = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
    };

    private static final float[] POS_VERTICES = {
            -1.0f, -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final String VERTEX_SHADER = "attribute vec4 a_position;\n"
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_model_view; \n"
            + "varying vec2 v_texcoord;\n"
            + "void main() {\n" + "  gl_Position = u_model_view*a_position;\n"
            + "  v_texcoord = a_texcoord;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform sampler2D tex_sampler;\n"
            + "uniform float alpha;\n"
            + "varying vec2 v_texcoord;\n"
            + "void main() {\n"
            + "vec4 color = texture2D(tex_sampler, v_texcoord);\n"
            + "gl_FragColor = color;\n"
            + "}\n";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final float DEGREE_TO_RADIAN = (float) Math.PI / 180.0f;

    public static int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        checkGlError("glGenTextures");
        return textures[0];
    }

    public static int createTexture(Bitmap bitmap) {
        int texture = createTexture();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, bitmap, type, 0);
        // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("texImage2D");

        return texture;
    }

    public static int createTexture(int texture, Bitmap bitmap) {
        if (texture < 0) {
            texture = createTexture();
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, bitmap, type, 0);
        // GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("texImage2D");

        return texture;
    }

    public static Bitmap saveTexture(int texture, int width, int height) {
        int[] frame = new int[1];
        GLES20.glGenFramebuffers(1, frame, 0);
        checkGlError("glGenFramebuffers");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);
        checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);
        checkGlError("glFramebufferTexture2D");

        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        checkGlError("glReadPixels");
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        checkGlError("glBindFramebuffer");
        GLES20.glDeleteFramebuffers(1, frame, 0);
        checkGlError("glDeleteFramebuffer");
        return bitmap;
    }

    public static void clearTexture(int texture) {
        int[] textures = new int[1];
        textures[0] = texture;
        GLES20.glDeleteTextures(textures.length, textures, 0);
        checkGlError("glDeleteTextures");
    }

    private static float[] getFitVertices(int srcWidth, int srcHeight,
            int dstWidth, int dstHeight) {
        float srcAspectRatio = ((float) srcWidth) / srcHeight;
        float dstAspectRatio = ((float) dstWidth) / dstHeight;
        float relativeAspectRatio = dstAspectRatio / srcAspectRatio;

        float[] vertices = new float[8];
        System.arraycopy(POS_VERTICES, 0, vertices, 0, vertices.length);
        if (relativeAspectRatio > 1.0f) {
            // Screen is wider than the camera, scale down X
            vertices[0] /= relativeAspectRatio;
            vertices[2] /= relativeAspectRatio;
            vertices[4] /= relativeAspectRatio;
            vertices[6] /= relativeAspectRatio;
        } else {
            vertices[1] *= relativeAspectRatio;
            vertices[3] *= relativeAspectRatio;
            vertices[5] *= relativeAspectRatio;
            vertices[7] *= relativeAspectRatio;
        }
        return vertices;
    }

    public static void setRenderMatrix(RenderContext context, float[] matrix) {
        context.mModelViewMat = matrix;
    }

    public static void setRenderToFit(RenderContext context, int srcWidth,
            int srcHeight, int dstWidth, int dstHeight) {
        context.posVertices = createVerticesBuffer(getFitVertices(srcWidth,
                srcHeight, dstWidth, dstHeight));
    }

    public static void setRenderToFit(RenderContext context, int srcWidth,
            int srcHeight, int dstWidth, int dstHeight, float offsetx, float offsetY, float scale) {
        Matrix4f matrix4f = new Matrix4f();

        float srcAspectRatio = ((float) srcWidth) / srcHeight;
        float dstAspectRatio = ((float) dstWidth) / dstHeight;
        float relativeAspectRatio = dstAspectRatio / srcAspectRatio;
        float ratioscale = 1.0f;
        if (relativeAspectRatio > 1.0f) {
            ratioscale = srcAspectRatio / dstAspectRatio;
            matrix4f.scale(ratioscale * scale, scale, 0);
            float x = -offsetx / (srcHeight * scale);
            float y = offsetY / (srcHeight * scale);
            matrix4f.translate(x, y, 0);
        } else {
            ratioscale = relativeAspectRatio;
            matrix4f.scale(scale, ratioscale * scale, 0);
            float x = -offsetx / (srcWidth * scale);
            float y = offsetY / (srcWidth * scale);
            matrix4f.translate(x, y, 0);
        }

        context.mModelViewMat = matrix4f.getArray();
    }

    public static void setRenderToAlpha(RenderContext context, int alpha) {
        context.alpha = (float) alpha / 255;
    }

    public static void setRenderToRotate(RenderContext context, int srcWidth,
            int srcHeight, int dstWidth, int dstHeight, float degrees) {
        float radian = -degrees * DEGREE_TO_RADIAN;
        float cosTheta = (float) Math.cos(radian);
        float sinTheta = (float) Math.sin(radian);
        float cosWidth = cosTheta * srcWidth;
        float sinWidth = sinTheta * srcWidth;
        float cosHeight = cosTheta * srcHeight;
        float sinHeight = sinTheta * srcHeight;

        float[] vertices = new float[8];
        vertices[0] = -cosWidth + sinHeight;
        vertices[1] = -sinWidth - cosHeight;
        vertices[2] = cosWidth + sinHeight;
        vertices[3] = sinWidth - cosHeight;
        vertices[4] = -vertices[2];
        vertices[5] = -vertices[3];
        vertices[6] = -vertices[0];
        vertices[7] = -vertices[1];

        float maxWidth = Math.max(Math.abs(vertices[0]), Math.abs(vertices[2]));
        float maxHeight = Math
                .max(Math.abs(vertices[1]), Math.abs(vertices[3]));
        float scale = Math.min(dstWidth / maxWidth, dstHeight / maxHeight);

        for (int i = 0; i < 8; i += 2) {
            vertices[i] *= scale / dstWidth;
            vertices[i + 1] *= scale / dstHeight;
        }
        context.posVertices = createVerticesBuffer(vertices);
    }

    public static void setRenderToFlip(RenderContext context, int srcWidth,
            int srcHeight, int dstWidth, int dstHeight,
            float horizontalDegrees, float verticalDegrees) {
        // Calculate the base flip coordinates.
        float[] base = getFitVertices(srcWidth, srcHeight, dstWidth, dstHeight);
        int horizontalRounds = (int) horizontalDegrees / 180;
        if (horizontalRounds % 2 != 0) {
            base[0] = -base[0];
            base[4] = base[0];
            base[2] = -base[2];
            base[6] = base[2];
        }
        int verticalRounds = (int) verticalDegrees / 180;
        if (verticalRounds % 2 != 0) {
            base[1] = -base[1];
            base[3] = base[1];
            base[5] = -base[5];
            base[7] = base[5];
        }

        float length = 5;
        float[] vertices = new float[8];
        System.arraycopy(base, 0, vertices, 0, vertices.length);
        if (horizontalDegrees % 180f != 0) {
            float radian = (horizontalDegrees - horizontalRounds * 180)
                    * DEGREE_TO_RADIAN;
            float cosTheta = (float) Math.cos(radian);
            float sinTheta = (float) Math.sin(radian);

            float scale = length / (length + sinTheta * base[0]);
            vertices[0] = cosTheta * base[0] * scale;
            vertices[1] = base[1] * scale;
            vertices[4] = vertices[0];
            vertices[5] = base[5] * scale;

            scale = length / (length + sinTheta * base[2]);
            vertices[2] = cosTheta * base[2] * scale;
            vertices[3] = base[3] * scale;
            vertices[6] = vertices[2];
            vertices[7] = base[7] * scale;
        }

        if (verticalDegrees % 180f != 0) {
            float radian = (verticalDegrees - verticalRounds * 180)
                    * DEGREE_TO_RADIAN;
            float cosTheta = (float) Math.cos(radian);
            float sinTheta = (float) Math.sin(radian);

            float scale = length / (length + sinTheta * base[1]);
            vertices[0] = base[0] * scale;
            vertices[1] = cosTheta * base[1] * scale;
            vertices[2] = base[2] * scale;
            vertices[3] = vertices[1];

            scale = length / (length + sinTheta * base[5]);
            vertices[4] = base[4] * scale;
            vertices[5] = cosTheta * base[5] * scale;
            vertices[6] = base[6] * scale;
            vertices[7] = vertices[5];
        }

        context.posVertices = createVerticesBuffer(vertices);
    }

    public static void renderBackground() {
        GLES20.glClearColor(0.10588f, 0.109804f, 0.12157f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public static void renderTexture(RenderContext context, int texture,
            int viewWidth, int viewHeight) {
        // Use our shader program
        GLES20.glUseProgram(context.shaderProgram);
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            createProgram();
            checkGlError("createProgram");
        }

        // Set viewport
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        checkGlError("glViewport");

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND);
        // GLES20.glEnable(GLES20.GL_BLEND);
        // GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
        // GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(context.texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.texVertices);
        GLES20.glEnableVertexAttribArray(context.texCoordHandle);
        GLES20.glVertexAttribPointer(context.posCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.posVertices);
        GLES20.glEnableVertexAttribArray(context.posCoordHandle);
        checkGlError("vertex attribute setup");

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glBindTexture");
        GLES20.glUniform1i(context.texSamplerHandle, 0);
        GLES20.glUniform1f(context.alphaHandle, context.alpha);
        GLES20.glUniformMatrix4fv(context.modelViewMatHandle, 1, false, context.mModelViewMat,
                0);
        checkGlError("modelViewMatHandle");
        // Draw!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();
    }

    private static RenderContext createProgram(float[] vertex, float[] tex) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        if (vertexShader == 0) {
            return null;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        if (pixelShader == 0) {
            return null;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String info = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                program = 0;
                throw new RuntimeException("Could not link program: " + info);
            }
        }

        // Bind attributes and uniforms
        RenderContext context = new RenderContext();
        context.texSamplerHandle = GLES20.glGetUniformLocation(program,
                "tex_sampler");
        context.alphaHandle = GLES20.glGetUniformLocation(program, "alpha");
        context.texCoordHandle = GLES20.glGetAttribLocation(program,
                "a_texcoord");
        context.posCoordHandle = GLES20.glGetAttribLocation(program,
                "a_position");
        context.modelViewMatHandle = GLES20.glGetUniformLocation(program,
                "u_model_view");
        context.texVertices = createVerticesBuffer(tex);
        context.posVertices = createVerticesBuffer(vertex);

        context.shaderProgram = program;
        return context;
    }

    public static RenderContext createProgram() {
        return createProgram(POS_VERTICES, TEX_VERTICES);
    }
    
    public static void releaseRenderContext(RenderContext context){
        if(context != null && context.shaderProgram>0){
            GLES20.glDeleteProgram(context.shaderProgram);
            context.shaderProgram = 0;
        }
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String info = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                shader = 0;
                throw new RuntimeException("Could not compile shader "
                        + shaderType + ":" + info);
            }
        }
        return shader;
    }

    public static FloatBuffer createVerticesBuffer(float[] vertices) {
        if (vertices.length != 8) {
            throw new RuntimeException("Number of vertices should be four.");
        }

        FloatBuffer buffer = ByteBuffer
                .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(vertices).position(0);
        return buffer;
    }

    public static String getEGLErrorString(int error) {
        switch (error) {
            case EGL10.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL10.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL10.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL10.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL10.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL10.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL10.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL10.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL10.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL10.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL10.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL10.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL11.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return " " + error;
        }
    }

    public static void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            // throw new RuntimeException(op + ": glError " + error);
            MiLog.e("RendererUtils", op + ": glError " + getEGLErrorString(error));
            java.util.Map<Thread, StackTraceElement[]> ts = Thread
                    .getAllStackTraces();
            StackTraceElement[] ste = ts.get(Thread.currentThread());
            for (StackTraceElement s : ste) {
                MiLog.e("SS     ", s.toString());
            }
        }
    }

    public static void createFrame() {
        GLES20.glGenFramebuffers(1, RendererUtils.frame, 0);
        checkGlError("glGenFramebuffers");
    }

    public static void deleteFrame() {
        GLES20.glDeleteFramebuffers(1, frame, 0);
        checkGlError("glDeleteFramebuffer");
    }

    public static int createFilterProgram(String vertexSource,
            String fragSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexSource == null ? VERTEX_SHADER : vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String info = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                program = 0;
                throw new RuntimeException("Could not link program: " + info);
            }
        }

        return program;
    }

    public static void renderTexture2FBO(FilterContext context, int texture,
            int dstTexture, int viewWidth, int viewHeight) {

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glActiveTexture");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, dstTexture);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, viewWidth,
                viewHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        checkGlError("glTexImage2D");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frame[0]);
        checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, dstTexture,
                0);
        checkGlError("glFramebufferTexture2D");

        // Set viewport
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        checkGlError("glViewport");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use our shader program
        GLES20.glUseProgram(context.shaderProgram);

        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            checkGlError("createProgram");
        }

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(context.texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.texVertices);
        GLES20.glEnableVertexAttribArray(context.texCoordHandle);
        GLES20.glVertexAttribPointer(context.posCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, context.posVertices);
        GLES20.glEnableVertexAttribArray(context.posCoordHandle);
        checkGlError("vertex attribute setup");
        GLES20.glUniform1i(context.texSamplerHandle, 0);
        checkGlError("glUniform1i");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        checkGlError("glBindTexture");
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // Draw!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();

        // GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
        // GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // checkGlError("glBindTexture");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        checkGlError("glBindFramebuffer");
        deleteProgram(context.shaderProgram);
    }

    public static void deleteProgram(int id) {
        GLES20.glDeleteProgram(id);
    }
}
