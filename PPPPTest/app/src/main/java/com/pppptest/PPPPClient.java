package com.pppptest;

import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;

/**
 * Created by SMY on 2017/12/30.
 */

public class PPPPClient {

    private static final String TAG = "PPPPClient";
    private String licenseDeviceKey = "JGCFAC";

    private PPPPCallback callback;

    public PPPPClient(PPPPCallback callback) {
        this.callback = callback;
    }

    public void startConnect() {
        new ClientConnectThread().run();
    }

    private class ClientConnectThread implements Runnable {

        boolean stop = false;

        @Override
        public void run() {
            int nRet = PPPP_APIs.PPPP_Initialize("".getBytes(), 12);
            Log.d(TAG, "run: init result :" + nRet);

            byte enableLanSearch = (byte) 1;
            byte p2pTryTime =      (byte) 5;
            byte relayOff =        (byte) 0;
            byte serverRelayOnly = (byte) 1;
            byte connectFlag = (byte) (enableLanSearch | (p2pTryTime << 1) | (relayOff << 5) | (serverRelayOnly << 6) );

            while (!stop){
                int session = PPPP_APIs.PPPP_ConnectByServer(PPPPServer.did, connectFlag, 0, PPPPServer.serverString, licenseDeviceKey);
                Log.d(TAG, "run: connect session: " + session);
                if (callback != null) {
                    callback.onLog("connect result session: " + session);
                }
                if (session > 0) {
                    Log.d(TAG, "run: connect success");
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
