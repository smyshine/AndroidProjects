
#include "ImageBlender.h"

#include "ImageIOConverter.h"

#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <string.h>

namespace YiPanorama {
namespace util {

#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4

#define USE_GAUSSIAN_BLUR       1   // use gaussian blur or box blur
// function definitions ========================================================

// buffer for temporary images during pyramid operations
static float *gTempImageBuf = NULL;       // for pyramid smooth operations, temporary images' buffer
static void *gTmpVoidBuf = NULL;          // temp void buffer


// ====================================================================
ImageBlender::ImageBlender() :
	pMaskY(NULL),
	pMaskUV(NULL),
	pMaskStitchEdge(NULL)
{
}

ImageBlender::~ImageBlender()
{
}

int ImageBlender::init(int sizeW, int sizeH)
{
    mSizeW = sizeW;
    mSizeH = sizeH;
    uvMaskValid = false;
    pMaskY = new unsigned char[sizeW * sizeH];
    //pMaskUV = new unsigned char[sizeW * sizeH / 4];
    return 0;
}

int ImageBlender::loadMask(char *filePath, int width, int height)
{
    FILE *fp = NULL;
    fp = fopen(filePath, "rb");
    if (fp == NULL)
    {
        return -1;
    }

    fread(pMaskY, 1, sizeof(unsigned char)*width*height, fp);
    fclose(fp);

    return 0;
}

int ImageBlender::dinit()
{
    mSizeW = 0;
    mSizeH = 0;
    if (pMaskY != NULL)
    {
        delete[] pMaskY;
        pMaskY = NULL;
    }
    if (pMaskUV != NULL)
    {
        delete[] pMaskUV;
        pMaskUV = NULL;
    }

    if (pMaskStitchEdge != NULL)
    {
        delete[] pMaskStitchEdge;
        pMaskStitchEdge = NULL;
    }
    return 0;
}


}   // namespace pyra
}   // namespace YiPanorama