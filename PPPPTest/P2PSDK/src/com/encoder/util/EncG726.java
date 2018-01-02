package com.encoder.util;


public class EncG726 {
    public static final int API_ER_ANDROID_NULL = -10000;
    public static final byte FORMAT_ALAW = 1;
    public static final byte FORMAT_LINEAR = 2;
    public static final byte FORMAT_ULAW = 0;
    public static final int G726_16 = 0;
    public static final int G726_24 = 1;
    public static final int G726_32 = 2;
    public static final int G726_40 = 3;

    static {
//    try
//    {
//      System.loadLibrary("G726Android");
//      //return;
//    }
//    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
//    {
//      System.out.println("loadLibrary(G726Android)," + localUnsatisfiedLinkError.getMessage());
//    }
    }

    public static native int g726_enc_state_create(byte paramByte1, byte paramByte2);

    public static native void g726_enc_state_destroy();

    public static native int g726_encode(byte[] paramArrayOfByte1, long paramLong, byte[] paramArrayOfByte2, long[] paramArrayOfLong);
}

