/************************************************************************/
/* the mapping.cl for image warper using OpenCL                         */
/************************************************************************/
#pragma once
#ifndef _MAPPING_CL_H
#define _MAPPING_CL_H

namespace YiPanorama {
namespace warper {

const char mapping_full_cl[] = 
"kernel void mappingFull(global const unsigned char *srcImg_y, global const unsigned char *srcImg_u, global const unsigned char *srcImg_v,   \
\n    global unsigned char *proImg_y, global unsigned char *proImg_u, global unsigned char *proImg_v,   \
\n    global const float *pmapX, global const float *pmapY, global const int *srcImageW, global const int *srcImageH,   \
\n    global const int *srcImageWStrideY, global const int *srcImageWStrideUV, global const int *proImageW, global const int *proImageH)   \
\n{   \
\n    float mapx, mapy;   \
\n    int imgoff_ori_0, imgoff_ori_1, imgoff_ori_2, imgoff_ori_3;   \
\n    int imgoff_ori_quart_0, imgoff_ori_quart_1, imgoff_ori_quart_2, imgoff_ori_quart_3;   \
\n    int imgoff_pro_base;   \
\n    int imgoff_pro, imageoff_pro_quart;   \
\n    int col, row, col_half, row_half, srcImgW, srcImgH, proImgW, proImgH, proImgW_half, srcImgWStrideY, srcImgWStrideUV;   \
\n    float col_f, row_f;   \
\n    unsigned char pv_0, pv_1, pv_2, pv_3;   \
\n    float pixelTmp;   \
\n   \
\n    srcImgW = *srcImageW;   \
\n    srcImgH = *srcImageH;   \
\n    srcImgWStrideY = *srcImageWStrideY;   \
\n    srcImgWStrideUV = *srcImageWStrideUV;   \
\n    proImgW = *proImageW;   \
\n    proImgH = *proImageH;   \
\n   \
\n    proImgW_half = proImgW >> 1;   \
\n   \
\n    int i = get_global_id(1);   \
\n    int j = get_global_id(0);   \
\n   \
\n    imgoff_pro_base = i*proImgW;   \
\n   \
\n    mapx = pmapX[imgoff_pro_base + j];   \
\n    mapy = pmapY[imgoff_pro_base + j];   \
\n   \
\n    col = mapx * srcImgW;   \
\n    row = mapy * srcImgH;   \
\n    col_f = mapx * srcImgW;   \
\n    row_f = mapy * srcImgH;   \
\n   \
\n    col_half = col >> 1;   \
\n    row_half = row >> 1;   \
\n   \
\n    imgoff_ori_0 = row*srcImgWStrideY + col;   \
\n    imgoff_ori_1 = imgoff_ori_0 + 1;   \
\n    imgoff_ori_2 = imgoff_ori_0 + srcImgWStrideY;   \
\n    imgoff_ori_3 = imgoff_ori_1 + srcImgWStrideY;   \
\n   \
\n    imgoff_ori_quart_0 = row_half*srcImgWStrideUV + col_half;   \
\n    imgoff_ori_quart_1 = imgoff_ori_quart_0 + 1;   \
\n    imgoff_ori_quart_2 = imgoff_ori_quart_0 + srcImgWStrideUV;   \
\n    imgoff_ori_quart_3 = imgoff_ori_quart_1 + srcImgWStrideUV;   \
\n   \
\n    imgoff_pro = imgoff_pro_base + j;   \
\n   \
\n    if (col >= 0 && row >= 0 && imgoff_ori_0 >= 0)   \
\n    {   \
\n        pv_0 = *(srcImg_y + imgoff_ori_0);   \
\n        pv_1 = *(srcImg_y + imgoff_ori_1);   \
\n        pv_2 = *(srcImg_y + imgoff_ori_2);   \
\n        pv_3 = *(srcImg_y + imgoff_ori_3);   \
\n   \
\n        pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);   \
\n        *(proImg_y + imgoff_pro) = pixelTmp;   \
\n   \
\n        if (i % 2 == 0 && j % 2 == 0)   \
\n        {   \
\n            imageoff_pro_quart = (i / 2)*proImgW_half + (j / 2);   \
\n   \
\n            pv_0 = *(srcImg_u + imgoff_ori_quart_0);   \
\n            pv_1 = *(srcImg_u + imgoff_ori_quart_1);   \
\n            pv_2 = *(srcImg_u + imgoff_ori_quart_2);   \
\n            pv_3 = *(srcImg_u + imgoff_ori_quart_3);   \
\n            pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);   \
\n            *(proImg_u + imageoff_pro_quart) = pixelTmp;   \
\n   \
\n            pv_0 = *(srcImg_v + imgoff_ori_quart_0);   \
\n            pv_1 = *(srcImg_v + imgoff_ori_quart_1);   \
\n            pv_2 = *(srcImg_v + imgoff_ori_quart_2);   \
\n            pv_3 = *(srcImg_v + imgoff_ori_quart_3);   \
\n            pixelTmp = ((pv_0)*(col + 1 - col_f) + (pv_1)*(col_f - col))*(row + 1 - row_f) + ((pv_2)*(col + 1 - col_f) + (pv_3)*(col_f - col))*(row_f - row);   \
\n            *(proImg_v + imageoff_pro_quart) = pixelTmp;   \
\n        }   \
\n    }   \
\n}   \
";

}   // namespace warper
}   // namespace YiPanorama

#endif  //!_MAPPING_CL_H