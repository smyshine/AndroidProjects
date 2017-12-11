package smy.com.vrplayer.objects.video;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.TransformHelper;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.objects.base.CombineParams;
import smy.com.vrplayer.objects.base.XYVRBaseObject;

/**
 * 用途：视频详情，播放相机中的双鱼眼视频
 * Created by hzb on 16-10-10.
 */

public class DualVideoSphere extends XYVRBaseObject {
    private TransformHelper mCombine = TransformHelper.getInstance();

    private ShaderProgram mShaderProgram;
    /*1,转换为YUV分量
    * 2，在球体xyz中的y坐标-50~+50区间内，做YUV分量的线性缩减*/
    private final String mVertexShader =
            "precision mediump float;\n" +
                    "attribute vec4 vPosition;\n" +
                    "uniform mat4 mvpMatrix;\n" + /*MVP矩阵*/
                    " uniform mat4 stMatrix;\n" + /*纹理旋转矩阵，surface获取*/
                    "attribute vec2 aTextureCoordFront;\n" +/*材质坐标*/
                    "attribute vec2 aTextureCoordBack;\n" +/*材质坐标*/
                    "varying vec2 vTextureCoordFront;\n" +
                    "varying vec2 vTextureCoordBack;\n" +
                    "varying vec4 Position;\n" +
                    "void main() {\n" +
                    "gl_Position =  mvpMatrix * vPosition ;\n" +
                    "Position = vPosition;\n" +
                    "vTextureCoordFront = (stMatrix * vec4(aTextureCoordFront.x, 1.0 - aTextureCoordFront.y, 0.0,1.0)).xy;\n" +
                    "vTextureCoordBack = (stMatrix * vec4(aTextureCoordBack.x, 1.0 - aTextureCoordBack.y, 0.0,1.0)).xy;;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    " varying vec2 vTextureCoordFront;\n" +
                    " varying vec2 vTextureCoordBack;\n" +
                    " varying vec4 Position;\n" +
                    " uniform samplerExternalOES sTexture;\n" +
                    " void main() \n{" +
                    " vec4 RGBAFront;\n" +
                    " vec4 RGBABack;\n" +
                    " vec3 rgb;\n" +
                    " float yAxis = Position.y;\n" +
                    " RGBAFront = texture2D(sTexture, vTextureCoordFront);\n" +
                    " RGBABack = texture2D(sTexture, vTextureCoordBack);\n" +

                    "if(yAxis>-30.0 && yAxis<30.0){\n" +
                    "gl_FragColor = (0.5-(yAxis/60.0))*RGBAFront+ (yAxis/60.0+0.5)*RGBABack;\n" +
                    "}\n" +
                    "   if(yAxis>=30.0){\n" +
                    "   gl_FragColor = RGBABack;\n" +
                    " }\n" +
                    "   if(yAxis<=-30.0){\n" +
                    "    gl_FragColor = RGBAFront;\n" +
                    "  }\n" +
                    "}\n";

    public DualVideoSphere(CombineParams params) {
        super(params.out_width,params.out_height);
        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = params.out_width / step + 1;
        int jMax = params.out_height / step + 1;
        int nVertices = iMax * jMax;

        FloatBuffer stFront = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer stBack = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mCombine.getSingleVertices(mVertices, stFront, stBack,params);

        mVertices.position(0);
        stFront.position(0);
        stBack.position(0);

        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);
        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        aTextureCoordLocationFront = mShaderProgram.getAttribute("aTextureCoordFront");
        aTextureCoordLocationBack = mShaderProgram.getAttribute("aTextureCoordBack");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");
        singleTextureHandle = mShaderProgram.getUniform("sTexture");
        mSTMatrixHandler = mShaderProgram.getUniform("stMatrix");
        initDualSphereVAO(mVertices,stFront,stBack,mIndices);
    }

    @Override
    public void draw(float[] modelMatrix, float[] stMatrix,  int eyeType) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(mSTMatrixHandler,1,false,stMatrix,0);

        GLES20.glUniform1i(singleTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, singleTex);

        drawDualSphereObject();
    }

}


