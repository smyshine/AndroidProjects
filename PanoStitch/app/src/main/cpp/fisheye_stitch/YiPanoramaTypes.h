/************************************************************************/
/* Basic image frame and color space definitions                        */
/************************************************************************/

#pragma once

#ifndef _TYPES_H
#define _TYPES_H

namespace YiPanorama {
namespace util {

#define MAX_IMAGE_CHANNELS 4

enum ePixelColorSpace
{
    PIXELCOLORSPACE_INVALID = -1,
    PIXELCOLORSPACE_MONO = 0,
    PIXELCOLORSPACE_YUV420PYV,
    PIXELCOLORSPACE_RGB,
};

struct imageRoi
{
    int imgW;   // image frame width
    int imgH;   // image frame height
    int roiX;   // region of interest, could be seam cut\ frame valid area \color summary zone
    int roiY;
    int roiW;
    int roiH;

    imageRoi& operator=(const imageRoi& a)
    {
        imgW = a.imgW;
        imgH = a.imgH;
        roiX = a.roiX;
        roiY = a.roiY;
        roiW = a.roiW;
        roiH = a.roiH;
        return *this;
    }

    imageRoi operator/(int div)
    {
        imageRoi a;
        a.imgW = imgW / div;
        a.imgH = imgH / div;
        a.roiX = roiX / div;
        a.roiY = roiY / div;
        a.roiW = roiW / div;
        a.roiH = roiH / div;

        return a;
    }
};

struct imageFrame
{// all image data related operations should use this structure
    // and each of this structure should correspond to a imageSize structure

    int imageW; // effective image data width, excluding possible stride, IMPORTANT!!
    int imageH;
    ePixelColorSpace pxlColorFormat;

    unsigned char *plane[MAX_IMAGE_CHANNELS];   // pointers to actual data
    int strides[MAX_IMAGE_CHANNELS];

    imageFrame() :
        imageW(0),
        imageH(0),
        pxlColorFormat(PIXELCOLORSPACE_INVALID),
        plane(),
        strides()
    {
    }
};

}   // namespace util
}   // namespace YiPanorama

#endif // !_TYPES_H