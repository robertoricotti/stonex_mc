package services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.MyApp;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.MyData;

public class UpdateValuesService extends Service {
    public static CRSFactory crsFactory;
    public static CoordinateReferenceSystem WGS84, UTM;
    public static CoordinateTransformFactory ctFactory;
    public static CoordinateTransform wgsToUtm, utmToWgs;
    public static ProjCoordinate shifted,result, resultWgs;
    long startTime, stopTime;
    public static boolean startedService;
    public static boolean isUpodating;
    static int indexBucket = 0;
    public static boolean firstLaunch;
    private Executor mExecutor;
    private static final int THREAD_POOL_SIZE = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startTime = System.currentTimeMillis();

        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mExecutor.execute(new MyAsync_Excecutor());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MyData.get_Int("volumeAudioSystem") * 10, 0);
        } catch (NumberFormatException ignored) {
            MyData.push("volumeAudioSystem", "1");
        }

        UpdateValuesService.this.stopSelf();

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ExecutorService) mExecutor).shutdown();
        stopTime = System.currentTimeMillis();
        long result = Math.abs(stopTime - startTime);
    }

    private class MyAsync_Excecutor implements Runnable {
        @Override
        public void run() {
            try {
                isUpodating = true;
                String m = MyData.get_String("MachineSelected");
                if (m == null) {
                    MyData.push("MachineSelected", "1");
                    m = "1";
                }
                int indexMach = 0;
                try {
                    indexMach = Integer.parseInt(m);
                } catch (NumberFormatException e) {
                    indexMach = 1;
                }


                for (int i = 1; i <= 4; i++) {
                    String b = MyData.get_String("M" + i + "BucketSelected");
                    if (b == null) {
                        MyData.push("M" + i + "BucketSelected", "1");
                        b = "1";
                    }

                    try {
                        indexBucket = Integer.parseInt(MyData.get_String("M" + indexMach + "BucketSelected"));
                        Log.e("DataSaved.L_Bucket", MyData.get_String("M" + indexMach + "BucketSelected") + "  " + indexBucket);
                    } catch (NumberFormatException e) {
                        indexBucket = 1;

                    }
                    String nameM = MyData.get_String("M" + i + "_Name");
                    String reverseRoto = MyData.get_String("M" + i + "revTiltRot");
                    String lengthBoom1 = MyData.get_String("M" + i + "_LengthBoom1");
                    String offsetAngleBoom1 = MyData.get_String("M" + i + "_OffsetBoom1");
                    String mountPosBoom1 = MyData.get_String("M" + i + "_Boom1_MountPos");
                    String lengthBoom2 = MyData.get_String("M" + i + "_LengthBoom2");
                    String offsetAngleBoom2 = MyData.get_String("M" + i + "_OffsetBoom2");
                    String mountPosBoom2 = MyData.get_String("M" + i + "_Boom2_MountPos");
                    String lengthStick = MyData.get_String("M" + i + "_LengthStick");
                    String isExtension = MyData.get_String("M" + i + "_isExt");
                    String offsetAngleStick = MyData.get_String("M" + i + "_OffsetStick");
                    String mountPosStick = MyData.get_String("M" + i + "_Stick_MountPos");
                    String laserVStick = MyData.get_String("M" + i + "_LaserVStick");
                    String laserHStick = MyData.get_String("M" + i + "_LaserHStick");
                    String L1length = MyData.get_String("M" + i + "_LengthL1");
                    String L2length = MyData.get_String("M" + i + "_LengthL2");
                    String L3length = MyData.get_String("M" + i + "_LengthL3");
                    String bucketMountPos = MyData.get_String("M" + i + "_Bucket_MountPos");
                    String offsetDogBone = MyData.get_String("M" + i + "_OffsetDB");
                    String pitchLength = MyData.get_String("M" + i + "_LengthPitch");
                    String rollLength = MyData.get_String("M" + i + "_LengthRoll");
                    String mountPosFrame = MyData.get_String("M" + i + "_Frame_MountPos");
                    String offsetPitch = MyData.get_String("M" + i + "_OffsetFrameY");
                    String offsetRoll = MyData.get_String("M" + i + "_OffsetFrameX");
                    String gpsdeltaX = MyData.get_String("M" + i + "_OffsetGPSX");
                    String gpsdeltaY = MyData.get_String("M" + i + "_OffsetGPSY");
                    String gpsdeltaZ = MyData.get_String("M" + i + "_OffsetGPSZ");
                    String gps2Dev = MyData.get_String("M" + i + "_OffsetGPS2");
                    String distG1_G2 = MyData.get_String("M" + i + "_distG1_G2");
                    String useYawFrame = MyData.get_String("M" + i + "useYawFrame");
                    String useQuickSwitch = MyData.get_String("M" + i + "useQuickSwitch");
                    String Wheel_Steer_Rev = MyData.get_String("M" + i + "Wheel_Steer_Rev");
                    String Wheel_Steer_Min = MyData.get_String("M" + i + "Wheel_Steer_Min");
                    String Wheel_Steer_Med = MyData.get_String("M" + i + "Wheel_Steer_Med");
                    String Wheel_Steer_Max = MyData.get_String("M" + i + "Wheel_Steer_Max");
                    String Wheel_Steer_Range = MyData.get_String("M" + i + "Wheel_Steer_Range");

                    String swingLen = MyData.get_String("M" + i + "_Swing_Len");


                    String iswl = MyData.get_String("M" + i + "_isWL");
                    String enOUT = MyData.get_String("M" + i + "_enOUT");
                    String HighThreshold=MyData.get_String("M"+i+"HighThreshold");
                    String LowThreshold=MyData.get_String("M"+i+"LowThreshold");
                    String PassoGriglia=MyData.get_String("M"+i+"PassoGriglia");
                    String EnableMapping=MyData.get_String("M"+i+"EnableMapping");
                    String comPort = MyData.get_String("M" + i + "_comPort");
                    String sc600 = MyData.get_String("M" + i + "_sc600");
                    String reqSpeed = MyData.get_String("M" + i + "reqSpeed");
                    String radioMode = MyData.get_String("M" + i + "radioMode");
                    String priorityNet = MyData.get_String("M" + i + "priorityNet");
                    String useLowRes = MyData.get_String("M" + i + "_useLowRes");
                    String macaddress = MyData.get_String("M" + i + "_macaddress");
                    String macaddresscan = MyData.get_String("M" + i + "_macaddressCAN");
                    String deviceName = MyData.get_String("M" + i + "_deviceName");
                    String ebubbleX = MyData.get_String("M" + i + "_ebubbleX");
                    String ebubbleY = MyData.get_String("M" + i + "_ebubbleY");
                    String ebubbleDB = MyData.get_String("M" + i + "_ebubbleDB");
                    String ebubUsetilt = MyData.get_String("M" + i + "_ebubbleUseTilt");
                    String useCanOpen = MyData.get_String("M" + i + "_useCanOpen");
                    String hasQuick = MyData.get_String("M" + i + "_hasQuick");
                    String bladeW_L = MyData.get_String("M" + i + "_Bucket_" + "0" + "_Width_L");
                    String bladeW_R = MyData.get_String("M" + i + "_Bucket_" + "0" + "_Width_R");
                    String altPalo = MyData.get_String("M" + i + "_Bucket_" + "0" + "_Palo");
                    String distBetween = MyData.get_String("M" + i + "_Bucket_" + "0" + "_Between");
                    String altLama = MyData.get_String("M" + i + "_Bucket_" + "0" + "_Lama");
                    String larghezza_Carro = MyData.get_String("M" + i + "larghezza_Carro");
                    String lunghrzza_Carro = MyData.get_String("M" + i + "lunghrzza_Carro");
                    String larghezza_Frame = MyData.get_String("M" + i + "larghezza_Frame");
                    String lunghezza_Frame = MyData.get_String("M" + i + "lunghezza_Frame");
                    String larghezza_Braccio = MyData.get_String("M" + i + "larghezza_Braccio");

                    String usuraLamaSX = MyData.get_String("M" + i + "usuraLamaSX");
                    String usuraLamaCX = MyData.get_String("M" + i + "usuraLamaCX");
                    String usuraLamaDX = MyData.get_String("M" + i + "usuraLamaDX");

                    String Extra_Heading = MyData.get_String("M" + i + "Extra_Heading");


                    String Interface_Type = MyData.get_String("M" + i + "Interface_Type");
                    String minSpeedLeftUP = MyData.get_String("M" + i + "minSpeedLeftUP");
                    String maxSpeedLeftUP = MyData.get_String("M" + i + "maxSpeedLeftUP");
                    String minSpeedLeftDW = MyData.get_String("M" + i + "minSpeedLeftDW");
                    String maxSpeedLeftDW = MyData.get_String("M" + i + "maxSpeedLeftDW");

                    String minSpeedRightUP = MyData.get_String("M" + i + "minSpeedRightUP");
                    String maxSpeedRightUP = MyData.get_String("M" + i + "maxSpeedRightUP");
                    String minSpeedRightDW = MyData.get_String("M" + i + "minSpeedRightDW");
                    String maxSpeedRightDW = MyData.get_String("M" + i + "maxSpeedRightDW");
                    String minSpeedSS_A = MyData.get_String("M" + i + "minSpeedSS_A");
                    String maxSpeedSS_A = MyData.get_String("M" + i + "maxSpeedSS_A");
                    String minSpeedSS_B = MyData.get_String("M" + i + "minSpeedSS_B");
                    String maxSpeedSS_B = MyData.get_String("M" + i + "maxSpeedSS_B");
                    String CAT_Type = MyData.get_String("M" + i + "CAT_Type");

                    String GAIN_LEFT = MyData.get_String("M" + i + "GAIN_LEFT");
                    String GAIN_RIGHT = MyData.get_String("M" + i + "GAIN_RIGHT");
                    String HYDRAULIC_CONTROL_POINT_GRADER = MyData.get_String("M" + i + "HYDRAULIC_CONTROL_POINT_GRADER");
                    String HYDRAULIC_CONTROL_POINT_DOZER = MyData.get_String("M" + i + "HYDRAULIC_CONTROL_POINT_DOZER");


                    String colorBenna = MyData.get_String("M" + i + "coloreBenna");
                    String colorStick = MyData.get_String("M" + i + "coloreStick");

                    if (nameM == null) {
                        MyData.push("M" + i + "_Name", ("DEFAULT " + i).toUpperCase());
                    }
                    if (reverseRoto == null) {
                        MyData.push("M" + i + "revTiltRot", "0");
                    }
                    if (lengthBoom1 == null) {
                        MyData.push("M" + i + "_LengthBoom1", "-1");
                    }
                    if (offsetAngleBoom1 == null) {
                        MyData.push("M" + i + "_OffsetBoom1", "0.00");
                    }
                    if (mountPosBoom1 == null) {
                        MyData.push("M" + i + "_Boom1_MountPos", "0");
                    }
                    if (lengthBoom2 == null) {
                        MyData.push("M" + i + "_LengthBoom2", "0");
                    }
                    if (offsetAngleBoom2 == null) {
                        MyData.push("M" + i + "_OffsetBoom2", "0.00");
                    }
                    if (mountPosBoom2 == null) {
                        MyData.push("M" + i + "_Boom2_MountPos", "0");
                    }
                    if (lengthStick == null) {
                        MyData.push("M" + i + "_LengthStick", "0");
                    }
                    if (isExtension == null) {
                        MyData.push("M" + i + "_isExt", "0");
                    }
                    if (offsetAngleStick == null) {
                        MyData.push("M" + i + "_OffsetStick", "0.00");
                    }
                    if (mountPosStick == null) {
                        MyData.push("M" + i + "_Stick_MountPos", "0");
                    }
                    if (laserVStick == null) {
                        MyData.push("M" + i + "_LaserVStick", "0");
                    }
                    if (laserHStick == null) {
                        MyData.push("M" + i + "_LaserHStick", "0");
                    }
                    if (L1length == null) {
                        MyData.push("M" + i + "_LengthL1", "0");
                    }
                    if (L2length == null) {
                        MyData.push("M" + i + "_LengthL2", "0");
                    }
                    if (L3length == null) {
                        MyData.push("M" + i + "_LengthL3", "0");
                    }
                    if (bucketMountPos == null) {
                        MyData.push("M" + i + "_Bucket_MountPos", "0");
                    }
                    if (offsetDogBone == null) {
                        MyData.push("M" + i + "_OffsetDB", "0.00");
                    }
                    if (pitchLength == null) {
                        MyData.push("M" + i + "_LengthPitch", "0.2F");
                    }
                    if (rollLength == null) {
                        MyData.push("M" + i + "_LengthRoll", "0.2");
                    }
                    if (mountPosFrame == null) {
                        MyData.push("M" + i + "_Frame_MountPos", "0");
                    }
                    if (offsetPitch == null) {
                        MyData.push("M" + i + "_OffsetFrameY", "0.00");
                    }
                    if (offsetRoll == null) {
                        MyData.push("M" + i + "_OffsetFrameX", "0.00");
                    }

                    if (gpsdeltaX == null) {

                        MyData.push("M" + i + "f", "1.00");
                    }
                    if (gpsdeltaY == null) {
                        MyData.push("M" + i + "_OffsetGPSY", "0.50");
                    }
                    if (gpsdeltaZ == null) {
                        MyData.push("M" + i + "_OffsetGPSZ", "1.00");
                    }
                    if (gps2Dev == null) {
                        MyData.push("M" + i + "_OffsetGPS2", "-90.0");
                    }
                    if (distG1_G2 == null) {

                        MyData.push("M" + i + "_distG1_G2", "1.00");
                    }
                    if (useYawFrame == null) {
                        MyData.push("M" + i + "useYawFrame", "0");
                    }
                    if (useQuickSwitch == null) {
                        MyData.push("M" + i + "useQuickSwitch", "0");
                    }
                    if(Wheel_Steer_Rev== null){
                        MyData.push("M"+i+"Wheel_Steer_Rev","1");
                    }
                    if(Wheel_Steer_Min== null){
                        MyData.push("M"+i+"Wheel_Steer_Min","0");
                    }
                    if(Wheel_Steer_Med== null){
                        MyData.push("M"+i+"Wheel_Steer_Med","2500");
                    }
                    if(Wheel_Steer_Max== null){
                        MyData.push("M"+i+"Wheel_Steer_Max","5000");
                    }
                    if(Wheel_Steer_Range== null){
                        MyData.push("M"+i+"Wheel_Steer_Range","70.0");
                    }

                    if (swingLen == null) {
                        MyData.push("M" + i + "_Swing_Len", "0.0");
                    }

                    //draw
                    if (colorBenna == null) {
                        ;
                        MyData.push("M" + i + "coloreBenna", String.valueOf(ContextCompat.getColor(UpdateValuesService.this, R.color.bg)));
                    }
                    if (colorStick == null) {
                        MyData.push("M" + i + "coloreStick", String.valueOf(ContextCompat.getColor(UpdateValuesService.this, R.color.bg)));
                    }
                    if (larghezza_Carro == null) {
                        MyData.push("M" + i + "larghezza_Carro", "2.0");
                    }
                    if (lunghrzza_Carro == null) {
                        MyData.push("M" + i + "lunghrzza_Carro", "3.0");
                    }
                    if (larghezza_Frame == null) {
                        MyData.push("M" + i + "larghezza_Frame", "2.0");
                    }
                    if (lunghezza_Frame == null) {
                        MyData.push("M" + i + "lunghezza_Frame", "2.0");
                    }
                    if (larghezza_Braccio == null) {
                        MyData.push("M" + i + "larghezza_Braccio", "0.2");
                    }


                    if (iswl == null) {
                        if (i == 1 || i == 2) {
                            MyData.push("M" + i + "_isWL", "0");
                        } else if (i == 3) {
                            MyData.push("M" + i + "_isWL", "1");
                        } else if (i == 4) {
                            MyData.push("M" + i + "_isWL", "2");
                        }

                    }
                    //dragaggio
                    if (HighThreshold == null) {
                        MyData.push("M" + i + "HighThreshold", "0.5");
                    }
                    if (LowThreshold == null) {
                        MyData.push("M" + i + "LowThreshold", "0.5");
                    }
                    if (PassoGriglia == null) {
                        MyData.push("M" + i + "PassoGriglia", "1.0");
                    }
                    if (EnableMapping == null) {
                        MyData.push("M" + i + "EnableMapping", "0");
                    }

                    ///
                    if (enOUT == null) {
                        MyData.push("M" + i + "_enOUT", "0");
                    }
                    if (comPort == null) {
                        MyData.push("M" + i + "_comPort", "0");

                    }
                    if (sc600 == null) {
                        MyData.push("M" + i + "_sc600", "0");

                    }
                    if (reqSpeed == null) {
                        MyData.push("M" + i + "reqSpeed", "0");

                    }
                    if (radioMode == null) {
                        MyData.push("M" + i + "radioMode", "0");

                    }
                    if (priorityNet == null) {
                        MyData.push("M" + i + "priorityNet", "3");

                    }

                    if (useLowRes == null) {
                        MyData.push("M" + i + "_useLowRes", "0");

                    }

                    if (macaddress == null) {
                        MyData.push("M" + i + "_macaddress", "00:00:00:00:00:00");

                    }
                    if (macaddresscan == null) {
                        MyData.push("M" + i + "_macaddressCAN", "00:00:00:00:00:00");

                    }

                    if (deviceName == null) {
                        MyData.push("M" + i + "_deviceName", "GPS Name");

                    }

                    if (ebubbleX == null) {
                        MyData.push("M" + i + "_ebubbleX", "0");

                    }
                    if (ebubbleY == null) {
                        MyData.push("M" + i + "_ebubbleY", "0");

                    }
                    if (ebubbleDB == null) {
                        MyData.push("M" + i + "_ebubbleDB", "1");

                    }
                    if (ebubUsetilt == null) {
                        MyData.push("M" + i + "_ebubbleUseTilt", "1");

                    }

                    if (useCanOpen == null) {
                        MyData.push("M" + i + "_useCanOpen", "3");

                    }
                    if (hasQuick == null) {
                        MyData.push("M" + i + "_hasQuick", "0");

                    }
                    if (bladeW_R == null) {
                        MyData.push("M" + i + "_Bucket_" + "0" + "_Width_R", "1.25");

                    }
                    if (bladeW_L == null) {
                        MyData.push("M" + i + "_Bucket_" + "0" + "_Width_L", "1.25");

                    }
                    if (altPalo == null) {
                        MyData.push("M" + i + "_Bucket_" + "0" + "_Palo", "1.0");

                    }
                    if (distBetween == null) {
                        MyData.push("M" + i + "_Bucket_" + "0" + "_Between", "0.5");

                    }

                    if (altLama == null) {
                        MyData.push("M" + i + "_Bucket_" + "0" + "_Lama", "0.5");

                    }
                    if (usuraLamaSX == null) {
                        MyData.push("M" + i + "usuraLamaSX", "0.1");

                    }
                    if (usuraLamaCX == null) {
                        MyData.push("M" + i + "usuraLamaCX", "0.1");

                    }
                    if (usuraLamaDX == null) {
                        MyData.push("M" + i + "usuraLamaDX", "0.1");

                    }
                    if (Extra_Heading == null) {
                        MyData.push("M" + i + "Extra_Heading", "0");
                    }
                    if (Interface_Type == null) {
                        MyData.push("M" + i + "Interface_Type", "0");
                    }
                    ///


                    if (minSpeedLeftUP == null) {
                        MyData.push("M" + i + "minSpeedLeftUP", "0");
                    }
                    if (maxSpeedLeftUP == null) {
                        MyData.push("M" + i + "maxSpeedLeftUP", "150");
                    }
                    if (minSpeedLeftDW == null) {
                        MyData.push("M" + i + "minSpeedLeftDW", "0");
                    }
                    if (maxSpeedLeftDW == null) {
                        MyData.push("M" + i + "maxSpeedLeftDW", "150");
                    }


                    if (minSpeedRightUP == null) {
                        MyData.push("M" + i + "minSpeedRightUP", "0");
                    }
                    if (maxSpeedRightUP == null) {
                        MyData.push("M" + i + "maxSpeedRightUP", "150");
                    }
                    if (minSpeedRightDW == null) {
                        MyData.push("M" + i + "minSpeedRightDW", "0");
                    }
                    if (maxSpeedRightDW == null) {
                        MyData.push("M" + i + "maxSpeedRightDW", "150");
                    }


                    if (minSpeedSS_A == null) {
                        MyData.push("M" + i + "minSpeedSS_A", "0");
                    }
                    if (maxSpeedSS_A == null) {
                        MyData.push("M" + i + "maxSpeedSS_A", "150");
                    }
                    if (minSpeedSS_B == null) {
                        MyData.push("M" + i + "minSpeedSS_B", "0");
                    }
                    if (maxSpeedSS_B == null) {
                        MyData.push("M" + i + "maxSpeedSS_B", "150");
                    }

                    if (CAT_Type == null) {
                        MyData.push("M" + i + "CAT_Type", "0");
                    }

                    if (GAIN_LEFT == null) {
                        MyData.push("M" + i + "GAIN_LEFT", "1");
                    }
                    if (GAIN_RIGHT == null) {
                        MyData.push("M" + i + "GAIN_RIGHT", "1");
                    }
                    if (HYDRAULIC_CONTROL_POINT_GRADER == null) {
                        MyData.push("M" + i + "HYDRAULIC_CONTROL_POINT_GRADER", "2");
                    }
                    if (HYDRAULIC_CONTROL_POINT_DOZER == null) {
                        MyData.push("M" + i + "HYDRAULIC_CONTROL_POINT_DOZER", "0");
                    }


                    ///
                }

                for (int i = 1; i <= 4; i++) {
                    for (int j = 1; j <= 20; j++) {
                        String name = MyData.get_String("M" + i + "_Bucket_" + j + "_Name");
                        String length = MyData.get_String("M" + i + "_Bucket_" + j + "_Length");
                        String width = MyData.get_String("M" + i + "_Bucket_" + j + "_Width");
                        String L4 = MyData.get_String("M" + i + "_Bucket_" + j + "_L4");
                        String offset = MyData.get_String("M" + i + "_Bucket_" + j + "_Offset");
                        String flatOffset = MyData.get_String("M" + i + "_Bucket_" + j + "_Flat_Offset");
                        String flat = MyData.get_String("M" + i + "_Bucket_" + j + "_Flat");

                        String mountPosTilt = MyData.get_String("M" + i + "_Tilt_MountPos" + j);
                        String tiltLength = MyData.get_String("M" + i + "_Tilt_Length" + j);
                        String offsetLength = MyData.get_String("M" + i + "_Tilt_Offset" + j);
                        String piccolabucket = MyData.get_String("M" + i + "_Tilt_piccolaBucket" + j);
                        String offsettilA = MyData.get_String("M" + i + "_Tilt_Offset_Angle" + j);

                        String degwtilt = MyData.get_String("M" + i + "_Offset_DegWTilt" + j);


                        if (name == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Name", ("bucket" + j).toUpperCase());
                        }
                        if (length == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Length", "0");
                        }
                        if (width == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Width", "1");
                        }
                        if (L4 == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_L4", "1");
                        }
                        if (offset == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Offset", "0.00");
                        }
                        if (flatOffset == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Flat_Offset", "0.00");
                        }
                        if (flat == null) {
                            MyData.push("M" + i + "_Bucket_" + j + "_Flat", "0.00");
                        }
                        if (mountPosTilt == null) {
                            MyData.push("M" + i + "_Tilt_MountPos" + j, "0");
                        }
                        if (tiltLength == null) {
                            MyData.push("M" + i + "_Tilt_Length" + j, "0");
                        }
                        if (offsetLength == null) {
                            MyData.push("M" + i + "_Tilt_Offset" + j, "0");
                        }
                        if (piccolabucket == null) {
                            MyData.push("M" + i + "_Tilt_piccolaBucket" + j, "0");
                        }
                        if (offsettilA == null) {
                            MyData.push("M" + i + "_Tilt_Offset_Angle" + j, "0");
                        }

                        if (degwtilt == null) {
                            MyData.push("M" + i + "_Offset_DegWTilt" + j, "0");
                        }

                    }
                }
                String indexProfile = MyData.get_String("ProfileSelected");
                if (indexProfile == null) {
                    MyData.push("ProfileSelected", "0");
                }
                for (int i = 1; i <= 6; i++) {
                    String punti = MyData.get_String("Profile" + i + "_punti");

                    if (punti == null) {
                        MyData.push("Profile" + i + "_punti", "");
                    }
                    String name = MyData.get_String("Profile" + i + "_name");
                    if (name == null) {
                        MyData.push("Profile" + i + "_name", ("Profile" + i).toUpperCase());
                    }
                    String op = MyData.get_String("Profile" + i + "_OP");
                    if (op == null) {
                        MyData.push("Profile" + i + "_OP", "1");
                    }
                    String profilePage = MyData.get_String("Profile" + i + "_page");
                    if (profilePage == null) {
                        MyData.push("Profile" + i + "_page", "0");//0=picchettati 1=manuali
                    }
                }

                String licenza = MyData.get_String("licenza");
                String screenOr = MyData.get_String("screenOr");
                String wifiSSID = MyData.get_String("wifiSSID");
                String boomresult = MyData.get_String("boomresult");

                String indexAudioSystem = MyData.get_String("indexAudioSystem");
                String laserMode = MyData.get_String("LaserMode");
                String slopeY = MyData.get_String("SLOPE_Y");
                String slopeX = MyData.get_String("SLOPE_X");
                String operatorOffset = MyData.get_String("Operator_Offset");
                String offsetZero = MyData.get_String("Offset_Zero");
                String canBaud1 = MyData.get_String("canBaud1");
                String canBaud2 = MyData.get_String("canBaud2");

                String glScale = MyData.get_String("glScale");
                String glFace = MyData.get_String("glFace");


                String laser_height_Zero = MyData.get_String("Laser_Height_Zero");
                String laser_reach_Zero = MyData.get_String("Laser_Reach_Zero");
                String deadbandH = MyData.get_String("Deadband_H");
                String tolleranza_Z = MyData.get_String("tolleranza_Z");
                String HYDRAULIC_WINDOW = MyData.get_String("HYDRAULIC_WINDOW");
                String tolleranza_XY = MyData.get_String("tolleranza_XY");
                String tolleranza_Slope = MyData.get_String("tolleranza_Slope");
                String driftStep = MyData.get_String("driftStep");
                String driftSign = MyData.get_String("driftSign");
                String deadbandFA = MyData.get_String("Deadband_FlatAngle");
                String pivotHeightAlarm = MyData.get_String("Pivot_Height_Alarm");
                String unitOfMeasure = MyData.get_String("Unit_Of_Measure");
                String offsetHdt = MyData.get_String("Offset_Hdt");

                String volumeAudioSystem = MyData.get_String("volumeAudioSystem");
                String colorY = MyData.get_String("coloreY");
                String colorX = MyData.get_String("coloreX");

                String scaleFactor = MyData.get_String("scaleFactor");
                String scaleFactor3d = MyData.get_String("scaleFactor3D");
                String scaleFactorvista1D = MyData.get_String("scaleFactor_vista1D");
                String scaleFactorvista2D = MyData.get_String("scaleFactor_vista2D");
                String start2DX = MyData.get_String("start2DX");
                String start2DY = MyData.get_String("start2DY");
                String start2DZ = MyData.get_String("start2DZ");
                String offsetmDeltaX = MyData.get_String("offsetmDeltaX");
                String offsetmDeltaY = MyData.get_String("offsetmDeltaY");
                String projFlag = MyData.get_String("projectionFlag");
                String lan = MyData.get_String("language");
                String techInfo = MyData.get_String("techInfo");
                String UpperBar_Visible = MyData.get_String("UpperBar_Visible");
                String mybrightness = MyData.get_String("brightness");
                String colorMode = MyData.get_String("colorMode");
                String laserOn = MyData.get_String("laserOn");
                String cq3d = MyData.get_String("_cq3d");
                String arcodaMessageList = MyData.get_String("arcodaMessageList");
                String crs = MyData.get_String("crs");
                String xyz = MyData.get_String("xyz");
                String Colore_Surf = MyData.get_String("Colore_Surf");
                String Triangoli_Surf = MyData.get_String("Triangoli_Surf");
                String Punti_Surf = MyData.get_String("Punti_Surf");
                String Poly_Surf = MyData.get_String("Poly_Surf");
                String Mostra_Testo = MyData.get_String("Mostra_Testo");
                String ShowUtils = MyData.get_String("Mostra_Utils");
                String ShowJson = MyData.get_String("Mostra_Json");
                String Tema_SW = MyData.get_String("Tema_SW");
                String typeView = MyData.get_String("typeView");
                String gradientDB = MyData.get_String("gradientDB");
                String showAlign = MyData.get_String("showAlign");
                String line_Offset = MyData.get_String("line_Offset");
                String ckSchermo = MyData.get_String("ckSchermo");
                String drwaMachieSchema = MyData.get_String("drwaMachieSchema");
                String geoidPath = MyData.get_String("geoidPath");
                String lock3dRotation = MyData.get_String("lock3dRotation");
                String Off_Incr_Step = MyData.get_String("Off_Incr_Step");

                if (!startedService) {
                    if (licenza == null) {
                        MyData.push("licenza", "000000");
                    }
                    if (screenOr == null) {
                        MyData.push("screenOr", "0");
                    }
                    if (wifiSSID == null) {
                        MyData.push("wifiSSID", "SMCXXXXXXXXXX");
                    }
                    if (boomresult == null) {
                        MyData.push("boomresult", "0,0,0");
                    }

                    if (lan == null) {
                        MyData.push("language", "en_GB");
                    }

                    if (indexAudioSystem == null) {
                        MyData.push("indexAudioSystem", "0");
                    }
                    if (slopeY == null) {
                        MyData.push("SLOPE_Y", "0.0");
                    }
                    if (slopeX == null) {
                        MyData.push("SLOPE_X", "0.0");
                    }
                    if (operatorOffset == null) {
                        MyData.push("Operator_Offset", "0.00");
                    }
                    if (offsetZero == null) {
                        MyData.push("Offset_Zero", "0");
                    }
                    if (canBaud1 == null) {
                        MyData.push("canBaud1", "250000");
                    }
                    if (canBaud2 == null) {
                        MyData.push("canBaud2", "250000");
                    }
                    if (glScale == null) {
                        MyData.push("glScale", "0.7");
                    }
                    if (glFace == null) {
                        MyData.push("glFace", "true");
                    }

                    if (mybrightness == null) {
                        MyData.push("brightness", "1.0");
                    }

                    if (deadbandH == null) {
                        MyData.push("Deadband_H", "0.02");
                    }
                    if (tolleranza_Z == null) {
                        MyData.push("tolleranza_Z", "0.02");
                    }
                    if (HYDRAULIC_WINDOW == null) {
                        MyData.push("HYDRAULIC_WINDOW", "0.5");
                    }
                    if (tolleranza_XY == null) {
                        MyData.push("tolleranza_XY", "0.02");
                    }
                    if (tolleranza_Slope == null) {
                        MyData.push("tolleranza_Slope", "3.0");
                    }
                    if (driftStep == null) {
                        MyData.push("driftStep", "0");
                    }
                    if (driftSign == null) {
                        MyData.push("driftSign", "0");
                    }
                    if (deadbandFA == null) {
                        MyData.push("Deadband_FlatAngle", "5.0");
                    }
                    if (pivotHeightAlarm == null || pivotHeightAlarm.equals("")) {
                        MyData.push("Pivot_Height_Alarm", "10000000.0");
                    }
                    if (unitOfMeasure == null) {
                        MyData.push("Unit_Of_Measure", "1");
                    }
                    if (laser_height_Zero == null) {
                        MyData.push("Laser_Height_Zero", "0");
                    }
                    if (laser_reach_Zero == null) {
                        MyData.push("Laser_Reach_Zero", "0");
                    }
                    if (laserMode == null) {
                        MyData.push("LaserMode", "0");
                    }
                    if (offsetHdt == null) {
                        MyData.push("Offset_Hdt", "0");
                    }


                    if (volumeAudioSystem == null) {
                        MyData.push("volumeAudioSystem", "1");
                    }

                    if (colorY == null) {
                        MyData.push("coloreY", "0");
                    }
                    if (colorX == null) {
                        MyData.push("coloreX", "3");
                    }

                    if (scaleFactor == null) {
                        MyData.push("scaleFactor", "150");
                    }
                    if (scaleFactor3d == null) {
                        MyData.push("scaleFactor3D", "1");
                    }
                    ////

                    if (scaleFactorvista1D == null) {
                        MyData.push("scaleFactor_vista1D", "1");
                    }
                    if (scaleFactorvista2D == null) {
                        MyData.push("scaleFactor_vista2D", "1");
                    }


                    ////
                    if (start2DX == null) {
                        MyData.push("start2DX", "0");
                    }
                    if (start2DY == null) {
                        MyData.push("start2DY", "0");
                    }
                    if (start2DZ == null) {
                        MyData.push("start2DZ", "0");
                    }
                    if (offsetmDeltaX == null) {
                        MyData.push("offsetmDeltaX", "0");
                    }
                    if (offsetmDeltaY == null) {
                        MyData.push("offsetmDeltaY", "0");
                    }
                    if (projFlag == null) {
                        MyData.push("projectionFlag", "0");
                    }
                    if (techInfo == null) {
                        MyData.push("techInfo", getResources().getString(R.string.infoMessage));
                    }
                    if (UpperBar_Visible == null) {
                        MyData.push("UpperBar_Visible", "1");
                    }
                    if (colorMode == null) {
                        MyData.push("colorMode", "1");
                    }
                    if (laserOn == null) {
                        MyData.push("laserOn", "1");
                    }
                    if (cq3d == null) {
                        MyData.push("_cq3d", "0.07");
                    }

                    if (arcodaMessageList == null) {
                        MyData.push("arcodaMessageList", "");
                    }
                    if (crs == null) {
                        MyData.push("crs", "UTM");
                    }
                    if (xyz == null) {
                        MyData.push("xyz", "1");
                    }
                    if (Colore_Surf == null) {
                        MyData.push("Colore_Surf", "0");
                    }
                    if (Triangoli_Surf == null) {
                        MyData.push("Triangoli_Surf", "0");
                    }
                    if (Punti_Surf == null) {
                        MyData.push("Punti_Surf", "0");
                    }
                    if (Poly_Surf == null) {
                        MyData.push("Poly_Surf", "0");
                    }
                    if (Mostra_Testo == null) {
                        MyData.push("Mostra_Testo", "0");
                    }
                    if (ShowUtils == null) {
                        MyData.push("Mostra_Utils", "0");
                    }
                    if (ShowJson == null) {
                        MyData.push("Mostra_Json", "0");
                    }
                    if (Tema_SW == null) {
                        MyData.push("Tema_SW", "2");
                    }

                    if (typeView == null) {
                        MyData.push("typeView", "0");
                    }
                    if (gradientDB == null) {
                        MyData.push("gradientDB", "0.1");
                    }
                    if (showAlign == null) {
                        MyData.push("showAlign", "0");
                    }
                    if (line_Offset == null) {
                        MyData.push("line_Offset", "0.0");
                    }
                    if (ckSchermo == null) {
                        MyData.push("ckSchermo", "0");

                    }
                    if (drwaMachieSchema == null) {
                        MyData.push("drwaMachieSchema", "0");

                    }
                    if (geoidPath == null) {
                        MyData.push("geoidPath", "");

                    }
                    if (lock3dRotation == null) {
                        MyData.push("lock3dRotation", "1");
                    }
                    if (Off_Incr_Step == null) {
                        MyData.push("Off_Incr_Step", "0.01");
                    }
                }

                for (int i = 1; i <= 7; i++) {
                    String shortcutName = MyData.get_String("shortcutName_" + i);
                    String shortcutIndex = MyData.get_String("shortcutIndex");
                    String shortcutViewPort = MyData.get_String("shortcutViewPort_" + i);
                    String shortcutSlopeY = MyData.get_String("shortcutSlopeY_" + i);
                    String shortcutSlopeX = MyData.get_String("shortcutSlopeX_" + i);
                    String shortcutOffset = MyData.get_String("shortcutOffset_" + i);


                    if (shortcutName == null) {
                        MyData.push("shortcutName_" + i, "");
                    }
                    if (shortcutIndex == null) {
                        MyData.push("shortcutIndex", "1");
                    }
                    if (shortcutViewPort == null) {
                        MyData.push("shortcutViewPort_" + i, "0.0");
                    }
                    if (shortcutSlopeY == null) {
                        MyData.push("shortcutSlopeY_" + i, "0.0");
                    }
                    if (shortcutSlopeX == null) {
                        MyData.push("shortcutSlopeX_" + i, "0.0");
                    }
                    if (shortcutOffset == null) {
                        MyData.push("shortcutOffset_" + i, "0.00");
                    }
                }

                String demoLat = MyData.get_String("demoNORD");
                String demoLon = MyData.get_String("demoEAST");
                String demoZ = MyData.get_String("demoZ");
                if (demoLat == null) {
                    MyData.push("demoNORD", "4848012.123");
                }
                if (demoLon == null) {
                    MyData.push("demoEAST", "678515.123");
                }
                if (demoZ == null) {
                    MyData.push("demoZ", "90.0");
                }

                String progettoselected = MyData.get_String("progettoSelected");

                if (progettoselected == null) {
                    MyData.push("progettoSelected", "");
                }
                String progettoselectedPOLY = MyData.get_String("progettoSelected_POLY");

                if (progettoselectedPOLY == null) {
                    MyData.push("progettoSelected_POLY", "");
                }
                String progettoselectedPOINT = MyData.get_String("progettoSelected_POINT");

                if (progettoselectedPOINT == null) {
                    MyData.push("progettoSelected_POINT", "");
                }


                try {
                    DataSaved.screenOr = MyData.get_Int("screenOr");

                    if (Build.BRAND.equals("SRT8PROS") || Build.BRAND.equals("SRT7PROS")) {
                        DataSaved.screenOr = 0;
                        MyData.push("screenOn", "0");
                    }
                } catch (Exception e) {
                    DataSaved.screenOr = 0;
                }
                try {
                    DataSaved.wifiSSID = MyData.get_String("wifiSSID");
                } catch (Exception e) {
                    DataSaved.wifiSSID = "";
                }


                try {
                    DataSaved.language = MyData.get_String("language");

                } catch (Exception e) {
                    DataSaved.language = ("en_GB");
                    MyData.push("language", "en_GB");
                }

                try {
                    DataSaved.shortcutIndex = MyData.get_Int("shortcutIndex");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di shortcutIndex: " + e.getMessage());
                }

                try {
                    DataSaved.start2DX = MyData.get_Double("start2DX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di start2DX: " + e.getMessage());
                }

                try {
                    DataSaved.start2DY = MyData.get_Double("start2DY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di start2DY: " + e.getMessage());
                }

                try {
                    DataSaved.start2DZ = MyData.get_Double("start2DZ");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di start2DZ: " + e.getMessage());
                }

                ExcavatorLib.startRX = DataSaved.start2DX;
                ExcavatorLib.startRY = DataSaved.start2DY;
                ExcavatorLib.startRZ = DataSaved.start2DZ;
                try {
                    DataSaved.profileSelected = MyData.get_Int("ProfileSelected");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di profileSelected: " + e.getMessage());
                }


                try {
                    MyColorClass.colorBucket = MyData.get_Int("M" + indexMach + "coloreBenna");
                } catch (Exception e) {
                    MyColorClass.colorBucket = R.color.bg;
                }
                try {
                    MyColorClass.colorStick = MyData.get_Int("M" + indexMach + "coloreStick");
                } catch (Exception e) {
                    MyColorClass.colorStick = R.color.bg;
                }


                try {
                    DataSaved.machineName = MyData.get_String("M" + indexMach + "_Name");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di machineName: " + e.getMessage());
                }

                try {
                    DataSaved.lrBoom1 = MyData.get_Int("M" + indexMach + "_Boom1_MountPos");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrBoom1: " + e.getMessage());
                }

                try {
                    DataSaved.lrBoom2 = MyData.get_Int("M" + indexMach + "_Boom2_MountPos");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrBoom2: " + e.getMessage());
                }

                try {
                    DataSaved.lrStick = MyData.get_Int("M" + indexMach + "_Stick_MountPos");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrStick: " + e.getMessage());
                }

                try {
                    DataSaved.lrBucket = MyData.get_Int("M" + indexMach + "_Bucket_MountPos");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrBucket: " + e.getMessage());
                }

                try {
                    DataSaved.lrFrame = MyData.get_Int("M" + indexMach + "_Frame_MountPos");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrFrame: " + e.getMessage());
                }
                try {
                    DataSaved.reverseRotator = MyData.get_Int("M" + indexMach + "revTiltRot");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di revTiltRot: " + e.getMessage());
                }

                try {
                    DataSaved.L_Boom1 = MyData.get_Double("M" + indexMach + "_LengthBoom1");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Boom1: " + e.getMessage());
                }

                try {
                    DataSaved.L_Boom2 = MyData.get_Double("M" + indexMach + "_LengthBoom2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Boom2: " + e.getMessage());
                }

                try {
                    DataSaved.L_Stick = MyData.get_Double("M" + indexMach + "_LengthStick");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Stick: " + e.getMessage());
                }
                try {
                    DataSaved.isExtensionBoom = MyData.get_Int("M" + indexMach + "_isExt");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di _isExt: " + e.getMessage());
                }

                try {
                    DataSaved.L1 = MyData.get_Double("M" + indexMach + "_LengthL1");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L1: " + e.getMessage());
                }

                try {
                    DataSaved.L2 = MyData.get_Double("M" + indexMach + "_LengthL2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L2: " + e.getMessage());
                }

                try {
                    DataSaved.L3 = MyData.get_Double("M" + indexMach + "_LengthL3");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L3: " + e.getMessage());
                }

                try {
                    DataSaved.L4 = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_L4");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L4: " + e.getMessage());
                }

                try {
                    DataSaved.L_Pitch = MyData.get_Double("M" + indexMach + "_LengthPitch");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Pitch: " + e.getMessage());
                }

                try {
                    DataSaved.L_Roll = MyData.get_Double("M" + indexMach + "_LengthRoll");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Roll: " + e.getMessage());
                }

                try {
                    DataSaved.LSV = MyData.get_Double("M" + indexMach + "_LaserVStick");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di LSV: " + e.getMessage());
                }

                try {
                    DataSaved.LSH = MyData.get_Double("M" + indexMach + "_LaserHStick");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di LSH: " + e.getMessage());
                }

                try {

                    DataSaved.L_Bucket = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_Length");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Bucket: " + e.getMessage());
                }

                try {
                    DataSaved.W_Bucket = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_Width");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_Bucket: " + e.getMessage());
                }
                try {
                    DataSaved.W_Blade_RIGHT = MyData.get_Double("M" + indexMach + "_Bucket_" + "0" + "_Width_R");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_Blade_RIGHT: " + e.getMessage());
                }


                try {
                    DataSaved.W_Blade_LEFT = MyData.get_Double("M" + indexMach + "_Bucket_" + "0" + "_Width_L");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_Blade_LEFT: " + e.getMessage());
                }
                try {
                    DataSaved.W_Blade_TOT = DataSaved.W_Blade_LEFT + DataSaved.W_Blade_RIGHT;
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_BladeTOTt: " + e.getMessage());
                }
                try {
                    DataSaved.altezzaPali = MyData.get_Double("M" + indexMach + "_Bucket_" + "0" + "_Palo");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_Blade_LEFT: " + e.getMessage());
                }
                try {
                    DataSaved.distBetween = MyData.get_Double("M" + indexMach + "_Bucket_" + "0" + "_Between");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di _BetweenT: " + e.getMessage());
                }
                try {
                    DataSaved.altezzaLama = MyData.get_Double("M" + indexMach + "_Bucket_" + "0" + "_Lama");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di W_Blade_LEFT: " + e.getMessage());
                }

                try {
                    DataSaved.flat = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_Flat");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di flat: " + e.getMessage());
                }

                try {
                    DataSaved.offsetBoom1 = MyData.get_Double("M" + indexMach + "_OffsetBoom1");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetBoom1: " + e.getMessage());
                }

                try {
                    DataSaved.offsetBoom2 = MyData.get_Double("M" + indexMach + "_OffsetBoom2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetBoom2: " + e.getMessage());
                }

                try {
                    DataSaved.offsetStick = MyData.get_Double("M" + indexMach + "_OffsetStick");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetStick: " + e.getMessage());
                }

                try {
                    DataSaved.offsetDogBone = MyData.get_Double("M" + indexMach + "_OffsetDB");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetDogBone: " + e.getMessage());
                }

                try {
                    DataSaved.offsetRoll = MyData.get_Double("M" + indexMach + "_OffsetFrameX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetRoll: " + e.getMessage());
                }

                try {
                    DataSaved.offsetPitch = MyData.get_Double("M" + indexMach + "_OffsetFrameY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetPitch: " + e.getMessage());
                }

                try {
                    DataSaved.offsetBucket = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_Offset");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetBucket: " + e.getMessage());
                }

                try {
                    DataSaved.offsetFlat = MyData.get_Double("M" + indexMach + "_Bucket_" + indexBucket + "_Flat_Offset");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetFlat: " + e.getMessage());
                }
                //////draw
                try {
                    DataSaved.larghezza_Carro = MyData.get_Double("M" + indexMach + "larghezza_Carro");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di larghezza_Carro: " + e.getMessage());
                }
                try {
                    DataSaved.lunghrzza_Carro = MyData.get_Double("M" + indexMach + "lunghrzza_Carro");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lunghrzza_Carro: " + e.getMessage());
                }
                try {
                    DataSaved.larghezza_Frame = MyData.get_Double("M" + indexMach + "larghezza_Frame");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di larghezza_Frame: " + e.getMessage());
                }

                try {
                    DataSaved.lunghezza_Frame = MyData.get_Double("M" + indexMach + "lunghezza_Frame");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lunghezza_Frame: " + e.getMessage());
                }
                try {
                    DataSaved.larghezza_Braccio = MyData.get_Double("M" + indexMach + "larghezza_Braccio");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di larghezza_Braccio: " + e.getMessage());
                }
                try {
                    DataSaved.Extra_Heading = MyData.get_Int("M" + indexMach + "Extra_Heading");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Extra_Heading: " + e.getMessage());
                }

                try {
                    DataSaved.Interface_Type = MyData.get_Int("M" + indexMach + "Interface_Type");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Interface_Type: " + e.getMessage());
                }

                try {
                    DataSaved.minSpeedLeftUP = MyData.get_Int("M" + indexMach + "minSpeedLeftUP");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedLeftUP: " + e.getMessage());
                }

                try {
                    DataSaved.maxSpeedLeftUP = MyData.get_Int("M" + indexMach + "maxSpeedLeftUP");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedLeftUP: " + e.getMessage());
                }
                try {
                    DataSaved.minSpeedLeftDW = MyData.get_Int("M" + indexMach + "minSpeedLeftDW");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedLeftDW: " + e.getMessage());
                }
                try {
                    DataSaved.maxSpeedLeftDW = MyData.get_Int("M" + indexMach + "maxSpeedLeftDW");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedLeftDW: " + e.getMessage());
                }


                try {
                    DataSaved.minSpeedRightUP = MyData.get_Int("M" + indexMach + "minSpeedRightUP");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedRightUP: " + e.getMessage());
                }
                try {
                    DataSaved.maxSpeedRightUP = MyData.get_Int("M" + indexMach + "maxSpeedRightUP");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedRightUP: " + e.getMessage());
                }
                try {
                    DataSaved.minSpeedRightDW = MyData.get_Int("M" + indexMach + "minSpeedRightDW");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedRightDW: " + e.getMessage());
                }
                try {
                    DataSaved.maxSpeedRightDW = MyData.get_Int("M" + indexMach + "maxSpeedRightDW");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedRightDW: " + e.getMessage());
                }

                try {
                    DataSaved.minSpeedSS_A = MyData.get_Int("M" + indexMach + "minSpeedSS_A");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedSS_A: " + e.getMessage());
                }
                try {
                    DataSaved.maxSpeedSS_A = MyData.get_Int("M" + indexMach + "maxSpeedSS_A");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedSS_A: " + e.getMessage());
                }
                try {
                    DataSaved.minSpeedSS_B = MyData.get_Int("M" + indexMach + "minSpeedSS_B");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di minSpeedSS_B: " + e.getMessage());
                }
                try {
                    DataSaved.maxSpeedSS_B = MyData.get_Int("M" + indexMach + "maxSpeedSS_B");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di maxSpeedSS_B: " + e.getMessage());
                }

                try {
                    DataSaved.CAT_Type = MyData.get_Int("M" + indexMach + "CAT_Type");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di CAT_Type: " + e.getMessage());
                }

                try {
                    DataSaved.GAIN_LEFT = MyData.get_Int("M" + indexMach + "GAIN_LEFT");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di GAIN_LEFT: " + e.getMessage());
                }
                try {
                    DataSaved.GAIN_RIGHT = MyData.get_Int("M" + indexMach + "GAIN_RIGHT");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di GAIN_RIGHT: " + e.getMessage());
                }
                try {
                    DataSaved.HYDRAULIC_CONTROL_POINT_GRADER = MyData.get_Int("M" + indexMach + "HYDRAULIC_CONTROL_POINT_GRADER");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di HYDRAULIC_CONTROL_POINT_GRADER: " + e.getMessage());
                }
                try {
                    DataSaved.HYDRAULIC_CONTROL_POINT_DOZER = MyData.get_Int("M" + indexMach + "HYDRAULIC_CONTROL_POINT_DOZER");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di HYDRAULIC_CONTROL_POINT_DOZER: " + e.getMessage());
                }

                ////end draw

                try {
                    DataSaved.offsetH = MyData.get_Double("shortcutOffset_" + DataSaved.shortcutIndex) * -1;
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetH: " + e.getMessage());
                }

                try {
                    DataSaved.slopeY = MyData.get_Double("shortcutSlopeY_" + DataSaved.shortcutIndex);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di slopeY: " + e.getMessage());
                }

                try {
                    DataSaved.slopeX = MyData.get_Double("shortcutSlopeX_" + DataSaved.shortcutIndex);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di slopeX: " + e.getMessage());
                }

                try {
                    DataSaved.offsetZH = MyData.get_Double("Offset_Zero");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetZH: " + e.getMessage());
                }
                try {
                    DataSaved.boudrateCAN1 = MyData.get_Int("canBaud1");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di canBaud1: " + e.getMessage());
                }
                try {
                    DataSaved.boudrateCAN2 = MyData.get_Int("canBaud2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di canBaud2: " + e.getMessage());
                }
                try {
                    MyApp.GEOIDE_PATH = MyData.get_String("geoidPath");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di geoidPath: " + e.getMessage());
                }
                try {
                    DataSaved.ckSchermo = MyData.get_Int("ckSchermo");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di ckSchermo: " + e.getMessage());
                }
                try {
                    DataSaved.drwaMachieSchema = MyData.get_Int("drwaMachieSchema");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di drwaMachieSchema: " + e.getMessage());
                }
                try {
                    DataSaved.myBrightness = Float.parseFloat(MyData.get_String("brightness"));
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di myBrightness: " + e.getMessage());
                }

                try {
                    DataSaved.offsetLaserZH = MyData.get_Double("Laser_Height_Zero");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetLaserZH: " + e.getMessage());
                }

                try {
                    DataSaved.offsetLaserZR = MyData.get_Double("Laser_Reach_Zero");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetLaserZR: " + e.getMessage());
                }

                try {
                    DataSaved.offsetHDT = MyData.get_Double("Offset_Hdt");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetHDT: " + e.getMessage());
                }

                try {
                    DataSaved.deadbandH = MyData.get_Double("Deadband_H");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deadbandH: " + e.getMessage());
                }
                /// dozer
                try {
                    DataSaved.tolleranza_Z = MyData.get_Double("tolleranza_Z");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di tolleranza_Z: " + e.getMessage());
                }
                try {
                    DataSaved.HYDRAULIC_WINDOW = MyData.get_Double("HYDRAULIC_WINDOW");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di HYDRAULIC_WINDOW: " + e.getMessage());
                }
                try {
                    DataSaved.tolleranza_XY = MyData.get_Double("tolleranza_XY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di tolleranza_XY: " + e.getMessage());
                }
                try {
                    DataSaved.tolleranza_Slope = MyData.get_Double("tolleranza_Slope");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di tolleranza_Slope: " + e.getMessage());
                }


                /// end dozer

                try {
                    DataSaved.driftStep = MyData.get_Int("driftStep");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di driftStep: " + e.getMessage());
                }

                try {
                    DataSaved.driftSign = MyData.get_Int("driftSign");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di driftSign: " + e.getMessage());
                }
                try {
                    DataSaved.deadbandFlatAngle = MyData.get_Double("Deadband_FlatAngle");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deadbandFlatAngle: " + e.getMessage());
                }


                try {
                    DataSaved.deltaX = MyData.get_Double("M" + indexMach + "_OffsetGPSX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deltaX: " + e.getMessage());
                }

                try {
                    DataSaved.deltaY = MyData.get_Double("M" + indexMach + "_OffsetGPSY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deltaY: " + e.getMessage());
                }

                try {
                    DataSaved.deltaZ = MyData.get_Double("M" + indexMach + "_OffsetGPSZ");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deltaZ: " + e.getMessage());
                }

                try {
                    DataSaved.offsetmDeltaX = MyData.get_Double("offsetmDeltaX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetmDeltaX: " + e.getMessage());
                }

                try {
                    DataSaved.offsetmDeltaY = MyData.get_Double("offsetmDeltaY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetmDeltaY: " + e.getMessage());
                }

                try {
                    DataSaved.deltaGPS2 = MyData.get_Double("M" + indexMach + "_OffsetGPS2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di deltaGPS2: " + e.getMessage());
                }
                try {
                    DataSaved.distG1_G2 = MyData.get_Double("M" + indexMach + "_distG1_G2");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di distG1_G2: " + e.getMessage());
                }

                try {
                    DataSaved.useYawFrame = MyData.get_Int("M" + indexMach + "useYawFrame");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di useYawFrame: " + e.getMessage());
                }
                try {
                    DataSaved.useQuickSwitch = MyData.get_Int("M" + indexMach + "useQuickSwitch");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di useQuickSwitch: " + e.getMessage());
                }

                try {
                    DataSaved.Wheel_Steer_Rev = MyData.get_Int("M" + indexMach + "Wheel_Steer_Rev");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Wheel_Steer_Rev: " + e.getMessage());
                }
                try {
                    DataSaved.Wheel_Steer_Min = MyData.get_Int("M" + indexMach + "Wheel_Steer_Min");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Wheel_Steer_Min: " + e.getMessage());
                }
                try {
                    DataSaved.Wheel_Steer_Med = MyData.get_Int("M" + indexMach + "Wheel_Steer_Med");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Wheel_Steer_Max: " + e.getMessage());
                }
                try {
                    DataSaved.Wheel_Steer_Max = MyData.get_Int("M" + indexMach + "Wheel_Steer_Max");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Wheel_Steer_Max: " + e.getMessage());
                }
                try {
                    DataSaved.Wheel_Steer_Range = MyData.get_Double("M" + indexMach + "Wheel_Steer_Range");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Wheel_Steer_Range: " + e.getMessage());
                }


                try {
                    DataSaved.miniPitch_L = MyData.get_Double("M" + indexMach + "_Swing_Len");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di miniPitch_L: " + e.getMessage());
                }


                try {
                    DataSaved.isWL = MyData.get_Int("M" + indexMach + "_isWL");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di isWL: " + e.getMessage());
                }

                try {
                    DataSaved.enOUT = MyData.get_Int("M" + indexMach + "_enOUT");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di enOUT: " + e.getMessage());
                }

                try {
                    DataSaved.HighThreshold = MyData.get_Double("M" + indexMach + "HighThreshold");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di HighThreshold: " + e.getMessage());
                }
                try {
                    DataSaved.LowThreshold = MyData.get_Double("M" + indexMach + "LowThreshold");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di LowThreshold: " + e.getMessage());
                }
                try {
                    DataSaved.PassoGriglia = MyData.get_Double("M" + indexMach + "PassoGriglia");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di PassoGriglia: " + e.getMessage());
                }
                try {
                    DataSaved.EnableMapping = MyData.get_Int("M" + indexMach + "EnableMapping");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di EnableMapping: " + e.getMessage());
                }






                try {
                    DataSaved.usuraLamaSX = MyData.get_Double("M" + indexMach + "usuraLamaSX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di usuraLamaSX: " + e.getMessage());
                }
                try {
                    DataSaved.usuraLamaCX = MyData.get_Double("M" + indexMach + "usuraLamaCX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di usuraLamaCX: " + e.getMessage());
                }
                try {
                    DataSaved.usuraLamaDX = MyData.get_Double("M" + indexMach + "usuraLamaDX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di usuraLamaDX: " + e.getMessage());
                }

                DataSaved.progettoSelected = MyData.get_String("progettoSelected");
                DataSaved.progettoSelected_POLY = MyData.get_String("progettoSelected_POLY");
                DataSaved.progettoSelected_POINT = MyData.get_String("progettoSelected_POINT");


                try {
                    DataSaved.my_comPort = MyData.get_Int("M" + indexMach + "_comPort");
                } catch (NumberFormatException e) {
                    MyData.push("M" + indexMach + "_comPort", "0");
                    DataSaved.my_comPort = 0;
                }
                try {
                    DataSaved.gpsType = MyData.get_Int("M" + indexMach + "_sc600");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di gpsType: " + e.getMessage());
                }

                try {
                    DataSaved.reqSpeed = MyData.get_Int("M" + indexMach + "reqSpeed");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di reqSpeed: " + e.getMessage());
                }

                try {
                    DataSaved.radioMode = MyData.get_Int("M" + indexMach + "radioMode");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di radioMode: " + e.getMessage());
                }
                try {
                    DataSaved.priorityNet = MyData.get_Int("M" + indexMach + "priorityNet");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di priorityNet: " + e.getMessage());
                }

                try {
                    DataSaved.useLowResolution = MyData.get_Int("M" + indexMach + "_useLowRes");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di useLowResolution: " + e.getMessage());
                }

                DataSaved.macaddress = MyData.get_String("M" + indexMach + "_macaddress");
                DataSaved.S_macAddress_CAN = MyData.get_String("M" + indexMach + "_macaddressCAN");
                DataSaved.deviceName = MyData.get_String("M" + indexMach + "_deviceName");

                try {
                    DataSaved.offsetBubble_X = MyData.get_Double("M" + indexMach + "_ebubbleX");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetBubble_X: " + e.getMessage());
                }

                try {
                    DataSaved.offsetBubble_Y = MyData.get_Double("M" + indexMach + "_ebubbleY");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetBubble_Y: " + e.getMessage());
                }

                try {
                    DataSaved.bubble_DB = MyData.get_Double("M" + indexMach + "_ebubbleDB");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di bubble_DB: " + e.getMessage());
                }

                try {
                    DataSaved.useTiltEbubble = MyData.get_Int("M" + indexMach + "_ebubbleUseTilt");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di useTiltEbubble: " + e.getMessage());
                }

                try {
                    DataSaved.isCanOpen = MyData.get_Int("M" + indexMach + "_useCanOpen");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di isCanOpen: " + e.getMessage());
                }

                try {
                    DataSaved.hasQuick = MyData.get_Int("M" + indexMach + "_hasQuick");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di hasQuick: " + e.getMessage());
                }

                try {
                    DataSaved.lrTilt = MyData.get_Int("M" + indexMach + "_Tilt_MountPos" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lrTilt: " + e.getMessage());
                }

                try {
                    DataSaved.L_Tilt = MyData.get_Double("M" + indexMach + "_Tilt_Length" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di L_Tilt: " + e.getMessage());
                }

                try {
                    DataSaved.offsetTiltDeltaAngle = MyData.get_Double("M" + indexMach + "_Tilt_Offset" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetTiltDeltaAngle: " + e.getMessage());
                }

                try {
                    DataSaved.piccolaBucket = MyData.get_Double("M" + indexMach + "_Tilt_piccolaBucket" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di piccolaBucket: " + e.getMessage());
                }

                try {
                    DataSaved.offsetTilt = MyData.get_Double("M" + indexMach + "_Tilt_Offset_Angle" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetTilt: " + e.getMessage());
                }

                try {
                    DataSaved.offsetDegWTilt = MyData.get_Double("M" + indexMach + "_Offset_DegWTilt" + indexBucket);
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di offsetDegWTilt: " + e.getMessage());
                }

                try {
                    DataSaved.scale_Factor = MyData.get_Double("scaleFactor");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di scale_Factor: " + e.getMessage());
                }

                try {
                    DataSaved.scale_Factor3D = MyData.get_Double("scaleFactor3D");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di scale_Factor3D: " + e.getMessage());
                }

                try {
                    DataSaved.scale_FactorVista1D = MyData.get_Double("scaleFactor_vista1D");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di scale_FactorVista1D: " + e.getMessage());
                }

                try {
                    DataSaved.scale_FactorVista2D = MyData.get_Double("scaleFactor_vista2D");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di scale_FactorVista2D: " + e.getMessage());
                }

                try {
                    DataSaved.projectionFlag = MyData.get_Int("projectionFlag");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di projectionFlag: " + e.getMessage());
                }

                try {
                    DataSaved.colorMode = MyData.get_Int("colorMode");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di colorMode: " + e.getMessage());
                }

                try {
                    DataSaved.laserOn = MyData.get_Int("laserOn");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di laserOn: " + e.getMessage());
                }

                try {
                    DataSaved.Max_CQ3D = MyData.get_Double("_cq3d");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Max_CQ3D: " + e.getMessage());
                }

                DataSaved.S_CRS = MyData.get_String("crs");

                try {
                    DataSaved.Colore_Surf = MyData.get_Int("Colore_Surf");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Colore_Surf: " + e.getMessage());
                }

                try {
                    DataSaved.Triangoli_Surf = MyData.get_Int("Triangoli_Surf");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Triangoli_Surf: " + e.getMessage());
                }

                try {
                    DataSaved.Punti_Surf = MyData.get_Int("Punti_Surf");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Punti_Surf: " + e.getMessage());
                }

                try {
                    DataSaved.Poly_Surf = MyData.get_Int("Poly_Surf");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Poly_Surf: " + e.getMessage());
                }

                try {
                    DataSaved.ShowText = MyData.get_Int("Mostra_Testo");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di ShowText: " + e.getMessage());
                }
                try {
                    DataSaved.ShowUtils = MyData.get_Int("Mostra_Utils");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di polilinee2D: " + e.getMessage());
                }
                try {
                    DataSaved.ShowJson = MyData.get_Int("Mostra_Json");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Sottoservizi: " + e.getMessage());
                }

                try {
                    DataSaved.gradientDB = MyData.get_Double("gradientDB");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di gradientDB: " + e.getMessage());
                }
                try {
                    DataSaved.showAlign = MyData.get_Int("showAlign");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di showAlign: " + e.getMessage());
                }
                try {
                    DataSaved.line_Offset = MyData.get_Double("line_Offset");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inuzializzazione di line_Offset: " + e.getMessage());
                }
                try {
                    DataSaved.lock3dRotation = MyData.get_Int("lock3dRotation");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di lock3dRotation: " + e.getMessage());
                }

                try {
                    DataSaved.Off_Incr_Step = MyData.get_Double("Off_Incr_Step");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di Off_Incr_Step: " + e.getMessage());
                }
                try {
                    DataSaved.temaSoftware = MyData.get_Int("Tema_SW");

                } catch (NumberFormatException e) {
                    DataSaved.temaSoftware = 0;
                }
                try {
                    DataSaved.typeView = MyData.get_Int("typeView");

                } catch (NumberFormatException e) {
                    DataSaved.typeView = 1;
                }

                try {
                    DataSaved.demoNORD = MyData.get_Double("demoNORD");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di demoNORD: " + e.getMessage());
                }

                try {
                    DataSaved.demoEAST = MyData.get_Double("demoEAST");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di demoEAST: " + e.getMessage());
                }

                try {
                    DataSaved.demoZ = MyData.get_Double("demoZ");
                } catch (Exception e) {
                    Log.e("Error", "Errore nell'inizializzazione di demoZ: " + e.getMessage());
                }


                String tmp = MyData.get_String("Profile" + indexProfile + "_punti");
                if (tmp != null) {
                    DataSaved.puntiProfilo = new double[tmp.split(";").length][3];
                    for (int i = 0; i < tmp.split(";").length; i++) {
                        DataSaved.puntiProfilo[i][0] = Double.parseDouble(tmp.split(";")[i].split("/")[0]);
                        DataSaved.puntiProfilo[i][1] = Double.parseDouble(tmp.split(";")[i].split("/")[1]);
                        DataSaved.puntiProfilo[i][2] = Double.parseDouble(tmp.split(";")[i].split("/")[2]);
                    }
                }
                if (!startedService) {
                    try {
                        startService(new Intent(UpdateValuesService.this, CanService.class));
                    } catch (Exception e) {
                        Log.e("Error", "Errore nell'inizializzazione di CanService: " + e.getMessage());
                    }
                    try {
                        startService(new Intent(UpdateValuesService.this, CanSender.class));
                    } catch (Exception e) {
                        Log.e("Error", "Errore nell'inizializzazione di CanSender: " + e.getMessage());
                    }
                    result = new ProjCoordinate();
                    shifted = new ProjCoordinate();
                    resultWgs = new ProjCoordinate();
                    ReadProjectService.startCRS();

                    startedService = true;


                }

                isUpodating = false;
            } catch (Exception e) {
                Log.e("Error", "Exception non gestita");
            }
        }
    }
}