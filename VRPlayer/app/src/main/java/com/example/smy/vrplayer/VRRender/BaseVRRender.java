package com.example.smy.vrplayer.VRRender;

import android.content.Context;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.vr.renderer.VRRenderer;

/**
 * Created by SMY on 2016/6/22.
 */
public class BaseVRRender extends VRRenderer {

    public boolean mUseSensor = true;
    public Sphere mSphere;
    public Plane mPlane;

    private float mGestureXAngle = 0;
    private float mGestureYAngle = 0;

    private boolean mIsResetOrientation;

    private Matrix4 mTmpMatrix;
    private Quaternion mTmpOrientation;

    private final double INIT_FIELD_VIEW = 75.0;

    public BaseVRRender(Context context) {
        super(context);
        mCurrentEyeMatrix = new Matrix4();
        mTmpMatrix = new Matrix4();
        mTmpOrientation = new Quaternion();
        mCameraPosition = new Vector3();
        mCurrentEyeOrientation = new Quaternion();
    }

    @Override
    protected void initScene() {

    }

    protected void initScene(Material material){
        if(mSphere == null) {
            mSphere = new Sphere(100, 128, 64);
            //相机初始位置设置为旋转90度，即指向视频中央。
            mSphere.setRotY(90.0);
            mSphere.setScaleX(-1);
        }
        if(material != null) {
            mSphere.setMaterial(material);
        }
        getCurrentScene().addChild(mSphere);
        getCurrentCamera().setFieldOfView(INIT_FIELD_VIEW);

        getCurrentCamera().setPosition(Vector3.ZERO);
    }

    public void initScene(Texture texture){
        mPlane = createSettingPlane(texture);//new Texture("playBtn", R.drawable.play)
        getCurrentScene().addChild(mPlane);
    }

    private static Sphere createPhotoSphereWithTexture(ATexture texture) {

        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }

    private static Plane createSettingPlane(ATexture texture){
        Material playBtnM = new Material();
        playBtnM.setColorInfluence(0);
        try {
            //place the button picture in "res/drawable-nodpi/"
            playBtnM.addTexture(texture);
        } catch(ATexture.TextureException e) {
            e.printStackTrace();
        }

        Plane playBtn;
        playBtn = new Plane(2, 0.4f, 8, 1);
        //playBtn.setRotZ(90.0);
        playBtn.setScale(-1);
        playBtn.setMaterial(playBtnM);
        playBtn.setPosition(1, -2, -2);
        playBtn.setAlpha(0);

        // getCurrentScene().addChild(playBtn);
        return playBtn;
    }

    @Override
    public void onDrawEye(Eye eye) {
        /*
        getCurrentCamera().updatePerspective(
                eye.getFov().getLeft(),
                eye.getFov().getRight(),
                eye.getFov().getBottom(),
                eye.getFov().getTop());*/

        // 左右滑动时camera绕世界坐标的Y轴旋转， 上下滑动时camera绕自身的X轴旋转
        mCurrentEyeMatrix.identity();

        if(mUseSensor) {
            mCurrentEyeMatrix.multiply(mTmpMatrix.setAll(eye.getEyeView()));
        } else {
            mCurrentEyeMatrix.multiply(mTmpMatrix.setAll(mTmpOrientation.fromAngleAxis(Vector3.X, mGestureXAngle)));
            mCurrentEyeMatrix.multiply(mTmpMatrix.setAll(mTmpOrientation.fromAngleAxis(Vector3.Y, mGestureYAngle)));
        }
        mCurrentEyeOrientation.fromMatrix(mCurrentEyeMatrix);
        if(mIsResetOrientation){
            Quaternion q = new Quaternion();
            q.fromMatrix(mCurrentEyeMatrix);
            getCurrentCamera().setOrientation(q.inverse());
            mIsResetOrientation = false;
        }
        getCurrentCamera().setCameraOrientation(mCurrentEyeOrientation);
        //getCurrentCamera().setOrientation(mCurrentEyeOrientation);
        //getCurrentCamera().setPosition(mCameraPosition);
        // getCurrentCamera().getPosition().add(mCurrentEyeMatrix.getTranslation().inverse());
        super.onRenderFrame(null);
    }

    public void changeWatchType(){
        mUseSensor = !mUseSensor;
        resetCameraOrientation();
    }

    public void resetCameraOrientation() {
        mIsResetOrientation = true;
        getCurrentCamera().setFieldOfView(INIT_FIELD_VIEW);
        mGestureXAngle = 0;
        mGestureYAngle = 0;
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    public void addGestureRotateAngle(float rotateXAngle, float rotateYAngle){
        if(mUseSensor){
            return;
        }
        mGestureXAngle += rotateXAngle;
        mGestureYAngle += rotateYAngle;

        if(mGestureXAngle >= 360){
            mGestureXAngle -= 360;
        }

        else if(mGestureXAngle <= -360){
            mGestureXAngle += 360;
        }

        if(mGestureYAngle >= 360){
            mGestureYAngle -= 360;
        }
        else if(mGestureYAngle <= -360){
            mGestureYAngle += 360;
        }
    }

    public void onZoomGesture(float newDist, float oldDist){
        double fieldOfView = getCurrentCamera().getFieldOfView();
        if (newDist - oldDist > 0) {
            if (fieldOfView > 20) {
                fieldOfView = fieldOfView - (newDist - oldDist) / 10;
                getCurrentCamera().setFieldOfView(fieldOfView);
            }
        } else if (oldDist - newDist > 0) {
            if (fieldOfView < 130) {
                fieldOfView = fieldOfView + (oldDist - newDist) / 10;
                getCurrentCamera().setFieldOfView(fieldOfView);
            }
        }
    }
}
