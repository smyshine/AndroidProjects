
package com.xiaomi.fastvideo;


public class VideoFrame {
    public byte[] data;
    public short num;
    public int size;
    public int width;
    public int height;
    public long timeStamp;//毫秒
    public boolean isIFrame;

    public VideoFrame(byte[] data,
                      short frameNumber, int frameSize,
                      int width, int height,
                      long timestamp, boolean isIFrame) {
        this.data = data;
        this.num = frameNumber;
        this.size = frameSize;
        this.width = width;
        this.height = height;
        this.timeStamp = timestamp;
        this.isIFrame = isIFrame;
    }

    public String toString(){
        return "VideoFrame: " + (isIFrame ? "I" : "P") + " frame, "
                + num + "-" + timeStamp + "-" + size
                + ", [" + width + "," + height + "]";
    }

}
