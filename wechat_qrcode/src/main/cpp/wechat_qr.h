//
// Created by Hsj on 2023-10-29.
//

#ifndef ANDROID_WECHAT_QRCODE_WECHAT_QR_H
#define ANDROID_WECHAT_QRCODE_WECHAT_QR_H

#include <vector>
#include <string>
#include <cstdlib>
#include <unistd.h>
#include <libgen.h>
#include <android/log.h>
#include <opencv2/wechat_qrcode.hpp>

#define ERROR_EXE    -0x01;
#define ERROR_ARG    -0x02;
#define ERROR_INIT   -0x03;
#define LOG_TAG "QR"

#ifdef LOG_SWITCH
    #define LOGE(FMT,...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "[%d*%s:%d:%s]:" FMT,\
                                    gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ##__VA_ARGS__)
    #define LOGW(FMT,...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, "[%d*%s:%d:%s]:" FMT, \
                                    gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ##__VA_ARGS__)
    #define LOGD(FMT,...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "[%d*%s:%d:%s]:" FMT,\
                                    gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ##__VA_ARGS__)
    #define LOGI(FMT,...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "[%d*%s:%d:%s]:" FMT, \
                                    gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ##__VA_ARGS__)
#else
    #define LOGE(...)
    #define LOGW(...)
    #define LOGD(...)
    #define LOGI(...)
#endif

class wechat_qr {
public:
    explicit wechat_qr(const std::string& model,  const std::string& proto,
                       const std::string& model2, const std::string& proto2);
    ~wechat_qr();
    std::vector<cv::Mat> get_points(){ return points; };
    std::vector<std::string> get_codes(){ return codes; };
    int decode(const uint8_t* img, const int width, const int height, const int format, const float scale);
private:
    cv::wechat_qrcode::WeChatQRCode* qr;
    std::vector<std::string> codes;
    std::vector<cv::Mat> points;
};

#endif //ANDROID_WECHAT_QRCODE_WECHAT_QR_H
