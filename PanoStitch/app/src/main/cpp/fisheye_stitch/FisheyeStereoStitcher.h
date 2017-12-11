/************************************************************************/
/* The stitcher has complete functionality, including:                  */
/* 1) calibrating, camera parameter optimization                        */
/* 2) warp table generating(full and sparse tables)                     */
/* 3) different warper(soft, opencl, opengl)                            */
/************************************************************************/
#pragma once
#ifndef _FISHEYE_STEREO_STITCHER_H
#define _FISHEYE_STEREO_STITCHER_H

#include "ImageWarper.h"
#include "FeatureBasedOptimization.h"

namespace YiPanorama {
namespace fisheyeStereo {

using namespace calibration;
using namespace warper;
using namespace util;

class fisheyeStereoStitcher
{
public:
    fisheyeStereoStitcher();
    ~fisheyeStereoStitcher();


    int init(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH);

    int updateFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams);
    int updateWarpers();
    int imageStitch(imageFrame fisheyeImage[2], imageFrame panoImage);  // warping


    int intAndExtCalibration(imageFrame fisheyeImage[2], int checkerNumH, int checkerNumV, int checkerSize, bool drawResults, char *filePathLeft, char *filePathRight);
    int getImageCenters(double centersL[2], double centersR[2]);
    int setImageCenters(double centersL[2], double centersR[2]);

    int dinit();

private:
    int setFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH); // metadata from picture/video

    int setWorkParams();    // fisheyePanoParamsCore and cameraMetadatas
    int setWarpers();       // check warp device and generate warp tables
    int setWorkMems();      // image and roi memories

    int clean();

    // params from metadata
    fisheyePanoParams mFisheyePanoParams;   // this struct only for initialize from metadata/default file
                                            // should not be used at any other places

    // params for internal works which actually are used when generating warp tables
    fisheyePanoParamsCore mFisheyePanoParamsCore;
    cameraMetadata mCameraMetadata[2];

    // for stitch
    ImageWarper mImageWarper[2];
    imageFrame warpedImage[2];
    imageFrame pStereoImage;   // just a pointer, no memories allocated for this
    imageRoi warpedImageRoi[2];

    // for intrinsic calibration
    intrinsicParamOptimizer mIntrinsicParamOptimizer;

    // for extrinsic calibration
    extrinsicParamRelativeCalculator mExtrinsicParamRelativeCalculator;

    unsigned char *pProjImgData;

};


}   // namespace fisheyeStereo
}   // namespace YiPanorama

#endif // !_FISHEYE_STEREO_STITCHER_H
