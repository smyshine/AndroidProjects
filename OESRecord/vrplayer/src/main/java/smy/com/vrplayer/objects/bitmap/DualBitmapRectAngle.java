package smy.com.vrplayer.objects.bitmap;


import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.ShaderProgram;
import smy.com.vrplayer.common.TransformHelper;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.objects.base.CombineParams;
import smy.com.vrplayer.objects.base.XYVRBaseObject;

/**
 * 用户：后台将双鱼眼图片转为全景图片
 */
public class DualBitmapRectAngle extends XYVRBaseObject {
    private TransformHelper mTransform = TransformHelper.getInstance();

    private ShaderProgram mShaderProgram;
    private final String mVertexShader =
            "precision mediump float;\n" +
                    "attribute vec4 vPosition;\n" +
                    "uniform mat4 mvpMatrix;\n" + /*MVP矩阵*/
                    "attribute vec2 aTextureCoordFront;\n" +/*材质坐标*/
                    "attribute vec2 aTextureCoordBack;\n" +/*材质坐标*/
                    "varying vec2 vTextureCoordFront;\n" +
                    "varying vec2 vTextureCoordBack;\n" +
                    "varying vec4 Position;\n" +
                    "void main() {\n" +
                    "gl_Position =  mvpMatrix * vPosition ;\n" +
                    "Position = vPosition;\n" +
                    "vTextureCoordFront = aTextureCoordFront;\n" +
                    "vTextureCoordBack = aTextureCoordBack;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#define pi 3.14159265358979 \n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoordFront;\n" +
                    "varying vec2 vTextureCoordBack;\n" +
                    "varying vec4 Position;\n" +
                    "uniform sampler2D singleTexture;\n" +
                    "uniform float sphereWidth; \n" +
                    "void main() {\n" +
                    "   vec4 RGBAFront;\n" +
                    "   vec4 RGBABack;\n" +
                    "   float xAxis = (1.0 + Position.x)*pi;\n" +
                    "   float yAxis = (1.0 - Position.y)/2.0*pi;\n" +
                    "   RGBAFront = texture2D(singleTexture, vTextureCoordFront);\n" +
                    "   RGBABack = texture2D(singleTexture, vTextureCoordBack);\n" +
                    "   float sphereY = sin(yAxis)*cos(xAxis);\n" +
                    "   if(sphereY>-0.06 && sphereY<0.06){\n" +
                    "       gl_FragColor = (0.5-(sphereY/0.12))*RGBAFront+ (sphereY/0.12+0.5)*RGBABack;;\n" +
                    "   }\n" +
                    "       if(sphereY>=0.06){\n" +
                    "       gl_FragColor = RGBABack;\n" +
                    "   }\n" +
                    "   if(sphereY<=-0.06){\n" +
                    "       gl_FragColor = RGBAFront;\n" +
                    "   }\n" +
                    "}\n";

    /*
     * @param nSlices how many slice in horizontal direction.
     *                The same slice for vertical direction is applied.
     *                nSlices should be > 1 and should be <= 180
     * @param x,y,z the origin of the sphere
     * @param r the radius of the sphere
     * @param viewRadius the radius of 2 picture
     * @param centerFront the center of the front camera
     * @param centerBack the center of the back camera
     *
     */
    public DualBitmapRectAngle(Bitmap bitmap, CombineParams params) {/*关键是得到这些顶点坐标和纹理坐标*/
        super(params.out_width, params.out_height);

        int step = VrConstant.SPHERE_SAMPLE_STEP;
        int iMax = params.out_width / step + 1;
        int jMax = params.out_height / step + 1;
        int nVertices = iMax * jMax;

        FloatBuffer stFront = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer stBack = ByteBuffer.allocateDirect(nVertices * 2 * VrConstant.FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTransform.getSingleRectAngleVertices(mVertices, stFront, stBack, params);

        mVertices.position(0);
        stFront.position(0);
        stBack.position(0);

        mShaderProgram = new ShaderProgram(mVertexShader, mFragmentShader);
        /*顶点着色器属性及统一变量*/
        aPositionLocation = mShaderProgram.getAttribute("vPosition");
        aTextureCoordLocationFront = mShaderProgram.getAttribute("aTextureCoordFront");
        aTextureCoordLocationBack = mShaderProgram.getAttribute("aTextureCoordBack");
        mMVPMatrixHandle = mShaderProgram.getUniform("mvpMatrix");
        singleTextureHandle = mShaderProgram.getUniform("singleTexture");
        singleTex = GLHelpers.loadTexture(bitmap);

        initDualSphereVAO(mVertices,stFront,stBack,mIndices);
    }

    @Override
    public void draw(float[] modelMatrix, float[] stMatrix, int eyeType) {
        GLES20.glUseProgram(mShaderProgram.getShaderHandle());

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, modelMatrix, 0);

        GLES20.glUniform1i(singleTextureHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, singleTex);

        drawDualSphereObject();
    }
}
