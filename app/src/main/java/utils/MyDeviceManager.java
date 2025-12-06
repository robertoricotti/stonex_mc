package utils;


import static gui.MyApp.GEN1;
import static gui.MyApp.GEN2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.cp.cputils.shellcommand.CpCmd;
import com.cpdevice.cpcomm.frame.ICPCanFrame;
import com.van.jni.VanCmd;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import packexcalib.exca.DataSaved;


public class MyDeviceManager {
    public static final String ACTION_HIDE_NAVIGATION = "action.ACTION_HIDE_NAVIGATION";
    public static final String ACTION_SHOW_NAVIGATION = "action.ACTION_SHOW_NAVIGATION";
    public static final String GO_LANSCAPE = "com.cpdevice.hvscreen.landscape";
    public static final String GO_PORTRAIT = "com.cpdevice.hvscreen.portrait";

    public MyDeviceManager() {

    }


    public static void hideBar(Context context) {

        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
            case "TANK2_7_10":

                Intent intent = new Intent(ACTION_HIDE_NAVIGATION);
                context.sendBroadcast(intent);

                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,-60,0,-60", 10);
                break;
            case "MEGA_1":
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());
                    // 🔴 DISABILITA SYSTEMUI (barra di stato + nav bar)
                    os.writeBytes("pm disable-user --user 0 com.android.systemui\n");
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

    public static void showBar(Context context) {
        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
            case "TANK2_7_10":
                Intent intent = new Intent(ACTION_SHOW_NAVIGATION);
                context.sendBroadcast(intent);
                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,0,0,0", 10);
                break;
            case "MEGA_1":
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());
                    // 🔴 ABILITA SYSTEMUI (barra di stato + nav bar)
                    os.writeBytes("pm enable com.android.systemui\n");
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

    public static void setSize(Activity activity) {
        String s0 = "";
        String s1 = "";
        String s2 = "";
        switch (Build.BRAND) {
            case "APOLLO2_10":
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());

                    // ✅ DENSITY SMALL
                    os.writeBytes("wm density 204\n");

                    // ✅ FONT SMALL
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

                    // ✅ USB HOST
                    //os.writeBytes("echo host > /sys/devices/platform/fd5d0000.syscon/fd5d0000.syscon:usb2-phy@0/otg_mode\n");

                    // ✅ DISABILITA ADC-KEYS (Volume, Back, Menu, F21)
                    os.writeBytes("echo 1 > /sys/devices/platform/adc-keys/input/input7/inhibited\n");

                    // ✅ DENSITY SMALL
                    os.writeBytes("wm density 170\n");

                    // ✅ FONT SMALL
                    os.writeBytes("settings put system font_scale 0.85\n");

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
                    Apollo2.getInstance(activity).setOutput1(0);

                }
                break;

            case 1:
                if (DataSaved.enOUT == 1) {
                    if (GEN1) {
                        VanCmd.exec("echo \"100007\" >/dev/gpio_dev", 0);//out 2 =OFF

                    } else if (GEN2) {
                        Apollo2.getInstance(activity).setOutput1(1);
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
                    Apollo2.getInstance(activity).setOutput2(0);

                }
                break;

            case 1:
                if (DataSaved.enOUT == 1) {
                    if (GEN1) {
                        VanCmd.exec("echo \"100009\" >/dev/gpio_dev", 0);
                    } else if (GEN2) {
                        Apollo2.getInstance(activity).setOutput2(1);
                    }
                }
                break;

        }
    }

    public static void host(Activity activity) {

        Apollo2 apollo2 = Apollo2.getInstance(activity);
        apollo2.setUsbHost(1);

        if(Build.BRAND.equals("MEGA_1")){
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());

                // ✅ USB HOST
                os.writeBytes("echo host > /sys/devices/platform/fd5d0000.syscon/fd5d0000.syscon:usb2-phy@0/otg_mode\n");
                os.writeBytes("sync\n");
                os.writeBytes("exit\n");

                os.flush();
                su.waitFor();
            } catch (Exception e){
                Log.e("setHost",Log.getStackTraceString(e));
            }
        }


    }

    public static void periph(Activity activity) {
/*
        if (GEN1) {
            ApolloPro.getInstance(activity).setUsbHost(0);
        } else if (GEN2) {
            Apollo2.getInstance(activity).setUsbHost(0);
        }

*/
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

    public static void changOR(Activity activity, int i) {
        if (i > 0) {
            Intent intent1 = new Intent(GO_PORTRAIT);
            activity.sendBroadcast(intent1);
        } else {
            Intent intent2 = new Intent(GO_LANSCAPE);
            activity.sendBroadcast(intent2);
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

    public static String getDeviceSN(Activity activity) {

        if (GEN1) {
            return ApolloPro.getInstance(activity).getDeviceSN().toUpperCase();
        } else if (GEN2) {

            return Apollo2.getInstance(activity).getDeviceSN().toUpperCase();

        }
        return "STX_UNKNOWN";
    }

    public static String getSerial() {//android 6 ,9 getsn
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getSN", Log.getStackTraceString(e));

        }
        // Log.d(TAG, "++sdk+" + Build.VERSION.SDK_INT + "--sn--" + serial);
        return serial;//Build.SERIAL
    }


}
