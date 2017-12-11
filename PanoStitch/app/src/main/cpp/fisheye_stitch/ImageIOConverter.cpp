
#include <cstdio>
#include <string.h>
#include "ImageIOConverter.h"

namespace YiPanorama {
namespace util {

// function definitions ========================================================
//------------------------------------------------------------------------------
int RGBtoYUV420NV(unsigned char *rgbBuf, int iWidth, int iHeight, unsigned char *yuvBuf)
{
    // rgb buf is interleaved
    // Y and UV buffer are separated
    int i, j;
    int m_size = iHeight * iWidth;

    unsigned char *pRGBBuf = rgbBuf;
    unsigned char r, g, b;
    int y, u, v;

    unsigned char *pYBuf = yuvBuf;
    unsigned char *pVUBuf = yuvBuf + m_size;

    for (i = 0; i < iHeight; i += 2)
    {
        for (j = 0; j < iWidth; j += 2)
        {
            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
            u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
            v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

            *(pYBuf + j) = y>255 ? 255 : (y < 0 ? 0 : y);
            *(pVUBuf + j) = v > 255 ? 255 : (v < 0 ? 0 : v);        // channel v
            *(pVUBuf + j + 1) = u > 255 ? 255 : (u < 0 ? 0 : u);    // channel u

            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
            *(pYBuf + j + 1) = y > 255 ? 255 : (y < 0 ? 0 : y);
        }

        pYBuf += iWidth;
        pVUBuf += iWidth;

        for (j = 0; j < iWidth; j++)
        {
            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;

            *(pYBuf + j) = y>255 ? 255 : (y < 0 ? 0 : y);

        }
        pYBuf += iWidth;

    }

    return 0;

}

//------------------------------------------------------------------------------
int RGBtoYUV420YV(unsigned char *rgbBuf, int iWidth, int iHeight, unsigned char *yuvBuf)
{
    // rgb buf is interleaved
    // YUV buffer is planar
    int i, j;
    int m_size = iHeight*iWidth;

    unsigned char *pRGBBuf = rgbBuf;
    unsigned char r, g, b;
    int y, u, v;

    unsigned char *pYBuf = yuvBuf;
    unsigned char *pUBuf = pYBuf + m_size;
    unsigned char *pVBuf = pUBuf + m_size / 4;

    for (i = 0; i < iHeight; i += 2)
    {
        for (j = 0; j < iWidth; j += 2)
        {
            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
            u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
            v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

            *(pYBuf + j) = y>255 ? 255 : (y < 0 ? 0 : y);
            *(pUBuf + j / 2) = u > 255 ? 255 : (u < 0 ? 0 : u);  // channel u
            *(pVBuf + j / 2) = v > 255 ? 255 : (v < 0 ? 0 : v);  // channel v

            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
            *(pYBuf + j + 1) = y > 255 ? 255 : (y < 0 ? 0 : y);
        }

        pYBuf += iWidth;
        pUBuf += iWidth / 2;
        pVBuf += iWidth / 2;

        for (j = 0; j < iWidth; j++)
        {
            r = *pRGBBuf++;
            g = *pRGBBuf++;
            b = *pRGBBuf++;

            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;

            *(pYBuf + j) = y>255 ? 255 : (y < 0 ? 0 : y);

        }
        pYBuf += iWidth;

    }

    return 0;

}

//------------------------------------------------------------------------------
int YUV420NVtoRGB(unsigned char *yuvBuf, int iWidth, int iHeight, unsigned char *rgbBuf)
{
    int i, j;
    int im_size = iHeight*iWidth;

    unsigned char *pY = yuvBuf;
    unsigned char *pVU = pY + im_size;
    unsigned char *pRGB = rgbBuf;

    unsigned char y, u, v;
    int r, g, b;

    for (i = 0; i < iHeight; i++)
        for (j = 0; j < iWidth; j++)
        {
            y = *(pY + i*iWidth + j);
            v = *(pVU + (i / 2)*iWidth + j - (j % 2));
            u = *(pVU + (i / 2)*iWidth + j - (j % 2) + 1);

            r = (64 * y + 90 * v - 11488) >> 6;
            g = (64 * y - 22 * u - 46 * v + 8736) >> 6;
            b = (64 * y + 113 * u - 14432) >> 6;

            *pRGB++ = (r > 255) ? 255 : ((r < 0) ? 0 : r);
            *pRGB++ = (g > 255) ? 255 : ((g < 0) ? 0 : g);
            *pRGB++ = (b > 255) ? 255 : ((b < 0) ? 0 : b);
        }

    return 0;

}

//------------------------------------------------------------------------------
int YUV420YVtoRGB(unsigned char *yuvBuf, int iWidth, int iHeight, unsigned char *rgbBuf)
{
    int i, j;
    int im_size = iHeight*iWidth;

    unsigned char *pY = yuvBuf;
    unsigned char *pU = pY + im_size;
    unsigned char *pV = pU + im_size / 4;
    unsigned char *pRGB = rgbBuf;

    unsigned char y, u, v;
    int r, g, b;

    for (i = 0; i < iHeight; i++)
        for (j = 0; j < iWidth; j++)
        {
            y = *(pY + i*iWidth + j);
            u = *(pU + (i / 2)*(iWidth / 2) + (j / 2));
            v = *(pV + (i / 2)*(iWidth / 2) + (j / 2));

            r = (64 * y + 90 * v - 11488) >> 6;
            g = (64 * y - 22 * u - 46 * v + 8736) >> 6;
            b = (64 * y + 113 * u - 14432) >> 6;

            *pRGB++ = (r > 255) ? 255 : ((r < 0) ? 0 : r);
            *pRGB++ = (g > 255) ? 255 : ((g < 0) ? 0 : g);
            *pRGB++ = (b > 255) ? 255 : ((b < 0) ? 0 : b);
        }

    return 0;

}

//------------------------------------------------------------------------------
int loadImageData(const char *imgPath, unsigned char *imageData, int iWidth, int iHeight, ePixelColorSpace imgCs)
{
    int rgbChannels = 3;
    unsigned char *pTmp = NULL;

    IplImage *cvImgData = cvLoadImage(imgPath, -1);
    if (cvImgData == NULL)
        return -1;

    switch (imgCs)
    {
    case PIXELCOLORSPACE_RGB:
        for (int j = 0; j < iHeight; ++j) {
            for (int i = 0; i < iWidth; ++i) {
                imageData[(j*iWidth + i)*rgbChannels + 0] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 0];
                imageData[(j*iWidth + i)*rgbChannels + 1] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 1];
                imageData[(j*iWidth + i)*rgbChannels + 2] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 2];
            }
        }
        break;

    case PIXELCOLORSPACE_MONO:
        for (int j = 0; j < iHeight; ++j)
            for (int i = 0; i < iWidth; ++i)
                imageData[j*iWidth + i] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i];
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        pTmp = new unsigned char[iWidth * iHeight * 3];
        for (int j = 0; j < iHeight; ++j) {
            for (int i = 0; i < iWidth; ++i) {
                pTmp[(j*iWidth + i)*rgbChannels + 0] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 0];
                pTmp[(j*iWidth + i)*rgbChannels + 1] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 1];
                pTmp[(j*iWidth + i)*rgbChannels + 2] = (unsigned char)cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 2];
            }
        }
        RGBtoYUV420YV(pTmp, iWidth, iHeight, imageData);
        delete[] pTmp;
        pTmp = NULL;
        break;

    default:
        break;
    }

    cvReleaseImage(&cvImgData);
    return 0;
}

//------------------------------------------------------------------------------
int saveImage(const char *imgPath, unsigned char *imageData, int iWidth, int iHeight, ePixelColorSpace imgCS)
{
    int imgChannels = 3;
    if (imgCS == PIXELCOLORSPACE_MONO)
        imgChannels = 1;
    unsigned char *pTmp = NULL;

    IplImage *cvImgData = cvCreateImage(cvSize(iWidth, iHeight), 8, imgChannels);
    if (cvImgData == NULL)
        return -1;

    switch (imgCS)
    {
    case PIXELCOLORSPACE_RGB:
        for (int j = 0; j < iHeight; ++j) {
            for (int i = 0; i < iWidth; ++i) {
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 0] = imageData[(j*iWidth + i)*imgChannels + 0];
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 1] = imageData[(j*iWidth + i)*imgChannels + 1];
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 2] = imageData[(j*iWidth + i)*imgChannels + 2];
            }
        }
        break;

    case PIXELCOLORSPACE_MONO:
        for (int j = 0; j < iHeight; ++j)
            for (int i = 0; i < iWidth; ++i)
                cvImgData->imageData[j*cvImgData->widthStep + i] = imageData[j*iWidth + i];
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        pTmp = new unsigned char[iWidth * iHeight * 3];
        YUV420YVtoRGB(imageData, iWidth, iHeight, pTmp);
        for (int j = 0; j < iHeight; ++j) {
            for (int i = 0; i < iWidth; ++i) {
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 0] = pTmp[(j*iWidth + i)*imgChannels + 0];
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 1] = pTmp[(j*iWidth + i)*imgChannels + 1];
                cvImgData->imageData[j*cvImgData->widthStep + i * 3 + 2] = pTmp[(j*iWidth + i)*imgChannels + 2];
            }
        }
        delete[] pTmp;
        pTmp = NULL;
        break;

    default:
        break;
    }

    cvSaveImage(imgPath, cvImgData);
    cvReleaseImage(&cvImgData);

    return 0;
}

int addStride1Chn(unsigned char *srcImageData, unsigned char *dstImageData, int iWidth, int iWidthStride, int iHeight)
{// this function is used only to simulate the video frame input with stride at the end of each lines
    // and both srcImagedata and dstImageData have preallocated memory spaces.
    unsigned char *pSrc = srcImageData;
    unsigned char *pDst = dstImageData;

    for (int i = 0; i < iHeight; i++)
    {
        memcpy(pDst, pSrc, sizeof(unsigned char)*iWidth);
        pDst += iWidthStride;
        pSrc += iWidth;
    }

    return 0;
}

void convertUCGrayToCvMat(unsigned char *imgGray, Mat imgDst)
{
    int height = imgDst.rows;
    int width = imgDst.cols;

    int offset;

    for (int i = 0; i < height; ++i)
    {
        offset = i*width;
        for (int j = 0; j < width; ++j)
        {
            imgDst.at<uchar>(i, j) = imgGray[offset + j];
        }
    }

    return;
}

void convertUCRGBtoCvMat(unsigned char *imgRgb, Mat imgDst)
{
    int height = imgDst.rows;
    int width = imgDst.cols;

    int offset;

    for (int i = 0; i < height; ++i)
    {
        offset = i*width * 3;
        for (int j = 0; j < width; ++j)
        {
            imgDst.at<Vec3b>(i, j)[0] = imgRgb[offset + 3 * j];
            imgDst.at<Vec3b>(i, j)[1] = imgRgb[offset + 3 * j + 1];
            imgDst.at<Vec3b>(i, j)[2] = imgRgb[offset + 3 * j + 2];
        }
    }

    return;
}

void convertUCYUV420toCvMat(unsigned char *imgYuv, Mat imgDst)
{

    int height = imgDst.rows;
    int width = imgDst.cols;

    unsigned char *pTmp = new unsigned char[width * height * 3];
    YUV420YVtoRGB(imgYuv, width, height, pTmp);
    int offset;

    for (int i = 0; i < height; ++i)
    {
        offset = i*width * 3;
        for (int j = 0; j < width; ++j)
        {
            imgDst.at<Vec3b>(i, j)[0] = pTmp[offset + 3 * j];
            imgDst.at<Vec3b>(i, j)[1] = pTmp[offset + 3 * j + 1];
            imgDst.at<Vec3b>(i, j)[2] = pTmp[offset + 3 * j + 2];
        }
    }

    delete[] pTmp;
    return;
}


// exposed functions ===========================================================
int readImageParameters(const char *imgPath, int *iWidth, int *iHeight, int *iChn)
{
    IplImage *cvImgData = cvLoadImage(imgPath, -1);

    if (cvImgData == NULL)
    {
        return -1;
    }
    *iWidth = cvImgData->width;
    *iHeight = cvImgData->height;
    *iChn = cvImgData->nChannels;

    cvReleaseImage(&cvImgData);
    return 0;
}

int initImageFrame(imageFrame *pImage, int width, int height, ePixelColorSpace imgCs)
{
    pImage->imageW = width;
    pImage->imageH = height;
    pImage->pxlColorFormat = imgCs;

    for (int k = 0; k < MAX_IMAGE_CHANNELS; k++)
    {
        pImage->plane[k] = NULL;
        pImage->strides[k] = 0;
    }

    switch (imgCs)
    {
    case PIXELCOLORSPACE_INVALID:
        return -1;
        break;

    case PIXELCOLORSPACE_MONO:
        pImage->strides[0] = width;
        pImage->plane[0] = new unsigned char[width * height];
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        pImage->strides[0] = width;
        pImage->strides[1] = width / 2;
        pImage->strides[2] = width / 2;
        pImage->plane[0] = new unsigned char[width * height];
        pImage->plane[1] = new unsigned char[width * height / 4];
        pImage->plane[2] = new unsigned char[width * height / 4];
        break;

    case PIXELCOLORSPACE_RGB:
        pImage->strides[0] = width * 3;
		pImage->strides[1] = 0;
		pImage->strides[2] = 0;
		pImage->plane[0] = new unsigned char[width * height * 3];
		pImage->plane[1] = NULL;
		pImage->plane[2] = NULL;
		memset(pImage->plane[0], 0, width * height * 3);
 
        break;
    default:
        break;
    }

    return 0;
}

int dinitImageFrame(imageFrame *pImage)
{
    for (int k = 0; k < MAX_IMAGE_CHANNELS; k++)
    {
        pImage->strides[k] = 0;
        if (pImage->plane[k] != NULL)
        {
            delete[] pImage->plane[k];
            pImage->plane[k] = NULL;
        }
    }
    return 0;
}

int loadImageData(const char *imgPath, imageFrame image)
{
    int imageDataSize;
    switch (image.pxlColorFormat)
    {
    case PIXELCOLORSPACE_MONO:
        imageDataSize = image.imageW * image.imageH;
        break;
    case PIXELCOLORSPACE_YUV420PYV:
        imageDataSize = image.imageW * image.imageH * 3 / 2;
        break;
    case PIXELCOLORSPACE_RGB:
        imageDataSize = image.imageW * image.imageH * 3;
        break;
    default:
        return -1;
        break;
    }

    unsigned char *TmpImgData = new unsigned char[imageDataSize];
    loadImageData(imgPath, TmpImgData, image.imageW, image.imageH, image.pxlColorFormat);

    switch (image.pxlColorFormat)
    {
    case PIXELCOLORSPACE_MONO:
        memcpy(image.plane[0], TmpImgData, sizeof(unsigned char) * image.imageW * image.imageH);
        break;
    case PIXELCOLORSPACE_YUV420PYV:
        memcpy(image.plane[0], TmpImgData, sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(image.plane[1], TmpImgData + image.imageW * image.imageH, sizeof(unsigned char) * image.imageW * image.imageH / 4);
        memcpy(image.plane[2], TmpImgData + image.imageW * image.imageH * 5 / 4, sizeof(unsigned char) * image.imageW * image.imageH / 4);
        break;
    case PIXELCOLORSPACE_RGB:
        memcpy(image.plane[0], TmpImgData, sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(image.plane[1], TmpImgData + image.imageW * image.imageH, sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(image.plane[2], TmpImgData + image.imageW * image.imageH * 2, sizeof(unsigned char) * image.imageW * image.imageH);
        break;
    default:
        return -1;
        break;
    }
    delete[] TmpImgData;
    return 0;
}

int loadImageDataFisheyePair(const char *imgPath, imageFrame fisheyeImage[2], imageLayout layout)
{// the image's size is known after they are loaded
    int iResult = 0;
    IplImage *pImgLoad = cvLoadImage(imgPath);
    IplImage *pImg = NULL;
    IplImage *pImgF = NULL;
    IplImage *pImgB = NULL;

    unsigned char *pTmpRgb = NULL;
    unsigned char *pTmpYuv = NULL;

    if (pImgLoad == NULL)
    {
        iResult = -1;
    }

    // check if the load image doesn't have size of 2880*5760
    pImg = cvCreateImage(cvSize(2880, 5760), pImgLoad->depth, pImgLoad->nChannels);
    cvResize(pImgLoad, pImg);

    int sizeW,sizeH;
    CvRect roiF, roiB;

    if (0 == iResult)
    {
        switch (layout)
        {
        case sideBySide:
            sizeW = pImg->width / 2;
            sizeH = pImg->height;
            roiF.x = 0;
            roiF.y = 0;
            roiB.x = sizeW;
            roiB.y = 0;
            break;
        case overAndUnder:
            sizeW = pImg->width;
            sizeH = pImg->height / 2;
            roiF.x = 0;
            roiF.y = 0;
            roiB.x = 0;
            roiB.y = sizeH;
            break;
        default:
            iResult = -2;  // 
            break;
        }
        
        roiF.width = sizeW;
        roiF.height = sizeH;
        roiB.width = sizeW;
        roiB.height = sizeH;
        pImgF = cvCreateImage(cvSize(sizeW, sizeH), 8, 3);
        pImgB = cvCreateImage(cvSize(sizeW, sizeH), 8, 3);
        cvSetImageROI(pImg, roiF);
        cvResize(pImg, pImgF);
        cvResetImageROI(pImg);
        cvSetImageROI(pImg, roiB);
        cvResize(pImg, pImgB);
        cvResetImageROI(pImg);

        initImageFrame(&fisheyeImage[0], sizeW, sizeH, PIXELCOLORSPACE_RGB);
        initImageFrame(&fisheyeImage[1], sizeW, sizeH, PIXELCOLORSPACE_RGB);

        //pTmpRgb = new unsigned char[sizeW * sizeH * 3];

        for (int j = 0; j < sizeH; ++j) {
			memcpy(fisheyeImage[0].plane[0] + j * fisheyeImage[0].strides[0], pImgF->imageData + j * pImgF->widthStep, sizeof(unsigned char) * fisheyeImage[0].strides[0]);
        }


        for (int j = 0; j < sizeH; ++j) {
			memcpy(fisheyeImage[1].plane[0] + j * fisheyeImage[1].strides[0], pImgB->imageData + j * pImgB->widthStep, sizeof(unsigned char) * fisheyeImage[1].strides[0]);
		}
       
        cvReleaseImage(&pImgF);
        cvReleaseImage(&pImgB);

    }
    cvReleaseImage(&pImgLoad);
    cvReleaseImage(&pImg);
    return iResult;
}

int saveImage(const char *imgPath, imageFrame image)
{
    int imageDataSize;
    unsigned char *TmpImgData = NULL;

    switch (image.pxlColorFormat)
    {
    case PIXELCOLORSPACE_MONO:
        imageDataSize = image.imageW * image.imageH;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        imageDataSize = image.imageW * image.imageH * 3 / 2;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH, image.plane[1], sizeof(unsigned char) * image.imageW * image.imageH / 4);
        memcpy(TmpImgData + image.imageW * image.imageH * 5 / 4, image.plane[2], sizeof(unsigned char) * image.imageW * image.imageH / 4);
        break;
    case PIXELCOLORSPACE_RGB:
        imageDataSize = image.imageW * image.imageH * 3;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH, image.plane[1], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH * 2, image.plane[2], sizeof(unsigned char) * image.imageW * image.imageH);
        break;
    default:
        return -1;
        break;
    }

    saveImage(imgPath, TmpImgData, image.imageW, image.imageH, image.pxlColorFormat);

    delete[] TmpImgData;
    return 0;
}

int copyImage(imageFrame *pSrcImage, imageFrame *pDstImage)
{// dst image is already malloced 
    int imageSize = pSrcImage->imageW * pSrcImage->imageH;

    switch (pSrcImage->pxlColorFormat)
    {
    case PIXELCOLORSPACE_MONO:
        memcpy(pDstImage->plane[0], pSrcImage->plane[0], sizeof(unsigned char) * imageSize);
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        memcpy(pDstImage->plane[0], pSrcImage->plane[0], sizeof(unsigned char) * imageSize);
        imageSize /= 4;
        memcpy(pDstImage->plane[1], pSrcImage->plane[1], sizeof(unsigned char) * imageSize);
        memcpy(pDstImage->plane[2], pSrcImage->plane[2], sizeof(unsigned char) * imageSize);
        break;

    case PIXELCOLORSPACE_RGB:
        memcpy(pDstImage->plane[0], pSrcImage->plane[0], sizeof(unsigned char) * imageSize);
        memcpy(pDstImage->plane[1], pSrcImage->plane[1], sizeof(unsigned char) * imageSize);
        memcpy(pDstImage->plane[2], pSrcImage->plane[2], sizeof(unsigned char) * imageSize);
        break;

    default:
        break;
    }

    return 0;
}

int convertImageFrametoCvMat(imageFrame image, Mat imgDst)
{
    int imageDataSize = 0;
    unsigned char *TmpImgData = NULL;

    switch (image.pxlColorFormat)
    {
    case PIXELCOLORSPACE_MONO:
        imageDataSize = image.imageW * image.imageH;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        convertUCGrayToCvMat(TmpImgData, imgDst);
        break;

    case PIXELCOLORSPACE_YUV420PYV:
        imageDataSize = image.imageW * image.imageH * 3 / 2;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH, image.plane[1], sizeof(unsigned char) * image.imageW * image.imageH / 4);
        memcpy(TmpImgData + image.imageW * image.imageH * 5 / 4, image.plane[2], sizeof(unsigned char) * image.imageW * image.imageH / 4);
        convertUCYUV420toCvMat(TmpImgData, imgDst);
        break;

    case PIXELCOLORSPACE_RGB:
        imageDataSize = image.imageW * image.imageH * 3;
        TmpImgData = new unsigned char[imageDataSize];
        memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH, image.plane[1], sizeof(unsigned char) * image.imageW * image.imageH);
        memcpy(TmpImgData + image.imageW * image.imageH * 2, image.plane[2], sizeof(unsigned char) * image.imageW * image.imageH);
        convertUCRGBtoCvMat(TmpImgData, imgDst);
        break;

    default:
        return -1;
        break;
    }

    delete[] TmpImgData;
    return 0;
}

int convertImageFrametoCvMatGray(imageFrame image, Mat imgDst)
{
    int imageDataSize = 0;
    unsigned char *TmpImgData = NULL;

    imageDataSize = image.imageW * image.imageH;
    TmpImgData = new unsigned char[imageDataSize];
    memcpy(TmpImgData, image.plane[0], sizeof(unsigned char) * image.imageW * image.imageH);
    convertUCGrayToCvMat(TmpImgData, imgDst);

    delete[] TmpImgData;
    return 0;
}

}   // namespace util
}   // namespace YiPanorama
