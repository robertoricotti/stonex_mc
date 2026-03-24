package packexcalib.gnss;

import android.content.Context;
import java.io.File;

public class NativeCzechGridTransformer {

    static {
        System.loadLibrary("sqlite3");
        System.loadLibrary("proj");
        System.loadLibrary("nativeczechproj");
    }

    private long handle = 0;

    public void init(Context context) throws Exception {
        if (handle != 0) {
            close();
        }

        File projDir = ProjAssetUtils.copyProjAssetsIfNeeded(context);
        handle = nativeInit(projDir.getAbsolutePath());

        if (handle == 0) {
            throw new RuntimeException("Init NativeCzechGridTransformer fallita");
        }
    }

    public double[] wgs84To150581(double lon, double lat, double h) {
        checkInit();
        return nativeWgs84To150581(handle, lon, lat, h);
    }

    public double[] wgs84To150582(double lon, double lat, double h) {
        checkInit();
        return nativeWgs84To150582(handle, lon, lat, h);
    }

    public double[] wgs84GridOnlyQ3(double lon, double lat, double h) {
        checkInit();
        return nativeWgs84GridOnlyQ3(handle, lon, lat, h);
    }

    public double[] wgs84GridOnlyQ1(double lon, double lat, double h) {
        checkInit();
        return nativeWgs84GridOnlyQ1(handle, lon, lat, h);
    }

    public void close() {
        if (handle != 0) {
            nativeClose(handle);
            handle = 0;
        }
    }

    private void checkInit() {
        if (handle == 0) {
            throw new IllegalStateException("NativeCzechGridTransformer non inizializzato");
        }
    }

    private static native long nativeInit(String projDataDir);
    private static native double[] nativeWgs84To150581(long handle, double lon, double lat, double h);
    private static native double[] nativeWgs84To150582(long handle, double lon, double lat, double h);
    private static native double[] nativeWgs84GridOnlyQ3(long handle, double lon, double lat, double h);
    private static native double[] nativeWgs84GridOnlyQ1(long handle, double lon, double lat, double h);
    private static native void nativeClose(long handle);
}