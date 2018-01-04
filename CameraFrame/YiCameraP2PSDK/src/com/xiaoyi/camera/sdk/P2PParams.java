package com.xiaoyi.camera.sdk;

/**
 * Created by Chenyc on 2015/7/22.
 */
public class P2PParams {

    private P2PParams() {

    }

    private int avClientStartTimeOut = 15;

    private static P2PParams p2PParams = null;

    public static P2PParams getInstance() {

        if (p2PParams == null) {
            p2PParams = new P2PParams();
        }
        return p2PParams;
    }


    public void setAvClientStartTimeOut(int avClientStartTimeOut) {
        if (avClientStartTimeOut <= 0) {
            return;
        }
        this.avClientStartTimeOut = avClientStartTimeOut;
    }

    public int getAvClientStartTimeOut() {
        return avClientStartTimeOut;
    }
}
