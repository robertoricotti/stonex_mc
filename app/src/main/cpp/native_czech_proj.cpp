#include <jni.h>
#include <cstring>
#include <android/log.h>
#include "proj.h"

#define TAG "NativeCzechProj"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

struct ProjHolder {
    PJ_CONTEXT* ctx = nullptr;
    PJ* to5514 = nullptr;
    PJ* to5513 = nullptr;
    PJ* to5516 = nullptr;   // custom NO v1710 oppure EPSG:5516 fallback
};

static const char* ctxErr(PJ_CONTEXT* ctx) {
    int err = proj_context_errno(ctx);
    return err ? proj_context_errno_string(ctx, err) : "no error";
}

static const char* pjErr(PJ* P) {
    int err = proj_errno(P);
    return err ? proj_errno_string(err) : "no error";
}

static PJ* normalizeIfPossible(PJ_CONTEXT* ctx, PJ* raw) {
    if (!raw) return nullptr;
    PJ* normalized = proj_normalize_for_visualization(ctx, raw);
    if (!normalized) {
        LOGE("proj_normalize_for_visualization fallita: %s", ctxErr(ctx));
        proj_destroy(raw);
        return nullptr;
    }
    proj_destroy(raw);
    return normalized;
}

static PJ* createTransform(PJ_CONTEXT* ctx, const char* src, const char* dst) {
    PJ* raw = proj_create_crs_to_crs(ctx, src, dst, nullptr);
    if (!raw) {
        LOGE("proj_create_crs_to_crs(%s -> %s) fallita: %s", src, dst, ctxErr(ctx));
        return nullptr;
    }
    PJ* normalized = normalizeIfPossible(ctx, raw);
    if (!normalized) {
        LOGE("normalize %s -> %s fallita: %s", src, dst, ctxErr(ctx));
        return nullptr;
    }
    LOGI("Transform creata: %s -> %s", src, dst);
    return normalized;
}

static PJ* createPipeline(PJ_CONTEXT* ctx, const char* pipeline, const char* label) {
    PJ* raw = proj_create(ctx, pipeline);
    if (!raw) {
        LOGE("proj_create(%s) fallita: %s", label, ctxErr(ctx));
        return nullptr;
    }
    PJ* normalized = normalizeIfPossible(ctx, raw);
    if (!normalized) {
        LOGE("normalize pipeline %s fallita: %s", label, ctxErr(ctx));
        return nullptr;
    }
    LOGI("Pipeline creata: %s", label);
    return normalized;
}

static jdoubleArray transformInternal(JNIEnv* env, PJ* transform, double lon, double lat, double h) {
    if (!transform) {
        LOGE("transformInternal: transform nullo");
        return nullptr;
    }

    proj_errno_reset(transform);

    PJ_COORD in;
    std::memset(&in, 0, sizeof(PJ_COORD));

    // Con proj_normalize_for_visualization:
    // input geografico in gradi = lon, lat, h
    in.lpzt.lam = lon;
    in.lpzt.phi = lat;
    in.lpzt.z   = h;
    in.lpzt.t   = 0.0;

    PJ_COORD out = proj_trans(transform, PJ_FWD, in);

    if (proj_errno(transform) != 0) {
        LOGE("proj_trans fallita: %s", pjErr(transform));
        return nullptr;
    }

    // Controllo basilare anche sui numeri
    if (!(out.xyz.x == out.xyz.x) || !(out.xyz.y == out.xyz.y) || !(out.xyz.z == out.xyz.z)) {
        LOGE("proj_trans ha restituito NaN");
        return nullptr;
    }

    jdouble result[3];
    result[0] = out.xyz.x;
    result[1] = out.xyz.y;
    result[2] = out.xyz.z;

    jdoubleArray arr = env->NewDoubleArray(3);
    if (!arr) return nullptr;
    env->SetDoubleArrayRegion(arr, 0, 3, result);
    return arr;
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

    const char* searchPaths[1] = { projDataDir };
    proj_context_set_search_paths(holder->ctx, 1, searchPaths);

    // Questi restano come li hai già e ti funzionano
    holder->to5514 = createTransform(holder->ctx, "EPSG:4326", "EPSG:5514");
    holder->to5513 = createTransform(holder->ctx, "EPSG:4326", "EPSG:5513");

    // Pipeline custom per il sistema del software topografico:
    // ETRF2000 / S-JTSK (Ferro) / Krovak Modified (NO) v1710
    //
    // ATTENZIONE:
    // +proj=mod_krovak richiede PROJ >= 9.4.0
    const char* noV1710Pipeline =
        "+proj=pipeline "
        "+step +proj=unitconvert +xy_in=deg +xy_out=rad "
        "+step +proj=cart +ellps=GRS80 "
        "+step +proj=helmert "
        "+x=572.203 +y=85.328 +z=461.934 "
        "+rx=-4.973117 +ry=-1.529001 +rz=-5.248327 "
        "+s=3.5393 +convention=position_vector "
        "+step +inv +proj=cart +ellps=bessel "
        "+step +proj=mod_krovak "
        "+pm=ferro "
        "+lat_0=49.5 "
        "+lon_0=42.5 "
        "+k_0=0.9999 "
        "+x_0=0 +y_0=0";

    holder->to5516 = createPipeline(holder->ctx, noV1710Pipeline, "NO_v1710_mod_krovak");

    // Fallback: se la pipeline custom fallisce, prova EPSG:5516 standard
    if (!holder->to5516) {
        LOGE("Pipeline custom 5516 fallita, provo fallback EPSG:5516");
        holder->to5516 = createTransform(holder->ctx, "EPSG:4326", "EPSG:5516");
    }

    env->ReleaseStringUTFChars(projDataDir_, projDataDir);

    if (!holder->to5514 || !holder->to5513 || !holder->to5516) {
        LOGE("Init fallita: to5514=%p to5513=%p to5516=%p",
             holder->to5514, holder->to5513, holder->to5516);

        if (holder->to5514) proj_destroy(holder->to5514);
        if (holder->to5513) proj_destroy(holder->to5513);
        if (holder->to5516) proj_destroy(holder->to5516);
        if (holder->ctx) proj_context_destroy(holder->ctx);
        delete holder;
        return 0;
    }

    return reinterpret_cast<jlong>(holder);
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
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeCzechTransformer_nativeWgs84To5516(
        JNIEnv* env,
        jclass,
        jlong handle,
        jdouble lon,
        jdouble lat,
        jdouble h) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return nullptr;
    return transformInternal(env, holder->to5516, lon, lat, h);
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
    if (holder->to5516) proj_destroy(holder->to5516);
    if (holder->ctx) proj_context_destroy(holder->ctx);

    delete holder;
}