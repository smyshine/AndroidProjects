package com.tutk.IOTC;

import java.io.Serializable;

public class AVFrame implements Serializable {// CH1之前流类型
    public static final int AUDIO_CHANNEL_MONO = 0;
    public static final int AUDIO_CHANNEL_STERO = 1;
    public static final int AUDIO_DATABITS_16 = 1;
    public static final int AUDIO_DATABITS_8 = 0;
    public static final int AUDIO_SAMPLE_11K = 1;
    public static final int AUDIO_SAMPLE_12K = 2;
    public static final int AUDIO_SAMPLE_16K = 3;
    public static final int AUDIO_SAMPLE_22K = 4;
    public static final int AUDIO_SAMPLE_24K = 5;
    public static final int AUDIO_SAMPLE_32K = 6;
    public static final int AUDIO_SAMPLE_44K = 7;
    public static final int AUDIO_SAMPLE_48K = 8;
    public static final int AUDIO_SAMPLE_8K = 0;
    public static final int FRAMEINFO_SIZE = 24;
    public static final byte FRM_STATE_COMPLETE = 0;
    public static final byte FRM_STATE_INCOMPLETE = 1;
    public static final byte FRM_STATE_LOSED = 2;
    public static final byte FRM_STATE_UNKOWN = -1;
    public static final int IPC_FRAME_FLAG_IFRAME = 1;
    public static final int IPC_FRAME_FLAG_IO = 3;
    public static final int IPC_FRAME_FLAG_MD = 2;
    public static final int IPC_FRAME_FLAG_PBFRAME = 0;
    public static final int MEDIA_CODEC_AUDIO_AAC = 138;
    public static final int MEDIA_CODEC_AUDIO_ADPCM = 139;
    public static final int MEDIA_CODEC_AUDIO_PCM = 140;
    public static final int MEDIA_CODEC_AUDIO_SPEEX = 141;
    public static final int MEDIA_CODEC_AUDIO_MP3 = 142;
    public static final int MEDIA_CODEC_AUDIO_G726 = 143;

    public static final int MEDIA_CODEC_UNKNOWN = 0;
    public static final int MEDIA_CODEC_VIDEO_H263 = 77;
    public static final int MEDIA_CODEC_VIDEO_H264 = 78;
    public static final int MEDIA_CODEC_VIDEO_MJPEG = 79;
    public static final int MEDIA_CODEC_VIDEO_MPEG4 = 76;
    private short codec_id = 0;
    private byte flags = -1;
    public byte[] frmData = null;
    private short frmNo = -1;
    private int frmSize = 0;
    private byte frmState = 0;
    private byte onlineNum = 0;
    private int timestamp = 0;
    private int videoHeight = 0;
    private int videoWidth = 0;
    private long oldfrmNo;

    public byte liveFlag;       // 0=实时， 1=录像

    public byte useCount;       // 使用次数，在seek发送时每次递增

    private byte isDay = 1;     // 1 白天，0夜间
    private byte cover_state;   // 0,无遮挡;1,遮挡
    private byte outloss;       // 外网丢包率
    private byte inloss;        // 内网丢包率
    private int timestamp_ms = 0;


    public PanState panState = new PanState((byte) 0); //云台状态

    public int getTimestamp_ms() {
        return timestamp_ms;
    }

    public void setTimestamp_ms(int timestamp_ms) {
        this.timestamp_ms = timestamp_ms;
    }
    
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public AVFrame(long frmNo, byte frmState, byte[] frameInfo, byte[] frmData, int frmSize, boolean isByteOrderBig) {
        this.codec_id = Packet.byteArrayToShort(frameInfo, 0, isByteOrderBig);
        this.frmState = frmState;
        this.flags = frameInfo[2];
        this.liveFlag = frameInfo[3];
        this.onlineNum = frameInfo[4];
        this.useCount = frameInfo[5];
        this.frmNo = Packet.byteArrayToShort(frameInfo, 6, isByteOrderBig);
        // if (frameInfo.length > 16) {
        this.videoWidth = Packet.byteArrayToShort(frameInfo, 8, isByteOrderBig);
        // Log.i("AVFrame", "video---宽："+w/*+"video高："+videoHeight*/);
        this.videoHeight = Packet.byteArrayToShort(frameInfo, 10, isByteOrderBig);
        // }
        this.frmSize = frmSize;
        this.frmData = frmData;

        this.timestamp = Packet.byteArrayToInt(frameInfo, 12, isByteOrderBig);

        if (frameInfo.length >= 20) {
            this.isDay = frameInfo[16];
            this.cover_state = frameInfo[17];
            this.outloss = frameInfo[18];
            this.inloss = frameInfo[19];
        }
        if (frameInfo.length > 20) {
            this.timestamp_ms = Packet.byteArrayToInt(frameInfo, 20, isByteOrderBig);
        }
        this.oldfrmNo = frmNo;
        return;
    }

    // 浪涛AVFrame
    public AVFrame(byte[] frameInfo, byte[] frmData, boolean isByteOrderBig) {
        this.codec_id = Packet.byteArrayToShort(frameInfo, 0, isByteOrderBig);
        this.flags = frameInfo[2];
        this.liveFlag = frameInfo[3];
        this.onlineNum = frameInfo[4];
        this.useCount = frameInfo[5];
        this.frmNo = Packet.byteArrayToShort(frameInfo, 6, isByteOrderBig);
        // if (frameInfo.length > 16) {
        this.videoWidth = Packet.byteArrayToShort(frameInfo, 8, isByteOrderBig);
        // Log.i("AVFrame", "video---宽："+w/*+"video高："+videoHeight*/);
        this.videoHeight = Packet.byteArrayToShort(frameInfo, 10, isByteOrderBig);
        // }
        this.frmSize = frmData.length;
        this.frmData = frmData;

        if (frameInfo.length <= 16) { return; }
        this.isDay = frameInfo[16];
        this.cover_state = frameInfo[17];
        this.outloss = frameInfo[18];
        this.inloss = frameInfo[19];
        this.timestamp = Packet.byteArrayToInt(frameInfo, 20, isByteOrderBig);
    }

    // TNP,AVFrame
    public AVFrame(byte[] data, int dataSize, boolean isByteOrderBig, boolean isAudio){
        this.codec_id = Packet.byteArrayToShort(data, 0, isByteOrderBig);
        this.flags = data[2];
        this.liveFlag = data[3];
        this.onlineNum = data[4];
        this.useCount = data[5];
        this.frmNo = Packet.byteArrayToShort(data, 6, isByteOrderBig);
        this.videoWidth = Packet.byteArrayToShort(data, 8, isByteOrderBig);
        this.videoHeight = Packet.byteArrayToShort(data, 10, isByteOrderBig);
        this.timestamp = Packet.byteArrayToInt(data, 12, isByteOrderBig);
        this.isDay = data[16];
        this.cover_state = data[17];
        this.outloss = data[18];
        this.inloss = data[19];
        this.timestamp_ms = Packet.byteArrayToInt(data, 20, isByteOrderBig);
        this.panState = new PanState(this.onlineNum);

        this.frmSize = dataSize - FRAMEINFO_SIZE;
        this.frmData = new byte[frmSize];
        System.arraycopy(data, FRAMEINFO_SIZE, frmData, 0, frmSize);

    }


    public static int getSamplerate(byte paramByte) {
        int samplerate = 16000;
        switch (paramByte >>> 2) {
        case AUDIO_SAMPLE_8K:
            samplerate = 16000;
            break;
        case AUDIO_SAMPLE_11K:
            samplerate = 11025;
            break;
        case AUDIO_SAMPLE_12K:
            samplerate = 12000;
            break;
        case AUDIO_SAMPLE_16K:
            samplerate = 16000;
            break;
        case AUDIO_SAMPLE_22K:
            samplerate = 22050;
            break;
        case AUDIO_SAMPLE_24K:
            samplerate = 24000;
            break;
        case AUDIO_SAMPLE_32K:
            samplerate = 32000;
            break;
        case AUDIO_SAMPLE_44K:
            samplerate = 44100;
            break;
        case AUDIO_SAMPLE_48K:
            samplerate = 48000;
            break;
        }
        return samplerate;
    }

    public short getCodecId() {
        return this.codec_id;
    }

    public byte getFlags() {
        return this.flags;
    }

    public short getFrmNo() {
        return this.frmNo;
    }

    public int getFrmSize() {
        return this.frmSize;
    }

    public byte getFrmState() {
        return this.frmState;
    }

    public byte getOnlineNum() {
        return this.onlineNum;
    }

    public int getTimeStamp() {
        return this.timestamp;
    }

    public int getVideoHeight() {
        return this.videoHeight;
    }

    public int getVideoWidth() {
        return this.videoWidth;
    }

    public long getOldfrmNo() {
        return oldfrmNo;
    }

    public boolean isDay() {
        return isDay == 1;
    }

    public boolean isIFrame() {
        return (0x1 & this.flags) == 1;
    }

    public boolean isCovered() {
        return this.cover_state == 1;
    }

    public byte getOutloss() {
        return outloss;
    }

    public void setOutloss(byte outloss) {
        this.outloss = outloss;
    }

    public byte getInloss() {
        return inloss;
    }

    public void setInloss(byte inloss) {
        this.inloss = inloss;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("codec_id:" + codec_id);
        sb.append(", flags:" + flags);
        sb.append(", liveFlag:" + liveFlag);
        sb.append(", onlineNum:" + onlineNum);
        sb.append(", useCount:" + useCount);
        sb.append(", frmNo:" + frmNo);
        sb.append(", videoWidth:" + videoWidth);
        sb.append(", videoHeight:" + videoHeight);
        sb.append(", timestamp:" + timestamp);
        sb.append(", isDay:" + isDay);
        sb.append(", cover_state:" + cover_state);
        sb.append(", outloss:" + outloss);
        sb.append(", inloss:" + inloss);
        sb.append(", timestamp_ms:" + timestamp_ms);
        sb.append(", frmSize:" + frmSize);
        return sb.toString();
    }

    public String toFrameString() {
        return "AVFrame: " + (isIFrame() ? "I" : "P") + " frame, "
                + getFrmNo() + "-" + getTimeStamp() + "-" + getFrmSize()
                + ", [" + getVideoWidth() + "," + getVideoHeight() + "]";
    }


    public static class PanState{

        private byte value;

        public static final byte MOVIING_STATE      = (byte) 0x80; //移动状态
        public static final byte X_BORDER_STATE     = (byte) 0x20; //x轴到边界
        public static final byte Y_BORDER_STATE     = (byte) 0x10; //y轴到边界
        public static final byte CURISE_STATE       = (byte) 0x04; //巡航状态
        public static final byte MOVETRACK_STATE    = (byte) 0x02; //移动跟踪状态
        public static final byte PRESET_STATE       = (byte) 0x01; //位置点
        public static final byte PANORAMA_CAPTURING_STATE       = (byte) 0x08; //全景拍摄状态

        public PanState(byte value) {
            this.value = value;
        }

        public boolean isPanMoving(){
            return isState(MOVIING_STATE);
        }

        public boolean isXBorderState() {
            return isState(X_BORDER_STATE);
        }

        public boolean isYBorderState() {
            return isState(Y_BORDER_STATE);
        }

        public boolean isCuriseState(){
            return isState(CURISE_STATE);
        }

        public boolean isMoveTrackState(){
            return isState(MOVETRACK_STATE);
        }

        public boolean isPresetState(){
            return isState(PRESET_STATE);
        }

        public boolean isPanoramaCapturingState(){
            return isState(PANORAMA_CAPTURING_STATE);
        }

        private boolean isState(byte state){
            return (value & state) == state;
        }

    }



}
