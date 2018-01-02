package com.tnp.model;

import com.tutk.IOTC.Packet;

/**
 * Created by Chuanlong on 2015/11/17.
 */
public class TNPHead {
    public static final int LEN_HEAD = 8;

    public static final byte IO_TYPE_UNKNOWN = 0x00;
    public static final byte IO_TYPE_VIDEO = 0x01;
    public static final byte IO_TYPE_AUDIO = 0x02;
    public static final byte IO_TYPE_COMMAND = 0x03;

    public static final byte VERSION_ONE = 0x01;
    public static final byte VERSION_TWO = 0x02;

    public byte version; // current 2
    public byte ioType;  // 0 unknown, 1 video, 2 audio, 3 command
    public byte[] reserved; // reserve 2 byte
    public int dataSize;

    public boolean isByteOrderBig;

    private TNPHead(boolean isByteOrderBig){
        this.isByteOrderBig = isByteOrderBig;
    }

    public TNPHead(byte type, int size, boolean isByteOrderBig){
        version = VERSION_ONE;  // old version 1
        ioType = type;
        reserved = new byte[2];
        dataSize = size;
        this.isByteOrderBig = isByteOrderBig;
    }

    public TNPHead(byte v, byte type, int size, boolean isByteOrderBig){
        version = v;
        ioType = type;
        reserved = new byte[2];
        dataSize = size;
        this.isByteOrderBig = isByteOrderBig;
    }

    public byte[] toByteArray(){
        byte[] data = new byte[LEN_HEAD];
        data[0] = version;
        data[1] = ioType;
        System.arraycopy(reserved, 0, data, 2, 2);
        System.arraycopy(Packet.intToByteArray(dataSize, isByteOrderBig), 0, data, 4, 4);
        return data;
    }

    public static TNPHead parse(byte[] data, boolean isByteOrderBig){
        TNPHead tnpHead = new TNPHead(isByteOrderBig);
        tnpHead.version = data[0];
        tnpHead.ioType = data[1];
        tnpHead.dataSize = Packet.byteArrayToInt(data, 4, isByteOrderBig);
        return tnpHead;
    }

}
