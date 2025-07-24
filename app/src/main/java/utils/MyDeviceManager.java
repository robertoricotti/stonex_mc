package utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.cp.cputils.shellcommand.CpCmd;
import com.cpdevice.cpbase.CPSDKException;
import com.cpdevice.cpcomm.frame.ICPCanFrame;
import com.van.jni.VanCmd;


public class MyDeviceManager {
    public static final String ACTION_HIDE_NAVIGATION = "action.ACTION_HIDE_NAVIGATION";
    public static final String ACTION_SHOW_NAVIGATION = "action.ACTION_SHOW_NAVIGATION";
    public static final String GO_LANSCAPE = "com.cpdevice.hvscreen.landscape";
    public static final String GO_PORTRAIT = "com.cpdevice.hvscreen.portrait";
    public static final boolean GEN1 = Build.BRAND.equals("SRT8PROS") || Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("qti");
    public static final boolean GEN2 = Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS");

    public MyDeviceManager() {

    }

    public static void hideBar(Context context) {

        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
                Intent intent = new Intent(ACTION_HIDE_NAVIGATION);
                context.sendBroadcast(intent);

                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,-60,0,-60", 10);
                break;
        }

    }

    public static void showBar(Context context) {
        switch (Build.BRAND) {
            case "APOLLO2_7":
            case "APOLLO2_10":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
                Intent intent = new Intent(ACTION_SHOW_NAVIGATION);
                context.sendBroadcast(intent);
                break;
            case "SRT8PROS":
            case "SRT7PROS":
            case "qti":
                VanCmd.exec("wm overscan 0,0,0,0", 10);
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
    public static void setSize(Activity activity){
        String s="";
        String s2="";
        if(Build.BRAND.equals("APOLLO2_10")){
            s="wm density 204";
            s2="settings put system font_scale 0.85";
            Apollo2 apollo2 = Apollo2.getInstance(activity);
            apollo2.exec(s);
            apollo2.exec(s2);
            try {
                new CpCmd().exceCmd("settings put system font_scale 0.85");
                new CpCmd().exceCmd("wm density 204");
            } catch (Exception ignored) {

            }


        }else if(Build.BRAND.equals("SRT8PROS")){
            s="wm density 200";
            s2="settings put system font_scale 1.0";
            VanCmd.exec(s, 0);
            VanCmd.exec(s2, 0);
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
                if (GEN1) {
                    VanCmd.exec("echo \"100007\" >/dev/gpio_dev", 0);//out 2 =OFF
                } else if (GEN2) {
                    Apollo2.getInstance(activity).setOutput1(1);
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
                if (GEN1) {
                    VanCmd.exec("echo \"100009\" >/dev/gpio_dev", 0);
                } else if (GEN2) {
                    Apollo2.getInstance(activity).setOutput2(1);
                }
                break;

        }
    }

    public static void host(Activity activity) {
/*
        if (GEN1) {
            ApolloPro.getInstance(activity).setUsbHost(1);
        } else if (GEN2) {
            Apollo2.getInstance(activity).setUsbHost(1);
        }
*/

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

    public static float getVoltage() {
        float result = 0;
        String s = Build.BRAND;
        switch (s) {
            case "APOLLO2_7":
            case "APOLLO2_12_PRO":
            case "APOLLO2_12_PLUS":
            case "APOLLO2_10":
                result = Float.parseFloat(CPCanHelper.voltApollo2);

                break;
            case "SRT8PROS":
            case "SRT7PROS":

                break;

        }



        return result;
    }

    public static void CanWrite(int channel, int id, int dlc, byte[] msg) {
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

        CPCanHelper.getInstance().canWrite(channel1, id, msg);


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

    public static String getMacAddress(Activity activity){
        if (GEN1) {
            return ApolloPro.getInstance(activity).getDeviceMacAddress();
        } else if (GEN2) {
            return Apollo2.getInstance(activity).getDeviceMacAddress();
        }
        return "";
    }
    public static String getDeviceSN(Activity activity){
        if (GEN1) {
            return ApolloPro.getInstance(activity).getDeviceSN().toUpperCase();
        } else if (GEN2) {
            return Apollo2.getInstance(activity).getDeviceSN().toUpperCase();
        }
        return "";
    }


}
