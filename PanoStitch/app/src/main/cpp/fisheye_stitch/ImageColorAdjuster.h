/************************************************************************/
/* Image color consistency adjuster                                     */
/* calculate luminance and chrominance difference between images        */
/* and compensate obvious color sudden change                           */
/************************************************************************/

#pragma once
#ifndef _COLOR_ADJUSTER_H
#define _COLOR_ADJUSTER_H

#include "YiPanoramaTypes.h"

namespace YiPanorama {
namespace util {

#define  GRAY_SCALE 256

enum colorSummarySectionLayout
{// color summary sections in one target(image) may connected to each other
 // or separated at different places;
 // several or one sections contribute a summary coefficient
    connected,  // one section -> one coefficient
    interleaved   // several section -> one coefficient
};

enum colorAdjustHeadTailConnection
{
    arround,    // head and tail are connected as loop
    straight    // head and tail are not connected
};

enum colorAdjustScanlineDirection
{// direction of the adjust coefficients
    horizontal, // a row of adjust coefficients
    vertical    // a column of adjust coefficients
};

struct colorSummaryTarget
{// a single summary target is an image need to be checked with its color information
 // and each target may have several sections
    
    imageFrame *pSummaryFrame;  // pointer to the image, no allocated memories newly
    colorAdjustScanlineDirection mSectionsStackDirection;
    int mSectionNum;
    imageRoi *pSectionRois; // ROIs of the sections, and they are stored up-down, left-right
    float *mAverages[3];    // summarized mean values of each section
};

struct colorAdjustTarget
{// a single adjust target is an image need to be adjusted
 // and each target is adjuster line by line(row or column)
    
    imageFrame *pAdjustFrame;   // pointer to the image, no memories newly allocated
    colorAdjustScanlineDirection mAdjustDirection;
    int coeffsLen[3];
    float *coeffs[3];   // adjust coefficients of a whole scan line for 3 channels   
};

class colorAdjuster
{// a common color adjuster is used to adjust color differences between cameras
 // which may be caused by different luminance environment

 // typically, a pair of images have one image as the adjust referee and the other as the adjust target
 // when more than 2 images in sequence consist a panorama image(head and tail connected), every image in the sequence will be the adjust target
 // (possibly one of them has an adjust coefficient close to 1, which is the begin of the optimization process of the coefficients) 

public:
    colorAdjuster();
    ~colorAdjuster();

    // working process:
    // 1. init to set summary targets
    // 2. calculate coefficients and set adjust target
    // 3. adjust the target 
    int init(int imageNum, imageFrame *pImageFrames, int seamRoiNumEachImage, imageRoi *seamRois, int sectionNumEachRoi, colorAdjustScanlineDirection adjustDirection, colorSummarySectionLayout sumSecLayout);

    // calculate color adjust coefficients and set adjust target
    virtual int colorCoeffs() = 0;

    // perform the adjustment
    int colorAdjust();

//private:
    int dinit();

    int colorSummary();
    int colorExposureWeights();

    int colorCoeffScanline();

    int mSummaryImageNum;   
    int mTargetImageNum;
    //int mSummarySectionNumOfEachRoi; // sections of each summary target
    colorSummarySectionLayout mColorSummarySectionLayout;
    colorAdjustHeadTailConnection mColorAdjustHeadTailConnection;

    colorSummaryTarget *mSummaryTargets;
    colorAdjustTarget *mAdjustTargets;
	int mAdjustIdx;
    int mCoeffSectionsNum;
    float *pCoeffSections[3];

    float mExpoCurbWeights[GRAY_SCALE];
};


// =====================================================
class colorAdjusterPair : public colorAdjuster
{
public:
    colorAdjusterPair();
    ~colorAdjusterPair();

    int colorCoeffs();

private:
    int colorCoeffSectionsPair();
};


// =====================================================
#if 0
class colorAdjusterOuroboros : public colorAdjuster
{
public:
    colorAdjusterOuroboros();
    ~colorAdjusterOuroboros();

    int colorCoeffs();

private:
    int colorCoeffSectionsOuroboros();  

};
#endif
}   // namespace util
}   // namespace YiPanorama

#endif  //!_COLOR_ADJUSTER_H
