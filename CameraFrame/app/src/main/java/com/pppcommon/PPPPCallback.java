package com.pppcommon;

/**
 * Created by SMY on 2018/1/3.
 */

public interface PPPPCallback {
    void log(String message);

    void onData(byte[] data);

    void onHead(PPPHead head);
}
