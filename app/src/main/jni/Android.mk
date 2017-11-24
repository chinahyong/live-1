LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := feizao
LOCAL_SRC_FILES := feizao.c 
LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid -lOpenSLES
#LOCAL_MODULE    := tutorial07
#LOCAL_SRC_FILES := tutorial07.c
#LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libwsresample librtmp
#LOCAL_C_INCLUDES := /librtmp

include $(BUILD_SHARED_LIBRARY)
$(call import-add-path,/cygdrive/e/BaiduYunDownload/android-ndk-r5)
$(call import-module,ffmpeg-2.0.6/android/arm)
