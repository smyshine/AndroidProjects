/************************************************************************/
/* Image optical flow and conversion using OpenCV                       */
/************************************************************************/
#pragma once
#ifndef _IMAGE_OPT_FLOW_H
#define _IMAGE_OPT_FLOW_H

#include "YiPanoramaTypes.h"
#include <opencv.hpp>
#include <opencv2/ximgproc.hpp>
#include <string>

namespace YiPanorama {
namespace util {

using namespace cv;
using namespace cv::ximgproc;

// function declarations ========================================================
struct optFlowFarneback
{
    double pyrScale;
    int numLevels;
    int winSize;
    int numIters;
    int polyN;
    double polySigma;
    int flags;
    bool fastPyramids;
}; 

class ImageOptFlow
{
public:
    ImageOptFlow();
    ~ImageOptFlow();

    // initialize the kernel of this optical flow
    int init(int imageW, int imageH);

    // compute optical flow from left to right image, only gray channel of the image is needed
    int computeImageFlow(imageFrame pImgL, imageFrame pImgR);

    // generate the warp maps according to the optical flows
    int genWarpMap();
    
    // generate novel view image from left image by calculated flow
    int genNovalImage(imageFrame pSrcImgL, imageFrame pSrcImgR);

    // free
    int dinit();


private:
    // opticalFlow related data
    optFlowFarneback farn;
    int mImageW;
    int mImageH;

    // opencv image structure
    Mat imageL; // since main data structure are imageFrame(YUV420) in video, this is generated from the Y channel of the image
    Mat imageR;

    Mat flowLtoR;   // flow from left to right
    Mat flowRtoL;   // flow from right to left

    Mat warpMapL;   // warp map generated from flow right to left, the trans-shift is needed
    Mat warpMapR;   // warp map generated from flow left to right, the trans-shift is needed

    imageFrame tempImage;
};


}   // namespace util
}   // namespace YiPanorama

#endif  //!_IMAGE_OPT_FLOW_H