LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        onLoad.cpp\
        music_i2c.cpp\
				keyevent.cpp\


        
LOCAL_C_INCLUDES:= value.h\



LOCAL_SHARED_LIBRARIES:= \
        libbinder                       \
        libcutils                       \
 



LOCAL_MODULE:= libmusic_i2c

LOCAL_MODULE_TAGS := debug

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)
################################################################################


################################################################################

