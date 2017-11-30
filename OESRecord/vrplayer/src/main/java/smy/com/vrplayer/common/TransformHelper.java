package smy.com.vrplayer.common;


import java.nio.FloatBuffer;

import smy.com.vrplayer.objects.base.CombineParams;

/**
 * Created by ChenZheng on 2016/8/8.
 */
public class TransformHelper {

    private static TransformHelper sInstance = null;

    private TransformHelper() {
    }

    public static synchronized TransformHelper getInstance() {
        if (sInstance == null) {
            sInstance = new TransformHelper();
        }

        return sInstance;
    }

    private float outXF = 0, outYF = 0;
    private float outXB = 0, outYB = 0;

    public void getSingleVertices(FloatBuffer vBuffer, FloatBuffer stBufferF, FloatBuffer stBufferB, CombineParams params){
        float[] fv = new float[vBuffer.limit()];
        vBuffer.get(fv);

        float[] f = new float[stBufferF.limit()];
        stBufferF.get(f);

        float[] b = new float[stBufferB.limit()];
        stBufferB.get(b);

        float frontRotate[] = new float[9];
        float frontTranslate[] = new float[3];

        setContraryRotationMtx(frontRotate, params.front_camera_rotation,9);
        setContraryTranslateVecXYZ(frontTranslate, params.front_camera_translation,3);

        float backRotate[] = new float[9];
        float backTranslate[] = new float[3];

        setContraryRotationMtx(backRotate, params.back_camera_rotation,9);
        setContraryTranslateVecXYZ(backTranslate,params.back_camera_translation,3);

        int sphereW = params.out_width; // sphere image width and height
        int sphereH = params.out_height;

        int inX, inY;

        int step = VrConstant.SPHERE_SAMPLE_STEP;
        double angleStep = Math.PI /sphereH;

        int R = 500;
        // example
        // calculate sphere points coordinates in front camera image
        int vtBase = 0;
        int stBaseF = 0;
        int stBaseB = 0;

        for (inY = 0; inY <= sphereH; inY += step) {
            for (inX = 0; inX <= sphereW; inX += step) {
                point_coords_conversion(true, sphereW, sphereH, inX, inY,
                        frontRotate, frontTranslate, params);
                point_coords_conversion(false, sphereW, sphereH, inX, inY,
                        backRotate, backTranslate, params);

                float sinY = (float) Math.sin(angleStep * inY);
                float sinX = (float) Math.sin(angleStep * inX);
                float cosY = (float) Math.cos(angleStep * inY);
                float cosX = (float) Math.cos(angleStep * inX);

                fv[vtBase++] = R * sinY * sinX;
                fv[vtBase++] = R * sinY * cosX;
                fv[vtBase++] = R * cosY;

                float sCoordinateF =  outXF / params.fish_eye_width;
                float tCoordinateF = 0.5f *  outYF / params.fish_eye_height;
                if(tCoordinateF > 0.5f){
                    tCoordinateF = 0.5f;
                }

                float sCoordinateB =  outXB / params.fish_eye_width;
                float tCoordinateB =  0.5f + 0.5f * outYB / params.fish_eye_height;
                if(tCoordinateB > 1.0f){
                    tCoordinateB = 1.0f;
                }

                f[stBaseF++] = sCoordinateF;
                f[stBaseF++] = tCoordinateF;

                b[stBaseB++] = sCoordinateB;
                b[stBaseB++] = tCoordinateB;
            }
        }

        vBuffer.clear();
        vBuffer.put(fv);

        stBufferF.clear();
        stBufferF.put(f);

        stBufferB.clear();
        stBufferB.put(b);
    }

    public void getSingleRectAngleVertices(FloatBuffer vBuffer, FloatBuffer stBufferF,FloatBuffer stBufferB, CombineParams params){

        float[] fv = new float[vBuffer.limit()];
        vBuffer.get(fv);

        float[] f = new float[stBufferF.limit()];
        stBufferF.get(f);

        float[] b = new float[stBufferB.limit()];
        stBufferB.get(b);

        float frontRotate[] = new float[9];
        float frontTranslate[] = new float[3];

        setContraryRotationMtx(frontRotate, params.front_camera_rotation,9);
        setContraryTranslateVecXYZ(frontTranslate, params.front_camera_translation,3);

        float backRotate[] = new float[9];
        float backTranslate[] = new float[3];

        setContraryRotationMtx(backRotate, params.back_camera_rotation,9);
        setContraryTranslateVecXYZ(backTranslate,params.back_camera_translation,3);

        int sphereW = params.out_width; // sphere image width and height
        int sphereH = params.out_height;

        int inX, inY;

        int step = VrConstant.SPHERE_SAMPLE_STEP;
        // example
        // calculate sphere points coordinates in front camera image
        int vtBase = 0;
        int stBaseF = 0;
        int stBaseB = 0;

        for (inY = 0; inY <= sphereH; inY += step) {
            for (inX = 0; inX <= sphereW; inX += step) {

                point_coords_conversion(true, sphereW, sphereH, inX, inY,
                        frontRotate, frontTranslate, params);
                point_coords_conversion(false, sphereW, sphereH, inX, inY,
                        backRotate, backTranslate, params);

                fv[vtBase++] = (float)inX * 2 / sphereW - 1;
                fv[vtBase++] = 1 - (float)inY * 2 / sphereH;
                fv[vtBase++] = 0.0f;

                float sCoordinateF =  outXF / params.fish_eye_width;
                float tCoordinateF =  0.5f * outYF / params.fish_eye_height;

                if(tCoordinateF > 0.5f){
                    tCoordinateF = 0.5f;
                }

                float sCoordinateB =   outXB / params.fish_eye_width;
                float tCoordinateB = 0.5f + 0.5f * outYB / params.fish_eye_height;
                if(tCoordinateB > 1.0f){
                    tCoordinateB = 1.0f;
                }

                f[stBaseF++] = sCoordinateF;
                f[stBaseF++] = tCoordinateF;

                b[stBaseB++] = sCoordinateB;
                b[stBaseB++] = tCoordinateB;
            }
        }


        vBuffer.clear();
        vBuffer.put(fv);

        stBufferF.clear();
        stBufferF.put(f);

        stBufferB.clear();
        stBufferB.put(b);
    }

    private void setContraryRotationMtx(float des[], float src[], int length){
        int i;
        for(i=0; i < length; i++){
            des[i] = src[i];
        }
    }

    private void setContraryTranslateVecXYZ(float des[], float src[], int length)
    {
        int i;
        for(i=0; i < length; i++){
            des[i] = src[i];
        }
        return;
    }

    private void point_coords_conversion(boolean front, int sphereImgW, int sphereImgH, int inX, int inY,
                                 float rotationMtx[], float translateVec[], CombineParams params)
    {// sphereImgW/sphereImgH:  dst sphere image width/height
        // inX/inY:                input points coordinate (in sphere image)
        // outX/outY:              output points coordinate (in fisheye image)
        // ocam_model:             fisheye camera model
        // extParam:               extrinsic parameters between camera and sphere

        float sphere[] = new float[3], cam[] = new float[3], img[] = new float[2];

        float theta = (float) (Math.PI / 2 - Math.PI * inY / sphereImgH);	// latitude ------
        float phi = (float) ( 2 * Math.PI * inX / sphereImgW);	// longitude ---------

        sphere[1] =  (float) (Math.sin(theta) * params.sphere_radius);                // y
        sphere[2] =  (float) (Math.cos(theta) * Math.cos(phi) * params.sphere_radius);     // z
        sphere[0] =  (float) (-Math.cos(theta) * Math.sin(phi) * params.sphere_radius);    // x
        matrixMul(rotationMtx, sphere, cam, 3, 3, 1);
        vectorAdd(cam, translateVec, cam, 3);
        world2cam(front, img, cam, params);

        if (front){
            outXF = img[1];
            outYF = img[0];
        } else {
            outXB = img[1];
            outYB = img[0];
        }
    }

    private void matrixMul(float mtxA[], float mtxB[], float mtxC[], int a, int b, int c)
    {// mtxA(a x b) dot multiple mtxB(b x c) = mtxC(a x c)
        float sub = 0.0f;
        int i, j, k;
        for (i = 0; i < a; i++)
        {
            for (j = 0; j < c; j++)
            {
                sub = 0.0f;
                for (k = 0; k < b; k++)
                {
                    sub += mtxA[i*b + k] * mtxB[k*c + j];
                }
                mtxC[i*c + j] = sub;
            }
        }
    }

    private void vectorAdd(float vecA[], float vecB[], float vecC[], int a)
    {// vecA(a x 1) add up with vecB(a x 1) = vecC(a x 1)
        int i;
        for (i = 0; i < a; i++)
        {
            vecC[i] = vecA[i] + vecB[i];
        }
    }

    private void world2cam(boolean front, float point2D[], float point3D[], CombineParams params)
    {// actually this function transforms camera coords to image coords.
        float invpol[] = front ? params.front_invpol : params.back_invpol;//pocam_model->invpol;
        double xc = front ? params.front_camera_center[0] : params.back_camera_center[0];//(pocam_model->uc);
        double yc = front ? params.front_camera_center[1] : params.back_camera_center[1];//(pocam_model->vc);
        double c = front ? params.front_affine_param[0] : params.front_affine_param[0];//(pocam_model->c);
        double d = front ? params.front_affine_param[1] : params.front_affine_param[1];//(pocam_model->d);
        double e = front ? params.front_affine_param[2] : params.front_affine_param[2];//(pocam_model->e);
        int length_invpol = params.front_invpol.length;//(pocam_model->length_invpol);
        double norm = Math.sqrt(point3D[0] * point3D[0] + point3D[1] * point3D[1]);
        double theta = Math.atan(point3D[2] / norm);
        double t, t_i;
        double rho, x, y;
        double invnorm;
        int i;

        if (norm != 0)
        {
            invnorm = 1 / norm;
            t = theta;
            rho = invpol[0];
            t_i = 1;

            for (i = 1; i < length_invpol; i++)
            {
                t_i *= t;
                rho += t_i*invpol[i];
            }

            x = point3D[0] * invnorm*rho;
            y = point3D[1] * invnorm*rho;

            point2D[0] = (float) (x*c + y*d + xc);
            point2D[1] = (float) (x*e + y + yc);
        }
        else
        {
            point2D[0] = (float) xc;
            point2D[1] = (float) yc;
        }
    }

}
