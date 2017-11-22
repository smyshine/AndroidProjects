package smy.com.screencapture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by SMY on 2017/11/22.
 */

public class ScreenShotter {
    public static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String APP_DIR = SDCARD_ROOT + "/smy/";
    private final SoftReference<Context> mRefContext;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private String mLocalUrl = "";

    private int mScreenWidth, mScreenHeight;
    private Handler handler = new Handler();

    public ScreenShotter(Context context, Intent data, int mScreenWidth, int mScreenHeight) {
        this(context, data, mScreenWidth, mScreenHeight, false);
    }

    public ScreenShotter(Context context, Intent data, int mScreenWidth, int mScreenHeight, boolean record) {
        this.mRefContext = new SoftReference<Context>(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            this.mScreenWidth = mScreenWidth;
            this.mScreenHeight = mScreenHeight;
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, data);
            if (!record){
                mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
            }
        }
    }

    private MediaProjectionManager getMediaProjectionManager(){
        return (MediaProjectionManager) getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private Context getContext(){
        return mRefContext.get();
    }

    private OnShotListener mShotListener;

    public void startScreenShot(OnShotListener listener){
        mShotListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            virtualDisplay();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Image image = mImageReader.acquireLatestImage();
                    new SaveImageTask().execute(image);
                }
            }, 300);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay(){
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-morror",
                mScreenWidth,
                mScreenHeight,
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,
                null);
    }

    private class SaveImageTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
            }

            if (mShotListener != null){
                mShotListener.onFinish(mLocalUrl);
            }

            if (mVirtualDisplay != null){
                mVirtualDisplay.release();
            }
        }

        @Override
        protected Bitmap doInBackground(Image... images) {
            if (images == null || images.length < 1 || images[0] == null){
                return null;
            }

            Image image = images[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane planes = image.getPlanes()[0];
            final ByteBuffer buffer = planes.getBuffer();
            //每个像素的间距
            int pixelStride = planes.getPixelStride();
            //总的间距
            int rowStride = planes.getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();

            File fileImage = null;
            if (bitmap != null){
                try {
                    if (TextUtils.isEmpty(mLocalUrl)){
                        File dir = new File(APP_DIR);
                        if (!dir.exists()){
                            dir.mkdirs();
                        }
                        mLocalUrl = APP_DIR + System.currentTimeMillis() + ".jpg";
                    }
                    fileImage = new File(mLocalUrl);

                    if (!fileImage.exists()){
                        fileImage.createNewFile();
                    }

                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null){
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            if (fileImage != null){
                return bitmap;
            }
            return null;
        }
    }

    public interface OnShotListener{
        void onFinish(String path);
    }


    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private MediaMuxer mediaMuxer;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private int videoTrackIndex = -1;
    private boolean muxerStarted = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScreenRecord(OnShotListener listener){
        mShotListener = listener;
        new Thread() {
            @Override
            public void run() {
                try {
                    prepareRecord();
                    mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenShout",
                            mScreenWidth,
                            mScreenHeight,
                            Resources.getSystem().getDisplayMetrics().densityDpi,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                            surface,
                            null,
                            null);

                    recordVirtualDisplay();
                } finally {
                    release();
                }
            }
        }.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void release(){
        if (mediaCodec != null){
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (mVirtualDisplay != null){
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null){
            mMediaProjection.stop();
        }
        if (mediaMuxer != null){
            mediaMuxer.release();
            mediaMuxer = null;
        }
        if (mShotListener != null){
            mShotListener.onFinish(mLocalUrl);
        }
    }

    public void stopRecord(){
        mQuit.set(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void prepareRecord(){
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", mScreenWidth, mScreenHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            surface = mediaCodec.createInputSurface();
            mediaCodec.start();
            if (TextUtils.isEmpty(mLocalUrl)){
                File dir = new File(APP_DIR);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                mLocalUrl = APP_DIR + System.currentTimeMillis() + ".mp4";
            }
            mediaMuxer = new MediaMuxer(mLocalUrl, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void recordVirtualDisplay(){
        while (!mQuit.get()){
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                resetOutputFormat();
            } else if (index >= 0){
                encodeToVideoTrack(index);
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }

    private void resetOutputFormat(){
        MediaFormat newFormat = mediaCodec.getOutputFormat();
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
        muxerStarted = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void encodeToVideoTrack(int index){
        ByteBuffer encodeData = mediaCodec.getOutputBuffer(index);
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0){
            encodeData = null;
        }
        if (encodeData != null){
            encodeData.position(bufferInfo.offset);
            encodeData.limit(bufferInfo.offset + bufferInfo.size);
            mediaMuxer.writeSampleData(videoTrackIndex, encodeData, bufferInfo);
        }
    }

}
