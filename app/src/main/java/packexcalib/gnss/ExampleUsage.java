package packexcalib.gnss;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Esempio d'uso: carica il file una sola volta e poi richiama toLocalFast a 20Hz senza allocazioni.
 */
public class ExampleUsage {
    public static void main(String[] args) throws Exception {
        LocalizationModel model = LocalizationFactory.fromFile(new File("/mnt/data/ragusa.SP"));

        // array riutilizzabile da passare ad ogni chiamata per evitare allocazioni
        final double[] out = new double[3];

        // simuliamo ricezione GNSS a 20Hz
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            // esempio: lat/lon/h
            double lat = 37.089; double lon = 14.895; double h = 20.0;
            model.toLocalFast(lat, lon, h, out);
            Log.d("Test_CRSSP","Local: X=%.3f Y=%.3f Z=%.3f%n"+"   "+  out[0]+"   "+ out[1]+"   "+ out[2]);
        };
        ex.scheduleAtFixedRate(task, 0, 50, TimeUnit.MILLISECONDS); // 20Hz

        // per demo: stop dopo 5s
        Thread.sleep(5000);
        ex.shutdownNow();
    }
}
