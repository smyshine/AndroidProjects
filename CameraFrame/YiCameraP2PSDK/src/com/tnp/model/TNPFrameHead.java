package com.tnp.model;

import com.tutk.IOTC.Packet;

/**
 * Created by Chuanlong on 2015/11/17.
 */
public class TNPFrameHead {
    public static final int LEN_HEAD = 24;

    public short codec_id;
    byte flags;
    byte liveFlag;

    byte onlineNum;
    byte useCount;

    short seqNumber;

    short videoWidth;
    short videoHeight;
    public int timestamp;

    byte isday;
    byte ref;
    byte outloss;
    byte inloss;
    int timestamp_ms;

    boolean isByteOrderBig;

    private TNPFrameHead(boolean isByteOrderBig){
        this.isByteOrderBig = isByteOrderBig;
    }

    public TNPFrameHead(short codec_id, byte flags, byte liveFlag, byte onlineNum, int timestamp, boolean isByteOrderBig){
        this.codec_id = codec_id;
        this.flags = flags;
        this.liveFlag = 0;
        this.onlineNum = 0;
        this.timestamp = timestamp;
        this.isByteOrderBig = isByteOrderBig;
    }

    public byte[] toByteArray(){
        byte[] data = new byte[LEN_HEAD];
        System.arraycopy(Packet.shortToByteArray(codec_id, isByteOrderBig), 0, data, 0, 2);
        data[2] = flags;
        data[3] = liveFlag;
        data[4] = onlineNum;
        data[5] = useCount;
        System.arraycopy(Packet.shortToByteArray(seqNumber, isByteOrderBig), 0, data, 6, 2);
        System.arraycopy(Packet.shortToByteArray(videoWidth, isByteOrderBig), 0, data, 8, 2);
        System.arraycopy(Packet.shortToByteArray(videoHeight, isByteOrderBig), 0, data, 10, 2);
        System.arraycopy(Packet.intToByteArray(timestamp, isByteOrderBig), 0, data, 12, 4);
        data[16] = isday;
        data[17] = ref;
        data[18] = outloss;
        data[19] = inloss;
        System.arraycopy(Packet.intToByteArray(timestamp_ms, isByteOrderBig), 0, data, 20, 4);
        return data;
    }

    public static TNPFrameHead parse(byte[] data, boolean isByteOrderBig){
        TNPFrameHead tnpFrameHead = new TNPFrameHead(isByteOrderBig);
        tnpFrameHead.codec_id = Packet.byteArrayToShort(data, 0, isByteOrderBig);
        tnpFrameHead.flags = data[2];
        tnpFrameHead.liveFlag = data[3];
        tnpFrameHead.onlineNum = data[4];
        tnpFrameHead.useCount = data[5];
        tnpFrameHead.seqNumber = Packet.byteArrayToShort(data, 6, isByteOrderBig);
        tnpFrameHead.videoWidth = Packet.byteArrayToShort(data, 8, isByteOrderBig);
        tnpFrameHead.videoHeight = Packet.byteArrayToShort(data, 10, isByteOrderBig);
        tnpFrameHead.timestamp = Packet.byteArrayToInt(data, 12, isByteOrderBig);
        tnpFrameHead.isday = data[16];
        tnpFrameHead.ref = data[17];
        tnpFrameHead.outloss = data[18];
        tnpFrameHead.inloss = data[19];
        tnpFrameHead.timestamp_ms = Packet.byteArrayToInt(data, 20, isByteOrderBig);
        return tnpFrameHead;
    }


}
