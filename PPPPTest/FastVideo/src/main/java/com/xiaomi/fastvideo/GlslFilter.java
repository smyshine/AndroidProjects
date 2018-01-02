
package com.xiaomi.fastvideo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

public class GlslFilter extends Filter{

    private static final int FLOAT_SIZE_BYTES = 4;
    // private static final float DEGREE_TO_RADIAN = (float) Math.PI / 180.0f;
    private static final float[] TEX_VERTICES = {
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f
    };
    private static final float[] TEX_VERTICES_SURFACE_TEXTURE = {
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f
    };

    private static final float[] POS_VERTICES = {
            -1.0f, -1.0f, 0.0f, -1.0f,
            1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f
    };

    private static final float IDENTIFY_MATRIX[] = {
            1, 0, 0, 0, 0, 1, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 1
    };

    private static final String VERTEX_SHADER = "attribute vec4 a_position;\n"
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_texture_mat; \n"
            + "uniform mat4 u_model_view; \n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_Position = u_model_view*a_position;\n"
            + "  vec4 tmp = u_texture_mat*vec4(a_texcoord.x,a_texcoord.y,0.0,1.0);\n"
            + "  textureCoordinate = tmp.xy;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform sampler2D inputImageTexture;\n" + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" + "}\n";
    // Shader for output
    private final String SURFACE_TEXTURE_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES inputImageTexture;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" + "}\n";

    // GLES20.GL_TEXTURE_2D
    // GL_TEXTURE_2D GL_TEXTURE_EXTERNAL_OES
    public static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    public static final int GL_TEXTURE_2D = GLES20.GL_TEXTURE_2D;
    int mInputTextureType = GL_TEXTURE_2D;
    protected int shaderProgram;
    private int texSamplerHandle;
    private int texCoordHandle;
    private int posCoordHandle;
    private FloatBuffer texVertices;
    private FloatBuffer posVertices;
    private int texCoordMatHandle;
    private int modelViewMatHandle;
    private int[] frameBufferObjectId = {
        0
    };
    final float[] mTextureMat = {
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0,
            1
    };
    final float[] mModelViewMat = {
            1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0,
            0, 1
    };

    boolean isInitialed = false;
    GlslFilter mNextGlslFilter;
    Photo mMiddlePhoto;

    protected Context mContext;

    public GlslFilter(Context context) {
        mContext = context;
    }

    public String vertexShader() {
        return VERTEX_SHADER;
    }

    public String fragmentShader() {
        if (mInputTextureType == GL_TEXTURE_2D)
            return FRAGMENT_SHADER;
        else
            return SURFACE_TEXTURE_FRAGMENT_SHADER;
    }

    public void setNextFilter(GlslFilter filter){
        mNextGlslFilter = filter;
    }

    // GL_TEXTURE_2D 或者 GL_TEXTURE_EXTERNAL_OES
    final public void setType(int type) {
        mInputTextureType = type;
    }

    final public void initial() {
        if(isInitialed)
            return;
        isInitialed = true;
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader());
        if (vertexShader == 0) {
            throw new RuntimeException("Could not load vertex shader: "
                    + vertexShader());
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShader());
        if (pixelShader == 0) {
            throw new RuntimeException("Could not load fragment shader: "
                    + fragmentShader());
        }

        shaderProgram = GLES20.glCreateProgram();
        if (shaderProgram != 0) {
            GLES20.glAttachShader(shaderProgram, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(shaderProgram, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(shaderProgram);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS,
                    linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String info = GLES20.glGetProgramInfoLog(shaderProgram);
                GLES20.glDeleteProgram(shaderProgram);
                shaderProgram = 0;
                throw new RuntimeException("Could not link program: " + info);
            }
        } else {
            throw new RuntimeException("Could not create program");
        }

        // Bind attributes and uniforms
        texSamplerHandle = GLES20.glGetUniformLocation(shaderProgram,
                "inputImageTexture");
        texCoordHandle = GLES20
                .glGetAttribLocation(shaderProgram, "a_texcoord");
        posCoordHandle = GLES20
                .glGetAttribLocation(shaderProgram, "a_position");
        texCoordMatHandle = GLES20.glGetUniformLocation(shaderProgram,
                "u_texture_mat");
        modelViewMatHandle = GLES20.glGetUniformLocation(shaderProgram,
                "u_model_view");
        
        if (mInputTextureType == GL_TEXTURE_2D) {
            texVertices = createVerticesBuffer(TEX_VERTICES);
        } else {
            texVertices = createVerticesBuffer(TEX_VERTICES_SURFACE_TEXTURE);

        }

        posVertices = createVerticesBuffer(POS_VERTICES);
        prepareParams();

        if(mNextGlslFilter!=null){
            mNextGlslFilter.initial();
        }
    }

    protected void prepareParams() {

    }

    protected void updateParams() {

    }

    protected void doRelease() {

    }

    final public void release() {
        if(!isInitialed)
            return;
        isInitialed = false;
        if(mMiddlePhoto!=null){
            mMiddlePhoto.clear();
            mMiddlePhoto = null;
        }
        if(mNextGlslFilter!=null){
            mNextGlslFilter.release();
        }
        doRelease();
        if (shaderProgram > 0) {
            GLES20.glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }
        if (frameBufferObjectId[0] > 0) {
            GLES20.glDeleteFramebuffers(1, frameBufferObjectId, 0);
            frameBufferObjectId[0] = 0;
        }
    }

    final public void updateModelViewMatrix(float[] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            mModelViewMat[i] = matrix[i];
        }
    }

    final public void flipXModelView() {
        mModelViewMat[0] = -mModelViewMat[0];
        mModelViewMat[1] = -mModelViewMat[1];
        mModelViewMat[2] = -mModelViewMat[2];
        mModelViewMat[3] = -mModelViewMat[3];
    }

    final public void flipYModelView() {
        mModelViewMat[4] = -mModelViewMat[4];
        mModelViewMat[5] = -mModelViewMat[5];
        mModelViewMat[6] = -mModelViewMat[6];
        mModelViewMat[7] = -mModelViewMat[7];
    }

    final public void rotationModelView(int rotation) {
        updateModelViewMatrix(IDENTIFY_MATRIX);
        float c = (float) Math.cos((double) rotation * 3.1415926 / 180.0);
        float s = (float) Math.sin((double) rotation * 3.1415926 / 180.0);
        mModelViewMat[0] = c;
        mModelViewMat[1] = -s;
        mModelViewMat[4] = s;
        mModelViewMat[5] = c;
    }

    final public void updateTextureMatrix(float[] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            mTextureMat[i] = matrix[i];
        }
    }

    public void process(Photo in, Photo out){
        if (mNextGlslFilter == null) {
            processInner(in, out);
        } else {
            if(mMiddlePhoto==null){
                Photo tmp = in;
                if(tmp==null){
                    tmp = out;
                }
                if(tmp!=null)
                mMiddlePhoto = Photo.create(tmp.width(), tmp.height());
            }
            processInner(in, mMiddlePhoto);
            mNextGlslFilter.processInner(mMiddlePhoto, out);
        }
    }

    private void processInner(Photo in, Photo out) {
        if (shaderProgram == 0)
            return;
        if (out == null) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        } else {
            if (frameBufferObjectId[0] == 0) {
                GLES20.glGenFramebuffers(1, frameBufferObjectId, 0);
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, out.texture());

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, out.width(),
                    out.height(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,
                    frameBufferObjectId[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, out.texture(), 0);

            checkGlError("glBindFramebuffer");
        }
        // Use our shader program
        GLES20.glUseProgram(shaderProgram);
        checkGlError("glUseProgram");

        // Set viewport
        GLES20.glViewport(0, 0, out.width(),
                out.height());
        checkGlError("glViewport");

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND);

        // Set the vertex attributes 3, GL_FLOAT, 0,3 * sizeof(float)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false,
                0, texVertices);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(posCoordHandle, 3, GLES20.GL_FLOAT, false,
                0, posVertices);
        GLES20.glEnableVertexAttribArray(posCoordHandle);
        checkGlError("vertex attribute setup");

        if (in!=null && texSamplerHandle >= 0) {
            // Set the input texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            checkGlError("glActiveTexture");
            GLES20.glBindTexture(mInputTextureType, in.texture());
            checkGlError("glBindTexture");
            GLES20.glTexParameteri(mInputTextureType, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(mInputTextureType, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(mInputTextureType, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(mInputTextureType, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glUniform1i(texSamplerHandle, 0);
            checkGlError("texSamplerHandle");
        }

        GLES20.glUniformMatrix4fv(texCoordMatHandle, 1, false, mTextureMat, 0);
        checkGlError("texCoordMatHandle");
        GLES20.glUniformMatrix4fv(modelViewMatHandle, 1, false, mModelViewMat,
                0);
        checkGlError("modelViewMatHandle");

        updateParams();

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        checkGlError("glDrawArrays");

        GLES20.glFinish();

        if (out!=null) {
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
        checkGlError("after process");
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

    private static FloatBuffer createVerticesBuffer(float[] vertices) {
        // if (vertices.length != 8) {
        // throw new RuntimeException("Number of vertices should be four.");
        // }

        FloatBuffer buffer = ByteBuffer
                .allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(vertices).position(0);
        return buffer;
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(getError(op, error));
        }
    }

    public static String getError(String message, int status) {
        StringBuffer sb = new StringBuffer();
        sb.append(message).append(" - ");
        switch (status) {
            case GLES20.GL_FRAMEBUFFER_UNSUPPORTED:
                sb.append("OpenGL framebuffer format not supported. ");
                break;
            case GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                sb.append("OpenGL framebuffer missing attachment.");
                break;
            case GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                sb.append("OpenGL framebuffer attached images must have same dimensions.");
                break;
            case GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                sb.append("OpenGL framebuffer attached images must have same format.");
                break;
            case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
                sb.append("OpenGL invalid framebuffer operation.");
                break;
            case GLES20.GL_NO_ERROR:
                sb.append("No errors.");
                break;
            case GLES20.GL_INVALID_VALUE:
                sb.append("Invalid value");
                break;
            case GLES20.GL_INVALID_OPERATION:
                sb.append("Invalid operation");
                break;
            case GLES20.GL_INVALID_ENUM:
                sb.append("Invalid enum");
                break;
            case GLES20.GL_FRAMEBUFFER_COMPLETE:
                sb.append("Framebuffer complete.");
                break;
            default:
                sb.append("OpenGL error: " + status);
                break;
        }
        return sb.toString();
    }
}
