package com.xiaomi.fastvideo;

import android.content.Context;
import android.os.Build;

/**
 * Created by livy on 16/3/17.
 */
public class VideoGlSurfaceViewFactory {
    public static VideoGlSurfaceView createVideoGlSurfaceView(Context context,HardDecodeExceptionCallback callback, boolean useHard) {
        if (useHard) {
            AndroidH264DecoderUtil.DecoderProperties decoderProperty = AndroidH264DecoderUtil.findAVCDecoder();
            if (decoderProperty != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    return new VideoGlSurfaceViewGPU(context, null, decoderProperty, callback);
                } else {
                    return new VideoGlSurfaceViewGPULollipop2(context, null, decoderProperty, callback);
                }
            }
        }
        return new VideoGlSurfaceViewFFMPEG(context, null, callback);
    }
}