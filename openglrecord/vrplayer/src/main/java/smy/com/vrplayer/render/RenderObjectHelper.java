package smy.com.vrplayer.render;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;

import java.nio.ByteBuffer;

import smy.com.vrplayer.common.VrConstant;
import smy.com.vrplayer.listener.ViewDirectionChangeListener;
import smy.com.vrplayer.objects.base.CombineParams;
import smy.com.vrplayer.objects.base.XYVRBaseObject;
import smy.com.vrplayer.objects.bitmap.DualBitmapRectMatrix;
import smy.com.vrplayer.objects.bitmap.DualBitmapSphere;
import smy.com.vrplayer.objects.bitmap.PanoBitmapRectMatrix;
import smy.com.vrplayer.objects.bitmap.PanoBitmapSphere;
import smy.com.vrplayer.objects.video.DualVideoRectMatrix;
import smy.com.vrplayer.objects.video.DualVideoSphere;
import smy.com.vrplayer.objects.video.PanoVideoRectMatrix;
import smy.com.vrplayer.objects.video.PanoVideoSphere;
import smy.com.vrplayer.objects.video.StereoPanoVideoSphere;
import smy.com.vrplayer.objects.yuv.DualYUVRectAngle;
import smy.com.vrplayer.objects.yuv.DualYUVSphere;
import smy.com.vrplayer.objects.yuv.PanoYUVRectAngle;
import smy.com.vrplayer.objects.yuv.PanoYUVSphere;
import smy.com.vrplayer.render.model.AbstractRenderModel;
import smy.com.vrplayer.render.model.RenderModelOverallMatrix;
import smy.com.vrplayer.render.model.RenderModelPlanet;
import smy.com.vrplayer.render.model.RenderModelSphere;
import smy.com.vrplayer.render.model.RenderModelSphereOut;
import smy.com.vrplayer.render.model.RenderModelVR;

/**
 * Created by SMY on 2017/5/17.
 */

public class RenderObjectHelper {

    /**-----------对外---------*/
    //1.提供接口给AbstractRender执行具体绘制(draw和drawYUV)
    //2.存储AbstractRender的数据(转存给renderModel)


    //1.对外接口
    public void draw(){
        realDraw();
    }

    public void drawYUV(){
        setViewPortYUV();
        realDraw();
        releaseViewYUV();
    }

    //2.存储数据及其他接口，主要是数据操作
    public static final int SOURCE_PICTURE = 0;
    public static final int SOURCE_VIDEO = 1;
    public static final int SOURCE_YUV = 2;

    public int source = 0;// 0 picture, 1 video, 2 yuv
    public boolean isPano = false;

    public RenderMatrix mRenderMatrix = new RenderMatrix();

    //for yuv
    public ByteBuffer bb_y;
    public ByteBuffer bb_u;
    public ByteBuffer bb_v;
    public CombineParams params;

    public void init(CombineParams params){
        setParams(params);
        init();
    }

    public void init(){
        mRenderMatrix.identitySensor();
    }

    public void setParams(CombineParams params){
        this.params = params;
    }

    public void destroy(){
        mPlayModelFactory.clear();
        mPlayObjectFactory.clear();
        mCurrentYUVObject = null;
    }

    public interface ChangeRenderListener{
        void changeStart();
        void changeEnd();
    }

    public void setRenderMode(int renderMode, ChangeRenderListener listener) {
        if (mRenderMatrix.mRenderMode != renderMode) {
            mRenderMatrix.setRenderMode(renderMode);
            startRenderAnimation(getPlayModel().getAnimationDuration(), listener,
                    getPlayModel().getValuesHolder(AbstractRenderModel.PROPERTY_FIELD_OF_VIEW, mRenderMatrix),
                    getPlayModel().getValuesHolder(AbstractRenderModel.PROPERTY_EYE_Y, mRenderMatrix),
                    getPlayModel().getValuesHolder(AbstractRenderModel.PROPERTY_GESTURE_X, mRenderMatrix),
                    getPlayModel().getValuesHolder(AbstractRenderModel.PROPERTY_GESTURE_Y, mRenderMatrix));
            getPlayModel().init(mRenderMatrix.screenWidth, mRenderMatrix.screenHeight);
        }
    }

    private ValueAnimator animator;

    private void startRenderAnimation(int duration, final ChangeRenderListener listener, PropertyValuesHolder... values){
        if (animator != null){
            animator.cancel();
        }

        animator = ValueAnimator.ofPropertyValuesHolder(values).setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float view = (float) animation.getAnimatedValue(AbstractRenderModel.PROPERTY_FIELD_OF_VIEW);
                float eyeY = (float) animation.getAnimatedValue(AbstractRenderModel.PROPERTY_EYE_Y);
                float gestureX = (float) animation.getAnimatedValue(AbstractRenderModel.PROPERTY_GESTURE_X);
                float gestureY = (float) animation.getAnimatedValue(AbstractRenderModel.PROPERTY_GESTURE_Y);
                mRenderMatrix.setFieldOfView(view).setEyeY(eyeY).setGestureX(gestureX).setGestureY(gestureY);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null){
                    listener.changeStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null){
                    listener.changeEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public int getCurrentRenderMode(){
        return mRenderMatrix.mRenderMode;
    }

    public void onOrientationChange(boolean landscape){
        mRenderMatrix.onOrientationChange(landscape);
    }

    public void onZoomGesture(float scale) {
        if (source == SOURCE_YUV && mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL){
            return;//预览界面全景模式下，不处理缩放手势
        }
        mRenderMatrix.onZoomGesture(scale);
    }

    public void resetOrientation() {
        mRenderMatrix.resetOrientation();
    }

    public void setGestureRotateAngle(float rotateXAngle, float rotateYAngle) {
        if (animator != null && animator.isRunning()){
            return;//正在执行模式切换渐变动画，不处理手指滑动数据
        }
        if (source == SOURCE_YUV && mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL){
            return;//预览界面全景模式下，不处理手指滑动数据
        }
        mRenderMatrix.setGestureRotateAngle(rotateXAngle, rotateYAngle);
    }

    public void setFlingVelocity(float velocityX, float velocityY){
        if (animator != null && animator.isRunning()){
            return;//正在执行模式切换渐变动画，不处理手指滑动数据
        }
        if (source == SOURCE_YUV && mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL){
            return;//预览界面全景模式下，不处理手指滑动数据
        }
        mRenderMatrix.setFlingVelocity(velocityX, velocityY);
    }

    public void setSensorMatrix(float[] matrixS) {
        mRenderMatrix.setSensorMatrix(matrixS);
    }


    public void setSTMatrix(float[] matrix){
        mRenderMatrix.setSTMatrix(matrix);
    }

    public void setViewDirectionChangeListener(ViewDirectionChangeListener listener){
        mRenderMatrix.setViewDirectionChangeListener(listener);
    }

    public void setYUVBuffer( ByteBuffer bb_y, ByteBuffer bb_u, ByteBuffer bb_v){
        this.bb_y = bb_y;
        this.bb_u = bb_u;
        this.bb_v = bb_v;
    }

    public boolean updateYUVImage(){
        return !(bb_y == null || mRenderMatrix.mFrameWidth == 0 || mRenderMatrix.mFrameHeight == 0);
    }

    public void setFrameSize(int width, int height){
        isPano = width > height;
        mRenderMatrix.setFrameSize(width, height);
    }

    public void setScreenSize(int width, int height){
        mRenderMatrix.setScreenSize(width, height);
        getPlayModel().init(width,height);
    }



    /**-----------对内---------*/
    //绘制流程：
    // 1.根据是否拼接确定viewPort大小
    // 2.根据预览类型更新matrix
    // 3.根据源类型调用具体object的draw
    // 4.如果是YUV则进行view善后

    // 1.根据是否拼接确定viewPort大小；

    private void setViewPortYUV(){
        getPlayModel().setViewPortYUV(isPano);
    }

    // 3.根据源类型调用具体object的draw；
    private void realDraw(){
        getPlayModel().update(mRenderMatrix);
        if (source == SOURCE_PICTURE){
            mPlayObjectFactory.getBitmapObject(mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL)
                    .draw(mRenderMatrix.mMVPMatrix,mRenderMatrix.mSTMatrix, 0);
        } else if (source == SOURCE_VIDEO){
            mPlayObjectFactory.getVideoObject(mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL)
                    .draw(mRenderMatrix.mMVPMatrix,mRenderMatrix.mSTMatrix, 0);
        } else if (source == SOURCE_YUV){
            mPlayObjectFactory.getYUVObject(mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL)
                    .draw(mRenderMatrix.mMVPMatrix, bb_y, bb_u, bb_v, mRenderMatrix.mFrameWidth, mRenderMatrix.mFrameHeight);
        }
    }

    // 4.view善后
    private void releaseViewYUV(){
        getPlayModel().releaseViewPortYUV(isPano);
    }

    //管理渲染Model（RenderMode）
    public AbstractRenderModel getPlayModel(){
        if (mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_SPHERE){
            return mPlayModelFactory.getSphereRender();
        }
        if (mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_PLANET){
            return mPlayModelFactory.getPlanetRender();
        }
        if (mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_VR){
            return mPlayModelFactory.getVRRender();
        }
        if (mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_SPHERE_OUTSIDE){
            return mPlayModelFactory.getSphereOutRender();
        }
        if (mRenderMatrix.mRenderMode == VrConstant.RENDER_MODE_OVERALL){
            return mPlayModelFactory.getOverAllRender();
        }
        return mPlayModelFactory.getSphereRender();
    }

    protected PlayModelFactory mPlayModelFactory = new PlayModelFactory();

    class PlayModelFactory {
        AbstractRenderModel renderSphere;
        AbstractRenderModel renderPlanet;
        AbstractRenderModel renderVR;
        AbstractRenderModel renderSphereOut;
        AbstractRenderModel renderOverAll;

        void clear(){
            renderSphere = null;
            renderPlanet = null;
            renderSphereOut = null;
            renderVR = null;
            renderOverAll = null;
        }

        private AbstractRenderModel getSphereRender(){
            if (renderSphere == null){
                renderSphere = new RenderModelSphere();
            }
            return renderSphere;
        }

        private AbstractRenderModel getPlanetRender(){
            if (renderPlanet == null){
                renderPlanet = new RenderModelPlanet();
            }
            return renderPlanet;
        }

        private AbstractRenderModel getVRRender(){
            if (renderVR == null){
                renderVR = new RenderModelVR();
            }
            return renderVR;
        }

        private AbstractRenderModel getSphereOutRender(){
            if (renderSphereOut == null){
                renderSphereOut = new RenderModelSphereOut();
            }
            return renderSphereOut;
        }

        private AbstractRenderModel getOverAllRender(){
            if (renderOverAll == null){
//                renderOverAll = new RenderModelOverall();
                renderOverAll = new RenderModelOverallMatrix();
            }
            return renderOverAll;
        }
    }


    //管理渲染object
    public void initBitmapObject(boolean isPano, Bitmap bitmap){
        this.isPano = isPano;
        if (isPano){
            mPlayObjectFactory.objectBitmapSphere = new PanoBitmapSphere(VrConstant.FULL_VIDEO_WIDTH,
                    VrConstant.FULL_VIDEO_HEIGHT, bitmap);
            //mPlayObjectFactory.objectBitmapRect = new PanoBitmapRectAngle(bitmap);
            mPlayObjectFactory.objectBitmapRect = new PanoBitmapRectMatrix(VrConstant.FULL_VIDEO_WIDTH,
                    VrConstant.FULL_VIDEO_HEIGHT,bitmap);
        } else {
            mPlayObjectFactory.objectBitmapSphere = new DualBitmapSphere(bitmap, params);
           // mPlayObjectFactory.objectBitmapRect = new DualBitmapRectAngle(bitmap, params);
            mPlayObjectFactory.objectBitmapRect = new DualBitmapRectMatrix(VrConstant.FULL_VIDEO_WIDTH,
                    VrConstant.FULL_VIDEO_HEIGHT,bitmap,params);
        }
    }

    public void initVideoObject(boolean isPano, int mVideoType, int sphereW, int sphereH){
        this.isPano = isPano;
        if(mPlayObjectFactory.objectVideoSphere == null) {
            if (mVideoType == VrConstant.SOURCE_FILE_TYPE_DUAL) {
                mPlayObjectFactory.objectVideoSphere = new DualVideoSphere(params);
            } else if (mVideoType == VrConstant.SOURCE_FILE_TYPE_PANO) {
                mPlayObjectFactory.objectVideoSphere = new PanoVideoSphere(sphereW, sphereH);
            } else if (mVideoType == VrConstant.SOURCE_FILE_TYPE_STERE) {
                mPlayObjectFactory.objectVideoSphere = new StereoPanoVideoSphere(sphereW, sphereH);
            }
        }
        if(mPlayObjectFactory.objectVideoRect == null){
            if (isPano){
                //mPlayObjectFactory.objectVideoRect = new PanoVideoRectAngle();
                mPlayObjectFactory.objectVideoRect = new PanoVideoRectMatrix(VrConstant.FULL_VIDEO_WIDTH,
                                                            VrConstant.FULL_VIDEO_HEIGHT);
            } else {
               // mPlayObjectFactory.objectVideoRect = new DualVideoRectAngle(params);
                mPlayObjectFactory.objectVideoRect = new DualVideoRectMatrix(sphereW,
                        sphereH,params);
            }
        }
    }

    public XYVRBaseObject getVideoObject(boolean overAll){
        return mPlayObjectFactory.getVideoObject(overAll);
    }

    public XYVRBaseObject getBitmapObject(boolean overAll){
        return mPlayObjectFactory.getBitmapObject(overAll);
    }

    public PlayObjectFactory mPlayObjectFactory = new PlayObjectFactory();

    public XYVRBaseObject mCurrentYUVObject;

    class PlayObjectFactory {
        private XYVRBaseObject objectBitmapSphere;
        private XYVRBaseObject objectBitmapRect;
        private XYVRBaseObject objectVideoSphere;
        private XYVRBaseObject objectVideoRect;

        private DualYUVSphere dualYUVSphere;
        private DualYUVRectAngle dualYUVRectAngle;
        private PanoYUVSphere panoYUVSphere;
        private PanoYUVRectAngle panoYUVRectAngle;

        void clear(){
            objectBitmapSphere = null;
            objectBitmapRect = null;
            objectVideoSphere = null;
            objectVideoRect = null;

            dualYUVSphere = null;
            dualYUVRectAngle = null;
            panoYUVSphere = null;
            panoYUVRectAngle = null;
        }

        public XYVRBaseObject getBitmapObject(boolean overAll){
            if (overAll){
                return objectBitmapRect;
            }
            return objectBitmapSphere;
        }

        public XYVRBaseObject getVideoObject(boolean overAll){
            if (overAll){
                return objectVideoRect;
            }
            return objectVideoSphere;
        }


        private DualYUVSphere getDualYUVSphere(){
            if(dualYUVSphere == null){
                dualYUVSphere = new DualYUVSphere(params);
            }
            return dualYUVSphere;
        }

        private DualYUVRectAngle getDualYUVRectAngle(){
            if(dualYUVRectAngle == null){
                dualYUVRectAngle = new DualYUVRectAngle(params);
            }
            return dualYUVRectAngle;
        }

        private PanoYUVSphere getPanoYUVSphere(){
            if(panoYUVSphere == null){
                panoYUVSphere = new PanoYUVSphere(VrConstant.FULL_VIDEO_WIDTH,VrConstant.FULL_VIDEO_HEIGHT);
            }
            return panoYUVSphere;
        }

        private PanoYUVRectAngle getPanoYUVRectAngle(){
            if(panoYUVRectAngle == null){
                panoYUVRectAngle = new PanoYUVRectAngle(VrConstant.FULL_VIDEO_WIDTH,VrConstant.FULL_VIDEO_HEIGHT);
            }
            return panoYUVRectAngle;
        }

        public XYVRBaseObject getYUVObject(boolean overAll){
            if (isPano){
                if (overAll){
                    if (mCurrentYUVObject == null || !(mCurrentYUVObject instanceof PanoYUVRectAngle)){
                        mCurrentYUVObject = getPanoYUVRectAngle();
                    }
                } else if (mCurrentYUVObject == null || !(mCurrentYUVObject instanceof PanoYUVSphere)){
                    mCurrentYUVObject = getPanoYUVSphere();
                }
            } else {
                if (overAll){
                    if (mCurrentYUVObject == null || !(mCurrentYUVObject instanceof DualYUVRectAngle)){
                        mCurrentYUVObject = getDualYUVRectAngle();
                    }
                } else if (mCurrentYUVObject == null || !(mCurrentYUVObject instanceof DualYUVSphere)){
                    mCurrentYUVObject = getDualYUVSphere();
                }
            }
            return mCurrentYUVObject;
        }
    }

}
