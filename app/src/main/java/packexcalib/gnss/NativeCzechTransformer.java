package packexcalib.gnss;

import android.content.Context;

import java.io.File;

public class NativeCzechTransformer {

//    static {
//        System.loadLibrary("sqlite3");
//        System.loadLibrary("proj");
//        System.loadLibrary("nativeczechproj");
//    }

    private long handle = 0;

    public void init(Context context) throws Exception {
        if (handle != 0) {
            close();
        }

        File projDir = ProjAssetUtils.copyProjDbIfNeeded(context);
        handle = nativeInit(projDir.getAbsolutePath());

        if (handle == 0) {
            throw new RuntimeException("Init PROJ fallita");
        }
    }

    public double[] wgs84To5514(double lon, double lat, double h) {
        checkInit();
        double[] out = nativeWgs84To5514(handle, lon, lat, h);
        if (out == null || out.length < 3) {
            throw new RuntimeException("Trasformazione 5514 fallita");
        }
        return out;
    }

    public double[] wgs84To5513(double lon, double lat, double h) {
        checkInit();
        double[] out = nativeWgs84To5513(handle, lon, lat, h);
        if (out == null || out.length < 3) {
            throw new RuntimeException("Trasformazione 5513 fallita");
        }
        return out;
    }

    public void close() {
        if (handle != 0) {
            nativeClose(handle);
            handle = 0;
        }
    }

    private void checkInit() {
        if (handle == 0) {
            throw new IllegalStateException("NativeCzechTransformer non inizializzato");
        }
    }

    private static native long nativeInit(String projDataDir);
    private static native double[] nativeWgs84To5514(long handle, double lon, double lat, double h);
    private static native double[] nativeWgs84To5513(long handle, double lon, double lat, double h);
    private static native void nativeClose(long handle);
}
