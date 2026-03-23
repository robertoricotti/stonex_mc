package packexcalib.gnss;



import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import packexcalib.exca.DataSaved;
import serial.SerialPortManager;
import serial.SerialReadThread;
import utils.MyData;
import utils.MyDeviceManager;
import utils.MyTypes;

public class Difference {
    private final String TAG = "Difference_ZJL";
    private final mHandler handler;
    private Thread readThread;
    private String host;
    private int port;
    private String mountPoint;
    private String username;
    private String password;

    private Socket socket = null;
    private InputStream is = null;
    private OutputStream os = null;
    private final SerialReadThread mGps;
    private boolean isRunning = false;
    private boolean isStopping = false;


    public Difference(SerialReadThread gps) {
        mGps = gps;
        handler = new mHandler(this);
    }

    public boolean open() {
        Log.d(TAG, "OPEN DIFFERENCE");

        close();

        String enabled = MyData.get_String("ntripEnabled");
        if (!"ENABLED".equals(enabled)) {
            Log.d(TAG, "NTRIP DISABLED");
            return false;
        }

        host = MyData.get_String("ntripHostIp");
        port = MyData.get_Int("ntripPort");
        mountPoint = MyData.get_String("ntripMountPoint");
        username = MyData.get_String("ntripUsername");
        password = MyData.get_String("ntripPassword");

        if (host == null || host.isEmpty() || mountPoint == null || mountPoint.isEmpty() || port <= 0) {
            Log.e(TAG, "Configurazione NTRIP non valida");
            return false;
        }

        start();

        Log.d(TAG, "host: " + host +
                " port: " + port +
                " mountpoint: " + mountPoint +
                " username: " + username +
                " password: " + password);

        return true;
    }

    public void close() {
        isRunning = false;
        handler.removeMessages(mHandler.SOCKET_EXCEPTION);
        stop();

    }

    private void start() {
        readThread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "START DIFFERENCE");
                    isRunning = true;
                    Log.d(TAG, "START DIFFERENCE 1");
                    socket = new Socket(host, port);
                    Log.d(TAG, "START DIFFERENCE 2");
                    socket.setSoTimeout(10000);
                    Log.d(TAG, "START DIFFERENCE 3");
                    os = socket.getOutputStream();
                    Log.d(TAG, "START DIFFERENCE 4");
                    is = socket.getInputStream();
                    Log.d(TAG, "START DIFFERENCE 5");
                    String header = formatHeader(mountPoint, username, password);
                    Log.d(TAG, "START DIFFERENCE 6");
                    os.write(header.getBytes());
                    Log.d(TAG, "START DIFFERENCE 7");
                    os.flush();
                    Log.d(TAG, "START DIFFERENCE 8");
                    byte[] data = new byte[1024];
                    int length;
                    length = is.read(data);
                    if (length > 0) {
                        String result = new String(data, 0, length);
                        Log.d(TAG, "result = " + result);
                        if (result.contains("OK")) {
                            while (mGps.isOpen() && isRunning) {
                                try {
                                    length = is.read(data);
                                } catch (Exception e) {
                                    if (e instanceof SocketTimeoutException) {
                                        continue;
                                    } else {
                                        throw e;
                                    }
                                }
                                if (length > 0) {
                                    //mGps.onDifferentialData(data);
                                    Log.d(TAG, "SEND DIFFERENCE TO COM" + MyDeviceManager.serialCom(DataSaved.my_comPort) + Arrays.toString(data));
                                    SerialPortManager.instance().sendCommand(data);
                                } else if (-1 == length) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG,Log.getStackTraceString(e));
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignored) {
                        }

                        is = null;
                    }

                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ignored) {
                        }

                        os = null;
                    }

                    if (socket != null) {
                        try {
                            socket.shutdownInput();
                            socket.shutdownOutput();
                            socket.close();
                        } catch (IOException ignored) {
                        }
                        socket = null;
                    }
                }

                readThread = null;
                handler.sendEmptyMessageDelayed(mHandler.SOCKET_EXCEPTION, 3000);
            }
        };
        readThread.start();
    }

    private void stop() {
        try {
            if (os != null) {
                os.close();
                os = null;
            }

            if (is != null) {
                is.close();
                is = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = false;
        if (readThread != null && readThread.isAlive()) {
            try {
                readThread.join();
            } catch (InterruptedException ignored) {
            }
        }

        readThread = null;
    }

    public void writeGGA(byte[] data, int length) {
        if (os != null) {
            try {
                os.write(data, 0, length);
            } catch (IOException ignored) {
            }
        }
    }

    private String formatHeader(String mountPoint,
                                String username, String password) {
        StringBuffer sb = new StringBuffer();
        if (mountPoint == null) {
            sb.append("GET " + "/" + " HTTP/1.1\r\n");
        } else {
            sb.append("GET " + "/" + mountPoint + " HTTP/1.1\r\n");
        }

        sb.append("User-Agent:NTRIP UniNtrip/0.1\r\n");
        sb.append("Accept:*/*\r\n");
        if (username != null && password != null) {
            sb.append("Authorization: Basic "
                    + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP)
                    + "\r\n");
        }

        if (true) {
            sb.append("Connection: Keep-Alive\r\n");
        } else {
            sb.append("Connection: Close\r\n");
        }

        sb.append("\r\n");

        return sb.toString();
    }

    static class mHandler extends Handler {
        public static final int SOCKET_EXCEPTION = 0;

        private final WeakReference<Difference> mOuter;

        public mHandler(Difference obj) {
            mOuter = new WeakReference<>(obj);
        }

        @Override
        public void handleMessage(Message msg) {
            Difference obj = mOuter.get();

            if (null == obj) {
                return;
            }

            if (msg.what == SOCKET_EXCEPTION) {
                if (obj.mGps.isOpen() && null == obj.readThread) {
                    obj.start();
                }
            }
        }
    }
}
