#include <jni.h>
#include <cstring>
#include "proj.h"

struct ProjHolder {
    PJ_CONTEXT* ctx = nullptr;
    PJ* to5514 = nullptr;
    PJ* to5513 = nullptr;
};

static PJ* createTransform(PJ_CONTEXT* ctx, const char* src, const char* dst) {
    PJ* raw = proj_create_crs_to_crs(ctx, src, dst, nullptr);
    if (!raw) return nullptr;

    PJ* normalized = proj_normalize_for_visualization(ctx, raw);
    proj_destroy(raw);
    return normalized;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_packexcalib_gnss_NativeCzechTransformer_nativeInit(
        JNIEnv* env,
        jclass,
        jstring projDataDir_) {

    const char* projDataDir = env->GetStringUTFChars(projDataDir_, nullptr);

    auto* holder = new ProjHolder();
    holder->ctx = proj_context_create();
    if (!holder->ctx) {
        env->ReleaseStringUTFChars(projDataDir_, projDataDir);
        delete holder;
        return 0;
    }

    const char* searchPaths[1];
    searchPaths[0] = projDataDir;
    proj_context_set_search_paths(holder->ctx, 1, searchPaths);

    holder->to5514 = createTransform(holder->ctx, "EPSG:4326", "EPSG:5514");
    holder->to5513 = createTransform(holder->ctx, "EPSG:4326", "EPSG:5513");

    env->ReleaseStringUTFChars(projDataDir_, projDataDir);

    if (!holder->to5514 || !holder->to5513) {
        if (holder->to5514) proj_destroy(holder->to5514);
        if (holder->to5513) proj_destroy(holder->to5513);
        if (holder->ctx) proj_context_destroy(holder->ctx);
        delete holder;
        return 0;
    }

    return reinterpret_cast<jlong>(holder);
}

static jdoubleArray transformInternal(JNIEnv* env, PJ* transform, double lon, double lat, double h) {
    if (!transform) return nullptr;

    PJ_COORD in;
    std::memset(&in, 0, sizeof(PJ_COORD));

    // Con normalize_for_visualization: input geografico = lon, lat
    in.lpzt.lam = lon;
    in.lpzt.phi = lat;
    in.lpzt.z = h;
    in.lpzt.t = 0.0;

    PJ_COORD out = proj_trans(transform, PJ_FWD, in);

    jdouble result[3];
    result[0] = out.xyz.x;
    result[1] = out.xyz.y;
    result[2] = out.xyz.z;

    jdoubleArray arr = env->NewDoubleArray(3);
    env->SetDoubleArrayRegion(arr, 0, 3, result);
    return arr;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeCzechTransformer_nativeWgs84To5514(
        JNIEnv* env,
        jclass,
        jlong handle,
        jdouble lon,
        jdouble lat,
        jdouble h) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return nullptr;
    return transformInternal(env, holder->to5514, lon, lat, h);
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeCzechTransformer_nativeWgs84To5513(
        JNIEnv* env,
        jclass,
        jlong handle,
        jdouble lon,
        jdouble lat,
        jdouble h) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return nullptr;
    return transformInternal(env, holder->to5513, lon, lat, h);
}

extern "C"
JNIEXPORT void JNICALL
Java_packexcalib_gnss_NativeCzechTransformer_nativeClose(
        JNIEnv*,
        jclass,
        jlong handle) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return;

    if (holder->to5514) proj_destroy(holder->to5514);
    if (holder->to5513) proj_destroy(holder->to5513);
    if (holder->ctx) proj_context_destroy(holder->ctx);

    delete holder;
}