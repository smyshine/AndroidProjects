/************************************************************************/
/* The stitcher has complete functionality, including:                  */
/* 1) calibrating, camera parameter optimization                        */
/* 2) warp table generating(full and sparse tables)                     */
/* 3) different warper(soft, opencl, opengl)                            */
/************************************************************************/
#pragma once
#ifndef _FISHEYE_PANO_STITCHER_COMP_H
#define _FISHEYE_PANO_STITCHER_COMP_H


#include "ImageWarper.h"
#include "ImageColorAdjuster.h"
#include "ImageBlender.h"

//#define STITCH_EDGE

namespace YiPanorama {
namespace fisheyePano {

using namespace calibration;
using namespace warper;
using namespace util;

enum complexLevel
{
    advanced,
    normal,
    simple,
    calib
};


enum extParamType
{
	extCam2World,
	extWorld2Cam
};


// OpenGL rendering (reserved)
struct DescriptorGL
{
	GLint heightSrc, widthSrc; // image frame width, height;
	GLint heightDst, widthDst;
	GLchar *vertexShaderWarpSrc;
	GLchar *fragmentShaderWarpSrc;
	GLchar *vertexShaderColorAdjSrc;
	GLchar *fragmentShaderColorAdjSrc;
	GLchar *vertexShaderBlenderSrc;
	GLchar *fragmentShaderBlenderSrc;
	GLuint framebuffer;
	GLuint curTexBuf;
	GLenum attachmentpoints;
	GLuint textureColorBuffer;
	GLuint pixelBuffer;
	GLuint texture;
	GLuint texture1;
	GLuint texture2;
	GLuint textureMask;
	GLuint textureAdjCoef;
	GLuint VAO, VBO, EBO;
	GLuint64 frameCount;
	Shader shaderWarp;
	Shader shaderColorAdj;
	Shader shaderBlender;
	GLuint pbosWrite[2];
	GLuint pbosRead[2];
	GLuint curPBOWrite;
	GLuint curPBORead;
	GLuint nBytesSrc;
	GLuint nBytesDst;

	GLfloat *vertices; //pointer to vertices' data (position(x, y), texture(x, y), veg;
	GLuint *indices; //pointer to vertices' index data for opengl draw;
	GLuint vertcesAmount;
	GLuint indicesAmount;
	GLfloat *coefRGB;
	GLubyte *pMask[4];
};

class fisheyePanoStitcherComp
{
public:
    fisheyePanoStitcherComp();
    ~fisheyePanoStitcherComp();

    // choose to initialize from default file or media file metadata
    //int init(char *filePath, complexLevel ComplexLevel, int panoW, int panoH);
    int init(fisheyePanoParams *pFisheyePanoParams, complexLevel ComplexLevel, int panoW, int panoH);

    int updateFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams);
    int updateFisheyeCenters(fisheyePanoParams *pFisheyePanoParams);
    int updateWarpers();

    int imageStitch(imageFrame fisheyeImage[2], imageFrame panoImage);  // warping, color adjusting, blending, extra warping


    int intrinsicCalibration(int camIdx, imageFrame fisheyeImage, int checkerNumH, int checkerNumV, int checkerSize, bool drawResults, char *filePath);
    int getImageCenters(double centersF[2], double centersB[2]);
    int setImageCenters(double centersF[2], double centersB[2]);

    // extrinsic calibration only works under BASIC complex level
    int extrinsicCalibration(imageFrame fisheyeImage[2], bool drawResults, char *filePath, ocamModel *pOcamCalibLinear);

    int dinit();
    complexLevel mComplexLevel;

private:

    int setFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH); // metadata from picture/video

    int setWorkParams(complexLevel ComplexLevel);    // fisheyePanoParamsCore and cameraMetadatas
    int setWarpers();       // check warp device and generate warp tables
	int initBlender(ImageBlender *pImageBlender, int sizeW, int sizeH); // initial imageBlender;
	int setBlendMask(ImageBlender *pImageBlender, char *maskFilePath, int panoW, int panoH); // load mask for imageBlender;
    int setWorkMems();      // image and roi memories

    int clean();

    int visualizeTable(char *filePath, int srcImgW, int srcImgH);
    int drawStitchingEdge(imageFrame panoImage);    // for test only

	int initWarpVerticesGL(ImageWarper *pImageWarper, DescriptorGL *pDescriptorGL); // produce vertices data and VAO / VBO / EBO
	int deInitWarpVerticesGL(DescriptorGL *pDescriptorGL);
	int initWarpGL(ImageWarper *pImageWarper, DescriptorGL *pDescirptorGL);
	int warpImageGL(ImageWarper *pImageWarper, DescriptorGL *pDescirptorGL, imageFrame *srcImage, imageFrame *proImage);
	int initColAdjBlendGL(DescriptorGL *pDescriptorGL);
	int deInitColAdjBlendGL(DescriptorGL *pDescriptorGL);
	int initColorAdjCoefGL(colorAdjustTarget *pColorAdjTarget, ImageBlender *pImageBlender, DescriptorGL *pDescriptorGL);
	int deInitColorAdjCoefGL(DescriptorGL *pDescriptorGL);
	int colorAdjustRGBChnScanlineGL(imageFrame *pImageFrame, colorAdjustTarget *pColorAdjTarget, colorAdjusterPair *pColorAdjPair, ImageBlender *pImageBlender, DescriptorGL *pDescriptorGL);
	int deinitWarpGL(DescriptorGL *pDescirptorGL);
	int storePanoImage(imageFrame *renderResult, imageFrame *panoImage);
    // params from metadata
    fisheyePanoParams mFisheyePanoParams;   // this struct only for initialize from metadata/default file
                                            // should not be used at any other places

    // params for internal works which actually are used when generating warp tables
    fisheyePanoParamsCore mFisheyePanoParamsCore;
	cameraMetadata mCameraMetadata[2];
    double mSphereRotMtx[EXT_PARAM_R_MTX_NUM];
    int mSeamWidth;

    // for stitch
	ImageWarper mImageWarperB[8];
	imageFrame warpedImageB[8];
    imageFrame *pSeamImages;
    imageFrame pPanoImage;   // just a pointer, no memories allocated for this
    int mSeamRoisNum;
    // parameters below should be able to calculated/initialized from the fisheyePanoParamsCore and cameraMetadata
    imageRoi *pSeamRois;  // for each projected image may has several seam images
    
    imageRoi simpleBoneRoi[4];   // for simple stitching only
    imageFrame simpleBone;
    
    //imageRoi colorRois;
    colorAdjusterPair mColorAdjusterPair;

    ImageBlender mImageBlender[4];     // for mobile

    unsigned char *pProjImgData;
    unsigned char *pSeamImgData;

	// device for opengl, for stitch / color adjust / blend
	bool openGLStitch;
	DescriptorGL mDescriptorGL;
};


}   // namespace YiPanorama 
}   // namespace fisheyePano

#endif  //!_FISHEYE_PANO_STITCHER_COMP_H