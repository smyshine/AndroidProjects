# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             imageStitch

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/imageStitch.cpp
             src/main/cpp/fisheye_stitch/FisheyePanoStitcherComp.cpp
             src/main/cpp/fisheye_stitch/CameraMetadata.cpp
             src/main/cpp/fisheye_stitch/ImageWarper.cpp
             src/main/cpp/fisheye_stitch/ShaderClass.cpp
             src/main/cpp/fisheye_stitch/ImageColorAdjuster.cpp
             src/main/cpp/fisheye_stitch/ImageBlender.cpp
             src/main/cpp/fisheye_stitch/ImageWarpTable.cpp
             src/main/cpp/fisheye_stitch/ImageIOConverter.cpp
             src/main/cpp/fisheye_stitch/FisheyePanoParams.cpp
             src/main/cpp/fisheye_stitch/MatrixVectors.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

include_directories( # include dependent directories
                     ${CMAKE_SOURCE_DIR}/src/main/cpp/fisheye_stitch/native/jni/include
                     ${CMAKE_SOURCE_DIR}/src/main/cpp/fisheye_stitch/native/jni/include/opencv2
                     ${CMAKE_SOURCE_DIR}/src/main/cpp/fisheye_stitch/native
                     )

set(opencvlibs "${CMAKE_SOURCE_DIR}/src/main/cpp/fisheye_stitch/native/libs/armeabi-v7a/")

add_library(opencv_java SHARED IMPORTED)
set_target_properties(opencv_java PROPERTIES
    IMPORTED_LOCATION "${opencvlibs}libopencv_java3.so"
)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       imageStitch

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       EGL
                       GLESv3
                       opencv_java)
