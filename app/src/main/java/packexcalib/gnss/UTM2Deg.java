package packexcalib.gnss;

import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.Deg2UTM.nativeProjReady;
import static packexcalib.gnss.Deg2UTM.nativeProjTransformerToGeo;

import services.ReadProjectService;

public class UTM2Deg {
    static double[] out = new double[4];

    public static double[] toGeo(double e, double n, double z, String crs) {

        switch (crs) {
            case _LOCAL_COORDINATES_FROM_GNSS:
                return new double[]{0, 0, z};

            case _NONE:
                ReadProjectService.model.toGeoFast(e, n, z, out);
                return new double[]{out[0], out[1], out[2]};

            case "150582": {
                ensureNativeProjReady();
                double[] g = nativeProjTransformerToGeo.transformPrepared(-e, -n, z);
                return new double[]{g[1], g[0], g[2]};
            }

            case "150580":
                throw new UnsupportedOperationException("Inverse HEPOS grid shift non implementata");

            default: {
                ensureNativeProjReady();
                double[] g = nativeProjTransformerToGeo.transformPrepared(e, n, z);
                return new double[]{g[1], g[0], g[2]};
            }
        }
    }

    private static void ensureNativeProjReady() {
        if (!nativeProjReady) {
            throw new IllegalStateException("NativeProjTransformer not init");
        }
    }
}
