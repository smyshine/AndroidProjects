/************************************************************************/
/* Fish eye panorama parameters                                         */
/************************************************************************/

#pragma once
#ifndef _FISHEYE_PANO_PARAMS
#define _FISHEYE_PANO_PARAMS

#define CMV_MAX_BUF 1024
#define POL_LENGTH 5        
#define POL_LENGTH_INV 10       // restrict length, need curve fitting
#define EXT_PARAM_R_MTX_NUM 9	
#define EXT_PARAM_T_VEC_NUM 3
#define VCF_FACTOR_NUM      9

#define RESERVED_PARAMS_NUM 32

namespace YiPanorama {

// structure definitions ========================================================
struct ocamModel
{// this is the intrinsic parameter of the omni camera
    int length_pol;                // length of polynomial
    double pol[POL_LENGTH];    // the polynomial coefficients: pol[0] + x"pol[1] + x^2*pol[2] + ... + x^(N-1)*pol[N-1]
    int length_invpol;             // length of inverse polynomial
    double invpol[POL_LENGTH_INV]; // the coefficients of the inverse polynomial
    double uc;         // row coordinate of the center, vertical
    double vc;         // column coordinate of the center, horizontal
    double c;          // affine parameter
    double d;          // affine parameter
    double e;          // affine parameter
    int width;         // image width
    int height;        // image height
    double vcf_factors[VCF_FACTOR_NUM];
};

struct extParam
{// this is the extrinsic parameter of the camera
    double rotationMtx[EXT_PARAM_R_MTX_NUM];	// rotation matrix, 3x3
    double translateVec[EXT_PARAM_T_VEC_NUM];	// translate vector, 3x1
};

struct fisheyePanoParamsCore    // the core parameters of a panorama
{
    int fisheyeImgW;    // input fisheye image size, depend on the choice of resolution and complex
    int fisheyeImgH;    // high and low bit rate
            
    int panoImgW;       // destiny panoramic image size, for onboard DSP only, mobile and desktop app should assign a new value
    int panoImgH;       // high and low bit rate

    int sphereRadius;   // viewing sphere radius, objects on this distance should be perfectly aligned.
                        // this is IMPORTANT, and it determines the final result.
    float maxFovAngle;  // maximum used fov angle of the fisheye camera. 
};                      


// the parameters should be:
// 1. send from the calibration tool on PC to the device
// 2. saved into ROM by the device
struct fisheyePanoParams    
{
    fisheyePanoParamsCore stFisheyePanoParamsCore;
    ocamModel staOcamModels[2];      // 2 cameras will have different omni-camera parameters
    //ocamModelCenter staOcamModelCenter[2];  // and each camera has its own optical center
    extParam staExtParam[2];                // each camera has an unique extrinsic parameter, 
                                            // describing world against camera rotation and translation
    int cropOffsetF[2];             // sensor crop offset for front camera
    int cropOffsetR[2];             // sensor crop offset for rear camera
    double reserved[RESERVED_PARAMS_NUM];    // reserved space for parameters
                            // and the total size of this structure is 1024 bytes/128 doubles
};

//#ifdef _WIN32

enum dataFileType
{
    txt,
    dat
};

int readFisheyePanoParamsFromFile(const char *filePath, fisheyePanoParams *pFisheyePanoParams);
int writeFisheyePanoParamsToFile(char *filePath, char *fileName, fisheyePanoParams *pFisheyePanoParams, dataFileType type);

//#endif

}   // namespace YiPanorama

#endif  // !_FISHEYE_PANO_PARAMS