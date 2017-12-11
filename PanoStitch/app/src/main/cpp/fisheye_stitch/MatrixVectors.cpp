
#include "MatrixVectors.h"
#include <math.h>
#include <string.h>

namespace YiPanorama {
namespace util {

#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4

//------------------------------------------------------------------------------
void matrixDotMul(double *mtxA, double *mtxB, double *mtxC, int a, int b, int c)
{// mtxA(a x b) dot multiple mtxB(b x c) = mtxC(a x c)
    double sub = 0.0;
    int i, j, k;
    for (i = 0; i < a; i++)
    {
        for (j = 0; j < c; j++)
        {
            sub = 0.0;
            for (k = 0; k < b; k++)
            {
                sub += mtxA[i*b + k] * mtxB[k*c + j];
            }
            mtxC[i*c + j] = sub;
        }
    }
    return;
}

//------------------------------------------------------------------------------
void vectorAdd(double *vecA, double *vecB, double *vecC, int a)
{// vecA(a x 1) add up with vecB(a x 1) = vecC(a x 1)
    double sub = 0.0;
    int i, j, k;
    for (i = 0; i < a; i++)
    {
        vecC[i] = vecA[i] + vecB[i];
    }
    return;
}

//------------------------------------------------------------------------------
void genRotationMtxZYZ(double *R, double angleZ1, double angleY, double angleZ2)
{// consider the original position is frameA, which rotates to frameB,
 // the order of rotation angles are: Z1, Y, Z2
 // then the matrix R represents the rotation from frameB coordinates to frameA
 // which is: frameA(x,y,z) = R * frameB(x,y,z)

 // angleZ1: yaw,		angleY: pitch,		angleZ2: roll

    double radZ1 = angleZ1*M_PI / 180;
    double radY = angleY*M_PI / 180;
    double radZ2 = angleZ2*M_PI / 180;

    double Rz1[9], Ry[9], Rz2[9], Rtmp[9];

    Rz1[0] = cos(radZ1);	Rz1[1] = -sin(radZ1);	Rz1[2] = 0;
    Rz1[3] = sin(radZ1);	Rz1[4] = cos(radZ1);	Rz1[5] = 0;
    Rz1[6] = 0;			Rz1[7] = 0;			Rz1[8] = 1;

    Ry[0] = cos(radY);	Ry[1] = 0;		Ry[2] = sin(radY);
    Ry[3] = 0;			Ry[4] = 1;		Ry[5] = 0;
    Ry[6] = -sin(radY);	Ry[7] = 0;		Ry[8] = cos(radY);

    Rz2[0] = cos(radZ2);	Rz2[1] = -sin(radZ2);	Rz2[2] = 0;
    Rz2[3] = sin(radZ2);	Rz2[4] = cos(radZ2);	Rz2[5] = 0;
    Rz2[6] = 0;			Rz2[7] = 0;			Rz2[8] = 1;

    // Rz1 * Ry * Rz2, for interior coordinates reference system, this is used
    // Rz2 * Ry * Rz1, for exterior coordinates reference system

    matrixDotMul(Rz1, Ry, Rtmp, 3, 3, 3);
    matrixDotMul(Rtmp, Rz2, R, 3, 3, 3);

    return;
}

//------------------------------------------------------------------------------
void genContraryRotationMtxZYZ(double *R, double angleZ1, double angleY, double angleZ2)
{
    // THIS IS IMPORTANT !!!

    // consider the original position is frameA, which rotates to frameB,
    // the order of rotation angles are: Z1, Y, Z2
    // then the CONTRARY matrix R represents the rotation from frameA coordinates to frameB
    // which is: frameB(x,y,z) = R * frameA(x,y,z)

    // angleZ1: yaw,		angleY: pitch,		angleZ2: roll
    double radZ1 = -angleZ1*M_PI / 180;
    double radY = -angleY*M_PI / 180;
    double radZ2 = -angleZ2*M_PI / 180;

    double Rz1[9], Ry[9], Rz2[9], Rtmp[9];

    Rz1[0] = cos(radZ1);	Rz1[1] = -sin(radZ1);	Rz1[2] = 0;
    Rz1[3] = sin(radZ1);	Rz1[4] = cos(radZ1);	Rz1[5] = 0;
    Rz1[6] = 0;			Rz1[7] = 0;			Rz1[8] = 1;

    Ry[0] = cos(radY);	Ry[1] = 0;		Ry[2] = sin(radY);
    Ry[3] = 0;			Ry[4] = 1;		Ry[5] = 0;
    Ry[6] = -sin(radY);	Ry[7] = 0;		Ry[8] = cos(radY);

    Rz2[0] = cos(radZ2);	Rz2[1] = -sin(radZ2);	Rz2[2] = 0;
    Rz2[3] = sin(radZ2);	Rz2[4] = cos(radZ2);	Rz2[5] = 0;
    Rz2[6] = 0;			Rz2[7] = 0;			Rz2[8] = 1;

    // since it's contrary rotation, and interior coordinate reference system used,
    // so the order of matrix multiplication is (Rz2 * Ry * Rz1)
    matrixDotMul(Rz2, Ry, Rtmp, 3, 3, 3);
    matrixDotMul(Rtmp, Rz1, R, 3, 3, 3);

    return;
}

//------------------------------------------------------------------------------
void setTranslateVecXYZ(double *T, double *T_in, int size)
{
    memcpy(T, T_in, sizeof(double) * size);
    return;
}

//求三维矩阵的行列式det(M);
double getDetMat33f(const double *SrcMat)
{
    double detM(0);
    detM = SrcMat[0] * (SrcMat[4] * SrcMat[8] - SrcMat[5] * SrcMat[7])
        - SrcMat[1] * (SrcMat[3] * SrcMat[8] - SrcMat[5] * SrcMat[6])
        + SrcMat[2] * (SrcMat[3] * SrcMat[7] - SrcMat[4] * SrcMat[6]);
    return detM;
}

//求三维矩阵的伴随矩阵adj(M);
int getAdjMat33f(const double *SrcMat, double *AdjMat)
{
    AdjMat[0] = SrcMat[4] * SrcMat[8] - SrcMat[5] * SrcMat[7];
    AdjMat[3] = -(SrcMat[3] * SrcMat[8] - SrcMat[5] * SrcMat[6]);
    AdjMat[6] = SrcMat[3] * SrcMat[7] - SrcMat[4] * SrcMat[6];
    AdjMat[1] = -(SrcMat[1] * SrcMat[8] - SrcMat[2] * SrcMat[7]);
    AdjMat[4] = SrcMat[0] * SrcMat[8] - SrcMat[2] * SrcMat[6];
    AdjMat[7] = -(SrcMat[0] * SrcMat[7] - SrcMat[1] * SrcMat[6]);
    AdjMat[2] = SrcMat[1] * SrcMat[5] - SrcMat[2] * SrcMat[4];
    AdjMat[5] = -(SrcMat[0] * SrcMat[5] - SrcMat[2] * SrcMat[3]);
    AdjMat[8] = SrcMat[0] * SrcMat[4] - SrcMat[1] * SrcMat[3];
    return 0;
}

//求逆矩阵inv(M);
int getInvMat33f(const double *SrcMat, double *InvMat)
{
    double DetMat = getDetMat33f(SrcMat);
    double AdjMat[9] = { 0 };
    getAdjMat33f(SrcMat, AdjMat);
    for (int i(0); i != 9; ++i)
    {
        InvMat[i] = AdjMat[i] / DetMat;
    }
    return 0;
}

//------------------------------------------------------------------------------
void contraryRotationMtx(double *srcRotMtx, double *dstRotMtx, int dim)
{// since the rotation matrix's transpose equals to its inverse
 // so just transpose it to get its inverse matrix
    //for (int k = 0; k < dim; k++)
    //{
    //    for (int m = 0; m < dim; m++)
    //    {
    //        dstRotMtx[k * dim + m] = srcRotMtx[m * dim + k];
    //    }
    //}
    //return;
    getInvMat33f(srcRotMtx, dstRotMtx);
    
    return ;
}

int calcInvExtMatrix(double *pExt, double *R, double *T, double* pR, double* pT)
{
    int i;
    double R1[3], R2[3], R3[3], tT[3];

    // calculate reproject matrix
    for (i = 0; i < 3; i++)
    {
        R1[i] = pExt[3 * i];
        R2[i] = pExt[3 * i + 1];
        T[i] = pExt[3 * i + 2]; // chessboard to camera T
        tT[i] = -T[i];
    }
    R3[0] = R1[1] * R2[2] - R1[2] * R2[1];
    R3[1] = R1[2] * R2[0] - R1[0] * R2[2];
    R3[2] = R1[0] * R2[1] - R1[1] * R2[0];
    for (i = 0; i < 3; i++)
    {
        R[3 * i] = R1[i];
        R[3 * i + 1] = R2[i];
        R[3 * i + 2] = R3[i];   // chessboard to camera R
    }

#if 1
    getInvMat33f(R, pR);            // camera to chessboard R
#else
    contraryRotationMtx(R, pR, 3);  // camera to chessboard R
#endif

    matrixDotMul(pR, tT, pT, 3, 3, 1);  // camera to chessboard T

    return 1;
}

}   // namespace util
}   // namespace YiPanorama
