package com.cameraframe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.h264encoder.AvcEncoder;
import com.permissions.AfterPermissionGranted;
import com.permissions.EasyPermissions;
import com.tnpserver.TNPService;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import smy.com.cameraframe.R;


public class ServerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "ServerActivity";
    private TextView tvLog;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认前置或后置
    int framerate = 30;

    int biterate = 8500 * 1000;
    private int videoWidth, videoHeight;

    Intent tnpIntent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_server);

        tvLog = findViewById(R.id.log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.changeCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        initView();
        tnpIntent = new Intent(this, TNPService.class);
        startService(tnpIntent);

        SupportAvcCodec();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            getCamera(mCameraId);
            if (mCamera != null && mSurfaceHolder != null) {
                startPreview(mCamera, mSurfaceHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(tnpIntent);
    }

    @SuppressLint("NewApi")
    private boolean SupportAvcCodec(){
        for(int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--){
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

            String[] types = codecInfo.getSupportedTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].equalsIgnoreCase("video/avc")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % mCamera.getNumberOfCameras();
        getCamera(mCameraId);
        if (mSurfaceHolder != null) {
            startPreview(mCamera, mSurfaceHolder);
        }
    }

    @AfterPermissionGranted(EasyPermissions.REQUEST_PERMISSION_CAMERA)
    private void getCamera(int id)  {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            log("apply for camera permission");
            getCameraInternal(id);
        } else {
            EasyPermissions.requestPermissions(this, "with out this permission, app may not work normally",
                    EasyPermissions.REQUEST_PERMISSION_CAMERA, Manifest.permission.CAMERA);
        }
    }

    private void getCameraInternal(int id) {
        mCamera = Camera.open(id);
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    private void log(String msg) {
        Log.d(TAG, "smy: " + msg);
        tvLog.append("\n" + msg);
    }

    @AfterPermissionGranted(EasyPermissions.REQUEST_PERMISSION_CAMERA)
    private void startPreview(Camera camera, SurfaceHolder holder)  {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            log("apply for camera permission");
            startPreviewInternal(camera, holder);
        } else {
            EasyPermissions.requestPermissions(this, "with out this permission, app may not work normally",
                    EasyPermissions.REQUEST_PERMISSION_CAMERA, Manifest.permission.CAMERA);
        }
    }

    private void startPreviewInternal(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        avcCodec = new AvcEncoder(videoWidth, videoHeight, framerate, biterate);
        avcCodec.startEncoderThread();
    }

    private void setupCamera(Camera camera) {
        if (camera != null) {
            camera.setPreviewCallback(this);
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.size() > 0) {
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    //设置自动对焦
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    log("set continuous picture, thus auto focus");
                }
            }

            List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
            if (videoSizes != null && videoSizes.size() > 0) {
                Camera.Size size = getPropVideoSize(videoSizes, 720);
                videoWidth = size.width;
                videoHeight = size.height;
                log("set video width: " + videoWidth + ", height: " + videoHeight);
            }

            Camera.Size previewSize = getPropPreviewSize(parameters.getSupportedPreviewSizes(), videoWidth);
            parameters.setPreviewSize(previewSize.width, previewSize.height);

            Camera.Size pictureSize = getPropPictureSize(parameters.getSupportedPictureSizes(), videoWidth);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);

            List<Integer> formats = parameters.getSupportedPreviewFormats();
            for (Integer format : formats) {
                log("support preview format: " + format);
            }
            //一般支持格式 NV21和YV12
            parameters.setPreviewFormat(ImageFormat.NV21);


            camera.setParameters(parameters);
        }
    }

    /**
     * 获取所有支持的返回图片尺寸
     *
     * @param list
     * @param
     * @param minWidth
     * @return
     */
    public Camera.Size getPropPictureSize(List<Camera.Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的预览尺寸
     *
     * @param list
     * @param minWidth
     * @return
     */
    public Camera.Size getPropPreviewSize(List<Camera.Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    public Camera.Size getPropVideoSize(List<Camera.Size> list, int minHeight) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    private CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();

    //升序
    public class CameraAscendSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            avcCodec.stopThread();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private static int yuvQueueSize = 10;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<>(yuvQueueSize);

    private AvcEncoder avcCodec;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //在此拿到相机摄像头的每一帧数据
//        log(data.toString());
        putYUVData(data, data.length);
    }

    public void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }
}
