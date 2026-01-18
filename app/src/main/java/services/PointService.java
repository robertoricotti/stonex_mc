package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.my_opengl.GLDrawer;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DrillLib;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.TriangleHelper;
import utils.DistToPoint;

public class PointService extends Service {
    private boolean isRunning = false;
    private ExecutorService executor;
    private static double[] lastPosition;
    TriangleHelper triangleHelper;
    public PointService() {
    }

    @Override
    public void onCreate(){
        Log.d("PointService","Created");
        triangleHelper = new TriangleHelper();
        lastPosition = new double[]{0, 0, 0};  // Posizione iniziale
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PointService","Started");
        if (!isRunning) {
            isRunning = true;
            executor.execute(pointRunnable);
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPointLoop();
        Log.d("PointService","Destroyed");
    }

    private void stopPointLoop() {
        isRunning = false;
        executor.shutdownNow();
    }






    private final Runnable pointRunnable = () -> {
        try{
            while (isRunning){
                long startTime = System.currentTimeMillis();
                Log.d("PointService","Running..");
                updateCurrentPosition(ExcavatorLib.toolEndCoord, DataSaved.RaggioDXF);
                /** do Running Stuff Here
                 *
                 *
                 */



                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = 100 - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(Math.abs(sleepTime));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        } catch (Exception ignored) {

        }
    };


    private void updateCurrentPosition(double[] position,double raggio){
        double r = raggio / 4;
        r = Math.min(r, 30);
        if (DistToPoint.dist2D(position, lastPosition) > r) {

            lastPosition = position;
            triangleHelper.updatePointRaius(lastPosition, DataSaved.RaggioDXF);

        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}