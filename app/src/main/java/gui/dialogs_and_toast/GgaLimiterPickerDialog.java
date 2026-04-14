package gui.dialogs_and_toast;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import utils.FullscreenActivity;

public class GgaLimiterPickerDialog {

    public interface Callback {
        /**
         * valueSec:
         * 0 = OFF
         * 1, 5, 10, 15, 30, 60 = intervallo in secondi
         */
        void onSelected(int valueSec, String label);

        void onCancelled();
    }

    private AlertDialog dialog;

    // 0 = OFF
    private final int[] valuesSec = new int[]{0, 1, 5, 10, 15, 30, 60};
    private final String[] labels = new String[]{"OFF", "1S", "5S", "10S", "15S", "30S", "60S"};

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void show(Activity activity, Callback callback) {
        if (activity == null || activity.isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("GGA limiter")
                .setItems(labels, (d, which) -> {
                    if (callback != null) {
                        callback.onSelected(valuesSec[which], labels[which]);
                    }
                    d.dismiss();
                })
                .setNegativeButton("Back", (d, which) -> {
                    if (callback != null) {
                        callback.onCancelled();
                    }
                });


        dialog = builder.create();
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
    }
}