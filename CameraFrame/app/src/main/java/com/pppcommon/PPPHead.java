package com.pppcommon;

import com.tutk.IOTC.Packet;

/**
 * Created by SMY on 2018/1/4.
 */

public class PPPHead {

    public static final int HEAD_SIZE = 24;
    public static boolean isBigOrder = false;

    public static final int DATASIZE_POS = 0;
    public static final int WIDTH_POS = 4;
    public static final int HEIGHT_POS = 8;
    public static final int BITRATE_POS = 12;
    public static final int FRAMERATE_POS = 16;
    public static final int IINTERVAL_POS = 20;


    //占据 6 * 4 byte 长度

    private int dataSize;//size of frame data after
    private int width;//video width
    private int height;//video height
    private int bitRate;//
    private int frameRate;//
    private int iInterval = 1;//interval of I frame

    public PPPHead(int length, int width, int height, int bitRate, int frameRate, int iInterval) {
        this.dataSize = length;
        this.width = width;
        this.height = height;
        this.bitRate = bitRate;
        this.frameRate = frameRate;
        this.iInterval = iInterval;
    }

    public PPPHead(byte[] head) {
        splitDateFromByte(head);
    }

    public byte[] toByteArray() {
        byte[] head = new byte[HEAD_SIZE];

        System.arraycopy(Packet.intToByteArray(dataSize, isBigOrder), 0, head, DATASIZE_POS, 4);
        System.arraycopy(Packet.intToByteArray(width, isBigOrder), 0, head, WIDTH_POS, 4);
        System.arraycopy(Packet.intToByteArray(height, isBigOrder), 0, head, HEIGHT_POS, 4);
        System.arraycopy(Packet.intToByteArray(bitRate, isBigOrder), 0, head, BITRATE_POS, 4);
        System.arraycopy(Packet.intToByteArray(frameRate, isBigOrder), 0, head, FRAMERATE_POS, 4);
        System.arraycopy(Packet.intToByteArray(iInterval, isBigOrder), 0, head, IINTERVAL_POS, 4);

        return head;
    }

    private void splitDateFromByte(byte[] head) {
        this.dataSize = Packet.byteArrayToInt(head, DATASIZE_POS, isBigOrder);
        this.width = Packet.byteArrayToInt(head, WIDTH_POS, isBigOrder);
        this.height = Packet.byteArrayToInt(head, HEIGHT_POS, isBigOrder);
        this.bitRate = Packet.byteArrayToInt(head, BITRATE_POS, isBigOrder);
        this.frameRate = Packet.byteArrayToInt(head, FRAMERATE_POS, isBigOrder);
        this.iInterval = Packet.byteArrayToInt(head, IINTERVAL_POS, isBigOrder);
    }

    public static boolean isBigOrder() {
        return isBigOrder;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitRate() {
        return bitRate;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getiInterval() {
        return iInterval;
    }
}
