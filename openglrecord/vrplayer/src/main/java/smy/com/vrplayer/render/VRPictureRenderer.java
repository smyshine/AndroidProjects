package smy.com.vrplayer.render;

import android.graphics.Bitmap;

import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

import smy.com.vrplayer.listener.VRPlayListener;
import smy.com.vrplayer.objects.base.CombineParams;

/**
 * 静态全景图像Renderer
 */
public class VRPictureRenderer extends AbstractRenderer {
    private Bitmap mPicture;

    public VRPictureRenderer(CombineParams params) {
        super(params);
    }

    @Override
    public void initBitmap(Bitmap bitmap) {
        this.mPicture = bitmap;
        mRenderObjectHelper.source = RenderObjectHelper.SOURCE_PICTURE;
        mRenderObjectHelper.initBitmapObject(bitmap.getWidth() > bitmap.getHeight(), mPicture);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getHeadView(mHeadView, 0);
    }

    @Override
    protected boolean updateImage() {
        return (mRenderObjectHelper.getBitmapObject(false) != null);
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
        super.onSurfaceChanged(width,height);
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
    }


    @Override
    public void onDestroy() {
        mRenderObjectHelper.destroy();
        if (mPicture != null && !mPicture.isRecycled()){
            mPicture.recycle();
        }
    }


    @Override
    public void setVideoPlayListener(VRPlayListener listener) {
    }

    @Override
    public void onStopPlay() {
    }

    @Override
    public void releasePlayer() {
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void startPlay() {

    }

    @Override
    public void pausePlay(){
    }

    @Override
    public void setMediaPlayerSeekTo(int progress) {
    }
}
