
#include "FisheyePanoParams.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

namespace YiPanorama {

//#ifdef _WIN32

int readFisheyePanoParamsFromFile(const char *filePath, fisheyePanoParams *pFisheyePanoParams)
{// binary default file is used
    int iResult = 0;

    FILE *fp = NULL;
    char fileExtension[4];
    bool fileType;

    //errno_t err;
    char buf[CMV_MAX_BUF];
    int i, k, fileLen;
    fileLen = strlen(filePath);
    memcpy(fileExtension, filePath + fileLen - 3, 4);

    fileType = strcmp(fileExtension, "txt");

    if (fileType)
    {
        fp = fopen(filePath, "rb");
        if (fp == NULL)
        {
            return -1;
        }
        fread(pFisheyePanoParams, 1, sizeof(fisheyePanoParams), fp);
        fclose(fp);
    }
    else
    {
        fp = fopen(filePath, "rt");
        if (fp == NULL)
        {
            return -1;
        }
        //----------------
        fgets(buf, CMV_MAX_BUF, fp);    // fisheyePanoParamsCore
        fgets(buf, CMV_MAX_BUF, fp);    // fisheyeImgW, fisheyeImgW
        fscanf(fp, "%d %d\n", &pFisheyePanoParams->stFisheyePanoParamsCore.fisheyeImgW, &pFisheyePanoParams->stFisheyePanoParamsCore.fisheyeImgH);
        fgets(buf, CMV_MAX_BUF, fp);    // panoImgW, panoImgH
        fscanf(fp, "%d %d\n", &pFisheyePanoParams->stFisheyePanoParamsCore.panoImgW, &pFisheyePanoParams->stFisheyePanoParamsCore.panoImgH);
        fgets(buf, CMV_MAX_BUF, fp);    // sphereRadius
        fscanf(fp, "%d\n", &pFisheyePanoParams->stFisheyePanoParamsCore.sphereRadius);
        fgets(buf, CMV_MAX_BUF, fp);    // maxFovAngle
        fscanf(fp, "%f\n", &pFisheyePanoParams->stFisheyePanoParamsCore.maxFovAngle);

        //----------------
        fscanf(fp, "\n");             //
        fgets(buf, CMV_MAX_BUF, fp);    // ocamModels 
        for (k = 0; k < 2; k++)
        {
            fscanf(fp, "\n");
            fgets(buf, CMV_MAX_BUF, fp);    // ocamModel 0&1
            fgets(buf, CMV_MAX_BUF, fp);    // polynomial length
            fscanf(fp, "%d\n", &pFisheyePanoParams->staOcamModels[k].length_pol);
            fgets(buf, CMV_MAX_BUF, fp);    // polynomial coeffs
            for (i = 0; i < pFisheyePanoParams->staOcamModels[k].length_pol; i++)
            {
                fscanf(fp, "%lf ", &pFisheyePanoParams->staOcamModels[k].pol[i]);
            }
            fgets(buf, CMV_MAX_BUF, fp);    // inv_polynomial length
            fscanf(fp, "%d\n", &pFisheyePanoParams->staOcamModels[k].length_invpol);
            fgets(buf, CMV_MAX_BUF, fp);    // inv_polynomial coeffs
            for (i = 0; i < pFisheyePanoParams->staOcamModels[k].length_invpol; i++)
            {
                fscanf(fp, "%lf ", &pFisheyePanoParams->staOcamModels[k].invpol[i]);
            }

            fscanf(fp, "\n");
            fgets(buf, CMV_MAX_BUF, fp);    // camera centers
            fscanf(fp, "%lf %lf\n", &pFisheyePanoParams->staOcamModels[k].uc, &pFisheyePanoParams->staOcamModels[k].vc);

            fscanf(fp, "\n");
            fgets(buf, CMV_MAX_BUF, fp);    // affine parameters
            fscanf(fp, "%lf %lf %lf\n", &pFisheyePanoParams->staOcamModels[k].c, &pFisheyePanoParams->staOcamModels[k].d, &pFisheyePanoParams->staOcamModels[k].e);

            fscanf(fp, "\n");
            fgets(buf, CMV_MAX_BUF, fp);    // image height, image width
            fscanf(fp, "%d %d", &pFisheyePanoParams->staOcamModels[k].height, &pFisheyePanoParams->staOcamModels[k].width);

            fscanf(fp, "\n");
            fgets(buf, CMV_MAX_BUF, fp);    // vignette coeffs
            for (i = 0; i < VCF_FACTOR_NUM; i++)
            {
                fscanf(fp, "%lf ", &pFisheyePanoParams->staOcamModels[k].vcf_factors[i]);
            }
        }

        // ----------------
        fscanf(fp, "\n");
        fgets(buf, CMV_MAX_BUF, fp);    // camera extrinsic parameters
        fgets(buf, CMV_MAX_BUF, fp);    // camera 0 rotation mtx:
        for (i = 0; i < EXT_PARAM_R_MTX_NUM; i++)
        {
            fscanf(fp, "%lf ", &pFisheyePanoParams->staExtParam[0].rotationMtx[i]);
        }
        fgets(buf, CMV_MAX_BUF, fp);    // camera 0 translation vec
        for (i = 0; i < EXT_PARAM_T_VEC_NUM; i++)
        {
            fscanf(fp, "%lf ", &pFisheyePanoParams->staExtParam[0].translateVec[i]);
        }
        fgets(buf, CMV_MAX_BUF, fp);    // camera 1 rotation mtx
        for (i = 0; i < EXT_PARAM_R_MTX_NUM; i++)
        {
            fscanf(fp, "%lf ", &pFisheyePanoParams->staExtParam[1].rotationMtx[i]);
        }
        fgets(buf, CMV_MAX_BUF, fp);    // camera 1 translation vec
        for (i = 0; i < EXT_PARAM_T_VEC_NUM; i++)
        {
            fscanf(fp, "%lf ", &pFisheyePanoParams->staExtParam[1].translateVec[i]);
        }
        // reserved ----------------
        memset(pFisheyePanoParams->reserved, 0, sizeof(double)*RESERVED_PARAMS_NUM);
        fclose(fp);
    }
    return 0;
}

int writeFisheyePanoParamsToFile(char *filePath, char *fileName, fisheyePanoParams *pFisheyePanoParams, dataFileType type)
{// binary default file is used
    char fileFullName[512];

    FILE *fp = NULL;
    //errno_t err;
    char buf[CMV_MAX_BUF];
    int i, k;

    if (dat == type)
    {
        sprintf(fileFullName, "%s\\%s.dat", filePath, fileName);
        fp = fopen(fileFullName, "wb");
        if (fp == NULL)
        {
            return -1;
        }
        fwrite(pFisheyePanoParams, 1, sizeof(fisheyePanoParams), fp);
        fclose(fp);
    }
    else
    {
        sprintf(fileFullName, "%s\\%s.txt", filePath, fileName);
        fp = fopen(fileFullName, "wt");
        if (fp == NULL)
        {
            return -1;
        }
        //---------------- fisheyePanoParamsCore
        fprintf(fp, "fisheyePanoParamsCore =========================\n");
        fprintf(fp, "fisheyeImgW, fisheyeImgW:\n");
        fprintf(fp, "%d %d\n", pFisheyePanoParams->stFisheyePanoParamsCore.fisheyeImgW, pFisheyePanoParams->stFisheyePanoParamsCore.fisheyeImgH);

        fprintf(fp, "panoImgW, panoImgH:\n");
        fprintf(fp, "%d %d\n", pFisheyePanoParams->stFisheyePanoParamsCore.panoImgW, pFisheyePanoParams->stFisheyePanoParamsCore.panoImgH);

        fprintf(fp, "sphereRadius:\n");
        fprintf(fp, "%d\n", pFisheyePanoParams->stFisheyePanoParamsCore.sphereRadius);

        fprintf(fp, "maxFovAngle:\n");
        fprintf(fp, "%f\n", pFisheyePanoParams->stFisheyePanoParamsCore.maxFovAngle);

        //---------------- ocamModels
        fprintf(fp, "\nomni-camera models =========================");
        for (k = 0; k < 2; k++)
        {
            fprintf(fp, "\n");
            fprintf(fp, "ocamModel %d ---------- \n", k);

            fprintf(fp, "polynomial length:\n");
            fprintf(fp, "%d\n", pFisheyePanoParams->staOcamModels[k].length_pol);

            fprintf(fp, "polynomial coeffs:\n");
            for (i = 0; i < pFisheyePanoParams->staOcamModels[k].length_pol; i++)
            {
                fprintf(fp, "%e ", pFisheyePanoParams->staOcamModels[k].pol[i]);
            }

            fprintf(fp, "\ninv_polynomial length:\n");
            fprintf(fp, "%d\n", pFisheyePanoParams->staOcamModels[k].length_invpol);

            fprintf(fp, "inv_polynomial coeffs:\n");
            for (i = 0; i < pFisheyePanoParams->staOcamModels[k].length_invpol; i++)
            {
                fprintf(fp, "%e ", pFisheyePanoParams->staOcamModels[k].invpol[i]);
            }

            // ------------------- uc, vc, c, d, e
            fprintf(fp, "\nimage centers:\n");
            fprintf(fp, "%lf %lf\n", pFisheyePanoParams->staOcamModels[k].uc, pFisheyePanoParams->staOcamModels[k].vc);
        
            fprintf(fp, "affine parameters:\n");
            fprintf(fp, "%lf %lf %lf\n", pFisheyePanoParams->staOcamModels[k].c, pFisheyePanoParams->staOcamModels[k].d, pFisheyePanoParams->staOcamModels[k].e);

            fprintf(fp, "image height, image width:\n");
            fprintf(fp, "%d %d\n", pFisheyePanoParams->staOcamModels[k].height, pFisheyePanoParams->staOcamModels[k].width);

            fprintf(fp, "vignette coeffs:\n");
            for (i = 0; i < VCF_FACTOR_NUM; i++)
            {
                fprintf(fp, "%lf ", pFisheyePanoParams->staOcamModels[k].vcf_factors[i]);
            }
            fprintf(fp, "\n");
        }

        // ---------------- camera extrinsic
        fprintf(fp, "\ncamera extrinsic parameters =========================\n");
        fprintf(fp, "camera 0 rotation mtx:\n");
        for (i = 0; i < EXT_PARAM_R_MTX_NUM; i++)
        {
            fprintf(fp, "%lf ", pFisheyePanoParams->staExtParam[0].rotationMtx[i]);
        }
        fprintf(fp, "\n");
        fprintf(fp, "camera 0 translation vec:\n");
        for (i = 0; i < EXT_PARAM_T_VEC_NUM; i++)
        {
            fprintf(fp, "%lf ", pFisheyePanoParams->staExtParam[0].translateVec[i]);
        }

        fprintf(fp, "\n");
        fprintf(fp, "camera 1 rotation mtx:\n");
        for (i = 0; i < EXT_PARAM_R_MTX_NUM; i++)
        {
            fprintf(fp, "%lf ", pFisheyePanoParams->staExtParam[1].rotationMtx[i]);
        }
        fprintf(fp, "\n");
        fprintf(fp, "camera 1 translation vec:\n");
        for (i = 0; i < EXT_PARAM_T_VEC_NUM; i++)
        {
            fprintf(fp, "%lf ", pFisheyePanoParams->staExtParam[1].translateVec[i]);
        }

        fprintf(fp, "\n");

        // reserved ----------------
        fclose(fp);
    }
    return 0;
}

//#endif

}   // namespace YiPanorama