
#include "FeatureBasedOptimization.h"
#include "MatrixVectors.h"
#include "ImageIOConverter.h"
#include "ImageWarpTable.h"
#include "ImageTailor.h"
#include <math.h>

#include <opencv.hpp>
#include <xfeatures2d.hpp>
#include <calib3d/calib3d.hpp>
#include <highgui/highgui.hpp>
#include <imgproc/imgproc.hpp>

#include <levmar.h>

namespace YiPanorama {
namespace calibration {

using namespace util;
using namespace warper;
using namespace cv;
using namespace std;

#define POLY_DEGREE 4
#define SIG(a,b) ((b) > 0 ? fabs(a) : -fabs(a))
#define SIGN(x) ((x)<0?-1:((x)>0?1:0))

#define IMAGE_CENTER_NUM 2
#define AFFINE_PARAM_NUM 3

#define ASSEMBLE_ERR    2*120   // um
#define PIXEL_SIZE      1.55    // um
#define CENTER_STEP 5
#define CENTER_OFFSET_PIXEL (((int)(ASSEMBLE_ERR/PIXEL_SIZE)/CENTER_STEP)*CENTER_STEP)
#define THRESHOLD_CENTER_DIS 20.0
#define THRESHOLD_BLUR 50.0

#define OCAM_MODEL_WEIGHTS 2
#define ROT_VEC_DIM 3	
#define IMAGE_CENTER_MAGNIFY 1000
#define TRAN_VEC_MAGNIFY 1000
#define SPHERE_RADIUS_MAGNIFY 1000

enum RotationMtxVecTrans
{
    rtMtx2Vec,   // convert rotation matrix to vector
    rtVec2Mtx    // convert rotation vector to matrix
};

struct optiDataExtrnAndIntrn
{// extra data needed in the extrinsic parameter optimization
    int sphereRadius;
    double polParamWeights[2];       // for both cameras' intrinsic polynomial interpolation coefficients
    matchPoint *pMatchedPoints;
    ImageWarper *pImgWarper;  // pointer to the warp table(s)
    cameraMetadata *pCamera;        // pointer to the camera model(s)
    ocamModel stOcamModelDesign[2];  // ocam model from design table(curves too much at fov range of 80-100)
    ocamModel stOcamModelCalib; // ocam model from calibrated result(curves too straight at fov range of 80-100)
};

struct optiDataCentExt
{// extra data needed in the intrinsic parameter optimization
    chessboardCorner *pChessboardCorner;
    cameraMetadata *pCamera;
    double errSum;
};

struct optiDataRadius
{// extra data needed in the sphere radius optimization
    matchPoint *pMatchedPoints; // matched points
    ImageWarper *pImgWarper;    // pointer to the warp table(s)
    cameraMetadata *pCamera;    // pointer to the camera model(s)
};

// functions definitions ===============================================
bool findCornersMat(Mat gray, Size board_size, vector<Point2f>& corners)
{
    bool found = findChessboardCorners(gray, board_size, corners,
        CALIB_CB_ADAPTIVE_THRESH | CALIB_CB_FILTER_QUADS | CALIB_CB_FAST_CHECK);

    if (corners.size() != 0)
        cornerSubPix(gray, corners, Size(11, 11), Size(-1, -1),
            TermCriteria(TermCriteria::EPS | TermCriteria::MAX_ITER, 30, 0.1));

    return found;
}

int drawCornersRaw(Mat image, int pointNum, chessboardCorner *pChessboardCorners)
{
    int ptX, ptY;

    for (int k = 0; k < pointNum; k++)
    {
        ptX = pChessboardCorners[k].dV;
        ptY = pChessboardCorners[k].dU;
        
        line(image, cvPoint(ptX - 3, ptY), cvPoint(ptX + 3, ptY), CV_RGB(255, 0, 0), 3);
        line(image, cvPoint(ptX, ptY - 3), cvPoint(ptX, ptY + 3), CV_RGB(255, 0, 0), 3);
    }

    //imwrite("corners.jpg", image);
    return 0;
}

int drawProjectPoints(char *fileName, cameraMetadata *pCamera, int pointNum, chessboardCorner *pChessboardCorners)
{
    Mat image;
    image = imread("corners.jpg");
    double world[2], cam[3], img[2];
    int ptX, ptY;

    for (int k = 0; k < pointNum; k++)
    {
        world[0] = pChessboardCorners[k].iX;
        world[1] = pChessboardCorners[k].iY;
        pCamera->chess2cam(world, cam);
        pCamera->cam2img(img, cam);

        ptX = img[1];
        ptY = img[0];
        line(image, cvPoint(ptX - 3, ptY), cvPoint(ptX + 3, ptY), CV_RGB(0, 255, 0), 1);
        line(image, cvPoint(ptX, ptY - 3), cvPoint(ptX, ptY + 3), CV_RGB(0, 255, 0), 1);

    }

    imwrite(fileName, image);
    return 0;
}

int findCornersFrame(imageFrame fisheyeImage, int checkerNumH, int checkerNumV, int checkerSize, int &pointNum, chessboardCorner **pChessboardCorners)
{// find chessboard corners, both image coordinates and chessboard coordinates
 // desired num of points should be obtained, so the pChessboardCorners should has memories pre-allocated

    Mat img, img_gray;
    img.create(Size(fisheyeImage.imageW, fisheyeImage.imageH), CV_8UC3);
    img_gray.create(Size(fisheyeImage.imageW, fisheyeImage.imageH), CV_8UC1);

    // covert to rgb, and transform to mat
    convertImageFrametoCvMat(fisheyeImage, img);
    cvtColor(img, img_gray, CV_RGB2GRAY);

    vector<Point2f> corners;
    Size boardSize(checkerNumH, checkerNumV);

    bool found = findCornersMat(img_gray, boardSize, corners);
    if (found == 0)
    {
        //cerr << "function setCornerData() error, can't find all corners" << endl;
        return -1;
    }

    pointNum = boardSize.height * boardSize.width;
    *pChessboardCorners = new chessboardCorner[pointNum];

    for (size_t h = 0; h != boardSize.height; ++h)
    {
        for (size_t w = 0; w != boardSize.width; ++w)
        {
            size_t idx = h * boardSize.width + w;
            (*pChessboardCorners)[idx].dU = corners[idx].y;	//vertical
            (*pChessboardCorners)[idx].dV = corners[idx].x;	//horizontal
            (*pChessboardCorners)[idx].iX = h * checkerSize;	//vertical
            (*pChessboardCorners)[idx].iY = w * checkerSize;	//horizontal
        }
    }

#if 0
    drawCornersRaw(img, pointNum, *pChessboardCorners);
#endif

    return 0;
}

bool plot_RR(double* RRdef, double *int_par, struct chessboardCorner *cornerData, int cornerCount)
{
    bool bResult = false;
    int i, j;
    double sg[2] = { 1, -1 };

    double rho, rho2;
    CvMat *P = cvCreateMat(cornerCount * 2, 4, CV_64F);
    CvMat *Temp = cvCreateMat(4, 1, CV_64F);
    CvMat *Q = cvCreateMat(cornerCount * 2, 1, CV_64F);
    double *PP = P->data.db;
    double *temp = Temp->data.db;
    double *QQ = Q->data.db;
    double MA, MC;
    double Xp_abs, Yp_abs;

    for (i = 0; i < 2; i++)
    {
        for (j = 0; j < 9; j++)
            RRdef[j] *= sg[i];
        if (SIGN(RRdef[2]) != SIGN(cornerData[0].dU - int_par[3]) || SIGN(RRdef[5]) != SIGN(cornerData[0].dV - int_par[4]))
            continue;

        for (j = 0; j < cornerCount; j++)
        {
            Xp_abs = cornerData[j].dU - int_par[3];
            Yp_abs = cornerData[j].dV - int_par[4];
            MA = RRdef[3] * cornerData[j].iX + RRdef[4] * cornerData[j].iY + RRdef[5];
            MC = RRdef[0] * cornerData[j].iX + RRdef[1] * cornerData[j].iY + RRdef[2];
            rho2 = pow(Xp_abs, 2.0) + pow(Yp_abs, 2.0);
            rho = sqrt(rho2);
            PP[j * 4] = MA;
            PP[j * 4 + 1] = MA*rho;
            PP[j * 4 + 2] = MA*rho2;
            PP[j * 4 + 3] = -Yp_abs;
            PP[(j + cornerCount) * 4] = MC;
            PP[(j + cornerCount) * 4 + 1] = MC*rho;
            PP[(j + cornerCount) * 4 + 2] = MC*rho2;
            PP[(j + cornerCount) * 4 + 3] = -Xp_abs;
            QQ[j] = Yp_abs*(RRdef[6] * cornerData[j].iX + RRdef[7] * cornerData[j].iY);
            QQ[j + cornerCount] = Xp_abs*(RRdef[6] * cornerData[j].iX + RRdef[7] * cornerData[j].iY);
        }

        cvSolve(P, Q, Temp, CV_SVD);
        if (Temp->data.db[2] >= 0)
        {
            bResult = true;
            break;
        }
        bResult = true;
        break;
    }

    cvReleaseMat(&P);
    cvReleaseMat(&Temp);
    cvReleaseMat(&Q);
    return bResult;
}

void ppp(double a[], double e[], double s[], double v[], int m, int n)
{
    int i, j, p, q;
    double d;

    if (m >= n)
        i = n;
    else
        i = m;

    for (j = 1; j <= i - 1; j++)
    {
        a[(j - 1)*n + j - 1] = s[j - 1];
        a[(j - 1)*n + j] = e[j - 1];
    }

    a[(i - 1)*n + i - 1] = s[i - 1];
    if (m < n)
        a[(i - 1)*n + i] = e[i - 1];

    for (i = 1; i <= n - 1; i++)
    {
        for (j = i + 1; j <= n; j++)
        {
            p = (i - 1)*n + j - 1;
            q = (j - 1)*n + i - 1;
            d = v[p];
            v[p] = v[q];
            v[q] = d;
        }
    }
}

void sss(double fg[2], double cs[2])
{
    double r, d;

    if ((fabs(fg[0]) + fabs(fg[1])) == 0.0)
    {
        cs[0] = 1.0;
        cs[1] = 0.0;
        d = 0.0;
    }
    else
    {
        d = sqrt(fg[0] * fg[0] + fg[1] * fg[1]);
        if (fabs(fg[0]) > fabs(fg[1]))
        {
            d = fabs(d);
            if (fg[0] < 0.0)
                d = -d;
        }
        if (fabs(fg[1]) >= fabs(fg[0]))
        {
            d = fabs(d);
            if (fg[1]<0.0)
                d = -d;
        }

        cs[0] = fg[0] / d;
        cs[1] = fg[1] / d;
    }

    r = 1.0;
    if (fabs(fg[0])>fabs(fg[1]))
        r = cs[1];
    else if (cs[0] != 0.0)
        r = 1.0 / cs[0];

    fg[0] = d;
    fg[1] = r;
}

bool splitUV(double *m_pData, double* mtxU, double* mtxV, int row, int col)
{
    int i, j, k, l, it, ll, kk, ix, iy, mm, nn, iz, m1, ks;
    double d, dd, t, sm, sm1, em1, sk, ek, b, c, shh, fg[2], cs[2];
    double *s, *e, *w;
    double eps = 0.000001;
    int m = row;
    int n = col;
    int ka = (m > n ? m : n) + 1;
    s = (double*)calloc(ka, sizeof(double));
    e = (double*)calloc(ka, sizeof(double));
    w = (double*)calloc(ka, sizeof(double));
    k = n;
    if (m - 1 < n)
        k = m - 1;//U进行K次迭代，V进行l次迭代
    l = m;
    if (n - 2 < m)
        l = n - 2;
    if (l < 0)
        l = 0;
    // 循环迭代计算
    ll = k;
    if (l > k)
        ll = l;
    if (ll >= 1)
    {
        for (kk = 1; kk <= ll; kk++)
        {
            if (kk <= k)
            {
                d = 0.0;
                for (i = kk; i <= m; i++)
                {
                    ix = (i - 1)*n + kk - 1;
                    d = d + m_pData[ix] * m_pData[ix];
                }

                s[kk - 1] = sqrt(d);
                if (s[kk - 1] != 0.0)
                {
                    ix = (kk - 1)*n + kk - 1;
                    if (m_pData[ix] != 0.0)
                    {
                        s[kk - 1] = fabs(s[kk - 1]);
                        if (m_pData[ix] < 0.0)
                            s[kk - 1] = -s[kk - 1];
                    }

                    for (i = kk; i <= m; i++)
                    {
                        iy = (i - 1)*n + kk - 1;
                        m_pData[iy] = m_pData[iy] / s[kk - 1];
                    }

                    m_pData[ix] = 1.0 + m_pData[ix];
                }

                s[kk - 1] = -s[kk - 1];
            }

            if (n >= kk + 1)
            {
                for (j = kk + 1; j <= n; j++)
                {
                    if ((kk <= k) && (s[kk - 1] != 0.0))
                    {
                        d = 0.0;
                        for (i = kk; i <= m; i++)
                        {
                            ix = (i - 1)*n + kk - 1;
                            iy = (i - 1)*n + j - 1;
                            d = d + m_pData[ix] * m_pData[iy];
                        }

                        d = -d / m_pData[(kk - 1)*n + kk - 1];
                        for (i = kk; i <= m; i++)
                        {
                            ix = (i - 1)*n + j - 1;
                            iy = (i - 1)*n + kk - 1;
                            m_pData[ix] = m_pData[ix] + d*m_pData[iy];
                        }
                    }

                    e[j - 1] = m_pData[(kk - 1)*n + j - 1];
                }
            }
            double pp;
            if (kk <= k)
            {
                for (i = kk; i <= m; i++)
                {
                    ix = (i - 1)*m + kk - 1;
                    iy = (i - 1)*n + kk - 1;
                    pp = m_pData[iy];
                    pp = mtxU[ix];
                    mtxU[ix] = m_pData[iy];
                }
            }

            if (kk <= l)
            {
                d = 0.0;
                for (i = kk + 1; i <= n; i++)
                    d = d + e[i - 1] * e[i - 1];

                e[kk - 1] = sqrt(d);
                if (e[kk - 1] != 0.0)
                {
                    if (e[kk] != 0.0)
                    {
                        e[kk - 1] = fabs(e[kk - 1]);
                        if (e[kk] < 0.0)
                            e[kk - 1] = -e[kk - 1];
                    }

                    for (i = kk + 1; i <= n; i++)
                        e[i - 1] = e[i - 1] / e[kk - 1];

                    e[kk] = 1.0 + e[kk];
                }

                e[kk - 1] = -e[kk - 1];
                if ((kk + 1 <= m) && (e[kk - 1] != 0.0))
                {
                    for (i = kk + 1; i <= m; i++)
                        w[i - 1] = 0.0;

                    for (j = kk + 1; j <= n; j++)
                        for (i = kk + 1; i <= m; i++)
                            w[i - 1] = w[i - 1] + e[j - 1] * m_pData[(i - 1)*n + j - 1];

                    for (j = kk + 1; j <= n; j++)
                    {
                        for (i = kk + 1; i <= m; i++)
                        {
                            ix = (i - 1)*n + j - 1;
                            m_pData[ix] = m_pData[ix] - w[i - 1] * e[j - 1] / e[kk];
                        }
                    }
                }

                for (i = kk + 1; i <= n; i++)
                    mtxV[(i - 1)*n + kk - 1] = e[i - 1];
            }

        }
    }

    mm = n;
    if (m + 1 < n)
        mm = m + 1;
    if (k < n)
        s[k] = m_pData[k*n + k];
    if (m < mm)
        s[mm - 1] = 0.0;
    if (l + 1 < mm)
        e[l] = m_pData[l*n + mm - 1];

    e[mm - 1] = 0.0;
    nn = m;
    if (m > n)
        nn = n;
    if (nn >= k + 1)
    {
        for (j = k + 1; j <= nn; j++)
        {
            for (i = 1; i <= m; i++)
                mtxU[(i - 1)*m + j - 1] = 0.0;
            mtxU[(j - 1)*m + j - 1] = 1.0;
        }
    }

    if (k >= 1)
    {
        for (ll = 1; ll <= k; ll++)
        {
            kk = k - ll + 1;
            iz = (kk - 1)*m + kk - 1;
            if (s[kk - 1] != 0.0)
            {
                if (nn >= kk + 1)
                {
                    for (j = kk + 1; j <= nn; j++)
                    {
                        d = 0.0;
                        for (i = kk; i <= m; i++)
                        {
                            ix = (i - 1)*m + kk - 1;
                            iy = (i - 1)*m + j - 1;
                            d = d + mtxU[ix] * mtxU[iy] / mtxU[iz];
                        }

                        d = -d;
                        for (i = kk; i <= m; i++)
                        {
                            ix = (i - 1)*m + j - 1;
                            iy = (i - 1)*m + kk - 1;
                            mtxU[ix] = mtxU[ix] + d*mtxU[iy];
                        }
                    }
                }

                for (i = kk; i <= m; i++)
                {
                    ix = (i - 1)*m + kk - 1;
                    mtxU[ix] = -mtxU[ix];
                }

                mtxU[iz] = 1.0 + mtxU[iz];
                if (kk - 1 >= 1)
                {
                    for (i = 1; i <= kk - 1; i++)
                        mtxU[(i - 1)*m + kk - 1] = 0.0;
                }
            }
            else
            {
                for (i = 1; i <= m; i++)
                    mtxU[(i - 1)*m + kk - 1] = 0.0;
                mtxU[(kk - 1)*m + kk - 1] = 1.0;
            }
        }
    }

    for (ll = 1; ll <= n; ll++)
    {
        kk = n - ll + 1;
        iz = kk*n + kk - 1;

        if ((kk <= l) && (e[kk - 1] != 0.0))
        {
            for (j = kk + 1; j <= n; j++)
            {
                d = 0.0;
                for (i = kk + 1; i <= n; i++)
                {
                    ix = (i - 1)*n + kk - 1;
                    iy = (i - 1)*n + j - 1;
                    d = d + mtxV[ix] * mtxV[iy] / mtxV[iz];
                }

                d = -d;
                for (i = kk + 1; i <= n; i++)
                {
                    ix = (i - 1)*n + j - 1;
                    iy = (i - 1)*n + kk - 1;
                    mtxV[ix] = mtxV[ix] + d*mtxV[iy];
                }
            }
        }

        for (i = 1; i <= n; i++)
            mtxV[(i - 1)*n + kk - 1] = 0.0;

        mtxV[iz - n] = 1.0;
    }

    for (i = 1; i <= m; i++)
        for (j = 1; j <= n; j++)
            m_pData[(i - 1)*n + j - 1] = 0.0;

    m1 = mm;
    it = 60;
    while (true)
    {
        if (mm == 0)
        {
            ppp(m_pData, e, s, mtxV, m, n);
            free(s);
            free(e);
            free(w);
            return true;
        }
        if (it == 0)
        {
            ppp(m_pData, e, s, mtxV, m, n);
            free(s);
            free(e);
            free(w);
            return false;
        }

        kk = mm - 1;
        while ((kk != 0) && (fabs(e[kk - 1]) != 0.0))
        {
            d = fabs(s[kk - 1]) + fabs(s[kk]);
            dd = fabs(e[kk - 1]);
            if (dd > eps*d)
                kk = kk - 1;
            else
                e[kk - 1] = 0.0;
        }

        if (kk == mm - 1)
        {
            kk = kk + 1;
            if (s[kk - 1] < 0.0)
            {
                s[kk - 1] = -s[kk - 1];
                for (i = 1; i <= n; i++)
                {
                    ix = (i - 1)*n + kk - 1;
                    mtxV[ix] = -mtxV[ix];
                }
            }

            while ((kk != m1) && (s[kk - 1] < s[kk]))
            {
                d = s[kk - 1];
                s[kk - 1] = s[kk];
                s[kk] = d;
                if (kk < n)
                {
                    for (i = 1; i <= n; i++)
                    {
                        ix = (i - 1)*n + kk - 1;
                        iy = (i - 1)*n + kk;
                        d = mtxV[ix];
                        mtxV[ix] = mtxV[iy];
                        mtxV[iy] = d;
                    }
                }

                if (kk < m)
                {
                    for (i = 1; i <= m; i++)
                    {
                        ix = (i - 1)*m + kk - 1;
                        iy = (i - 1)*m + kk;
                        d = mtxU[ix];
                        mtxU[ix] = mtxU[iy];
                        mtxU[iy] = d;
                    }
                }

                kk = kk + 1;
            }

            it = 60;
            mm = mm - 1;
        }
        else
        {
            ks = mm;
            while ((ks > kk) && (fabs(s[ks - 1]) != 0.0))
            {
                d = 0.0;
                if (ks != mm)
                    d = d + fabs(e[ks - 1]);
                if (ks != kk + 1)
                    d = d + fabs(e[ks - 2]);

                dd = fabs(s[ks - 1]);
                if (dd > eps*d)
                    ks = ks - 1;
                else
                    s[ks - 1] = 0.0;
            }

            if (ks == kk)
            {
                kk = kk + 1;
                d = fabs(s[mm - 1]);
                t = fabs(s[mm - 2]);
                if (t > d)
                    d = t;

                t = fabs(e[mm - 2]);
                if (t > d)
                    d = t;

                t = fabs(s[kk - 1]);
                if (t > d)
                    d = t;

                t = fabs(e[kk - 1]);
                if (t > d)
                    d = t;

                sm = s[mm - 1] / d;
                sm1 = s[mm - 2] / d;
                em1 = e[mm - 2] / d;
                sk = s[kk - 1] / d;
                ek = e[kk - 1] / d;
                b = ((sm1 + sm)*(sm1 - sm) + em1*em1) / 2.0;
                c = sm*em1;
                c = c*c;
                shh = 0.0;

                if ((b != 0.0) || (c != 0.0))
                {
                    shh = sqrt(b*b + c);
                    if (b < 0.0)
                        shh = -shh;

                    shh = c / (b + shh);
                }

                fg[0] = (sk + sm)*(sk - sm) - shh;
                fg[1] = sk*ek;
                for (i = kk; i <= mm - 1; i++)
                {
                    sss(fg, cs);
                    if (i != kk)
                        e[i - 2] = fg[0];

                    fg[0] = cs[0] * s[i - 1] + cs[1] * e[i - 1];
                    e[i - 1] = cs[0] * e[i - 1] - cs[1] * s[i - 1];
                    fg[1] = cs[1] * s[i];
                    s[i] = cs[0] * s[i];

                    if ((cs[0] != 1.0) || (cs[1] != 0.0))
                    {
                        for (j = 1; j <= n; j++)
                        {
                            ix = (j - 1)*n + i - 1;
                            iy = (j - 1)*n + i;
                            d = cs[0] * mtxV[ix] + cs[1] * mtxV[iy];
                            mtxV[iy] = -cs[1] * mtxV[ix] + cs[0] * mtxV[iy];
                            mtxV[ix] = d;
                        }
                    }

                    sss(fg, cs);
                    s[i - 1] = fg[0];
                    fg[0] = cs[0] * e[i - 1] + cs[1] * s[i];
                    s[i] = -cs[1] * e[i - 1] + cs[0] * s[i];
                    fg[1] = cs[1] * e[i];
                    e[i] = cs[0] * e[i];

                    if (i < m)
                    {
                        if ((cs[0] != 1.0) || (cs[1] != 0.0))
                        {
                            for (j = 1; j <= m; j++)
                            {
                                ix = (j - 1)*m + i - 1;
                                iy = (j - 1)*m + i;
                                d = cs[0] * mtxU[ix] + cs[1] * mtxU[iy];
                                mtxU[iy] = -cs[1] * mtxU[ix] + cs[0] * mtxU[iy];
                                mtxU[ix] = d;
                            }
                        }
                    }
                }

                e[mm - 2] = fg[0];
                it = it - 1;
            }
            else
            {
                if (ks == mm)
                {
                    kk = kk + 1;
                    fg[1] = e[mm - 2];
                    e[mm - 2] = 0.0;
                    for (ll = kk; ll <= mm - 1; ll++)
                    {
                        i = mm + kk - ll - 1;
                        fg[0] = s[i - 1];
                        sss(fg, cs);
                        s[i - 1] = fg[0];
                        if (i != kk)
                        {
                            fg[1] = -cs[1] * e[i - 2];
                            e[i - 2] = cs[0] * e[i - 2];
                        }

                        if ((cs[0] != 1.0) || (cs[1] != 0.0))
                        {
                            for (j = 1; j <= n; j++)
                            {
                                ix = (j - 1)*n + i - 1;
                                iy = (j - 1)*n + mm - 1;
                                d = cs[0] * mtxV[ix] + cs[1] * mtxV[iy];
                                mtxV[iy] = -cs[1] * mtxV[ix] + cs[0] * mtxV[iy];
                                mtxV[ix] = d;
                            }
                        }
                    }
                }
                else
                {
                    kk = ks + 1;
                    fg[1] = e[kk - 2];
                    e[kk - 2] = 0.0;
                    for (i = kk; i <= mm; i++)
                    {
                        fg[0] = s[i - 1];
                        sss(fg, cs);
                        s[i - 1] = fg[0];
                        fg[1] = -cs[1] * e[i - 1];
                        e[i - 1] = cs[0] * e[i - 1];
                        if ((cs[0] != 1.0) || (cs[1] != 0.0))
                        {
                            for (j = 1; j <= m; j++)
                            {
                                ix = (j - 1)*m + i - 1;
                                iy = (j - 1)*m + kk - 2;
                                d = cs[0] * mtxU[ix] + cs[1] * mtxU[iy];
                                mtxU[iy] = -cs[1] * mtxU[ix] + cs[0] * mtxU[iy];
                                mtxU[ix] = d;
                            }
                        }
                    }
                }
            }
        }
    }
    free(s);
    free(e);
    free(w);
    return true;
}

bool calcExtParam(double *RRdef, double *ss, double *int_par, chessboardCorner *cornerData, int iCornerCount)
{
    bool bState = false;
    int i, j, k;
    double R11, R12, T1, R21, R22, T2, R31, R32, T3;
    double dlenR1;

    double c, d, e, uc, vc;
    double invdet;
    double u1, v1, u, v, rho, rhoi, frho;
    double frhoX, frhoY, uX, uY, vX, vY;
    double *Mmtx, *Umtx, *Vmtx;
    CvMat *MatM, *MatU, *MatV;

    c = int_par[0];
    d = int_par[1];
    e = int_par[2];
    uc = int_par[3];	//vertical
    vc = int_par[4];	//horizontal
    invdet = 1 / (c - d*e);
    MatM = cvCreateMat(iCornerCount * 3, 9, CV_64F);
    MatU = cvCreateMat(3 * iCornerCount, 3 * iCornerCount, CV_64F);
    MatV = cvCreateMat(9, 9, CV_64F);
    Mmtx = MatM->data.db;
    Umtx = MatU->data.db;
    Vmtx = MatV->data.db;

    for (i = 0; i < iCornerCount; i++)
    {
        j = i * 27;
        u1 = cornerData[i].dU - uc;	//vertical
        v1 = cornerData[i].dV - vc;	//horizontal
        u = invdet*(u1 - d*v1);
        v = invdet*(c*v1 - e*u1);

        rho = sqrt(u*u + v*v);
        frho = ss[0];
        rhoi = 1;
        for (k = 1; k <= POLY_DEGREE; k++)
        {
            rhoi *= rho;
            frho += ss[k] * rhoi;
        }
        frhoX = frho*cornerData[i].iX;
        frhoY = frho*cornerData[i].iY;
        uX = u*cornerData[i].iX;
        uY = u*cornerData[i].iY;
        vX = v*cornerData[i].iX;
        vY = v*cornerData[i].iY;
        Mmtx[j + 0] = 0;
        Mmtx[j + 1] = 0;
        Mmtx[j + 2] = 0;
        Mmtx[j + 3] = -frhoX;
        Mmtx[j + 4] = -frhoY;
        Mmtx[j + 5] = -frho;
        Mmtx[j + 6] = vX;
        Mmtx[j + 7] = vY;
        Mmtx[j + 8] = v;
        Mmtx[j + 9] = frhoX;
        Mmtx[j + 10] = frhoY;
        Mmtx[j + 11] = frho;
        Mmtx[j + 12] = 0;
        Mmtx[j + 13] = 0;
        Mmtx[j + 14] = 0;
        Mmtx[j + 15] = -uX;
        Mmtx[j + 16] = -uY;
        Mmtx[j + 17] = -u;
        Mmtx[j + 18] = -vX;
        Mmtx[j + 19] = -vY;
        Mmtx[j + 20] = -v;
        Mmtx[j + 21] = uX;
        Mmtx[j + 22] = uY;
        Mmtx[j + 23] = u;
        Mmtx[j + 24] = 0;
        Mmtx[j + 25] = 0;
        Mmtx[j + 26] = 0;
    }

    if (splitUV(Mmtx, Umtx, Vmtx, 3 * iCornerCount, 9))
    {
        R11 = Vmtx[8 * 9];
        R12 = Vmtx[8 * 9 + 1];
        T1 = Vmtx[8 * 9 + 2];
        R21 = Vmtx[8 * 9 + 3];
        R22 = Vmtx[8 * 9 + 4];
        T2 = Vmtx[8 * 9 + 5];
        R31 = Vmtx[8 * 9 + 6];
        R32 = Vmtx[8 * 9 + 7];
        T3 = Vmtx[8 * 9 + 8];

        dlenR1 = 1 / sqrt(pow(R11, 2.0) + pow(R21, 2.0) + pow(R31, 2.0));
        RRdef[0] = dlenR1*R11;
        RRdef[1] = dlenR1*R12;
        RRdef[2] = dlenR1*T1;
        RRdef[3] = dlenR1*R21;
        RRdef[4] = dlenR1*R22;
        RRdef[5] = dlenR1*T2;
        RRdef[6] = dlenR1*R31;
        RRdef[7] = dlenR1*R32;
        RRdef[8] = dlenR1*T3;
        if (plot_RR(RRdef, int_par, cornerData, iCornerCount))
            bState = true;
    }

    cvReleaseMat(&MatM);
    cvReleaseMat(&MatU);
    cvReleaseMat(&MatV);
    return bState;
}

void rotationMtxVecTrans(double *pRotMtx, double *pRotVec, RotationMtxVecTrans direction)
{// conversion between rotation matrix and vector

    int k, m;
    CvMat *RotMtx = cvCreateMat(3, 3, CV_32F);
    CvMat *RotVec = cvCreateMat(3, 1, CV_32F);

    switch (direction)
    {
    case rtMtx2Vec:
        // initialize input
        for (k = 0; k < 3; k++)
        {
            for (m = 0; m < 3; m++)
            {
                cvmSet(RotMtx, k, m, pRotMtx[k * 3 + m]);
            }
        }
        cvRodrigues2(RotMtx, RotVec);
        // output result
        for (k = 0; k < 3; k++)
        {
            pRotVec[k] = cvmGet(RotVec, k, 0);
        }
        break;

    case rtVec2Mtx:

        for (k = 0; k < 3; k++)
        {
            cvmSet(RotVec, k, 0, pRotVec[k]);
        }

        // conversion
        cvRodrigues2(RotVec, RotMtx);

        // output result
        for (k = 0; k < 3; k++)
        {
            for (m = 0; m < 3; m++)
            {
                pRotMtx[k * 3 + m] = cvmGet(RotMtx, k, m);
            }
        }
        break;

    default:
        break;
    }

    cvReleaseMat(&RotMtx);
    cvReleaseMat(&RotVec);

    return;
}

int RotAndTrans(double point3Dd[3], double point3Ds[3], const double *Rc2p)
{
    point3Dd[0] = Rc2p[0] * point3Ds[0] + Rc2p[1] * point3Ds[1] + Rc2p[2] * point3Ds[2]/* + Rc2p[3]*/;
    point3Dd[1] = Rc2p[4] * point3Ds[0] + Rc2p[5] * point3Ds[1] + Rc2p[6] * point3Ds[2]/* + Rc2p[7]*/;
    point3Dd[2] = Rc2p[8] * point3Ds[0] + Rc2p[9] * point3Ds[1] + Rc2p[10] * point3Ds[2]/* + Rc2p[11]*/;

    double k = -Rc2p[11] / point3Dd[2];

    point3Dd[0] *= k;
    point3Dd[1] *= k;

    point3Dd[0] += Rc2p[3];			//vertical
    point3Dd[1] += Rc2p[7];			//horizontal

    return 0;
}

int initialGuessExtParams(cameraMetadata *pCamera, int pointNum, chessboardCorner *pChessboardCorners)
{// giving an random initial value of the centers, estimate the extrinsic parameters
 // and optimize all of them(2 for center, 3 for rotation vector, 3 for translation vector)
 // return the optimized center if they meet the threshold(close enough to the initial value)
    double ss[POL_LENGTH], int_par[5], RRdef[9];
    double R[EXT_PARAM_R_MTX_NUM], T[EXT_PARAM_T_VEC_NUM];

    //pCamera->setImageCenters(center);
    pCamera->getPol(ss);
    pCamera->getInterParams(int_par);

    // compute initial extParameters from planar to camera
    calcExtParam(RRdef, ss, int_par, pChessboardCorners, pointNum);

    // RRdef is the extrinsic parameter of chessboard against camera
    pCamera->setExtParamsFromRT(RRdef);
    
    return 0;
}

double SSREChessboardCorners(cameraMetadata *pCamera, int cornerNum, chessboardCorner *pCorners, double *x)
{
    double dResult = 0.0;
    double img[2], cam[3], chess[2], dis[2];

    for (int k = 0; k < cornerNum; k++)
    {
#if 0   // transform each corner's coordinate from image to chessboard
        img[0] = pCorners[k].dU;
        img[1] = pCorners[k].dV;
        pCamera->img2cam(cam, img);
        pCamera->cam2chess(chess, cam);

        dis[0] = (chess[0] - pCorners[k].iX) * (chess[0] - pCorners[k].iX);
        dis[1] = (chess[1] - pCorners[k].iY) * (chess[1] - pCorners[k].iY);

#else   // transform each corner's coordinate from chessboard to image
        chess[0] = pCorners[k].iX;
        chess[1] = pCorners[k].iY;
        pCamera->chess2cam(chess, cam);
        pCamera->cam2img(img, cam);

        dis[0] = (img[0] - pCorners[k].dU) * (img[0] - pCorners[k].dU);
        dis[1] = (img[1] - pCorners[k].dV) * (img[1] - pCorners[k].dV);

#endif
        x[k] = sqrt(dis[0] + dis[1]);
        dResult += x[k];
    }

    return dResult;
}

void errorChessboardPoint(double *p, double *x, int m, int n, void *data)
{
    double imgCenter[IMAGE_CENTER_NUM], rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM], transVec[EXT_PARAM_T_VEC_NUM];
    double img[2], cam[3], chess[2], dis[2];
    optiDataCentExt *pOptiData = (optiDataCentExt*)data;

    // set camera parameters
    memcpy(imgCenter, p, sizeof(double)*IMAGE_CENTER_NUM);
    imgCenter[0] *= IMAGE_CENTER_MAGNIFY;
    imgCenter[1] *= IMAGE_CENTER_MAGNIFY;
    pOptiData->pCamera->setImageCenters(imgCenter); // image center

    memcpy(rotVec, p + IMAGE_CENTER_NUM, sizeof(double)*ROT_VEC_DIM);
    rotationMtxVecTrans(rotMtx, rotVec, rtVec2Mtx);
    pOptiData->pCamera->setRotMtx(rotMtx, extCam2World);  // camera 2 world rotation matrix

    memcpy(transVec, p + IMAGE_CENTER_NUM + ROT_VEC_DIM, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    transVec[0] *= TRAN_VEC_MAGNIFY;
    transVec[1] *= TRAN_VEC_MAGNIFY;
    transVec[2] *= TRAN_VEC_MAGNIFY;
    pOptiData->pCamera->setTransVec(transVec, extCam2World);  // camera 2 world translation vector

    pOptiData->errSum = SSREChessboardCorners(pOptiData->pCamera, n, pOptiData->pChessboardCorner, x);
    return;
}

void errorChessboardPointWithExtGuess(double *p, double *x, int m, int n, void *data)
{
    double imgCenter[IMAGE_CENTER_NUM];
    double img[2], cam[3], chess[2], dis[2];
    optiDataCentExt *pOptiData = (optiDataCentExt*)data;

    // set camera parameters
    memcpy(imgCenter, p, sizeof(double)*IMAGE_CENTER_NUM);
    imgCenter[0] *= IMAGE_CENTER_MAGNIFY;
    imgCenter[1] *= IMAGE_CENTER_MAGNIFY;
    pOptiData->pCamera->setImageCenters(imgCenter); // image center

    initialGuessExtParams(pOptiData->pCamera, n, pOptiData->pChessboardCorner);
    pOptiData->errSum = SSREChessboardCorners(pOptiData->pCamera, n, pOptiData->pChessboardCorner, x);
    return;
}
void errorChessboardPointWithExtGuessAFF(double *p, double *x, int m, int n, void *data)
{
    double imgIntPar[IMAGE_CENTER_NUM + AFFINE_PARAM_NUM];
    double img[2], cam[3], chess[2], dis[2];
    optiDataCentExt *pOptiData = (optiDataCentExt*)data;

    // set camera parameters
    memcpy(imgIntPar, p, sizeof(double)*(IMAGE_CENTER_NUM + AFFINE_PARAM_NUM));
    imgIntPar[3] *= IMAGE_CENTER_MAGNIFY;
    imgIntPar[4] *= IMAGE_CENTER_MAGNIFY;
    pOptiData->pCamera->setInterParams(imgIntPar); // image center

    initialGuessExtParams(pOptiData->pCamera, n, pOptiData->pChessboardCorner);
    pOptiData->errSum = SSREChessboardCorners(pOptiData->pCamera, n, pOptiData->pChessboardCorner, x);
    return;
}


double optimizeImageCenterAndExt(cameraMetadata *pCamera, int pointNum, chessboardCorner *pChessboardCorners)
{// camera extrinsic parameters are set before this function is called
    int ret, itrMax = 10000;
    double dRet, imgCenterBefore[IMAGE_CENTER_NUM], imgCenterAfter[IMAGE_CENTER_NUM], rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM], transVec[EXT_PARAM_T_VEC_NUM];
    // optimization initialization --------------------------------------------------
    int m, n, iResult;
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataCentExt stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.pChessboardCorner = pChessboardCorners;
    stOptiData.pCamera = pCamera;

    // set initial values -----
    m = IMAGE_CENTER_NUM + ROT_VEC_DIM + EXT_PARAM_T_VEC_NUM;
    n = pointNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }

    // set params
    pCamera->getImageCenters(imgCenterBefore);  // ocamModel centers for pParams[0] & pParams[1]
    pParams[0] = imgCenterBefore[0] / IMAGE_CENTER_MAGNIFY;
    pParams[1] = imgCenterBefore[1] / IMAGE_CENTER_MAGNIFY;

    pCamera->getCam2WorldRotMtx(rotMtx);  // camera 2 world rotation matrix
    rotationMtxVecTrans(rotMtx, rotVec, rtMtx2Vec);
    memcpy(pParams + IMAGE_CENTER_NUM, rotVec, sizeof(double) * ROT_VEC_DIM);

    pCamera->getCam2WorldTransVec(transVec);  // camera 2 world translation vector
    transVec[0] /= TRAN_VEC_MAGNIFY;
    transVec[1] /= TRAN_VEC_MAGNIFY;
    transVec[2] /= TRAN_VEC_MAGNIFY;
    memcpy(pParams + IMAGE_CENTER_NUM + ROT_VEC_DIM, transVec, sizeof(double) * EXT_PARAM_T_VEC_NUM);

    // dlevmar_dif
    ret = dlevmar_dif(errorChessboardPoint, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void*)&stOptiData);  // no Jacobian

    // check center result
    imgCenterAfter[0] = pParams[0] * IMAGE_CENTER_MAGNIFY;
    imgCenterAfter[1] = pParams[1] * IMAGE_CENTER_MAGNIFY;
    pCamera->setImageCenters(imgCenterAfter);

    memcpy(rotVec, pParams + IMAGE_CENTER_NUM, sizeof(double)*ROT_VEC_DIM);
    rotationMtxVecTrans(rotMtx, rotVec, rtVec2Mtx);
    pCamera->setRotMtx(rotMtx, extCam2World);  // camera 2 world rotation matrix

    memcpy(transVec, pParams + IMAGE_CENTER_NUM + ROT_VEC_DIM, sizeof(double) * EXT_PARAM_T_VEC_NUM);
    transVec[0] *= TRAN_VEC_MAGNIFY;
    transVec[1] *= TRAN_VEC_MAGNIFY;
    transVec[2] *= TRAN_VEC_MAGNIFY;
    pCamera->setTransVec(transVec, extCam2World);  // camera 2 world translation vector

    if (ret)
    {
        dRet = sqrt((imgCenterAfter[0]- imgCenterBefore[0])*(imgCenterAfter[0] - imgCenterBefore[0]) + (imgCenterAfter[1] - imgCenterBefore[1])*(imgCenterAfter[1] - imgCenterBefore[1]));
    } 
    else
    {
        dRet = -1;
    }
    return dRet;
}

bool comparep(DMatch &a, DMatch &b)
{
    return (a.distance < b.distance);
}

int refineMatchesWithHomography(const vector<KeyPoint>& queryKeypoints, const vector<KeyPoint>& trainKeypoints, float reprojectionThreshold, vector<DMatch>& matches, Mat& homography)
{
    vector<Point2f> srcPoints(matches.size());
    vector<Point2f> dstPoints(matches.size());

    for (size_t i = 0; i < matches.size(); i++)
    {
        srcPoints[i] = trainKeypoints[matches[i].trainIdx].pt;
        dstPoints[i] = queryKeypoints[matches[i].queryIdx].pt;
    }

    vector<unsigned char> inliersMask(srcPoints.size());    // mask of the inliers/outliers

    homography = findHomography(srcPoints, dstPoints, CV_FM_RANSAC, reprojectionThreshold, inliersMask);

    vector<DMatch> inliers; // to collect the inliers and output them as the matches 

    for (size_t i = 0; i < inliersMask.size(); i++)
    {
        if (inliersMask[i])
        {
            inliers.push_back(matches[i]);
        }
    }

    matches.swap(inliers);  // output inliers

    vector<Point2f>().swap(srcPoints);
    vector<Point2f>().swap(dstPoints);
    vector<unsigned char>().swap(inliersMask);
    vector<DMatch>().swap(inliers); // original matches are cleared

    return 0;
}

int getMatchedPoints(imageFrame imgA, imageFrame imgB, matchPoint *pMatchPoints, int &matchPointsMinNum, bool useRansac, int neighborThreshold)
{// giving a pair of YUV image data, detect SIFT feature points, and get the matched points.
 // at most matchPointsMinNum pairs will be obtained.

 // NOTICE1: all points coordinates are restricted in the image frame range, no matter how this image is obtained(cut or projected), and related coordinates
 // operation should be done after this function. 

 // NOTICE2: using neighborThreshold to filter out the points too close to existing points, to avoid points locate in small region;
 // using RANSAC to filter out bad matches, which will effect the optimization of the extrinsic parameters of the cameras.
 // and horizontal area separation is optional only for fisheye image detection, this is also to spread out the points into larger region. 

    int iResult = 0;
    int maxNum;

    Mat img_a, img_b;
    img_a.create(Size(imgA.imageW, imgA.imageH), CV_8UC3);
    img_b.create(Size(imgB.imageW, imgB.imageH), CV_8UC3);

    // covert to rgb, and transform to mat
    convertImageFrametoCvMat(imgA, img_a);
    convertImageFrametoCvMat(imgB, img_b);

    // for feature detection
    vector<KeyPoint> kp_a, kp_b;
    vector<DMatch> matches;
    Mat descriptor_a, descriptor_b;

    //features detection;
    //SiftFeatureDetector detector;                 // opencv2410
    //detector.detect(img_a, kp_a);
    //detector.detect(img_b, kp_b);
    //generate feature descriptor;
    //SiftDescriptorExtractor extractor;
    //extractor.compute(img_a, kp_a, descriptor_a);
    //extractor.compute(img_b, kp_b, descriptor_b);

    Ptr<Feature2D> detector = xfeatures2d::SIFT::create();  // opencv 310
    detector->detect(img_a, kp_a);
    detector->detect(img_b, kp_b);
    detector->compute(img_a, kp_a, descriptor_a);
    detector->compute(img_b, kp_b, descriptor_b);


    //feature points matching;
    BFMatcher matcher(NORM_L2, true);
    matcher.match(descriptor_a, descriptor_b, matches);

    Mat matHomo;
    float ransacThreshold = 10.0;   // this threshold determine if the point is inlier or outliers, usually set between 1 to 10

    if (useRansac)
    {// using RANSAC to filter out some bad matches, this may not appropriate to fisheye panorama, but jump panorama is OK
        if(matches.size() < 9)
        return -1;
        refineMatchesWithHomography(kp_a, kp_b, ransacThreshold, matches, matHomo);
        // and the effect should be checked after RANSAC
    }

    //sort the matched points by euclidean distance;
    vector<DMatch>::iterator iterator_begin = matches.begin();
    vector<DMatch>::iterator iterator_end = matches.end();
    sort(iterator_begin, iterator_end, comparep);

    // filter out points using neighboring threshold
    vector<DMatch> matches_fnl; // this is used to collect the final result points
    int idx = 0;
    int effectiveNum = 0;

    maxNum = matches.size();
    if (0 == maxNum)
        iResult = -1;  // no matched points found.
    else
    {
        if (matchPointsMinNum > maxNum)
            matchPointsMinNum = maxNum; // collect maxNum points at most

        matchPoint Point_candidate;
        matchPoint Point;
        bool dist_flag;
        double dist_x, dist_y, dist_ab;

        while (effectiveNum < matchPointsMinNum && idx < maxNum)
        {
            if (effectiveNum == 0)
            {// put the first point into matches_fnl, and remove it from matches.
                matches_fnl.push_back(matches[0]);
                matches.erase(matches.begin());
                effectiveNum++;
            }
            else
            {
                Point_candidate.coordsInA[1] = kp_a[matches[0].queryIdx].pt.x;  // currently the head of matches is the next point to verify
                Point_candidate.coordsInA[0] = kp_a[matches[0].queryIdx].pt.y;

                dist_flag = true;
                for (int j = 0; j < matches_fnl.size(); j++)
                {// check the candidate point's distance to each point in the final result list 
                    Point.coordsInA[1] = kp_a[matches_fnl[j].queryIdx].pt.x;
                    Point.coordsInA[0] = kp_a[matches_fnl[j].queryIdx].pt.y;

                    dist_x = (Point_candidate.coordsInA[1] - Point.coordsInA[1])*(Point_candidate.coordsInA[1] - Point.coordsInA[1]);
                    dist_y = (Point_candidate.coordsInA[0] - Point.coordsInA[0])*(Point_candidate.coordsInA[0] - Point.coordsInA[0]);
                    dist_ab = sqrt(dist_x + dist_y);

                    if (dist_ab < neighborThreshold)
                    {
                        dist_flag = false;
                        break;
                    }
                }

                if (dist_flag)
                {
                    matches_fnl.push_back(matches[0]);
                    matches.erase(matches.begin());
                    effectiveNum++;
                }
                else
                {
                    matches.erase(matches.begin());
                }
            }

            idx++;
        }

        // output points to pMatchPoints
        matchPointsMinNum = effectiveNum;
        for (int i = 0; i < matches_fnl.size(); i++)
        {
            pMatchPoints[i].coordsInA[1] = kp_a[matches_fnl[i].queryIdx].pt.x;
            pMatchPoints[i].coordsInA[0] = kp_a[matches_fnl[i].queryIdx].pt.y;
            pMatchPoints[i].coordsInB[1] = kp_b[matches_fnl[i].trainIdx].pt.x;
            pMatchPoints[i].coordsInB[0] = kp_b[matches_fnl[i].trainIdx].pt.y;
        }

    }
    return 0;
}


// weight 2 camera models(calibrated and designed) to a middle valued one
int weightCameraModel(ocamModel *pOcamCalib, ocamModel *pOcamDesign, ocamModel *pOcamWeighted, double calibWeight)
{
    float designWeight = 1 - calibWeight;
    // first check the parameters num
    if ((pOcamCalib->length_pol != pOcamDesign->length_pol) || (pOcamCalib->length_invpol != pOcamDesign->length_invpol))
    {
        return -1;
    }

    // basic dimensions ------------------------------------------------
    memcpy(pOcamWeighted, pOcamDesign, sizeof(ocamModel));

    // polynomials -------------------------------------------------------
    for (int k = 0; k < pOcamWeighted->length_pol; k++)
    {
        pOcamWeighted->pol[k] = pOcamCalib->pol[k] * calibWeight + pOcamDesign->pol[k] * designWeight;
    }
    for (int k = 0; k < pOcamWeighted->length_invpol; k++)
    {
        pOcamWeighted->invpol[k] = pOcamCalib->invpol[k] * calibWeight + pOcamDesign->invpol[k] * designWeight;
    }

    return 0;
}


// ------------------------------------------------------------------------ 
void errorMatchPoint(double *p, double *x, int m, int n, void *data)
{// calculate the RMS error of each pair of matched points
    double *rotVec = p;
    double tranVec[EXT_PARAM_T_VEC_NUM];
    double panoCoords[2], dis[2], rotMtx[EXT_PARAM_R_MTX_NUM];
    optiDataExtrnAndIntrn *pOptiData = (optiDataExtrnAndIntrn*)data;

    static int num = 0;
    double x_sum = 0;
    rotationMtxVecTrans(rotMtx, rotVec, rtVec2Mtx);
    pOptiData->pCamera->setRotMtx(rotMtx, extCam2World);

    tranVec[0] = *(p + ROT_VEC_DIM + 0) * TRAN_VEC_MAGNIFY;
    tranVec[1] = *(p + ROT_VEC_DIM + 1) * TRAN_VEC_MAGNIFY;
    tranVec[2] = *(p + ROT_VEC_DIM + 2) * TRAN_VEC_MAGNIFY;
    pOptiData->pCamera->setTransVec(tranVec, extCam2World);

    for (int k = 0; k < n; k++)
    {// transform each point in image B to panorama coordinates
        pOptiData->pImgWarper->coordTransFisheyeToPano(panoCoords, pOptiData->pMatchedPoints[k].coordsInB, pOptiData->sphereRadius, pOptiData->pCamera);
        dis[0] = (panoCoords[0] - pOptiData->pMatchedPoints[k].coordsInA[0]) * (panoCoords[0] - pOptiData->pMatchedPoints[k].coordsInA[0]);
        dis[1] = (panoCoords[1] - pOptiData->pMatchedPoints[k].coordsInA[1]) * (panoCoords[1] - pOptiData->pMatchedPoints[k].coordsInA[1]);
        x[k] = sqrt(dis[0] + dis[1]);
        x_sum += x[k];
    }
    printf("%d:\tx_average: %lf\n", num++, x_sum/n);
    return;
}
void errorMatchPointFixT(double *p, double *x, int m, int n, void *data)
{// calculate the RMS error of each pair of matched points
    double *rotVec = p;
    double panoCoords[2], dis[2], rotMtx[EXT_PARAM_R_MTX_NUM];
    optiDataExtrnAndIntrn *pOptiData = (optiDataExtrnAndIntrn*)data;

    static int num = 0;
    double x_sum = 0;
    rotationMtxVecTrans(rotMtx, rotVec, rtVec2Mtx);
    pOptiData->pCamera->setRotMtx(rotMtx, extCam2World);

    for (int k = 0; k < n; k++)
    {// transform each point in image B to panorama coordinates
        pOptiData->pImgWarper->coordTransFisheyeToPano(panoCoords, pOptiData->pMatchedPoints[k].coordsInB, pOptiData->sphereRadius, pOptiData->pCamera);
        dis[0] = (panoCoords[0] - pOptiData->pMatchedPoints[k].coordsInA[0]) * (panoCoords[0] - pOptiData->pMatchedPoints[k].coordsInA[0]);
        dis[1] = (panoCoords[1] - pOptiData->pMatchedPoints[k].coordsInA[1]) * (panoCoords[1] - pOptiData->pMatchedPoints[k].coordsInA[1]);
        x[k] = sqrt(dis[0] + dis[1]);
        x_sum += x[k];
    }
    printf("%d:\tx_average: %lf\n", num++, x_sum/n);
    return;
}
void errorMatchPointExtrnAndIntrn(double *p, double *x, int m, int n, void *data)
{// calculate the RMS error of each pair of matched points
    double *rotVec = p;
    double panoCoordsA[2], panoCoordsB[2], dis[2], rotMtx[EXT_PARAM_R_MTX_NUM], centers[2];
    optiDataExtrnAndIntrn *pOptiData = (optiDataExtrnAndIntrn*)data;
    ocamModel tmpOcamModel;

    static int num = 0;
    double x_sum = 0;
    rotationMtxVecTrans(rotMtx, rotVec, rtVec2Mtx);
    pOptiData->pCamera[1].setRotMtx(rotMtx, extCam2World);

    // weight 2 cameras intrinsic polynomial curves
    pOptiData->polParamWeights[0] = *(p + ROT_VEC_DIM);
    weightCameraModel(&pOptiData->stOcamModelCalib, &pOptiData->stOcamModelDesign[0], &tmpOcamModel, pOptiData->polParamWeights[0]);
    // set camera metadata
    //pOptiData->pCamera[0].getImageCenters(centers);
    pOptiData->pCamera[0].setOcamModel(&tmpOcamModel);
    //pOptiData->pCamera[0].setImageCenters(centers);

    pOptiData->polParamWeights[1] = *(p + ROT_VEC_DIM + 1);
    weightCameraModel(&pOptiData->stOcamModelCalib, &pOptiData->stOcamModelDesign[1], &tmpOcamModel, pOptiData->polParamWeights[1]);
    //pOptiData->pCamera[1].getImageCenters(centers);
    pOptiData->pCamera[1].setOcamModel(&tmpOcamModel);
    //pOptiData->pCamera[1].setImageCenters(centers);

    for (int k = 0; k < n; k++)
    {// transform each point in image B to panorama coordinates
        pOptiData->pImgWarper[0].coordTransFisheyeToPano(panoCoordsA, pOptiData->pMatchedPoints[k].coordsInA, pOptiData->sphereRadius, &pOptiData->pCamera[0]);
        pOptiData->pImgWarper[1].coordTransFisheyeToPano(panoCoordsB, pOptiData->pMatchedPoints[k].coordsInB, pOptiData->sphereRadius, &pOptiData->pCamera[1]);
        dis[0] = (panoCoordsA[0] - panoCoordsB[0]) * (panoCoordsA[0] - panoCoordsB[0]);
        dis[1] = (panoCoordsA[1] - panoCoordsB[1]) * (panoCoordsA[1] - panoCoordsB[1]);
        x[k] = sqrt(dis[0] + dis[1]);
        x_sum += x[k];
    }
    printf("%d:\tx_average: %lf\tweights: %lf\t%lf\n", num++, x_sum / n, pOptiData->polParamWeights[0], pOptiData->polParamWeights[1]);
    return;
}
void errorMatchPointRadius(double *p, double *x, int m, int n, void *data)
{// calculate the RMS error of each pair of matched points
    int radius;
    double *rotVec = p;
    double panoCoordsA[2], panoCoordsB[2], dis[2], rotMtx[EXT_PARAM_R_MTX_NUM], centers[2];
    optiDataRadius *pOptiData = (optiDataRadius*)data;

    static int num = 0;
    double x_sum = 0;
    radius = p[0] * SPHERE_RADIUS_MAGNIFY;
    for (int k = 0; k < n; k++)
    {// transform each point in image B to panorama coordinates
        pOptiData->pImgWarper[0].coordTransFisheyeToPano(panoCoordsA, pOptiData->pMatchedPoints[k].coordsInA, radius, &pOptiData->pCamera[0]);
        pOptiData->pImgWarper[1].coordTransFisheyeToPano(panoCoordsB, pOptiData->pMatchedPoints[k].coordsInB, radius, &pOptiData->pCamera[1]);
        dis[0] = (panoCoordsA[0] - panoCoordsB[0]) * (panoCoordsA[0] - panoCoordsB[0]);
        dis[1] = (panoCoordsA[1] - panoCoordsB[1]) * (panoCoordsA[1] - panoCoordsB[1]);
        x[k] = sqrt(dis[0] + dis[1]);
        x_sum += x[k];
    }
    printf("%d:\tx_average: %lf\t radius: %d\n", num++, x_sum / n, radius);
    return;
}

// =============================================================================
    intrinsicParamOptimizer::intrinsicParamOptimizer()
    {
    }

    intrinsicParamOptimizer::~intrinsicParamOptimizer()
    {
    }

int intrinsicParamOptimizer::setCameraMetadata()
{
    return 0;
}

int intrinsicParamOptimizer::findChessboardCorners(imageFrame fisheyeImage, int checkerNumH, int checkerNumV, int checkerSize)
{
    int iResult = 0;
    iResult = findCornersFrame(fisheyeImage, checkerNumH, checkerNumV, checkerSize, cornersNum, &pChessboardCorners);

    return iResult;
}

int intrinsicParamOptimizer::optimizeOptcCen(cameraMetadata *pCamera)
{
    // giving the initial value, e.x. 960 960
    // optimization initialization --------------------------------------------------
    int ret, itrMax = 10000;
    int m, n, iResult;
    double imgCenterBefore[IMAGE_CENTER_NUM];
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataCentExt stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    //opts[0] = LM_INIT_MU; opts[1] = 1E-18; opts[2] = 1E-18; opts[3] = 1E-23;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.pChessboardCorner = pChessboardCorners;
    stOptiData.pCamera = pCamera;

    // set initial values -----
    m = IMAGE_CENTER_NUM;
    n = cornersNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }
#if 0
    drawProjectPoints("corners_proj_init.jpg", pCamera, cornersNum, pChessboardCorners);
#endif
    // set params
    pCamera->getImageCenters(imgCenterBefore);  // ocamModel centers for pParams[0] & pParams[1]
    pParams[0] = imgCenterBefore[0] / IMAGE_CENTER_MAGNIFY;
    pParams[1] = imgCenterBefore[1] / IMAGE_CENTER_MAGNIFY;

    ret = dlevmar_dif(errorChessboardPointWithExtGuess, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void*)&stOptiData);  // no Jacobian

#if 0
    drawProjectPoints("corners_proj_optd.jpg", pCamera, cornersNum, pChessboardCorners);
#endif
    delete[] pParams;
    delete[] pTrueValues;
    return 0;
}

int intrinsicParamOptimizer::optimizeOptcCenAndAffine(cameraMetadata *pCamera)
{
    // giving the initial value, e.x. 960 960
    // optimization initialization --------------------------------------------------
    int ret, itrMax = 10000;
    int m, n, iResult;
    double imgCenAffineBefore[IMAGE_CENTER_NUM + AFFINE_PARAM_NUM];
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataCentExt stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    //opts[0] = LM_INIT_MU; opts[1] = 1E-18; opts[2] = 1E-18; opts[3] = 1E-23;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.pChessboardCorner = pChessboardCorners;
    stOptiData.pCamera = pCamera;

    // set initial values -----
    m = IMAGE_CENTER_NUM + AFFINE_PARAM_NUM;
    n = cornersNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }
#if 0
    drawProjectPoints("corners_proj_init.jpg", pCamera, cornersNum, pChessboardCorners);
#endif
    // set params
    pCamera->getInterParams(imgCenAffineBefore);  // ocamModel centers for pParams[0] & pParams[1]
    pParams[0] = imgCenAffineBefore[0];
    pParams[1] = imgCenAffineBefore[1];
    pParams[2] = imgCenAffineBefore[2];
    pParams[3] = imgCenAffineBefore[3] / IMAGE_CENTER_MAGNIFY;
    pParams[4] = imgCenAffineBefore[4] / IMAGE_CENTER_MAGNIFY;

    ret = dlevmar_dif(errorChessboardPointWithExtGuessAFF, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void*)&stOptiData);  // no Jacobian

#if 0
    drawProjectPoints("corners_proj_optd.jpg", pCamera, cornersNum, pChessboardCorners);
#endif

    delete[] pParams;
    delete[] pTrueValues;
    return 0;
}

int intrinsicParamOptimizer::drawCorners(char *filePath, imageFrame imageframe, double imageCenters[2])
{   
    Mat image;
    int ptX, ptY;
    image.create(Size(imageframe.imageW, imageframe.imageH), CV_8UC3);

    // covert to rgb, and transform to mat
    convertImageFrametoCvMat(imageframe, image);

    // corners
    drawCornersRaw(image, cornersNum, pChessboardCorners);

    ptX = imageCenters[1];
    ptY = imageCenters[0];
    line(image, cvPoint(ptX - 3, ptY), cvPoint(ptX + 3, ptY), CV_RGB(0, 255, 0), 3);
    line(image, cvPoint(ptX, ptY - 3), cvPoint(ptX, ptY + 3), CV_RGB(0, 255, 0), 3);

    imwrite(filePath, image);

    return 0;
}

// =============================================================================
    extrinsicParamOptimizer::extrinsicParamOptimizer()
    {
    }

    extrinsicParamOptimizer::~extrinsicParamOptimizer()
    {
    }

int extrinsicParamOptimizer::setCameraMetadata()
{
    return 0;
}

int extrinsicParamOptimizer::findMatchPoints(imageFrame imgA, imageFrame imgB, matchPoint *pMatchPoints, int &matchPointsMinNum, bool useRansac, int neighborThreshold)
{
    int iResult = 0;
    iResult = getMatchedPoints(imgA, imgB, pMatchPoints, matchPointsMinNum, useRansac, neighborThreshold);
    return iResult;
}

int extrinsicParamOptimizer::drawPoints(char *folderPath, imageFrame imgA, imageFrame imgB, matchPoint *matchedPoints, int pointNum)
{
    char fileName[512];
    int  ptA_x, ptA_y, ptB_x, ptB_y;

    IplImage *cvImgA = cvCreateImage(cvSize(imgA.imageW, imgA.imageH), 8, 1);
    IplImage *cvImgB = cvCreateImage(cvSize(imgB.imageW, imgB.imageH), 8, 1);
    if ((cvImgA == NULL) || (cvImgB == NULL))
    {
        return -1;
    }

    for (int j = 0; j < imgA.imageH; ++j)
        for (int i = 0; i < imgA.imageW; ++i)
        {
            cvImgA->imageData[j*cvImgA->widthStep + i] = imgA.plane[0][j*imgA.imageW + i];
            cvImgB->imageData[j*cvImgB->widthStep + i] = imgB.plane[0][j*imgA.imageW + i];
        }

    for (int k = 0; k < pointNum; k++)
    {
        ptA_x = matchedPoints[k].coordsInA[1];
        ptA_y = matchedPoints[k].coordsInA[0];
        ptB_x = matchedPoints[k].coordsInB[1];
        ptB_y = matchedPoints[k].coordsInB[0];

        cvLine(cvImgA, cvPoint(ptA_x - 3, ptA_y), cvPoint(ptA_x + 3, ptA_y), CV_RGB(255, 0, 0), 1);
        cvLine(cvImgA, cvPoint(ptA_x, ptA_y - 3), cvPoint(ptA_x, ptA_y + 3), CV_RGB(255, 0, 0), 1);

        cvLine(cvImgB, cvPoint(ptB_x - 3, ptB_y), cvPoint(ptB_x + 3, ptB_y), CV_RGB(255, 0, 0), 1);
        cvLine(cvImgB, cvPoint(ptB_x, ptB_y - 3), cvPoint(ptB_x, ptB_y + 3), CV_RGB(255, 0, 0), 1);

    }


    sprintf_s(fileName, "%s\\matchPointsImgA.jpg", folderPath);
    cvSaveImage(fileName, cvImgA);
    sprintf_s(fileName, "%s\\matchPointsImgB.jpg", folderPath);
    cvSaveImage(fileName, cvImgB);

    cvReleaseImage(&cvImgA);
    cvReleaseImage(&cvImgB);
    return 0;
}


int extrinsicParamOptimizer::drawPairs(char *fileName, imageFrame imgA, imageFrame imgB, matchPoint *matchedPoints, int pointNum, bool isVertical)
{
    int  ptA_x, ptA_y, ptB_x, ptB_y;

    IplImage *cvImgA = cvCreateImage(cvSize(imgA.imageW, imgA.imageH), 8, 1);
    IplImage *cvImgB = cvCreateImage(cvSize(imgB.imageW, imgB.imageH), 8, 1);
    if ((cvImgA == NULL) || (cvImgB == NULL))
    {
        return -1;
    }

    for (int j = 0; j < imgA.imageH; ++j)
        for (int i = 0; i < imgA.imageW; ++i)
        {
            cvImgA->imageData[j*cvImgA->widthStep + i] = imgA.plane[0][j*imgA.imageW + i];
            cvImgB->imageData[j*cvImgB->widthStep + i] = imgB.plane[0][j*imgA.imageW + i];
        }

    IplImage *cvImg = NULL;
    CvRect roi;

    if (isVertical) // vertically stack 2 images
    {
        cvImg = cvCreateImage(cvSize(cvImgA->width, cvImgA->height + cvImgB->height), 8, 1);
        roi = cvRect(0, 0, cvImgA->width, cvImgA->height);

        cvSetImageROI(cvImg, roi);
        cvCopy(cvImgA, cvImg);
        cvResetImageROI(cvImg);

        roi = cvRect(0, cvImgA->height, cvImgB->width, cvImgB->height);
        cvSetImageROI(cvImg, roi);
        cvCopy(cvImgB, cvImg);
        cvResetImageROI(cvImg);

        for (int k = 0; k < pointNum; k++)
        {
            ptA_x = matchedPoints[k].coordsInA[1];
            ptA_y = matchedPoints[k].coordsInA[0];
            ptB_x = matchedPoints[k].coordsInB[1];
            ptB_y = matchedPoints[k].coordsInB[0] + cvImgA->height;

            cvLine(cvImg, cvPoint(ptA_x, ptA_y), cvPoint(ptB_x, ptB_y), CV_RGB(255, 0, 0), 2);
        }
    }
    else // horizontally stack 2 images
    {
        cvImg = cvCreateImage(cvSize(cvImgA->width + cvImgB->width, cvImgA->height), 8, 1);
        roi = cvRect(0, 0, cvImgA->width, cvImgA->height);

        cvSetImageROI(cvImg, roi);
        cvCopy(cvImgA, cvImg);
        cvResetImageROI(cvImg);

        roi = cvRect(cvImgA->width, 0, cvImgB->width, cvImgB->height);
        cvSetImageROI(cvImg, roi);
        cvCopy(cvImgB, cvImg);
        cvResetImageROI(cvImg);

        for (int k = 0; k < pointNum; k++)
        {
            ptA_x = matchedPoints[k].coordsInA[1];
            ptA_y = matchedPoints[k].coordsInA[0];
            ptB_x = matchedPoints[k].coordsInB[1] + cvImgA->width;
            ptB_y = matchedPoints[k].coordsInB[0];

            cvLine(cvImg, cvPoint(ptA_x, ptA_y), cvPoint(ptB_x, ptB_y), CV_RGB(255, 0, 0), 2);
        }
    }



    cvSaveImage(fileName, cvImg);

    cvReleaseImage(&cvImgA);
    cvReleaseImage(&cvImgB);
    cvReleaseImage(&cvImg);
    return 0;
}

int extrinsicParamOptimizer::setMatchPoints(matchPoint *matchPointL, int numL, matchPoint *matchPointM, int numM, matchPoint *matchPointR, int numR, double offsetCoords)
{
    mPointNum = numL + numR + numM;
    pMatchPoints = new matchPoint[mPointNum];
    matchPoint *localMatchPtPtr = pMatchPoints;

    // important: calculating from infinite point and viewing sphere radius

    for (int k = 0; k < numL; k++)
    {
        localMatchPtPtr->coordsInA[0] = matchPointL[k].coordsInA[0] - offsetCoords;
        localMatchPtPtr->coordsInA[1] = matchPointL[k].coordsInA[1];
        localMatchPtPtr->coordsInB[0] = matchPointL[k].coordsInB[0] + offsetCoords;
        localMatchPtPtr->coordsInB[1] = matchPointL[k].coordsInB[1];
        localMatchPtPtr++;
    }

    for (int k = 0; k < numM; k++)
    {
        localMatchPtPtr->coordsInA[0] = matchPointM[k].coordsInA[0] - offsetCoords;
        localMatchPtPtr->coordsInA[1] = matchPointM[k].coordsInA[1];
        localMatchPtPtr->coordsInB[0] = matchPointM[k].coordsInB[0] + offsetCoords;
        localMatchPtPtr->coordsInB[1] = matchPointM[k].coordsInB[1];
        localMatchPtPtr++;
    }

    for (int k = 0; k < numR; k++)
    {
        localMatchPtPtr->coordsInA[0] = matchPointR[k].coordsInA[0] - offsetCoords;
        localMatchPtPtr->coordsInA[1] = matchPointR[k].coordsInA[1];
        localMatchPtPtr->coordsInB[0] = matchPointR[k].coordsInB[0] + offsetCoords;
        localMatchPtPtr->coordsInB[1] = matchPointR[k].coordsInB[1];
        localMatchPtPtr++;
    }
    return 0;
}

int extrinsicParamOptimizer::prepareMatchPoints(ImageWarper *pImageWarpers, int sphereRadius, cameraMetadata *cameras)
{
    for (int k = 0; k < mPointNum; k++)
    {
        // firstly, transform coordinates to panorama image
        imageCoordsTransfer(pMatchPoints[k].coordsInA, pMatchPoints[k].coordsInA, pImageWarpers[0].mWarpImgDstRoi, roi2whole);
        imageCoordsTransfer(pMatchPoints[k].coordsInB, pMatchPoints[k].coordsInB, pImageWarpers[1].mWarpImgDstRoi, roi2whole);
        // secondly, transform coordinates to fisheye image
        pImageWarpers[0].coordTransPanoToFisheye(pMatchPoints[k].coordsInA, pMatchPoints[k].coordsInA, sphereRadius, &cameras[0]);
        pImageWarpers[1].coordTransPanoToFisheye(pMatchPoints[k].coordsInB, pMatchPoints[k].coordsInB, sphereRadius, &cameras[1]);
    }
    return 0;
}

int extrinsicParamOptimizer::clean()
{
    mPointNum = 0;
    if (pMatchPoints != NULL)
    {
        delete[] pMatchPoints;
        pMatchPoints = NULL;
    }
    return 0;
}

int extrinsicParamOptimizer::optimizeExtrinsicParams(int sphereRadius, ImageWarper *pImageWarper, cameraMetadata *pCamera)
{
    // local variables
    int itrMax = 10000;
    double rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM], transVec[EXT_PARAM_T_VEC_NUM];
    // optimization initialization --------------------------------------------------
    int m, n, iResult;
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataExtrnAndIntrn stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.sphereRadius = sphereRadius;
    stOptiData.pMatchedPoints = pMatchPoints;
    stOptiData.pImgWarper = pImageWarper;
    stOptiData.pCamera = pCamera;

    // set initial values -----
    m = ROT_VEC_DIM + EXT_PARAM_T_VEC_NUM;
    n = mPointNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }

    // get camera B's rotation vector from its rotation matrix
    pCamera->getCam2WorldRotMtx(rotMtx);        
    rotationMtxVecTrans(rotMtx, rotVec, rtMtx2Vec);
    pCamera->getCam2WorldTransVec(transVec);

    for (int k = 0; k < ROT_VEC_DIM; k++)       // set camera's extrinsic parameter(rotation and translation vector)as the optimization object
    {
        pParams[k] = rotVec[k];
    }
    for (int k = 0; k < EXT_PARAM_T_VEC_NUM; k++)
    {
        pParams[k + ROT_VEC_DIM] = transVec[k] / TRAN_VEC_MAGNIFY;
    }

    iResult = dlevmar_dif(errorMatchPoint, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void *)(&stOptiData));

    // apply the optimized parameters
    rotationMtxVecTrans(rotMtx, pParams, rtVec2Mtx);
    pCamera->setRotMtx(rotMtx, extCam2World);

    memcpy(transVec, pParams + ROT_VEC_DIM, sizeof(double)*EXT_PARAM_T_VEC_NUM);
    transVec[0] *= TRAN_VEC_MAGNIFY;
    transVec[1] *= TRAN_VEC_MAGNIFY;
    transVec[2] *= TRAN_VEC_MAGNIFY;
    pCamera->setTransVec(transVec, extCam2World);

    // optimization clean up --------------------------------------------------
    if (pParams != NULL)
    {
        delete[] pParams;
        pParams = NULL;
    }
    if (pTrueValues != NULL)
    {
        delete[] pTrueValues;
        pTrueValues = NULL;
    }
    return 0;
}

int extrinsicParamOptimizer::optimizeExtrinsicParamsFixT(int sphereRadius, ImageWarper *pImageWarper, cameraMetadata *pCamera)
{
    // local variables
    int itrMax = 10000;
    double rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM];
    // optimization initialization --------------------------------------------------
    int m, n, iResult;
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataExtrnAndIntrn stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.sphereRadius = sphereRadius;
    stOptiData.pMatchedPoints = pMatchPoints;
    stOptiData.pImgWarper = pImageWarper;
    stOptiData.pCamera = pCamera;

    // set initial values -----
    m = ROT_VEC_DIM;
    n = mPointNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }

    // get camera B's rotation vector from its rotation matrix
    pCamera->getCam2WorldRotMtx(rotMtx);
    rotationMtxVecTrans(rotMtx, rotVec, rtMtx2Vec);

    for (int k = 0; k < ROT_VEC_DIM; k++)       // set camera's extrinsic parameter(rotation and translation vector)as the optimization object
    {
        pParams[k] = rotVec[k];
    }


    iResult = dlevmar_dif(errorMatchPointFixT, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void *)(&stOptiData));

    // apply the optimized parameters
    rotationMtxVecTrans(rotMtx, pParams, rtVec2Mtx);
    pCamera->setRotMtx(rotMtx, extCam2World);


    // optimization clean up --------------------------------------------------
    if (pParams != NULL)
    {
        delete[] pParams;
        pParams = NULL;
    }
    if (pTrueValues != NULL)
    {
        delete[] pTrueValues;
        pTrueValues = NULL;
    }
    return 0;
}

int extrinsicParamOptimizer::optimizeExtrnAndIntrnParams(int sphereRadius, ImageWarper *pImageWarpers, cameraMetadata *pCameras, ocamModel *pOcamCalibLinear)
{
    // local variables
    int itrMax = 10000;
    double rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM], transVec[EXT_PARAM_T_VEC_NUM];
    // optimization initialization --------------------------------------------------
    int m, n, iResult;
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataExtrnAndIntrn stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.polParamWeights[0] = 0.5;
    stOptiData.polParamWeights[1] = 0.5;
    stOptiData.sphereRadius = sphereRadius;
    stOptiData.pMatchedPoints = pMatchPoints;
    stOptiData.pImgWarper = pImageWarpers;
    stOptiData.pCamera = pCameras;

    memcpy(&stOptiData.stOcamModelCalib, pOcamCalibLinear, sizeof(ocamModel));  // save this ocam model as the interpolation input
    pCameras[0].getOcamModel(&stOptiData.stOcamModelDesign[0]);  // save this ocam model also as the interpolation input
    pCameras[1].getOcamModel(&stOptiData.stOcamModelDesign[1]);  // save this ocam model also as the interpolation input

    // set initial values -----
    m = ROT_VEC_DIM + OCAM_MODEL_WEIGHTS;  // 3 for rotation vector and 2 for 2 cameras intrinsic weights
    n = mPointNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }

    // get camera B's rotation vector from its rotation matrix
    pCameras[1].getCam2WorldRotMtx(rotMtx);
    rotationMtxVecTrans(rotMtx, rotVec, rtMtx2Vec);

    for (int k = 0; k < ROT_VEC_DIM; k++)       // set camera's extrinsic parameter(rotation and translation vector)as the optimization object
    {
        pParams[k] = rotVec[k];
    }
    for (int k = 0; k < OCAM_MODEL_WEIGHTS; k++)
    {
        pParams[k + ROT_VEC_DIM] = stOptiData.polParamWeights[k];
    }

    iResult = dlevmar_dif(errorMatchPointExtrnAndIntrn, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void *)(&stOptiData));

    // apply the optimized parameters
    rotationMtxVecTrans(rotMtx, pParams, rtVec2Mtx);
    pCameras[1].setRotMtx(rotMtx, extCam2World);

    // optimization clean up --------------------------------------------------
    if (pParams != NULL)
    {
        delete[] pParams;
        pParams = NULL;
    }
    if (pTrueValues != NULL)
    {
        delete[] pTrueValues;
        pTrueValues = NULL;
    }
    return 0;
}

int extrinsicParamOptimizer::optimizeSphereRadius(int sphereRadius, ImageWarper *pImageWarpers, cameraMetadata *pCameras)
{
    // local variables
    int itrMax = 10000;
    double rotMtx[EXT_PARAM_R_MTX_NUM], rotVec[ROT_VEC_DIM], transVec[EXT_PARAM_T_VEC_NUM];
    // optimization initialization --------------------------------------------------
    int m, n, iResult, radiusOpt = 0;
    double *pParams;
    double *pTrueValues;
    //double *pWorkMem;
    //double *Covar;
    double opts[LM_OPTS_SZ], info[LM_INFO_SZ];
    optiDataRadius stOptiData;

    opts[0] = LM_INIT_MU; opts[1] = 1E-15; opts[2] = 1E-15; opts[3] = 1E-20;
    opts[4] = LM_DIFF_DELTA; // relevant only if the Jacobian is approximated using finite differences; specifies forward differencing 

    stOptiData.pMatchedPoints = pMatchPoints;
    stOptiData.pImgWarper = pImageWarpers;
    stOptiData.pCamera = pCameras;

    // set initial values -----
    m = 1;  // only the radius needs optimization
    n = mPointNum;
    pParams = new double[m];
    pTrueValues = new double[n];

    for (int k = 0; k < n; k++)
    {
        pTrueValues[k] = 0;     // set all points' RMS error to 0 as the condition
    }

    pParams[0] = (double)sphereRadius / SPHERE_RADIUS_MAGNIFY;

    iResult = dlevmar_dif(errorMatchPointRadius, pParams, pTrueValues, m, n, itrMax, opts, info, NULL, NULL, (void *)(&stOptiData));

    radiusOpt = pParams[0] * SPHERE_RADIUS_MAGNIFY;

    // optimization clean up --------------------------------------------------
    if (pParams != NULL)
    {
        delete[] pParams;
        pParams = NULL;
    }
    if (pTrueValues != NULL)
    {
        delete[] pTrueValues;
        pTrueValues = NULL;
    }
    return radiusOpt;
}

// =============================================================================
    extrinsicParamRelativeCalculator::extrinsicParamRelativeCalculator()
    {

    }

    extrinsicParamRelativeCalculator::~extrinsicParamRelativeCalculator()
    {

    }

int extrinsicParamRelativeCalculator::setCameraMetadata()
{
    return 0;
}

int extrinsicParamRelativeCalculator::relativeExtParamCalc(cameraMetadata cameras[2])
{// after the intrinsic calibration procedure, the extrinsic parameters of each lens against the chessboard is obtained.
 // then the extrinsic parameters between 2 lenses are calculated
 // and both lenses extrinsic parameters are represented as lens against the central coordinates system of the camera rig.

    // since the 2 lenses against the same chessboard, just a chain multiplication is needed.
    double RWorld2Cam1[EXT_PARAM_R_MTX_NUM], RCam2World2[EXT_PARAM_R_MTX_NUM], RCam21[EXT_PARAM_R_MTX_NUM];
    double TWorld2Cam1[EXT_PARAM_T_VEC_NUM], TCam2World2[EXT_PARAM_T_VEC_NUM], tmpT[EXT_PARAM_T_VEC_NUM], TCam21[EXT_PARAM_T_VEC_NUM];

    cameras[0].getWorld2CamRotMtx(RWorld2Cam1);     // rotation matrix of chessboard to camera 1
    cameras[0].getWorld2CamTransVec(TWorld2Cam1);   // translation vector of chessboard to camera 1
    cameras[1].getCam2WorldRotMtx(RCam2World2);     // rotation matrix of camera 2 to chessboard
    cameras[1].getCam2WorldTransVec(TCam2World2);   // translation vector of camera 2 to chessboard


    matrixDotMul(RWorld2Cam1, RCam2World2, RCam21, 3, 3, 3);
    matrixDotMul(RWorld2Cam1, TCam2World2, tmpT, 3, 3, 1);
    vectorAdd(tmpT, TWorld2Cam1, TCam21, 3);

    cameras[1].setRotMtx(RCam21, extCam2World);     // rotation matrix of camera 2 to camera 1
    cameras[1].setTransVec(TCam21, extCam2World);   // translation vector of camera 2 to camera 1

    // the set first camera's extrinsic parameters against the camera rig 
    double R_mtx_left[EXT_PARAM_R_MTX_NUM], T_vec_left[EXT_PARAM_T_VEC_NUM];

    // set the coordinate system for the left camera
    genContraryRotationMtxZYZ(R_mtx_left, -90.0, 0.0, 0.0); // world against camera rotation
    T_vec_left[0] = 0.0;   // world against camera displacement in camera coordinate system                        
    T_vec_left[1] = 32.0;
    T_vec_left[2] = 0.0;
    cameras[0].setRotMtx(R_mtx_left, extWorld2Cam);
    cameras[0].setTransVec(T_vec_left, extWorld2Cam);


    cameras[0].getCam2WorldRotMtx(RWorld2Cam1);     // rotation matrix of camera 1 to rig
    cameras[0].getCam2WorldTransVec(TWorld2Cam1);   // translation vector of camera 1 to rig
    cameras[1].getCam2WorldRotMtx(RCam2World2);     // rotation matrix of camera 2 to camera 1
    cameras[1].getCam2WorldTransVec(TCam2World2);   // translation vector of camera 2 to camera 1


    matrixDotMul(RWorld2Cam1, RCam2World2, RCam21, 3, 3, 3);
    matrixDotMul(RWorld2Cam1, TCam2World2, tmpT, 3, 3, 1);
    vectorAdd(tmpT, TWorld2Cam1, TCam21, 3);

    cameras[1].setRotMtx(RCam21, extCam2World);     // rotation matrix of camera 2 to rig
    cameras[1].setTransVec(TCam21, extCam2World);   // translation vector of camera 2 to rig

    return 0;
}


}   // namespace calibration
}   // namespace YiPanorama