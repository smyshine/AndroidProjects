
#include "FisheyePanoStitcherComp.h"
#include "MatrixVectors.h"
#include "ImageTailor.h"

#include "ImageIOConverter.h"

//#include <glm/glm.hpp>
//#include <glm/gtc/matrix_transform.hpp>
//#include <glm/gtc/type_ptr.hpp>

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <EGL/egl.h>
#include <android/log.h>

namespace YiPanorama {
namespace fisheyePano {

#define SIGMA 4
#define SPARSE_STEP 40
#define PYRA_BRICK_CONST 16
#define PI_HALF_ANGLE   90.0
#define PI_ANGLE        180.0
#define PI_2_ANGLE      360.0
#define MATCH_POINT_DIST_THRES 30
#define M_PI       3.14159265358979323846   // pi

#define GEN_NORMAL_BLEND_MASK 0

//#define TMP_IMG_SAVE


#ifdef TMP_IMG_SAVE
    //char folder[512] = { "E:\\Solutions\\Repo_root\\VR_Algo\\Stitching\\VR_Camera\\testdata\\2880" };
    char folder[512] = { "E:\\Solutions\\Repo_root\\VR_Algo\\Stitching\\VR_Camera\\testdata\\V10" };
    char fileName[512];
    FILE *fp = NULL;

#endif
	int rgba2rgb(const GLubyte* rgbaSrc, GLubyte *rgbDst, const int width, const int height)
	{
		if (rgbaSrc == NULL || rgbDst == NULL || width < 0 || height < 0)
			return -1;

		const GLubyte *pSrcRow = rgbaSrc;
		GLubyte *pDstRow = rgbDst;
		for (int h = 0; h != height; ++h)
		{
			for (int w = 0; w != width; ++w)
			{
				pDstRow[3 * w + 0] = pSrcRow[4 * w + 0];
				pDstRow[3 * w + 1] = pSrcRow[4 * w + 1];
				pDstRow[3 * w + 2] = pSrcRow[4 * w + 2];
			}
			pSrcRow += 4 * width;
			pDstRow += 3 * width;
		}
		return 0;
	}

    fisheyePanoStitcherComp::fisheyePanoStitcherComp()
    {
		mDescriptorGL.isInitialized = GL_FALSE;
    }

    fisheyePanoStitcherComp::~fisheyePanoStitcherComp()
    {
    }



int fisheyePanoStitcherComp::init(fisheyePanoParams *pFisheyePanoParams, complexLevel ComplexLevel, int panoW, int panoH, const char* dat)
{
    setFisheyePanoParams(pFisheyePanoParams, panoW, panoH);
    setWorkParams(ComplexLevel);
    setWarpers();
    setWorkMems(dat);
    return 0;
}

int fisheyePanoStitcherComp::dinit()
{
    clean();
    return 0;
}


int fisheyePanoStitcherComp::setFisheyePanoParams(fisheyePanoParams *pFisheyePanoParams, int panoW, int panoH) // metadata from picture/video
{
    memcpy(&mFisheyePanoParams, pFisheyePanoParams, sizeof(fisheyePanoParams));
    mFisheyePanoParams.stFisheyePanoParamsCore.panoImgW = panoW;
    mFisheyePanoParams.stFisheyePanoParamsCore.panoImgH = panoH;
    return 0;
}


int fisheyePanoStitcherComp::setWorkParams(complexLevel ComplexLevel)
{// set camera and pano parameters from fisheyePanoParams
 // especially the camera extrinsic parameters will be convert into different orientation

    memcpy(&mFisheyePanoParamsCore, &mFisheyePanoParams.stFisheyePanoParamsCore, sizeof(fisheyePanoParamsCore));
    mCameraMetadata[0].setFromFisheyePanoParams(&mFisheyePanoParams, 0);
    mCameraMetadata[1].setFromFisheyePanoParams(&mFisheyePanoParams, 1);

    mComplexLevel = ComplexLevel;

    genContraryRotationMtxZYZ(mSphereRotMtx, 90, 90, -90);

    return 0;
}

int fisheyePanoStitcherComp::setWarpers()       // check warp device and generate warp tables
{
    // warp table choice of computing complex
    int projImgW, projImgH, stepX, stepY;
    float vertAngleUp, vertAngleDown, horiAngleLeft, horiAngleRight;
	int vertPixelUp[4], vertPixelDown[4], horiPixelLeft[4], horiPixelRight[4];
    float /*desiredAngle = 100.0, */anglesPerStep;

	switch (mComplexLevel)
	{
	case normal:    // front and back
		projImgW = mFisheyePanoParamsCore.panoImgW;
		projImgH = mFisheyePanoParamsCore.panoImgH;
		stepX = SPARSE_STEP;
		stepY = SPARSE_STEP;

		anglesPerStep = (PI_2_ANGLE / (mFisheyePanoParamsCore.panoImgW / SPARSE_STEP));
		horiAngleLeft = floor(2 * mFisheyePanoParamsCore.maxFovAngle / anglesPerStep) * anglesPerStep;
		mSeamWidth = mFisheyePanoParamsCore.panoImgW * (horiAngleLeft - PI_ANGLE) / PI_2_ANGLE;

		//***********************************************************//
		//            |*******************************|              //
		//            |       0       |       1       |              //
		//            |***************|***************|              //
		//            |       2       |       3       |              //
		//            |*******************************|              //
		//***********************************************************//

		vertPixelUp[0] = vertPixelUp[1] = 0;
		vertPixelUp[2] = vertPixelUp[3] = projImgH / 2;
		vertPixelDown[0] = vertPixelDown[1] = projImgH / 2;
		vertPixelDown[2] = vertPixelDown[3] = projImgH;
		horiPixelLeft[0] = horiPixelLeft[2] = 0;
		horiPixelLeft[1] = horiPixelLeft[3] = projImgW / 2;
		horiPixelRight[0] = horiPixelRight[2] =  projImgW / 2;
		horiPixelRight[1] = horiPixelRight[3] = projImgW;

		for (int i = 0; i != 8; ++i)
		{
			mImageWarperB[i].init(projImgW, projImgH, vertPixelUp[i % 4], vertPixelDown[i % 4], horiPixelLeft[i % 4], horiPixelRight[i % 4], true, stepX, stepY, false);
			mImageWarperB[i].setWarpDevice(useOpengl);
		}

// #if GEN_NORMAL_BLEND_MASK
//         // for mask generating only
//         mImageWarperB[2].init(projImgW, projImgH, vertAngleUp, vertAngleDown, horiAngleLeft, horiAngleRight, false, stepX, stepY, false);
//         mImageWarperB[2].setWarpDevice(useSoftware);
//
// #endif

		break;
    default:
        break;
    }

    // generate warp tables
	for (int i = 0; i != 4; ++i)
	{
		mImageWarperB[i].genWarperCam(&mCameraMetadata[0], mFisheyePanoParamsCore.sphereRadius);
		mImageWarperB[4 + i].genWarperCam(&mCameraMetadata[1], mFisheyePanoParamsCore.sphereRadius);

	}

	// set roi in source image
	mImageWarperB[0].setSrcRoi(0.0, 0.0, 1.0, 0.6); //now 0 & 1 use the same TOP half source image and 2 & 3 use BOTTOM half source image;
	mImageWarperB[1].setSrcRoi(0.0, 0.0, 1.0, 0.6); //this will be set in future to decrease the size of source image, as well as  4,5,6,7;
	mImageWarperB[2].setSrcRoi(0.0, 0.4, 1.0, 0.6);
	mImageWarperB[3].setSrcRoi(0.0, 0.4, 1.0, 0.6);

	mImageWarperB[4].setSrcRoi(0.0, 0.0, 1.0, 0.6);
	mImageWarperB[5].setSrcRoi(0.0, 0.0, 1.0, 0.6);
	mImageWarperB[6].setSrcRoi(0.0, 0.4, 1.0, 0.6);
	mImageWarperB[7].setSrcRoi(0.0, 0.4, 1.0, 0.6);


#if GEN_NORMAL_BLEND_MASK
    if (mComplexLevel != simple)
        mImageWarperB[2].genWarperSph(mSphereRotMtx);
#endif
    return 0;
}

int fisheyePanoStitcherComp::initBlender(ImageBlender *pImageBlender, int sizeW, int sizeH)
{
	pImageBlender->mSizeW = sizeW;
	pImageBlender->mSizeH = sizeH;

	pImageBlender->uvMaskValid = false;
	pImageBlender->pMaskY = new unsigned char[sizeW * sizeH];

	return 0;
}

int fisheyePanoStitcherComp::setBlendMask(ImageBlender *pImageBlender, const char *maskFilePath, int panoW, int panoH)
{
	FILE *fp = NULL;
	fp = fopen(maskFilePath, "rb");
	if (fp == NULL)
	{
		return -1;
	}
	unsigned char *pMask[4] = { pImageBlender[0].pMaskY, pImageBlender[1].pMaskY, pImageBlender[2].pMaskY, pImageBlender[3].pMaskY };
	for (int h = 0; h != pImageBlender[0].mSizeH; ++h)
	{
		fread(pMask[0], 1, sizeof(unsigned char) * pImageBlender[0].mSizeW, fp);
		fread(pMask[1], 1, sizeof(unsigned char) * pImageBlender[0].mSizeW, fp);
		pMask[0] += pImageBlender[0].mSizeW;
		pMask[1] += pImageBlender[0].mSizeW;
	}
	for (int h = 0; h != pImageBlender[0].mSizeH; ++h)
	{
		fread(pMask[2], 1, sizeof(unsigned char) * pImageBlender[0].mSizeW, fp);
		fread(pMask[3], 1, sizeof(unsigned char) * pImageBlender[0].mSizeW, fp);
		pMask[2] += pImageBlender[0].mSizeW;
		pMask[3] += pImageBlender[0].mSizeW;
	}

	fclose(fp);

	return 0;
}


int fisheyePanoStitcherComp::setWorkMems(const char* dat)      // image and roi memories
{
    switch (mComplexLevel)
    {
    case normal:    // front and back
        mSeamRoisNum = 8; // the seam rois in each (back & front) image are all the same;
        pSeamRois = new imageRoi[mSeamRoisNum];
        pSeamImages = new imageFrame[mSeamRoisNum];

		for (int i = 0; i != mSeamRoisNum; ++i)
		{
			pSeamRois[i].imgW = mImageWarperB[i].mWarpImageW;
			pSeamRois[i].imgH = mImageWarperB[i].mWarpImageH;
			pSeamRois[i].roiX = (mImageWarperB[i].mWarpImageW - mSeamWidth) / 2;
			pSeamRois[i].roiY = 0;
			pSeamRois[i].roiW = mSeamWidth;
			pSeamRois[i].roiH = mImageWarperB[i].mWarpImageH;
		}

		break;

    default:
        break;
    }

    // warp image memories ------------------------------------------------------
    int singleChnProjImgSizeA = mImageWarperB[0].mWarpImageW * mImageWarperB[0].mWarpImageH;
	int singleChnProjImgSizeB = singleChnProjImgSizeA;
    pProjImgData = new unsigned char[singleChnProjImgSizeA * 3 * 4 + singleChnProjImgSizeB * 3 * 4];// for 2 projected images in RGB, each pano image is divided in 4 subimage;
    memset(pProjImgData, 128, singleChnProjImgSizeA * 3 * 4 + singleChnProjImgSizeB * 3 * 4);

	for (int i = 0; i != 8; ++i)
	{
		warpedImageB[i].imageW = mImageWarperB[i].mWarpImageW;
		warpedImageB[i].imageH = mImageWarperB[i].mWarpImageH;
		warpedImageB[i].pxlColorFormat = PIXELCOLORSPACE_RGB;
		warpedImageB[i].strides[0] = warpedImageB[i].imageW * 3;
		warpedImageB[i].strides[1] = 0;
		warpedImageB[i].strides[2] = 0;
	}

    pPanoImage.imageW = mFisheyePanoParamsCore.panoImgW;
    pPanoImage.imageH = mFisheyePanoParamsCore.panoImgH;
    pPanoImage.pxlColorFormat = PIXELCOLORSPACE_RGB;
    pPanoImage.strides[0] = pPanoImage.imageW * 3;
    pPanoImage.strides[1] = 0;
    pPanoImage.strides[2] = 0;

	for (int i = 0; i != 8; ++i)
	{
		warpedImageB[i].plane[0] = pProjImgData + i * singleChnProjImgSizeA * 3;
		warpedImageB[i].plane[1] = NULL;
		warpedImageB[i].plane[2] = NULL;
	}

    pPanoImage.plane[0] = pProjImgData;
    pPanoImage.plane[1] = NULL;
    pPanoImage.plane[2] = NULL;

    // seam image memories blending
    int singleChnSeamImgSize = pSeamRois[0].roiW * pSeamRois[0].roiH;
    pSeamImgData = new unsigned char[mSeamRoisNum * singleChnSeamImgSize * 3]; // R, G, B three channels;
    memset(pSeamImgData, 128, mSeamRoisNum * singleChnSeamImgSize * 3);

    for (int k = 0; k < mSeamRoisNum; k++)
    {
        pSeamImages[k].imageW = pSeamRois[k].roiW;
        pSeamImages[k].imageH = pSeamRois[k].roiH;
        pSeamImages[k].pxlColorFormat = PIXELCOLORSPACE_RGB;
        pSeamImages[k].strides[0] = pSeamImages[k].imageW * 3;
        pSeamImages[k].strides[1] = 0;
        pSeamImages[k].strides[2] = 0;

        pSeamImages[k].plane[0] = pSeamImgData + k * singleChnSeamImgSize * 3;
        pSeamImages[k].plane[1] = NULL;
        pSeamImages[k].plane[2] = NULL;
    }

    // color adjuster
    switch (mComplexLevel)
    {
    case normal:
        mColorAdjusterPair.init(8, warpedImageB, 1, pSeamRois, 1, vertical, interleaved);
        break;

    default:
        break;
    }

    // image blender
    unsigned char *tmpMask = NULL;
    switch (mComplexLevel)
    {
    case normal:    // front and back

#if GEN_NORMAL_BLEND_MASK   // generate blend mask

        //mImageBlender.init(mImageWarperB[2].mWarpImageW, mImageWarperB[2].mWarpImageH);
        //mImageBlender.genMask(20, horiz);
        //tmpMask = new unsigned char[mImageBlender.mSizeW * mImageBlender.mSizeH];
        //mImageWarperB[2].warpImageSoftFullWithoutVCSingleChn(mImageBlender.pMaskY, tmpMask);
        //memcpy(mImageBlender.pMaskY, tmpMask, mImageBlender.mSizeW * mImageBlender.mSizeH);
        //delete[] tmpMask;
        //tmpMask = NULL;

#else   // load the pre-generated mask
		for (int i = 0; i != 4; ++i)
		{
			mImageBlender[i].init(mImageWarperB[0].mWarpImageW, mImageWarperB[0].mWarpImageH);
		}
        setBlendMask(mImageBlender, dat/*".\\normal_blend_mask.dat"*/, 5760, 2880);

#endif

        break;

    default:
        break;
    }
//
//#ifdef STITCH_EDGE
//    unsigned char *pTmpMask = NULL;
//    if (simple != mComplexLevel)
//    {
//        tmpMask = new unsigned char[mImageWarper[2].mWarpImageW * mImageWarper[2].mWarpImageH];
//        memset(tmpMask, 0, mImageWarperB[2].mWarpImageW * mImageWarperB[2].mWarpImageH);
//
//        pTmpMask = tmpMask + mImageWarperB[2].mWarpImageW * (mImageWarperB[2].mWarpImageH - mSeamWidth) / 2;
//        memset(pTmpMask, 255, mImageWarperB[2].mWarpImageW);
//        pTmpMask += mImageWarperB[2].mWarpImageW * mSeamWidth;
//        memset(pTmpMask, 255, mImageWarperB[2].mWarpImageW);
//        mImageBlender.pMaskStitchEdge = new unsigned char[mImageWarper[2].mWarpImageW * mImageWarper[2].mWarpImageH];
//        mImageWarperB[2].warpImageSoftFullWithoutVCSingleChn(tmpMask, mImageBlender.pMaskStitchEdge);
//
//        delete[] tmpMask;
//        tmpMask = NULL;
//    }
//    //else
//    //{
//    //    mImageBlender.pMaskStitchEdge = new unsigned char[pSeamRois[0].roiW, pSeamRois[0].roiH];
//    //    memset(mImageBlender.pMaskStitchEdge, 0, pSeamRois[0].roiW * pSeamRois[0].roiH);
//    //    pTmpMask = mImageBlender.pMaskStitchEdge;
//    //    int gap = pSeamRois[0].roiW - 1;
//    //    for (int k = 0; k < pSeamRois[0].roiH; k++)
//    //    {
//    //        *pTmpMask = 255;
//    //        *(pTmpMask + gap) = 255;
//    //        pTmpMask += pSeamRois[0].roiW;
//    //    }
//    //}
//#endif

    return 0;
}

int fisheyePanoStitcherComp::clean()
{
    // clean warpers
    mImageWarperB[0].dinit();
    mImageWarperB[1].dinit();
    if (simple != mComplexLevel)
    {
        mImageWarperB[2].dinit();
    }

    // clean work mems
    if (pSeamRois != NULL)
        delete[] pSeamRois;
    if (pSeamImages != NULL)
        delete[] pSeamImages;
    if (pProjImgData != NULL)
        delete[] pProjImgData;
    if (pSeamImgData != NULL)
        delete[] pSeamImgData;

    dinitImageFrame(&simpleBone);

    mColorAdjusterPair.dinit();
	for (int i = 0; i != 4; ++i)
		mImageBlender[i].dinit();
   // mOptFlow.dinit();
    return 0;
}

int fisheyePanoStitcherComp::initWarpGLES(ImageWarper *pImageWarper, DescriptorGLES *pDescriptorGLES)
{
	// check if opengles context has initialized
	if (pDescriptorGLES->isInitialized == GL_FALSE)
	{
		// create display;
		pDescriptorGLES->eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
		if (pDescriptorGLES->eglDisplay == EGL_NO_DISPLAY)
		{
			std::cout << "eglDisplay create failed, error code: " << std::endl;
			return -1;
		}
		// initial display
		if (!eglInitialize(pDescriptorGLES->eglDisplay, &pDescriptorGLES->majorVersion, &pDescriptorGLES->minorVersion))
		{
			std::cerr << "Unable to initialize EGL, handle and recover" << std::endl;
			return -1;
		}

		// create config
		EGLint configAttribList[] =
		{
			EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,// very important!
			EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
			EGL_RED_SIZE,   8,
			EGL_GREEN_SIZE, 8,
			EGL_BLUE_SIZE,  8,
			EGL_ALPHA_SIZE, 0,// if you need the alpha channel
			EGL_DEPTH_SIZE, 0,// if you need the depth buffer
			EGL_STENCIL_SIZE,0,
			EGL_NONE
		};
		EGLint numConfigs;
		if (!eglChooseConfig(pDescriptorGLES->eglDisplay, configAttribList, &pDescriptorGLES->eglConfig, 1, &numConfigs))
		{
			std::cerr << "Fail in EGL surface config" << std::endl;
			return -1;
		}

		// create surface
		// create a surface
		EGLint surfaceAttribList[] =
		{
			EGL_WIDTH, 1,
			EGL_HEIGHT, 1,
			EGL_NONE
		};
		pDescriptorGLES->eglSurface = eglCreatePbufferSurface(pDescriptorGLES->eglDisplay, pDescriptorGLES->eglConfig, surfaceAttribList);
		if (pDescriptorGLES->eglSurface == EGL_NO_SURFACE)
		{
			std::cerr << "create surface fail" << std::endl;
			switch (eglGetError())
			{
			case EGL_BAD_ALLOC:
				// Not enough resources available. Handle and recover
				std::cerr << "Not enough resources available" << std::endl;
				break;
			case EGL_BAD_CONFIG:
				// Verify that provided EGLConfig is valid
				std::cerr << "provided EGLConfig is invalid" << std::endl;
				break;
			case EGL_BAD_PARAMETER:
				// Verify that the EGL_WIDTH and EGL_HEIGHT are
				// non-negative values
				std::cerr << "provided EGL_WIDTH and EGL_HEIGHT is invalid" << std::endl;
				break;
			case EGL_BAD_MATCH:
				// Check window and EGLConfig attributes to determine
				// compatibility and pbuffer-texture parameters
				std::cerr << "Check window and EGLConfig attributes" << std::endl;
				break;
			}
			return -1;
		}

		// create context
		EGLint contextAttribList[] =
		{
			EGL_CONTEXT_CLIENT_VERSION, 3,
			EGL_NONE
		};
		pDescriptorGLES->eglContext = eglCreateContext(pDescriptorGLES->eglDisplay, pDescriptorGLES->eglConfig, EGL_NO_CONTEXT, contextAttribList);
		if (pDescriptorGLES->eglContext == EGL_NO_CONTEXT)
		{
			std::cerr << "create context fail" << std::endl;
			return -1;
		}

		// bind the context to corrent thread
		eglMakeCurrent(pDescriptorGLES->eglDisplay, pDescriptorGLES->eglSurface, pDescriptorGLES->eglSurface, pDescriptorGLES->eglContext);
		if (glGetError())
		{
			std::cout << "bind context failed" << std::endl;
		}
		pDescriptorGLES->isInitialized = GL_TRUE;
	}

	pDescriptorGLES->heightSrc = pImageWarper->mSrcImageH;
	pDescriptorGLES->widthSrc = pImageWarper->mSrcImageW;

	pDescriptorGLES->heightDst = pImageWarper[0].mWarpImageH;
	pDescriptorGLES->widthDst = pImageWarper[0].mWarpImageW;
	pDescriptorGLES->attachmentpoints = GL_COLOR_ATTACHMENT0;
	pDescriptorGLES->nBytesSrc = pImageWarper[0].mSrcImageH * pImageWarper[0].mSrcImageW * sizeof(GLubyte);
	pDescriptorGLES->nBytesDst = pImageWarper[0].mWarpImageH * pImageWarper[0].mWarpImageW * sizeof(GLubyte);


	// Shader
	// ******************************************build and compile shaders********************************
	pDescriptorGLES->vertexShaderWarpSrc = "#version 300 es\n"
		"layout(location = 0) in vec3 position;\n"
		"layout(location = 1) in vec2 texCoords;\n"
		"out vec2 TexCoords;\n"
		"out float f;\n"

		"void main()\n"
		"{\n"
		"f = position.z;\n"
		"gl_Position = vec4(position.x, -position.y, 0.0f, 1.0f);\n"
		"TexCoords = vec2(texCoords.x, texCoords.y);\n"
		"}";


	pDescriptorGLES->fragmentShaderWarpSrc = "#version 300 es\n"
		"precision mediump float;\n"
		"in vec2 TexCoords;\n"
		"in float f;\n"
		"out vec4 color;\n"
		"uniform sampler2D ourtexture;\n"

		"void main()\n"
		"{\n"
		"color = f * texture(ourtexture, TexCoords);\n"
		"color = color.bgra;"
		"}";

	pDescriptorGLES->shaderWarp.init(pDescriptorGLES->vertexShaderWarpSrc, pDescriptorGLES->fragmentShaderWarpSrc);

	pDescriptorGLES->vertexShaderColorAdjSrc = "#version 300 es\n"
		"layout(location = 0) in vec2 position;\n"
		"layout(location = 1) in vec2 texCoords;\n"
		"out vec2 TexCoords;\n"
		
		"void main()\n"
		"{\n"
		"gl_Position = vec4(position.x, -position.y, 0.0f, 1.0f);\n"
		"TexCoords = vec2(texCoords.x, texCoords.y);\n"
		"}";

	pDescriptorGLES->fragmentShaderColorAdjSrc = "#version 300 es\n"
		"precision mediump float;\n"
		"in vec2 TexCoords;\n"
		"out vec4 color;\n"
		"uniform float exploreWeight[256];\n"
		"uniform sampler2D ourtexture0;\n"
		"uniform sampler2D ourtexture1;\n"
		"uniform sampler2D ourMask;\n"
		"uniform sampler2D adjCof;\n"

		"void main()\n"
		"{\n"
		"vec4 adjTarColor = texture(ourtexture1, TexCoords);\n"
		"float Y = adjTarColor.r * 0.229 + adjTarColor.g * 0.587 + adjTarColor.b * 0.114;\n"
		"vec3 cof = 1.0 + ((texture(adjCof, vec2(1.0, TexCoords.y)) - 1.0) * exploreWeight[int(255.0 * Y)]).rgb;\n"
		//"vec3 cof = 1.0 + ((texture(adjCof, vec2(1.0, TexCoords.y)) - 1.0) * exploreWeight[int(255.0 * Y)]).rgb;\n"
		"adjTarColor = vec4(adjTarColor.rgb * cof, 1.0);\n"
		"color = mix(texture(ourtexture0, TexCoords), adjTarColor, 1.0 - texture(ourMask, TexCoords).r);\n"
		"}";

	pDescriptorGLES->shaderColorAdj.init(pDescriptorGLES->vertexShaderColorAdjSrc, pDescriptorGLES->fragmentShaderColorAdjSrc);

	// Source Texture
	glGenTextures(1, &pDescriptorGLES->texture);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, pImageWarper->mSrcImageRoi.roiW, pImageWarper->mSrcImageRoi.roiH, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glBindTexture(GL_TEXTURE_2D, 0);
	

	// Frame buffer (dst texture)
	glGenFramebuffers(1, &pDescriptorGLES->framebuffer);
	glGenRenderbuffers(1, &pDescriptorGLES->textureColorBuffer);
	//glGenTextures(1, &pDescriptorGLES->textureColorBuffer);
	glGenBuffers(1, &pDescriptorGLES->pixelBuffer);
	glBindFramebuffer(GL_FRAMEBUFFER, pDescriptorGLES->framebuffer);

    glBindRenderbuffer(GL_RENDERBUFFER, pDescriptorGLES->textureColorBuffer);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_RGB8, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, pDescriptorGLES->textureColorBuffer);

    //glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureColorBuffer);
    //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
	//glFramebufferTexture2D(GL_FRAMEBUFFER, pDescriptorGLES->attachmentpoints, GL_TEXTURE_2D, pDescriptorGLES->textureColorBuffer, 0);
	if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
        __android_log_print(ANDROID_LOG_ERROR,"libstitch","ERROR::FRAMEBUFFER::frame buffer is not complete, 2");
        std::cout << "ERROR::FRAMEBUFFER:: Framebuffer is not complete!" << std::endl;
    }

	/*glBindBuffer(GL_PIXEL_PACK_BUFFER, pDescriptorGLES->pixelBuffer);
	glBufferData(GL_PIXEL_PACK_BUFFER, (GLuint)pDescriptorGLES->heightDst * (GLuint)pDescriptorGLES->widthDst * 3 * sizeof(GL_UNSIGNED_BYTE), NULL, GL_STREAM_READ);
	glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);*/
	
	glBindFramebuffer(GL_FRAMEBUFFER, 0);


	return 0;
}

int fisheyePanoStitcherComp::deinitWarpGLES(DescriptorGLES *pDescriptorGLES)
{
	//to do deinit;
	//delete texture
	glDeleteTextures(1, &pDescriptorGLES->texture);
	//delete vbo, array buffer;
	glDeleteBuffers(1, &pDescriptorGLES->VBO);
	//delete ebo, element array buffer;
	glDeleteBuffers(1, &pDescriptorGLES->EBO);
	
	return 0;
}

int fisheyePanoStitcherComp::initWarpVerticesGLES(ImageWarper *pImageWarper, DescriptorGLES *pDescriptorGLES)
{
	int TableH = pImageWarper->mTableH;
	int TableW = pImageWarper->mTableW;
	float *PposX = pImageWarper->mPposX;
	float *PposY = pImageWarper->mPposY;
	float *Pvcfr = pImageWarper->mPvcfr;
	float *PmapX = pImageWarper->mPmapX;
	float *PmapY = pImageWarper->mPmapY;

	pDescriptorGLES->vertcesAmount = TableH * TableW * 5;
	pDescriptorGLES->indicesAmount = (TableH - 1) * (TableW - 1) * 6;
	pDescriptorGLES->vertices = new GLfloat[pDescriptorGLES->vertcesAmount];
	pDescriptorGLES->indices = new GLuint[pDescriptorGLES->indicesAmount];
	GLfloat *pVertices = pDescriptorGLES->vertices;

	float roiY = pImageWarper->mSrcImageRoi.roiY;
	float roiX = pImageWarper->mSrcImageRoi.roiX;
	float invRoiH = 1.0 / pImageWarper->mSrcImageRoi.roiH;
	float invRoiW = 1.0 / pImageWarper->mSrcImageRoi.roiW;

	if (pImageWarper->mHasVC)
		for (int i = 0; i != TableH * TableW; ++i)
		{
			*(pVertices++) = PposX[i];
			*(pVertices++) = PposY[i];
			*(pVertices++) = Pvcfr[i];
			*(pVertices++) = (PmapX[i] - roiX) * invRoiW;
			*(pVertices++) = (PmapY[i] - roiY) * invRoiH;
		}
	else
		for (int i = 0; i != pImageWarper->mTableH * pImageWarper->mTableW; ++i)
		{
			*(pVertices++) = PposX[i];
			*(pVertices++) = PposY[i];
			*(pVertices++) = 1.0f;
			*(pVertices++) = (PmapX[i] - roiX) * invRoiW;
			*(pVertices++) = (PmapY[i] - roiY) * invRoiH;
		}

	// vertexIndeices
	GLuint *pVerIdx = pDescriptorGLES->indices;
	GLuint indices[4] = { 0, 1, TableW, TableW + 1 };
	for (GLint h = 0; h != TableH - 1; ++h)
	{
		for (GLint w = 0; w != TableW - 1; ++w)
		{
			*(pVerIdx++) = indices[0];
			*(pVerIdx++) = indices[2];
			*(pVerIdx++) = indices[1];
			*(pVerIdx++) = indices[1];
			*(pVerIdx++) = indices[2];
			*(pVerIdx++) = indices[3];
			++indices[0];
			++indices[1];
			++indices[2];
			++indices[3];
		}
		++indices[0];
		++indices[1];
		++indices[2];
		++indices[3];
	}

	// VAO, VBO, EBO;
	glGenVertexArrays(1, &pDescriptorGLES->VAO);
	glGenBuffers(1, &pDescriptorGLES->VBO);
	glGenBuffers(1, &pDescriptorGLES->EBO);
	glBindVertexArray(pDescriptorGLES->VAO);
	glBindBuffer(GL_ARRAY_BUFFER, pDescriptorGLES->VBO);
	glBufferData(GL_ARRAY_BUFFER, pDescriptorGLES->vertcesAmount * sizeof(GLfloat), pDescriptorGLES->vertices, GL_STATIC_DRAW);
	glEnableVertexAttribArray(0);
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(GLfloat), (GLvoid*)0);
	glEnableVertexAttribArray(1);
	glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(GLfloat), (GLvoid*)(3 * sizeof(GLfloat)));
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pDescriptorGLES->EBO);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, pDescriptorGLES->indicesAmount * sizeof(GLuint), pDescriptorGLES->indices, GL_STATIC_DRAW);
	glBindVertexArray(0);

	delete[] pDescriptorGLES->vertices;
	delete[] pDescriptorGLES->indices;

	return 0;
}

int fisheyePanoStitcherComp::deInitWarpVerticesGLES(DescriptorGLES *pDescriptorGLES)
{
	//delete VAO, VBO, EBO;
	glDeleteBuffers(1, &pDescriptorGLES->VBO);
	glDeleteBuffers(1, &pDescriptorGLES->EBO);
	glDeleteVertexArrays(1, &pDescriptorGLES->VAO);
	
	return 0;
}

int fisheyePanoStitcherComp::warpImageGLES(ImageWarper *pImageWarper, DescriptorGLES *pDescriptorGLES, imageFrame *srcImage, imageFrame *proImage)
{	//warp image in opengl with sparse map table;

	// vertices process, include VAO
	initWarpVerticesGLES(pImageWarper, pDescriptorGLES);
	
	// bind framebuffer
	glBindFramebuffer(GL_FRAMEBUFFER, pDescriptorGLES->framebuffer);

	glClearColor(0.0f, 0.5f, 1.0f, 1.0f);
	glClear(GL_COLOR_BUFFER_BIT);

	glViewport(0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);
	
	// load image data from render to GPU;
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, pImageWarper->mSrcImageRoi.roiW, pImageWarper->mSrcImageRoi.roiH, GL_RGB, GL_UNSIGNED_BYTE, srcImage->plane[0] + pImageWarper->mSrcImageRoi.roiY * srcImage->strides[0] + pImageWarper->mSrcImageRoi.roiX * 3);
	
	// shader;
	pDescriptorGLES->shaderWarp.Use();

	// bind VAO;
	glBindVertexArray(pDescriptorGLES->VAO);

	// render
	glDrawBuffers(1, &pDescriptorGLES->attachmentpoints);
	glDrawElements(GL_TRIANGLES, pDescriptorGLES->indicesAmount, GL_UNSIGNED_INT, 0);
	
	// deattach VAO;
	glBindVertexArray(0);
	// deattach image;
	glBindTexture(GL_TEXTURE_2D, 0);

	//bind pbo;
	//glBindBuffer(GL_PIXEL_PACK_BUFFER, pDescriptorGLES->pixelBuffer);
	glReadBuffer(pDescriptorGLES->attachmentpoints);
	if (GL_NO_ERROR != glGetError())
	{
		std::cout << "ERROR::3" << std::endl;
	}

	// can not readpixel in GL_RGB format, so read rgba and transform to rgb;
	GLubyte *tempImage = new GLubyte[pDescriptorGLES->widthDst * pDescriptorGLES->heightDst * 4];
	memset(tempImage, 128, sizeof(GLubyte) * pDescriptorGLES->widthDst * pDescriptorGLES->heightDst * 4);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","warp read pixels");

	glReadPixels(0, 0, (GLuint)pDescriptorGLES->widthDst, (GLuint)pDescriptorGLES->heightDst, GL_RGBA, GL_UNSIGNED_BYTE, tempImage);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","warp rgba 2 rgb");

    rgba2rgb(tempImage, proImage->plane[0], pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);
	delete[] tempImage;
	//glFlush();
	//GLubyte *pBuffer = (GLubyte *)glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY);
	//if (pBuffer != NULL)
		//memcpy(proImage.plane[0], pBuffer, (unsigned int)pDescriptorGLES->heightDst * (unsigned int)pDescriptorGLES->widthDst * 3 * sizeof(GLubyte));

	//SOIL_save_image("Yresult.bmp", SOIL_SAVE_TYPE_BMP, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, 3, proImage->plane[0]);
	// deattach framebuffer;
	glBindFramebuffer(GL_FRAMEBUFFER, 0);

	deInitWarpVerticesGLES(pDescriptorGLES);

	return 0;
}

int fisheyePanoStitcherComp::initColAdjBlendGLES(DescriptorGLES *pDescriptorGLES)
{
	// Texture 1
	glGenTextures(1, &pDescriptorGLES->texture);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture 2
	glGenTextures(1, &pDescriptorGLES->texture1);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture1);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture Mask
	glGenTextures(1, &pDescriptorGLES->textureMask);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureMask);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, 0, GL_RED, GL_UNSIGNED_BYTE, NULL);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture coefficient
	glGenTextures(1, &pDescriptorGLES->textureAdjCoef);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureAdjCoef);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, 1, pDescriptorGLES->heightDst, 0, GL_RGB, GL_FLOAT, NULL);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glBindTexture(GL_TEXTURE_2D, 0);

	// vertex and Texture coordinate;
	GLfloat vertices[] = {
		-1.0f, 1.0f, 0.0f, 0.0f,
		-1.0f, -1.0f, 0.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 0.0f,

		1.0f, 1.0f, 1.0f, 0.0f,
		-1.0f, -1.0f, 0.0f, 1.0f,
		1.0f, -1.0f, 1.0f, 1.0f
	};

	// VAO
	glGenVertexArrays(1, &pDescriptorGLES->VAO);
	glGenBuffers(1, &pDescriptorGLES->VBO);
	glBindVertexArray(pDescriptorGLES->VAO);
	glBindBuffer(GL_ARRAY_BUFFER, pDescriptorGLES->VBO);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
	glEnableVertexAttribArray(0);
	glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
	glEnableVertexAttribArray(1);
	glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat)));
	glBindVertexArray(0);

	return 0;
}

int fisheyePanoStitcherComp::deInitColAdjBlendGLES(DescriptorGLES *pDescriptorGLES)
{//  delete textures
	glDeleteTextures(1, &pDescriptorGLES->texture);
	glDeleteTextures(1, &pDescriptorGLES->texture1);
	glDeleteTextures(1, &pDescriptorGLES->textureAdjCoef);
	glDeleteTextures(1, &pDescriptorGLES->textureMask);

	// delete buffer and vertex array
	glDeleteBuffers(1, &pDescriptorGLES->VBO);
	glDeleteVertexArrays(1, &pDescriptorGLES->VAO);

	// delete GLES
	eglMakeCurrent(pDescriptorGLES->eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
	eglDestroyContext(pDescriptorGLES->eglDisplay, pDescriptorGLES->eglContext);
	eglDestroySurface(pDescriptorGLES->eglDisplay, pDescriptorGLES->eglSurface);
	eglTerminate(pDescriptorGLES->eglDisplay);

	pDescriptorGLES->eglDisplay = EGL_NO_DISPLAY;
	pDescriptorGLES->eglSurface = EGL_NO_SURFACE;
	pDescriptorGLES->eglContext = EGL_NO_CONTEXT;

	return 0;
}

int fisheyePanoStitcherComp::initColorAdjCoefGLES(colorAdjustTarget *pColorAdjTarget, ImageBlender *pImageBlender, DescriptorGLES *pDescriptorGLES)
{// initial data for color adjust and blend, include explore weight, color coefficient, blend mask;

	//process the color coefficient;
	int coefLen = pColorAdjTarget->coeffsLen[0];
	pDescriptorGLES->coefRGB = new float[coefLen * 3]; // for R,G,B three channel;
	float *coefR = pColorAdjTarget->coeffs[0];
	float *coefG = pColorAdjTarget->coeffs[1];
	float *coefB = pColorAdjTarget->coeffs[2];

	for (int i = 0; i != coefLen; ++i)
	{
		pDescriptorGLES->coefRGB[3 * i + 0] = coefR[i];
		pDescriptorGLES->coefRGB[3 * i + 1] = coefG[i];
		pDescriptorGLES->coefRGB[3 * i + 2] = coefB[i];
	}

	return 0;
}

int fisheyePanoStitcherComp::deInitColorAdjCoefGLES(DescriptorGLES *pDescriptorGLES)
{
	delete[] pDescriptorGLES->coefRGB;
	return 0;
}

int fisheyePanoStitcherComp::colorAdjustRGBChnScanlineGLES(imageFrame *pImageFrame, colorAdjustTarget *pColorAdjTarget, colorAdjusterPair *pColorAdjPair, ImageBlender *pImageBlender, DescriptorGLES *pDescriptorGLES)
{// adjust the image between borders using the coefficients which has same length of the image
 // and the weights is the exposure curbs
	//initial color coefficient
	initColorAdjCoefGLES(pColorAdjTarget, pImageBlender, pDescriptorGLES);
	// texture1
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RGB, GL_UNSIGNED_BYTE, pImageFrame->plane[0]);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture 2
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture1);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RGB, GL_UNSIGNED_BYTE, pColorAdjTarget->pAdjustFrame->plane[0]);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture Mask
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureMask);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RED, GL_UNSIGNED_BYTE, pImageBlender->pMaskY);
	glBindTexture(GL_TEXTURE_2D, 0);
	// texture coefficient
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureAdjCoef);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 1, pDescriptorGLES->heightDst, GL_RGB, GL_FLOAT, pDescriptorGLES->coefRGB);
	glBindTexture(GL_TEXTURE_2D, 0);
	//bind framebuffer
	glBindFramebuffer(GL_FRAMEBUFFER, pDescriptorGLES->framebuffer);

	glClearColor(0.0f, 0.5f, 1.0f, 1.0f);
	glClear(GL_COLOR_BUFFER_BIT);
	glViewport(0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);

	//shader use
	pDescriptorGLES->shaderColorAdj.Use();

	//pass texture
	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture);
	glUniform1i(glGetUniformLocation(pDescriptorGLES->shaderColorAdj.Program, "ourtexture0"), 0);
	//glBindTexture(GL_TEXTURE_2D, 0);
	glActiveTexture(GL_TEXTURE1);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->texture1);
	glUniform1i(glGetUniformLocation(pDescriptorGLES->shaderColorAdj.Program, "ourtexture1"), 1);
	//glBindTexture(GL_TEXTURE_2D, 0);
	glActiveTexture(GL_TEXTURE2);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureMask);
	glUniform1i(glGetUniformLocation(pDescriptorGLES->shaderColorAdj.Program, "ourMask"), 2);
	//glBindTexture(GL_TEXTURE_2D, 0);
	glActiveTexture(GL_TEXTURE3);
	glBindTexture(GL_TEXTURE_2D, pDescriptorGLES->textureAdjCoef);
	glUniform1i(glGetUniformLocation(pDescriptorGLES->shaderColorAdj.Program, "adjCof"), 3);
	//glBindTexture(GL_TEXTURE_2D, 0);
	glUniform1fv(glGetUniformLocation(pDescriptorGLES->shaderColorAdj.Program, "exploreWeight"), 256, pColorAdjPair->mExpoCurbWeights);

	// bind VAO
	glBindVertexArray(pDescriptorGLES->VAO);
	glDrawBuffers(1, &pDescriptorGLES->attachmentpoints);
	glDrawArrays(GL_TRIANGLES, 0, 6);
	glBindVertexArray(0);
	
	
	// read pixel data
	glReadBuffer(pDescriptorGLES->attachmentpoints);

	// can not read pixels in rgb format , so read rgba and transform to rgb;
	GLubyte *tempImage = new GLubyte[pDescriptorGLES->widthDst * pDescriptorGLES->heightDst * 4]; // rgba 4 channels
    __android_log_print(ANDROID_LOG_ERROR,"libstitch","color adjust read pixels");
	glReadPixels(0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RGBA, GL_UNSIGNED_BYTE, tempImage);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","color adjust rgba 2 rgb");
	if (pColorAdjPair->mAdjustIdx == 0) //here store the render result in the 4~8 warpedImage, then restore in 0 ~ 3 warpedImage which is the panoImage;
		rgba2rgb(tempImage, pImageFrame->plane[0], pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);
	else
		rgba2rgb(tempImage, pColorAdjTarget->pAdjustFrame->plane[0], pDescriptorGLES->widthDst, pDescriptorGLES->heightDst);
	delete[] tempImage;
	//if (pColorAdjPair->mAdjustIdx == 0) //here store the render result in the 4~8 warpedImage, then restore in 0 ~ 3 warpedImage which is the panoImage;
	//	glReadPixels(0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RGB, GL_UNSIGNED_BYTE, pImageFrame->plane[0]);
	//else
	//	glReadPixels(0, 0, pDescriptorGLES->widthDst, pDescriptorGLES->heightDst, GL_RGB, GL_UNSIGNED_BYTE, pColorAdjTarget->pAdjustFrame->plane[0]);

	deInitColorAdjCoefGLES(pDescriptorGLES);

	
	//SOIL_save_image("BLENDER.bmp", SOIL_SAVE_TYPE_BMP, pImageFrame->imageW, pImageFrame->imageH, 3, pImageFrame->plane[0]);

	return 0;
}


int fisheyePanoStitcherComp::storePanoImage(imageFrame *renderResult, imageFrame *panoImage)
{
	unsigned char *renderedImage[4] = { renderResult[4].plane[0], renderResult[5].plane[0], renderResult[6].plane[0], renderResult[7].plane[0] };
	unsigned char *panoQuaImage[4];
	panoQuaImage[0] = panoImage->plane[0];
	panoQuaImage[1] = panoQuaImage[0] + panoImage->strides[0] / 2;
	panoQuaImage[2] = panoQuaImage[0] + panoImage->strides[0] * panoImage->imageH / 2;
	panoQuaImage[3] = panoQuaImage[2] + panoImage->strides[0] / 2;

	for (int h = 0; h != renderResult[0].imageH; ++h)
	{
		for (int k = 0; k != 4; ++k)
		{
			memcpy(panoQuaImage[k], renderedImage[k], sizeof(unsigned char) * renderResult->strides[0]);
			renderedImage[k] += renderResult->strides[0];
			panoQuaImage[k] += panoImage->strides[0];
		}	
	}
	
	return 0;
}


int fisheyePanoStitcherComp::imageStitch(imageFrame fisheyeImage[2], imageFrame panoImage)  // warping, color adjusting, blending, extra warping
{
    unsigned char *ptrSeamL = NULL;
    unsigned char *ptrSeamR = NULL;
    int gap;

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","init warp gles");

    // image warping
	initWarpGLES(mImageWarperB, &mDescriptorGL);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","warp image gles");

    warpImageGLES(&mImageWarperB[0], &mDescriptorGL, &fisheyeImage[0], &warpedImageB[0]);
	warpImageGLES(&mImageWarperB[1], &mDescriptorGL, &fisheyeImage[0], &warpedImageB[1]);
	warpImageGLES(&mImageWarperB[2], &mDescriptorGL, &fisheyeImage[0], &warpedImageB[2]);
	warpImageGLES(&mImageWarperB[3], &mDescriptorGL, &fisheyeImage[0], &warpedImageB[3]);
												  					
	warpImageGLES(&mImageWarperB[4], &mDescriptorGL, &fisheyeImage[1], &warpedImageB[4]);
	warpImageGLES(&mImageWarperB[5], &mDescriptorGL, &fisheyeImage[1], &warpedImageB[5]);
	warpImageGLES(&mImageWarperB[6], &mDescriptorGL, &fisheyeImage[1], &warpedImageB[6]);
	warpImageGLES(&mImageWarperB[7], &mDescriptorGL, &fisheyeImage[1], &warpedImageB[7]);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","dinit warp gles");

    deinitWarpGLES(&mDescriptorGL);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","color coeffs");

    // calculate color adjust coefficients;
	mColorAdjusterPair.colorCoeffs();

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","init color adj blend gles");

    // color adjust and blending;
	initColAdjBlendGLES(&mDescriptorGL);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","color adjust rgb chn scanline gles");

    if (mColorAdjusterPair.mAdjustIdx == 0)
	{//adjust target is first image;
		colorAdjustRGBChnScanlineGLES(&warpedImageB[4], &mColorAdjusterPair.mAdjustTargets[0], &mColorAdjusterPair, &mImageBlender[0], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[5], &mColorAdjusterPair.mAdjustTargets[1], &mColorAdjusterPair, &mImageBlender[1], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[6], &mColorAdjusterPair.mAdjustTargets[2], &mColorAdjusterPair, &mImageBlender[2], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[7], &mColorAdjusterPair.mAdjustTargets[3], &mColorAdjusterPair, &mImageBlender[3], &mDescriptorGL);
	}
	else
	{// adjust target is second image;
		colorAdjustRGBChnScanlineGLES(&warpedImageB[0], &mColorAdjusterPair.mAdjustTargets[0], &mColorAdjusterPair, &mImageBlender[0], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[1], &mColorAdjusterPair.mAdjustTargets[1], &mColorAdjusterPair, &mImageBlender[1], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[2], &mColorAdjusterPair.mAdjustTargets[2], &mColorAdjusterPair, &mImageBlender[2], &mDescriptorGL);
		colorAdjustRGBChnScanlineGLES(&warpedImageB[3], &mColorAdjusterPair.mAdjustTargets[3], &mColorAdjusterPair, &mImageBlender[3], &mDescriptorGL);
	}

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","d init col adj blend gles");

    deInitColAdjBlendGLES(&mDescriptorGL);

    __android_log_print(ANDROID_LOG_ERROR,"libstitch","store pano image");

    storePanoImage(warpedImageB, &panoImage);

    return 0;
}


}   // namespace YiPanorama 
}   // namespace fisheyePano
