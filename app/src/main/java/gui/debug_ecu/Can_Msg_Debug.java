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
import java.util.Locale;

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
            clearListC();
            updateC();
        });
        extended.setOnClickListener(view -> {
            clearListC();
        });
        fixedP.setOnClickListener(view -> {
            clearListC();
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

        if (Can_Nr == 1) {
            if (b_playC && canEvents.channel == 1) {

                if (extended.isChecked()) {
                    if (canEvents.id > 2047 || canEvents.id < 0) {

                        itemListC.add(currentTimeString + "  CAN_" + canEvents.channel + "    " + canEvents.candata);
                        adapterC.notifyDataSetChanged();
                        listViewC.smoothScrollToPosition(itemListC.size() - 1);
                        listViewC.setSelection(itemListC.size() - 1);
                        if (adapterC.getCount() > 500) {
                            clearListC();
                        }
                    }
                } else {
                    if (canEvents.id <= 2047) {
                        itemListC.add(currentTimeString + "  CAN_" + canEvents.channel + "    " + canEvents.candata);
                        adapterC.notifyDataSetChanged();
                        listViewC.smoothScrollToPosition(itemListC.size() - 1);
                        listViewC.setSelection(itemListC.size() - 1);
                        if (adapterC.getCount() > 500) {
                            clearListC();
                        }
                    }
                }
            }
        } else if (Can_Nr == 2) {
            if (b_playC && canEvents.channel == 2) {

                if (extended.isChecked()) {
                    if (canEvents.id > 2047 || canEvents.id < 0) {

                        itemListC.add(currentTimeString + "  CAN_" + canEvents.channel + "    " + canEvents.candata);
                        adapterC.notifyDataSetChanged();
                        listViewC.smoothScrollToPosition(itemListC.size() - 1);
                        listViewC.setSelection(itemListC.size() - 1);
                        if (adapterC.getCount() > 500) {
                            clearListC();
                        }
                    }
                } else {
                    if (canEvents.id <= 2047) {
                        itemListC.add(currentTimeString + "  CAN_" + canEvents.channel + "    " + canEvents.candata);
                        adapterC.notifyDataSetChanged();
                        listViewC.smoothScrollToPosition(itemListC.size() - 1);
                        listViewC.setSelection(itemListC.size() - 1);
                        if (adapterC.getCount() > 500) {
                            clearListC();
                        }
                    }
                }
            }
        }

    }



    public void clearListC() {
        adapterC.clear();
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