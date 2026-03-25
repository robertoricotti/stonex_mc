package packexcalib.gnss;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class NativeProjTransformer implements AutoCloseable {

    static {
        System.loadLibrary("sqlite3");
        System.loadLibrary("proj");
        System.loadLibrary("nativeczechproj");
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
            throw new RuntimeException("Init PROJ fallita: " + safeLastError(null));
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
            throw new RuntimeException("Init params->params fallita: " + safeLastError(handle));
        }
    }

    /**
     * Nuovo: permette di preparare una pipeline PROJ esplicita.
     * Non rompe nulla del codice esistente.
     */
    public synchronized void initPipeline(String pipeline) {
        checkInit();
        boolean ok = nativeInitPipeline(handle, pipeline);
        if (!ok) {
            String nativeErr = safeLastError(handle);
            throw new RuntimeException(
                    "Init pipeline fallita\n" +
                            "pipeline = " + pipeline + "\n" +
                            "nativeErr = " + nativeErr
            );
        }
    }

    public synchronized double[] transformPrepared(double x, double y, double z) {
        checkInit();
        double[] out = nativeTransformPrepared(handle, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException("Transform prepared fallita: " + safeLastError(handle));
        }
        return out;
    }

    // opzionali, solo debug / uso saltuario
    public synchronized double[] fromCsToCs(String sourceCrs, String targetCrs, double x, double y, double z) {
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

    public synchronized double[] fromParameters(String sourceParams, String targetParams, double x, double y, double z) {
        checkInit();
        double[] out = nativeFromParameters(handle, sourceParams, targetParams, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException("Trasformazione params->params fallita: " + safeLastError(handle));
        }
        return out;
    }

    /**
     * Nuovo: one-shot pipeline esplicita.
     */
    public synchronized double[] fromPipeline(String pipeline, double x, double y, double z) {
        checkInit();
        double[] out = nativeFromPipeline(handle, pipeline, x, y, z);
        if (out == null || out.length < 3) {
            throw new RuntimeException("Trasformazione pipeline fallita: " + safeLastError(handle));
        }
        return out;
    }

    public synchronized void clearPrepared() {
        checkInit();
        nativeClearPrepared(handle);
    }

    public synchronized String getLastError() {
        if (handle == 0) return safeLastError(null);
        String s = nativeGetLastError(handle);
        return (s == null || s.trim().isEmpty()) ? "(nessun dettaglio)" : s;
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
            throw new IllegalStateException("NativeProjTransformer non inizializzato");
        }
    }

    private String safeLastError(Long h) {
        try {
            String s = (h == null || h == 0) ? nativeGetLastError(0) : nativeGetLastError(h);
            return (s == null || s.trim().isEmpty()) ? "(nessun dettaglio)" : s;
        } catch (Throwable t) {
            return "(errore nativo non disponibile)";
        }
    }

    private static native long nativeInit(String projDataDir);

    private static native boolean nativeInitCsToCs(long handle, String sourceCrs, String targetCrs);
    private static native boolean nativeInitFromParameters(long handle, String sourceParams, String targetParams);

    // nuovi
    private static native boolean nativeInitPipeline(long handle, String pipeline);

    private static native double[] nativeTransformPrepared(long handle, double x, double y, double z);
    private static native void nativeClearPrepared(long handle);

    private static native double[] nativeFromCsToCs(long handle, String sourceCrs, String targetCrs,
                                                    double x, double y, double z);
    private static native double[] nativeFromParameters(long handle, String sourceParams, String targetParams,
                                                        double x, double y, double z);

    // nuovo
    private static native double[] nativeFromPipeline(long handle, String pipeline,
                                                      double x, double y, double z);

    private static native String nativeGetLastError(long handle);
    private static native void nativeClose(long handle);
}