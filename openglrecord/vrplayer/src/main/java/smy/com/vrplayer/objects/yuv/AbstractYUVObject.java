package smy.com.vrplayer.objects.yuv;


import java.nio.ByteBuffer;

import smy.com.vrplayer.objects.base.XYVRBaseObject;

/**
 * Created by HanZengbo on 2017/3/20.
 */
public abstract class AbstractYUVObject extends XYVRBaseObject {

    protected int[] id_y = new int[1];
    protected int[] id_u = new int[1];
    protected int[] id_v = new int[1];

    protected ByteBuffer bb_y;
    protected ByteBuffer bb_u;
    protected ByteBuffer bb_v;

    protected int mMVPMatrixHandle;

    protected int mTextureUniformY, mTextureUniformU, mTextureUniformV;

    public AbstractYUVObject(){

    }

    public AbstractYUVObject(int sphereW, int sphereH){
        super(sphereW,sphereH);
    }

    public void draw(float[] modelMatrix, float[] stMatrix, int eyeType){

    }

}
