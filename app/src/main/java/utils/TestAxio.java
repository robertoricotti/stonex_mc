package utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import packexcalib.exca.PLC_DataTypes_LittleEndian;


public class TestAxio extends Activity {

    private static final String TAG = "AXIO_TEST";
    private static boolean faultErr = false;
    private static boolean isInit = false;
    private static int simulatedValue = 1000;
    private static int lastFV1uA = 4000; // DA FARE PUBBLICA STATICA --ultimo FV1 impostato =corrente minima ad input=0
    private ScheduledExecutorService scheduler;

    Button exit, piu, men, fault;
    TextView valore;

    static double scaleMaxValue = 1200.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_axio);

        exit = findViewById(R.id.exit);
        piu = findViewById(R.id.piu);
        men = findViewById(R.id.men);
        fault = findViewById(R.id.fault);
        valore = findViewById(R.id.valore);

        valore.setText(String.valueOf(simulatedValue));

        exit.setOnClickListener(v -> System.exit(0));//ammazza app IGNORARE

        piu.setOnClickListener(v -> {//simula incremento input
            simulatedValue += 10;
            valore.setText(String.valueOf(simulatedValue));
        });
        men.setOnClickListener(v -> {//simula decremento input
            simulatedValue -= 10;
            valore.setText(String.valueOf(simulatedValue));
        });

        fault.setOnClickListener(v -> faultErr = !faultErr);//simula flip flop fault e ok


        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (!isInit) {//config iniziale da inviare solo una volta
                    initialConfig();
                    isInit = true;
                }
                sendPosition(simulatedValue, faultErr);//invio ogni 100mS del valore di quota

                runOnUiThread(() -> {
                    valore.setText(String.valueOf(simulatedValue));
                    valore.setTextColor(faultErr ? Color.RED : Color.BLUE);
                });

            } catch (Exception e) {
                Log.e(TAG, "Errore nel ciclo CAN: ", e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduler != null) scheduler.shutdownNow();
    }

    // ===============================================================
    // INVIO POSIZIONE + GESTIONE STATO
    // ===============================================================
    public static void sendPosition(int position_mm, boolean fault) {

        if (fault) {
            // Fault ... FV1 = 0 microA (uscita 0 mA)
            setFV1(0);
            byte[] pv0 = PLC_DataTypes_LittleEndian.U32_to_bytes(0);
            MyDeviceManager.CanWrite(1, 0x27F, 4, pv0);
            Log.d(TAG, "FAULT - 0 mA");
            return;//esce dal metodo e non esegue il setFV1
        }

        if (position_mm < 0) {
            // Valore negativo ... FV1 = 3500 microA
            setFV1(3500);
            byte[] pv0 = PLC_DataTypes_LittleEndian.U32_to_bytes(0);
            MyDeviceManager.CanWrite(1, 0x27F, 4, pv0);
            Log.d(TAG, "NEGATIVO - 3.5 mA");
            return;//esce dal metodo e non esegue il setFV1
        }

        // In range → FV1 = 4000 microA
        setFV1(4000);

        // Invia valore RPDO- mandare quota
        byte[] pvBytes = PLC_DataTypes_LittleEndian.U32_to_bytes(position_mm);
        MyDeviceManager.CanWrite(1, 0x27F, 4, pvBytes);
        Log.d(TAG, "IN RANGE - PV=" + position_mm);
    }

    // ===============================================================
    // Cambia FV1 solo se necessario (per non saturare CAN)
    // ===============================================================
    private static void setFV1(int microAmps) {
        if (microAmps == lastFV1uA)
            return; // evita riscritture inutili se il valore minimo mA non è cambiato

        byte[] val = PLC_DataTypes_LittleEndian.U32_to_bytes(microAmps);
        MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x21, 0x73, 0x01, val[0], val[1], val[2], val[3]});

        lastFV1uA = microAmps;
        Log.d(TAG, "Set FV1 = " + microAmps + " microA");
    }

    // ===============================================================
    // CONFIGURAZIONE INIZIALE COMPLETA
    // ===============================================================
    public static void initialConfig() {
        Log.d(TAG, "Initial configuration starting...");
        lastFV1uA = 4000;
        int result = (int) Math.round(scaleMaxValue);
        byte[] data = PLC_DataTypes_LittleEndian.U32_to_bytes(result);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        int delay = 0; // millisecondi cumulativi

        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x000, 2,
                new byte[]{(byte) 0x80, 0x7F}), delay += 50, TimeUnit.MILLISECONDS); // NMT Pre-Operational

        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x2B, 0x10, 0x63, 0x01, 0x14, 0x00, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS); // Output 4–20mA

        // Scaling curve
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x20, 0x73, 0x01, 0x00, 0x00, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x21, 0x73, 0x01, (byte) 0xA0, 0x0F, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x22, 0x73, 0x01, data[0], data[1], data[2], data[3]}), delay += 50, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x23, 0x73, 0x01, 0x20, 0x4E, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);

        // Control via CANopen RPDO1
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x2F, 0x40, 0x23, 0x01, 0x01, 0x00, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x2F, 0x41, 0x23, 0x01, 0x01, 0x00, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);

        // Fault mode OFF
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x2F, 0x40, 0x63, 0x01, 0x00, 0x00, 0x00, 0x00}), delay += 50, TimeUnit.MILLISECONDS);

        // Save configuration
        scheduler.schedule(() -> MyDeviceManager.CanWrite(1, 0x67F, 8,
                new byte[]{0x23, 0x10, 0x10, 0x01, 0x73, 0x61, 0x76, 0x65}), delay += 50, TimeUnit.MILLISECONDS);

        // --- Wait 3 seconds, then Start Operational ---
        scheduler.schedule(() -> {
            MyDeviceManager.CanWrite(1, 0x000, 2, new byte[]{0x01, 0x7F});
            Log.d(TAG, "Initial configuration complete - scaleMaxValue=" + scaleMaxValue);

            scheduler.shutdown();
        }, delay + 3000, TimeUnit.MILLISECONDS);
    }



}
