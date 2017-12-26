package com.panostitch;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by SMY on 2017/12/26.
 */

public class CombineParams {

    private int version;
    private final int POL_LENGTH = 5;
    private final int INVPOL_LENGTH = 15;
    public float front_pol[];
    public float back_pol[];
    public float front_invpol[];
    public float back_invpol[];
    public float front_camera_center[] = new float[2];
    public float back_camera_center[] = new float[2];
    public float front_affine_param[] = new float[3];
    public float back_affine_param[] = new float[3];
    public float front_camera_rotation[] = new float[9];
    public float back_camera_rotation[] = new float[9];
    public float front_camera_translation[] = new float[3];
    public float back_camera_translation[] = new float[3];
    public int fish_eye_width = 0;
    public int fish_eye_height = 0;
    public int out_width = 0;
    public int out_height = 0;
    public int sphere_radius = 0;
    public int step = 0;
    public float front_vcf_factors[] = new float[9];
    public float back_vcf_factors[] = new float[9];

    public CombineParams(int out_width, int out_height, int step, String panoParams,
                         String frontOcamModel, String backOcamModel,String frontParams, String backParams) {
        this.out_width = out_width;
        this.out_height = out_height;
        this.step = step;
        initPanoParams(panoParams);
        initOcamModel(frontOcamModel, backOcamModel);
        initExtrinsicParams(frontParams, backParams);
    }

    //先读取视频里的拼接参数，若没有再使用设备返回的参数
    public CombineParams(int out_width, int out_height, int step, String panoParams, String frontOcamModel, String backOcamModel,
                         String frontParams, String backParams, byte[] paramBytes) {
        this.out_width = out_width;
        this.out_height = out_height;
        this.step = step;
        ByteBuffer buffer = ByteBuffer.wrap(paramBytes, 8, paramBytes.length - 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        version = buffer.getInt();
        buffer.getInt(); //预留位置
        fish_eye_width = buffer.getInt();
        if (fish_eye_width == 0) {
            initPanoParams(panoParams);
            initOcamModel(frontOcamModel, backOcamModel);
            initExtrinsicParams(frontParams, backParams);
        } else {
            if (version == 0) {
                readBufferByVersion0(buffer);
            } else {
                initPanoParams(panoParams);
                initOcamModel(frontOcamModel, backOcamModel);
                initExtrinsicParams(frontParams, backParams);
            }
        }
    }

    private void readBufferByVersion0(ByteBuffer buffer) {
        fish_eye_height = buffer.getInt();
        buffer.getInt(); //panoImgW
        buffer.getInt(); //panoImgH
        sphere_radius = buffer.getInt();
        buffer.getFloat(); //maxFovAngle

        //front ocamModel
        int polLength = buffer.getInt();
        if (buffer.position() % 8 != 0) {
            buffer.getInt();  //跳过4位让内存8位对齐
        }
        front_pol = new float[polLength];
        for (int i = 0; i < polLength; i++) {
            front_pol[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 5 - polLength; i++) { //跳过占位的字节
            buffer.getDouble();
        }
        int invpolLength = buffer.getInt();
        if (buffer.position() % 8 != 0) {
            buffer.getInt();  //跳过4位让内存8位对齐
        }
        front_invpol = new float[invpolLength];
        for (int i = 0; i < invpolLength; i++) {
            front_invpol[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 10 - invpolLength; i++) { //跳过占位的字节
            buffer.getDouble();
        }
        front_camera_center[0] = (float) (buffer.getDouble());
        front_camera_center[1] = (float) (buffer.getDouble());
        for (int i = 0; i < 3; i++) {
            front_affine_param[i] = (float) (buffer.getDouble());
        }
        buffer.getInt(); //image width
        buffer.getInt(); //image height
        for (int i = 0; i < 9; i++) {
            buffer.getDouble(); //vcf_factors
        }

        //back ocamModel
        int backPolLength = buffer.getInt();
        if (buffer.position() % 8 != 0) {
            buffer.getInt();  //跳过4位让内存8位对齐
        }
        back_pol = new float[backPolLength];
        for (int i = 0; i < backPolLength; i++) {
            back_pol[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 5 - backPolLength; i++) { //跳过占位的字节
            buffer.getDouble();
        }
        int backInvpolLength = buffer.getInt();
        if (buffer.position() % 8 != 0) {
            buffer.getInt();  //跳过4位让内存8位对齐
        }
        back_invpol = new float[backInvpolLength];
        for (int i = 0; i < backInvpolLength; i++) {
            back_invpol[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 10 - backInvpolLength; i++) { //跳过占位的字节
            buffer.getDouble();
        }
        back_camera_center[0] = (float) (buffer.getDouble());
        back_camera_center[1] = (float) (buffer.getDouble());
        for (int i = 0; i < 3; i++) {
            back_affine_param[i] = (float) (buffer.getDouble());
        }
        buffer.getInt(); //image width
        buffer.getInt(); //image height
        for (int i = 0; i < 9; i++) {
            buffer.getDouble(); //vcf_factors
        }

        //front extParam
        for (int i = 0; i < 9; i++) {
            front_camera_rotation[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 3; i++) {
            front_camera_translation[i] = (float) (buffer.getDouble());
        }

        //back extParam
        for (int i = 0; i < 9; i++) {
            back_camera_rotation[i] = (float) (buffer.getDouble());
        }
        for (int i = 0; i < 3; i++) {
            back_camera_translation[i] = (float) (buffer.getDouble());
        }
    }

    private void initPanoParams(String panoParams) {
        initDefaultPanoParams();
        if (!TextUtils.isEmpty(panoParams)) {
            try {
                Log.d("CombineParams", "panoParams = " + panoParams);
                JSONObject jsonObject = new JSONObject(panoParams);
                fish_eye_width = jsonObject.getInt("fisheyeImgW");
                fish_eye_height = jsonObject.getInt("fisheyeImgH");
                sphere_radius = jsonObject.getInt("sphereRadius");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initOcamModel(String frontOcam, String backOcam) {
        if (!TextUtils.isEmpty(frontOcam)) {
            try {
                Log.d("CombineParams", "frontOcam = " + frontOcam);
                JSONObject jsonObject = new JSONObject(frontOcam);
                String[] vcf_factors = jsonObject.optString("vcf_factors").split(",");
                String[] pols = jsonObject.optString("pol").split(",");
                String[] invpols = jsonObject.optString("invpol").split(",");
                if (vcf_factors.length != 0) {
                    for (int i = 0; i < vcf_factors.length; i++) {
                        if (!TextUtils.isEmpty(vcf_factors[i])) {
                            front_vcf_factors[i] = Float.valueOf(vcf_factors[i]);
                        }
                    }
                }
                if (pols.length == 0) {
                    initDefaultFrontPol();
                } else {
                    front_pol = new float[pols.length];
                    for (int i = 0; i < pols.length; i++) {
                        if (!TextUtils.isEmpty(pols[i])) {
                            front_pol[i] = Float.valueOf(pols[i]);
                        }
                    }
                }
                if (invpols.length == 0) {
                    initDefaultFrontInvpol();
                } else {
                    front_invpol = new float[invpols.length];
                    for (int i = 0; i < invpols.length; i++) {
                        if (!TextUtils.isEmpty(invpols[i])) {
                            front_invpol[i] = Float.valueOf(invpols[i]);
                        }
                    }
                }
                initDefalutFrontAffine();
                String affineC = jsonObject.optString("c");
                if (!TextUtils.isEmpty(affineC)) {
                    front_affine_param[0] = Float.valueOf(affineC);
                }
                String affineD = jsonObject.optString("d");
                if (!TextUtils.isEmpty(affineD)) {
                    front_affine_param[1] = Float.valueOf(affineD);
                }
                String affineE = jsonObject.optString("e");
                if (!TextUtils.isEmpty(affineE)) {
                    front_affine_param[2] = Float.valueOf(affineE);
                }
                String uc = jsonObject.optString("uc");
                if (!TextUtils.isEmpty(uc)) {
                    front_camera_center[0] = Float.valueOf(uc);
                }
                String vc = jsonObject.optString("vc");
                if (!TextUtils.isEmpty(vc)) {
                    front_camera_center[1] = Float.valueOf(vc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                initDefaultFrontPol();
                initDefaultFrontInvpol();
                initDefalutFrontAffine();
            } catch (NumberFormatException e) {
                initDefaultFrontPol();
                initDefaultFrontInvpol();
                initDefalutFrontAffine();
            }
        } else {
            initDefaultFrontPol();
            initDefaultFrontInvpol();
            initDefalutFrontAffine();
        }

        if (!TextUtils.isEmpty(backOcam)) {
            try {
                Log.d("CombineParams", "backOcam = " + backOcam);
                JSONObject jsonObject = new JSONObject(backOcam);
                String[] vcf_factors = jsonObject.optString("vcf_factors").split(",");
                String[] pols = jsonObject.optString("pol").split(",");
                String[] invpols = jsonObject.optString("invpol").split(",");
                if (vcf_factors.length != 0) {
                    for (int i = 0; i < vcf_factors.length; i++) {
                        if (!TextUtils.isEmpty(vcf_factors[i])) {
                            back_vcf_factors[i] = Float.valueOf(vcf_factors[i]);
                        }
                    }
                }
                if (pols.length == 0) {
                    initDefaultBackPol();
                } else {
                    back_pol = new float[pols.length];
                    for (int i = 0; i < pols.length; i++) {
                        if (!TextUtils.isEmpty(pols[i])) {
                            back_pol[i] = Float.valueOf(pols[i]);
                        }
                    }
                }
                if (invpols.length == 0) {
                    initDefaultBackInvpol();
                } else {
                    back_invpol = new float[invpols.length];
                    for (int i = 0; i < invpols.length; i++) {
                        if (!TextUtils.isEmpty(invpols[i])) {
                            back_invpol[i] = Float.valueOf(invpols[i]);
                        }
                    }
                }
                initDefalutBackAffine();
                String affineC = jsonObject.optString("c");
                if (!TextUtils.isEmpty(affineC)) {
                    back_affine_param[0] = Float.valueOf(affineC);
                }
                String affineD = jsonObject.optString("d");
                if (!TextUtils.isEmpty(affineD)) {
                    back_affine_param[1] = Float.valueOf(affineD);
                }
                String affineE = jsonObject.optString("e");
                if (!TextUtils.isEmpty(affineE)) {
                    back_affine_param[2] = Float.valueOf(affineE);
                }
                String uc = jsonObject.optString("uc");
                if (!TextUtils.isEmpty(uc)) {
                    back_camera_center[0] = Float.valueOf(uc);
                }
                String vc = jsonObject.optString("vc");
                if (!TextUtils.isEmpty(vc)) {
                    back_camera_center[1] = Float.valueOf(vc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                initDefaultBackPol();
                initDefaultBackInvpol();
                initDefalutBackAffine();
            } catch (NumberFormatException e) {
                initDefaultBackPol();
                initDefaultBackInvpol();
                initDefalutBackAffine();
            }
        } else {
            initDefaultBackPol();
            initDefaultBackInvpol();
            initDefalutBackAffine();
        }
    }

    private void initExtrinsicParams(String frontParams, String backParams) {
        initDefaultFrontParams();
        if (!TextUtils.isEmpty(frontParams)) {
            try {
                Log.d("CombineParams", "frontParams = " + frontParams);
                JSONObject jsonObject = new JSONObject(frontParams);
                String[] rotations = jsonObject.optString("rotationMtx").split(",");
                String[] translations = jsonObject.optString("translateVec").split(",");
                for (int i = 0; i < rotations.length; i++) {
                    if (!TextUtils.isEmpty(rotations[i])) {
                        front_camera_rotation[i] = Float.valueOf(rotations[i]);
                    }
                }
                for (int i = 0; i < translations.length; i++) {
                    if (!TextUtils.isEmpty(translations[i])) {
                        front_camera_translation[i] = Float.valueOf(translations[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        initDefaultBackParams();
        if (!TextUtils.isEmpty(backParams)) {
            try {
                Log.d("CombineParams", "backParams = " + backParams);
                JSONObject jsonObject = new JSONObject(backParams);
                String[] rotations = jsonObject.optString("rotationMtx").split(",");
                String[] translations = jsonObject.optString("translateVec").split(",");
                for (int i = 0; i < rotations.length; i++) {
                    if (!TextUtils.isEmpty(rotations[i])) {
                        back_camera_rotation[i] = Float.valueOf(rotations[i]);
                    }
                }
                for (int i = 0; i < translations.length; i++) {
                    if (!TextUtils.isEmpty(translations[i])) {
                        back_camera_translation[i] = Float.valueOf(translations[i]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initDefaultFrontParams() {
        front_camera_rotation[0] = 0.000000f;
        front_camera_rotation[1] = -1.000000f;
        front_camera_rotation[2] = 0.000000f;
        front_camera_rotation[3] = 1.000000f;
        front_camera_rotation[4] = 0.000000f;
        front_camera_rotation[5] = 0.000000f;
        front_camera_rotation[6] = 0.000000f;
        front_camera_rotation[7] = 0.000000f;
        front_camera_rotation[8] = 1.000000f;

        front_camera_translation[0] = 0.000000f;
        front_camera_translation[1] = 0.000000f;
        front_camera_translation[2] = 25.000000f;
    }

    private void initDefaultBackParams() {
        back_camera_rotation[0] = 0.003125f;
        back_camera_rotation[1] = -0.999938f;
        back_camera_rotation[2] = 0.010709f;
        back_camera_rotation[3] = -0.999995f;
        back_camera_rotation[4] = -0.003130f;
        back_camera_rotation[5] = -0.000425f;
        back_camera_rotation[6] = 0.000459f;
        back_camera_rotation[7] = -0.010707f;
        back_camera_rotation[8] = -0.999943f;

        back_camera_translation[0] = 2.110239f;
        back_camera_translation[1] = -5.548682f;
        back_camera_translation[2] = -29.119710f;
    }

    private void initDefaultFrontPol() {
        front_pol = new float[POL_LENGTH];
        front_pol[0] = -7.845495e+02f;
        front_pol[1] = 0.000000e+00f;
        front_pol[2] = 5.050000e-04f;
        front_pol[3] = 0.000000e+00f;
        front_pol[4] = 0.000000e+00f;
    }

    private void initDefaultBackPol() {
        back_pol = new float[POL_LENGTH];
        back_pol[0] = -7.845495e+02f;
        back_pol[1] = 0.000000e+00f;
        back_pol[2] = 5.050000e-04f;
        back_pol[3] = 0.000000e+00f;
        back_pol[4] = 0.000000e+00f;
    }

    private void initDefaultFrontInvpol() {
        front_invpol = new float[INVPOL_LENGTH];
        front_invpol[0] = 1.303116e+03f;
        front_invpol[1] = 7.881812e+02f;
        front_invpol[2] = -1.016078e+02f;
        front_invpol[3] = 9.726736e+01f;
        front_invpol[4] = 1.098825e+02f;
        front_invpol[5] = -8.687727e+01f;
        front_invpol[6] = 5.514366e+01f;
        front_invpol[7] = 1.181969e+02f;
        front_invpol[8] = -1.158368e+02f;
        front_invpol[9] = -9.709421e+01f;
        front_invpol[10] = 9.649237e+01f;
        front_invpol[11] = 7.792490e+01f;
        front_invpol[12] = -2.168770e+01f;
        front_invpol[13] = -2.973025e+01f;
        front_invpol[14] = -6.484474e+00f;
    }

    private void initDefaultBackInvpol() {
        back_invpol = new float[INVPOL_LENGTH];
        back_invpol[0] = 1.303116e+03f;
        back_invpol[1] = 7.881812e+02f;
        back_invpol[2] = -1.016078e+02f;
        back_invpol[3] = 9.726736e+01f;
        back_invpol[4] = 1.098825e+02f;
        back_invpol[5] = -8.687727e+01f;
        back_invpol[6] = 5.514366e+01f;
        back_invpol[7] = 1.181969e+02f;
        back_invpol[8] = -1.158368e+02f;
        back_invpol[9] = -9.709421e+01f;
        back_invpol[10] = 9.649237e+01f;
        back_invpol[11] = 7.792490e+01f;
        back_invpol[12] = -2.168770e+01f;
        back_invpol[13] = -2.973025e+01f;
        back_invpol[14] = -6.484474e+00f;
    }

    private void initDefalutFrontAffine() {
        front_affine_param[0] = 1.000051f;
        front_affine_param[1] = 0.000079f;
        front_affine_param[2] = -0.000201f;

        front_camera_center[0] = 1438.713782f;
        front_camera_center[1] = 1444.570518f;
    }

    private void initDefalutBackAffine() {
        back_affine_param[0] = 1.000051f;
        back_affine_param[1] = 0.000079f;
        back_affine_param[2] = -0.000201f;

        back_camera_center[0] = 1439.770007f;
        back_camera_center[1] = 1437.460547f;
    }

    private void initDefaultPanoParams() {
        fish_eye_width = 2880;
        fish_eye_height = 2880;
        sphere_radius = 1500;
    }

}
