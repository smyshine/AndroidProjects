
package com.xiaomi.fastvideo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.renderscript.Matrix4f;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.nio.IntBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PhotoView extends GLSurfaceView {
    public static final String TAG = "PhotoView";
    private final PhotoRenderer renderer;
    public int mMaxTextureSize;
    int mWidth;
    int mHeight;
    float mScale = 1.0f;
    boolean mIsFinger = false;
    float mTargeScaleOffset = 0.0f;
    float mStartScale;
    float mOffsetX = 0;
    float mOffsetY = 0;
    int mMaxOffsetX;
    int mMaxOffsetY;
    float mMiniScale;
    long mAnimaStartTime;
    long mAnimaTime = 400;
    volatile boolean mIsResume = false;

    volatile boolean isInitial = false;

    Photo firstPhoto;
    Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public static interface PhotoSnapCallback {
        void onSnap(Bitmap bitmap);
    }

    public void snap(PhotoSnapCallback callback) {
        renderer.snap(callback);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!supportsOpenGLES2(context)){
            throw new RuntimeException("not support gles 2.0");
        }
        renderer = new PhotoRenderer();
        setEGLContextClientVersion(2);

        setEGLConfigChooser(new CustomChooseConfig.ComponentSizeChooser(8, 8, 8, 8, 0, 0));
//        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);


        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private boolean supportsOpenGLES2(final Context context) {
        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    public interface OnScreenWindowChangedListener {
        void onScreenWindowChanged(boolean isFinger, int width, int height, int x1, int y1, int x2, int y2);
    }

    private OnScreenWindowChangedListener onScreenWindowChangedListener = null;

    public void setOnScreenWindowChangedListener(OnScreenWindowChangedListener listener){
        onScreenWindowChangedListener = listener;
    }


    public void reset() {
        mScale = 1.0f;
        mIsFinger = false;
        mStartScale = mScale;
        mOffsetX = 0;
        mOffsetY = 0;
        mTargeScaleOffset = 0;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale, boolean animal) {
//        Log.d(TAG, "setScale, scale:" + scale + ", animal:" + animal);
        if (animal) {
            mTargeScaleOffset = scale - mScale;
            mStartScale = mScale;
            mAnimaStartTime = System.currentTimeMillis();
        } else {
            mScale = scale;
            mStartScale = mScale;
            mTargeScaleOffset = 0;
        }
        if(scale > 1.0){
            mIsFinger = true;
        }else{
            mIsFinger = false;
        }
    }

    public float getMiniScale() {
        return mMiniScale;
    }

    public void move(float x, float y, boolean isFinger) {
//        Log.d(TAG, "move, x:" + x + ", y:" + y + ", isFinger:" + isFinger);
        mOffsetX += x;
        mOffsetY += y;
        if(mScale > 1.0){
            mIsFinger = isFinger;
        }else{
            mIsFinger = false;
        }
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public int getPhotoWith() {
        return mWidth;
    }

    public int getPhotoHeight() {
        return mHeight;
    }

    public void queue(Runnable r) {
        renderer.queue.add(r);
        requestRender();
    }

    public void remove(Runnable runnable) {
        renderer.queue.remove(runnable);
    }

    public void flush() {
        renderer.queue.clear();
    }

    public void setPhoto(Photo photo) {
        renderer.setPhoto(photo);
        mWidth = photo.width();
        mHeight = photo.height();
    }

    public void setRenderMatrix(float[] matrix) {
        renderer.setRenderMatrix(matrix);
    }

    @Override
    public void onResume() {
        super.onResume();
        MiLog.d(TAG, "onResume");
        mIsResume = true;
        flush();
        queue(new Runnable() {
            @Override
            public void run() {
                if (!isInitial) {
                    isInitial = true;
                    initial();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        MiLog.d(TAG, "onPause");
        mIsResume = false;
        
        queueEvent(new Runnable() {
            @Override
            public void run() {
                flush();
                if(isInitial) {
                    release();
                    isInitial = false;
                }

            }
        });

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        MiLog.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        MiLog.d(TAG, "surfaceCreated");
        // queue(new Runnable() {
        // @Override
        // public void run() {
        // initial();
        // }
        // });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        MiLog.d(TAG, "surfaceDestroyed");

        // queue(new Runnable() {
        // @Override
        // public void run() {
        // release();
        // }
        // });
    }

    protected void initial() {
        MiLog.d(TAG, "initial");
    }

    protected void release() {
        MiLog.d(TAG, "release");
        renderer.release();
        if (firstPhoto != null) {
            firstPhoto.clear();
        }
    }

    public void drawFrame() {
       
    }

    public void setFirstBitmap(final Bitmap bitmap){
        queue(new Runnable() {
            @Override
            public void run() {
                firstPhoto = Photo.create(bitmap);
                setPhoto(firstPhoto);
                bitmap.recycle();
            }
        });

    }

    class PhotoRenderer implements Renderer {

        final Vector<Runnable> queue = new Vector<Runnable>();
        RendererUtils.RenderContext renderContext;
        Photo photo;
        int viewWidth;
        int viewHeight;
        int lastWidth, lastHeight, lastX1, lastY1, lastX2, lastY2;

        void setPhoto(Photo photo) {
            this.photo = photo;
        }

        void setRenderMatrix(float[] matrix) {
            RendererUtils.setRenderMatrix(renderContext, matrix);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
//            Log.d(TAG, "ondrawframe");
            Runnable r = null;
            synchronized (queue) {
                if (!queue.isEmpty()) {
                    r = queue.remove(0);
                }
            }
            if (r != null) {
                r.run();
            }
            if (!queue.isEmpty()) {
                requestRender();
            }
            if (mIsResume) {
                RendererUtils.renderBackground();
                drawFrame();
                if (photo != null) {
                    buildAnimal();
                    setRenderMatrix(photo.width(), photo.height());
                    RendererUtils.renderTexture(renderContext, photo.texture(),
                            viewWidth, viewHeight);
                }
            }
        }

        void buildAnimal() {
            long time = System.currentTimeMillis() - mAnimaStartTime;
            if (time > mAnimaTime) {
                mScale = mStartScale + mTargeScaleOffset;
                return;
            }

            float ratio = mInterpolator.getInterpolation((float) (time * 1.0 / mAnimaTime));
            mScale = mStartScale + ratio * mTargeScaleOffset;
            requestRender();

        }

        void setRenderMatrix(int srcWidth, int srcHeight) {

//            int srcWidth = photo.width();
//            int srcHeight = photo.height();

            Matrix4f matrix4f = new Matrix4f();
            float srcAspectRatio = ((float) srcWidth) / srcHeight;
            float dstAspectRatio = ((float) viewWidth) / viewHeight;
            float relativeAspectRatio = dstAspectRatio / srcAspectRatio;
            float ratioscale = 1.0f;
            float x, y;
            float xScale, yScale;
            if (relativeAspectRatio < 1.0f) {
                ratioscale = srcAspectRatio / dstAspectRatio;
                mMiniScale = relativeAspectRatio;

                mMaxOffsetX = (int) (viewWidth * ratioscale * mScale - viewWidth);
                mMaxOffsetY = (int) (viewHeight * mScale - viewHeight);
                if (mOffsetX < -mMaxOffsetX) {
                    mOffsetX = -mMaxOffsetX;
                }
                if (mOffsetX > mMaxOffsetX) {
                    mOffsetX = mMaxOffsetX;
                }
                if (mOffsetY < -mMaxOffsetY) {
                    mOffsetY = -mMaxOffsetY;
                }
                if (mOffsetY > mMaxOffsetY) {
                    mOffsetY = mMaxOffsetY;
                }

                xScale = ratioscale * mScale;
                yScale = mScale;
                matrix4f.scale(xScale, yScale, 0);
                x = mOffsetX / (viewWidth * xScale);
                y = mOffsetY / (viewHeight * yScale);
                if (mScale < 1.0) {
                    y = 0.0f;
                }
                matrix4f.translate(x, y, 0);
            } else {
                mMiniScale = 1.0f;
                ratioscale = relativeAspectRatio;

                mMaxOffsetX = (int) (viewWidth * mScale - viewWidth);
                mMaxOffsetY = (int) (viewHeight * ratioscale * mScale - viewHeight);
                if (mOffsetX < -mMaxOffsetX) {
                    mOffsetX = -mMaxOffsetX;
                }
                if (mOffsetX > mMaxOffsetX) {
                    mOffsetX = mMaxOffsetX;
                }
                if (mOffsetY < -mMaxOffsetY) {
                    mOffsetY = -mMaxOffsetY;
                }
                if (mOffsetY > mMaxOffsetY) {
                    mOffsetY = mMaxOffsetY;
                }

                xScale = mScale;
                yScale = ratioscale * mScale;
                matrix4f.scale(xScale, yScale, 0);
                x = mOffsetX / (viewWidth * xScale);
                y = mOffsetY / (viewHeight * yScale);
                matrix4f.translate(x, y, 0);
            }

            renderContext.mModelViewMat = matrix4f.getArray();


            // 计算看视频窗口在图像本身的矩形坐标
            int x1, y1, x2, y2;
            x1 = (int) ((1 - 1/xScale - x) * srcWidth/2);
            x2 = (int) (x1 + 1/xScale * srcWidth);
            y1 = (int) ((1/yScale - 1 - y) * srcHeight/2);
            y2 = (int) (y1 - 1/yScale * srcHeight);

            // 0<=x1<x2<=srcWidth; 0>=y1>y2>=(-srcHeight);
            if(x1 < 0) x1 = 0;
            if(x2 > srcWidth) x2 = srcWidth;
            if(y1 > 0) y1 = 0;
            if(y2 < (0 - srcHeight)) y2 = (0 - srcHeight);


            if(lastWidth != srcWidth || lastHeight != srcHeight
                    || lastX1 != x1 || lastY1 != y1 || lastX2 != x2 || lastY2 != y2){
                if(onScreenWindowChangedListener != null){
                    onScreenWindowChangedListener.onScreenWindowChanged(mIsFinger, srcWidth, srcHeight, x1, y1, x2, y2);
                }
                lastWidth = srcWidth;
                lastHeight = srcHeight;
                lastX1 = x1;
                lastY1 = y1;
                lastX2 = x2;
                lastY2 = y2;
            }


//            Log.d("change", "screen:" + viewWidth + "*" + viewHeight
//                    + ", picture:" + srcWidth + "*" + srcHeight
//                    + ", offset(x,y):(" + (int)mOffsetX + "," + (int)mOffsetY + ")"
//                    + ", (x,y):" + x + "," + y + ""
//                    + ", xScale:"+ xScale + ", yScale:" + yScale
//                    + ", (x1, x2, y1, y2):" + (int)x1 +","+ (int)x2 +"," + (int)y1 + "," + (int)y2);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            MiLog.d(TAG, "onSurfaceChanged");
            viewWidth = width;
            viewHeight = height;
            reset();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            MiLog.d(TAG, "onSurfaceCreated");
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            IntBuffer buffer = IntBuffer.allocate(1);
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, buffer);
            mMaxTextureSize = buffer.get(0);
            GLES20.glGetError();
            renderContext = RendererUtils.createProgram();
        }

        public void release() {
            RendererUtils.releaseRenderContext(renderContext);
        }

        public void snap(final PhotoSnapCallback callback) {
            if (callback == null)
                return;
            if (photo == null) {
                if (callback != null) {
                    callback.onSnap(null);
                }
                return;
            }
            queue(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = RendererUtils.saveTexture(photo.texture(), photo.width(),
                            photo.height());
                    if (callback != null) {
                        callback.onSnap(bitmap);
                    }
                }
            });

//            Log.d(TAG, "request render");
            requestRender();

        }
    }

}
