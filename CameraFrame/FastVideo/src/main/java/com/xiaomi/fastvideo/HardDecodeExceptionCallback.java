package com.xiaomi.fastvideo;

/**
 * Created by livy on 16/3/18.
 */
public interface HardDecodeExceptionCallback {
    public void onHardDecodeException(Exception e);

    public void onOtherException(Throwable e);

    public void onDecodePerformance(long decodeOneFrameMilliseconds);
}
