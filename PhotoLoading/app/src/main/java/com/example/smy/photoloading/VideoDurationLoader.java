package com.example.smy.photoloading;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SMY on 2016/9/2.
 */
public class VideoDurationLoader {
    private static final String TAG = "VideoDurationLoader";
    private static final int DURATION_CACHE_MAX = 400;
    private static VideoDurationLoader instance;


    private ExecutorService taskExecutor;
    private LruCache<String, Integer> durationCache;

    private VideoDurationLoader() {
        durationCache = new LruCache<String, Integer>(DURATION_CACHE_MAX);
        initExecutor();
    }

    public synchronized static VideoDurationLoader getInstance() {
        if (instance == null) {
            instance = new VideoDurationLoader();
        }
        return instance;
    }

    /**
     * 初始化
     */
    private void initExecutor() {
        taskExecutor = Executors.newSingleThreadExecutor(new CustomThreadPoolFactory(TAG));
    }

    public void stop() {
        if (taskExecutor != null && !(taskExecutor).isShutdown()) {
            (taskExecutor).shutdownNow();
        }
    }

    /*后台获取视频文件的时长，并更新在TextView中*/
    public void loadDuration(Context context, LocalMediaInfo info, TextView textView, DurationLoadingListener listener) {
        String path = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(info.id))
                .toString();
        textView.setTag(R.integer.video_duration_textview_key, path);
        Integer cachedDuration = durationCache.get(path);
        if (cachedDuration != null) {
            listener.onLoadingComplete(path, textView, cachedDuration);
        } else {
            if (info.type == LocalMediaInfo.TYPE_VIDEO) {
                VideoDurationParseTask newTask = new VideoDurationParseTask(context, path, textView, listener, PathType.LOCAL_URI);
                if (taskExecutor == null || taskExecutor.isShutdown()) {
                    initExecutor();
                }
                taskExecutor.execute(newTask);
            }
        }

    }

    class VideoDurationParseTask implements Runnable {
        private final Context context;
        private final PathType pathType;
        private final WeakReference<TextView> weakTextView;
        private final String path;
        private final DurationLoadingListener listener;

        @Override
        public void run() {
            TextView textView = weakTextView.get();
            if (textView == null) {
                listener.onLoadingFailed(path, null);
            } else if (isViewWasReused(textView)) {
                listener.onLoadingCancelled(path, textView);
            } else {
                listener.onLoadingStarted(path, textView);
                String realPath;
                if (pathType == PathType.LOCAL_URI) {
                    realPath = FileUtil.getRealFilePath(context, Uri.parse(path));
                } else {
                    realPath = path;
                }
                int duration = getVideoDurationInternal(realPath);
                if (duration == 0) duration = 1;
                durationCache.put(path, duration);/*即便view被reused了，获取到的时长不能浪费，缓存之*/
                if (!isViewWasReused(textView)) {
                    listener.onLoadingComplete(path, textView, duration);
                }
            }
        }

        public VideoDurationParseTask(Context context, String path, TextView textView, DurationLoadingListener listener, PathType pathType) {
            this.context = context;
            this.path = path;
            this.weakTextView = new WeakReference<TextView>(textView);
            this.listener = listener;
            this.pathType = pathType;
        }


        private boolean isViewWasReused(View view) {
            String currentCacheKey = (String) view.getTag(R.integer.video_duration_textview_key);
            return !TextUtils.equals(path, currentCacheKey);
        }
    }

    private int getVideoDurationInternal(String path) {
        return 1000;/*MediaUtil.getDurationInteger(path);*/
    }

    public interface DurationLoadingListener {
        /*NOTE: Those method maybe called on backgrond thread*/
        void onLoadingStarted(String path, View view);

        void onLoadingFailed(String path, View view);

        void onLoadingComplete(String path, View view, int durationS);

        void onLoadingCancelled(String path, View view);
    }

    public enum PathType {
        LOCAL_URI, HTTP_PATH
    }
}
