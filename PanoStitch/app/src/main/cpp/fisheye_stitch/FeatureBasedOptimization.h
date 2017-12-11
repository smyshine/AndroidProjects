/************************************************************************/
/* for camera intrinsic and extrinsic parameters optimization           */
/* 1. intrinsic parameter: image circle center                          */
/*    1) chessboard corners detection                                   */
/*    2) use lev-mar optimization(RMS of detected chessboard corners)   */
/* 2. extrinsic parameters: rotation and translation vector             */
/*    1) SIFT or OTHER feature detection and matching                   */
/*    2) use lev-mar optimization(RMS of matched feature points)        */
/************************************************************************/
#pragma once
#ifndef _FEATURE_BASED_OPTIMIZATION
#define _FEATURE_BASED_OPTIMIZATION

#include "YiPanoramaTypes.h"
#include "CameraMetadata.h"
#include "ImageWarper.h"

namespace YiPanorama {
namespace calibration{

using namespace util;
using namespace warper;

struct chessboardCorner
{
    //int chessCoords[2];  // chessboard coordinate, [0]:x(vertical), [1]:y(horizontal), left-top corner as the origin point 
    //double imageCoords[2];  // image coordinate, [0]:u(vertical), [1]:v(horizontal)
    int iX;     // chessboard coordinate, [0]:x(vertical), [1]:y(horizontal), left-top corner as the origin point 
    int iY;
    double dU;  // image coordinate, [0]:u(vertical), [1]:v(horizontal)
    double dV;
};

struct matchPoint
{// define a pair of matched points in two same size images
    double coordsInA[2];     // point in image A, [0]:u(vertical), [1]:v(horizontal)
    double coordsInB[2];     // point in image B
};

// ==========================================================================
class intrinsicParamOptimizer
{// giving the camera's all intrinsic parameters except centers
 // and capture a chessboard picture to optimize the lens optical center 
public:
    intrinsicParamOptimizer();
    ~intrinsicParamOptimizer();

    // -------------------------------------------------
    // get camera metadata from stitcher
    int setCameraMetadata();
    // find chessboard corners using OpenCV
    int findChessboardCorners(imageFrame fisheyeImage, int checkerNumH, int checkerNumV, int checkerSize);
    // using lev_mar to optimize the center of image circle
    int optimizeOptcCen(cameraMetadata *pCamera);
    // using lev_mar to optimize both image centers and affine parameters
    int optimizeOptcCenAndAffine(cameraMetadata *pCamera);

    // draw corners
    int drawCorners(char *filePath, imageFrame imageframe, double imageCenters[2]);
private:
    int cornersNum;
    chessboardCorner *pChessboardCorners;   // corner detection of each chessboard capture
};


// ==========================================================================
class extrinsicParamOptimizer
{
public:
    extrinsicParamOptimizer();
    ~extrinsicParamOptimizer();

    // -------------------------------------------------
    // get camera metadata from stitcher
    int setCameraMetadata();
    // find matched points in image pairs
    int findMatchPoints(imageFrame imgA, imageFrame imgB, matchPoint *pMatchPoints, int &matchPointsMinNum, bool useRansac, int neighborThreshold);
    int drawPoints(char *folderPath, imageFrame imgA, imageFrame imgB, matchPoint *matchedPoints, int pointNum);
    int drawPairs(char *fileName, imageFrame imgA, imageFrame imgB, matchPoint *matchedPoints, int pointNum, bool isVertical);

    // merge all sections of matched points
    int setMatchPoints(matchPoint *matchPointL, int numL, matchPoint *matchPointM, int numM, matchPoint *matchPointR, int numR, double offsetCoords);
    int prepareMatchPoints(ImageWarper *pImageWarpTable, int sphereRadius, cameraMetadata *camera);
    int clean();
    
    // using lev_mar to optimize the extrinsic
    // optimize one(back) camera's all 6 extrinsic DOF
    int optimizeExtrinsicParams(int sphereRadius, ImageWarper *pImageWarpTable, cameraMetadata *pCamera);
    // optimize one(back) camera's 3 extrinsic DOF(only rotation, translation is fixed)
    int optimizeExtrinsicParamsFixT(int sphereRadius, ImageWarper *pImageWarpTable, cameraMetadata *pCamera);
    // optimize one(back) camera's 3 extrinsic DOF(only rotation), and both cameras intrinsic adjust coefficient
    int optimizeExtrnAndIntrnParams(int sphereRadius, ImageWarper *pImageWarpTables, cameraMetadata *pCameras, ocamModel *pOcamCalibLinear);
    // optimize the radius sphere according to current scene
    int optimizeSphereRadius(int sphereRadius, ImageWarper *pImageWarpTables, cameraMetadata *pCameras);
//private:
    //cameraMetadata mCameraMetadata[2];  // the camera pair
    int mPointNum;
    matchPoint *pMatchPoints;           // matched points coordinates
};


class extrinsicParamRelativeCalculator
{
public:
    extrinsicParamRelativeCalculator();
    ~extrinsicParamRelativeCalculator();

    // -------------------------------------------------
    // get camera metadata from stitcher
    int setCameraMetadata();

    // calculation of the extrinsic parameters between 2 lenses
    int relativeExtParamCalc(cameraMetadata cameras[2]);

};

}   // namespace calibration
}   // namespace YiPanorama

#endif  //!_FEATURE_BASED_OPTIMIZATION
