
#include "ImageColorAdjuster.h"

#include <math.h>
#include <string.h>

namespace YiPanorama {
namespace util {

#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4

#define EXPOSURE_WEIGHT_THRES 150

// =============================================================================
//------------------------------------------------------------------------------
float colorSummary1Chn1Roi(unsigned char *img, imageRoi roiInImage)
{// calculate the average intensity of the roi area in the image

    unsigned char *pImg = img;
    int count = 0;
    double amout = 0.0;

    pImg += roiInImage.imgW * roiInImage.roiY + roiInImage.roiX;
    for (int k = 0; k < roiInImage.roiH; k++)
    {
        for (int m = 0; m < roiInImage.roiW; m++)
        {
            amout += *(pImg + m);
            count++;
        }
        pImg += roiInImage.imgW;
    }

    return amout / count;
}

int colorSummary3Chn1Roi(unsigned char *img, imageRoi roiInImage, double *average)
{// calculate the R,G,B three channel average intensity of the roi area in the image

	unsigned char *pImg = img;
	int count = 0;
	double amout[3] = { 0.0, 0.0, 0.0 };

	pImg += roiInImage.imgW * 3 * roiInImage.roiY + roiInImage.roiX * 3;
	for (int k = 0; k < roiInImage.roiH; k++)
	{
		for (int m = 0; m < roiInImage.roiW; m++)
		{
			amout[0] += *(pImg + 3 * m + 0);
			amout[1] += *(pImg + 3 * m + 1);
			amout[2] += *(pImg + 3 * m + 2);
			count++;
		}
		pImg += roiInImage.imgW * 3;
	}
	average[0] = amout[0] / count;
	average[1] = amout[1] / count;
	average[2] = amout[2] / count;
	return 0;
}

int colorSummary1Target(colorSummaryTarget *pColorSummaryTarget)
{
    imageRoi localRoi;
    for (int k = 0; k < pColorSummaryTarget->mSectionNum; k++)
    {
		localRoi = pColorSummaryTarget->pSectionRois[k];
		double tmpAverage[3] = { 0.0 };
		colorSummary3Chn1Roi(pColorSummaryTarget->pSummaryFrame[k].plane[0], localRoi, tmpAverage);
        for (int m = 0; m < 3; m++)
        {
            pColorSummaryTarget->mAverages[m][k] = tmpAverage[m];
        }
    }
    return 0;
}

    colorAdjuster::colorAdjuster()
    {
    }

    colorAdjuster::~colorAdjuster()
    {
    }

int colorAdjuster::init(int imageNum, imageFrame *pImageFrames, int seamRoiNumEachImage, imageRoi *seamRois, int sectionNumEachRoi, colorAdjustScanlineDirection adjustDirection, colorSummarySectionLayout sumSecLayout)
{
    int singleSectionSizeW, singleSectionSizeH;

    colorExposureWeights();
    mSummaryImageNum = imageNum;
    mTargetImageNum = imageNum / 2; // as always, 1 image is not adjusted
    //mSummarySectionNumOfEachRoi = sectionNumEachRoi;

    mSummaryTargets = new colorSummaryTarget[mSummaryImageNum];
    mAdjustTargets = new colorAdjustTarget[mTargetImageNum];

    mColorSummarySectionLayout = sumSecLayout;

    for (int k = 0; k < imageNum; k++)
    {
        mSummaryTargets[k].pSummaryFrame = &pImageFrames[k]; 
        mSummaryTargets[k].mSectionsStackDirection = adjustDirection;
        mSummaryTargets[k].mSectionNum = seamRoiNumEachImage * sectionNumEachRoi;
        mSummaryTargets[k].pSectionRois = new imageRoi[mSummaryTargets[k].mSectionNum];
        mSummaryTargets[k].mAverages[0] = new float[mSummaryTargets[k].mSectionNum];
        mSummaryTargets[k].mAverages[1] = new float[mSummaryTargets[k].mSectionNum];
        mSummaryTargets[k].mAverages[2] = new float[mSummaryTargets[k].mSectionNum];
        
        // set section ROIs
        for (int m = 0; m < seamRoiNumEachImage; m++)
        {
            if (adjustDirection == horizontal)
            {
                singleSectionSizeW = seamRois[k * seamRoiNumEachImage + m].roiW / sectionNumEachRoi;
                singleSectionSizeH = seamRois[k * seamRoiNumEachImage + m].roiH;
            } 
            else
            {
                singleSectionSizeW = seamRois[k * seamRoiNumEachImage + m].roiW;
                singleSectionSizeH = seamRois[k * seamRoiNumEachImage + m].roiH / sectionNumEachRoi;
            }
            
            for (int h = 0; h < sectionNumEachRoi; h++)
            {
                mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].imgW = seamRois[k * seamRoiNumEachImage + m].imgW;
                mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].imgH = seamRois[k * seamRoiNumEachImage + m].imgH;
                mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiW = singleSectionSizeW;
                mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiH = singleSectionSizeH;
                if (adjustDirection == horizontal)
                {
                    mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiX = seamRois[k * seamRoiNumEachImage + m].roiX + h * singleSectionSizeW;
                    mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiY = seamRois[k * seamRoiNumEachImage + m].roiY;
                }
                else
                {
                    mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiX = seamRois[k * seamRoiNumEachImage + m].roiX;
                    mSummaryTargets[k].pSectionRois[m*sectionNumEachRoi + h].roiY = seamRois[k * seamRoiNumEachImage + m].roiY + h * singleSectionSizeH;
                }
            }
        }
    }

    return 0;
}

int colorAdjuster::dinit()
{
    if (mSummaryTargets != NULL)
    {
        delete[] mSummaryTargets;
        mSummaryTargets = NULL;
    }

    if (mAdjustTargets != NULL)
    {
        delete[] mAdjustTargets;
        mAdjustTargets = NULL;
    }
    return 0;
}

//------------------------------------------------------------------------------
int colorAdjuster::colorExposureWeights()
{// used for exposure curb
    // for 256 gray scales, each one has a adjust weight to curb overexposure, and these weights range from 0 to 1

    int kk;
    unsigned char point = EXPOSURE_WEIGHT_THRES;

    for (kk = 0; kk < point; kk++)  // no curbing for gray scales under this threshold
        mExpoCurbWeights[kk] = 1.0;

    for (kk = point; kk < 256; kk++)
    {
        float f = ((float)(kk - point)) / (255 - point);
        f = f*M_PI / 2.0;
        mExpoCurbWeights[kk] = cos(f);
    }
    return 0;
}

int colorAdjuster::colorSummary()
{// summary all sections of all images
    for (int k = 0; k < mSummaryImageNum; k++)
    {
        colorSummary1Target(&mSummaryTargets[k]);
    }
    return 0;
}

//------------------------------------------------------------------------------
int colorCoeffsWidth1Chn(float *coeffsSection, int sectionNum, float *coeffsWidth, int width, colorAdjustHeadTailConnection ColorAdjustHeadTailConnection)
{// calculate coefficients for each column in the width direction
 // and the transition area between sections has the width smaller than the section width

 // NOTICE: the width is separated by sectionNum commonly

    int k, m;
    int *pLeftCoeffIdx = new int[sectionNum];
    int *pMiddCoeffIdx = new int[sectionNum];
    int *pRightCoeffIdx = new int[sectionNum];


    int sectionWidth = width / sectionNum;    // NOTICE: the section width should be multiple of 4
    int transition_len = sectionWidth / 2;   // transition area has the quarter width of a section, 

    float *pCoeffs = coeffsWidth;
    float ratio, transitTargetCoeff;   // average coefficient of two adjacent coefficients

    for (k = 0; k < sectionNum; k++)
    {
        if (ColorAdjustHeadTailConnection == arround)
        {
            pLeftCoeffIdx[k] = (k + sectionNum - 1) % sectionNum;
            pMiddCoeffIdx[k] = k;
            pRightCoeffIdx[k] = (k + 1) % sectionNum;
        } 
        else
        {
            pLeftCoeffIdx[k] = (k - 1) % sectionNum;
            pMiddCoeffIdx[k] = k;
            pRightCoeffIdx[k] = (k + 1) % sectionNum;

            if (pLeftCoeffIdx[k] < 0)
                pLeftCoeffIdx[k] = 0;

            if (pRightCoeffIdx[k] == 0)
                pRightCoeffIdx[k] = sectionNum - 1;
        }
    }

    for (k = 0; k < sectionNum; k++)
    {// 3 separated parts
     // left transition len
        transitTargetCoeff = (coeffsSection[pLeftCoeffIdx[k]] + coeffsSection[pMiddCoeffIdx[k]]) / 2;
        for (m = 0; m < transition_len; m++)
        {
            ratio = ((float)m) / transition_len;
            *pCoeffs++ = ratio*(coeffsSection[pMiddCoeffIdx[k]] - transitTargetCoeff) + transitTargetCoeff;
        }

        // middle
        for (m = transition_len; m < sectionWidth - transition_len; m++)
        {
            *pCoeffs++ = coeffsSection[pMiddCoeffIdx[k]];
        }

        // right transition len
        transitTargetCoeff = (coeffsSection[pMiddCoeffIdx[k]] + coeffsSection[pRightCoeffIdx[k]]) / 2;
        for (m = sectionWidth - transition_len; m < sectionWidth; m++)
        {
            ratio = ((float)(sectionWidth - m)) / transition_len;
            *pCoeffs++ = ratio*(coeffsSection[pMiddCoeffIdx[k]] - transitTargetCoeff) + transitTargetCoeff;
        }
    }

    delete[] pLeftCoeffIdx;
    delete[] pMiddCoeffIdx;
    delete[] pRightCoeffIdx;

    return 0;
}

//------------------------------------------------------------------------------
int colorCoeffsWidth1Chn(float *coeffsSection, int sectionNum, float *coeffsWidth0, float *coeffsWidth1, int width, colorAdjustHeadTailConnection ColorAdjustHeadTailConnection)
{// calculate coefficients for each column in the width direction
 // and the transition area between sections has the width smaller than the section width

 // NOTICE: the width is separated by sectionNum commonly

	int k, m;
	int *pLeftCoeffIdx = new int[sectionNum];
	int *pMiddCoeffIdx = new int[sectionNum];
	int *pRightCoeffIdx = new int[sectionNum];


	int sectionWidth = width;    // NOTICE: the section width should be multiple of 4
	int transition_len = sectionWidth / 4;   // transition area has the quarter width of a section, 

	float *coeffsWidth[2] = { coeffsWidth0, coeffsWidth1 };
	float ratio, transitTargetCoeff;   // average coefficient of two adjacent coefficients

	for (k = 0; k < sectionNum; k++)
	{
		if (ColorAdjustHeadTailConnection == arround)
		{
			pLeftCoeffIdx[k] = (k + sectionNum - 1) % sectionNum;
			pMiddCoeffIdx[k] = k;
			pRightCoeffIdx[k] = (k + 1) % sectionNum;
		}
		else
		{
			pLeftCoeffIdx[k] = (k - 1) % sectionNum;
			pMiddCoeffIdx[k] = k;
			pRightCoeffIdx[k] = (k + 1) % sectionNum;

			if (pLeftCoeffIdx[k] < 0)
				pLeftCoeffIdx[k] = 0;

			if (pRightCoeffIdx[k] == 0)
				pRightCoeffIdx[k] = sectionNum - 1;
		}
	}

	for (k = 0; k < sectionNum; k++)
	{// 3 separated parts
	 // left transition len
		float *pCoeffs = coeffsWidth[k];

		transitTargetCoeff = (coeffsSection[pLeftCoeffIdx[k]] + coeffsSection[pMiddCoeffIdx[k]]) / 2;
		for (m = 0; m < transition_len; m++)
		{
			ratio = ((float)m) / transition_len;
			*pCoeffs++ = ratio*(coeffsSection[pMiddCoeffIdx[k]] - transitTargetCoeff) + transitTargetCoeff;
		}

		// middle
		for (m = transition_len; m < sectionWidth - transition_len; m++)
		{
			*pCoeffs++ = coeffsSection[pMiddCoeffIdx[k]];
		}

		// right transition len
		transitTargetCoeff = (coeffsSection[pMiddCoeffIdx[k]] + coeffsSection[pRightCoeffIdx[k]]) / 2;
		for (m = sectionWidth - transition_len; m < sectionWidth; m++)
		{
			ratio = ((float)(sectionWidth - m)) / transition_len;
			*pCoeffs++ = ratio*(coeffsSection[pMiddCoeffIdx[k]] - transitTargetCoeff) + transitTargetCoeff;
		}
	}

	delete[] pLeftCoeffIdx;
	delete[] pMiddCoeffIdx;
	delete[] pRightCoeffIdx;

	return 0;
}


int colorAdjuster::colorCoeffScanline()
{// giving a set of coefficients for sections, calculate the coefficients with transitions in a length of scanline

	colorCoeffsWidth1Chn(pCoeffSections[0], mCoeffSectionsNum, mAdjustTargets[0].coeffs[0], mAdjustTargets[2].coeffs[0], mAdjustTargets[0].coeffsLen[0], mColorAdjustHeadTailConnection);
	colorCoeffsWidth1Chn(pCoeffSections[1], mCoeffSectionsNum, mAdjustTargets[0].coeffs[1], mAdjustTargets[2].coeffs[1], mAdjustTargets[0].coeffsLen[1], mColorAdjustHeadTailConnection);
	colorCoeffsWidth1Chn(pCoeffSections[2], mCoeffSectionsNum, mAdjustTargets[0].coeffs[2], mAdjustTargets[2].coeffs[2], mAdjustTargets[0].coeffsLen[2], mColorAdjustHeadTailConnection);

	// subImage 0 and 1 has same coeffs, as well as sumImage 2 and 3;
	for (int i = 0; i != 3; ++i)
	{
		memcpy(mAdjustTargets[1].coeffs[i], mAdjustTargets[0].coeffs[i], sizeof(float) * mAdjustTargets[0].coeffsLen[i]);
		memcpy(mAdjustTargets[3].coeffs[i], mAdjustTargets[2].coeffs[i], sizeof(float) * mAdjustTargets[2].coeffsLen[i]);
	}
	
    return 0;
}

//------------------------------------------------------------------------------
int colorAdjustLuminChnScanline(unsigned char *img, int imageW, int imageH, int scanlineLen, float *coeffs, float *weights, colorAdjustScanlineDirection ColorAdjustScanlineDirection)
{// adjust the image between borders using the coefficients which has same length of the image
 // and the weights is the exposure curbs
    unsigned char *pImg = img;
    float *pCoeffs = coeffs;
    int tmpPixelValue;

    if (ColorAdjustScanlineDirection == horizontal)
    {
        for (int k = 0; k < imageH; k++)
        {
            pCoeffs = coeffs;
            for (int m = 0; m < imageW; m++)
            {
                tmpPixelValue = (1 + (*pCoeffs - 1)*weights[*pImg])**pImg + 0.5;
                *pImg++ = tmpPixelValue > 255 ? 255 : (tmpPixelValue < 0 ? 0 : tmpPixelValue);
                pCoeffs++;
            }

        }
    } 
    else
    {
        for (int k = 0; k < imageH; k++)
        {
            //pCoeffs = coeffs;
            for (int m = 0; m < imageW; m++)
            {
                tmpPixelValue = (1 + (*pCoeffs - 1)*weights[*pImg])**pImg + 0.5;
                *pImg++ = tmpPixelValue > 255 ? 255 : (tmpPixelValue < 0 ? 0 : tmpPixelValue);
            }
            pCoeffs++;
        }
    }

    return 0;
}

//------------------------------------------------------------------------------
int colorAdjustChromChnScanline(unsigned char *img, int imageW, int imageH, int scanlineLen, float *coeffs, colorAdjustScanlineDirection ColorAdjustScanlineDirection)
{// adjust the image between borders using the coefficients which has same length of the image
 // since this is UV channel, no exposure curbing is performed.
    unsigned char *pImg = img;
    float *pCoeffs = coeffs;
    int tmpPixelValue;

    //pImg += imgW * upBorder;    // pointing to the start of the area needed to be adjusted

    if (ColorAdjustScanlineDirection == horizontal)
    {
        for (int k = 0; k < imageH; k++)
        {
            pCoeffs = coeffs;
            for (int m = 0; m < imageW; m++)
            {
                tmpPixelValue = *pCoeffs**pImg + 0.5;
                *pImg++ = tmpPixelValue > 255 ? 255 : (tmpPixelValue < 0 ? 0 : tmpPixelValue);
                pCoeffs++;
            }
        }
    }
    else
    {
        for (int k = 0; k < imageH; k++)
        {
            //pCoeffs = coeffs;
            for (int m = 0; m < imageW; m++)
            {
                tmpPixelValue = *pCoeffs**pImg + 0.5;
                *pImg++ = tmpPixelValue > 255 ? 255 : (tmpPixelValue < 0 ? 0 : tmpPixelValue);
            }
            pCoeffs++;
        }
    }

    return 0;
}

int colorAdjuster::colorAdjust()
{
    colorAdjustLuminChnScanline(mAdjustTargets->pAdjustFrame->plane[0], mAdjustTargets->pAdjustFrame->imageW, mAdjustTargets->pAdjustFrame->imageH, mAdjustTargets->coeffsLen[0], mAdjustTargets->coeffs[0], mExpoCurbWeights, mAdjustTargets->mAdjustDirection);
    colorAdjustChromChnScanline(mAdjustTargets->pAdjustFrame->plane[1], mAdjustTargets->pAdjustFrame->imageW / 2, mAdjustTargets->pAdjustFrame->imageH / 2, mAdjustTargets->coeffsLen[1], mAdjustTargets->coeffs[1], mAdjustTargets->mAdjustDirection);
    colorAdjustChromChnScanline(mAdjustTargets->pAdjustFrame->plane[2], mAdjustTargets->pAdjustFrame->imageW / 2, mAdjustTargets->pAdjustFrame->imageH / 2, mAdjustTargets->coeffsLen[2], mAdjustTargets->coeffs[2], mAdjustTargets->mAdjustDirection);

    return 0;
}


// =============================================================================
    colorAdjusterPair::colorAdjusterPair()
    {
    }

    colorAdjusterPair::~colorAdjusterPair()
    {
    }

int colorAdjusterPair::colorCoeffs()
{// procedure:
 // 1. summary color informations
 // 2. choose adjust target 
 // 3. coefficients sections
 // 4. coefficients scan-line
    colorSummary();

    if (mColorSummarySectionLayout == connected)
    {//
        mCoeffSectionsNum = mSummaryImageNum / 2;
    } 
    else
    {
        mCoeffSectionsNum = mSummaryImageNum / 4; // 2 pano images, every pano image is divided into 4 subimage;
    }
    for (int k = 0; k < 3; k++) // 3 channels
    {
        pCoeffSections[k] = new float[mCoeffSectionsNum];
    }
    
    colorCoeffSectionsPair();
    colorCoeffScanline();
    return 0;
}

int colorAdjusterPair::colorCoeffSectionsPair()
{// for sections connected to each other

 // determine the referee 
    int refereeIdx, adjustIdx;
    float refereeAverage, adjustAverage;
    float *averageSums = new float[mSummaryImageNum];       // only 2 images as input
    memset(averageSums, 0, sizeof(float)*mSummaryImageNum);

    for (int k = 0; k < mSummaryImageNum; k++)
    {
        for (int m = 0; m < mSummaryTargets[0].mSectionNum; m++)
        {
			for (int i = 0; i != 3; ++i)
			{
				averageSums[k] += mSummaryTargets[k].mAverages[i][m];   // R,G,B channelS, all sections together of a summary target
			}
        }
    }

    if ((averageSums[0] + averageSums[1] + averageSums[2] + averageSums[3]) >
		(averageSums[4] + averageSums[5] + averageSums[6] + averageSums[7]))    // currently only 2 images as input
        adjustIdx = 1;  // adjust the lower one
    else
        adjustIdx = 0;
    refereeIdx = 1 - adjustIdx;

	mAdjustIdx = adjustIdx;

    // set adjust target
	for (int i = 0; i != mSummaryImageNum / 2; ++i)
	{
		mAdjustTargets[i].pAdjustFrame = mSummaryTargets[adjustIdx * mSummaryImageNum / 2 + i].pSummaryFrame;
		mAdjustTargets[i].mAdjustDirection = mSummaryTargets[adjustIdx * mSummaryImageNum / 2 + i].mSectionsStackDirection;
		if (mAdjustTargets->mAdjustDirection == horizontal)
		{
			mAdjustTargets[i].coeffsLen[0] = mAdjustTargets[i].pAdjustFrame->imageW;
			mAdjustTargets[i].coeffsLen[1] = mAdjustTargets[i].pAdjustFrame->imageW;
			mAdjustTargets[i].coeffsLen[2] = mAdjustTargets[i].pAdjustFrame->imageW;
		}
		else
		{
			mAdjustTargets[i].coeffsLen[0] = mAdjustTargets[i].pAdjustFrame->imageH;
			mAdjustTargets[i].coeffsLen[1] = mAdjustTargets[i].pAdjustFrame->imageH;
			mAdjustTargets[i].coeffsLen[2] = mAdjustTargets[i].pAdjustFrame->imageH;
		}

		mAdjustTargets[i].coeffs[0] = new float[mAdjustTargets[i].coeffsLen[0]];
		mAdjustTargets[i].coeffs[1] = new float[mAdjustTargets[i].coeffsLen[1]];
		mAdjustTargets[i].coeffs[2] = new float[mAdjustTargets[i].coeffsLen[2]];
	}
    

    // coeffs of sections calculation

    if (mColorSummarySectionLayout == connected)
    {
        for (int k = 0; k < 3; k++)
        {
            for (int m = 0; m < mCoeffSectionsNum; m++)
            {
                pCoeffSections[k][m] = mSummaryTargets[refereeIdx].mAverages[k][m] / mSummaryTargets[adjustIdx].mAverages[k][m];
            }
        }
    }
    else
    {
        for (int k = 0; k < 3; k++)
        {
            for (int m = 0; m < mCoeffSectionsNum; ++m)
            {
                //pCoeffSections[k][m] = mSummaryTargets[refereeIdx].mAverages[k][m] / mSummaryTargets[adjustIdx].mAverages[k][m];
                refereeAverage = mSummaryTargets[refereeIdx * mSummaryImageNum / 2 + 2 * m].mAverages[k][0] + mSummaryTargets[refereeIdx * mSummaryImageNum / 2 + 2 * m + 1].mAverages[k][0];
                adjustAverage = mSummaryTargets[adjustIdx * mSummaryImageNum / 2 + 2 * m].mAverages[k][0] + mSummaryTargets[adjustIdx * mSummaryImageNum / 2 + 2 * m + 1].mAverages[k][0];
                pCoeffSections[k][m] = refereeAverage / adjustAverage;
            }
        }
    }

    return 0;
}

// =============================================================================
#if 0
    colorAdjusterOuroboros::colorAdjusterOuroboros()
    {
    }

    colorAdjusterOuroboros::~colorAdjusterOuroboros()
    {
    }

int colorAdjusterOuroboros::colorCoeffs()
{
    colorSummary();

    // to be done:
    // coeff sections

    colorCoeffSectionsOuroboros();
    colorCoeffScanline();
    return 0;
}

int colorAdjusterOuroboros::colorCoeffSectionsOuroboros()  // 3 different types of section coeffs
{
    // reserved for jump.
    return 0;
}

#endif


}   // namespace util
}   // namespace YiPanorama