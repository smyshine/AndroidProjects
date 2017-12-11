/************************************************************************/
/* Image cut and stitch operations related with ROI                     */
/************************************************************************/
#pragma once
#ifndef _IMAGE_TAILOR_H
#define _IMAGE_TAILOR_H

#include "YiPanoramaTypes.h"

namespace YiPanorama {
namespace util {

enum imageCoordTransferChoice
{
    whole2roi, // transfer a point's coordinates from whole image to seam image
    roi2whole
};


// cut the roi from the image, NOTICE: the images may have n channels and pSeamCutInfo has same number 
int roiCut(imageFrame wholeImage, imageFrame roiImage, imageRoi roiInfo);

// merge the roi into the image, NOTICE: the images may have n channels and pSeamCutInfo has same number 
int roiMerge(imageFrame wholeImage, imageFrame roiImage, imageRoi roiInfo);

// coordinates transformation between whole image and seam image
int imageCoordsTransfer(double wholeCoords[2], double roiCoords[2], imageRoi pSeamCutInfo, imageCoordTransferChoice choice);


}   // namespace util
}   // namespace YiPanorama

#endif // !_IMAGE_TAILOR_H
