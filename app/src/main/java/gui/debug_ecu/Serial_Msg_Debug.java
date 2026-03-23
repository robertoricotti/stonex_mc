package gui.debug_ecu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import drill_pile.gui.Drill_MainPage;
import event_bus.SerialEvent;
import gui.BaseClass;
import gui.gps.Nuovo_Gps;
import gui.tech_menu.ExcavatorChooserActivity;
import gui.tech_menu.NetworkConfigSettings;
import packexcalib.exca.DataSaved;
import utils.MyDeviceManager;

public class Serial_Msg_Debug extends BaseClass {
    TextView title;
    ImageView back, playpause, clear;
    private ListView listViewC;
    private ArrayAdapter<String> adapterC;
    private ArrayList<String> itemListC;
    private boolean b_playC = false;
    String chi = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_msg_debug);
        b_playC=true;
        findView();
        onClick();
        updateC();
        Intent intent = getIntent();
        chi = intent.getStringExtra("chi");
        if(chi==null){
            chi="gps";
        }

    }

    private void findView() {
        title=findViewById(R.id.title);
        back = findViewById(R.id.back);
        playpause = findViewById(R.id.playPause);
        clear = findViewById(R.id.clearCan);
        listViewC = findViewById(R.id.listView_CAN);
        itemListC = new ArrayList<>();
        adapterC = new ArrayAdapter<>(this, R.layout.layout_custom_spinner_3, itemListC);
        listViewC.setAdapter(adapterC);
        title.setText(MyDeviceManager.serialCom(DataSaved.my_comPort)+"  "+"Debug");
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
                case "easy":
                    startActivity(new Intent(this, NetworkConfigSettings.class));
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

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void SerialEvent(SerialEvent serialEvent) {
        if (b_playC) {

            itemListC.add(serialEvent.nmeaData);
            adapterC.notifyDataSetChanged();
            listViewC.smoothScrollToPositionFromTop(0, itemListC.size() - 1);
            if (adapterC.getCount() > 100) {
                clearListC();
            }


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