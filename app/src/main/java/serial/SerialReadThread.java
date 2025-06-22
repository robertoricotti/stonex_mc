package serial;


import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import event_bus.CMD_Event;
import event_bus.SerialEvent;
import gui.MyApp;
import gui.debug_ecu.Serial_Msg_Debug;
import gui.gps.Nuovo_Gps;
import packexcalib.gnss.NmeaListener;


/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {
    public static boolean serialEmpty;
    Thread workerThread_Gnss;
    byte[] readBuffer_Gnss;
    final int[] readBufferPosition_Gnss = new int[1];
    final boolean[] stopWorker_Gnss = new boolean[1];


    private BufferedInputStream mmInputStream_Gnss;

    public SerialReadThread(InputStream is) {
        mmInputStream_Gnss = new BufferedInputStream(is);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        final int delimiter = 10;
        stopWorker_Gnss[0] = false;
        readBufferPosition_Gnss[0] = 0;
        readBuffer_Gnss = new byte[1024];
        workerThread_Gnss = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker_Gnss[0]) {
                    try {
                        int bytesAvailable = mmInputStream_Gnss.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream_Gnss.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition_Gnss[0]];

                                    System.arraycopy(readBuffer_Gnss, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                    readBufferPosition_Gnss[0] = 0;

                                    onDataReceive(data, bytesAvailable);

                                } else {
                                    try {
                                        readBuffer_Gnss[readBufferPosition_Gnss[0]++] = b;
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker_Gnss[0] = true;
                    }
                }
            }
        });
        workerThread_Gnss.start();
    }

    /**
     * Trattare i dati acquisiti
     *
     * @param received
     * @param size
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onDataReceive(String received, int size) {

        //String hexStr = ByteUtil.bytes2HexStr(received, 0, size);

        //Log.d("SerialPortManager", received);
        handler_tl.removeCallbacks(timeoutRunnable_tl);
        handler_tl.postDelayed(timeoutRunnable_tl, 3000);
        serialEmpty = false;
        NmeaListener.NmeaStandard(received);
       //Log.d("Programmo",received);
        if (MyApp.visibleActivity instanceof Nuovo_Gps || MyApp.visibleActivity instanceof Serial_Msg_Debug) {
            EventBus.getDefault().post(new CMD_Event(received));
            EventBus.getDefault().post(new SerialEvent(received));

        }


    }


    /**
     * 停止读线程
     */
    public void close() {

        try {
            mmInputStream_Gnss.close();
        } catch (IOException e) {
            //LogPlus.e("anormale", e);
        } finally {
            super.interrupt();
        }
    }

    private final Handler handler_tl = new Handler();
    private final Runnable timeoutRunnable_tl = new Runnable() {
        @Override
        public void run() {
            serialEmpty = true;


        }
    };
}