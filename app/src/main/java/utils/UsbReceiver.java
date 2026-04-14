package utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

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
                usbIsConnected = true;
                //new CustomToast(MyApp.visibleActivity,usbIsConnected+"   "+"USB device attached: " + device.getDeviceName()).show_error();

                // Handle the attached device
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                // Device is detached
                usbIsConnected = false;
                //new CustomToast(MyApp.visibleActivity,usbIsConnected+"   "+"USB device detached: " + device.getDeviceName()).show_error();

                // Handle the detached device
            }
        }
    }
}