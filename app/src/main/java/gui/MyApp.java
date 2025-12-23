package gui;


import static services.CanService.boom1Disc;
import static services.CanService.boom1OK;
import static services.CanService.boom2Disc;
import static services.CanService.boom2OK;
import static services.CanService.bucketDisc;
import static services.CanService.bucketOK;
import static services.CanService.frameDisc;
import static services.CanService.frameOK;
import static services.CanService.stickDisc;
import static services.CanService.stickOK;
import static services.CanService.tiltDisc;
import static services.CanService.tiltOK;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;

import org.greenrobot.eventbus.EventBus;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cloud.S3ManagerSingleton;
import drill_pile.gui.Drill_MainPage;
import drill_pile.gui.Ecu_Sensors_Activity;
import event_bus.SerialEvent;
import gui.boot_and_choose.Activity_Home_Page;
import gui.buckets.BucketCalib;
import gui.buckets.BucketCalibTilt;
import gui.buckets.BucketChooserActivity;
import gui.debug_ecu.Can_Msg_Debug;
import gui.debug_ecu.DebugExcavatorActivity;
import gui.debug_ecu.Serial_Msg_Debug;
import gui.dialogs_and_toast.SensorAlertDialog;
import gui.dialogs_user_settings.Nuova_User_Settings;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.Digging3D_DXF;
import gui.digging_excavator.DiggingProfile;
import gui.digging_excavator.Digging_CutAndFill1D;
import gui.digging_excavator.Digging_CutAndFill2D;
import gui.gps.NmeaGenerator;
import gui.gps.Nuovo_Gps;
import gui.grading_dozergrader.Grading3D_DXF;
import gui.hydro.CASE_Activity;
import gui.hydro.CAT_SEA_Activity;
import gui.hydro.DEERE_LIEBHERR_Activity;
import gui.hydro.ECU_Activity;
import gui.hydro.Hydro_Activity_Entering;
import gui.hydro.KOMATSU_Activity;
import gui.my_opengl.My3DActivity;
import gui.my_opengl.MyGLRenderer;
import gui.profiles.ProfileCalibAuto;
import gui.profiles.ProfileCalibManual;
import gui.profiles.ProfilesMenuActivity;
import gui.projects.Activity_Crea_Superficie;
import gui.projects.PickProject;
import gui.projects.Remote_Activity;
import gui.projects.Usb_Project_Nova;
import gui.tech_menu.Boom1Calib;
import gui.tech_menu.Boom2Calib;
import gui.tech_menu.CanOpenTSM;
import gui.tech_menu.DrillEncoder;
import gui.tech_menu.DrillToolCalib;
import gui.tech_menu.ExcavatorChooserActivity;
import gui.tech_menu.FrameCalib;
import gui.tech_menu.GPS_Autocalib;
import gui.tech_menu.LinkageCalib;
import gui.tech_menu.MastLinkCalib;
import gui.tech_menu.Nuova_Blade_Calib;
import gui.tech_menu.Nuova_Machine_Settings;
import gui.tech_menu.StickCalib;
import gui.tech_menu.TiltCalib;
import gui.tech_menu.Tilt_Blade;
import gui.tech_menu.XYZ_Calib;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.gnss.GridShiftTransformer;
import packexcalib.gnss.NmeaListener;
import services.CanSender;
import services.CanService;
import services.TriangleService;
import services.UpdateValuesService;
import utils.FullscreenActivity;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {
    public static  int MAX_NUMERO_FACCE=5000;
    public static final int numGeoidiInterni = 1;//TODO DECIDERE QUALI GEOIDI METTERE DI BUILTIN
    //audio
    public static boolean isAlto, isBasso, isCentro;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable soundChecker;
    private String currentState = "";
    private boolean isCheckerRunning = false;
    //license
    public static String deviceBuild = "";
    public static int errorCode;
    public static String activationCode = "none";
    public static String restoreCode;
    public static int licenseType = 5;
    public static String expiry = "2001-12-31";
    public static final long timeUI = 65;
    public static String[] geoidAll = new String[]{};
    public static String GEOIDE_PATH = null;
    public static GridShiftTransformer heposTransformer;
    public static String gridFile_GR_dE = "";
    public static String gridFile_GR_dN = "";
    public static String DEVICE_SN = "";
    public static Activity visibleActivity;
    public static String Actualactivity;
    public static boolean hAlarm, isApollo, canError, isOffgrid;
    public static String folderPath;
    public static String deu;
    public double h;
    SensorAlertDialog sensorAlertDialog1, sensorAlertDialog2, sensorAlertDialog3, sensorAlertDialog4, sensorAlertDialog5, sensorAlertDialog6, sensorAlertDialogBLADE;
    int frameCounter;
    int showConnCounter, checkCounter;
    boolean soundOn, soundOn2;
    ApolloPro apolloPro;
    Apollo2 apollo2;
    private volatile boolean mRunning = false;
    private ScheduledExecutorService executorService;
    public static final boolean GEN1 = Build.BRAND.equals("SRT8PROS") || Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("qti");
    public static final boolean GEN2 = Build.BRAND.equals("MEGA_1") || Build.BRAND.equals("TANK2_7_10") || Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS");

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.BRAND.equals("MEGA_1")){
            MAX_NUMERO_FACCE=10000;
        }
        UpdateValuesService.isUpodating = true;
        registerActivityLifecycleCallbacks(this);
        Log.d("trt", Build.BRAND);
        if (Build.BRAND.equals("MEGA_1") || Build.BRAND.equals("TANK2_7_10") || Build.BRAND.equals("SRT8PROS") || Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("qti") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            isApollo = true;
            folderPath = "/StonexMC_V4";
            MyApp.DEVICE_SN=MyDeviceManager.getDeviceSN(this);


        } else {

            isApollo = false;
            folderPath = "/StonexMC_V4";
            showConnCounter = 0;
            MyApp.DEVICE_SN = "STX_UNKNOWN";

        }
        if (MyData.get_String("machinestate") != null) {
            if (MyData.get_String("machinestate").equals("0")) {
                try {
                    if (isApollo) {

                        stopService(new Intent(this, CanService.class));
                        stopService(new Intent(this, CanSender.class));
                        stopService(new Intent(this, TriangleService.class));

                    }


                    //Log.d("machinestate", "service stopped");
                } catch (Exception e) {
                    //Log.d("machinestate", e.toString());
                }
            }
            //Log.d("machinestate", MyData.get_String("machinestate"));
        }

        deu = copyGeoidFromAssets(this, "DEUTSCH_GEOID.GGF", "DEUTSCH_GEOID.GGF");


        String pp = Environment.getExternalStorageDirectory().toString() + folderPath + "/Geoids/";


        geoidAll = listFilesInFolderGeoid(pp);

        // Crea un nuovo array con spazio per i 3 elementi aggiuntivi
        int originalLength = geoidAll != null ? geoidAll.length : 0;
        String[] newGeoidAll = new String[originalLength + numGeoidiInterni];

        // Se l'array originale non è nullo, copialo
        if (geoidAll != null) {
            System.arraycopy(geoidAll, 0, newGeoidAll, 0, originalLength);
        }

        newGeoidAll[originalLength] = deu;
        geoidAll = newGeoidAll;
        try {
            gridFile_GR_dE = copyGeoidFromAssets(this, "dE_2km_V1-0.grd", "dE_2km_V1-0.grd");
            gridFile_GR_dN = copyGeoidFromAssets(this, "dN_2km_V1-0.grd", "dN_2km_V1-0.grd");
            if (heposTransformer == null) {
                try {
                    File dEfile = new File(gridFile_GR_dE);
                    File dNfile = new File(gridFile_GR_dN);

                    heposTransformer = new GridShiftTransformer(dEfile, dNfile);
                } catch (Exception e) {
                    Log.e("GridShift", Log.getStackTraceString(e));
                    heposTransformer = null;
                }
            }
        } catch (Exception e) {
            heposTransformer = null;
            Log.e("GridShift", Log.getStackTraceString(e));
        }


        myCrash();

    }


    public void myCrash() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {

            if (isApollo) {
                MyDeviceManager.showBar(visibleActivity);
                MyDeviceManager.OUT1(visibleActivity, 0);
                MyDeviceManager.OUT2(visibleActivity, 0);


            }
            MyData.push("progettoSelected", "");
            MyData.push("progettoSelected_POLY", "");
            MyData.push("progettoSelected_POINT", "");
            MyData.push("machinestate", "1");
            // Chiudi l'applicazione e tutti i suoi processi
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10); // Codice di uscita 10 per indicare un crash
        });
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

/*
git add .
git commit -m "Messaggio"
git push
 */

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity != null) {

            if (activity instanceof My3DActivity) {
                startConditionChecker(activity);
            } else {
                stopConditionChecker();
                stopSound();
            }
            sensorAlertDialog1 = new SensorAlertDialog(activity, "#1\n\nSENSOR ERROR \n\n FRAME or CAN disconnected!");
            sensorAlertDialog2 = new SensorAlertDialog(activity, "#2\n\nSENSOR ERROR \n\n BOOM 1 sensor disconnected!");
            sensorAlertDialog3 = new SensorAlertDialog(activity, "#3\n\nSENSOR ERROR \n\n BOOM 2 sensor disconnected!");
            sensorAlertDialog4 = new SensorAlertDialog(activity, "#4\n\nSENSOR ERROR \n\n STICK sensor disconnected!");
            sensorAlertDialog5 = new SensorAlertDialog(activity, "#5\n\nSENSOR ERROR \n\n BUCKET sensor disconnected!");
            sensorAlertDialog6 = new SensorAlertDialog(activity, "#6\n\nSENSOR ERROR \n\n TILT sensor disconnected!");
            sensorAlertDialogBLADE = new SensorAlertDialog(activity, "#1\n\nSENSOR ERROR \n\n BLADE sensor disconnected!");
            if (MyData.get_String("Pivot_Height_Alarm") != null) {
                h = MyData.get_Double("Pivot_Height_Alarm");
                if (MyData.get_String("DeltaPivot") != null) {
                    double h2 = MyData.get_Double("DeltaPivot");
                    h = h - h2;
                }
            } else {
                h = Double.MAX_VALUE;
            }
            visibleActivity = activity;
            Actualactivity = String.valueOf(activity);//
            m_updateUI(activity, true);


            DataSaved.myBrightness = Math.max(DataSaved.myBrightness, 0.01f);
            WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
            layoutParams.screenBrightness = DataSaved.myBrightness;

        }

        try {
            FullscreenActivity.setFullScreen(activity);
            LanguageSetter.setLocale(activity, MyData.get_String("language"));

        } catch (Exception e) {
            Toast.makeText(activity, "Failed to set Language", Toast.LENGTH_LONG).show();
        }

    }

    private void startConditionChecker(Context context) {
        if (isCheckerRunning) return;

        handler = new Handler(Looper.getMainLooper());
        soundChecker = new Runnable() {
            @Override
            public void run() {
                checkAndPlaySound(context);
                handler.postDelayed(this, 1000); // ogni 1 secondo
            }
        };
        handler.post(soundChecker);
        isCheckerRunning = true;
    }

    private void stopConditionChecker() {
        if (handler != null && soundChecker != null) {
            handler.removeCallbacks(soundChecker);
        }
        isCheckerRunning = false;
    }

    private void checkAndPlaySound(Context context) {
        String newState = "";
        int indexAudioSystem = MyData.get_Int("indexAudioSystem");
        if (indexAudioSystem != 2) {
            if (isAlto) {
                newState = "alto";
            } else if (isCentro) {
                newState = "centro";
            } else if (isBasso) {
                newState = "basso";
            } else {
                newState = "";
            }
        } else {
            if (isAlto) {
                newState = "";
            } else if (isCentro) {
                newState = "centro";
            } else if (isBasso) {
                newState = "basso";
            } else {
                newState = "";
            }
        }

        if (!newState.equals(currentState) || mediaPlayer == null || !mediaPlayer.isPlaying()) {
            stopSound();
            currentState = newState;

            if ("alto".equals(newState)) {
                playSound(context, R.raw.audio_blu);
            } else if ("centro".equals(newState)) {
                playSound(context, R.raw.audio_verde);
            } else if ("basso".equals(newState)) {
                playSound(context, R.raw.audio_rosso);
            }
        }
    }

    private void playSound(Context context, int resId) {
        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        try {
            stopSound();
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (activity instanceof My3DActivity) {
            stopConditionChecker();
            stopSound();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }


    @SuppressLint("DiscouragedApi")
    public void m_updateUI(Activity activity, boolean mRunning) {

        if (mRunning && executorService == null) {

            this.mRunning = true;

            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        @SuppressLint({"SetTextI18n", "DefaultLocale"})
                        @Override
                        public void run() {
                            try {

                                if (DataSaved.my_comPort == 4) {


                                    new SerialEvent(NmeaGenerator.generateLLQ());
                                    new SerialEvent(NmeaGenerator.generateGPHDT());
                                    new SerialEvent(NmeaGenerator.generateGPGGA());
                                    if (MyApp.visibleActivity instanceof Serial_Msg_Debug) {
                                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPGGA()));
                                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPHDT()));
                                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateLLQ()));
                                    }
                                    NmeaListener.NmeaStandard(NmeaGenerator.generateGPGGA());
                                    NmeaListener.NmeaStandard(NmeaGenerator.generateGPHDT());
                                    NmeaListener.NmeaStandard(NmeaGenerator.generateLLQ());


                                }

                                errori();
                                if (DataSaved.useYawFrame == 1 && DataSaved.driftStep > 0) {
                                    frameCounter += 1;

                                    if (frameCounter % (DataSaved.driftStep) == 0) {
                                        switch (DataSaved.driftSign) {
                                            case 0:
                                                DataSaved.offsetHDT -= 0.1;
                                                //Log.d("Drift", DataSaved.driftSign + "  " + DataSaved.driftStep + " ");
                                                break;
                                            case 1:
                                                DataSaved.offsetHDT += 0.1;

                                                break;
                                        }

                                    }
                                }

                                updateAll(visibleActivity);
                                if (!isApollo) {
                                    if (CanSender.tryingBTCAN) {
                                        Toast.makeText(visibleActivity.getApplicationContext(), "Connecting to...\n" + DataSaved.S_macAddress_CAN, Toast.LENGTH_LONG).show();
                                        CanSender.tryingBTCAN = false;
                                    }
                                }
                                if ((visibleActivity instanceof My3DActivity
                                        || visibleActivity instanceof Digging2D
                                        || visibleActivity instanceof Digging1D
                                        || visibleActivity instanceof DiggingProfile)) {
                                    try {
                                        checkCounter++;
                                        if (checkCounter == 99) {
                                            checkDialogs();
                                        }
                                        if (checkCounter == 100) {
                                            checkCounter = 0;
                                        }

                                        hAlarm = ExcavatorLib.highestPoint > h;
                                        if (hAlarm) {
                                            if (!soundOn) {
                                                if (isApollo) {
                                                    MyDeviceManager.OUT1(visibleActivity, 1);
                                                }
                                                soundOn = true;
                                            }

                                        } else {
                                            if (soundOn) {
                                                if (isApollo) {

                                                    MyDeviceManager.OUT1(visibleActivity, 0);

                                                }
                                                soundOn = false;
                                            }

                                        }


                                        switch (DataSaved.bucketEdge) {
                                            case -1:
                                                isOffgrid = TriangleService.ltOffGrid;

                                                break;

                                            case 0:
                                                isOffgrid = TriangleService.ctOffGrid;
                                                break;

                                            case 1:
                                                isOffgrid = TriangleService.rtOffGrid;
                                                break;
                                        }


                                        if (isOffgrid) {
                                            if (!soundOn2) {
                                                if (isApollo) {

                                                    MyDeviceManager.OUT2(visibleActivity, 1);
                                                }
                                                soundOn2 = true;
                                            }

                                        } else {
                                            if (soundOn2) {
                                                if (isApollo) {

                                                    MyDeviceManager.OUT2(visibleActivity, 0);

                                                }
                                                soundOn2 = false;
                                            }

                                        }


                                    } catch (Exception e) {
                                        hAlarm = false;
                                        isOffgrid = false;
                                    }
                                } else {
                                    hAlarm = false;
                                    isOffgrid = false;
                                }

                            } catch (Exception e) {
                                Log.e("TestCRSSS", Log.getStackTraceString(e));
                                hAlarm = false;
                                isOffgrid = false;
                            }
                            if (hAlarm) {
                                CanSender.d0 = 1;
                            } else {
                                CanSender.d0 = 0;
                            }
                        }
                    });
                }
            }, 0, timeUI, TimeUnit.MILLISECONDS);
        } else if (!mRunning && executorService != null) {
            // Ferma il ciclo solo se è stato avviato
            this.mRunning = false;
            executorService.shutdown();
            executorService = null;
        }
    }


    private void updateAll(Activity activity) {
        if (activity instanceof PickProject) {
            ((PickProject) activity).updateUI();
        } else if (activity instanceof Ecu_Sensors_Activity) {
            ((Ecu_Sensors_Activity) activity).updateUI();
        } else if (activity instanceof Drill_MainPage) {
            ((Drill_MainPage) activity).updateUI();
        } else if (activity instanceof BucketCalib) {
            ((BucketCalib) activity).updateUI();
        } else if (activity instanceof Remote_Activity) {
            ((Remote_Activity) activity).updateUI();
        } else if (activity instanceof BucketCalibTilt) {
            ((BucketCalibTilt) activity).updateUI();
        } else if (activity instanceof BucketChooserActivity) {
            ((BucketChooserActivity) activity).updateUI();
        } else if (activity instanceof DebugExcavatorActivity) {
            ((DebugExcavatorActivity) activity).updateUI();
        } else if (activity instanceof Nuova_User_Settings) {
            ((Nuova_User_Settings) activity).updateUI();
        } else if (activity instanceof Digging1D) {
            ((Digging1D) activity).updateUI();
        } else if (activity instanceof Digging2D) {
            ((Digging2D) activity).updateUI();
        } else if (activity instanceof Digging_CutAndFill1D) {
            ((Digging_CutAndFill1D) activity).updateUI();
        } else if (activity instanceof Digging_CutAndFill2D) {
            ((Digging_CutAndFill2D) activity).updateUI();
        } else if (activity instanceof DiggingProfile) {
            ((DiggingProfile) activity).updateUI();
        } else if (activity instanceof ProfileCalibAuto) {
            ((ProfileCalibAuto) activity).updateUI();
        } else if (activity instanceof ProfileCalibManual) {
            ((ProfileCalibManual) activity).updateUI();
        } else if (activity instanceof ProfilesMenuActivity) {
            ((ProfilesMenuActivity) activity).updateUI();
        } else if (activity instanceof Usb_Project_Nova) {
            ((Usb_Project_Nova) activity).updateUI();
        } else if (activity instanceof Boom1Calib) {
            ((Boom1Calib) activity).updateUI();
        } else if (activity instanceof Boom2Calib) {
            ((Boom2Calib) activity).updateUI();
        } else if (activity instanceof CanOpenTSM) {
            ((CanOpenTSM) activity).updateUI();
        } else if (activity instanceof ExcavatorChooserActivity) {
            ((ExcavatorChooserActivity) activity).updateUI();
        } else if (activity instanceof FrameCalib) {
            ((FrameCalib) activity).updateUI();
        } else if (activity instanceof GPS_Autocalib) {
            ((GPS_Autocalib) activity).updateUI();
        } else if (activity instanceof LinkageCalib) {
            ((LinkageCalib) activity).updateUI();
        } else if (activity instanceof StickCalib) {
            ((StickCalib) activity).updateUI();
        } else if (activity instanceof TiltCalib) {
            ((TiltCalib) activity).updateUI();
        } else if (activity instanceof XYZ_Calib) {
            ((XYZ_Calib) activity).updateUI();
        } else if (activity instanceof Digging3D_DXF) {
            ((Digging3D_DXF) activity).updateUI();
        } else if (activity instanceof Nuovo_Gps) {
            ((Nuovo_Gps) activity).updateUI();
        } else if (activity instanceof Tilt_Blade) {
            ((Tilt_Blade) activity).updateUI();
        } else if (activity instanceof Grading3D_DXF) {
            ((Grading3D_DXF) activity).updateUI();
        } else if (activity instanceof Activity_Crea_Superficie) {
            ((Activity_Crea_Superficie) activity).updateUI();
        } else if (activity instanceof ECU_Activity) {
            ((ECU_Activity) activity).updateUI();
        } else if (activity instanceof My3DActivity) {
            ((My3DActivity) activity).updateUI();

        } else if (activity instanceof Activity_Home_Page) {
            ((Activity_Home_Page) activity).updateUI();

        } else if (activity instanceof Nuova_Blade_Calib) {
            ((Nuova_Blade_Calib) activity).updateUI();

        } else if (activity instanceof Nuova_Machine_Settings) {
            ((Nuova_Machine_Settings) activity).updateUI();

        } else if (activity instanceof Hydro_Activity_Entering) {
            ((Hydro_Activity_Entering) activity).updateUI();

        } else if (activity instanceof Can_Msg_Debug) {
            ((Can_Msg_Debug) activity).updateUI();

        } else if (activity instanceof CAT_SEA_Activity) {
            ((CAT_SEA_Activity) activity).updateUI();

        } else if (activity instanceof DEERE_LIEBHERR_Activity) {
            ((DEERE_LIEBHERR_Activity) activity).updateUI();

        } else if (activity instanceof KOMATSU_Activity) {
            ((KOMATSU_Activity) activity).updateUI();
        } else if (activity instanceof CASE_Activity) {
            ((CASE_Activity) activity).updateUI();
        }else if (activity instanceof MastLinkCalib) {
            ((MastLinkCalib) activity).updateUI();
        }else if (activity instanceof DrillEncoder) {
            ((DrillEncoder) activity).updateUI();
        }else if (activity instanceof DrillToolCalib) {
            ((DrillToolCalib) activity).updateUI();
        }

    }

    private void checkDialogs() {

        if (DataSaved.isWL < DOZER) {
            canError = tiltDisc && DataSaved.lrTilt != 0 ||
                    bucketDisc && DataSaved.lrBucket != 0 ||
                    stickDisc && DataSaved.lrStick != 0 ||
                    boom2Disc && DataSaved.lrBoom2 != 0 ||
                    boom1Disc && DataSaved.lrBoom1 != 0 ||
                    frameDisc && DataSaved.lrFrame != 0;

        } else {
            canError = tiltDisc && DataSaved.lrBucket != 0;


        }


        if (visibleActivity instanceof Grading3D_DXF ||
                (visibleActivity instanceof Activity_Crea_Superficie && DataSaved.isWL == DOZER) || (visibleActivity instanceof Activity_Crea_Superficie && DataSaved.isWL == DOZER_SIX)) {
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialogBLADE.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    tiltDisc && DataSaved.lrBucket != 0) {
                sensorAlertDialogBLADE.show();
            }
        }
        if (visibleActivity instanceof Digging1D ||
                visibleActivity instanceof Digging_CutAndFill1D ||
                visibleActivity instanceof Digging2D ||
                visibleActivity instanceof Digging_CutAndFill2D ||
                visibleActivity instanceof DiggingProfile ||
                visibleActivity instanceof Digging3D_DXF ||
                visibleActivity instanceof Activity_Crea_Superficie) {

            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    tiltDisc && DataSaved.lrTilt != 0) {
                sensorAlertDialog6.show();
            }
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    bucketDisc && DataSaved.lrBucket != 0 && DataSaved.isWL < DOZER) {
                sensorAlertDialog5.show();
            }
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    stickDisc && DataSaved.lrStick != 0) {
                sensorAlertDialog4.show();
            }
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    boom2Disc && DataSaved.lrBoom2 != 0) {
                sensorAlertDialog3.show();
            }
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    boom1Disc && DataSaved.lrBoom1 != 0) {
                sensorAlertDialog2.show();
            }
            if (!sensorAlertDialog6.alertDialog.isShowing() &&
                    !sensorAlertDialog5.alertDialog.isShowing() &&
                    !sensorAlertDialog4.alertDialog.isShowing() &&
                    !sensorAlertDialog3.alertDialog.isShowing() &&
                    !sensorAlertDialog2.alertDialog.isShowing() &&
                    !sensorAlertDialog1.alertDialog.isShowing() &&
                    frameDisc && DataSaved.lrFrame != 0) {
                sensorAlertDialog1.show();
            }

        }
    }

    private void errori() {
        if (DataSaved.isWL ==EXCAVATOR||DataSaved.isWL==WHEELLOADER) {
            errorCode = PLC_DataTypes_BigEndian.Encode_8_bool_be(new boolean[]{
                    (!frameOK) && DataSaved.lrFrame != 0,
                    (!boom1OK) && DataSaved.lrBoom1 != 0,
                    (!boom2OK) && DataSaved.lrBoom2 != 0,
                    (!stickOK) && DataSaved.lrStick != 0,
                    (!bucketOK) && DataSaved.lrBucket != 0,
                    (!tiltOK) && DataSaved.lrTilt != 0, false, false

            });
        } else {
            errorCode = PLC_DataTypes_BigEndian.Encode_8_bool_be(new boolean[]{
                    tiltDisc && DataSaved.lrBucket != 0,
                    false,
                    false, false, false, false, false, false
            });
        }
    }

    public static String copyGeoidFromAssets(Context context, String assetName, String outFileName) {
        File outFile = new File(context.getCacheDir(), outFileName);
        try (InputStream inputStream = context.getAssets().open(assetName);
             OutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return outFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String copyGeoidFromExternalStorage(Context context, String sourcePath, String outFileName) {
        File sourceFile = new File(sourcePath);
        File outFile = new File(context.getCacheDir(), outFileName);

        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return outFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] listFilesInFolderGeoid(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            return new String[0]; // Cartella non valida
        }

        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            return new String[0]; // Nessun file presente
        }

        List<String> ugfFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && (file.getName().toLowerCase().endsWith(".ugf") ||
                    file.getName().toLowerCase().endsWith(".bin") ||
                    file.getName().toLowerCase().endsWith(".ggf"))) {
                ugfFiles.add(file.getAbsolutePath());
            }
        }

        return ugfFiles.toArray(new String[0]);
    }

    public static void updateGeoidFolderFromCloud(String localFolderPath, String remoteFolderPath) {
        S3ManagerSingleton s3Manager = S3ManagerSingleton.getInstance(visibleActivity);

        s3Manager.getFoldersFiles_2(remoteFolderPath, new S3ManagerSingleton.S3Callback() {
            @Override
            public void onSuccess(Map<String, Object> remoteFiles) {
                //Log.d("GeoidSync", "Trovati " + remoteFiles.size() + " file sul cloud.");

                ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(() -> {
                    File localFolder = new File(localFolderPath);
                    if (!localFolder.exists() && !localFolder.mkdirs()) {
                        //Log.w("GeoidSync", "Impossibile creare la cartella locale: " + localFolder.getAbsolutePath());
                        return;
                    }

                    for (String remoteFileName : remoteFiles.keySet()) {
                        String lowerName = remoteFileName.toLowerCase();
                        if (!(lowerName.endsWith(".ugf") || lowerName.endsWith(".bin") || lowerName.endsWith(".ggf"))) {
                            // Log.d("GeoidSync", "Saltato file non valido: " + remoteFileName);
                            continue;
                        }

                        File localFile = new File(localFolder, remoteFileName);
                        boolean shouldDownload = false;

                        if (!localFile.exists()) {
                            shouldDownload = true;
                            //Log.d("GeoidSync", "File mancante localmente: " + remoteFileName);
                        } else {
                            Object remoteSizeObj = remoteFiles.get(remoteFileName);
                            long remoteSize = 0;
                            if (remoteSizeObj instanceof Number) {
                                remoteSize = ((Number) remoteSizeObj).longValue();
                            } else {
                                //Log.w("GeoidSync", "Formato dimensione file remoto non valido per " + remoteFileName);
                                continue;
                            }

                            long localSize = localFile.length();
                            if (localSize != remoteSize) {
                                shouldDownload = true;
                                //Log.d("GeoidSync", "File differente: " + remoteFileName + " (locale=" + localSize + ", remoto=" + remoteSize + ")");
                            }
                        }

                        if (shouldDownload) {
                            String s3Key = remoteFolderPath.endsWith("/") ?
                                    remoteFolderPath + remoteFileName :
                                    remoteFolderPath + "/" + remoteFileName;
                            // Log.d("GeoidSync", "Scarico: " + s3Key + " -> " + localFile.getAbsolutePath());

                            try {
                                s3Manager.downloadFile(s3Key, localFile.getAbsolutePath());
                            } catch (Exception e) {
                                // Log.w("GeoidSync", "Errore download file " + remoteFileName + ": " + e.getMessage());
                            }
                        }
                    }

                    executor.shutdown();
                    // Log.d("GeoidSync", "Sincronizzazione completata.");
                });
            }

            @Override
            public void onError(Exception e) {
                // Log.w("GeoidSync", "Impossibile sincronizzare geoid (offline?): " + e.getMessage());
            }
        });
    }





}