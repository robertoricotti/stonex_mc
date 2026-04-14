package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.List;

import gui.MyApp;
import serial.SerialPortManager;
import utils.FullscreenActivity;
import utils.MyDeviceManager;

public class CustomMenu {
    private Activity activity;
    private TextView title;
    private ListView listView;
    private String whoCall;
    Button close, resetFrq;
    private ArrayAdapter<String> listAdapter;

    private Dialog alertDialog;

    public CustomMenu(Activity activity, String titolo) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        listAdapter = new ArrayAdapter<>(activity, R.layout.simple_list_item);
        whoCall = titolo;
    }

    public void show(List<String> listItems, final OnItemSelectedListener itemSelectedListener) {
        alertDialog.create();
        if (whoCall.equals("RADIO CHANNEL")) {
            alertDialog.setContentView(R.layout.layout_custom_menu_radio);
        } else {
            alertDialog.setContentView(R.layout.layout_custom_menu);
        }
        alertDialog.setCancelable(false);

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        FullscreenActivity.setFullScreen(alertDialog);
        alertDialog.show();
        findView();
        setupListView(listItems, itemSelectedListener);
    }

    private void findView() {

        if (whoCall.equals("RADIO CHANNEL")) {
            resetFrq = alertDialog.findViewById(R.id.resetR);
            resetFrq.setOnClickListener(view -> {
                MyDeviceManager.CanWrite(true, 0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x01, (byte) 0xFF, (byte) 1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                try {
                    SerialPortManager.instance().sendCommand("set,radio.frequency,438.125|440.125|441.125|442.125|443.125|444.125|446.125|447.125\r\n");

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                new CustomToast(MyApp.visibleActivity, "FREQUENCY RESTORED TO DEFAULT").show_long();
                alertDialog.dismiss();
            });
        }
        title = alertDialog.findViewById(R.id.menuTitle);
        listView = alertDialog.findViewById(R.id.listView);
        close = alertDialog.findViewById(R.id.chiudi);
        title.setText(whoCall);
        close.setOnClickListener(view -> {
            alertDialog.dismiss();
        });
    }

    private void setupListView(List<String> listItems, final OnItemSelectedListener itemSelectedListener) {
        listView.setAdapter(listAdapter);
        listAdapter.clear();
        listAdapter.addAll(listItems);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                String selectedItem = listAdapter.getItem(position);
                itemSelectedListener.onItemSelected(selectedItem); // Restituisci il valore selezionato
                alertDialog.dismiss(); // Chiudi la dialog
            }
        });
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String selectedItem);
    }
}
