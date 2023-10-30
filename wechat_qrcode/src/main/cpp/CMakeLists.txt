cmake_minimum_required(VERSION 3.10.2)
project(qrcode CXX)

# set flag
set(CMAKE_BUILD_TYPE RELEASE)
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -s")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

# log switch
option(LOG_SWITCH "option for print log" ON)
if(LOG_SWITCH)
    add_definitions(-DLOG_SWITCH)
endif(LOG_SWITCH)

# libiconv-1.14
#add_library(iconv SHARED IMPORTED)
#set_target_properties(iconv PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_SOURCE_DIR}/iconv/lib/${ANDROID_ABI}/libiconv.so)

# opencv-4.8.0
#set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/opencv/sdk/native/jni)
#find_package(OpenCV REQUIRED opencv_core opencv_imgproc opencv_objdetect opencv_dnn)
add_library(opencv SHARED IMPORTED)
set_target_properties(opencv PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/opencv/sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so)

# set include
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/qrcode/src
        ${CMAKE_CURRENT_SOURCE_DIR}/qrcode/include
        #${CMAKE_CURRENT_SOURCE_DIR}/iconv/include
        ${CMAKE_CURRENT_SOURCE_DIR}/opencv/sdk/native/jni/include)

# source code
file(GLOB_RECURSE SRC_FILES ${CMAKE_CURRENT_SOURCE_DIR}/qrcode/src/*.cpp)
add_library(${CMAKE_PROJECT_NAME} SHARED ${SRC_FILES} wechat_qr.cpp wechat_qr_native.cpp)

# find system support library
find_library(log-lib log)

# target link libraries turbojpeg
target_link_libraries(${CMAKE_PROJECT_NAME} ${log-lib} opencv)
        #opencv_core opencv_imgproc opencv_objdetect opencv_dnn)