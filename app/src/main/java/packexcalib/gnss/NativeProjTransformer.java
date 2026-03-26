package packexcalib.gnss;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class NativeProjTransformer implements AutoCloseable {

    static {
        System.loadLibrary("nativeproj");
    }

    private long handle = 0;

    public synchronized void init(Context context) throws Exception {
        if (handle != 0) {
            close();
        }

        File projDir = ProjAssetUtils.copyProjAssetsIfNeeded(context);
        Log.d("NativeCzech", "proj dir = " + projDir.getAbsolutePath());

        handle = nativeInit(projDir.getAbsolutePath());
        if (handle == 0) {
            throw new RuntimeException("Init PROJ fallita: " + safeLastError(0));
        }
    }

    public synchronized void initCsToCs(String sourceCrs, String targetCrs) {
        checkInit();

        boolean ok = nativeInitCsToCs(handle, sourceCrs, targetCrs);
        if (!ok) {
            throw new RuntimeException(
                    "Init CRS->CRS fallita: " + sourceCrs + " -> " + targetCrs +
                            " | " + safeLastError(handle)
            );
        }
    }

    public synchronized void initFromParameters(String sourceParams, String targetParams) {
        checkInit();

        boolean ok = nativeInitFromParameters(handle, sourceParams, targetParams);
        if (!ok) {
            throw new RuntimeException(
                    "Init params->params fallita: " + safeLastError(handle)
            );
        }
    }

    public synchronized void initPipeline(String pipeline) {
        checkInit();

        boolean ok = nativeInitPipeline(handle, pipeline);
        if (!ok) {
            throw new RuntimeException(
                    "Init pipeline fallita\n" +
                            "pipeline = " + pipeline + "\n" +
                            "nativeErr = " + safeLastError(handle)
            );
        }
    }

    public synchronized double[] transformPrepared(double x, double y, double z) {
        checkInit();

        double[] out = nativeTransformPrepared(handle, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException(
                    "Transform prepared fallita: " + safeLastError(handle)
            );
        }
        return out;
    }

    public synchronized double[] fromCsToCs(String sourceCrs, String targetCrs,
                                            double x, double y, double z) {
        checkInit();

        double[] out = nativeFromCsToCs(handle, sourceCrs, targetCrs, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException(
                    "Trasformazione CRS->CRS fallita: " + sourceCrs + " -> " + targetCrs +
                            " | " + safeLastError(handle)
            );
        }
        return out;
    }

    public synchronized double[] fromParameters(String sourceParams, String targetParams,
                                                double x, double y, double z) {
        checkInit();

        double[] out = nativeFromParameters(handle, sourceParams, targetParams, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException(
                    "Trasformazione params->params fallita: " + safeLastError(handle)
            );
        }
        return out;
    }

    public synchronized double[] fromPipeline(String pipeline, double x, double y, double z) {
        checkInit();

        double[] out = nativeFromPipeline(handle, pipeline, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException(
                    "Transformtion pipeline failed\n" +
                            "pipeline = " + pipeline + "\n" +
                            "nativeErr = " + safeLastError(handle)
            );
        }
        return out;
    }

    public synchronized void clearPrepared() {
        checkInit();
        nativeClearPrepared(handle);
    }

    public synchronized String getLastError() {
        return safeLastError(handle);
    }

    @Override
    public synchronized void close() {
        if (handle != 0) {
            nativeClose(handle);
            handle = 0;
        }
    }

    private void checkInit() {
        if (handle == 0) {
            throw new IllegalStateException("NativeProjTransformer not init");
        }
    }

    private String safeLastError(long h) {
        try {
            String s = nativeGetLastError(h);
            return (s == null || s.trim().isEmpty()) ? "(nessun dettaglio)" : s;
        } catch (Throwable t) {
            return "(errore nativo non disponibile)";
        }
    }

    private static native long nativeInit(String projDataDir);

    private static native boolean nativeInitCsToCs(long handle, String sourceCrs, String targetCrs);
    private static native boolean nativeInitFromParameters(long handle, String sourceParams, String targetParams);
    private static native boolean nativeInitPipeline(long handle, String pipeline);

    private static native double[] nativeTransformPrepared(long handle, double x, double y, double z);
    private static native void nativeClearPrepared(long handle);

    private static native double[] nativeFromCsToCs(long handle, String sourceCrs, String targetCrs,
                                                    double x, double y, double z);
    private static native double[] nativeFromParameters(long handle, String sourceParams, String targetParams,
                                                        double x, double y, double z);
    private static native double[] nativeFromPipeline(long handle, String pipeline,
                                                      double x, double y, double z);

    private static native String nativeGetLastError(long handle);
    private static native void nativeClose(long handle);
}