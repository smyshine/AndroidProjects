package com.audio.handle;

public class AudioProcIn {

	static {
//		System.loadLibrary("stlport_shared");
//		System.loadLibrary("audioproc");
		AudioProcIn.Init();
	}
	

	public static native int Init();

	public static native int Process(byte[] iAudioBuff, int buffLen,
			byte[] oAudioBuff, float increaseVolume);

	public static native int Clean();

	public static native int Destroy();
}
