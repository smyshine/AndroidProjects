LOCAL_PATH := $(call my-dir)

#static library info
include $(CLEAR_VARS)
LOCAL_MODULE := exiv2
LOCAL_SRC_FILES := lib/libexiv2.a
LOCAL_EXPORT_C_INCLUDES := include/
LOCAL_EXPORT_LDLIBS := -Llib -lz -lexpat
include $(PREBUILT_STATIC_LIBRARY)


#wrapper info
include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/
LOCAL_MODULE    := yiExiv2
LOCAL_SRC_FILES := yiExiv2Jni.cpp
LOCAL_STATIC_LIBRARIES := exiv2
include $(BUILD_SHARED_LIBRARY)
