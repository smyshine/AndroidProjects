
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;

import com.xiaomi.fastvideo.AndroidH264DecoderUtil.DecoderProperties;

import java.nio.ByteBuffer;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VideoGlSurfaceViewGPULollipop extends VideoGlSurfaceView implements OnFrameAvailableListener {

    private final static String TAG = "VideoDecoderGPULollipop";

    static final int MSG_INITIAL = 1;
    static final int MSG_UNINITIAL = 2;
    static final int MSG_REINITIAL = 3;
    static final int MSG_LAST = 4;

    Photo mPhoto;
    Photo mTexturePhoto;
    int mSurfaceTextureId;
    volatile boolean updateSurface = false;
    GlslFilter mTextureFilter;
    float[] mTextureMatrix = new float[16];

    int mVideoWidth;
    int mVideoHeight;
    int mWidth;
    int mHeight;
    DecoderProperties mDecoderProperties;
    private volatile MediaCodec decoder;

    HandlerThread mDecoderThread;
    Handler mDecoderThreadHandler;
    Surface mSurface;
    SurfaceTexture mSurfaceTexture;
    VideoFrame mRemainVideoFrame;

    volatile boolean mStarted = false;
    volatile  boolean mInitialError = false;


    public VideoGlSurfaceViewGPULollipop(Context context, AttributeSet attrs, DecoderProperties decoderProperties,
                                         HardDecodeExceptionCallback callback) {
        super(context, attrs, callback);
        mDecoderProperties = decoderProperties;
    }

    @Override
    protected void initial() {
        super.initial();
        if (mStarted)
            return;
        mInitialError = false;
        mDecoderThread = new HandlerThread("video_decoder");
        mDecoderThread.start();
        mDecoderThreadHandler = new Handler(mDecoderThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_INITIAL: {
                        configureMediaDecode(msg.arg1, msg.arg2);
                        break;
                    }
                    case MSG_REINITIAL: {
                        releseMediaDecode();
                        configureMediaDecode(msg.arg1, msg.arg2);
                        break;
                    }
                    case MSG_UNINITIAL: {
                        releseMediaDecode();
                        break;
                    }
                }
            }
        };

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

        mWidth = 0;
        mHeight = 0;
        mSurfaceTexture = new SurfaceTexture(getSurfaceTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                VideoGlSurfaceViewGPULollipop.this.onFrameAvailable(surfaceTexture);
            }
        });
        mSurface = new Surface(mSurfaceTexture);
        mStarted = true;
    }

    void releseMediaDecode() {
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

    void configureMediaDecode(int width, int height) {
        if (decoder != null)
            return;
        MiLog.d(TAG, "configureMediaDecode width:" + width + " height:" + height);
        try {
            MediaFormat format = MediaFormat.createVideoFormat(
                    AndroidH264DecoderUtil.AVC_MIME_TYPE,
                    width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    mDecoderProperties.colorFormat);
            MiLog.d(TAG, "Codec Name--------" + mDecoderProperties.codecName +
                    "Codec Format--------" + mDecoderProperties.colorFormat);
            decoder = MediaCodec.createByCodecName(mDecoderProperties.codecName);
            mWidth = width;
            mHeight = height;
            decoder.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec codec, final int index) {
                    if (decoder == null || !mStarted)
                        return;
                    MiLog.d(TAG, "onInputBufferAvailable");
                    ByteBuffer inputBuffer = decoder.getInputBuffer(index);

                    VideoFrame frame = null;
                    if (mRemainVideoFrame != null) {
                        frame = mRemainVideoFrame;
                        mRemainVideoFrame = null;
                    } else {
                        try {
                            frame = mAVFrameQueue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (frame == null || frame.data == null) {
                        return;
                    }
                    if (decoder == null || frame.width != mWidth
                            || frame.height != mHeight) {
                        MiLog.d(TAG, "release media decoder,"
                                + " isIFrame:" + frame.isIFrame
                                + " (" + mWidth + "," + mHeight
                                + ")-->(" + frame.width + "," + frame.height + ")" );
                        mRemainVideoFrame = frame;
                        clearMsg();
                        releseMediaDecode();
                        configureMediaDecode(frame.width, frame.height);
                        return;
                    }

                    inputBuffer.rewind();
                    inputBuffer.put(frame.data);
                    decoder.queueInputBuffer(index, 0, frame.data.length,
                            frame.timeStamp * 1000, 0);
                }


                @Override
                public void onOutputBufferAvailable(MediaCodec codec, final int index, MediaCodec.BufferInfo info) {
                    if (decoder == null || !mStarted)
                        return;
                    MiLog.d(TAG, "onOutputBufferAvailable");
                    MediaFormat outformat = decoder.getOutputFormat();
                    mVideoWidth = outformat.getInteger(MediaFormat.KEY_WIDTH);
                    mVideoHeight = outformat.getInteger(MediaFormat.KEY_HEIGHT);
                    decoder.releaseOutputBuffer(index, true);
                }

                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                    MiLog.e(TAG, "onError:" + e.getMessage());
                    onHardDecodeException(e);
                }

                @Override
                public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                    MiLog.d(TAG, "onOutputFormatChanged");

                }
            });
            decoder.configure(format, mSurface, null, 0);
            decoder.start();
        } catch (Exception e) {
            mInitialError = true;
            releseMediaDecode();
            onHardDecodeException(e);
        }
    }

    @Override
    protected void release() {
        super.release();
        mStarted = false;
        RendererUtils.clearTexture(mSurfaceTextureId);
        mTextureFilter.release();
        if (mPhoto != null) {
            mPhoto.clear();
            mPhoto = null;
        }

        mSurfaceTexture.setOnFrameAvailableListener(null);
        mSurfaceTexture.release();
        mSurface.release();

        clearMsg();
        mDecoderThreadHandler.sendEmptyMessage(MSG_UNINITIAL);
        mDecoderThread.quitSafely();
        mDecoderThread = null;
        mDecoderThreadHandler = null;
    }

    void clearMsg() {
        if (mDecoderThreadHandler == null)
            return;
        for (int i = 0; i < MSG_LAST; i++) {
            mDecoderThreadHandler.removeMessages(i);
        }
    }

    @Override
    public void drawVideoFrame(VideoFrame frame) {
        if (!mStarted) {
            return;
        }

        if (decoder == null && !mInitialError) {
            clearMsg();
            mDecoderThreadHandler.obtainMessage(MSG_INITIAL, frame.width, frame.height).sendToTarget();
        }
        super.drawVideoFrame(frame);
    }

    @Override
    public void drawFrame() {
        super.drawFrame();
        int videoWith = mVideoWidth;
        int videoHeight = mVideoHeight;
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
        if (updateSurface) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTextureMatrix);
            mTextureFilter.updateTextureMatrix(mTextureMatrix);
            updateSurface = false;
        }
        RendererUtils.checkGlError("drawFrame");
        mTextureFilter.process(mTexturePhoto, mPhoto);
        Photo dst = appFilter(mPhoto);
        setPhoto(dst);

        MiLog.d("P2PTime", "render frame:(" + videoWith + "," + videoHeight
                + "), render time:" + (System.currentTimeMillis() - lastTime));

    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateSurface = true;
        requestRender();
    }


    int getSurfaceTextureId() {
        return mSurfaceTextureId;
    }
}
