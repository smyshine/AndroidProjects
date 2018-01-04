
package com.decoder.xiaomi;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public class YUV2RGB {
    // YUV420P，Y，U，V三个分量都是平面格式，分为I420和YV12。I420格式和YV12格式的不同处在U平面和V平面的位置不同。在I420格式中，U平面紧跟在Y平面之后，然后才是V平面（即：YUV）；但YV12则是相反（即：YVU）。
    // YUV420SP, Y分量平面格式，UV打包格式, 即NV12。 NV12与NV21类似，U 和 V 交错排列,不同在于UV顺序。
    // I420: YYYYYYYY UU VV =>YUV420P
    // YV12: YYYYYYYY VV UU =>YUV420P
    // NV12: YYYYYYYY UVUV =>YUV420SP
    // NV21: YYYYYYYY VUVU =>YUV420SP

    // I420: YYYYYYYY UU VV =>YUV420P
    public static native void convertI420RGB(byte[] yuv, Bitmap bitmap);

    // YV12: YYYYYYYY VV UU =>YUV420P
    public static native void convertYV12RGB(byte[] yuv, Bitmap bitmap);

    // NV12: YYYYYYYY UVUV =>YUV420SP
    public static native void convertNV12RGB(byte[] yuv, Bitmap bitmap);

    // NV21: YYYYYYYY VUVU =>YUV420SP
    public static native void convertNV21RGB(byte[] yuv, Bitmap bitmap);

    //type 0 uv 1 vu
    public static native void YUVBuffer2Bitmap(ByteBuffer buffer,int[] offset,int[] stride,Bitmap bitmap,int type);
}
