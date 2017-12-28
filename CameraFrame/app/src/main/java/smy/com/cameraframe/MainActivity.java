package smy.com.cameraframe;

import android.hardware.Camera;
import android.media.VolumeShaper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "MainActivity";

    private TextView tvLog;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认前置或后置
    private int videoWidth, videoHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.log);
        tvLog.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.changeCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
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

    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % mCamera.getNumberOfCameras();
        mCamera = getCamera(mCameraId);
        if (mSurfaceHolder != null) {
            startPreview(mCamera, mSurfaceHolder);
        }
    }

    private Camera getCamera(int id) {
        return Camera.open(id);
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    private void log(String msg) {
        tvLog.append("\n" + msg);
    }


    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
       //在此拿到相机摄像头的每一帧数据
//        log(data.toString());

    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
