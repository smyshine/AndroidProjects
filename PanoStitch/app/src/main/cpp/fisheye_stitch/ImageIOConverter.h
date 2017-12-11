/************************************************************************/
/* Image IO and conversion using OpenCV                                 */
/************************************************************************/
#pragma once
#ifndef _IMAGE_IO_CONVERTER
#define _IMAGE_IO_CONVERTER


#include "YiPanoramaTypes.h"
#include <opencv.hpp>

namespace YiPanorama {
namespace util {


using namespace cv;

enum imageLayout
{
    mono,
    sideBySide,
    overAndUnder
};

// function declarations ========================================================
// read image parameters: width, height, channel
int readImageParameters(const char *imgPath, int *iWidth, int *iHeight, int *iChn);

// initialize image frame's size and buffer
int initImageFrame(imageFrame *pImage, int width, int height, ePixelColorSpace imgCs);

// free image frame buffer
int dinitImageFrame(imageFrame *pImage);

// load image data into preallocated memory
int loadImageData(const char *imgPath, imageFrame image);

int loadImageDataFisheyePair(const char *imgPath, imageFrame fisheyeImage[2], imageLayout layout);

// save image to file
int saveImage(const char *imgPath, imageFrame image);
int saveImage(const char *imgPath, unsigned char *imageData, int iWidth, int iHeight, ePixelColorSpace imgCS);

int copyImage(imageFrame *pSrcImage, imageFrame *pDstImage);

// convert uc image data to opencv mat
int convertImageFrametoCvMat(imageFrame image, Mat imgDst);
int convertImageFrametoCvMatGray(imageFrame image, Mat imgDst);

}   // namespace util
}   // namespace YiPanorama

#endif  // !_IMAGE_IO_CONVERTER