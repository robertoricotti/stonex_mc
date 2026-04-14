package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DrillService extends Service {
    public DrillService() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }
}