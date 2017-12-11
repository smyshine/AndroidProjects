
#include "CameraMetadata.h"
#include "MatrixVectors.h"

#include <string.h>
#include <stdio.h>
#include <math.h>

#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4

namespace YiPanorama {
namespace calibration {

using namespace util;

cameraMetadata::cameraMetadata()
{
}

cameraMetadata::~cameraMetadata()
{
}

//------------------------------------------------------------------------------
int cameraMetadata::setFromFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int camIdx)
{
    memcpy(&mOcamModel, &pFisheyePanoParams->staOcamModels[camIdx], sizeof(ocamModel));

    memcpy(mExtWorld2Cam.rotationMtx, pFisheyePanoParams->staExtParam[camIdx].rotationMtx, sizeof(double) * EXT_PARAM_R_MTX_NUM);
    memcpy(mExtWorld2Cam.translateVec, pFisheyePanoParams->staExtParam[camIdx].translateVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);

    // calculating cam2sph extrinsic parameters
    contraryRotationMtx(mExtWorld2Cam.rotationMtx, mExtCam2World.rotationMtx, 3);

    matrixDotMul(mExtCam2World.rotationMtx, mExtWorld2Cam.translateVec, mExtCam2World.translateVec, 3, 3, 1);

    for (int k = 0; k < EXT_PARAM_T_VEC_NUM; k++)
    {
        mExtCam2World.translateVec[k] = -mExtCam2World.translateVec[k];
    }

    return 0;
}

int cameraMetadata::setToFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int camIdx)
{
    memcpy(&pFisheyePanoParams->staOcamModels[camIdx], &mOcamModel, sizeof(ocamModel));

    memcpy(pFisheyePanoParams->staExtParam[camIdx].rotationMtx, mExtWorld2Cam.rotationMtx, sizeof(double) * EXT_PARAM_R_MTX_NUM);
    memcpy(pFisheyePanoParams->staExtParam[camIdx].translateVec, mExtWorld2Cam.translateVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::loadOcamModel(char *filename)
{
    // load camera parameter file which was calibrated in matlab program.
    double *pol = mOcamModel.pol;
    double *invpol = mOcamModel.invpol;
    double *xc = &(mOcamModel.uc);
    double *yc = &(mOcamModel.vc);
    double *c = &(mOcamModel.c);
    double *d = &(mOcamModel.d);
    double *e = &(mOcamModel.e);
    int    *width = &(mOcamModel.width);
    int    *height = &(mOcamModel.height);
    int    *length_pol = &(mOcamModel.length_pol);
    int    *length_invpol = &(mOcamModel.length_invpol);
    FILE *f;
    char buf[CMV_MAX_BUF];
    int i;

    //Open file
    f = fopen(filename, "r");
    if (f == NULL) {
        return -1;
    }

    //Read polynomial coefficients
    fgets(buf, CMV_MAX_BUF, f);
    fscanf(f, "\n");
    fscanf(f, "%d", length_pol);
    for (i = 0; i < *length_pol; i++)
    {
        fscanf(f, " %lf", &pol[i]);
    }

    //Read inverse polynomial coefficients
    fscanf(f, "\n");
    fgets(buf, CMV_MAX_BUF, f);
    fscanf(f, "\n");
    fscanf(f, "%d", length_invpol);
    for (i = 0; i < *length_invpol; i++)
    {
        fscanf(f, " %lf", &invpol[i]);
    }

    //Read center coordinates
    fscanf(f, "\n");
    fgets(buf, CMV_MAX_BUF, f);
    fscanf(f, "\n");
    fscanf(f, "%lf %lf\n", xc, yc);

    //Read affine coefficients
    fgets(buf, CMV_MAX_BUF, f);
    fscanf(f, "\n");
    fscanf(f, "%lf %lf %lf\n", c, d, e);

    //Read image size
    fgets(buf, CMV_MAX_BUF, f);
    fscanf(f, "\n");
    fscanf(f, "%d %d", height, width);

    fclose(f);
    return 0;
}

//------------------------------------------------------------------------------
void cameraMetadata::img2cam(double cam[3], double img[2])
{
    double *pol = mOcamModel.pol;
    double xc = (mOcamModel.uc);
    double yc = (mOcamModel.vc);
    double c = (mOcamModel.c);
    double d = (mOcamModel.d);
    double e = (mOcamModel.e);
    int length_pol = (mOcamModel.length_pol);
    double invdet = 1 / (c - d*e); // 1/det(A), where A = [c,d;e,1] as in the Matlab file

    double xp = invdet*((img[0] - xc) - d*(img[1] - yc));
    double yp = invdet*(-e*(img[0] - xc) + c*(img[1] - yc));

    double r = sqrt(xp*xp + yp*yp); //distance [pixels] of  the point from the image center
    double zp = pol[0];
    double r_i = 1;
    int i;

    for (i = 1; i < length_pol; i++)
    {
        r_i *= r;
        zp += r_i*pol[i];
    }

    //normalize to unit norm
    double invnorm = 1 / sqrt(xp*xp + yp*yp + zp*zp);

    cam[0] = invnorm*xp;
    cam[1] = invnorm*yp;
    cam[2] = invnorm*zp;
}

//------------------------------------------------------------------------------
void cameraMetadata::cam2img(double img[2], double cam[3])
{// actually this function transforms camera coords to image coords.
    double *invpol = mOcamModel.invpol;
    double xc = (mOcamModel.uc);
    double yc = (mOcamModel.vc);
    double c = (mOcamModel.c);
    double d = (mOcamModel.d);
    double e = (mOcamModel.e);
    int    width = (mOcamModel.width);
    int    height = (mOcamModel.height);
    int length_invpol = (mOcamModel.length_invpol);
    double norm = sqrt(cam[0] * cam[0] + cam[1] * cam[1]);
    double theta = atan(cam[2] / norm);
    double t, t_i;
    double rho, x, y;
    double invnorm;
    int i;

    if (norm != 0)
    {
        invnorm = 1 / norm;
        t = theta;
        rho = invpol[0];
        t_i = 1;

        for (i = 1; i < length_invpol; i++)
        {
            t_i *= t;
            rho += t_i*invpol[i];
        }

        x = cam[0] * invnorm*rho;
        y = cam[1] * invnorm*rho;

        img[0] = x*c + y*d + xc;
        img[1] = x*e + y + yc;
    }
    else
    {
        img[0] = xc;
        img[1] = yc;
    }
    return;
}

//------------------------------------------------------------------------------
int cameraMetadata::calcSphereRadius()
{
    float iResult = 0;
    double cam[3], img[2];

    cam[0] = 100; // just a random value
    cam[1] = 100;
    cam[2] = 0;
    cam2img(img, cam);
    iResult = sqrt((img[0] - mOcamModel.uc)*(img[0] - mOcamModel.uc) + (img[1] - mOcamModel.vc)*(img[1] - mOcamModel.vc));

    return (int)iResult;
}

//------------------------------------------------------------------------------
double cameraMetadata::inrayAngleCam(double cam[3])
{   // give a points coords in camera coordinate system,
    // calculate its in-ray's angle(degree) with the axis

    double result;
    result = sqrt(cam[0] * cam[0] + cam[1] * cam[1] + cam[2] * cam[2]);
    result = (-cam[2]) / result;
    result = acos(result);
    result = result * 180 / M_PI;
    return result;
}

//------------------------------------------------------------------------------
double cameraMetadata::vignettCorrectionFactor(double img[2])
{// give a point's image coordinate, calculate its vignetting correction factor
 // using a curved power function as the math model
    double result = 0.0;
    return result;
}

double cameraMetadata::setVcfFactors(double *pVcfFactors)
{
    memcpy(mOcamModel.vcf_factors, pVcfFactors, sizeof(double)* VCF_FACTOR_NUM);
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::setRotatMtxEuler(double z1, double y, double z2)
{

    genRotationMtxZYZ(mExtCam2World.rotationMtx, z1, y, z2);
    genContraryRotationMtxZYZ(mExtWorld2Cam.rotationMtx, z1, y, z2);

    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::setTransVec(double *T, extParamType paramType)
{
    double transVec[EXT_PARAM_T_VEC_NUM];

    switch (paramType)
    {
    case extCam2World:
        setTranslateVecXYZ(mExtCam2World.translateVec, T, EXT_PARAM_T_VEC_NUM);
        for (int k = 0; k < EXT_PARAM_T_VEC_NUM; k++)
        {
            transVec[k] = -T[k];
        }

        matrixDotMul(mExtWorld2Cam.rotationMtx, transVec, mExtWorld2Cam.translateVec, 3, 3, 1);
        break;

    case extWorld2Cam:// T represents the displacement of world origin to camera origin in camera coordinate system
        setTranslateVecXYZ(mExtWorld2Cam.translateVec, T, EXT_PARAM_T_VEC_NUM);
        for (int k = 0; k < EXT_PARAM_T_VEC_NUM; k++)
        {
            transVec[k] = -T[k];
        }

        matrixDotMul(mExtCam2World.rotationMtx, transVec, mExtCam2World.translateVec, 3, 3, 1);
        break;

    default:
        break;
    }

    return 0;
}

int cameraMetadata::setRotMtx(double *R, extParamType paramType)
{
    switch (paramType)
    {
    case extCam2World:
        memcpy(mExtCam2World.rotationMtx, R, sizeof(double) * EXT_PARAM_R_MTX_NUM);
        contraryRotationMtx(mExtCam2World.rotationMtx, mExtWorld2Cam.rotationMtx, 3);
        break;

    case extWorld2Cam:
        memcpy(mExtWorld2Cam.rotationMtx, R, sizeof(double) * EXT_PARAM_R_MTX_NUM);
        contraryRotationMtx(mExtWorld2Cam.rotationMtx, mExtCam2World.rotationMtx, 3);
        break;

    default:
        break;
    }
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::cam2sph(int radius, double sph[3], double cam[3])
{// the radius of the camera sphere does NOT equals to sphera radius
    double camera[3], norm;
    double midCam[3], midT[3];
    norm = 1 / sqrt(cam[0] * cam[0] + cam[1] * cam[1] + cam[2] * cam[2]);

    for (int k = 0; k < 3; k++)
    {// normalize camera coordinates
        camera[k] = cam[k] * norm;
        midT[k] = mExtCam2World.translateVec[k];
    }
    matrixDotMul(mExtCam2World.rotationMtx, camera, midCam, 3, 3, 1);
    //matrixDotMul(mExtCam2World.rotationMtx, mExtCam2World.translateVec, midT, 3, 3, 1);

    double a = midCam[0] * midCam[0] + midCam[1] * midCam[1] + midCam[2] * midCam[2];
    double b = 2 * (midCam[0] * midT[0] + midCam[1] * midT[1] + midCam[2] * midT[2]);
    double c = midT[0] * midT[0] + midT[1] * midT[1] + midT[2] * midT[2] - radius * radius;
    double delta = b * b - 4 * a * c;
    double d1, d2;
    if (delta >= 0)
    {
        d1 = (-b + sqrt(delta)) / (2 * a);
        d2 = (-b - sqrt(delta)) / (2 * a);
    }

    sph[0] = d1 * midCam[0] + midT[0];
    sph[1] = d1 * midCam[1] + midT[1];
    sph[2] = d1 * midCam[2] + midT[2];

    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::sph2cam(int radius, double cam[3], double sph[3])
{
    double sphere[3], norm;
    norm = 1 / sqrt(sph[0] * sph[0] + sph[1] * sph[1] + sph[2] * sph[2]);

    for (int k = 0; k < 3; k++)
    {
        sphere[k] = radius * sph[k] * norm;
    }

    matrixDotMul(mExtWorld2Cam.rotationMtx, sphere, cam, 3, 3, 1);
    vectorAdd(cam, mExtWorld2Cam.translateVec, cam, 3);

    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::cam2chess(double chessbrd[2], double cam[3])
{
    double RT[9], world[3];
    double tScale;
    
#if 0
    for (int k = 0; k < 3; k++)
    {
        RT[3 * k + 0] = mExtCam2Sph.rotationMtx[3 * k + 0];
        RT[3 * k + 1] = mExtCam2Sph.rotationMtx[3 * k + 1];
        RT[3 * k + 2] = mExtCam2Sph.translateVec[k];
    }

    matrixDotMul(RT, cam, world, 3, 3, 1);

    for (int k = 0; k < 3; k++)
    {
        world[k] /= world[2];
    }

#else
    matrixDotMul(mExtCam2World.rotationMtx, cam, world, 3, 3, 1);
    tScale = -mExtCam2World.translateVec[2] / world[2];
    for (int k = 0; k < 3; k++)
    {
        world[k] *= tScale;
        world[k] += mExtCam2World.translateVec[k];
    }

#endif

    memcpy(chessbrd, world, sizeof(double) * 2);
    return 0;
}

int cameraMetadata::chess2cam(double chessbrd[2], double cam[3])
{
    double world[3];
    world[0] = chessbrd[0];
    world[1] = chessbrd[1];
    world[2] = 0;

    matrixDotMul(mExtWorld2Cam.rotationMtx, world, cam, 3, 3, 1);
    vectorAdd(cam, mExtWorld2Cam.translateVec, cam, 3);

    return 0;
}


//------------------------------------------------------------------------------
int cameraMetadata::getCam2WorldTransVec(double *T)
{
    memcpy(T, mExtCam2World.translateVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    return 0;
}

int cameraMetadata::getCam2WorldRotMtx(double *R)
{
    memcpy(R, mExtCam2World.rotationMtx, sizeof(double)*EXT_PARAM_R_MTX_NUM);
    return 0;
}

int cameraMetadata::getWorld2CamTransVec(double *T)
{
    memcpy(T, mExtWorld2Cam.translateVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    return 0;
}

int cameraMetadata::getWorld2CamRotMtx(double *R)
{
    memcpy(R, mExtWorld2Cam.rotationMtx, sizeof(double)*EXT_PARAM_R_MTX_NUM);
    return 0;
}


// ocam model related
int cameraMetadata::getPol(double *pol)
{
    memcpy(pol, mOcamModel.pol, sizeof(double) * POL_LENGTH);
    return 0;
}

int cameraMetadata::getInterParams(double *interParams)
{
    interParams[0] = mOcamModel.c;
    interParams[1] = mOcamModel.d;
    interParams[2] = mOcamModel.e;
    interParams[3] = mOcamModel.uc;
    interParams[4] = mOcamModel.vc;
    return 0;
}
int cameraMetadata::setInterParams(double *interParams)
{
    mOcamModel.c = interParams[0]; 
    mOcamModel.d = interParams[1]; 
    mOcamModel.e = interParams[2]; 
    mOcamModel.uc = interParams[3]; 
    mOcamModel.vc = interParams[4]; 
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::setImageCenters(double imageCenters[2])
{
    mOcamModel.uc = imageCenters[0];
    mOcamModel.vc = imageCenters[1];
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::getImageCenters(double imageCenters[2])
{
    imageCenters[0] = mOcamModel.uc;
    imageCenters[1] = mOcamModel.vc;
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::setOcamModel(ocamModel *pOcamModel)
{
    memcpy(&mOcamModel, pOcamModel, sizeof(ocamModel));
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::getOcamModel(ocamModel *pOcamModel)
{
    memcpy(pOcamModel, &mOcamModel, sizeof(ocamModel));
    return 0;
}

//------------------------------------------------------------------------------
int cameraMetadata::getOcamImgW()
{
    return mOcamModel.width;
}

//------------------------------------------------------------------------------
int cameraMetadata::getOcamImgH()
{
    return mOcamModel.height;
}

int cameraMetadata::rotateExtrinsicParamsZYZ(double z1, double y, double z2)
{
    double rotateMtx[9], tmpRotMtx[9], tmpTranVec[3];
    genContraryRotationMtxZYZ(rotateMtx, z1, y, z2);

    memcpy(tmpRotMtx, mExtCam2World.rotationMtx, sizeof(double) * EXT_PARAM_R_MTX_NUM);
    matrixDotMul(rotateMtx, tmpRotMtx, mExtCam2World.rotationMtx, 3, 3, 3);   // cam to world rot mtx

    contraryRotationMtx(mExtCam2World.rotationMtx, mExtWorld2Cam.rotationMtx, 3);   // world to cam rot mtx

    memcpy(tmpTranVec, mExtCam2World.translateVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    matrixDotMul(rotateMtx, tmpTranVec, mExtCam2World.translateVec, 3, 3, 1);   // cam to world trans vector

    for (int k = 0; k < EXT_PARAM_T_VEC_NUM; k++)
    {
        tmpTranVec[k] = -mExtCam2World.translateVec[k];
    }
    matrixDotMul(mExtWorld2Cam.rotationMtx, tmpTranVec, mExtWorld2Cam.translateVec, 3, 3, 1);   // world to cam translate vector

    return 0;
}

int cameraMetadata::setExtParamsFromRT(double *RT)
{// RT is the extrinsic parameter of chessboard against camera

    calcInvExtMatrix(RT, mExtWorld2Cam.rotationMtx, mExtWorld2Cam.translateVec, mExtCam2World.rotationMtx, mExtCam2World.translateVec);

    return 0;
}

}   // namespace calibration
}   // namespace YiPanorama
