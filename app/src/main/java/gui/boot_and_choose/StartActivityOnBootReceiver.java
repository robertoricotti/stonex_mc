package gui.boot_and_choose;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import utils.MyDeviceManager;


public class StartActivityOnBootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            MyDeviceManager.showBar(context);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (Build.BRAND.equals("TANK2_7_10")||Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("APOLLO2_12_PRO")
            ||Build.BRAND.equals("APOLLO2_12_PLUS")) {

                Intent star = new Intent(context, LaunchScreenActivity.class);
                star.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(star);
            }


        }
    }


}
