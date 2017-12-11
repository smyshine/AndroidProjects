/************************************************************************/
/* using pyramids to blend 2 images with same size                      */
/************************************************************************/
#pragma once
#ifndef _IMAGE_BLENDER_H
#define _IMAGE_BLENDER_H


#include "YiPanoramaTypes.h"

namespace YiPanorama {
namespace util {

#define MAX_GAUSS_KERNEL_WIDTH 100  // half window width + 1
#define BOX_KERNEL_NUM      3   //
#define MAX_PYRAMID_LEVELS  5

// structure definitions ========================================================
typedef float pyraDataType;
typedef float imageDataType;

enum kernelChoice
{
    gaukernel = 0,
    boxKernel
};

enum blenderSeamDirection
{
    horiz,
    verti
};

struct GaussianKernel
{// 1D gaussian kernel
    float sigma;           // sigma
    int halfWindowWidth;          // total kernel width is (2*half_window_w + 1)
    float gCoeffs[MAX_GAUSS_KERNEL_WIDTH];    //
};

struct BoxKernel
{
    float sigma;            // equivalent sigma to gaussian kernel
    int boxNum;             // box filter num, BOX_KERNEL_NUM
    int boxSizes[BOX_KERNEL_NUM];      // size of each box filter window width, all are odd numbers
};

struct PyramidInfo
{
    int levelNum;                 // pyramid levels
    int width[MAX_PYRAMID_LEVELS];  // size of each level, and the base level has the same size of the image
    int height[MAX_PYRAMID_LEVELS];
    int level_size[MAX_PYRAMID_LEVELS];
    int total_size;             // total size of the pyramid.
};

struct PyramidDataPtrs
{
    int levelNum;
    pyraDataType *levelPtr[MAX_PYRAMID_LEVELS];
};

class ImageBlender
{
public:
    ImageBlender();
    ~ImageBlender();

    int init(int sizeW, int sizeH);
//     int genMask(float sigma, blenderSeamDirection direction);
    int loadMask(char *filePath, int width, int height);    // single channel mask only

   /* int weightedSumByMask1Level(imageDataType *inImgLeft, imageDataType *inImgRight, imageDataType *mask, imageDataType *outImg, int mask_size);
    int weightedSumByMask1Level(unsigned char *inImgLeft, unsigned char *inImgRight, unsigned char *mask, unsigned char *outImg, int mask_size);

    int weightedSumByMask(imageFrame imgLeft, imageFrame imgRight, imageFrame outImg);

    int weightEdge(unsigned char *image, int size);*/

    int dinit();

    //private:
    int mSizeW;
    int mSizeH;
    bool uvMaskValid;
    unsigned char *pMaskY;
    unsigned char *pMaskUV;

    unsigned char *pMaskStitchEdge; // for test only
};

#if 0

class pyramidBase
{
public:
    pyramidBase();
    ~pyramidBase();

    int init(int levelNum, int baseImgW, int baseImgH, float sigma, kernelChoice mkernel);

    // build up this pyramid(just fill up the content of each level)
    virtual int pyraBuild(unsigned char *image) = 0;

    PyramidInfo mPyramidInfo;
    PyramidDataPtrs mPyramidDataPtrs;

protected:
    kernelChoice mKernelChoice;
    BoxKernel mBoxKernel;
    GaussianKernel mGaussianKernel;

    pyraDataType *mpPyraData;

private:

};

class pyramidGaussian : public pyramidBase
{
public:
    pyramidGaussian();
    ~pyramidGaussian();

    int pyraBuild(unsigned char *image);

    // use this gaussian pyramid as mask to weight and summary 2 pyramids
    int levelWeightedSum(pyramidBase *pPyraL, pyramidBase *pPyraR);

    ImageBlender mImageBlender;
private:

};

class pyramidLOG : public pyramidBase
{
public:
    pyramidLOG();
    ~pyramidLOG();

    int pyraBuild(unsigned char *image);

    int pyraReconstuct(unsigned char *image);

private:

};

#endif

}   // namespace util
}   // namespace YiPanorama

#endif //!_IMAGE_BLENDER_H
