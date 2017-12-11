
/************************************************************************/
/* camera model related: distortion factors                             */
/*					   : coordinates transformation                     */
/************************************************************************/

#pragma once

#ifndef _CAMERA_METADATA_H
#define _CAMERA_METADATA_H

#include "FisheyePanoParams.h"

namespace YiPanorama {
namespace calibration {

// structure definitions ========================================================
//struct ocamModel
//{// this is the intrinsic parameter of the omni camera
//    int length_pol;                // length of polynomial
//    double pol[POL_LENGTH];    // the polynomial coefficients: pol[0] + x"pol[1] + x^2*pol[2] + ... + x^(N-1)*pol[N-1]
//    int length_invpol;             // length of inverse polynomial
//    double invpol[POL_LENGTH_INV]; // the coefficients of the inverse polynomial
//    double uc;         // row coordinate of the center
//    double vc;         // column coordinate of the center
//    double c;          // affine parameter
//    double d;          // affine parameter
//    double e;          // affine parameter
//    int width;         // image width
//    int height;        // image height
//};
//
//struct ocamVCF
//{
//    double vcf_factors[VCF_FACTOR_NUM];
//};

enum extParamType
{
    extCam2World,
    extWorld2Cam
};

class cameraMetadata
{
public:
    cameraMetadata();
    ~cameraMetadata();

    int setFromFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int camIdx);
    int setToFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int camIdx);

    int loadOcamModel(char *filename);

    // transform coordinates from image(2d) to camera(3d)
    void img2cam(double cam[3], double img[2]);

    // transform coordinates from camera(3d) to image(2d)
    void cam2img(double img[2], double cam[3]);

    // calculate the image circle representing the 180 degree fov, and this radius has unit of pixels
    int calcSphereRadius();

    // giving a point's coordinates in camera coordinate system, calculate its in-ray angle with the camera's axis
    double inrayAngleCam(double cam[3]);

    // giving a point's coordinates in image coordinate system, calculate its vignette correction factor(always bigger than 1)
    double vignettCorrectionFactor(double img[2]);

    // get vignette correction factors
    double setVcfFactors(double *pVcfFactors);

    // set rotation extrinsic parameters , these 3 angles are yaw/pitch/roll angles in camera local reference coordinate system
    int setRotatMtxEuler(double z1, double y, double z2);

    // set translation extrinsic parameter, these 3 translations are in camera local reference coordinate system
    int setRotMtx(double *R, extParamType paramType);
    int setTransVec(double *T, extParamType paramType);

    // transform coordinates from camera(3d) to sphere(3d), radius in millimeters is needed.
    int cam2sph(int radius, double sph[3], double cam[3]);
    // transform coordinates from camera(3d) to chessboard plane(2d, because z==0).
    int cam2chess(double chessbrd[2], double cam[3]);
    int chess2cam(double chessbrd[2], double cam[3]);

    // transform coordinates from sphere(3d) to camera(3d), radius in millimeters is needed.
    int sph2cam(int radius, double cam[3], double sph[3]);

    // get ext params
    int getCam2WorldTransVec(double *T);
    int getCam2WorldRotMtx(double *R);
    int getWorld2CamTransVec(double *T);
    int getWorld2CamRotMtx(double *R);

    // ocam model related
    int getPol(double *pol);
    int setInterParams(double *interParams);
    int getInterParams(double *interParams);
    int setImageCenters(double imageCenters[2]);
    int getImageCenters(double imageCenters[2]);
    int setOcamModel(ocamModel *pOcamModel);
    int getOcamModel(ocamModel *pOcamModel);

    // get ocam parameters original image width and height
    int getOcamImgW();
    int getOcamImgH();

    int rotateExtrinsicParamsZYZ(double z1, double y, double z2);
    int setExtParamsFromRT(double *RT);

private:
    ocamModel mOcamModel;   // distortion lens model
    //ocamVCF mOamVCF;        // vignette correction factors
    extParam mExtCam2World;   // extrinsic parameters of camera to world
    extParam mExtWorld2Cam;   // extrinsic parameters of world to camera
};

}   // namespace calibration
}   // namespace YiPanorama

#endif  // !_CAMERA_MODEL_H