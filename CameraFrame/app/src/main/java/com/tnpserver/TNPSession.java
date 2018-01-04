package com.tnpserver;

import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;


/**
 * Created by SMY on 2017/12/29.
 */

public class TNPSession {

    //Channel定义
    public static final byte CH_IO_CTRL = 0;
    public static final byte CH_VIDEO = 1;
    public static final byte CH_AUDIO = 2;

    private static final String TAG = "TNPSession";

    public int session;

    private VideoSender videoSender = null;

    private OnSessionListener listener;

    public TNPSession(int session, OnSessionListener listener) {
        this.session = session;
        this.listener = listener;
    }

    public void startSession() {
        //验证通过后
        startSendVideo();
    }

    private void startSendVideo() {
        Log.d(TAG, "startSendVideo: ");
        if (videoSender == null) {
            videoSender = new VideoSender(session);
            videoSender.start();
        }
    }

    private void stopSendVideo() {
        if (videoSender != null) {
            videoSender.stop();
            videoSender = null;
        }
    }

    public void stopSession() {
        listener = null;
        stopSendVideo();
        PPPP_APIs.PPPP_Close(session);
    }
}
