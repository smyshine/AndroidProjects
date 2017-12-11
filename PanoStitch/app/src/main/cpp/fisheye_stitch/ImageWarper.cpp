
#include "ImageWarper.h"
#include "MappingCL.h"

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>

#pragma warning (disable :4996)

namespace YiPanorama {
namespace warper {

using namespace std;

    ImageWarper::ImageWarper()
    {
    }

    ImageWarper::~ImageWarper()
    {
    }


int ImageWarper::setWarpDevice(renderDeviceType deviceType)
{
	renderDevice = deviceType;
	return 0;
}


}   // namespace warper
}   // namespace YiPanorama
