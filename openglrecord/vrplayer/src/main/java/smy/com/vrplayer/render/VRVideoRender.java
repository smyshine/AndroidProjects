package smy.com.vrplayer.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES20;
import android.view.Surface;

import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.listener.VRPlayListener;
import smy.com.vrplayer.objects.base.CombineParams;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 相机出来的原始的 双鱼眼镜头视频Renderer
 * Created by hzb on 16-10-9.
 */
public class VRVideoRender extends AbstractRenderer implements
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener, SurfaceTexture.OnFrameAvailableListener {

    public static final int AndroidMediaPlayer = 0;
    public static final int IjkMediaPlayer = 1;

    private int mTextureID = -1;
    private SurfaceTexture mSurfaceTexture;
    private IMediaPlayer mMediaPlayer;

    private VRPlayListener mVideoPlayListener;
    private Object mMediaplayerReleaseLock = new Object();

    private int mSphereWidth;
    private int mSphereHeight;

    private int mVideoType;
    private int mPlayerType;

    private Context mContext;

    private Uri mDataSource;
    private float[] mSTMatrix = new float[16];

    public VRVideoRender(Context context, int videoType, int playerType, CombineParams params) {
        super(params);
        mContext = context;
        mSphereWidth = VrConstant.FULL_VIDEO_WIDTH;
        mSphereHeight = VrConstant.FULL_VIDEO_HEIGHT;
        mVideoType = videoType;
        mPlayerType = playerType;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getHeadView(mHeadView, 0);
    }

    @Override
    protected boolean updateImage() {
        if (mRenderObjectHelper.getVideoObject(false) == null || mSurfaceTexture == null)
            return false;
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mRenderObjectHelper.setSTMatrix(mSTMatrix);
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                notifyTime((int) mMediaPlayer.getCurrentPosition());
            }
        }
        return true;
    }

    @Override
    protected void draw(int eyeType) {
        mRenderObjectHelper.draw();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        mRenderObjectHelper.source = RenderObjectHelper.SOURCE_VIDEO;
        mRenderObjectHelper.initVideoObject(mVideoType == VrConstant.SOURCE_FILE_TYPE_PANO,
                mVideoType, mSphereWidth, mSphereHeight);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        super.onSurfaceCreated(eglConfig);
    }

    @Override
    public void onRendererShutdown() {
        onDestroy();

    }

    @Override
    public void setVideoPath(String dataSource) {
        initMediaPlayer(Uri.parse(dataSource));
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onFrameAvailable();
        }
    }

    private void initMediaPlayer(Uri dataSource) {
        synchronized (mMediaplayerReleaseLock) {
            mDataSource = dataSource;
            if (mTextureID < 0) {
                mTextureID = GLHelpers.genExternalTexture();
                mSurfaceTexture = new SurfaceTexture(mTextureID);
                mSurfaceTexture.setOnFrameAvailableListener(this);
                if (mRenderObjectHelper.getVideoObject(false) != null) {
                    mRenderObjectHelper.getVideoObject(false).setSingleTex(mTextureID);
                }
                if (mRenderObjectHelper.getVideoObject(true) != null) {
                    mRenderObjectHelper.getVideoObject(true).setSingleTex(mTextureID);
                }
            }
            if (this.mMediaPlayer != null) {
                try {
                    notifyVideoInit((int) mMediaPlayer.getDuration());
                    mMediaPlayer.start();
                    return;
                } catch (IllegalStateException e) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }

            try {
                createPlayer();
                mMediaPlayer.setDataSource(mContext, dataSource);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);

                Surface surface = new Surface(mSurfaceTexture);
                mMediaPlayer.setSurface(surface);
                surface.release();

                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void createPlayer() {
        switch (mPlayerType) {
            case AndroidMediaPlayer:
                mMediaPlayer = new AndroidMediaPlayer();
                break;
            case IjkMediaPlayer:
                IjkMediaPlayer mPlayer = new IjkMediaPlayer();
                mPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                mMediaPlayer = mPlayer;
                break;
            default:
                mMediaPlayer = new AndroidMediaPlayer();
        }
    }

    @Override
    public void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseMediaPlayer();
                releaseSurfaceTexture();
                mRenderObjectHelper.destroy();
            }
        }, "videoRenderDestroy").start();
    }


    /*video类renderer，该方法为空*/
    @Override
    public void initBitmap(Bitmap bitmap) {

    }

    @Override
    public void setVideoPlayListener(VRPlayListener listener) {
        mVideoPlayListener = listener;
    }

    public void notifyVideoInit(int length) {
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoInit(length);
        }
    }

    public void notifyTime(int time) {
        if (mVideoPlayListener != null) {
            mVideoPlayListener.listenTime(time);
        }
    }

    @Override
    public void onStopPlay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseMediaPlayer();
                releaseSurfaceTexture();
            }
        }, "videoRenderStop").start();
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoFinish();
        }
    }

    @Override
    public void releasePlayer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseMediaPlayer();
            }
        }, "releasePlayer").start();
    }

    private void releaseMediaPlayer() {
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                try {
                    mMediaPlayer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mMediaPlayer.release();
                }
                mMediaPlayer = null;
            }
        }
    }

    private void releaseSurfaceTexture() {
        if (mTextureID != -1) {
            int[] textures = new int[1];
            textures[0] = mTextureID;
            GLES20.glDeleteTextures(1, textures, 0);
            mTextureID = -1;
        }

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    @Override
    public void onPause() {
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void startPlay() {
        synchronized (mMediaplayerReleaseLock) {
            if (mMediaPlayer == null) {
                initMediaPlayer(mDataSource);
            } else if (mMediaPlayer != null && mMediaPlayer.getVideoHeight() != 0 && mMediaPlayer.getVideoWidth() != 0 && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    public void pausePlay() {
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    public void setMediaPlayerSeekTo(int progress) {
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                mMediaPlayer.seekTo(progress);
            }
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer player, int i) {
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onBufferingUpdate(i);
        }
    }

    @Override
    public void onCompletion(IMediaPlayer player) {
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoFinish();
        }
    }

    @Override
    public boolean onError(IMediaPlayer player, int i, int i1) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseMediaPlayer();
            }
        }, "videoRenderError").start();
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onErrorPlaying(i, i1);
        }
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer player) {
        synchronized (mMediaplayerReleaseLock) {
            if (this.mMediaPlayer != null) {
                notifyVideoInit((int) mMediaPlayer.getDuration());
                mMediaPlayer.start();
            }
        }
    }
}
