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
JNIEXPORT void JNICALL
Java_com_panostitch_Stitcher_imageStitch(JNIEnv *env, jobject instance, jstring src_,
                                             jstring dst_, jstring params_) {
    const char *src = env->GetStringUTFChars(src_, 0);
    const char *dst = env->GetStringUTFChars(dst_, 0);
    const char *par = env->GetStringUTFChars(params_, 0);

    LOGEE("image stitch start");

    fisheyePanoParams stParams;
    fisheyePanoStitcherComp stStitherComp;
    imageFrame fisheyeImages[2];
    imageFrame panoImage;

    LOGEE("image stitch read params");
    readFisheyePanoParamsFromFile(par, &stParams);

    int panoWidth = stParams.stFisheyePanoParamsCore.panoImgW;
    int panoHeight = stParams.stFisheyePanoParamsCore.panoImgH;

    LOGEE("image stitch init comp");
    stStitherComp.init(&stParams, normal, panoWidth, panoHeight);

    LOGEE("image stitch init frame");
    initImageFrame(&panoImage, panoWidth, panoHeight, PIXELCOLORSPACE_RGB);

    LOGEE("image stitch load fisheye pair");
    loadImageDataFisheyePair(src, fisheyeImages, overAndUnder);
    /*string ffimage1 = dst;
    ffimage1.substr(ffimage1.length() - 4);
    ffimage1 += "img1.jpg";
    saveRGBImage(ffimage1.c_str(), fisheyeImages[0]);*/


    LOGEE("image stitch do stitch");
    stStitherComp.imageStitch(fisheyeImages, panoImage);

    LOGEE("image stitch save images");
    //saveImage(dst, panoImage);

    saveRGBImage(dst, panoImage);
/*
    cv::Mat imageMat(panoImage.imageH, panoImage.imageW, CV_8UC3);
    memcpy(imageMat.data, panoImage.plane[0], sizeof(unsigned char) * panoImage.imageH * panoImage.imageW * 3);
    cvtColor(imageMat, imageMat, CV_BGR2RGB);
    imwrite(dst, imageMat);
*/

    LOGEE("image stitch dinit");
    dinitImageFrame(&fisheyeImages[0]);
    dinitImageFrame(&fisheyeImages[1]);
    dinitImageFrame(&panoImage);
    stStitherComp.dinit();

    env->ReleaseStringUTFChars(src_, src);
    env->ReleaseStringUTFChars(dst_, dst);
}
