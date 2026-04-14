package utils;


import static gui.MyApp.GEN1;
import static gui.MyApp.GEN2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.cp.cputils.shellcommand.CpCmd;
import com.cpdevice.cpcomm.frame.ICPCanFrame;
import com.van.jni.VanCmd;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Map;

import packexcalib.exca.DataSaved;


public class MyDeviceManager {
    public static final String ACTION_HIDE_NAVIGATION = "action.ACTION_HIDE_NAVIGATION";
    public static final String ACTION_SHOW_NAVIGATION = "action.ACTION_SHOW_NAVIGATION";
    public static final String GO_LANSCAPE = "com.cpdevice.hvscreen.landscape";
    public static final String GO_PORTRAIT = "com.cpdevice.hvscreen.portrait";

    public MyDeviceManager() {

    }

    public class UsbDebugUtils {

        private static final String TAG = "USB_SNAPSHOT";

        public static void logUsbDevices(Context context) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            if (usbManager == null) {
                Log.e(TAG, "UsbManager NULL");
                return;
            }

            Map<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Log.i(TAG, "==============================");
            Log.i(TAG, "USB DEVICE SNAPSHOT");
            Log.i(TAG, "Connected devices: " + deviceList.size());

            if (deviceList.isEmpty()) {
                Log.i(TAG, "No USB devices connected");
                Log.i(TAG, "==============================");
                return;
            }

            for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
                UsbDevice device = entry.getValue();

                Log.i(TAG, "------------------------------");
                Log.i(TAG, "Key: " + entry.getKey());
                Log.i(TAG, "DeviceName: " + device.getDeviceName());
                Log.i(TAG, "VendorId: " + device.getVendorId());
                Log.i(TAG, "ProductId: " + device.getProductId());
                Log.i(TAG, "Class: " + device.getDeviceClass());
                Log.i(TAG, "Subclass: " + device.getDeviceSubclass());
                Log.i(TAG, "Protocol: " + device.getDeviceProtocol());
                Log.i(TAG, "Interfaces: " + device.getInterfaceCount());

                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface usbInterface = device.getInterface(i);

                    Log.i(TAG, "  Interface #" + i);
                    Log.i(TAG, "    Class: " + usbInterface.getInterfaceClass());
                    Log.i(TAG, "    Subclass: " + usbInterface.getInterfaceSubclass());
                    Log.i(TAG, "    Protocol: " + usbInterface.getInterfaceProtocol());
                    Log.i(TAG, "    Endpoints: " + usbInterface.getEndpointCount());

                    for (int e = 0; e < usbInterface.getEndpointCount(); e++) {
                        UsbEndpoint ep = usbInterface.getEndpoint(e);
                        Log.i(TAG, "      Endpoint #" + e +
                                " Addr=" + ep.getAddress() +
                                " Dir=" + (ep.getDirection() == UsbConstants.USB_DIR_IN ? "IN" : "OUT") +
                                " Type=" + ep.getType() +
                                " MaxPacket=" + ep.getMaxPacketSize());
                    }
                }
            }

            Log.i(TAG, "==============================");
        }
    }

    public static void setLumen(float value) {
        if (value < 0.0f) value = 0.0f;
        if (value > 1.0f) value = 1.0f;
        int androidValue = (int) (value * 1023f);
        new Thread(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());
                // Disattiva luminosità automatica
                os.writeBytes("settings put system screen_brightness_mode 0\n");

                // Imposta luminosità in base al float
                os.writeBytes("settings put system screen_brightness " + androidValue + "\n");

                os.writeBytes("sync\n");
                os.writeBytes("exit\n");

                os.flush();
                su.waitFor();

            } catch (Exception e) {
                Log.e("BRIGHTNESS", Log.getStackTraceString(e));
            }
        }).start();


    }


    public static void hideBar(Context context) {

        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
            case "TANK2_7_10":


                Intent i = new Intent(ACTION_HIDE_NAVIGATION);
                context.sendBroadcast(i);

                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,-60,0,-60", 10);
                break;

            case "MEGA_1":
                setSystemBarsVisible(context, false);
                break;

        }

    }

    public static void showBar(Context context) {
        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
            case "TANK2_7_10":

                Intent i = new Intent(ACTION_SHOW_NAVIGATION);
                context.sendBroadcast(i);
                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,0,0,0", 10);
                break;

            case "MEGA_1":
                setSystemBarsVisible(context, true);
                break;

        }
    }

    public static String serialCom(int com) {
        String s = null;

        switch (com) {
            case 0:
                s = "CAN";
                break;
            case 1:
                if (GEN1) {
                    s = "/dev/ttyHSL0";
                } else if (GEN2) {
                    s = "/dev/ttyS3";
                }
                break;
            case 2:
                if (GEN1) {
                    s = "/dev/ttyHSL2";
                } else if (GEN2) {
                    s = "/dev/ttyS4";
                }
                break;
            case 3:
                if (GEN1) {
                    s = "/dev/ttyWK0";
                } else if (GEN2) {
                    s = "/dev/ttyS0";
                }
                break;
            case 4:
                s = "DEMO";
                break;

            case 5:
                s = "BT";
                break;

        }
        return s;
    }

    public static void setSize() {
        String s0 = "";
        String s1 = "";
        String s2 = "";
        switch (Build.BRAND) {
            case "APOLLO2_10":
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());

                    //  DENSITY SMALL
                    os.writeBytes("wm density 204\n");

                    //  FONT SMALL
                    os.writeBytes("settings put system font_scale 0.85\n");

                    os.writeBytes("sync\n");
                    os.writeBytes("exit\n");

                    os.flush();
                    su.waitFor();

                } catch (Exception e) {
                    Log.e("CPCMDKK", Log.getStackTraceString(e));
                }

                break;
            case "SRT8PROS":
                s1 = "wm density 200";
                s2 = "settings put system font_scale 1.0";
                VanCmd.exec(s1, 0);
                VanCmd.exec(s2, 0);
                break;
            case "MEGA_1":
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());

                    // Temp disable SELinux (serve per scrivere in /sys)
                    os.writeBytes("setenforce 0\n");

                    //Physical Buttons DISABLED
                    // os.writeBytes(
                    //     "sh -c 'for f in /sys/devices/platform/adc-keys/input/*/inhibited; do echo 1 > $f; done'\n"
                    // );

                    //  USB HOST
                    // os.writeBytes("echo host > /sys/devices/platform/fd5d0000.syscon/fd5d0000.syscon:usb2-phy@0/otg_mode\n");

                    //  DENSITY SMALL
                    os.writeBytes("wm density 170\n");

                    //  FONT SMALL
                    os.writeBytes("settings put system font_scale 1.0\n");


                    os.writeBytes("sync\n");
                    os.writeBytes("exit\n");

                    os.flush();
                    su.waitFor();

                } catch (Exception e) {
                    Log.e("CPCMDKK", Log.getStackTraceString(e));
                }
                break;
        }

    }


    public static void OUT1(Activity activity, int out) {

        switch (out) {
            case 0:
                if (GEN1) {
                    VanCmd.exec("echo \"100006\" >/dev/gpio_dev", 0);//out 2 =OFF

                } else if (GEN2) {
                    if (Build.BRAND.equals("MEGA_1")) {
                        try {
                            new CpCmd().exceCmd("echo 0 > /sys/class/gpio/gpio61/value");
                            Log.d("SetOUT", "Basso");
                        } catch (Exception e) {
                            Log.e("CPCMDKK", Log.getStackTraceString(e));
                        }
                    } else {
                        Apollo2.getInstance(activity).setOutput1(0);
                    }

                }
                break;

            case 1:
                if (DataSaved.enOUT == 1) {
                    if (GEN1) {
                        VanCmd.exec("echo \"100007\" >/dev/gpio_dev", 0);//out 2 =OFF

                    } else if (GEN2) {
                        if (Build.BRAND.equals("MEGA_1")) {
                            try {
                                new CpCmd().exceCmd("echo 1 > /sys/class/gpio/gpio61/value");
                                Log.d("SetOUT", "Alto");

                            } catch (Exception e) {
                                Log.e("CPCMDKK", Log.getStackTraceString(e));
                            }
                        } else {
                            Apollo2.getInstance(activity).setOutput1(1);
                        }
                    }
                }
                break;

        }
    }

    public static void OUT2(Activity activity, int out) {

        switch (out) {
            case 0:
                if (GEN1) {
                    VanCmd.exec("echo \"100008\" >/dev/gpio_dev", 0);
                } else if (GEN2) {
                    if (Build.BRAND.equals("MEGA_1")) {
                        try {

                            new CpCmd().exceCmd("echo 0 > /sys/class/gpio/gpio52/value");

                        } catch (Exception e) {
                            Log.e("CPCMDKK", Log.getStackTraceString(e));
                        }
                    } else {
                        Apollo2.getInstance(activity).setOutput2(0);
                    }

                }
                break;

            case 1:
                if (DataSaved.enOUT == 1) {
                    if (GEN1) {
                        VanCmd.exec("echo \"100009\" >/dev/gpio_dev", 0);
                    } else if (GEN2) {
                        if (Build.BRAND.equals("MEGA_1")) {
                            try {
                                new CpCmd().exceCmd("echo 1 > /sys/class/gpio/gpio52/value");

                            } catch (Exception e) {
                                Log.e("CPCMDKK", Log.getStackTraceString(e));
                            }
                        } else {
                            Apollo2.getInstance(activity).setOutput2(1);
                        }
                    }
                }
                break;

        }
    }


    public static void CanWrite(boolean send, int channel, int id, int dlc, byte[] msg) {
        ICPCanFrame.Channel channel1 = null;
        switch (channel) {
            case 0:
                channel1 = ICPCanFrame.Channel.CHN_1;
                break;
            case 1:
                channel1 = ICPCanFrame.Channel.CHN_2;
                break;
            default:
                channel1 = ICPCanFrame.Channel.CHN_1;

                break;

        }
        if (send) {
            CPCanHelper.getInstance().canWrite(channel1, id, msg);
        }


    }

    public static void WiFiEnable(Activity activity, int en_dis) {
        if (GEN1) {
            ApolloPro.getInstance(activity).setWifiSwitch(en_dis);
        } else if (GEN2) {
            Apollo2.getInstance(activity).setWifiSwitch(en_dis);
        }
    }


    public static String getBuildVersion(Activity activity) {
        if (GEN1) {

            return ApolloPro.getInstance(activity).getDeviceSystemVersion();
        } else if (GEN2) {

            return Apollo2.getInstance(activity).getSystemVersion();

        }
        return "";
    }

    public static String getMacAddress(Activity activity) {
        if (GEN1) {

            return ApolloPro.getInstance(activity).getDeviceMacAddress();
        } else if (GEN2) {

            return Apollo2.getInstance(activity).getDeviceMacAddress();

        }
        return "20:00:00:00:00:FF";
    }

    public static String getDeviceSN(Context context) {

        if (GEN1) {
            return ApolloPro.getInstance(context).getDeviceSN().toUpperCase();
        } else if (GEN2) {

            return Apollo2.getInstance(context).getDeviceSN().toUpperCase();

        }
        return "STX_UNKNOWN";
    }

    public static String getSerial() {//android 6 ,9 getsn
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
            if (serial != null) {
                serial = serial.toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getSN", Log.getStackTraceString(e));

        }
        // Log.d(TAG, "++sdk+" + Build.VERSION.SDK_INT + "--sn--" + serial);
        return serial;//Build.SERIAL
    }

    public static void setSystemBarsVisible(Context context, boolean visible) {
        try {
            // ===== STATUS BAR =====
            String[] statusBarActions = {
                    "com.android.action.STATUSBAR_SWITCH_STATE",
                    "com.android.systemui.action.STATUSBAR_SWITCH_STATE"
            };

            for (String action : statusBarActions) {
                Intent intent = new Intent();
                intent.setClassName(
                        "com.android.systemui",
                        "com.android.systemui.SystemUIControllerReceiver"
                );
                intent.setAction(action);
                intent.putExtra("enable", visible);
                context.sendBroadcast(intent);
            }

            // ===== NAVIGATION BAR =====
            String navAction = visible
                    ? "com.android.systemui.action.SHOW_NAVIGATION"
                    : "com.android.systemui.action.HIDE_NAVIGATION";

            Intent navIntent = new Intent();
            navIntent.setClassName(
                    "com.android.systemui",
                    "com.android.systemui.SystemUIControllerReceiver"
            );
            navIntent.setAction(navAction);
            context.sendBroadcast(navIntent);

        } catch (Exception e) {
            Log.e("SYSTEM_BARS", Log.getStackTraceString(e));
        }
    }


}
