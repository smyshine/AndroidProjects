
#include "ImageTailor.h"

#include <string.h>

namespace YiPanorama {
namespace util {

int roiCut1Chn(unsigned char *wholeImg, unsigned char *roiImg, imageRoi roiCutInfo)
{
    unsigned char *pWholeImgPtr = wholeImg;
    unsigned char *pRoiImgPtr = roiImg;
    pWholeImgPtr += (roiCutInfo.roiY * roiCutInfo.imgW + roiCutInfo.roiX);
    for (int k = 0; k < roiCutInfo.roiH; k++)
    {
        memcpy(pRoiImgPtr, pWholeImgPtr, sizeof(unsigned char)*roiCutInfo.roiW);
        pRoiImgPtr += roiCutInfo.roiW;
        pWholeImgPtr += roiCutInfo.imgW;
    }
    return 0;
}

int roiCut(imageFrame wholeImage, imageFrame roiImage, imageRoi roiInfo)
{// cut the seam from the whole image
    // assume the wholeImage and roiImage have already allocated memories, and have same color space
    int planeNum;
    imageRoi imageRoiUV;

    imageRoiUV.imgW = roiInfo.imgW / 2;
    imageRoiUV.imgH = roiInfo.imgH / 2;
    imageRoiUV.roiX = roiInfo.roiX / 2;
    imageRoiUV.roiY = roiInfo.roiY / 2;
    imageRoiUV.roiW = roiInfo.roiW / 2;
    imageRoiUV.roiH = roiInfo.roiH / 2;

    if (wholeImage.pxlColorFormat == PIXELCOLORSPACE_MONO)
        planeNum = 1;
    else
        planeNum = 3;

    for (int k = 0; k < planeNum; k++)
    {
        if (0 == k)
            roiCut1Chn(wholeImage.plane[k], roiImage.plane[k], roiInfo);
        else
            roiCut1Chn(wholeImage.plane[k], roiImage.plane[k], imageRoiUV);
    }

    return 0;
}

int roiMerge1Chn(unsigned char *wholeImg, unsigned char *roiImg, imageRoi roiCutInfo)
{
    unsigned char *pWholeImgPtr = wholeImg;
    unsigned char *pRoiImgPtr = roiImg;
    pWholeImgPtr += (roiCutInfo.roiY * roiCutInfo.imgW + roiCutInfo.roiX);
    for (int k = 0; k < roiCutInfo.roiH; k++)
    {
        memcpy(pWholeImgPtr, pRoiImgPtr, sizeof(unsigned char)*roiCutInfo.roiW);
        pRoiImgPtr += roiCutInfo.roiW;
        pWholeImgPtr += roiCutInfo.imgW;
    }
    return 0;
}

int roiMerge(imageFrame wholeImage, imageFrame roiImage, imageRoi roiInfo)
{// merge the seam into the whole image
    // assume the wholeImage and roiImage have already allocated memories, and have same color space
    int planeNum;
    imageRoi imageRoiUV;

    imageRoiUV.imgW = roiInfo.imgW / 2;
    imageRoiUV.imgH = roiInfo.imgH / 2;
    imageRoiUV.roiX = roiInfo.roiX / 2;
    imageRoiUV.roiY = roiInfo.roiY / 2;
    imageRoiUV.roiW = roiInfo.roiW / 2;
    imageRoiUV.roiH = roiInfo.roiH / 2;

    if (wholeImage.pxlColorFormat == PIXELCOLORSPACE_MONO)
        planeNum = 1;
    else
        planeNum = 3;


    for (int k = 0; k < planeNum; k++)
    {
        if (0 == k)
            roiMerge1Chn(wholeImage.plane[k], roiImage.plane[k], roiInfo);
        else
            roiMerge1Chn(wholeImage.plane[k], roiImage.plane[k], imageRoiUV);
    }

    return 0;
}

//int roiMergeNeg(imageFrame wholeImage, imageFrame roiImage, imageRoi roiInfo)
//{// merge the opposite seam into the whole image
// // assume the wholeImage and roiImage have already allocated memories, and have same color space
//    int planeNum;
//    imageRoi roiInfoNeg, imageRoiUV;
//
//    roiInfoNeg.imgW = roiInfo.imgW;
//    roiInfoNeg.imgH = roiInfo.imgH;
//    roiInfoNeg.roiX = roiInfo.roiX;
//    roiInfoNeg.roiY = roiInfo.roiY;
//    roiInfoNeg.roiW = roiInfo.roiW;
//    roiInfoNeg.roiH = roiInfo.roiH;
//
//    imageRoiUV.imgW = roiInfoNeg.imgW / 2;
//    imageRoiUV.imgH = roiInfoNeg.imgH / 2;
//    imageRoiUV.roiX = roiInfoNeg.roiX / 2;
//    imageRoiUV.roiY = roiInfoNeg.roiY / 2;
//    imageRoiUV.roiW = roiInfoNeg.roiW / 2;
//    imageRoiUV.roiH = roiInfoNeg.roiH / 2;
//
//    if (wholeImage.pxlColorFormat == PIXELCOLORSPACE_MONO)
//        planeNum = 1;
//    else
//        planeNum = 3;
//
//
//    for (int k = 0; k < planeNum; k++)
//    {
//        if (0 == k)
//            roiMerge1Chn(wholeImage.plane[k], roiImage.plane[k], roiInfo);
//        else
//            roiMerge1Chn(wholeImage.plane[k], roiImage.plane[k], imageRoiUV);
//    }
//
//    return 0;
//}

int imageCoordsTransfer(double wholeCoords[2], double roiCoords[2], imageRoi roiCutInfo, imageCoordTransferChoice choice)
{
    int iResult = 0;
    switch (choice)
    {
    case whole2roi:
        roiCoords[0] = wholeCoords[0] - roiCutInfo.roiY;
        roiCoords[1] = wholeCoords[1] - roiCutInfo.roiX;
        break;

    case roi2whole:
        wholeCoords[0] = roiCoords[0] + roiCutInfo.roiY;
        wholeCoords[1] = roiCoords[1] + roiCutInfo.roiX;
        break;

    default:
        iResult = -1;
        break;
    }

    return iResult;
}


}
}