package gui.gps;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import gui.dialogs_and_toast.CustomToast;
import services.UpdateValuesService;
import utils.MyData;

public class BTPair extends AppCompatActivity {
    ImageView btn_exit, btn_search, btn_stop, img_cbt;

    BluetoothDeviceInfo selectedDeviceInfo;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDeviceInfo> deviceList;
    private ListView deviceListView;
    private ArrayAdapter<String> deviceListAdapter;
    int indexOfMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager windowManager = getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        float widthPercentage = 1f;
        float heightPercentage = 1f;

        int newWidth = (int) (screenWidth * widthPercentage);
        int newHeight = (int) (screenHeight * heightPercentage);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = newWidth; // Larghezza
        params.height = newHeight; // Altezza
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.activity_btpair);
        indexOfMachine = MyData.get_Int("MachineSelected");
        findView();
        onClick();

    }

    private void findView() {
        btn_exit = findViewById(R.id.backto);
        btn_search = findViewById(R.id.searchButton);
        btn_stop = findViewById(R.id.stopButton);


        // Inizializza l'adattatore Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth Supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Inizializza la lista dei dispositivi
        deviceList = new ArrayList<>();

        // Inizializza la ListView
        deviceListView = findViewById(R.id.deviceListView);
        deviceListAdapter = new ArrayAdapter<>(this, R.layout.layout_custom_spinner);
        deviceListView.setAdapter(deviceListAdapter);
        // Registra il BroadcastReceiver per ricevere gli aggiornamenti sullo stato dei dispositivi Bluetooth
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

    }

    private void disableAll() {
        btn_exit.setEnabled(false);
    }

    private void onClick() {


        btn_exit.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(BTPair.this, Nuovo_Gps.class));
            overridePendingTransition(0, 0);
            finish();
        });
        btn_search.setOnClickListener(view -> {
            new CustomToast(BTPair.this, "START SEARCH...").show();
            searchDevices();
        });
        btn_stop.setOnClickListener(view -> {
            stopSearch();
        });

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                selectedDeviceInfo = deviceList.get(position);
                if (!(selectedDeviceInfo.getDeviceName() == null)) {

                    MyData.push("M" + indexOfMachine + "_macaddress", selectedDeviceInfo.getDeviceAddress().toUpperCase());
                    MyData.push("M" + indexOfMachine + "_deviceName", selectedDeviceInfo.getDeviceName());
                    startService(new Intent(BTPair.this, UpdateValuesService.class));
                    new CustomToast(BTPair.this, selectedDeviceInfo.getDeviceAddress()).show();
                    pairWithDevice(selectedDeviceInfo.getDeviceAddress());
                    disableAll();
                    startActivity(new Intent(BTPair.this, Nuovo_Gps.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    new CustomToast(BTPair.this, "IMPOSSIBLE TO PAIR").show();
                }

            }
        });


    }


    @SuppressLint("MissingPermission")
    private void pairWithDevice(String deviceAddress) {
        // Get the BluetoothDevice object based on the device address
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        // Avvia il processo di abbinamento
        boolean pairingStarted = device.createBond();
        if (pairingStarted) {
            new CustomToast(BTPair.this, "PAIRING PROCESS STARTED WITH: " + device.getAddress()).show();
        } else {
            new CustomToast(BTPair.this, "MACADDRESS SAVED: " + device.getAddress()).show();
        }
    }


    @SuppressLint("MissingPermission")
    private void searchDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            // Se il Bluetooth non è abilitato, richiedi all'utente di abilitarlo
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, 1);


        } else {
            // Se il Bluetooth è abilitato, avvia la ricerca dei dispositivi
            deviceListAdapter.clear();
            deviceList.clear();
            bluetoothAdapter.startDiscovery();

        }
    }

    @SuppressLint("MissingPermission")
    private void stopSearch() {
        bluetoothAdapter.cancelDiscovery();
        new CustomToast(BTPair.this, "STOPPED..").show();


    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        // ...

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                @SuppressLint("MissingPermission") BluetoothDeviceInfo deviceInfo = new BluetoothDeviceInfo(device.getName(), device.getAddress());
                deviceList.add(deviceInfo);

                try {
                    // Update the list adapter with the new data
                    deviceListAdapter.add(deviceInfo.toString());
                } catch (Exception e) {
                    //do nothing
                }


            }
        }
        // ...
    };

    public class BluetoothDeviceInfo {
        private String deviceName;
        private String deviceAddress;

        public BluetoothDeviceInfo(String name, String address) {
            this.deviceName = name;
            this.deviceAddress = address;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getDeviceAddress() {
            return deviceAddress;
        }

        @Override
        public String toString() {
            return deviceName + "\n" + deviceAddress;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }
}
