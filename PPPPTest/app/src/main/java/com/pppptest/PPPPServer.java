package com.pppptest;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;
import com.tnp.model.st_PPPP_NetInfo;

import java.util.concurrent.ExecutorService;

/**
 * Created by SMY on 2017/12/30.
 */

public class PPPPServer {

    private static final String TAG = "PPPPServer";

    public static String serverString = "MJFCIGLGIGENLGHMOJEMOCECGLDHEEHEGLFNKFJFJNAKCAAIOMJKBOMLOFDNAOLIPBLJCLOHMLCOBNCDAPAADNOL";
    public static String did = "TNPCHNG-249839-EXLEY";

    private PPPPCallback listener ;

    private Handler mHandler;
    private ExecutorService mThroodPool;
    private HandlerThread tnpServer;
    private ServerStartThread serverStartThread = null;

    public PPPPServer(PPPPCallback listener) {
        this.listener = listener;

        tnpServer = new HandlerThread(TAG);
        tnpServer.start();
        mHandler = new Handler(tnpServer.getLooper());

        safeStartServer();
    }


    private void safeStartServer() {
        mHandler.removeCallbacks(startRunnable);
        mHandler.removeCallbacks(restartRunnable);
        mHandler.post(startRunnable);
    }

    private void safeRestartServer(){
        mHandler.removeCallbacks(startRunnable);
        mHandler.removeCallbacks(restartRunnable);
        mHandler.post(restartRunnable);
    }

    Runnable startRunnable= new Runnable() {
        @Override
        public void run() {
            startServer();
//            isRestarting = false;
//            isHandleError = false;
        }
    };

    Runnable restartRunnable= new Runnable() {
        @Override
        public void run() {
            restartTNPServer();
        }
    };

    private void restartTNPServer() {
        stop();
        mHandler.removeCallbacks(startRunnable);
        mHandler.removeCallbacks(restartRunnable);
        mHandler.postDelayed(startRunnable, 3000);
    }

    private void startServer() {
        if (serverStartThread == null) {
//            if (mThroodPool == null) {
//                mThroodPool = Executors.newSingleThreadExecutor();
//            }
            serverStartThread = new ServerStartThread();
            serverStartThread.run();
        }
    }

    public void stop() {
        if (serverStartThread != null) {
//            if (mThroodPool == null) {
//                mThroodPool = Executors.newSingleThreadExecutor();
//            }
            serverStartThread.stop();
            serverStartThread = null;
        }
    }

    private void log(String message) {
        if (listener != null) {
            listener.onLog(message);
        } else {
            Log.d(TAG, "log: " + message);
        }
    }

    private int retryCount = 0;
    private String key = "MGUWJmspOq7pVsWG";

    private class ServerStartThread implements Runnable {

        public boolean isRunning = true;
        private Object lock = new Object();

        @Override
        public void run() {
            retryCount = 0;
            PPPP_APIs.PPPP_Initialize(serverString.getBytes() , 12);
            st_PPPP_NetInfo netInfo = new st_PPPP_NetInfo();
            PPPP_APIs.PPPP_NetworkDetect(netInfo, 0);
            PPPP_APIs.PPPP_Share_Bandwidth((byte) 1);

            int i = 1;
            while (isRunning) {
                int session = PPPP_APIs.PPPP_Listen_With_Key(did, 50000, 0,
                        (byte) 1, key);
                log("server, listen with key, session: " + session);
                if (!isRunning) {
                    return;
                }

                if (session >= 0) {
                    log("server session success");
                    if (isRunning) {
                        synchronized (lock) {
                            if (isRunning) {
                                //handle session
                                handleSession(session);
                            }
                        }
                    }
                } else {
                    ++retryCount;
                    if (retryCount >= 3) {
                        log("retry count oversize, restart server");
                        retryCount = 0;
                        safeRestartServer();
                    }
                }
                ++i;
            }

            log("server stop");

            PPPP_APIs.PPPP_DeInitialize();
            lock = null;
        }

        public void stop() {
            synchronized (lock) {
                isRunning = false;
            }
            PPPP_APIs.PPPP_Listen_Break();
        }
    }

    private void handleSession(int session) {
        log("handle session send hello ");
        String message = "hello";
        int ret = PPPP_APIs.PPPP_Write(session, (byte) 0, message.getBytes(), message.getBytes().length);
        log("write ret: " + ret);

        byte[] buffer = new byte[256];
        int[] size = new int[1];
        size[0] = 24;
        ret = PPPP_APIs.PPPP_Read(session, (byte) 0, buffer, size, 0xffffff);
        log("read ret: " + ret + ", data: " + new String(buffer));
    }

}
