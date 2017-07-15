LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libandroidgl20
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS    := -Werror -O2 -ffast-math
LOCAL_SRC_FILES := AndroidGL20.cpp
LOCAL_LDLIBS    := -llog -lGLESv2

include $(BUILD_SHARED_LIBRARY)
