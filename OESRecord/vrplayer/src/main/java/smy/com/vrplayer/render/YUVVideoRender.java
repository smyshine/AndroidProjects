package smy.com.vrplayer.render;

import android.graphics.Bitmap;

import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import smy.com.vrplayer.listener.VRPlayListener;
import smy.com.vrplayer.objects.base.CombineParams;

/**
 * Created by hzb on 17-4-21.
 */

public class YUVVideoRender extends AbstractRenderer {

    public YUVVideoRender(){
        mRenderObjectHelper.source = RenderObjectHelper.SOURCE_YUV;
    }

    public void setCombineParams(CombineParams params){
        mRenderObjectHelper.setParams(params);
    }

    public void initYUVBuffer( ByteBuffer bb_y, ByteBuffer bb_u, ByteBuffer bb_v){
        mRenderObjectHelper.setYUVBuffer(bb_y, bb_u, bb_v);
    }

    public void setFrameSize(int width, int height){
        mRenderObjectHelper.setFrameSize(width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        mRenderObjectHelper.destroy();
    }

    @Override
    public void onSurfaceChanged(int width, int height){
        mRenderObjectHelper.setScreenSize(width, height);
        if(mXYGLHandler != null) {
            mXYGLHandler.dealMessage();
        }
    }

    @Override
    public void onDestroy() {
        mRenderObjectHelper.destroy();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void setVideoPath(String dataSource) {

    }

    @Override
    public void setMediaPlayerSeekTo(int progress) {

    }

    @Override
    public void startPlay() {

    }

    @Override
    public void pausePlay() {

    }

    @Override
    public void onStopPlay() {

    }

    @Override
    public void releasePlayer() {
        
    }

    @Override
    public void setVideoPlayListener(VRPlayListener listener) {

    }

    @Override
    public void initBitmap(Bitmap bitmap) {

    }

    @Override
    protected boolean updateImage() {
        return mRenderObjectHelper.updateYUVImage();
    }

    @Override
    protected void draw(int eyeType) {
        mRenderObjectHelper.drawYUV();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onRendererShutdown() {

    }
}
