package gui.boot_and_choose;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.cp.cputils.Apollo2;
import com.van.jni.VanCmd;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import serial.OpenSerialPort;
import serial.SerialPortManager;
import services.CanSender;
import services.CanService;
import utils.ApkInstaller;
import utils.CPCanHelper;
import utils.MyDeviceManager;


public class AnyDeskLauncher {
    private Activity activity;
ApkInstaller apkInstaller;
    public AnyDeskLauncher(Activity activity) {
        this.activity = activity;
    }

    public void launchAnyDesk(int i) {
        String anyDeskPackageName = "com.anydesk.anydeskandroid";
        switch (i){
            case 0:
                //anydesk
                anyDeskPackageName = "com.anydesk.anydeskandroid";
                break;
            case 1:
                //teamviewer
                anyDeskPackageName = "com.teamviewer.quicksupport.market";
                break;
        }

        // Controlla se AnyDesk è installato
        if (isAppInstalled(anyDeskPackageName)) {
            Intent launchIntent = activity.getApplicationContext().getPackageManager().getLaunchIntentForPackage(anyDeskPackageName);
            if (launchIntent != null) {
                activity.startActivity(launchIntent);
                MyDeviceManager.showBar(activity);
                CPCanHelper.getInstance().disconnectAll();
                activity.stopService(new Intent(activity, CanService.class));
                activity.stopService(new Intent(activity, CanSender.class));
                OpenSerialPort.mOpened = false;
                SerialPortManager.instance().close();
                activity.finish();
                System.exit(0);
            } else {
                new CustomToast(activity, "Impossible to start\n"+anyDeskPackageName).show_alert();

            }
        } else {
            new CustomToast(activity, anyDeskPackageName+"\nNOT FOUND").show_error();
           // apkInstaller=new ApkInstaller(activity);
           // apkInstaller.installApkFromAssets(anyDeskPackageName+".apk");
        }
    }

    // Metodo per verificare se un'app è installata
    private boolean isAppInstalled(String packageName) {
        PackageManager packageManager = activity.getApplicationContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
