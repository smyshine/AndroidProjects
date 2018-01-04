package com.pppcommon;

/**
 * Created by SMY on 2017/12/29.
 */

public class FrameH264Data {

    private int dataType;//1--key frame, 2--config, else--data

    private byte[] data;

    private PPPHead head;

    public FrameH264Data(byte[] data) {
        this.data = data;
    }

    public FrameH264Data(byte[] data, int dataType) {
        this.dataType = dataType;
        this.data = data;
    }

    public FrameH264Data(byte[] data, int dataType, int width, int height, int bitrate, int framerate) {
        this.dataType = dataType;
        this.data = data;
        this.head = new PPPHead(data.length, width, height, bitrate, framerate, 1);
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataType() {
        return dataType;
    }

    public PPPHead getHead() {
        return head;
    }
}
