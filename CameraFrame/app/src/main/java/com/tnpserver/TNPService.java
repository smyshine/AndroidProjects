package com.tnpserver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.p2p.pppp_api.PPPP_APIs;
import com.tnp.model.st_PPPP_NetInfo;
import com.xiaoyi.camera.sdk.AntsCamera;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by SMY on 2017/12/29.
 */

public class TNPService extends Service {

    private static final String TAG = "TNPService";
    private static final int MAX_RETRY_COUNT = 3;
    public static String serverString = "MJFCIGLGIGENLGHMOJEMOCECGLDHEEHEGLFNKFJFJNAKCAAIOMJKBOMLOFDNAOLIPBLJCLOHMLCOBNCDAPAADNOL";
    public static String did = "TNPCHNG-249839-EXLEY";
    public static String key = "MGUWJmspOq7pVsWG";
    private List<TNPSession> sessions = new CopyOnWriteArrayList<>();
    private Handler mHandler;
    private ExecutorService mThreadPool;
    private Future<?> submit;
    private HandlerThread tnpServer;
    private ServerStartThread serverStartThread = null;
    Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            startTNPServer();
//            isRestarting = false;
//            isHandleError = false;
        }
    };
    Runnable restartRunnable = new Runnable() {
        @Override
        public void run() {
            restartTNPServer();
        }
    };
    private int retryCounts = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        tnpServer = new HandlerThread(TAG);
        tnpServer.start();
        mHandler = new Handler(tnpServer.getLooper());
        //mHandler.postDelayed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        safeStartServer();
        return super.onStartCommand(intent, flags, startId);
    }

    private void safeStartServer() {
        mHandler.removeCallbacks(startRunnable);
        mHandler.removeCallbacks(restartRunnable);
        mHandler.post(startRunnable);
    }

    private void safeRestartServer() {
//        Log.d(TAG, "safeRestartServer: ");
        mHandler.removeCallbacks(startRunnable);
        mHandler.removeCallbacks(restartRunnable);
        mHandler.post(restartRunnable);
    }

    private void startTNPServer() {
        if (serverStartThread == null) {
            if (mThreadPool == null) {
                mThreadPool = Executors.newSingleThreadExecutor();
            }
            serverStartThread = new ServerStartThread();
            submit = mThreadPool.submit(serverStartThread);
//            serverStartThread.run();
        }
    }

    private void restartTNPServer() {
        stopTNPServer();
        mHandler.postDelayed(startRunnable, 5000);
//        startTNPServer();
    }

    public void stop() {
        stopTNPServer();
        VideoSenderManager.getInstance().release();
    }

    private void stopTNPServer() {
        if (serverStartThread != null) {
            if (mThreadPool == null) {
                mThreadPool = Executors.newSingleThreadExecutor();
            }
            serverStartThread.stop();
            if (submit != null) {
                submit.cancel(true);
                submit = null;
            }
            mThreadPool.shutdown();
            mThreadPool = null;
            serverStartThread = null;
        }
    }

    private void handleTNPSession(int session) {
        TNPSession tnpSession = new TNPSession(session, new OnSessionListener() {
            @Override
            public void sessionStop(int session) {
                //error occurs on session
            }
        });
        sessions.add(tnpSession);
        tnpSession.startSession();
    }

    private class ServerStartThread implements Runnable {

        public boolean isRunning = true;
        private Object lock = new Object();

        @Override
        public void run() {
            retryCounts = 0;
            int ret = PPPP_APIs.PPPP_Initialize(serverString.getBytes(), 12);
            Log.d(TAG, "run: ret of init: " + ret);
            st_PPPP_NetInfo netInfo = new st_PPPP_NetInfo();
//            ret = PPPP_APIs.PPPP_NetworkDetect(netInfo, 0);
            ret = PPPP_APIs.PPPP_NetworkDetectByServer(netInfo, 0, serverString);
            Log.d(TAG, "run: ret of detect: " + ret);

//            PPPP_APIs.PPPP_Share_Bandwidth((byte) 1);

            String version = AntsCamera.getTnpVersion();

            int i = 1;
            while (isRunning) {
//                try {
//                    DatagramSocket datagramSocket = new DatagramSocket(0);
////                    Socket socket = new Socket("", 0);
//                    Log.d(TAG, "run: create socket" + datagramSocket);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                int session = PPPP_APIs.PPPP_Listen_With_Key(did, 50000, 0, (byte) 1, key);
                Log.d(TAG, "run: listen with key, session:" + session);
                if (!isRunning) {
                    break;
                }

                if (session >= 0) {
                    if (isRunning) {
                        synchronized (lock) {
                            if (isRunning) {
                                handleTNPSession(session);
                            }
                        }
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ++retryCounts;
                    if (retryCounts >= MAX_RETRY_COUNT) {
                        safeRestartServer();
                    }
                }
                ++i;
            }

            PPPP_APIs.PPPP_DeInitialize();
            Log.d(TAG, "run: stop");
            lock = null;
        }

        public void stop() {
            synchronized (lock) {
                isRunning = false;
            }
            for (TNPSession session : sessions) {
                session.stopSession();
            }
            sessions.clear();
//            PPPP_APIs.PPPP_Listen_Break();
        }
    }
}
