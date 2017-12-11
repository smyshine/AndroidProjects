
#include "ImageWarpTable.h"
#include "MatrixVectors.h"

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>

#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4
#define MAX_PATH_LEN    512

namespace YiPanorama {
namespace warper {

    imageWarpTable::imageWarpTable() :
        mPmapX(NULL),
        mPmapY(NULL),
        mPvcfr(NULL),
		mPposX(NULL),
		mPposY(NULL)
    {
    }

    imageWarpTable::~imageWarpTable()
    {
    }

int imageWarpTable::init(int wholeImageW, int wholeImageH, float vertAngleUp, float vertAngleDown, float horiAngleLeft, float horiAngleRight, bool isSparse, int stepX, int stepY, bool hasVc)
{// giving the angle range of the desired warp image, calculate its ROI relative to the whole panorama image
 // and allocate all memories

    int modx, mody, tableSize;

    mWarpImgDstRoi.imgW = wholeImageW;  
    mWarpImgDstRoi.imgH = wholeImageH;
    mWarpImgDstRoi.roiX = wholeImageW * horiAngleLeft / 360;    // origin of the ROI
    mWarpImgDstRoi.roiY = wholeImageH * vertAngleUp / 180;      
    mWarpImgDstRoi.roiW = wholeImageW * (horiAngleRight - horiAngleLeft) / 360; // size of the ROI
    mWarpImgDstRoi.roiH = wholeImageH * (vertAngleDown - vertAngleUp) / 180;

    mWarpImageW = mWarpImgDstRoi.roiW;
    mWarpImageH = mWarpImgDstRoi.roiH;

    // sparse table or not
    mIsSparseTable = isSparse;
    if (true == mIsSparseTable)
    {// sparse table 
        mProStepX = stepX;
        mProStepY = stepY;
        modx = mWarpImageW % stepX;
        mody = mWarpImageH % stepY;
        mTableW = mWarpImageW / stepX + 1;
        mTableH = mWarpImageH / stepY + 1;
        if (modx)
            mTableW++;
        if (mody)
            mTableH++;
    }
    else
    {// full table
        mProStepX = 1;
        mProStepY = 1;
        mTableW = mWarpImageW;
        mTableH = mWarpImageH;
    }

    tableSize = mTableW * mTableH;

    // memory allocation, so delete is needed
    mPmapX = new float[tableSize];
    mPmapY = new float[tableSize];
	mPposX = new float[tableSize];
	mPposY = new float[tableSize];


    mHasVC = hasVc;  // some projection doesn't need vignette correction
    if (true == hasVc)
    {
        mPvcfr = new float[tableSize];
    }
    
    return 0;
}

int imageWarpTable::init(int wholeImageW, int wholeImageH, int vertPixelUp, int vertPixelDown, int horiPixelLeft, int horiPixelRight, bool isSparse, int stepX, int stepY, bool hasVc)
{
	// giving the angle range of the desired warp image, calculate its ROI relative to the whole panorama image
	// and allocate all memories

	int modx, mody, tableSize;

	mWarpImgDstRoi.imgW = wholeImageW;
	mWarpImgDstRoi.imgH = wholeImageH;
	mWarpImgDstRoi.roiX = horiPixelLeft;    // origin of the ROI
	mWarpImgDstRoi.roiY = vertPixelUp;
	mWarpImgDstRoi.roiW = horiPixelRight - horiPixelLeft; // size of the ROI
	mWarpImgDstRoi.roiH = vertPixelDown - vertPixelUp;

	mWarpImageW = mWarpImgDstRoi.roiW;
	mWarpImageH = mWarpImgDstRoi.roiH;

	// sparse table or not
	mIsSparseTable = isSparse;
	if (true == mIsSparseTable)
	{// sparse table 
		mProStepX = stepX;
		mProStepY = stepY;
		modx = mWarpImageW % stepX;
		mody = mWarpImageH % stepY;
		mTableW = mWarpImageW / stepX + 1;
		mTableH = mWarpImageH / stepY + 1;
		if (modx)
			mTableW++;
		if (mody)
			mTableH++;
	}
	else
	{// full table
		mProStepX = 1;
		mProStepY = 1;
		mTableW = mWarpImageW;
		mTableH = mWarpImageH;
	}

	tableSize = mTableW * mTableH;

	// memory allocation, so delete is needed
	mPmapX = new float[tableSize];
	mPmapY = new float[tableSize];
	mPposX = new float[tableSize];
	mPposY = new float[tableSize];


	mHasVC = hasVc;  // some projection doesn't need vignette correction
	if (true == hasVc)
	{
		mPvcfr = new float[tableSize];
	}

	return 0;
}

int imageWarpTable::dinit()
{
    // delete map coordinates and vc factors
    if (mPmapX != NULL)
        delete[] mPmapX;

    if (mPmapY != NULL)
        delete[] mPmapY;

    if (mPvcfr != NULL)
        delete[] mPvcfr;

	if (mPposX != NULL)
		delete[] mPposX;

	if (mPposY != NULL)
		delete[] mPposY;

    return 0;
}

int imageWarpTable::genWarperCam(cameraMetadata *pCameraMetadata, int sphereRadius)
{// when fisheye image is the input, vignette correction is needed. 
 // and normalized coordinates are saved.

 // vignette correction factor is calculated by its distance to the image center using a power function.
 // sphereRadius represent the viewing sphere, on this distance objects will be focused. This radius is set by will.

    float theta, phi;
    double sphere[3], cam[3], img[2];
    float vc_factor;
    int k_steps, m_steps;

    // firstly to generate the full projection table ---------------------------------------------
    float *pmapX = mPmapX;
    float *pmapY = mPmapY;
    float *pvcf = mPvcfr;
	float *pposX = mPposX;
	float *pposY = mPposY;

    mSrcImageW = pCameraMetadata->getOcamImgW();
	mSrcImageH = pCameraMetadata->getOcamImgH();

    for (int k = 0; k < mTableH; k++)
    {
        k_steps = mWarpImgDstRoi.roiY + k * mProStepY;
        if (k_steps > mWarpImgDstRoi.roiY + mWarpImgDstRoi.roiH)
            k_steps = mWarpImgDstRoi.roiY + mWarpImgDstRoi.roiH;

		// opengl position Y;
		float tempPosY = 1.0 - 2.0 * (k_steps - mWarpImgDstRoi.roiY) / mWarpImgDstRoi.roiH; // normalized to 1.0 ~ -1.0

        theta = M_PI_2 - M_PI * k_steps / mWarpImgDstRoi.imgH;	// latitude ------

        for (int m = 0; m < mTableW; m++)
        {
            m_steps = mWarpImgDstRoi.roiX + m * mProStepX;
            if (m_steps > mWarpImgDstRoi.roiX + mWarpImgDstRoi.roiW)
                m_steps = mWarpImgDstRoi.roiX + mWarpImgDstRoi.roiW;
			// opengl position X;
			float tempPosX = -1.0 + 2.0 * (m_steps - mWarpImgDstRoi.roiX) / mWarpImgDstRoi.roiW;  // normalized to -1.0 ~ 1.0

            phi = 2 * M_PI * m_steps / mWarpImgDstRoi.imgW;	// longitude, horizontal ---------

            sphere[1] = sin(theta);               // sphere axis Y, pointing to the north pole
            sphere[2] = cos(theta) * cos(phi);    // sphere axis Z, pointing to the viewer
            sphere[0] = -cos(theta) * sin(phi);   // sphere axis X, pointing to the right hand direction
            pCameraMetadata->sph2cam(sphereRadius, cam, sphere);
            pCameraMetadata->cam2img(img, cam);

            ////angleZ is the intersection angle of the camera with the Z axis
            //double angleZ = pCameraMetadata->inrayAngleCam(cam);

            img[0] = (img[0] < 1) ? 0 : (img[0] > (mSrcImageH - 1) ? 0 : img[0]);  // for points falls out of the image borders
            img[1] = (img[1] < 1) ? 0 : (img[1] > (mSrcImageW - 1) ? 0 : img[1]);

            // make them pointing to the head
            //if ((img[0] == 0) || (img[1] == 0) || (angleZ > thresAngle))
            if ((img[0] == 0) || (img[1] == 0))
            {
                img[0] = 0;
                img[1] = 0;
                vc_factor = 1;
            }
            else
            {
                vc_factor = pCameraMetadata->vignettCorrectionFactor(img);
            }

//             *pmapX = img[1] / mSrcImageW;   // normalized to 0 ~ 1
//             *pmapY = img[0] / mSrcImageH;
			*pmapX = img[1];
			*pmapY = img[0];
            pmapX++;
            pmapY++;
			*pposX++ = tempPosX;
			*pposY++ = tempPosY;

            if (mHasVC)
            {
                *pvcf = vc_factor;
                pvcf++;
            }
        }
    }
    return 0;
}

int imageWarpTable::genWarperSph(double *pSphereRotMtx)
{// sphere coordinate axis rotation, vignette correction is not needed

 // rotate sphere's axis with the given extrinsic rotation matrix
 // since all points' color are already adjusted, then the vignette factor is not needed.

 // NOTICE: source and destiny image have same size.
 // and currently only full dense projection is available

    double thetaVert, phiVert, thetaHori, phiHori, norm;
    double sphereVert[3], sphereHori[3], img[2];

    float *pmapX = mPmapX;
    float *pmapY = mPmapY;

    for (int k = 0; k < mWarpImageH; k++)
    {
        thetaVert = M_PI_2 - M_PI * k / mWarpImageH;	// latitude ------

        for (int m = 0; m < mWarpImageW; m++)
        {
            phiVert = 2 * M_PI * m / mWarpImageW;	// longitude ---------

            sphereVert[1] = sin(thetaVert);                 // sphere axis Y, pointing to the north pole
            sphereVert[2] = cos(thetaVert) * cos(phiVert);  // sphere axis Z, pointing to the viewer
            sphereVert[0] = -cos(thetaVert) * sin(phiVert); // sphere axis X, pointing to the right hand direction

            matrixDotMul(pSphereRotMtx, sphereVert, sphereHori, 3, 3, 1);

            thetaHori = asin(sphereHori[1]);
            norm = sqrt(sphereHori[0] * sphereHori[0] + sphereHori[2] * sphereHori[2]);
            phiHori = acos(sphereHori[2] / norm);
            if (sphereHori[0] > 0)
            {
                phiHori = 2 * M_PI - phiHori;
            }

#if 1
            if ((M_PI_2 - thetaHori) == M_PI)
            {
                img[0] = (M_PI_2 - thetaHori - 0.0001) / M_PI;  // avoid the last row of the src image
            }
            else
            {
                img[0] = (M_PI_2 - thetaHori) / M_PI;
            }
#else
            img[0] = (M_PI_2 - thetaHori) / M_PI;
#endif

            img[1] = phiHori / (2 * M_PI);

            *pmapX = img[1];   // normalized to 0 ~ 1
            *pmapY = img[0];

#if 0
            if (img[0] == 1)
            {
                printf_s("row:%d\tcol:%d\n", k, m);
            }
#endif
            pmapX++;
            pmapY++;
        }
    }

    return 0;
}

//------------------------------------------------------------------------------
int imageWarpTable::coordTransPanoToFisheye(double oriCoords[2], double panoCoords[2], int sphereRadius, cameraMetadata *pCameraMetadata)
{// giving a point's coordinates in sphere, calculate its coordinates in original fisheye image

 // CAUTION: the result calculated from this function, is the point's coordinate in the original fisheye image,
 // which is the camera model calibration used image, not the projection source image

    double sphere[3], cam[3], img[2];
    double theta, phi;

    theta = M_PI_2 - M_PI * panoCoords[0] / mWarpImgDstRoi.imgH;  //height;
    phi = 2 * M_PI * panoCoords[1] / mWarpImgDstRoi.imgW;         //width;

    sphere[1] = sin(theta);              // sphere axis Y, pointing to the north pole
    sphere[2] = cos(theta) * cos(phi);   // sphere axis Z, pointing to the viewer
    sphere[0] = -cos(theta) * sin(phi);  // sphere axis X, pointing to the right hand direction

    pCameraMetadata->sph2cam(sphereRadius, cam, sphere);
    pCameraMetadata->cam2img(img, cam);

    *oriCoords = img[0];        //y, rows;
    *(oriCoords + 1) = img[1];  //x, cols;

    return 0;
}

//------------------------------------------------------------------------------
int imageWarpTable::coordTransFisheyeToPano(double panoCoords[2], double oriCoords[2], int sphereRadius, cameraMetadata *pCameraMetadata)
{// giving a point's coordinates in original fisheye image, calculate its coordinates in projection image

 // CAUTION: the source coordinates of oriCoords, is the point's coordinate in the original fisheye image,
 // which is the camera model calibration used image, not the projection source image

    double sphere[3], cam[3], img[2];
    double theta, phi, norm;
    int k;

    img[0] = oriCoords[0];  // y
    img[1] = oriCoords[1];  // x

    pCameraMetadata->img2cam(cam, img);
    pCameraMetadata->cam2sph(sphereRadius, sphere, cam);


    norm = sqrt(sphere[0] * sphere[0] + sphere[1] * sphere[1] + sphere[2] * sphere[2]);
    for (k = 0; k < 3; k++)
    {
        sphere[k] /= norm;
    }

    theta = asin(sphere[1]);
    norm = sqrt(sphere[0] * sphere[0] + sphere[2] * sphere[2]);
    phi = acos(sphere[2] / norm);

    if (sphere[0] > 0)
    {
        phi = 2 * M_PI - phi;
    }

    panoCoords[1] = mWarpImgDstRoi.imgW * phi / (2 * M_PI);           // x
    panoCoords[0] = mWarpImgDstRoi.imgH * (M_PI_2 - theta) / M_PI;    // y

    return 0;
}

int imageWarpTable::checkMapTableRange()
{
    int *tmpX = new int[mTableH * mTableW];
    int *tmpY = new int[mTableH * mTableW];

    for (int k = 0; k < mTableH; k++)
    {
        for (int m = 0; m < mTableW; m++)
        {
            if (mPmapX[k * mTableW + m] == 0)
                tmpX[k * mTableW + m] = 0;
            else
                tmpX[k * mTableW + m] = 1;

            if (mPmapY[k * mTableW + m] == 0)
                tmpY[k * mTableW + m] = 0;
            else
                tmpY[k * mTableW + m] = 1;
        }
    }

    FILE *fp = NULL;
    fp = fopen("tableX.txt", "wt");
    for (int k = 0; k < mTableH; k++)
    {
        for (int m = 0; m < mTableW; m++)
        {
            fprintf(fp, "%d ", tmpX[k*mTableW + m]);
        }
        fscanf(fp, "\n");
    }
    fclose(fp);

    fp = fopen("tableY.txt", "wt");
    for (int k = 0; k < mTableH; k++)
    {
        for (int m = 0; m < mTableW; m++)
        {
            fscanf(fp, "%d ", tmpY[k*mTableW + m]);
        }
        fscanf(fp, "\n");
    }
    fclose(fp);

    delete[] tmpX;
    delete[] tmpY;
    return 0;
}

int imageWarpTable::setSrcRoi(int roiX, int roiY, int roiW, int roiH)
{
	mSrcImageRoi.imgH = mSrcImageH;
	mSrcImageRoi.imgW = mSrcImageW;
	mSrcImageRoi.roiY = roiY;
	mSrcImageRoi.roiX = roiX;
	mSrcImageRoi.roiH = roiH;
	mSrcImageRoi.roiW = roiW;

	return 0;
}

int imageWarpTable::setSrcRoi(double normalizedRoiX, double normalizedRoiY, double normalizedRoiW, double normalizedRoiH)
{
	mSrcImageRoi.imgH = mSrcImageH;
	mSrcImageRoi.imgW = mSrcImageW;
	mSrcImageRoi.roiY = normalizedRoiY * mSrcImageH;
	mSrcImageRoi.roiX = normalizedRoiX * mSrcImageW;
	mSrcImageRoi.roiH = normalizedRoiH * mSrcImageH;
	mSrcImageRoi.roiW = normalizedRoiW * mSrcImageW;

	return 0;
}

}   // namespace warper
}   // namespace YiPanorama