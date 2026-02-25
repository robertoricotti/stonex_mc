package packexcalib.gnss;
/*
import org.apache.sis.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CoordinateTransformer {

    private static volatile CoordinateTransformer INSTANCE;

    private static final String SOURCE_EPSG = "EPSG:4326";

    private final CoordinateReferenceSystem sourceCRS;
    private final Map<String, MathTransform> transformCache = new ConcurrentHashMap<>();

    // array riutilizzati (NO allocazioni)
    private final double[] src = new double[2];
    private final double[] dst = new double[2];

    private MathTransform currentTransform;

    private CoordinateTransformer() throws Exception {
        sourceCRS = CRS.forCode(SOURCE_EPSG);
    }

    public static CoordinateTransformer getInstance() {
        if (INSTANCE == null) {
            synchronized (CoordinateTransformer.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new CoordinateTransformer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    // =========================
    // INIT
    // =========================
    public void initWithEpsg(int epsg) {
        initWithEpsg("EPSG:" + epsg);
    }


    public void initWithEpsg(String epsg) {
        currentTransform = transformCache.computeIfAbsent(epsg, k -> {
            try {
                CoordinateReferenceSystem targetCRS = CRS.forCode(k);
                return CRS.findOperation(sourceCRS, targetCRS, null)
                        .getMathTransform();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void initWithWkt(String wkt) {
        String key = "WKT:" + wkt.hashCode();
        currentTransform = transformCache.computeIfAbsent(key, k -> {
            try {
                CoordinateReferenceSystem targetCRS = CRS.fromWKT(wkt);
                return CRS.findOperation(sourceCRS, targetCRS, null)
                        .getMathTransform();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // =========================
    // RUNTIME (20 Hz)
    // =========================
    public double getX(double lon, double lat) {
        transform(lon, lat);
        return dst[0];
    }

    public double getY(double lon, double lat) {
        transform(lon, lat);
        return dst[1];
    }

    private void transform(double lon, double lat) {
        if (currentTransform == null) {
            throw new IllegalStateException("Transformer not initialized");
        }

        try {
            src[0] = lon;
            src[1] = lat;
            currentTransform.transform(src, 0, dst, 0, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}*/