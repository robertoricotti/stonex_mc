#include <jni.h>
#include <cstring>
#include <string>
#include <cmath>
#include "proj.h"

struct ProjHolder {
    PJ_CONTEXT* ctx = nullptr;
    PJ* prepared = nullptr;
    std::string lastError;
};

static void setLastError(ProjHolder* holder, const std::string& msg) {
    if (holder) holder->lastError = msg;
}

static void setLastErrorFromContext(ProjHolder* holder, const std::string& prefix = "") {
    if (!holder || !holder->ctx) {
        if (holder) holder->lastError = prefix.empty() ? "Errore PROJ sconosciuto" : prefix;
        return;
    }

    const int err = proj_context_errno(holder->ctx);
    const char* errStr = proj_context_errno_string(holder->ctx, err);

    std::string out;
    if (!prefix.empty()) {
        out += prefix;
        out += " | ";
    }
    out += "PROJ errno=" + std::to_string(err);
    if (errStr && *errStr) {
        out += " | ";
        out += errStr;
    }
    holder->lastError = out;
}

static std::string joinPath(const char* base, const char* file) {
    std::string s = base ? std::string(base) : std::string();
    if (!s.empty() && s.back() != '/' && s.back() != '\\') s += "/";
    if (file) s += file;
    return s;
}

static PJ* normalizeIfPossible(PJ_CONTEXT* ctx, PJ* raw) {
    if (!raw) return nullptr;

    PJ* normalized = proj_normalize_for_visualization(ctx, raw);
    if (!normalized) {
        proj_destroy(raw);
        return nullptr;
    }

    proj_destroy(raw);
    return normalized;
}

static jdoubleArray transformWithPJ(JNIEnv* env, ProjHolder* holder, PJ* transform, double x, double y, double z) {
    if (!transform) {
        setLastError(holder, "Transform null");
        return nullptr;
    }

    proj_errno_reset(transform);

    PJ_COORD in;
    std::memset(&in, 0, sizeof(PJ_COORD));
    in.xyzt.x = x;
    in.xyzt.y = y;
    in.xyzt.z = z;
    in.xyzt.t = 0.0;

    PJ_COORD out = proj_trans(transform, PJ_FWD, in);

    const int pjErr = proj_errno(transform);
    const int ctxErr = (holder && holder->ctx) ? proj_context_errno(holder->ctx) : 0;

    if (pjErr != 0 || ctxErr != 0 ||
        !std::isfinite(out.xyzt.x) || !std::isfinite(out.xyzt.y) || !std::isfinite(out.xyzt.z)) {
        setLastErrorFromContext(holder, "proj_trans fallita");
        return nullptr;
    }

    const jdouble result[3] = { out.xyzt.x, out.xyzt.y, out.xyzt.z };
    jdoubleArray arr = env->NewDoubleArray(3);
    if (!arr) {
        setLastError(holder, "NewDoubleArray fallita");
        return nullptr;
    }

    env->SetDoubleArrayRegion(arr, 0, 3, result);
    return arr;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeInit(
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

    proj_context_set_enable_network(holder->ctx, false);

    const char* searchPaths[1] = { projDataDir };
    proj_context_set_search_paths(holder->ctx, 1, searchPaths);

    std::string dbPath = joinPath(projDataDir, "proj.db");
    proj_context_set_database_path(holder->ctx, dbPath.c_str(), nullptr, nullptr);

    holder->lastError = "OK";

    env->ReleaseStringUTFChars(projDataDir_, projDataDir);
    return reinterpret_cast<jlong>(holder);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeInitCsToCs(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring sourceCrs_,
        jstring targetCrs_) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return JNI_FALSE;

    const char* sourceCrs = env->GetStringUTFChars(sourceCrs_, nullptr);
    const char* targetCrs = env->GetStringUTFChars(targetCrs_, nullptr);

    PJ* raw = proj_create_crs_to_crs(holder->ctx, sourceCrs, targetCrs, nullptr);

    env->ReleaseStringUTFChars(sourceCrs_, sourceCrs);
    env->ReleaseStringUTFChars(targetCrs_, targetCrs);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create_crs_to_crs fallita");
        return JNI_FALSE;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization fallita");
        return JNI_FALSE;
    }

    if (holder->prepared) {
        proj_destroy(holder->prepared);
        holder->prepared = nullptr;
    }

    holder->prepared = transform;
    holder->lastError = "OK";
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeInitFromParameters(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring sourceParams_,
        jstring targetParams_) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return JNI_FALSE;

    const char* sourceParams = env->GetStringUTFChars(sourceParams_, nullptr);
    const char* targetParams = env->GetStringUTFChars(targetParams_, nullptr);

    PJ* src = proj_create(holder->ctx, sourceParams);
    PJ* dst = proj_create(holder->ctx, targetParams);

    env->ReleaseStringUTFChars(sourceParams_, sourceParams);
    env->ReleaseStringUTFChars(targetParams_, targetParams);

    if (!src || !dst) {
        if (src) proj_destroy(src);
        if (dst) proj_destroy(dst);
        setLastErrorFromContext(holder, "proj_create(source/dest) fallita");
        return JNI_FALSE;
    }

    PJ* raw = proj_create_crs_to_crs_from_pj(holder->ctx, src, dst, nullptr, nullptr);

    proj_destroy(src);
    proj_destroy(dst);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create_crs_to_crs_from_pj fallita");
        return JNI_FALSE;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization fallita");
        return JNI_FALSE;
    }

    if (holder->prepared) {
        proj_destroy(holder->prepared);
        holder->prepared = nullptr;
    }

    holder->prepared = transform;
    holder->lastError = "OK";
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeInitPipeline(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring pipeline_) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return JNI_FALSE;

    const char* pipeline = env->GetStringUTFChars(pipeline_, nullptr);
    PJ* raw = proj_create(holder->ctx, pipeline);
    env->ReleaseStringUTFChars(pipeline_, pipeline);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create(pipeline) fallita");
        return JNI_FALSE;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization(pipeline) fallita");
        return JNI_FALSE;
    }

    if (holder->prepared) {
        proj_destroy(holder->prepared);
        holder->prepared = nullptr;
    }

    holder->prepared = transform;
    holder->lastError = "OK";
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeTransformPrepared(
        JNIEnv* env,
        jclass,
        jlong handle,
        jdouble x,
        jdouble y,
        jdouble z) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->prepared) {
        if (holder) setLastError(holder, "Prepared transform non inizializzata");
        return nullptr;
    }

    return transformWithPJ(env, holder, holder->prepared, x, y, z);
}

extern "C"
JNIEXPORT void JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeClearPrepared(
        JNIEnv*,
        jclass,
        jlong handle) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return;

    if (holder->prepared) {
        proj_destroy(holder->prepared);
        holder->prepared = nullptr;
    }
    holder->lastError = "OK";
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeFromCsToCs(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring sourceCrs_,
        jstring targetCrs_,
        jdouble x,
        jdouble y,
        jdouble z) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return nullptr;

    const char* sourceCrs = env->GetStringUTFChars(sourceCrs_, nullptr);
    const char* targetCrs = env->GetStringUTFChars(targetCrs_, nullptr);

    PJ* raw = proj_create_crs_to_crs(holder->ctx, sourceCrs, targetCrs, nullptr);

    env->ReleaseStringUTFChars(sourceCrs_, sourceCrs);
    env->ReleaseStringUTFChars(targetCrs_, targetCrs);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create_crs_to_crs one-shot fallita");
        return nullptr;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization one-shot fallita");
        return nullptr;
    }

    jdoubleArray result = transformWithPJ(env, holder, transform, x, y, z);
    proj_destroy(transform);
    return result;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeFromParameters(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring sourceParams_,
        jstring targetParams_,
        jdouble x,
        jdouble y,
        jdouble z) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return nullptr;

    const char* sourceParams = env->GetStringUTFChars(sourceParams_, nullptr);
    const char* targetParams = env->GetStringUTFChars(targetParams_, nullptr);

    PJ* src = proj_create(holder->ctx, sourceParams);
    PJ* dst = proj_create(holder->ctx, targetParams);

    env->ReleaseStringUTFChars(sourceParams_, sourceParams);
    env->ReleaseStringUTFChars(targetParams_, targetParams);

    if (!src || !dst) {
        if (src) proj_destroy(src);
        if (dst) proj_destroy(dst);
        setLastErrorFromContext(holder, "proj_create(source/dest) one-shot fallita");
        return nullptr;
    }

    PJ* raw = proj_create_crs_to_crs_from_pj(holder->ctx, src, dst, nullptr, nullptr);

    proj_destroy(src);
    proj_destroy(dst);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create_crs_to_crs_from_pj one-shot fallita");
        return nullptr;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization one-shot fallita");
        return nullptr;
    }

    jdoubleArray result = transformWithPJ(env, holder, transform, x, y, z);
    proj_destroy(transform);
    return result;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeFromPipeline(
        JNIEnv* env,
        jclass,
        jlong handle,
        jstring pipeline_,
        jdouble x,
        jdouble y,
        jdouble z) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder || !holder->ctx) return nullptr;

    const char* pipeline = env->GetStringUTFChars(pipeline_, nullptr);
    PJ* raw = proj_create(holder->ctx, pipeline);
    env->ReleaseStringUTFChars(pipeline_, pipeline);

    if (!raw) {
        setLastErrorFromContext(holder, "proj_create(pipeline) one-shot fallita");
        return nullptr;
    }

    PJ* transform = normalizeIfPossible(holder->ctx, raw);
    if (!transform) {
        setLastErrorFromContext(holder, "proj_normalize_for_visualization(pipeline) one-shot fallita");
        return nullptr;
    }

    jdoubleArray result = transformWithPJ(env, holder, transform, x, y, z);
    proj_destroy(transform);
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeGetLastError(
        JNIEnv* env,
        jclass,
        jlong handle) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    std::string msg = holder ? holder->lastError : "Handle nullo / non inizializzato";
    return env->NewStringUTF(msg.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_packexcalib_gnss_NativeProjTransformer_nativeClose(
        JNIEnv*,
        jclass,
        jlong handle) {

    auto* holder = reinterpret_cast<ProjHolder*>(handle);
    if (!holder) return;

    if (holder->prepared) {
        proj_destroy(holder->prepared);
        holder->prepared = nullptr;
    }

    if (holder->ctx) {
        proj_context_destroy(holder->ctx);
        holder->ctx = nullptr;
    }

    delete holder;
}
