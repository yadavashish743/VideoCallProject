STLPORT_FORCE_REBUILD := false
APP_STL := stlport_static
APP_OPTIM := release
APP_PLATFORM := android-16
APP_CPPFLAGS += -std=c++11
LOCAL_PATH := $(call my-dir) NDK_TOOLCHAIN_VERSION=4.4.3 NDK_PROJECT_PATH := $(shell pwd) APP_BUILD_SCRIPT := $(LOCAL_PATH)/../Android.mk