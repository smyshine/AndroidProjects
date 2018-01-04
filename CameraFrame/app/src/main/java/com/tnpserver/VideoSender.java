package com.tnpserver;

import android.util.Log;

import com.p2p.pppp_api.PPPP_APIs;
import com.pppcommon.FrameH264Data;
import com.pppcommon.PPPHead;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by SMY on 2017/12/29.
 */

public class VideoSender implements VideoSenderManager.VideoDataObserver{
    private static final String TAG = "VideoSender";

    private final int session;

    private BlockingQueue<FrameH264Data> queue = new LinkedBlockingDeque<>();
    private ExecutorService executor;

    public VideoSender(int session) {
        this.session = session;
    }

    public void start() {
        Log.d(TAG, "start: video sender");

        queue.clear();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }

        executor = Executors.newSingleThreadExecutor();
        executor.execute(new SendVideoRunnable());
        VideoSenderManager.getInstance().addVideoObserver(this);
    }

    public void stop() {
        VideoSenderManager.getInstance().deleteVideoObserver(this);
    }

    @Override
    public void onVideo(FrameH264Data data) {
        if (data != null) {
            if (queue.size() > 0) {
                Log.d(TAG, "onVideo: queue size > 0, clear");
                queue.clear();
            }
            try {
                queue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendVideoRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    FrameH264Data frame = queue.take();
                    if (frame.getData() != null && frame.getData().length > 0) {
                        sendVideoFrame(frame);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendVideoFrame(FrameH264Data frame) {
        int len = frame.getData().length;
        Log.d(TAG, "sendVideoFrame: send head first, length: " + len);
        int ret = PPPP_APIs.PPPP_Write(session, TNPSession.CH_VIDEO, frame.getHead().toByteArray(), PPPHead.HEAD_SIZE);
        Log.d(TAG, "sendVideoFrame: send head result : " + ret);

        ret = PPPP_APIs.PPPP_Write(session, TNPSession.CH_VIDEO, frame.getData(), len);
        Log.d(TAG, "sendVideoFrame: ret:" + ret);
        if (ret < 0) {
            Log.e(TAG, "sendVideoFrame: error");
            stop();
        }
    }
}


