/************************************************************************/
/* Image warper, with or without hardware accelerations                 */
/************************************************************************/
#pragma once
#ifndef _IMAGE_WARPER_H
#define _IMAGE_WARPER_H

#include "ImageWarpTable.h"
#include "ShaderClass.h"

//#include <CL/cl.h>
//OpenGL 
#define GLEW_STATIC
#include <GLES3/gl3.h>
//#include <glew.h>
//#include <freeglut.h>
//#include <glm/glm.hpp>

//#include <soil/SOIL.h>


namespace YiPanorama {
namespace warper {

using namespace util;

struct ProjectionMapPoint
{// a single mapping point with coordinates and vc factor
    float x;
    float y;
    float f;
};

enum renderDeviceType
{
    useSoftware = 0,
    useOpencl,
    useOpengl
};


class ImageWarper : public imageWarpTable
{
public:
    ImageWarper();
    ~ImageWarper();

    // pure software warping without any hardware acceleration
    int setWarpDevice(renderDeviceType deviceType);
    int warpImage(imageFrame srcImage, imageFrame proImage);

    int warpImageSoftFullWithoutVCSingleChn(unsigned char *srcImage, unsigned char *proImage);// for basic mode mask generating only

private:


    renderDeviceType renderDevice;  // important 

};


}   // namespace warper
}   // namespace YiPanorama

#endif  //!_IMAGE_WARPER_SOFT