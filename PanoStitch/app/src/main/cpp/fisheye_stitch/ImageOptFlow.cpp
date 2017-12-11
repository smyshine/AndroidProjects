
#include "ImageOptFlow.h"
#include "ImageIOConverter.h"


namespace YiPanorama {
namespace util {
	using namespace cv;
	using namespace cv::ximgproc;

enum shiftMethod
{
    shift_linear_up_down,
    shift_linear_down_up,
    shift_fixed,
    shift_none
};

// local functions ===================================================
void drawOptFlowMap(const Mat& flow, Mat& cflowmap, int step, const Scalar& color)
{
    for (int y = 0; y < cflowmap.rows; y += step)
    {
        for (int x = 0; x < cflowmap.cols; x += step)
        {
            const Point2f& fxy = flow.at< Point2f>(y, x);
            //if (abs(fxy.x) > 15 || abs(fxy.y) > 15 )
            {
                line(cflowmap, Point(x, y), Point(cvRound(x + fxy.x), cvRound(y + fxy.y)), color);
                circle(cflowmap, Point(cvRound(x + fxy.x), cvRound(y + fxy.y)), 1, color, -1);
            }
        }
    }
    return;
}

int setFarneback(optFlowFarneback *pFarneback)
{
    // a practical set of farneback parameters 
    pFarneback->pyrScale = 0.5; // should < 1, typically 0.5
    pFarneback->numLevels = 3;  // pyramid levels
    pFarneback->winSize = 51;   // 
    pFarneback->numIters = 3;
    pFarneback->polyN = 5;
    pFarneback->polySigma = 1.2;
    pFarneback->flags = 0;
    return 0;
}

int calcSingleOptFlow(Mat imgSrc, Mat imgDst, Mat flow, optFlowFarneback *pFarn)
{
    calcOpticalFlowFarneback(imgSrc, imgDst, flow, pFarn->pyrScale, pFarn->numLevels, pFarn->winSize, pFarn->numIters, pFarn->polyN, pFarn->polySigma, pFarn->flags);
    return 0;
}

void genFlowWarpMat(Mat flow, Mat warpMap, int w, int h, shiftMethod shift, double shiftMax)
{
    double shiftScale;
    switch (shift)
    {
    case shift_linear_up_down:
        for (int y = 0; y < h; ++y)
        {
            shiftScale = (double)y / h;
            for (int x = 0; x < w; ++x)
            {
                Point2f flowDir = flow.at<Point2f>(y, x);
                warpMap.at<Point2f>(y, x) =
                    Point2f(x + flowDir.x * shiftScale * shiftMax, y + flowDir.y * shiftScale * shiftMax);
            }
        }
        break;

    case shift_linear_down_up:
        for (int y = 0; y < h; ++y)
        {
            shiftScale = 1.0 - (double)y / h;
            for (int x = 0; x < w; ++x)
            {
                Point2f flowDir = flow.at<Point2f>(y, x);
                warpMap.at<Point2f>(y, x) =
                    Point2f(x + flowDir.x * shiftScale * shiftMax, y + flowDir.y * shiftScale * shiftMax);
            }
        }
        break;

    case shift_fixed:
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                Point2f flowDir = flow.at<Point2f>(y, x);
                warpMap.at<Point2f>(y, x) =
                    Point2f(x + flowDir.x * shiftMax, y + flowDir.y * shiftMax);
            }
        }

        break;

    case shift_none:
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                Point2f flowDir = flow.at<Point2f>(y, x);
                warpMap.at<Point2f>(y, x) =
                    Point2f(x + flowDir.x, y + flowDir.y);
            }
        }
        break;

    default:
        break;
    }
    return;
}

int warpImageFrame(imageFrame srcImage, Mat warpMap, imageFrame dstImage)
{// source & destiny images have same size with the warp map
 // since the images are in YUV color space, so UV channel's warp map need to be downsampled
    unsigned char *srcImg_y = srcImage.plane[0];
    unsigned char *srcImg_u = srcImage.plane[1];
    unsigned char *srcImg_v = srcImage.plane[2];

    unsigned char *proImg_y = dstImage.plane[0];
    unsigned char *proImg_u = dstImage.plane[1];
    unsigned char *proImg_v = dstImage.plane[2];

    /////projection from image planar to spherical surface
    int imgoff_ori_0, imgoff_ori_1, imgoff_ori_2, imgoff_ori_3;
    int imgoff_ori_quart_0, imgoff_ori_quart_1, imgoff_ori_quart_2, imgoff_ori_quart_3;
    int imgoff_pro_base;
    int imgoff_pro, imageoff_pro_quart;
    int col, row, col_half, row_half, proImgW_half;
    Point2f coord;
    float col_f, row_f;
    unsigned char pv_0, pv_1, pv_2, pv_3;
    double pixelTmp;

    proImgW_half = dstImage.imageW >> 1;

    for (int i = 0; i < warpMap.rows; i++)
    {
        imgoff_pro_base = i * warpMap.cols;

        for (int j = 0; j < warpMap.cols; j++)
        {
            coord = warpMap.at<Point2f>(i, j);      // get the float coordinates in original image
            col_f = coord.x;
            row_f = coord.y;      // get the float coordinates in original image

            row = row_f;
            col = col_f;    // get the float coordinates in original image(with sub pixel precision)

            col_half = col >> 1;
            row_half = row >> 1;

            while (row_f >= srcImage.imageH - 2)    // when the coordinates exceed the last row, cut it
            {
                row_f -= 1;
                row -= 1;
                row_half -= 1;
            }

            imgoff_ori_0 = row*srcImage.strides[0] + col;
            imgoff_ori_1 = imgoff_ori_0 + 1;
            imgoff_ori_2 = imgoff_ori_0 + srcImage.strides[0];
            imgoff_ori_3 = imgoff_ori_1 + srcImage.strides[0];

            imgoff_ori_quart_0 = row_half*srcImage.strides[1] + col_half;
            imgoff_ori_quart_1 = imgoff_ori_quart_0 + 1;
            imgoff_ori_quart_2 = imgoff_ori_quart_0 + srcImage.strides[1];
            imgoff_ori_quart_3 = imgoff_ori_quart_1 + srcImage.strides[1];

            imgoff_pro = imgoff_pro_base + j;

            if (col >= 0 && row >= 0 && imgoff_ori_0 >= 0)
            {
                pv_0 = *(srcImg_y + imgoff_ori_0);
                pv_1 = *(srcImg_y + imgoff_ori_1);
                pv_2 = *(srcImg_y + imgoff_ori_2);
                pv_3 = *(srcImg_y + imgoff_ori_3);

                pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);
                *(proImg_y + imgoff_pro) = pixelTmp;

                if (i % 2 == 0 && j % 2 == 0)
                {
                    imageoff_pro_quart = (i / 2)*proImgW_half + (j / 2);

                    pv_0 = *(srcImg_u + imgoff_ori_quart_0);
                    pv_1 = *(srcImg_u + imgoff_ori_quart_1);
                    pv_2 = *(srcImg_u + imgoff_ori_quart_2);
                    pv_3 = *(srcImg_u + imgoff_ori_quart_3);
                    pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);
                    *(proImg_u + imageoff_pro_quart) = pixelTmp;

                    pv_0 = *(srcImg_v + imgoff_ori_quart_0);
                    pv_1 = *(srcImg_v + imgoff_ori_quart_1);
                    pv_2 = *(srcImg_v + imgoff_ori_quart_2);
                    pv_3 = *(srcImg_v + imgoff_ori_quart_3);
                    pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);
                    *(proImg_v + imageoff_pro_quart) = pixelTmp;
                }
            }
        }
    }
    return 0;
}

// interface functions ====================================================================
ImageOptFlow::ImageOptFlow()
{
	for (int k = 0; k != MAX_IMAGE_CHANNELS; ++k)
	{
		tempImage.plane[k] = NULL;
	}
}

ImageOptFlow::~ImageOptFlow()
{
}

int ImageOptFlow::init(int imageW, int imageH)
{// initialize all memories and objects needed in the calculation of optical flow
    
    // reused memories(image data\ flow\ others)
    mImageW = imageW;
    mImageH = imageH;
    imageL.create(Size(mImageW, mImageH), CV_8UC1);
    imageR.create(Size(mImageW, mImageH), CV_8UC1);

    flowLtoR.create(Size(mImageW, mImageH), CV_32FC2);
    flowRtoL.create(Size(mImageW, mImageH), CV_32FC2);

    warpMapL.create(Size(mImageW, mImageH), CV_32FC2);
    warpMapR.create(Size(mImageW, mImageH), CV_32FC2);

    tempImage.imageW = imageW;
    tempImage.imageH = imageH;
    tempImage.pxlColorFormat = PIXELCOLORSPACE_YUV420PYV;
    tempImage.plane[0] = new unsigned char[imageW * imageH];
    tempImage.plane[1] = new unsigned char[imageW * imageH / 4];
    tempImage.plane[2] = new unsigned char[imageW * imageH / 4];
    tempImage.strides[0] = imageW;
    tempImage.strides[1] = imageW / 2;
    tempImage.strides[2] = imageW / 2;
    // farneback kernel
    setFarneback(&farn);
    
    return 0;
}

int ImageOptFlow::computeImageFlow(imageFrame pImgL, imageFrame pImgR)
{// calculate flows between 2 seam images
    convertImageFrametoCvMatGray(pImgL, imageL);    // convert yuv image frame to single gray mat
    convertImageFrametoCvMatGray(pImgR, imageR);

    calcSingleOptFlow(imageL, imageR, flowLtoR, &farn);
    calcSingleOptFlow(imageR, imageL, flowRtoL, &farn);

#if 0
    // draw flow
    CvScalar color = CV_RGB(255, 0, 0);
    drawOptFlowMap(imageL, flowLtoR, 5, color);

#endif
    return 0;
}

int ImageOptFlow::genWarpMap()
{
    genFlowWarpMat(flowLtoR, warpMapR, mImageW, mImageH, shift_linear_down_up, 1.0);
    genFlowWarpMat(flowRtoL, warpMapL, mImageW, mImageH, shift_linear_up_down, 1.0);

    return 0;
}


int ImageOptFlow::genNovalImage(imageFrame pSrcImgL, imageFrame pSrcImgR)
{// remap image using imageFrame structure rather than mat

    copyImage(&pSrcImgL, &tempImage);
    warpImageFrame(tempImage, warpMapL, pSrcImgL);

    copyImage(&pSrcImgR, &tempImage);
    warpImageFrame(tempImage, warpMapR, pSrcImgR);

    return 0;
}

int ImageOptFlow::dinit()
{
    imageL.release();
    imageR.release();

    flowLtoR.release();
    flowRtoL.release();

    warpMapL.release();
    warpMapR.release();

    delete[] tempImage.plane[0];
    delete[] tempImage.plane[1];
    delete[] tempImage.plane[2];

    return 0;
}


}   // namespace util
}   // namespace YiPanorama
