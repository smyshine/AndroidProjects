#include <jni.h>
#include <string>
#include "fisheye_stitch/ImageIOConverter.h"
#include "fisheye_stitch/FisheyePanoStitcherComp.h"
#include "fisheye_stitch/FisheyePanoParams.h"


#include <android/log.h>

#define  LOG_TAGG    "libStitch"
#define  LOGEE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAGG,__VA_ARGS__)

using namespace std;

using namespace YiPanorama;
using namespace fisheyePano;

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_panostitch_Stitcher_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
void saveRGBImage(const char* path, imageFrame panoImage){
    cv::Mat imageMat(panoImage.imageH, panoImage.imageW, CV_8UC3);
    memcpy(imageMat.data, panoImage.plane[0], sizeof(unsigned char) * panoImage.imageH * panoImage.imageW * 3);
    cvtColor(imageMat, imageMat, CV_BGR2RGB);
    imwrite(path, imageMat);
}

extern "C"
void transJparamToCparam(JNIEnv * env, jobject jparam, YiPanorama::fisheyePanoParams *cparam){
    jclass jparamObject = env->GetObjectClass(jparam);
    if (jparamObject == NULL){
        LOGEE("get jara CombineParam class failed!");
        return;
    }

    //fisheye pano params core
    cparam->stFisheyePanoParamsCore.fisheyeImgW = env->GetIntField(
            jparam, env->GetFieldID(jparamObject, "fish_eye_width", "I"));
    LOGEE("fisheyeImgW: %d \n",cparam->stFisheyePanoParamsCore.fisheyeImgW);
    cparam->stFisheyePanoParamsCore.fisheyeImgH = env->GetIntField(
            jparam, env->GetFieldID(jparamObject, "fish_eye_height", "I"));
    LOGEE("fisheyeImgH: %d \n",cparam->stFisheyePanoParamsCore.fisheyeImgH);
    cparam->stFisheyePanoParamsCore.panoImgW = env->GetIntField(
            jparam, env->GetFieldID(jparamObject, "out_width", "I"));
    LOGEE("panoImgW: %d \n",cparam->stFisheyePanoParamsCore.panoImgW);
    cparam->stFisheyePanoParamsCore.panoImgH = env->GetIntField(
            jparam, env->GetFieldID(jparamObject, "out_height", "I"));
    LOGEE("panoImgH: %d \n",cparam->stFisheyePanoParamsCore.panoImgH);
    cparam->stFisheyePanoParamsCore.sphereRadius = env->GetIntField(
            jparam, env->GetFieldID(jparamObject, "sphere_radius", "I"));
    LOGEE("sphereRadius: %d \n",cparam->stFisheyePanoParamsCore.sphereRadius);
    cparam->stFisheyePanoParamsCore.maxFovAngle = 98.0f;
    LOGEE("maxFovAngle: %d \n",98);

    //ocam model, front eye
    LOGEE("front ocam \n");
    jfloatArray jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_pol", "[F"));
    jfloat *front_pol_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[0].length_pol = env->GetArrayLength(jarray);
    LOGEE("length_pol: %d \n",cparam->staOcamModels[0].length_pol);
    for (int i = 0; i < cparam->staOcamModels[0].length_pol; ++i){
        cparam->staOcamModels[0].pol[i] = front_pol_points[i];
        LOGEE(" %lf \n",front_pol_points[i]);
    }
    env->ReleaseFloatArrayElements(jarray, front_pol_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_invpol", "[F"));
    jfloat *front_invpol_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[0].length_invpol = env->GetArrayLength(jarray);
    LOGEE("length_invpol: %d \n",cparam->staOcamModels[0].length_invpol);
    for (int i = 0; i < cparam->staOcamModels[0].length_invpol; ++i){
        cparam->staOcamModels[0].invpol[i] = front_invpol_points[i];
        LOGEE(" %lf \n",front_invpol_points[i]);
    }
    env->ReleaseFloatArrayElements(jarray, front_invpol_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_vcf_factors", "[F"));
    jfloat *front_vcf = env->GetFloatArrayElements(jarray, NULL);
    int length_front_vcf = env->GetArrayLength(jarray);
    LOGEE("front_vcf: %d \n",length_front_vcf);
    for (int i = 0; i < length_front_vcf; ++i){
        cparam->staOcamModels[0].vcf_factors[i] = front_vcf[i];
        LOGEE(" %lf \n",front_vcf[i]);
    }
    env->ReleaseFloatArrayElements(jarray, front_vcf, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_camera_center", "[F"));
    jfloat *front_center_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[0].uc = front_center_points[0];
    cparam->staOcamModels[0].vc = front_center_points[1];
    LOGEE("uc: %lf ,vc:%lf \n",cparam->staOcamModels[0].uc,cparam->staOcamModels[0].vc);
    env->ReleaseFloatArrayElements(jarray, front_center_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_affine_param", "[F"));
    jfloat *front_affine_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[0].c = front_affine_points[0];
    cparam->staOcamModels[0].d = front_affine_points[1];
    cparam->staOcamModels[0].e = front_affine_points[2];
    LOGEE("c: %lf ,d:%lf ,e:%lf \n",cparam->staOcamModels[0].c,cparam->staOcamModels[0].d,cparam->staOcamModels[0].e);
    env->ReleaseFloatArrayElements(jarray, front_affine_points, 0);

    cparam->staOcamModels[0].width = cparam->stFisheyePanoParamsCore.fisheyeImgW;
    cparam->staOcamModels[0].height = cparam->stFisheyePanoParamsCore.fisheyeImgH;
    LOGEE("width: %d ,height: %d \n",cparam->staOcamModels[0].width,cparam->staOcamModels[0].height);

    //ocam model, back eye
    LOGEE("back ocam \n");
    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_pol", "[F"));
    jfloat *back_pol_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[1].length_pol = env->GetArrayLength(jarray);
    LOGEE("length_pol: %d \n",cparam->staOcamModels[1].length_pol);
    for (int i = 0; i < cparam->staOcamModels[1].length_pol; ++i){
        cparam->staOcamModels[1].pol[i] = back_pol_points[i];
        LOGEE(" %lf \n",back_pol_points[i]);
    }
    env->ReleaseFloatArrayElements(jarray, back_pol_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_invpol", "[F"));
    jfloat *back_invpol_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[1].length_invpol = env->GetArrayLength(jarray);
    LOGEE("length_invpol: %d \n",cparam->staOcamModels[1].length_invpol);
    for (int i = 0; i < cparam->staOcamModels[1].length_invpol; ++i){
        cparam->staOcamModels[1].invpol[i] = back_invpol_points[i];
        LOGEE(" %lf \n",back_invpol_points[i]);
    }
    env->ReleaseFloatArrayElements(jarray, back_invpol_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_vcf_factors", "[F"));
    jfloat *back_vcf = env->GetFloatArrayElements(jarray, NULL);
    int length_back_vcf = env->GetArrayLength(jarray);
    LOGEE("back_vcf: %d \n",length_back_vcf);
    for (int i = 0; i < length_back_vcf; ++i){
        cparam->staOcamModels[1].vcf_factors[i] = back_vcf[i];
        LOGEE(" %lf \n",back_vcf[i]);
    }
    env->ReleaseFloatArrayElements(jarray, back_vcf, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_camera_center", "[F"));
    jfloat *back_center_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[1].uc = back_center_points[0];
    cparam->staOcamModels[1].vc = back_center_points[1];
    LOGEE("uc: %lf ,vc:%lf \n",cparam->staOcamModels[1].uc,cparam->staOcamModels[1].vc);
    env->ReleaseFloatArrayElements(jarray, back_center_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_affine_param", "[F"));
    jfloat *back_affine_points = env->GetFloatArrayElements(jarray, NULL);
    cparam->staOcamModels[1].c = back_affine_points[0];
    cparam->staOcamModels[1].d = back_affine_points[1];
    cparam->staOcamModels[1].e = back_affine_points[2];
    LOGEE("c: %lf ,d:%lf ,e:%lf \n",cparam->staOcamModels[1].c,cparam->staOcamModels[1].d,cparam->staOcamModels[1].e);
    env->ReleaseFloatArrayElements(jarray, back_affine_points, 0);

    cparam->staOcamModels[1].width = cparam->stFisheyePanoParamsCore.fisheyeImgW;
    cparam->staOcamModels[1].height = cparam->stFisheyePanoParamsCore.fisheyeImgH;
    LOGEE("width: %d ,height: %d \n",cparam->staOcamModels[1].width,cparam->staOcamModels[1].height);

    //ext params, front
    LOGEE("front ext");
    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_camera_rotation", "[F"));
    jfloat *front_rotation_points = env->GetFloatArrayElements(jarray, NULL);
    int front_rotation_length = env->GetArrayLength(jarray);
    for (int i = 0; i < front_rotation_length; ++i){
        cparam->staExtParam[0].rotationMtx[i] = front_rotation_points[i];
        LOGEE("rotation %lf \n",cparam->staExtParam[0].rotationMtx[i]);
    }
    env->ReleaseFloatArrayElements(jarray, front_rotation_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "front_camera_translation", "[F"));
    jfloat *front_translate_points = env->GetFloatArrayElements(jarray, NULL);
    int front_translate_length = env->GetArrayLength(jarray);
    for (int i = 0; i < front_translate_length; ++i){
        cparam->staExtParam[0].translateVec[i] = front_translate_points[i];
        LOGEE("translate %lf \n",cparam->staExtParam[0].translateVec[i]);
    }
    env->ReleaseFloatArrayElements(jarray, front_translate_points, 0);

    //ext params, back
    LOGEE("back ext");
    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_camera_rotation", "[F"));
    jfloat *back_rotation_points = env->GetFloatArrayElements(jarray, NULL);
    int back_rotation_length = env->GetArrayLength(jarray);
    for (int i = 0; i < back_rotation_length; ++i){
        cparam->staExtParam[1].rotationMtx[i] = back_rotation_points[i];
        LOGEE("rotation %lf \n",cparam->staExtParam[1].rotationMtx[i]);
    }
    env->ReleaseFloatArrayElements(jarray, back_rotation_points, 0);

    jarray = (jfloatArray) env->GetObjectField(
            jparam, env->GetFieldID(jparamObject, "back_camera_translation", "[F"));
    jfloat *back_translate_points = env->GetFloatArrayElements(jarray, NULL);
    int back_translate_length = env->GetArrayLength(jarray);
    for (int i = 0; i < back_translate_length; ++i){
        cparam->staExtParam[1].translateVec[i] = back_translate_points[i];
        LOGEE("translate %lf \n",cparam->staExtParam[1].translateVec[i]);
    }
    env->ReleaseFloatArrayElements(jarray, back_translate_points, 0);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_panostitch_Stitcher_imageStitch(JNIEnv *env, jobject instance, jstring src_,
                                             jstring dst_, jobject params, jstring datPath_) {
    const char *src = env->GetStringUTFChars(src_, 0);
    const char *dst = env->GetStringUTFChars(dst_, 0);
    const char *datPath = env->GetStringUTFChars(datPath_, 0);

    LOGEE("image stitch start");

    fisheyePanoParams stParams;
    fisheyePanoStitcherComp stStitherComp;
    imageFrame fisheyeImages[2];
    imageFrame panoImage;

    LOGEE("image stitch transform params");
    transJparamToCparam(env, params, &stParams);

    int panoWidth = stParams.stFisheyePanoParamsCore.panoImgW;
    int panoHeight = stParams.stFisheyePanoParamsCore.panoImgH;

    LOGEE("image stitch init comp");
    stStitherComp.init(&stParams, normal, panoWidth, panoHeight, datPath);

    LOGEE("image stitch init frame");
    initImageFrame(&panoImage, panoWidth, panoHeight, PIXELCOLORSPACE_RGB);

    LOGEE("image stitch load fisheye pair");
    loadImageDataFisheyePair(src, fisheyeImages, overAndUnder);

    LOGEE("image stitch do stitch");
    stStitherComp.imageStitch(fisheyeImages, panoImage);

    LOGEE("image stitch save images");

    saveRGBImage(dst, panoImage);

    LOGEE("image stitch dinit");
    dinitImageFrame(&fisheyeImages[0]);
    dinitImageFrame(&fisheyeImages[1]);
    dinitImageFrame(&panoImage);
    stStitherComp.dinit();

    env->ReleaseStringUTFChars(src_, src);
    env->ReleaseStringUTFChars(dst_, dst);
    env->ReleaseStringUTFChars(datPath_, datPath);
}
