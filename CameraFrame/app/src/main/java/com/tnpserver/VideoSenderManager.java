package com.tnpserver;

import android.util.Log;

import com.pppcommon.FrameH264Data;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by SMY on 2017/12/29.
 */

public class VideoSenderManager {

    private static final String TAG = "VideoSenderManager";

    private static VideoSenderManager manager;

    public static VideoSenderManager getInstance() {
        if (manager == null) {
            synchronized (VideoSenderManager.class) {
                if (manager == null) {
                    manager = new VideoSenderManager();
                }
            }
        }
        return manager;
    }

    private VideoSenderManager() {

    }

    public void release() {
        videoDataObservers.clear();
    }


    private List<VideoDataObserver> videoDataObservers = new LinkedList<>();

    public synchronized void addVideoObserver(VideoDataObserver observer) {
        if (videoDataObservers.contains(observer)) {
            return;
        }
        videoDataObservers.add(observer);
    }

    public synchronized void deleteVideoObserver(VideoDataObserver observer) {
        videoDataObservers.remove(observer);
    }

    public interface VideoDataObserver {
        void onVideo(FrameH264Data data);
    }

    public void dispatchVideo(FrameH264Data data) {
        ListIterator<VideoDataObserver> iterator = videoDataObservers.listIterator();
        while (iterator.hasNext()) {
            VideoDataObserver observer = iterator.next();
            Log.d(TAG, "dispatchVideo:---!waitObservers.contains(observer)++++++");
            observer.onVideo(data);
        }
    }
}
