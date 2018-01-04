package com.tnp.model;


/**
 * Created by Chuanlong on 2015/11/12.
 */
public class TNP_Proto {
	
	//ENUM_STREAM_IO_TYPE
	public static final int SIO_TYPE_UNKN	=0;
	public static final int SIO_TYPE_VIDEO	=1;
	public static final int SIO_TYPE_AUDIO	=2;
	public static final int SIO_TYPE_IOCTRL	=3;
	
	//ENUM_CODECID
	public static final int CODECID_UNKN	=0;
	public static final int CODECID_V_MJPEG	=1;
	public static final int CODECID_V_MPEG4	=2;
	public static final int CODECID_V_H264	=3;
	
	public static final int CODECID_A_PCM 	=0x4FF;
	public static final int CODECID_A_ADPCM	=0x500;
	public static final int CODECID_A_SPEEX	=0x501;
	public static final int CODECID_A_AMR	=0x502;
	public static final int CODECID_A_AAC	=0x503;
		
	//ENUM_VFRAME
	public static final int VFRAME_FLAG_I	=0x00;
	public static final int VFRAME_FLAG_P	=0x01;
	public static final int VFRAME_FLAG_B	=0x02;
	
	//ENUM_AUDIO_SAMPLERATE
	public static final int ASAMPLE_RATE_8K	= 0x00;
	public static final int ASAMPLE_RATE_11K= 0x01;
	public static final int ASAMPLE_RATE_12K= 0x02;
	public static final int ASAMPLE_RATE_16K= 0x03;
	public static final int ASAMPLE_RATE_22K= 0x04;
	public static final int ASAMPLE_RATE_24K= 0x05;
	public static final int ASAMPLE_RATE_32K= 0x06;
	public static final int ASAMPLE_RATE_44K= 0x07;
	public static final int ASAMPLE_RATE_48K= 0x08;
	
	//ENUM_AUDIO_DATABITS
	public static final int ADATABITS_8		=0;
	public static final int ADATABITS_16	=1;
	
	//ENUM_AUDIO_CHANNEL
	public static final int ACHANNEL_MONO	=0;
	public static final int ACHANNEL_STERO	=1;
	
	//ENUM_IOCTRL_TYPE
	public static final int IOCTRL_TYPE_UNKN		=0x00;
	public static final int IOCTRL_TYPE_VIDEO_START	=0x01;
	public static final int IOCTRL_TYPE_VIDEO_STOP	=0x02;
	public static final int IOCTRL_TYPE_AUDIO_START	=0x03;
	public static final int IOCTRL_TYPE_AUDIO_STOP	=0x04;
	
	//st_AVStreamIOHead
	
	//st_AVFrameHead
		
	//st_AVIOCtrlHead
	
}
