//
// Created by Hsj on 2023-10-29.
//

#include "wechat_qr.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc/types_c.h>

wechat_qr::wechat_qr(const std::string& model,  const std::string& proto,
                     const std::string& model2, const std::string& proto2) {
    try {
        qr = new cv::wechat_qrcode::WeChatQRCode(model, proto, model2, proto2);
    } catch (const std::exception& e) {
        LOGE("wechat_qr: %s", e.what());
    }
}

wechat_qr::~wechat_qr() {
    delete qr;
    std::vector<float>().swap(points);
    std::vector<std::string>().swap(codes);
}

int wechat_qr::decode(const uint8_t* img, const int width, const int height) {
    points.clear();
    cv::Mat image = cv::Mat(height, width, CV_8UC1, (void *)img);
    if (height == 1) {
        cv::Mat mat = cv::imdecode(image, cv::IMREAD_COLOR);
        cv::Mat gray; cv::cvtColor(mat, gray, CV_BGR2GRAY);
        codes = qr->detectAndDecode(gray, points);
    } else {
        codes = qr->detectAndDecode(image, points);
    }
    return codes.size();
}
