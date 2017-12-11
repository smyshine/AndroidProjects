
#include "FisheyeStereoStitcher.h"
#include "MatrixVectors.h"
#include "ImageTailor.h"

#include "ImageIOConverter.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

namespace YiPanorama {
namespace fisheyeStereo {

#define SPARSE_STEP 40
#define PI_HALF_ANGLE   90.0
#define PI_ANGLE        180.0
#define PI_2_ANGLE      360.0
#define M_PI       3.14159265358979323846   // pi

    fisheyeStereoStitcher::fisheyeStereoStitcher()
    {
    }

    fisheyeStereoStitcher::~fisheyeStereoStitcher()
    {
    }

int fisheyeStereoStitcher::init(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH)
{
    setFisheyePanoParams(pFisheyePanoParams, panoW, panoH);
    setWorkParams();
    setWarpers();
    setWorkMems();
    return 0;
}

int fisheyeStereoStitcher::dinit()
{
    return 0;
}

int fisheyeStereoStitcher::updateFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams)
{// after the extrinsic parameters between 2 lenses are calculated, update them into the standard coordinate system of OpenGL
    
    // save to the fisheyePanoParam
    mCameraMetadata[0].setToFisheyePanoParams(&mFisheyePanoParams, 0);
    mCameraMetadata[1].setToFisheyePanoParams(&mFisheyePanoParams, 1);

    // save into the fisheyePanoParam
    memcpy(pFisheyePanoParams, &mFisheyePanoParams, sizeof(fisheyePanoParams));
    return 0;
}

int fisheyeStereoStitcher::updateWarpers()
{
    // only back cameras warper is modified
    mImageWarper[1].genWarperCam(&mCameraMetadata[1], mFisheyePanoParamsCore.sphereRadius);
    return 0;
}

int fisheyeStereoStitcher::getImageCenters(double centersL[2], double centersR[2])
{
    mCameraMetadata[0].getImageCenters(centersL);
    mCameraMetadata[1].getImageCenters(centersR);
    return 0;
}

int fisheyeStereoStitcher::setImageCenters(double centersL[2], double centersR[2])
{
    mCameraMetadata[0].setImageCenters(centersL);
    mCameraMetadata[1].setImageCenters(centersR);
    return 0;
}

int fisheyeStereoStitcher::setFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH)
{
    memcpy(&mFisheyePanoParams, pFisheyePanoParams, sizeof(fisheyePanoParams));
    mFisheyePanoParams.stFisheyePanoParamsCore.panoImgW = panoW;
    mFisheyePanoParams.stFisheyePanoParamsCore.panoImgH = panoH;
    return 0;
}

int fisheyeStereoStitcher::setWorkParams() 
{
    memcpy(&mFisheyePanoParamsCore, &mFisheyePanoParams.stFisheyePanoParamsCore, sizeof(fisheyePanoParamsCore));
    mCameraMetadata[0].setFromFisheyePanoParams(&mFisheyePanoParams, 0);
    mCameraMetadata[1].setFromFisheyePanoParams(&mFisheyePanoParams, 1);
    return 0;
}

int fisheyeStereoStitcher::setWarpers()
{
    // warp table choice of computing complex
    int projImgW, projImgH, stepX, stepY;
    float vertAngleUp, vertAngleDown, horiAngleLeft, horiAngleRight;
    float /*desiredAngle = 100.0, */anglesPerStep;

    projImgW = mFisheyePanoParamsCore.panoImgW;
    projImgH = mFisheyePanoParamsCore.panoImgH;
    stepX = SPARSE_STEP;
    stepY = SPARSE_STEP;

    vertAngleUp = 0.0;
    vertAngleDown = PI_ANGLE;
    horiAngleLeft = 0.0;
    horiAngleRight = PI_2_ANGLE;

    mImageWarper[0].init(projImgW, projImgH, vertAngleUp, vertAngleDown, horiAngleLeft, horiAngleRight, true, stepX, stepY, false);
    mImageWarper[1].init(projImgW, projImgH, vertAngleUp, vertAngleDown, horiAngleLeft, horiAngleRight, true, stepX, stepY, false);

    mImageWarper[0].setWarpDevice(useSoftware);
    mImageWarper[1].setWarpDevice(useSoftware);

    // generate warp tables
    mImageWarper[0].genWarperCam(&mCameraMetadata[0], mFisheyePanoParamsCore.sphereRadius);
    mImageWarper[1].genWarperCam(&mCameraMetadata[1], mFisheyePanoParamsCore.sphereRadius);

    return 0;
}

int fisheyeStereoStitcher::setWorkMems()
{
    // warp image memories ------------------------------------------------------
    int singleChnProjImgSizeA = mImageWarper[0].mWarpImageW * mImageWarper[0].mWarpImageH;
    int singleChnProjImgSizeQuarA = singleChnProjImgSizeA / 4;
    int singleChnProjImgSizeB = mImageWarper[1].mWarpImageW * mImageWarper[1].mWarpImageH;
    int singleChnProjImgSizeQuarB = singleChnProjImgSizeB / 4;
    pProjImgData = new unsigned char[singleChnProjImgSizeQuarA * 6 + singleChnProjImgSizeQuarB * 6];// for 2 projected images in YUV420
    memset(pProjImgData, 128, singleChnProjImgSizeQuarA * 6 + singleChnProjImgSizeQuarB * 6);
    warpedImage[0].imageW = mImageWarper[0].mWarpImageW;
    warpedImage[0].imageH = mImageWarper[0].mWarpImageH;
    warpedImage[0].pxlColorFormat = PIXELCOLORSPACE_YUV420PYV;
    warpedImage[0].strides[0] = warpedImage[0].imageW;
    warpedImage[0].strides[1] = warpedImage[0].imageW / 2;
    warpedImage[0].strides[2] = warpedImage[0].imageW / 2;

    warpedImage[1].imageW = mImageWarper[1].mWarpImageW;
    warpedImage[1].imageH = mImageWarper[1].mWarpImageH;
    warpedImage[1].pxlColorFormat = PIXELCOLORSPACE_YUV420PYV;
    warpedImage[1].strides[0] = warpedImage[1].imageW;
    warpedImage[1].strides[1] = warpedImage[1].imageW / 2;
    warpedImage[1].strides[2] = warpedImage[1].imageW / 2;

    pStereoImage.imageW = mFisheyePanoParamsCore.panoImgW;
    pStereoImage.imageH = mFisheyePanoParamsCore.panoImgH * 2;
    pStereoImage.pxlColorFormat = PIXELCOLORSPACE_YUV420PYV;
    pStereoImage.strides[0] = pStereoImage.imageW;
    pStereoImage.strides[1] = pStereoImage.imageW / 2;
    pStereoImage.strides[2] = pStereoImage.imageW / 2;

    warpedImage[0].plane[0] = pProjImgData;
    warpedImage[0].plane[1] = warpedImage[0].plane[0] + singleChnProjImgSizeA;
    warpedImage[0].plane[2] = warpedImage[0].plane[1] + singleChnProjImgSizeQuarA;
    warpedImage[1].plane[0] = warpedImage[0].plane[2] + singleChnProjImgSizeQuarA;
    warpedImage[1].plane[1] = warpedImage[1].plane[0] + singleChnProjImgSizeB;
    warpedImage[1].plane[2] = warpedImage[1].plane[1] + singleChnProjImgSizeQuarB;

    pStereoImage.plane[0] = pProjImgData;
    pStereoImage.plane[1] = pStereoImage.plane[0] + mFisheyePanoParamsCore.panoImgW * mFisheyePanoParamsCore.panoImgH * 2;
    pStereoImage.plane[2] = pStereoImage.plane[1] + mFisheyePanoParamsCore.panoImgW * mFisheyePanoParamsCore.panoImgH / 2;

    warpedImageRoi[0].imgW = pStereoImage.imageW;
    warpedImageRoi[0].imgH = pStereoImage.imageH;
    warpedImageRoi[0].roiW = mFisheyePanoParamsCore.panoImgW;
    warpedImageRoi[0].roiH = mFisheyePanoParamsCore.panoImgH;
    warpedImageRoi[0].roiX = 0;
    warpedImageRoi[0].roiY = 0;
    warpedImageRoi[1].imgW = pStereoImage.imageW;
    warpedImageRoi[1].imgH = pStereoImage.imageH;
    warpedImageRoi[1].roiW = mFisheyePanoParamsCore.panoImgW;
    warpedImageRoi[1].roiH = mFisheyePanoParamsCore.panoImgH;
    warpedImageRoi[1].roiX = 0;
    warpedImageRoi[1].roiY = mFisheyePanoParamsCore.panoImgH;

    return 0;
}

int fisheyeStereoStitcher::clean()
{
    // clean warpers
    mImageWarper[0].dinit();
    mImageWarper[1].dinit();

    // clean work mems
    if (pProjImgData != NULL)
        delete[] pProjImgData;

    return 0;
}

int fisheyeStereoStitcher::imageStitch(imageFrame fisheyeImage[2], imageFrame panoImage)  // warping
{
    // image warping
    mImageWarper[0].warpImage(fisheyeImage[0], warpedImage[0]);
    mImageWarper[1].warpImage(fisheyeImage[1], warpedImage[1]);

    roiMerge(panoImage, warpedImage[0], warpedImageRoi[0]);
    roiMerge(panoImage, warpedImage[1], warpedImageRoi[1]);

    return 0;
}

int fisheyeStereoStitcher::intAndExtCalibration(imageFrame fisheyeImage[2], int checkerNumH, int checkerNumV, int checkerSize, bool drawResults, char *filePathLeft, char *filePathRight)
{
    int iResult = 0;

    // 1. intrinsic parameter(optical center) calibration ==========================
    double centers[2];

    // find the chessboard corners of the left camera ---------------
    iResult = mIntrinsicParamOptimizer.findChessboardCorners(fisheyeImage[0], checkerNumH, checkerNumV, checkerSize);
    if (iResult == 0)
    {
        iResult = mIntrinsicParamOptimizer.optimizeOptcCen(&mCameraMetadata[0]);
        mCameraMetadata[0].getImageCenters(centers);
        if (drawResults)
        {// draw corners and center
            mIntrinsicParamOptimizer.drawCorners(filePathLeft, fisheyeImage[0], centers);
        }
    }

    // find the chessboard corners of the right camera ---------------
    iResult = mIntrinsicParamOptimizer.findChessboardCorners(fisheyeImage[1], checkerNumH, checkerNumV, checkerSize);
    if (iResult == 0)
    {
        iResult = mIntrinsicParamOptimizer.optimizeOptcCen(&mCameraMetadata[1]);
        mCameraMetadata[1].getImageCenters(centers);
        if (drawResults)
        {// draw corners and center
            mIntrinsicParamOptimizer.drawCorners(filePathRight, fisheyeImage[1], centers);
        }
    }

    // 2. extrinsic parameter calculation using both cameras' extrinsic parameters against the chessboard
    mExtrinsicParamRelativeCalculator.relativeExtParamCalc(mCameraMetadata);

    return iResult;
}

}   // namespace fisheyeStereo
}   // namespace YiPanorama
