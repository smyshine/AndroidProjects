package smy.com.vrplayer.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.text.TextUtils;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import smy.com.vrplayer.common.GLHelpers;
import smy.com.vrplayer.common.XYGLHandler;
import smy.com.vrplayer.listener.VRPlayListener;
import smy.com.vrplayer.listener.ViewDirectionChangeListener;
import smy.com.vrplayer.objects.base.CombineParams;

/**
 * Created by ChenZheng on 2016/10/21.
 * Image,Video,DualVideo,三种renderer的基类
 */
public abstract class AbstractRenderer implements GvrView.StereoRenderer {

    protected RenderObjectHelper mRenderObjectHelper = new RenderObjectHelper();

    public float[] mHeadView = new float[16];
    protected XYGLHandler mXYGLHandler;

    protected AbstractRenderer(CombineParams params) {
        mRenderObjectHelper.init(params);
    }

    protected AbstractRenderer() {
        mRenderObjectHelper.init();
    }

    public abstract void onDestroy();

    public abstract void onPause();

    public abstract void onResume();


    /*For Video*/
    public abstract void setVideoPath(String dataSource);

    public abstract void setMediaPlayerSeekTo(int progress);

    public abstract void startPlay();

    public abstract void pausePlay();

    public abstract void onStopPlay();

    public abstract void releasePlayer();

    public abstract void setVideoPlayListener(VRPlayListener listener);

    /*For Image*/
    public abstract void initBitmap(Bitmap bitmap);


    public void setGLHandler(XYGLHandler handler){
        mXYGLHandler = handler;
    }

    public void setViewDirectionChangeListener(ViewDirectionChangeListener listener){
        mRenderObjectHelper.setViewDirectionChangeListener(listener);
    }

    public void setRenderMode(int renderMode, RenderObjectHelper.ChangeRenderListener listener) {
        mRenderObjectHelper.setRenderMode(renderMode, listener);
    }

    public void setRenderMode(int renderMode) {
        setRenderMode(renderMode, null);
    }

    public int getRenderMode(){
        return mRenderObjectHelper.getCurrentRenderMode();
    }

    public void onZoomGesture(float scale) {
        mRenderObjectHelper.onZoomGesture(scale);
    }

    public void onOrientationChange(boolean landscape){
        mRenderObjectHelper.onOrientationChange(landscape);
    }

    public void setSensorMatrix(float[] matrixS) {
        mRenderObjectHelper.setSensorMatrix(matrixS);
    }

    /**
     * 重置摄像机朝向
     */
    public void resetCameraOrientation() {
        mRenderObjectHelper.resetOrientation();
    }

    public void setGestureRotateAngle(float rotateXAngle, float rotateYAngle) {
        mRenderObjectHelper.setGestureRotateAngle(rotateXAngle, rotateYAngle);
    }

    public void setFlingVelocity(float velocityX, float velocityY){
        mRenderObjectHelper.setFlingVelocity(velocityX, velocityY);
    }

    @Override
    public void onDrawEye(Eye eye) {
        if (!updateImage()) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            return;
        }
        mXYGLHandler.dealMessage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        int eyeType = eye == null ? 0 : eye.getType();
        draw(eyeType);
        doScreenShot(eye);//check if screen shot requested
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        GLHelpers.checkMaxTextureSize();
        mRenderObjectHelper.mPlayModelFactory.clear();
    }

    @Override
    public void onSurfaceChanged(int width, int height){
        GLES20.glViewport(0, 0, width, height);
        mRenderObjectHelper.setScreenSize(width,height);
        mXYGLHandler.dealMessage();
    }


    protected abstract boolean updateImage();

    protected abstract void draw(int eyeType);



    public interface screenShotListener{
        void onFinish(boolean result, String path);
    }

    protected String mScreenShotPath;
    protected screenShotListener mScreenListener;
    public void startScreenShot(String path, screenShotListener listener){
        this.mScreenShotPath = path;
        this.mScreenListener = listener;
    }

    private void doScreenShot(Eye eye){
        if (!TextUtils.isEmpty(mScreenShotPath)){
            int width = eye.getViewport().width;
            int height = eye.getViewport().height;
            int[] b = new int[width * height];
            int[] bt = new int[width * height];
            IntBuffer ib = IntBuffer.wrap(b);
            ib.position(0);
            GLES20.glReadPixels(eye.getViewport().x, eye.getViewport().y,
                    eye.getViewport().width, eye.getViewport().height,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
            for (int i = 0, k = 0; i < height; i++, k++) {
                for (int j = 0; j < width; j++) {
                    int pix = b[i * width + j];
                    int pb = (pix >> 16) & 0xff;
                    int pr = (pix << 16) & 0xffff0000;
                    int pix1 = (pix & 0xff00ff00) | pr | pb;
                    bt[(height - k - 1) * width + j] = pix1;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888);
            String url;
            File fileImage = null;
            FileOutputStream out = null;
            if (bitmap != null) {
                try {
                    fileImage = new File(mScreenShotPath);

                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                    out = new FileOutputStream(fileImage);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                } finally {
                    if (out != null){
                        try {
                            out.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (mScreenListener != null){
                mScreenListener.onFinish(fileImage != null, mScreenShotPath);
            }
            mScreenShotPath = null;
        }
    }
}
