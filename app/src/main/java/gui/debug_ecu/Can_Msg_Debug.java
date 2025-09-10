package gui.debug_ecu;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import drill_pile.gui.Drill_MainPage;
import event_bus.CanEvents;
import gui.gps.Nuovo_Gps;
import gui.tech_menu.ExcavatorChooserActivity;

public class Can_Msg_Debug extends AppCompatActivity {
    int Can_Nr;
    TextView title;
    ImageView back, playpause, clear;
    private ListView listViewC;
    private ArrayAdapter<String> adapterC;
    private ArrayList<String> itemListC;
    private boolean b_playC = false;
    CheckBox extended, fixedP;
    String chi = "";
    private boolean lastFixedMode = false;
    private Map<Integer, Integer> idPositionMap = new HashMap<>();

    private final Map<Integer, Long> lastNanoTimestamps = new HashMap<>();

    // opzionale: media esponenziale sul rate per renderlo meno "saltellante"
    private final Map<Integer, Double> avgRateMs = new HashMap<>();
    private static final double RATE_ALPHA = 0.35; // 0..1, più piccolo = più smoothing
    String newEntry="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_msg_debug);
        b_playC = true;
        Can_Nr = 1;
        findView();
        onClick();
        updateC();
        Intent intent = getIntent();
        chi = intent.getStringExtra("chi");
    }

    private void findView() {
        back = findViewById(R.id.back);
        playpause = findViewById(R.id.playPause);
        clear = findViewById(R.id.clearCan);
        listViewC = findViewById(R.id.listView_CAN);
        itemListC = new ArrayList<>();
        adapterC = new ArrayAdapter<>(this, R.layout.layout_custom_spinner_2, itemListC);
        listViewC.setAdapter(adapterC);
        extended = findViewById(R.id.extendedFrame);
        fixedP = findViewById(R.id.fixedPos);
        title = findViewById(R.id.title);



    }
    public void updateUI(){
        if (Can_Nr == 1) {
            title.setBackgroundColor(getResources().getColor(R.color._____cancel_text));
        } else {
            title.setBackgroundColor(getResources().getColor(R.color.blue));
        }

    }


    private void onClick() {
        back.setOnClickListener(view -> {
            back.setEnabled(false);
            switch (chi) {
                case "gps":
                    startActivity(new Intent(this, Nuovo_Gps.class));
                    break;

                case "drill_main":
                    startActivity(new Intent(this, Drill_MainPage.class));
                    break;
                case "menu":
                    startActivity(new Intent(this, ExcavatorChooserActivity.class));
                    break;
                default:
                    startActivity(new Intent(this, ExcavatorChooserActivity.class));

                    break;
            }

            finish();
        });
        playpause.setOnClickListener(view -> {
            b_playC = !b_playC;
            updateC();
        });
        clear.setOnClickListener(view -> {
            if(fixedP.isChecked()){
                clearFixedList();
            }else {
                clearListC();
            }

        });
        extended.setOnClickListener(view -> {
            if(fixedP.isChecked()){
                clearFixedList();
            }else {
                clearListC();
            }
        });
        fixedP.setOnClickListener(view -> {
            if(fixedP.isChecked()){
                clearFixedList();
            }else {
                clearListC();
            }
        });
        title.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Can_Msg_Debug.this);
            builder.setTitle("CAN SELECTION")
                    .setItems(new CharSequence[]{"CAN_1", "CAN_2"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // CAN_1
                                title.setText("CAN_1");
                                clear.callOnClick();
                                Can_Nr = 1;

                                break;
                            case 1: // CAN_2
                                title.setText("CAN_2");
                                clear.callOnClick();
                                Can_Nr = 2;

                                break;
                        }
                    });
            builder.show();
        });


    }
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void CanEvents(CanEvents canEvents) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SS", Locale.getDefault());
        String currentTimeString = timeFormat.format(new Date());
        boolean fixed = fixedP.isChecked();

        // calcola intervallo usando System.nanoTime per precisione
        long nowNano = System.nanoTime();
        long rateMs = 0L;
        double displayedRateMs = 0.0;

        if (lastNanoTimestamps.containsKey(canEvents.id)) {
            long lastNano = lastNanoTimestamps.get(canEvents.id);
            long diffNano = nowNano - lastNano;
            rateMs = diffNano / 1_000_000L; // converti in ms

            // opzionale: smoothing esponenziale per evitare salti grandi
            Double prevAvg = avgRateMs.get(canEvents.id);
            double smoothed = (prevAvg == null) ? rateMs : (RATE_ALPHA * rateMs + (1.0 - RATE_ALPHA) * prevAvg);
            avgRateMs.put(canEvents.id, smoothed);
            displayedRateMs = smoothed;
        } else {
            // primo messaggio per questo ID
            avgRateMs.put(canEvents.id, 0.0);
            displayedRateMs = 0.0;
        }
        lastNanoTimestamps.put(canEvents.id, nowNano);

        // gestione modalità fixed/reset come prima
        if (fixed != lastFixedMode) {
            itemListC.clear();
            idPositionMap.clear();
            adapterC.notifyDataSetChanged();
            lastFixedMode = fixed;
        }

        if ((Can_Nr == 1 && b_playC && canEvents.channel == 1) ||
                (Can_Nr == 2 && b_playC && canEvents.channel == 2)) {

            boolean isExtended = extended.isChecked();
            boolean validId = (isExtended && (canEvents.id > 2047 || canEvents.id < 0)) ||
                    (!isExtended && canEvents.id <= 2047);

            if (validId) {
                // prepara payload esadecimale e fissa la larghezza (24 char, adatta come preferisci)
                String payload = bytesToHex(canEvents.msg);
                payload = padOrTrim(payload, 24);

                // usa la rate arrotondata per la stampa
                long rateToShow = (long) (Math.ceil(displayedRateMs/10)*10);
                rateToShow = (rateToShow / 10) * 10;
                if(fixed) {
                    newEntry = String.format(
                            "%-12s CAN_%d  ID:0x%-6X  [%-1d]  %-24s  %5d ms",
                            currentTimeString,          // campo fisso 12 caratteri
                            canEvents.channel,          // numero canale
                            canEvents.id,               // ID esadecimale (X) allineato a sinistra
                            canEvents.dlc,              // DLC
                            payload,                    // payload già pad/troncato
                            rateToShow                  // rate in ms
                    );
                }else {
                    newEntry = currentTimeString + " CAN_" + canEvents.channel + " ID:0x" + String.format("%X", canEvents.id) +" ["+canEvents.dlc+"]"+ " " + bytesToHex(canEvents.msg);
                }

                if (fixed) {
                    if (idPositionMap.containsKey(canEvents.id)) {
                        int pos = idPositionMap.get(canEvents.id);
                        itemListC.set(pos, newEntry);
                    } else {
                        int insertPos = 0;
                        for (Integer existingId : idPositionMap.keySet()) {
                            if (canEvents.id > existingId) {
                                insertPos = idPositionMap.get(existingId) + 1;
                            }
                        }
                        itemListC.add(insertPos, newEntry);

                        // aggiorna la mappa con tutti gli indici (puoi ottimizzare evitando parse delle stringhe)
                        Map<Integer, Integer> newMap = new HashMap<>();
                        int index = 0;
                        for (String item : itemListC) {
                            String[] parts = item.split("ID:0x");
                            if (parts.length > 1) {
                                String hexId = parts[1].split(" ")[0];
                                int parsedId = Integer.parseInt(hexId, 16);
                                newMap.put(parsedId, index);
                            }
                            index++;
                        }
                        idPositionMap = newMap;
                    }
                    adapterC.notifyDataSetChanged();
                } else {
                    // modalità normale → append e scroll
                    itemListC.add(newEntry);
                    adapterC.notifyDataSetChanged();
                    listViewC.setSelection(itemListC.size() - 1);
                    listViewC.smoothScrollToPosition(itemListC.size() - 1);

                    if (adapterC.getCount() > 500) {
                        clearListC();
                        idPositionMap.clear();
                    }
                }
            }
        }
    }


    private String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));  // %02X = due cifre esadecimali maiuscole
        }
        return sb.toString().trim();
    }

    private String padOrTrim(String s, int width) {
        if (s == null) s = "";
        if (s.length() > width) return s.substring(0, width);
        return String.format("%-" + width + "s", s);
    }

    public void clearListC() {
        adapterC.clear();

    }
    private void clearFixedList() {
        itemListC.clear();
        idPositionMap.clear();
        adapterC.notifyDataSetChanged();
    }

    private void updateC() {

        if (b_playC) {

            playpause.setImageResource(R.drawable.btn_pause);

        } else {
            playpause.setImageResource(R.drawable.btn_play);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        b_playC = false;
    }
}