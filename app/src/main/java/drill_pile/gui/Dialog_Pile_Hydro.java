package drill_pile.gui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.Objects;

import gui.dialogs_and_toast.CustomToast;
import utils.FullscreenActivity;
import utils.MyDeviceManager;

/**
 * Dialog taratura idraulica valvole Danfoss.
 *
 * Scala valori:
 * - 0..10000 = 0..100%
 * - 6500 = 65%
 * - 3500 = 35%
 *
 * Per ogni valvola:
 * - EVx_UPPER = Porta A, barra superiore dal 50% verso 75%
 * - EVx_LOWER = Porta B, barra inferiore dal 50% verso 25%
 *
 * Range grafico:
 * - Porta A: 5000..7500
 * - Porta B: 5000..2500
 */
public class Dialog_Pile_Hydro {

    public static int EV1_UPPER=5000;
    public static int EV1_LOWER=5000;

    public static int EV2_UPPER=5000;
    public static int EV2_LOWER=5000;

    public static int EV3_UPPER=5000;
    public static int EV3_LOWER=5000;

    public static int EV4_UPPER=5000;
    public static int EV4_LOWER=5000;

    public static int EV5_UPPER=5000;
    public static int EV5_LOWER=5000;

    public static int EV6_UPPER=5000;
    public static int EV6_LOWER=5000;

    private static final int CENTER_VALUE = 5000;

    private static final int UPPER_MIN = 5000;
    private static final int UPPER_MAX = 7500;

    private static final int LOWER_MIN = 2500;
    private static final int LOWER_MAX = 5000;

    // 100 = 1%, 50 = 0.5%, 10 = 0.1%
    private static final int STEP_VALUE = 100;

    private final Activity activity;
    public Dialog dialog;

    private ImageView close,readEcu,writeEcu;

    private final View[] upperAreas = new View[6];
    private final View[] lowerAreas = new View[6];

    private final View[] upperBars = new View[6];
    private final View[] lowerBars = new View[6];

    private final TextView[] valueTexts = new TextView[6];

    private final TextView[] upperPlusButtons = new TextView[6];
    private final TextView[] upperMinusButtons = new TextView[6];
    private final TextView[] lowerPlusButtons = new TextView[6];
    private final TextView[] lowerMinusButtons = new TextView[6];
    static boolean hasReaded=false;

    public Dialog_Pile_Hydro(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        initDefaultsIfNeeded();
    }

    public void show() {
        MyDeviceManager.CanWrite(true,1,0x294,8,new byte[]{
                1,1,1,1,1,1,1, (byte) 0xFA
        });
        dialog.create();
        dialog.setContentView(R.layout.dialog_pile_hydro);
        dialog.setCancelable(false);

        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();

        FullscreenActivity.setFullScreen(dialog);

        findView();
        onClick();

        View root = dialog.findViewById(android.R.id.content);
        if (root != null) {
            root.post(this::refresh);
        } else {
            refresh();
        }
    }

    private void findView() {
        close = dialog.findViewById(R.id.chiudi);

        readEcu=dialog.findViewById(R.id.readEcu);
        writeEcu=dialog.findViewById(R.id.writeEcu);

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
        readEcu.setOnClickListener(v -> {
            MyDeviceManager.CanWrite(true,1,0x294,8,new byte[]{
                    1,1,1,1,1,1,1, (byte) 0xFA
            });
            hasReaded=true;
        });
        writeEcu.setOnClickListener(v -> {

            if(hasReaded){

                //TODO metodo scrittura parametri
                hasReaded=false;

            }else {
                new CustomToast(activity,"Read All Parameter First").show_error();
            }
        });
        if (close != null) {
            close.setOnClickListener(view -> dialog.dismiss());
        }

        for (int i = 1; i <= 6; i++) {
            final int valve = i;
            final int index = i - 1;

            if (upperPlusButtons[index] != null) {
                upperPlusButtons[index].setOnClickListener(v -> {
                    setUpperValue(valve, getUpperValue(valve) + STEP_VALUE);
                    refreshValve(valve);
                });
            }

            if (upperMinusButtons[index] != null) {
                upperMinusButtons[index].setOnClickListener(v -> {
                    setUpperValue(valve, getUpperValue(valve) - STEP_VALUE);
                    refreshValve(valve);
                });
            }

            if (lowerPlusButtons[index] != null) {
                lowerPlusButtons[index].setOnClickListener(v -> {
                    // + aumenta il valore numerico, ad esempio 3500 -> 3600.
                    // La barra inferiore si accorcia perché il valore si avvicina al 50%.
                    setLowerValue(valve, getLowerValue(valve) - STEP_VALUE);
                    refreshValve(valve);
                });
            }

            if (lowerMinusButtons[index] != null) {
                lowerMinusButtons[index].setOnClickListener(v -> {
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
}
