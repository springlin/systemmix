LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    ../../../../../vendor/mstar/dangs/bootx/boot.cpp\
	  ../../../../../vendor/mstar/dangs/bootx/os.c\
	  ../../../../../vendor/mstar/dangs/bootx/resolve_conf.c\
	  ../../../../../vendor/mstar/dangs/bootx/str2argv.c\
  	../../../../../vendor/mstar/dangs/bootx/util.c\
	  ../../../../../vendor/mstar/dangs/bootx/bootfile/leo_boot.c\
	  com_dangs_AdManager.cpp\

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libnativehelper \
    libutils \
    libbinder \
    libui \
    libcutils \
    libsystemmixservice \
    libusbapi\

LOCAL_STATIC_LIBRARIES :=

LOCAL_C_INCLUDES += \
    frameworks/base/core/jni \
    frameworks/base/dangzhi/os/libsystemmix \
    $(JNI_H_INCLUDE) \
    system/core/include/cutils \
    vendor/mstar/dangs/bootx\

LOCAL_CFLAGS +=

LOCAL_MODULE_TAGS := optional

#LOCAL_LDLIBS := -lpthread

LOCAL_MODULE:= libadmanager_jni

LOCAL_PRELINK_MODULE:= false

include $(BUILD_SHARED_LIBRARY)

