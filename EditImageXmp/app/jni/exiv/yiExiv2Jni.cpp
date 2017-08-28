#include <jni.h>
#include <string>

#include "exiv2/exiv2.hpp"
#include <iostream>
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_editimagexmp_MainActivity_setImageGPano(JNIEnv *env, jobject instance, jstring path_, jint width, jint height) {
    const char *path = env->GetStringUTFChars(path_, 0);

    Exiv2::XmpData xmpData;

    xmpData["Xmp.GPano.UsePanoramaViewer"] = true;
    xmpData["Xmp.GPano.ProjectionType"] = "equirectangular";
    xmpData["Xmp.GPano.CroppedAreaImageWidthPixels"] = width;
    xmpData["Xmp.GPano.CroppedAreaImageHeightPixels"] = height;
    xmpData["Xmp.GPano.CroppedAreaLeftPixels"] = 0;
    xmpData["Xmp.GPano.CroppedAreaTopPixels"] = 0;
    xmpData["Xmp.GPano.FullPanoWidthPixels"] = width;
    xmpData["Xmp.GPano.FullPanoHeightPixels"] = height;

    Exiv2::Image::AutoPtr image = Exiv2::ImageFactory::open(path);
    image->setXmpData(xmpData);
    image->writeMetadata();

    cout << path << endl;

    env->ReleaseStringUTFChars(path_, path);
}

