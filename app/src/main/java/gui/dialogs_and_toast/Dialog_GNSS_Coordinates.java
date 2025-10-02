package gui.dialogs_and_toast;

import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static packexcalib.gnss.CRS_Strings._150580;
import static packexcalib.gnss.CRS_Strings._28992;
import static packexcalib.gnss.CRS_Strings._31370;
import static packexcalib.gnss.CRS_Strings._UTM;
import static services.Bluetooth_GNSS_Service.GNSSServiceState;
import static services.CanService.nmeaSTX_Disc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.NmeaListener;
import services.Bluetooth_GNSS_Service;
import utils.CPCanHelper;
import utils.FullscreenActivity;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Dialog_GNSS_Coordinates extends BaseClass {
    TextView textViewPW;
    Dialog_Edit_Zeta_DXF dialogEditZetaDxf;
    ProgressBar progressBar;
    Activity activity;
    public Dialog alertDialog;
    ImageView save, title, BTConnect, serialCon, editZ, rtkMode;
    TextView txmchdt, txtant1, txtbennasx, txtbennacx, txtbennadx, txSat, txAge, txQual, txCrs, txCq, txCon, extraAng;
    TextView framA, boomA, boom2A, stickA, bucketA, tiltA;
    TextView framO, boomO, boom2O, stickO, bucketO, tiltO;
    Button cqpiu, cqmeno;
    TextView tvCq, frame, boom1, boom2, stick, bucket, tilt;
    private boolean isUpdating = false;
    private Handler handler;
    EpsgDialog epsgDialog;
    int indexMach;
    ImageView imbl, imbc, imbr;


    public Dialog_GNSS_Coordinates(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        epsgDialog = new EpsgDialog(activity);

    }

    public void show() {

        alertDialog.create();
        alertDialog.setContentView(R.layout.dialog_gnss_coordinates_2);
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        findView();
        onClick();
        init();
        startUpdatingCoordinates();
        FullscreenActivity.setFullScreen(alertDialog);
        dialogEditZetaDxf = new Dialog_Edit_Zeta_DXF(activity, DataSaved.bucketEdge);


    }

    private void findView() {
        LanguageSetter.setLocale(activity, "en");
        textViewPW = alertDialog.findViewById(R.id.powerS);
        title = alertDialog.findViewById(R.id.titleG);//
        txSat = alertDialog.findViewById(R.id.txSat);//
        txAge = alertDialog.findViewById(R.id.txage);//
        txCq = alertDialog.findViewById(R.id.txcq);//
        txQual = alertDialog.findViewById(R.id.txfix);//
        txCrs = alertDialog.findViewById(R.id.crstxt);
        save = alertDialog.findViewById(R.id._btnSavex);
        cqpiu = alertDialog.findViewById(R.id.cqpiu);//
        cqmeno = alertDialog.findViewById(R.id.cqmeno);//
        tvCq = alertDialog.findViewById(R.id.tvcq);//
        txCon = alertDialog.findViewById(R.id.txconnected);//
        BTConnect = alertDialog.findViewById(R.id.btBTconnect);
        serialCon = alertDialog.findViewById(R.id.serialCon);
        txtbennasx = alertDialog.findViewById(R.id.txtbennasx);
        txtbennacx = alertDialog.findViewById(R.id.txtbennacx);
        txtbennadx = alertDialog.findViewById(R.id.txtbennadx);
        txtant1 = alertDialog.findViewById(R.id.txtant1);
        txmchdt = alertDialog.findViewById(R.id.txmchdt);
        rtkMode = alertDialog.findViewById(R.id._switchRadio);
        imbl = alertDialog.findViewById(R.id.imbl);
        imbc = alertDialog.findViewById(R.id.imbc);
        imbr = alertDialog.findViewById(R.id.imbr);
        framA = alertDialog.findViewById(R.id.idframeAng);
        boomA = alertDialog.findViewById(R.id.idboom1Ang);
        boom2A = alertDialog.findViewById(R.id.idBoom2Ang);
        stickA = alertDialog.findViewById(R.id.idStickAng);
        bucketA = alertDialog.findViewById(R.id.idbucketAng);
        tiltA = alertDialog.findViewById(R.id.idTiltAng);
        framO = alertDialog.findViewById(R.id.idframeOff);
        boomO = alertDialog.findViewById(R.id.idboom1Off);
        boom2O = alertDialog.findViewById(R.id.idBoom2Off);
        stickO = alertDialog.findViewById(R.id.idStickOff);
        bucketO = alertDialog.findViewById(R.id.idbucketOff);
        tiltO = alertDialog.findViewById(R.id.idTiltOff);
        txCrs.setTextSize(14);
        editZ = alertDialog.findViewById(R.id._editZ);
        progressBar = alertDialog.findViewById(R.id.progresss);
        frame = alertDialog.findViewById(R.id.idframe);
        boom1 = alertDialog.findViewById(R.id.idboom1);
        boom2 = alertDialog.findViewById(R.id.idBoom2);
        stick = alertDialog.findViewById(R.id.idStick);
        bucket = alertDialog.findViewById(R.id.idbucket);
        tilt = alertDialog.findViewById(R.id.idTilt);
        extraAng = alertDialog.findViewById(R.id.idExtraAng);


    }

    public void init() {
        if (DataSaved.isWL == 2 || DataSaved.isWL == 3 || DataSaved.isWL == 4) {
            imbl.setImageResource(R.drawable.lama_misura_sinistra);
            imbc.setImageResource(R.drawable.lama_misura_cnt);
            imbr.setImageResource(R.drawable.lama_misura_destra);
        }
        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x01, 0x11, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP PSW REQUEST
        progressBar.setVisibility(View.INVISIBLE);
        indexMach = MyData.get_Int("MachineSelected");
        DataSaved.radioMode = MyData.get_Int("M" + indexMach + "radioMode");
        if (DataSaved.useQuickSwitch == 1) {
            rtkMode.setVisibility(View.VISIBLE);
        } else {
            rtkMode.setVisibility(View.GONE);
        }
        if (isTech) {
            editZ.setVisibility(View.VISIBLE);

            imbl.setBackgroundColor(DataSaved.bucketEdge == -1 ? activity.getColor(R.color.yellow) : activity.getColor(R.color.transparent));
            imbc.setBackgroundColor(DataSaved.bucketEdge == 0 ? activity.getColor(R.color.yellow) : activity.getColor(R.color.transparent));
            imbr.setBackgroundColor(DataSaved.bucketEdge == 1 ? activity.getColor(R.color.yellow) : activity.getColor(R.color.transparent));

        } else {
            editZ.setVisibility(View.GONE);
            rtkMode.setVisibility(View.GONE);
        }

        tvCq.setText("Max CQ\n" + Utils.showCoords(String.valueOf(DataSaved.Max_CQ3D)).replace(",", "."));

        frame.setBackgroundColor(Color.TRANSPARENT);
        boom1.setBackgroundColor(Color.TRANSPARENT);
        boom2.setBackgroundColor(Color.TRANSPARENT);
        stick.setBackgroundColor(Color.TRANSPARENT);
        bucket.setBackgroundColor(Color.TRANSPARENT);
        tilt.setBackgroundColor(Color.TRANSPARENT);


    }

    public void switchingRadio() {
        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, 0x11, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP PSW REQUEST
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
            new CustomToast(activity, "Wait for DataLink mode switching\nand check Fix...").show_alert();
            alertDialog.dismiss();
        }

    }

    private void onClick() {
        serialCon.setOnClickListener(view -> {
            byte speed = 0;
            switch (DataSaved.reqSpeed) {
                case 0:
                    speed = 5;
                    break;
                case 1:
                    speed = 4;
                    break;
                case 2:
                    speed = 3;
                    break;
                case 3:
                    speed = 0;
                    break;

            }
            byte msg = 0x03;


            MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
        });
        imbl.setOnClickListener(view -> {
            DataSaved.bucketEdge = -1;
            imbl.setBackgroundColor(activity.getColor(R.color.yellow));
            imbc.setBackgroundColor(activity.getColor(R.color.transparent));
            imbr.setBackgroundColor(activity.getColor(R.color.transparent));

        });
        imbc.setOnClickListener(view -> {
            DataSaved.bucketEdge = 0;
            imbl.setBackgroundColor(activity.getColor(R.color.transparent));
            imbc.setBackgroundColor(activity.getColor(R.color.yellow));
            imbr.setBackgroundColor(activity.getColor(R.color.transparent));

        });
        imbr.setOnClickListener(view -> {
            DataSaved.bucketEdge = 1;
            imbl.setBackgroundColor(activity.getColor(R.color.transparent));
            imbc.setBackgroundColor(activity.getColor(R.color.transparent));
            imbr.setBackgroundColor(activity.getColor(R.color.yellow));

        });
        rtkMode.setOnClickListener(view -> {
            rtkMode.setEnabled(false);
            DataSaved.radioMode += 1;
            DataSaved.radioMode = DataSaved.radioMode % 2;
            if (DataSaved.radioMode == 1) {
                rtkMode.setImageResource(R.drawable.base_mode);
                switch (DataSaved.gpsType) {
                    case 0:
                        //SMC
                        if (DataSaved.my_comPort == 0) {
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x11, 0, (byte) DataSaved.priorityNet, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                            MyData.push("M" + indexMach + "radioMode", "0");
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x73, 0x61, (byte) 0x76, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x6C});

                        } else {

                        }

                        break;

                    case 1:
                        //S980+BE

                        break;

                    case 2:
                        //SC600+

                        break;
                }
            } else {
                rtkMode.setImageResource(R.drawable.sim_96);
                switch (DataSaved.gpsType) {
                    case 0:
                        //SMC
                        if (DataSaved.my_comPort == 0) {
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x11, 0x1, (byte) DataSaved.priorityNet, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                            MyData.push("M" + indexMach + "radioMode", "1");
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x73, 0x61, (byte) 0x76, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x6C});

                        } else {

                        }

                        break;

                    case 1:
                        //S980+BE

                        break;

                    case 2:
                        //SC600+


                        break;
                }
            }
            progressBar.setVisibility(View.VISIBLE);
            new CustomToast(activity, "...WAIT...").show();

            save.setVisibility(View.INVISIBLE);
            editZ.setVisibility(View.INVISIBLE);

            (new Handler()).postDelayed(this::switchingRadio, 8000);

        });
        editZ.setOnClickListener(view -> {
            if (!dialogEditZetaDxf.alertDialog.isShowing()) {
                dialogEditZetaDxf.show();
            }
        });
        BTConnect.setOnClickListener(view -> {

            if (MyDeviceManager.serialCom(DataSaved.my_comPort).equals("BT")) {
                if (!GNSSServiceState) {
                    activity.startService(new Intent(activity.getApplicationContext(), Bluetooth_GNSS_Service.class));
                } else {
                    activity.stopService(new Intent(activity.getApplicationContext(), Bluetooth_GNSS_Service.class));
                }
            }
        });

        cqpiu.setOnClickListener(view -> {
            DataSaved.Max_CQ3D += 0.01;
            init();
        });
        cqmeno.setOnClickListener(view -> {
            if (DataSaved.Max_CQ3D >= 0.02) {
                DataSaved.Max_CQ3D -= 0.01;
            }
            init();
        });


        save.setOnClickListener((View v) -> {
            stopUpdatingCoordinates();
            MyData.push("_cq3d", String.valueOf(DataSaved.Max_CQ3D));

            alertDialog.dismiss();
        });
        txCrs.setOnClickListener(view -> {
            /*if(!epsgDialog.dialog.isShowing()){
                epsgDialog.show(txCrs);
            }*/
        });


    }

    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateCoordinates();
        }
    }

    private void stopUpdatingCoordinates() {
        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    private void updateCoordinates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update coord TextView with new coordinates
                try {
                    if(Build.BRAND.equals("SRT8PROS")||Build.BRAND.equals("SRT7PROS")){
                        textViewPW.setText("");
                    }else {
                        textViewPW.setText(CPCanHelper.voltApollo2 + " V");
                    }
                    if (DataSaved.Extra_Heading != 0) {
                        try {
                            extraAng.setText(String.format("%.2f", NmeaListener.roof_Orientation) + " °");
                        } catch (Exception e) {
                            extraAng.setText("Error");
                        }

                    } else {
                        extraAng.setText("NOT USED");
                    }
                    if (DataSaved.isWL < 2) {
                        boolean[] b = PLC_DataTypes_BigEndian.U8_to_bitmask_be((byte) errorCode);

                        tilt.setText("TILT");
                        if (b[0]) {
                            frame.setBackgroundColor(Color.RED);
                            frame.setTextColor(Color.WHITE);
                            framO.setText(String.format("%.02f", DataSaved.offsetPitch).replace(",", ".") + "°" + "/" +
                                    String.format("%.02f", DataSaved.offsetRoll).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrFrame != 0) {
                                frame.setBackgroundColor(Color.GREEN);
                                frame.setTextColor(Color.DKGRAY);
                                framA.setText(String.format("%.02f", ExcavatorLib.correctPitch).replace(",", ".") + "°" + "/" +
                                        String.format("%.02f", ExcavatorLib.correctRoll).replace(",", ".") + "°");
                                framO.setText(String.format("%.02f", DataSaved.offsetPitch).replace(",", ".") + "°" + "/" +
                                        String.format("%.02f", DataSaved.offsetRoll).replace(",", ".") + "°");
                            } else {
                                frame.setBackgroundColor(Color.GRAY);
                                frame.setTextColor(Color.WHITE);

                            }
                        }

                        if (b[1]) {
                            boom1.setBackgroundColor(Color.RED);
                            boom1.setTextColor(Color.WHITE);
                            boomO.setText(String.format("%.02f", DataSaved.offsetBoom1).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrBoom1 != 0) {
                                boom1.setBackgroundColor(Color.GREEN);
                                boom1.setTextColor(Color.DKGRAY);
                                boomA.setText(String.format("%.02f", ExcavatorLib.correctBoom1).replace(",", ".") + "°");
                                boomO.setText(String.format("%.02f", DataSaved.offsetBoom1).replace(",", ".") + "°");
                            } else {
                                boom1.setBackgroundColor(Color.GRAY);
                                boom1.setTextColor(Color.WHITE);
                            }
                        }
                        if (b[2]) {
                            boom2.setBackgroundColor(Color.RED);
                            boom2.setTextColor(Color.WHITE);
                            boom2O.setText(String.format("%.02f", DataSaved.offsetBoom2).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrBoom2 != 0) {
                                boom2.setBackgroundColor(Color.GREEN);
                                boom2.setTextColor(Color.DKGRAY);
                                boom2A.setText(String.format("%.02f", ExcavatorLib.correctBoom2).replace(",", ".") + "°");
                                boom2O.setText(String.format("%.02f", DataSaved.offsetBoom2).replace(",", ".") + "°");
                            } else {
                                boom2.setBackgroundColor(Color.GRAY);
                                boom2.setTextColor(Color.WHITE);
                            }
                        }
                        if (b[3]) {
                            stick.setBackgroundColor(Color.RED);
                            stick.setTextColor(Color.WHITE);
                            stickO.setText(String.format("%.02f", DataSaved.offsetStick).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrStick != 0) {
                                stick.setBackgroundColor(Color.GREEN);
                                stick.setTextColor(Color.DKGRAY);
                                stickA.setText(String.format("%.02f", ExcavatorLib.correctStick).replace(",", ".") + "°");
                                stickO.setText(String.format("%.02f", DataSaved.offsetStick).replace(",", ".") + "°");
                            } else {
                                stick.setBackgroundColor(Color.GRAY);
                                stick.setTextColor(Color.WHITE);
                            }
                        }
                        if (b[4]) {
                            bucket.setBackgroundColor(Color.RED);
                            bucket.setTextColor(Color.WHITE);
                            bucketO.setText(String.format("%.02f", DataSaved.offsetBucket).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrBucket != 0) {
                                bucket.setBackgroundColor(Color.GREEN);
                                bucket.setTextColor(Color.DKGRAY);
                                bucketA.setText(String.format("%.02f", ExcavatorLib.correctBucket).replace(",", ".") + "°");
                                bucketO.setText(String.format("%.02f", DataSaved.offsetBucket).replace(",", ".") + "°");
                            } else {
                                bucket.setBackgroundColor(Color.GRAY);
                                bucket.setTextColor(Color.WHITE);
                            }
                        }
                        if (b[5]) {
                            tilt.setBackgroundColor(Color.RED);
                            tilt.setTextColor(Color.WHITE);
                            tiltO.setText(String.format("%.02f", DataSaved.offsetTilt).replace(",", ".") + "°");
                        } else {
                            if (DataSaved.lrTilt != 0) {
                                tilt.setBackgroundColor(Color.GREEN);
                                tilt.setTextColor(Color.DKGRAY);
                                tiltA.setText(String.format("%.02f", ExcavatorLib.correctTilt).replace(",", ".") + "°");
                                tiltO.setText(String.format("%.02f", DataSaved.offsetTilt).replace(",", ".") + "°");
                            } else {
                                tilt.setBackgroundColor(Color.GRAY);
                                tilt.setTextColor(Color.WHITE);
                            }
                        }
                    } else {

                        frame.setBackgroundColor(Color.GRAY);
                        boom1.setBackgroundColor(Color.GRAY);
                        boom2.setBackgroundColor(Color.GRAY);
                        stick.setBackgroundColor(Color.GRAY);
                        bucket.setBackgroundColor(Color.GRAY);
                        tilt.setText("TILT/BLADE");
                        if (errorCode == 0) {
                            tilt.setBackgroundColor(Color.GREEN);
                            tilt.setTextColor(Color.DKGRAY);
                        } else {
                            tilt.setBackgroundColor(Color.RED);
                            tilt.setTextColor(Color.WHITE);
                        }

                    }

                    if (MyDeviceManager.serialCom(DataSaved.my_comPort).equals("BT")) {
                        BTConnect.setVisibility(View.VISIBLE);
                        serialCon.setVisibility(View.INVISIBLE);
                    } else {
                        BTConnect.setVisibility(View.INVISIBLE);
                        serialCon.setVisibility(View.VISIBLE);
                    }
                    if (GNSSServiceState) {


                        BTConnect.setImageResource(R.drawable.bluetooth_connetti);
                        BTConnect.setAlpha(1f);
                        BTConnect.setBackgroundResource(R.drawable.custom_background_ok);


                    } else {
                        BTConnect.setImageResource(R.drawable.bluetooth_disconnetti);
                        BTConnect.setAlpha(1f);
                        BTConnect.setBackgroundResource(R.drawable.custom_background_transp);


                    }

                    if (DataSaved.radioMode == 0) {
                        rtkMode.setImageResource(R.drawable.base_mode);
                    } else {
                        rtkMode.setImageResource(R.drawable.sim_96);
                    }

                    double hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
                    hdt = hdt % 360;
                    if (hdt > 360) {
                        hdt -= 360;
                    }
                    if (hdt < 0) {
                        hdt += 360;
                    }


                    txSat.setText(" " + NmeaListener.ggaSat);
                    txQual.setText(" " + NmeaListener.fix1);
                    txAge.setText(" " + NmeaListener.ggaRtk);
                    try {
                        txCq.setText(" " + Utils.showCoords(String.valueOf(NmeaListener.VRMS_)));
                    } catch (Exception e) {
                        txCq.setText("0.000");
                    }
                    switch (DataSaved.S_CRS) {
                        case _UTM:
                            txCrs.setText(_UTM);

                            break;
                        case "28992":
                            txCrs.setText(_28992);
                            break;
                        case "31370":
                            txCrs.setText(_31370);
                            break;
                        case "150580":
                            txCrs.setText(_150580);
                            break;
                        default:
                            txCrs.setText("CRS");
                            break;
                    }
                    if (DataSaved.my_comPort == 0) {
                        if (nmeaSTX_Disc) {

                            txCon.setText("GPS\nDISCONNECTED");
                            txCon.setTextColor(Color.BLACK);
                            serialCon.setBackgroundResource(R.drawable.custom_background_transp);
                        } else {
                            txCon.setText("GPS\nCONNECTED");
                            txCon.setTextColor(Color.BLACK);
                            serialCon.setBackgroundResource(R.drawable.custom_background_ok);

                        }
                        if (DataSaved.gpsOk) {

                            title.setImageResource(R.drawable.gps_si);
                        } else {


                            title.setImageResource(R.drawable.gps_no);
                        }
                    } else if (DataSaved.my_comPort == 5) {
                        if (GNSSServiceState) {
                            txCon.setText("BT\nCONNECTED");
                            txCon.setTextColor(Color.BLACK);
                        } else {
                            txCon.setText("BT\nDISCONNECTED");
                            txCon.setTextColor(Color.BLACK);
                        }
                    } else {
                        if (DataSaved.gpsOk) {
                            title.setImageResource(R.drawable.gps_si);
                            txCon.setText("NMEA OK");
                            txCon.setTextColor(Color.BLACK);
                            serialCon.setBackgroundResource(R.drawable.custom_background_ok);
                        } else {

                            title.setImageResource(R.drawable.gps_no);
                            txCon.setText("NMEA\nERROR");
                            txCon.setTextColor(Color.BLACK);
                            serialCon.setBackgroundResource(R.drawable.custom_background_transp);
                        }
                    }


                    try {

                        txtant1.setText("E: " + Utils.showCoords(String.valueOf(NmeaListener.Est1)) + "   N: " + Utils.showCoords(String.valueOf(NmeaListener.Nord1)) + "  Z: " + Utils.showCoords(String.valueOf(NmeaListener.Quota1)) + "  " + Utils.getMetriSimbolCoords());
                        if (MyApp.GEOIDE_PATH != null) {
                            if (Deg2UTM.geoidError) {
                                txCon.append("\nGRID ERROR");
                            } else {
                                txCon.append("\nGRID OK");
                            }
                        } else {
                            txCon.append("\n ");
                        }
                    } catch (Exception e) {

                    }
                    try {
                        txtbennasx.setText("E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "   N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "  " + "  Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[2])) + "  " + Utils.getMetriSimbolCoords());

                        txtbennacx.setText("E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[0])) + "   N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "  Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[2])) + "  " + Utils.getMetriSimbolCoords());

                        //txtbennacx.setText("E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[0])) + "   N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "  Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[2])) + "    " + decLatCx + "   " + decLonCx);
                        txtbennadx.setText("E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "   N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "  " + "  Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[2])) + "  " + Utils.getMetriSimbolCoords());
                        //txtbennadx.setText("E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "   N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "  " + "  Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[2])) + "    " + decLatDx + "   " + decLonDx);


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                    if (NmeaListener.mch_Hdt == 999.999) {
                        txmchdt.setText("HDT Error");

                    } else {
                        txmchdt.setText(String.format("%.2f", hdt).replace(",", ".") + " °");

                    }


                    if (isUpdating) {
                        updateCoordinates();
                    }
                } catch (Exception e) {
                }
            }
        }, 100);
    }

}
