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

        // se cambia modalità → reset lista e mappa
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
                String newEntry = currentTimeString + "  CAN_" + canEvents.channel +
                        "  ID:0x" + String.format("%X", canEvents.id) +"  ["+canEvents.dlc+"]"+
                        "  " + bytesToHex(canEvents.msg);

                if (fixed) {
                    if (idPositionMap.containsKey(canEvents.id)) {
                        // ID già esistente → aggiorna riga
                        int pos = idPositionMap.get(canEvents.id);
                        itemListC.set(pos, newEntry);
                    } else {
                        // nuovo ID → inserimento ordinato
                        int insertPos = 0;
                        for (Integer existingId : idPositionMap.keySet()) {
                            if (canEvents.id > existingId) {
                                insertPos = idPositionMap.get(existingId) + 1;
                            }
                        }
                        itemListC.add(insertPos, newEntry);

                        // aggiorna la mappa con tutti gli indici
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
                } else {
                    // modalità normale → append
                    itemListC.add(newEntry);

                    if (adapterC.getCount() > 500) {
                        clearListC();
                        idPositionMap.clear();
                    }

                    listViewC.smoothScrollToPosition(itemListC.size() - 1);
                    listViewC.setSelection(itemListC.size() - 1);
                }

                adapterC.notifyDataSetChanged();
            }
        }
    }
/*
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void CanEvents(CanEvents canEvents) {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SS", Locale.getDefault());
        String currentTimeString = timeFormat.format(new Date());
        boolean fixed = fixedP.isChecked(); // se true → posizione fissa per ogni ID

        // se cambia modalità → reset lista e mappa
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
                String newEntry = currentTimeString + "  CAN_" + canEvents.channel +
                        "  ID:0x" + String.format("%X", canEvents.id) +"  ["+canEvents.msg.length+"]"+
                        "  " + bytesToHex(canEvents.msg);

                if (fixed) {
                    // aggiornamento in posizione fissa
                    if (idPositionMap.containsKey(canEvents.id)) {
                        int pos = idPositionMap.get(canEvents.id);
                        itemListC.set(pos, newEntry);
                    } else {
                        // nuovo ID → aggiunta riga dedicata
                        itemListC.add(newEntry);
                        int pos = itemListC.size() - 1;
                        idPositionMap.put(canEvents.id, pos);
                    }
                } else {
                    // modalità normale → append
                    itemListC.add(newEntry);

                    if (adapterC.getCount() > 500) {
                        clearListC();
                        idPositionMap.clear(); // reset anche la mappa
                    }

                    listViewC.smoothScrollToPosition(itemListC.size() - 1);
                    listViewC.setSelection(itemListC.size() - 1);
                }

                adapterC.notifyDataSetChanged();
            }
        }
    }
*/
    private String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));  // %02X = due cifre esadecimali maiuscole
        }
        return sb.toString().trim();
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