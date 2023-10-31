//
// Created by Hsj on 2023-10-29.
//

#include "wechat_qr.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

wechat_qr::wechat_qr(const std::string& proto,  const std::string& model,
                     const std::string& proto2, const std::string& model2)
{
    try {
        qr = new cv::wechat_qrcode::WeChatQRCode(proto, model, proto2, model2);
    } catch (const std::exception& e) {
        LOGE("wechat_qr: %s", e.what());
    }
}

wechat_qr::~wechat_qr()
{
    delete qr;
    std::vector<cv::Mat>().swap(points);
    std::vector<std::string>().swap(codes);
}

int wechat_qr::decode(const uint8_t* img, const int width, const int height, const int format)
{
    codes.clear();
    points.clear();
    try {
        cv::Mat image(height, width, format, (void *)img);
        codes = qr->detectAndDecode(image, points);
    } catch (const std::exception& e) {
        LOGE("decode: %s", e.what());
    }
    return codes.size();
}
