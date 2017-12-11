
/************************************************************************/
/* matrix and vector operations                                         */
/*                                                                      */
/************************************************************************/
#pragma once
#ifndef _MATRIX_VECTOR_H
#define _MATRIX_VECTOR_H

namespace YiPanorama {
namespace util {

// function declarations ========================================================
// matrix multiplication, mtxA(a x b) dot multiple mtxB(b x c) = mtxC(a x c)
void matrixDotMul(double *mtxA, double *mtxB, double *mtxC, int a, int b, int c);

// vector addition, vecA(a x 1) add up with vecB(a x 1) = vecC(a x 1)
void vectorAdd(double *vecA, double *vecB, double *vecC, int a);

// generate rotation matrix using Euler angles
void genRotationMtxZYZ(double *R, double angleZ1, double angleY, double angleZ2);

// generate the contrary rotation matrix using Euler angles
void genContraryRotationMtxZYZ(double *R, double angleZ1, double angleY, double angleZ2);

// generate translation vector
void setTranslateVecXYZ(double *T, double *T_in, int size);

// calculate contrary rotation matrix by transpose transformation
void contraryRotationMtx(double *srcRotMtx, double *dstRotMtx, int dim);

int calcInvExtMatrix(double *pExt, double *R, double *T, double* pR, double* pT);

}   // namespace util
}   // namespace YiPanorama

#endif  // !_MATRIX_VECTOR_H
