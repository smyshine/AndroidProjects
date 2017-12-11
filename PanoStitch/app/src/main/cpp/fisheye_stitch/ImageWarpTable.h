/************************************************************************/
/* projection table definition and generating                           */
/************************************************************************/
#pragma once
#ifndef _IMAGE_WARP_TABLE_H
#define _IMAGE_WARP_TABLE_H

#include "YiPanoramaTypes.h"
#include "CameraMetadata.h"

namespace YiPanorama {
namespace warper {

using namespace util;
using namespace calibration;

class imageWarpTable
{// an image warp table represents a projection mapping table and generating operations related.
public:
    imageWarpTable();
    ~imageWarpTable();

    // set size parameters, especially the roi range according to the angle range, and allocate memories
    int init(int wholeImageW, int wholeImageH, float vertAngleUp, float vertAngleDown, float horiAngleLeft, float horiAngleRight, bool isSparse, int stepX, int stepY, bool hasVc);
	// set size parameters, especially the roi range according to the image row & column range, and allocate memories
	int init(int wholeImageW, int wholeImageH, int vertPixelUp, int vertPixelDown, int horiPixelLeft, int horiPixelRight, bool isSparse, int stepX, int stepY, bool hasVC);

    // release table memories
    int dinit();

    // save projection table into file
    //int save(char *fileName);

    // read projection table from file
    //int read(char *fileName);

    // set fisheye camera projection table, memories are set in this process
    int genWarperCam(cameraMetadata *pCameraMetadata, int sphereRadius);
    //int genWarperCamReserve(cameraMetadata *pCameraMetadata, int sphereRadius, int thresAngle);

    // set whole sphere projection, memories are set in this process
    int genWarperSph(double *pSphereRotMtx);

    // transform a point's coordinates from panoramic image to ORIGINAL fisheye image
    int coordTransPanoToFisheye(double oriCoords[2], double panoCoords[2], int sphereRadius, cameraMetadata *pCameraMetadata);

    // transform a point's coordinates from ORIGINAL fisheye image to panoramic image
    int coordTransFisheyeToPano(double panoCoords[2], double oriCoords[2], int sphereRadius, cameraMetadata *pCameraMetadata);

    // check the table's valid ROI
    int checkMapTableRange();

	// set the source image roi, para: roiX, roiY, roiW, roiH;
	int setSrcRoi(int roiX, int roiY, int roiW, int roiH);

	// set the source image roi, para: normalized roiX, roiY, roiW, roiH;
	int setSrcRoi(double normalizedRoiX, double normalizedRoiY, double normalizedRoiW, double normalizedRoiH);

//protected:
    // projection table free
    int mWarpImageW;         // size of the warp result image
    int mWarpImageH;

	int mSrcImageW;          // size of the source image
	int mSrcImageH;

    imageRoi mWarpImgDstRoi; // the warped image is part of a destiny image
                             // since this roi may not be available before projection
                             // so the size of the map table could also be set in the process of projection
	imageRoi mSrcImageRoi; // the source image is part of a destiny image

    bool mIsSparseTable;    // if the table is sparse table
    int mProStepX;          // sparse mapping table steps
    int mProStepY;
    int mTableW;            // sparse mapping table size
    int mTableH;

    bool mHasVC;

    float *mPmapX;            // mapping table coordinates, normalized, ranging from 0 ~ 1
    float *mPmapY;            // 
    float *mPvcfr;            // vignette correction factor

	float *mPposX;            // postion x, y in OpenGL, normalized , ranging from -1 ~ 1;
	float *mPposY;
};

}   // namespace warper
}   // namespace YiPanorama

#endif  //!_IMAGE_WARP_TABLE_H