package gui.debug_ecu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.stx_dig.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import event_bus.CanEvents;
import drill_pile.gui.Drill_MainPage;
import gui.gps.Nuovo_Gps;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;

public class Can_Msg_Debug extends AppCompatActivity {


    ImageView back,playpause,clear;
    private ListView  listViewC;
    private ArrayAdapter<String>  adapterC;
    private ArrayList<String>  itemListC;
    private boolean b_playC = false;
    CheckBox extended;
    String chi="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_msg_debug);
        b_playC=true;
        findView();
        onClick();
        updateC();
        Intent intent=getIntent();
        chi=intent.getStringExtra("chi");
    }
    private void findView(){
        back=findViewById(R.id.back);
        playpause=findViewById(R.id.playPause);
        clear=findViewById(R.id.clearCan);
        listViewC = findViewById(R.id.listView_CAN);
        itemListC = new ArrayList<>();
        adapterC = new ArrayAdapter<>(this,  R.layout.layout_custom_spinner_2 , itemListC);
        listViewC.setAdapter(adapterC);
        extended=findViewById(R.id.fixedPos);
    }

    private void onClick(){
        back.setOnClickListener(view -> {
            back.setEnabled(false);
            switch (chi){
                case "gps":
                    startActivity(new Intent(this, Nuovo_Gps.class));
                    break;

                case "drill_main":
                    startActivity(new Intent(this,Drill_MainPage.class));
                    break;
                default:
                    if(DataSaved.isWL<2) {
                        startActivity(new Intent(this, DebugExcavatorActivity.class));
                    }else {
                        startActivity(new Intent(this, ExcavatorChooserActivity.class));
                    }
                    break;
            }

            overridePendingTransition(0, 0);
            finish();
        });
        playpause.setOnClickListener(view ->{
            b_playC=!b_playC;
            updateC();
        });
        clear.setOnClickListener(view -> {
            clearListC();
            updateC();
        });
        extended.setOnClickListener(view -> {
            clearListC();
        });



    }



    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void CanEvents(CanEvents canEvents) {

        if (b_playC) {

            if(extended.isChecked()) {
                if(canEvents.id>2047||canEvents.id<0) {

                    itemListC.add("CAN_"+canEvents.channel+"    "+canEvents.candata);
                    adapterC.notifyDataSetChanged();
                    listViewC.smoothScrollToPositionFromTop(0, itemListC.size() - 1);
                    if (adapterC.getCount() > 100) {
                        clearListC();
                    }
                }
            }else {
                if(canEvents.id<=2047) {
                    itemListC.add("CAN_"+canEvents.channel+"    "+canEvents.candata);
                    adapterC.notifyDataSetChanged();
                    listViewC.smoothScrollToPositionFromTop(0, itemListC.size() - 1);
                    if (adapterC.getCount() > 100) {
                        clearListC();
                    }
                }
            }
            }

    }





    public void clearListC() {
        adapterC.clear();
    }
    private void updateC(){
        if(b_playC){

            playpause.setImageResource(R.drawable.btn_pause);

        }else {
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
        b_playC=false;
    }
}