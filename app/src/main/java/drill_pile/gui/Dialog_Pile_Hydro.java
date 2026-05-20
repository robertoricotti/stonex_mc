package drill_pile.gui;

import static packexcalib.exca.DataSaved.DRILL_STATUS;
import static services.CanService.ECU_Connected;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import com.example.stx_dig.R;

import java.util.Objects;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.projects.PickProject;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;

/**
 * Dialog taratura idraulica valvole Danfoss.
 * <p>
 * Scala valori:
 * - 0..10000 = 0..100%
 * - 6500 = 65%
 * - 3500 = 35%
 * <p>
 * Per ogni valvola:
 * - EVx_UPPER = Porta A, barra superiore dal 50% verso 75%
 * - EVx_LOWER = Porta B, barra inferiore dal 50% verso 25%
 * <p>
 * Range grafico:
 * - Porta A: 5000..7500
 * - Porta B: 5000..2500
 */
public class Dialog_Pile_Hydro {
    int mchint;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    byte[] bytes;
    private ProgressBar writeProgress;
    private final Handler writeHandler = new Handler(Looper.getMainLooper());
    private int writeCounter = 0;
    private boolean isWriting = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean autoRefreshEnabled = false;
    public static boolean REVERSE_PLUMB_AX_1;
    public static boolean REVERSE_PLUMB_AX_2;
    public static boolean SWAP_PLUMB_AX;
    public static boolean REVERSE_HAMMER;
    public static boolean REVERSE_RISE_LOW;
    public static boolean REVERSE_FOOT_ENCODER;
    public static int RISE_DIST_mm = 0;
    public static int HAMMER_ENGAGEMENT_DELAY_seconds = 0;

    public static int EV1_UPPER = 5000;
    public static int EV1_LOWER = 5000;

    public static int EV2_UPPER = 5000;
    public static int EV2_LOWER = 5000;

    public static int EV3_UPPER = 5000;
    public static int EV3_LOWER = 5000;

    public static int EV4_UPPER = 5000;
    public static int EV4_LOWER = 5000;

    public static int EV5_UPPER = 5000;
    public static int EV5_LOWER = 5000;

    public static int EV6_UPPER = 5000;
    public static int EV6_LOWER = 5000;

    private static final int CENTER_VALUE = 5000;

    private static final int UPPER_MIN = 5000;
    private static final int UPPER_MAX = 7500;

    private static final int LOWER_MIN = 2500;
    private static final int LOWER_MAX = 5000;

    // 100 = 1%, 50 = 0.5%, 10 = 0.1%
    private static final int STEP_VALUE = 10;

    private final Activity activity;
    public Dialog dialog;

    private CheckBox ck1, ck2, ck3, ck4, ck5, ck6, ck7;
    private ImageView close, readEcu, writeEcu, ecuCo;

    private final View[] upperAreas = new View[6];
    private final View[] lowerAreas = new View[6];

    private final View[] upperBars = new View[6];
    private final View[] lowerBars = new View[6];

    private final TextView[] valueTexts = new TextView[6];

    private final TextView[] upperPlusButtons = new TextView[6];
    private final TextView[] upperMinusButtons = new TextView[6];
    private final TextView[] lowerPlusButtons = new TextView[6];
    private final TextView[] lowerMinusButtons = new TextView[6];
    private EditText engD, riseD;
    private TextView setAllDef, isAutoTXT;
    CustomNumberDialog customNumberDialog;
    public static boolean hasReaded = false;

    public Dialog_Pile_Hydro(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        initDefaultsIfNeeded();
    }

    public void show() {
        MyDeviceManager.CanWrite(true, 1, 0x3AA, 8, new byte[]{
                1, 1, 1, 1, 1, 1, 1, (byte) 0xFA
        });
        dialog.create();
        dialog.setContentView(R.layout.dialog_pile_hydro);
        dialog.setCancelable(false);
        dialog.setOnDismissListener(d -> {
            stopWriteLoop();
            stopAutoRefresh(); // se presente
        });
        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();

        FullscreenActivity.setFullScreen(dialog);

        findView();
        onClick();

        View root = dialog.findViewById(android.R.id.content);
        if (root != null) {
            root.post(() -> {
                refresh();
                startAutoRefresh();
            });
        } else {
            refresh();
            startAutoRefresh();
        }
    }

    private void findView() {
        mchint = MyData.get_Int("MachineSelected");
        customNumberDialog = new CustomNumberDialog(activity, Integer.MAX_VALUE);
        writeProgress = dialog.findViewById(R.id.writeProgress);
        close = dialog.findViewById(R.id.chiudi);
        ecuCo = dialog.findViewById(R.id.ecuCo);
        readEcu = dialog.findViewById(R.id.readEcu);
        writeEcu = dialog.findViewById(R.id.writeEcu);
        ck1 = dialog.findViewById(R.id.ck1);
        ck2 = dialog.findViewById(R.id.ck2);
        ck3 = dialog.findViewById(R.id.ck3);
        ck4 = dialog.findViewById(R.id.ck4);
        ck5 = dialog.findViewById(R.id.ck5);
        ck6 = dialog.findViewById(R.id.ck6);
        ck7 = dialog.findViewById(R.id.ck7);
        engD = dialog.findViewById(R.id.engD);
        riseD = dialog.findViewById(R.id.riseD);
        setAllDef = dialog.findViewById(R.id.setAllDef);
        isAutoTXT = dialog.findViewById(R.id.isAutoTXT);

        for (int i = 1; i <= 6; i++) {
            int index = i - 1;

            upperAreas[index] = findViewByName("area_valvola_" + i + "_up");
            lowerAreas[index] = findViewByName("area_valvola_" + i + "_down");

            upperBars[index] = findViewByName("bar_valvola_" + i + "_a");
            lowerBars[index] = findViewByName("bar_valvola_" + i + "_b");

            valueTexts[index] = findTextViewByName("valore_valvola_" + i);

            upperPlusButtons[index] = findTextViewByName("btn_valvola_" + i + "_a_plus");
            upperMinusButtons[index] = findTextViewByName("btn_valvola_" + i + "_a_minus");

            lowerPlusButtons[index] = findTextViewByName("btn_valvola_" + i + "_b_plus");
            lowerMinusButtons[index] = findTextViewByName("btn_valvola_" + i + "_b_minus");
        }
    }

    private void onClick() {
        setAllDef.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("CONFIRM");

            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFB,
                            (byte) 0xFB,
                            (byte) 0xFA,});

                }

            });
            builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {


                }
            });
            // CREA E MOSTRA IL DIALOG
            AlertDialog dialog = builder.show();

            // APPLICA IL FULLSCREEN
            FullscreenActivity.setFullScreen(dialog);


            return true;
        });
        customNumberDialog.dialog.setOnDismissListener(dialog -> {
            if (!hasReaded) return;


            try {
                HAMMER_ENGAGEMENT_DELAY_seconds = Integer.parseInt(engD.getText().toString());
            } catch (NumberFormatException ignorede) {

            }

            try {
                RISE_DIST_mm = Integer.parseInt(riseD.getText().toString());
            } catch (NumberFormatException ignored) {
            }


        });
        engD.setOnClickListener(v -> {
            if (!hasReaded) return;
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(engD);
            }
        });
        riseD.setOnClickListener(v -> {
            if (!hasReaded) return;
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(riseD);
            }
        });
        ck1.setOnClickListener(v -> {
            if (!hasReaded) return;
            REVERSE_PLUMB_AX_1 = !REVERSE_PLUMB_AX_1;
        });
        ck2.setOnClickListener(v -> {
            if (!hasReaded) return;
            REVERSE_PLUMB_AX_2 = !REVERSE_PLUMB_AX_2;
        });
        ck3.setOnClickListener(v -> {
            if (!hasReaded) return;
            SWAP_PLUMB_AX = !SWAP_PLUMB_AX;
        });
        ck4.setOnClickListener(v -> {
            if (!hasReaded) return;
            REVERSE_HAMMER = !REVERSE_HAMMER;
        });
        ck5.setOnClickListener(v -> {
            if (!hasReaded) return;
            REVERSE_RISE_LOW = !REVERSE_RISE_LOW;
        });
        ck6.setOnClickListener(v -> {
            if (DataSaved.REVERSE_DRILL_X == 1) {
                DataSaved.REVERSE_DRILL_X = -1;
            } else if (DataSaved.REVERSE_DRILL_X == -1) {
                DataSaved.REVERSE_DRILL_X = 1;
            }
            ck6.setChecked(DataSaved.REVERSE_DRILL_X == -1);
            MyData.push("M" + mchint + "REVERSE_DRILL_X", String.valueOf(DataSaved.REVERSE_DRILL_X));
        });
        ck7.setOnClickListener(v -> {
            if (DataSaved.REVERSE_DRILL_Y == 1) {
                DataSaved.REVERSE_DRILL_Y = -1;
            } else if (DataSaved.REVERSE_DRILL_Y == -1) {
                DataSaved.REVERSE_DRILL_Y = 1;
            }
            ck7.setChecked(DataSaved.REVERSE_DRILL_Y == -1);
            MyData.push("M" + mchint + "REVERSE_DRILL_Y", String.valueOf(DataSaved.REVERSE_DRILL_Y));
        });
        readEcu.setOnClickListener(v -> {
            hasReaded = false;
            MyDeviceManager.CanWrite(true, 1, 0x3AA, 8, new byte[]{
                    1, 1, 1, 1, 1, 1, 1, (byte) 0xFA
            });

        });
        writeEcu.setOnClickListener(v -> {

            if (isWriting) {
                return;
            }

            if (hasReaded) {
                startWriteLoop();
            } else {
                new CustomToast(activity, "Read All Parameter First").show_error();
            }
        });
        if (close != null) {
            close.setOnClickListener(view -> {
                stopWriteLoop();
                stopAutoRefresh(); // se hai aggiunto anche il refresh automatico
                dialog.dismiss();
            });
        }

        for (int i = 1; i <= 6; i++) {
            final int valve = i;
            final int index = i - 1;

            if (upperPlusButtons[index] != null ) {
                setupAutoRepeat(upperPlusButtons[index], () -> {
                    setUpperValue(valve, getUpperValue(valve) + STEP_VALUE);
                    refreshValve(valve);
                });

            }

            if (upperMinusButtons[index] != null) {
                setupAutoRepeat(upperMinusButtons[index], () -> {
                    setUpperValue(valve, getUpperValue(valve) - STEP_VALUE);
                    refreshValve(valve);
                });

            }

            if (lowerPlusButtons[index] != null ) {
                setupAutoRepeat(lowerPlusButtons[index], () -> {
                    // + aumenta il valore numerico, ad esempio 3500 -> 3600.
                    // La barra inferiore si accorcia perché il valore si avvicina al 50%.
                    setLowerValue(valve, getLowerValue(valve) - STEP_VALUE);
                    refreshValve(valve);
                });

            }

            if (lowerMinusButtons[index] != null  ) {
                setupAutoRepeat(lowerMinusButtons[index], () -> {
                    // - diminuisce il valore numerico, ad esempio 3500 -> 3400.
                    // La barra inferiore si allunga perché il valore si avvicina al 25%.
                    setLowerValue(valve, getLowerValue(valve) + STEP_VALUE);
                    refreshValve(valve);
                });

            }
        }
    }

    public void refresh() {
        for (int i = 1; i <= 6; i++) {
            refreshValve(i);
        }
    }

    public void refreshValve(int valveNumber) {
        if (valveNumber < 1 || valveNumber > 6) {
            return;
        }

        int index = valveNumber - 1;

        int upperValue = clamp(getUpperValue(valveNumber), UPPER_MIN, UPPER_MAX);
        int lowerValue = clamp(getLowerValue(valveNumber), LOWER_MIN, LOWER_MAX);

        setUpperValue(valveNumber, upperValue);
        setLowerValue(valveNumber, lowerValue);

        updateUpperBar(index, upperValue);
        updateLowerBar(index, lowerValue);
        updateValueText(index, upperValue, lowerValue);
    }

    private void updateUpperBar(int index, int value) {
        View area = upperAreas[index];
        View bar = upperBars[index];

        if (area == null || bar == null) {
            return;
        }

        int areaHeight = area.getHeight();
        if (areaHeight <= 0) {
            area.post(() -> updateUpperBar(index, getUpperValue(index + 1)));
            return;
        }

        int clampedValue = clamp(value, UPPER_MIN, UPPER_MAX);
        float ratio = (float) (clampedValue - CENTER_VALUE) / (float) (UPPER_MAX - CENTER_VALUE);

        int barHeight = Math.round(areaHeight * ratio);
        setViewHeight(bar, barHeight);
    }

    private void updateLowerBar(int index, int value) {
        View area = lowerAreas[index];
        View bar = lowerBars[index];

        if (area == null || bar == null) {
            return;
        }

        int areaHeight = area.getHeight();
        if (areaHeight <= 0) {
            area.post(() -> updateLowerBar(index, getLowerValue(index + 1)));
            return;
        }

        int clampedValue = clamp(value, LOWER_MIN, LOWER_MAX);
        float ratio = (float) (CENTER_VALUE - clampedValue) / (float) (CENTER_VALUE - LOWER_MIN);

        int barHeight = Math.round(areaHeight * ratio);
        setViewHeight(bar, barHeight);
    }

    private void updateValueText(int index, int upperValue, int lowerValue) {
        TextView valueText = valueTexts[index];
        if (valueText == null) {
            return;
        }

        valueText.setText("A " + formatPercent(upperValue) + "\nB " + formatPercent(lowerValue));
    }

    private String formatPercent(int value) {
        float percent = value / 100.0f;

        if (value % 100 == 0) {
            return String.format(java.util.Locale.US, "%.0f%%", percent);
        } else {
            return String.format(java.util.Locale.US, "%.1f%%", percent);
        }
    }

    private void setViewHeight(View view, int height) {
        if (view == null) {
            return;
        }

        if (height < 0) {
            height = 0;
        }

        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }

        if (params.height != height) {
            params.height = height;
            view.setLayoutParams(params);
        }
    }

    private View findViewByName(String idName) {
        int id = getId(idName);
        if (id == 0) {
            return null;
        }
        return dialog.findViewById(id);
    }

    private TextView findTextViewByName(String idName) {
        View view = findViewByName(idName);
        if (view instanceof TextView) {
            return (TextView) view;
        }
        return null;
    }

    private int getId(String idName) {
        return activity.getResources().getIdentifier(idName, "id", activity.getPackageName());
    }

    private void initDefaultsIfNeeded() {
        if (EV1_UPPER == 0) EV1_UPPER = 6500;
        if (EV1_LOWER == 0) EV1_LOWER = 3500;

        if (EV2_UPPER == 0) EV2_UPPER = 6500;
        if (EV2_LOWER == 0) EV2_LOWER = 3500;

        if (EV3_UPPER == 0) EV3_UPPER = 6500;
        if (EV3_LOWER == 0) EV3_LOWER = 3500;

        if (EV4_UPPER == 0) EV4_UPPER = 6500;
        if (EV4_LOWER == 0) EV4_LOWER = 3500;

        if (EV5_UPPER == 0) EV5_UPPER = 6500;
        if (EV5_LOWER == 0) EV5_LOWER = 3500;

        if (EV6_UPPER == 0) EV6_UPPER = 6500;
        if (EV6_LOWER == 0) EV6_LOWER = 3500;
    }

    private int getUpperValue(int valveNumber) {
        switch (valveNumber) {
            case 1:
                return EV1_UPPER;
            case 2:
                return EV2_UPPER;
            case 3:
                return EV3_UPPER;
            case 4:
                return EV4_UPPER;
            case 5:
                return EV5_UPPER;
            case 6:
                return EV6_UPPER;
            default:
                return CENTER_VALUE;
        }
    }

    private int getLowerValue(int valveNumber) {
        switch (valveNumber) {
            case 1:
                return EV1_LOWER;
            case 2:
                return EV2_LOWER;
            case 3:
                return EV3_LOWER;
            case 4:
                return EV4_LOWER;
            case 5:
                return EV5_LOWER;
            case 6:
                return EV6_LOWER;
            default:
                return CENTER_VALUE;
        }
    }

    private void setUpperValue(int valveNumber, int value) {
        value = clamp(value, UPPER_MIN, UPPER_MAX);

        switch (valveNumber) {
            case 1:
                EV1_UPPER = value;
                break;
            case 2:
                EV2_UPPER = value;
                break;
            case 3:
                EV3_UPPER = value;
                break;
            case 4:
                EV4_UPPER = value;
                break;
            case 5:
                EV5_UPPER = value;
                break;
            case 6:
                EV6_UPPER = value;
                break;
        }
    }

    private void setLowerValue(int valveNumber, int value) {
        value = clamp(value, LOWER_MIN, LOWER_MAX);

        switch (valveNumber) {
            case 1:
                EV1_LOWER = value;
                break;
            case 2:
                EV2_LOWER = value;
                break;
            case 3:
                EV3_LOWER = value;
                break;
            case 4:
                EV4_LOWER = value;
                break;
            case 5:
                EV5_LOWER = value;
                break;
            case 6:
                EV6_LOWER = value;
                break;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (dialog != null && dialog.isShowing() && autoRefreshEnabled) {
                refresh();
                switch (DRILL_STATUS) {
                    case 0:
                        isAutoTXT.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                        isAutoTXT.setTextColor(activity.getResources().getColor(R.color._____cancel_text));
                        break;
                    case 1:
                    case 2:
                    case 3:
                        isAutoTXT.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                        isAutoTXT.setTextColor(activity.getResources().getColor(R.color.light_yellow));
                        break;

                    default:
                        isAutoTXT.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                        isAutoTXT.setTextColor(activity.getResources().getColor(R.color._____cancel_text));
                        break;

                }
                ck1.setChecked(REVERSE_PLUMB_AX_1);
                ck2.setChecked(REVERSE_PLUMB_AX_2);
                ck3.setChecked(SWAP_PLUMB_AX);
                ck4.setChecked(REVERSE_HAMMER);
                ck5.setChecked(REVERSE_RISE_LOW);
                ck6.setChecked(DataSaved.REVERSE_DRILL_X == -1);
                ck7.setChecked(DataSaved.REVERSE_DRILL_Y == -1);
                if (!hasReaded) {
                    writeEcu.setVisibility(View.INVISIBLE);
                } else {
                    writeEcu.setVisibility(View.VISIBLE);
                }
                if (ECU_Connected) {
                    ecuCo.setImageTintList(activity.getColorStateList(R.color.green));
                } else {
                    ecuCo.setImageTintList(activity.getColorStateList(R.color.red));
                }
                if (!customNumberDialog.dialog.isShowing()) {
                    engD.setText(String.valueOf(HAMMER_ENGAGEMENT_DELAY_seconds));
                    riseD.setText(String.valueOf(RISE_DIST_mm));
                }
                uiHandler.postDelayed(this, 100);
            }
        }
    };

    private void startAutoRefresh() {
        autoRefreshEnabled = true;
        uiHandler.removeCallbacks(autoRefreshRunnable);
        uiHandler.post(autoRefreshRunnable);
    }

    private void stopAutoRefresh() {
        autoRefreshEnabled = false;
        uiHandler.removeCallbacks(autoRefreshRunnable);
    }

    private final Runnable writeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isWriting) {
                return;
            }

            writeProgress.setProgress(writeCounter);

            // Qui puoi inserire la scrittura CAN step-by-step
            writeParameterStep(writeCounter);

            if (writeCounter >= 40) {

                isWriting = false;
                hasReaded = false;

                writeProgress.setProgress(40);

                writeProgress.postDelayed(() -> {
                    writeProgress.setVisibility(View.GONE);
                    writeProgress.setProgress(0);
                }, 300);

                new CustomToast(activity, "Parameters Written").show();
                return;
            }

            writeCounter++;
            writeHandler.postDelayed(this, 100);
        }
    };

    private void writeParameterStep(int step) {
        Log.d("DioCADD", writeCounter + "  " + step);
        switch (step) {
            case 0:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV1_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 1:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV1_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 2:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV2_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 3:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV2_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 4:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV3_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 5:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV3_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 6:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV4_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 7:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV4_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 8:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV5_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 9:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV5_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 10:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV6_UPPER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 11:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(EV6_LOWER);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 12:
                int val = 0;
                if (SWAP_PLUMB_AX) {
                    val = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 13:
                int val13 = 0;
                if (REVERSE_FOOT_ENCODER) {
                    val13 = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val13);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 14:
                int val14 = 0;
                if (REVERSE_PLUMB_AX_1) {
                    val14 = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val14);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;
            case 15:
                int val15 = 0;
                if (REVERSE_PLUMB_AX_2) {
                    val15 = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val15);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 16:
                int val16 = 0;
                if (REVERSE_HAMMER) {
                    val16 = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val16);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 17:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(HAMMER_ENGAGEMENT_DELAY_seconds);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });

                break;

            case 18:
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(RISE_DIST_mm);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;

            case 19:
                int val19 = 0;
                if (REVERSE_RISE_LOW) {
                    val19 = 1;
                }
                bytes = PLC_DataTypes_LittleEndian.U16_to_bytes(val19);
                MyDeviceManager.CanWrite(true, 1, 0x3AB, 8, new byte[]{
                        (byte) step,
                        bytes[0],
                        bytes[1],
                        0,
                        0,
                        0,
                        0,
                        (byte) 0xFA

                });
                break;


            // ...


        }
    }

    private void startWriteLoop() {
        isWriting = true;
        writeCounter = -1;

        if (writeProgress != null) {
            writeProgress.setMax(40);
            writeProgress.setProgress(0);
            writeProgress.setVisibility(View.VISIBLE);
        }

        writeHandler.removeCallbacks(writeRunnable);
        writeHandler.post(writeRunnable);
    }

    private void stopWriteLoop() {
        isWriting = false;
        writeHandler.removeCallbacks(writeRunnable);

        if (writeProgress != null) {
            writeProgress.setVisibility(View.GONE);
            writeProgress.setProgress(0);
        }
    }

    private void setupAutoRepeat(TextView button, Runnable action) {
        button.setOnClickListener(v -> {
            if(!hasReaded)return;
            action.run();
        });


        button.setOnLongClickListener(v -> {
            if(hasReaded) {
                isRepeating = true;


                // Primo ritardo di 500ms prima di iniziare la ripetizione
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRepeating) {
                            action.run();
                            handler.postDelayed(this, 50); // ripeti ogni 50ms
                        }
                    }
                }, 500);
            }
            return true; // segnala che il long click è gestito
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isRepeating) {


                    }
                    isRepeating = false; // stop

                    break;
            }
            return false;
        });
    }

}
