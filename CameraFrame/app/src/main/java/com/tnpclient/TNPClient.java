package com.tnpclient;

import android.os.Environment;
import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;
import com.pppcommon.PPPHead;
import com.pppcommon.PPPPCallback;
import com.tnpserver.TNPService;
import com.tnpserver.TNPSession;
import com.tutk.IOTC.Packet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by SMY on 2018/1/3.
 */

public class TNPClient {

    private static final String TAG = "TNPClient";
    private String licenseDeviceKey = "JGCFAC";

    private TNPClientThread mClient = null;
    private PPPPCallback callback = null;

    public TNPClient(PPPPCallback callback) {
        this.callback = callback;
    }

    public void startClient() {
        stopClient();
        mClient = new TNPClientThread();
        mClient.run();
    }

    public void stopClient() {
        if (mClient != null) {
            mClient.stop = true;
            mClient = null;
        }
    }

    private void log(String message) {
        Log.d(TAG, "log: " + message);
        if (callback != null) {
            callback.log(message);
        }
    }

    private class TNPClientThread implements Runnable {

        volatile boolean stop = false;

        @Override
        public void run() {
            int ret = PPPP_APIs.PPPP_Initialize("".getBytes(), 12);
            Log.d(TAG, "run: init res : " + ret);

            byte enableLanSearch = (byte) 1;
            byte p2pTryTime =      (byte) 5;
            byte relayOff =        (byte) 0;
            byte serverRelayOnly = (byte) 1;
            byte connectFlag = (byte) (enableLanSearch | (p2pTryTime << 1) | (relayOff << 5) | (serverRelayOnly << 6) );

            while (!stop){
                int session = PPPP_APIs.PPPP_ConnectByServer(TNPService.did, connectFlag, 0, TNPService.serverString, licenseDeviceKey);
                Log.d(TAG, "run: connect session: " + session);

                if (session > 0) {
                    Log.d(TAG, "run: connect success");
                    while (!stop) {
                        //read head
                        byte[] buffer = new byte[1024 * 1024];
                        int[] size = new int[1];
                        size[0] = PPPHead.HEAD_SIZE;
                        ret = PPPP_APIs.PPPP_Read(session, TNPSession.CH_VIDEO, buffer, size, 0xffffff);

                        PPPHead head = new PPPHead(buffer);
                        if (callback != null) {
                            callback.onHead(head);
                        }

                        size[0] = head.getDataSize();
                        log("read length ret: " + ret + ", dataSize: " + size[0]);

                        //read data
                        ret = PPPP_APIs.PPPP_Read(session, TNPSession.CH_VIDEO, buffer, size, 0xffffff);
                        log("read data ret: " + ret);

                        if (callback != null) {
                            callback.onData(buffer);
                        }

                        //save data
                        /*if (!isFileCreated) {
                            createFile();
                        }
                        try {
                            if (outputStream != null) {
                                outputStream.write(buffer, 0, size[0]);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/


                    }

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Log.d(TAG, "run: exit");
        }
    }



    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/smy/";
    private BufferedOutputStream outputStream;
    private boolean isFileCreated = false;
    private void createFile() {
        if (isFileCreated) {
            return;
        }
        isFileCreated = true;
        File fPath = new File(path);
        if (fPath.exists()) {
            fPath.mkdir();
        }

        File file = new File(path + "client_" + System.currentTimeMillis() + ".h264");
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
