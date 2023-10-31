//
// Created by Hsj on 2023-10-29.
//

#include <jni.h>
#include "wechat_qr.h"

#define HANDLE "handle"
#define JAVA_NAME "android/wechat/qrcode/QRResolver"

typedef jlong HANDLE_ID;

static void set_field_long(JNIEnv *env, jobject obj,
        const char *name, jlong value)
{
    jclass clazz = env->GetObjectClass(obj);
    jfieldID field = env->GetFieldID(clazz, name, "J");
    if (field) {
        env->SetLongField(obj, field, value);
    } else {
        LOGE("set_field_long failed: GetFieldID(%s).", name);
    }
    env->DeleteLocalRef(clazz);
}

static HANDLE_ID create(JNIEnv *env, jobject thiz,
        jstring proto1, jstring model1,
        jstring proto2, jstring model2)
{
    if (proto1 && model1 && proto2 && model2) {
        const char* _proto1 = env->GetStringUTFChars(proto1, JNI_FALSE);
        const char* _model1 = env->GetStringUTFChars(model1, JNI_FALSE);
        const char* _proto2 = env->GetStringUTFChars(proto2, JNI_FALSE);
        const char* _model2 = env->GetStringUTFChars(model2, JNI_FALSE);
        auto* qr = new wechat_qr(_proto1, _model1, _proto2, _model2);
        env->ReleaseStringUTFChars(proto1, _proto1);
        env->ReleaseStringUTFChars(model1, _model1);
        env->ReleaseStringUTFChars(proto2, _proto2);
        env->ReleaseStringUTFChars(model2, _model2);
        auto id = reinterpret_cast<HANDLE_ID>(qr);
        set_field_long(env, thiz, HANDLE, id);
        return id;
    } else {
        LOGE("create: had args null.");
        return ERROR_ARG;
    }
}

static jint decode1(JNIEnv *env, jobject thiz, HANDLE_ID id,
        jbyteArray img, jint width, jint height, jint format,
        jobject codes, jobject points)
{
    jint ret = ERROR_ARG;
    auto *qr = reinterpret_cast<wechat_qr *>(id);
    if (!qr) {
        ret = ERROR_INIT;
        LOGE("decode: already destroyed.");
    } else if (!codes) {
        LOGE("decode: codes is null.");
    } else if (!points) {
        LOGE("decode: points is null.");
    } else {
        jbyte* _img = env->GetByteArrayElements(img, JNI_FALSE);
        ret = qr-> decode((const uint8_t *)_img, width, height, format);
        env->ReleaseByteArrayElements(img, _img, JNI_ABORT);
        if (ret > 0) {
            // set points and codes
            std::vector<std::string> _codes = qr->get_codes();
            std::vector<cv::Mat> _points = qr->get_points();
            jclass list_class = env->GetObjectClass(codes);
            jmethodID method = env->GetMethodID(list_class, "add", "(Ljava/lang/Object;)Z");
            for (int i = 0; i < ret; ++i) {
                cv::Mat point = _points[i];
                env->CallBooleanMethod(points, method,  point.at<float>(0, 0));
                env->CallBooleanMethod(points, method,  point.at<float>(0, 1));
                env->CallBooleanMethod(points, method,  point.at<float>(1, 0));
                env->CallBooleanMethod(points, method,  point.at<float>(1, 1));
                env->CallBooleanMethod(points, method,  point.at<float>(2, 0));
                env->CallBooleanMethod(points, method,  point.at<float>(2, 1));
                env->CallBooleanMethod(points, method,  point.at<float>(3, 0));
                env->CallBooleanMethod(points, method,  point.at<float>(3, 1));
                env->CallBooleanMethod(codes,  method, env->NewStringUTF(_codes[i].c_str()));
            }
        }
    }
    return ret;
}

static jint decode2(JNIEnv *env, jobject thiz, HANDLE_ID id,
        jobject img, jint width, jint height, jint format,
        jobject codes, jobject points)
{
    jint ret = ERROR_ARG;
    auto *qr = reinterpret_cast<wechat_qr *>(id);
    if (!qr) {
        ret = ERROR_INIT;
        LOGE("decode: already destroyed.");
    } else if (!codes) {
        LOGE("decode: codes is null.");
    } else if (!points) {
        LOGE("decode: points is null.");
    } else {
        auto _img = (const uint8_t *)env->GetDirectBufferAddress(img);
        ret = qr-> decode(_img, width, height, format);
        if (ret > 0) {
            // set points and codes
            std::vector<std::string> _codes = qr->get_codes();
            std::vector<cv::Mat> _points = qr->get_points();
            jclass list_class = env->GetObjectClass(codes);
            jclass float_class = env->FindClass("java/lang/Float");
            jmethodID float_method = env->GetMethodID(float_class, "<init>", "(F)V");
            jmethodID add_method = env->GetMethodID(list_class, "add", "(Ljava/lang/Object;)Z");
            for (int i = 0; i < ret; ++i) {
                jstring code = env->NewStringUTF(_codes[i].c_str());
                env->CallBooleanMethod(codes,  add_method, code);
                env->DeleteLocalRef(code);

                cv::Mat point = _points[i];
                jobject float1 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(0, 0));
                jobject float2 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(0, 1));
                jobject float3 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(1, 0));
                jobject float4 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(1, 1));
                jobject float5 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(2, 0));
                jobject float6 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(2, 1));
                jobject float7 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(3, 0));
                jobject float8 = env->NewObject(float_class, float_method, (jfloat)point.at<float>(3, 1));

                env->CallBooleanMethod(points, add_method, float1);
                env->CallBooleanMethod(points, add_method, float2);
                env->CallBooleanMethod(points, add_method, float3);
                env->CallBooleanMethod(points, add_method, float4);
                env->CallBooleanMethod(points, add_method, float5);
                env->CallBooleanMethod(points, add_method, float6);
                env->CallBooleanMethod(points, add_method, float7);
                env->CallBooleanMethod(points, add_method, float8);

                env->DeleteLocalRef(float1);
                env->DeleteLocalRef(float2);
                env->DeleteLocalRef(float3);
                env->DeleteLocalRef(float4);
                env->DeleteLocalRef(float5);
                env->DeleteLocalRef(float6);
                env->DeleteLocalRef(float7);
                env->DeleteLocalRef(float8);
            }
        }
    }
    return ret;
}

static void destroy(JNIEnv *env, jobject thiz, HANDLE_ID id)
{
    auto *qr = reinterpret_cast<wechat_qr *>(id);
    if (qr) {
        set_field_long(env, thiz, HANDLE, 0);
        delete qr;
    } else {
        LOGW("destroy: already destroyed.");
    }
}

//==================================================================================================

static const JNINativeMethod NATIVE_METHODS[] = {
        {"create",  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)J",  (void *) create},
        {"decode1", "(J[BIIILjava/util/List;Ljava/util/List;)I",                                    (void *) decode1},
        {"decode2", "(JLjava/nio/ByteBuffer;IIILjava/util/List;Ljava/util/List;)I",                 (void *) decode2},
        {"destroy", "(J)V",                                                                         (void *) destroy}
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void* reserved) {
    JNIEnv *env;
    jint ret = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (ret == JNI_OK) {
        jclass cls = env->FindClass(JAVA_NAME);
        if (cls != nullptr) {
            jint size = sizeof(NATIVE_METHODS) / sizeof(JNINativeMethod);
            ret = env->RegisterNatives(cls, NATIVE_METHODS, size);
        } else {
            LOGE("JNI_OnLoad failed: FindClass(%s).", JAVA_NAME);
            ret = JNI_ERR;
        }
    } else {
        LOGE("JNI_OnLoad failed: GetEnv() %d.", ret);
    }
    return ret == JNI_OK ? JNI_VERSION_1_6 : ret;
}

//==================================================================================================