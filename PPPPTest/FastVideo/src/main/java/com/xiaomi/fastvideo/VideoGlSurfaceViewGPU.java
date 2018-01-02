
package com.xiaomi.fastvideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;

import com.xiaomi.fastvideo.AndroidH264DecoderUtil.DecoderProperties;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoGlSurfaceViewGPU extends VideoGlSurfaceView implements OnFrameAvailableListener {

    private final static String TAG = "VideoDecoderGPU";

    Photo mPhoto;
    Photo mTexturePhoto;

    int mSurfaceTextureId;
    volatile boolean updateSurface = false;
    GlslFilter mTextureFilter;
    float[] mTextureMatrix = new float[16];

    volatile VideoDecodeThread mVideoDecodeThread;

    DecoderProperties mDecoderProperties;

    static class VideoDecodeThread extends WorkThread {
        int mVideoWidth;
        int mVideoHeight;
        int mWidth;
        int mHeight;
        VideoFrame mRemainFrame;
        private static final int DEQUEUE_INPUT_TIMEOUT = 2000;
        private static final int DEQUEUE_OUTPUT_TIMEOUT = 2000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        DecoderProperties mDecoderProperties;
        private MediaCodec decoder;
        private ByteBuffer[] inputBuffers;
        private ByteBuffer[] outputBuffers;
        volatile boolean mInitialError = false;

        Surface mSurface;
        SurfaceTexture mSurfaceTexture;

        WeakReference<VideoGlSurfaceViewGPU> mVideoGlSurfaceViewGPURef;

        public VideoDecodeThread(VideoGlSurfaceViewGPU videoGlSurfaceViewGPU, DecoderProperties decoderProperties) {
            super("VideoDecodeThread");
            mVideoGlSurfaceViewGPURef = new WeakReference<VideoGlSurfaceViewGPU>(videoGlSurfaceViewGPU);
            mDecoderProperties = decoderProperties;

            MiLog.D(TAG + " VideoDecodeThread start");
        }

        public int getVideoWidth() {
            return mVideoWidth;
        }

        public int getVideoHeight() {
            return mVideoHeight;
        }

        @Override
        protected int doRepeatWork() throws InterruptedException {
            if (!mIsRunning)
                return 0;
            VideoFrame frame = null;
            if (mRemainFrame != null) {
                frame = mRemainFrame;
                mRemainFrame = null;
            } else {
                if (mVideoGlSurfaceViewGPURef != null && mVideoGlSurfaceViewGPURef.get() != null) {
                    frame = mVideoGlSurfaceViewGPURef.get().takeVideoFrame();
                }
            }
            if (!mIsRunning)
                return 0;

            long lastTime = System.currentTimeMillis();
            if (frame == null || frame.data == null) {
                return 0;
            }
            if (mInitialError) {
                return 0;
            }
            if (decoder == null
                    || frame.width != mWidth
                    || frame.height != mHeight) {
                MiLog.D(TAG + " release media decoder,"
                        + " isIFrame:" + frame.isIFrame
                        + " (" + mWidth + "," + mHeight
                        + ")-->(" + frame.width + "," + frame.height + ")" );

                mWidth = frame.width;
                mHeight = frame.height;
                releaseMediaDecode();
                configureMediaDecode(mWidth, mHeight);
            }
            if (decoder == null) {
                return 0;
            }

            int inputBufIndex = decoder.dequeueInputBuffer(DEQUEUE_INPUT_TIMEOUT);
            if (inputBufIndex >= 0) {
                ByteBuffer dstBuf = inputBuffers[inputBufIndex];
                dstBuf.rewind();
                dstBuf.put(frame.data);
                decoder.queueInputBuffer(inputBufIndex, 0, frame.data.length,
                        frame.timeStamp * 1000, 0);
            } else {
                mRemainFrame = frame;
            }
            while (true) {
                if (!mIsRunning)
                    return 0;
                int res = decoder.dequeueOutputBuffer(info, DEQUEUE_OUTPUT_TIMEOUT);
                if (res >= 0) {
                    MediaFormat outformat = decoder.getOutputFormat();
                    mVideoWidth = outformat.getInteger(MediaFormat.KEY_WIDTH);
                    mVideoHeight = outformat.getInteger(MediaFormat.KEY_HEIGHT);
                    decoder.releaseOutputBuffer(res, true);
                } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = decoder.getOutputBuffers();
                } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                } else {
                    break;
                }
            }

            long decodeOneFrameMilliseconds = System.currentTimeMillis() - lastTime;
            if (mVideoGlSurfaceViewGPURef != null && mVideoGlSurfaceViewGPURef.get() != null) {
                mVideoGlSurfaceViewGPURef.get().onDecodeTime(decodeOneFrameMilliseconds);
            }

            MiLog.d(TAG, "decode " + frame.toString() + ", decodeTime:" + decodeOneFrameMilliseconds);

            return 0;
        }

        @Override
        protected void doInitial() {
            MiLog.D(TAG + " doInitial");
            mWidth = 0;
            mHeight = 0;
            int surfaceId = 0;
            if (mVideoGlSurfaceViewGPURef != null && mVideoGlSurfaceViewGPURef.get() != null) {
                surfaceId = mVideoGlSurfaceViewGPURef.get().getSurfaceTextureId();
            }
            mSurfaceTexture = new SurfaceTexture(surfaceId);
            mSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {

                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    if (mVideoGlSurfaceViewGPURef != null
                            && mVideoGlSurfaceViewGPURef.get() != null) {
                        mVideoGlSurfaceViewGPURef.get().onFrameAvailable(surfaceTexture);
                    }
                }
            });
            mSurface = new Surface(mSurfaceTexture);
            mInitialError = false;
        }

        @Override
        protected void doRelease() {
            MiLog.d(TAG, "doRelease");

            mSurfaceTexture.setOnFrameAvailableListener(null);
            mSurfaceTexture.release();
            mSurface.release();

            mVideoGlSurfaceViewGPURef = null;
            releaseMediaDecode();

            MiLog.D(TAG + " VideoDecodeThread stop");
        }

        void configureMediaDecode(int width, int height) {
            MiLog.D(TAG + " configureMediaDecode width:" + width + " height:" + height);
            try {
                MediaFormat format = MediaFormat.createVideoFormat(
                        AndroidH264DecoderUtil.AVC_MIME_TYPE,
                        width,
                        height);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                        mDecoderProperties.colorFormat);
                MiLog.D(TAG + " Codec Name--------" + mDecoderProperties.codecName +
                        "Codec Format--------" + mDecoderProperties.colorFormat);
                try {
                    decoder = MediaCodec.createByCodecName(mDecoderProperties.codecName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // decoder =
                // MediaCodec.createDecoderByType(AndroidH264DecoderUtil.AVC_MIME_TYPE);
                decoder.configure(format, mSurface, null, 0);
                decoder.start();
                inputBuffers = decoder.getInputBuffers();
                outputBuffers = decoder.getOutputBuffers();
            } catch (Exception e) {
                mInitialError = true;
                releaseMediaDecode();
                if (mVideoGlSurfaceViewGPURef != null && mVideoGlSurfaceViewGPURef.get() != null) {
                    mVideoGlSurfaceViewGPURef.get().onHardDecodeException(e);
                }
            }
        }

        void releaseMediaDecode() {
            MiLog.D(TAG + " releaseMediaDecode");
            if (decoder != null) {
                try {
                    decoder.stop();
                    decoder.release();
                    decoder = null;
                    MiLog.d(TAG, "Release decoder success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void updateSurfaceTexture(float[] mtx) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mtx);
        }
    }

    public VideoGlSurfaceViewGPU(Context context, AttributeSet attrs, DecoderProperties decoderProperties,
                                 HardDecodeExceptionCallback callback) {
        super(context, attrs, callback);
        mDecoderProperties = decoderProperties;
    }

    @Override
    protected void initial() {
        super.initial();

        mTextureFilter = new GlslFilter(getContext());
        mTextureFilter.setType(GlslFilter.GL_TEXTURE_EXTERNAL_OES);
        mTextureFilter.initial();
        mSurfaceTextureId = RendererUtils.createTexture();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);
        RendererUtils.checkGlError("glBindTexture mTextureID");
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        updateSurface = false;

        RendererUtils.checkGlError("surfaceCreated");

        mVideoDecodeThread = new VideoDecodeThread(this, mDecoderProperties);
        mVideoDecodeThread.start();

    }

    @Override
    protected void release() {
        super.release();
        if (mVideoDecodeThread != null) {
            mVideoDecodeThread.stopThreadAsyn();
            mVideoDecodeThread = null;
        }

        RendererUtils.clearTexture(mSurfaceTextureId);
        mTextureFilter.release();
        if (mPhoto != null) {
            mPhoto.clear();
            mPhoto = null;
        }

    }

    @Override
    public void drawFrame() {
        super.drawFrame();
        if (mVideoDecodeThread == null) {
            return;
        }
        int videoWith = mVideoDecodeThread.getVideoWidth();
        int videoHeight = mVideoDecodeThread.getVideoHeight();
        if (videoWith == 0 || videoHeight == 0) {
            return;
        }
        long lastTime = System.currentTimeMillis();
        if (mPhoto == null) {
            mPhoto = Photo.create(videoWith, videoHeight);
        } else {
            mPhoto.updateSize(videoWith, videoHeight);
        }
        if(mTexturePhoto==null){
            mTexturePhoto = new Photo(mSurfaceTextureId,videoWith, videoHeight);
        }
        synchronized (this) {
            if (updateSurface) {
                mVideoDecodeThread.updateSurfaceTexture(mTextureMatrix);
                mTextureFilter.updateTextureMatrix(mTextureMatrix);
                updateSurface = false;
            }
        }
        RendererUtils.checkGlError("drawFrame");
        mTextureFilter.process(mTexturePhoto, mPhoto);
        Photo dst = appFilter(mPhoto);
        setPhoto(dst);

//        MiLog.d(TAG, "render frame:(" + videoWith + "," + videoHeight
//                + "), render time:" + (System.currentTimeMillis() - lastTime));

    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateSurface = true;
        requestRender();
    }

    VideoFrame takeVideoFrame() throws InterruptedException {
        return mAVFrameQueue.take();
    }

    int getSurfaceTextureId() {
        return mSurfaceTextureId;
    }
}
