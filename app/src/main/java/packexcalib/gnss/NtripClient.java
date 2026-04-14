package packexcalib.gnss;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NtripClient {

    public interface MountpointsCallback {
        void onSuccess(List<String> mountpoints);

        void onError(Exception e);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void fetchMountpointsOnce(String host, int port, MountpointsCallback callback) {
        executor.execute(() -> {
            try {
                List<String> result = fetchMountpoints(host, port);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void release() {
        executor.shutdownNow();
    }

    private List<String> fetchMountpoints(String host, int port) throws Exception {

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        socket.setSoTimeout(5000);

        try (socket) {

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            String request =
                    "GET / HTTP/1.0\r\n" +
                            "User-Agent: NTRIP Android Client/1.0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";

            os.write(request.getBytes(StandardCharsets.US_ASCII));
            os.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.US_ASCII)
            );

            String line;
            Set<String> mountpoints = new LinkedHashSet<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("STR;")) {
                    String[] parts = line.split(";");
                    if (parts.length > 1) mountpoints.add(parts[1].trim());
                }
                if (line.contains("ENDSOURCETABLE")) break;
            }

            List<String> out = new ArrayList<>(mountpoints);
            Collections.sort(out);
            return out;
        }
    }
}