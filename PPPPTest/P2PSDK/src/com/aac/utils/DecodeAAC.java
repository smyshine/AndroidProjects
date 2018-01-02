package com.aac.utils;

public class DecodeAAC {
//	static {
//		System.loadLibrary("faad");
//	}
	public static native int nOpen();

	public static native int nDecode(byte[] inbuf, int inbuf_size ,byte[] pcmbuf,int bufSize);

	public static native int nClose();

	// public native int nGetSampleRate(int paramInt);
	//
	// public native int nGetChannel(int paramInt);
}
