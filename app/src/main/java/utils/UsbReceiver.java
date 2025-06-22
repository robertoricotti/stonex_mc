package utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;

public class UsbReceiver extends BroadcastReceiver {

    public static boolean usbIsConnected;
    private static final String TAG = "UsbReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                // Device is attached
                Log.d(TAG, "USB device attached: " + device.getDeviceName());
                usbIsConnected=true;
                //new CustomToast(MyApp.visibleActivity,"USB CONNESSO "+device.getDeviceName()).show_long();
                // Handle the attached device
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                // Device is detached
                Log.d(TAG, "USB device detached: " + device.getDeviceName());
                usbIsConnected=false;
                //new CustomToast(MyApp.visibleActivity,"USB RILEVATA "+device.getDeviceName()).show_long();
                // Handle the detached device
            }
        }
    }
}