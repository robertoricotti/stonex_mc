package gui.dialogs_and_toast;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import utils.FullscreenActivity;

public class MountpointPickerDialog {

    public interface Callback {
        void onSelected(String mountpoint);
        void onCancelled();
    }

    private AlertDialog dialog;
    private String[] currentItems = new String[0];
    private int lastSelectedIndex = -1;

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void show(Activity activity,
                     List<String> mountpoints,
                     Callback callback) {

        if (activity == null || activity.isFinishing()) return;

        currentItems = toItems(mountpoints);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Select mountpoint")
                .setSingleChoiceItems(currentItems, lastSelectedIndex, (d, which) -> {
                    lastSelectedIndex = which;
                })
                .setPositiveButton("OK", (d, which) -> {
                    if (lastSelectedIndex >= 0 && lastSelectedIndex < currentItems.length) {
                        callback.onSelected(currentItems[lastSelectedIndex]);
                    } else {
                        callback.onCancelled();
                    }
                })
                .setNegativeButton("Cancel", (d, which) -> callback.onCancelled());

        dialog = builder.create();
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
    }

    /** Updates the list while the dialog is open */
    public void updateItems(List<String> mountpoints) {
        if (!isShowing()) return;

        currentItems = toItems(mountpoints);

        // Simple and robust: update by recreating the content while keeping the selection
        // (AlertDialog does not easily expose notifyDataSetChanged without a custom adapter)
        // Here we refresh through the internal ListView adapter.
        if (dialog.getListView() != null && dialog.getListView().getAdapter() instanceof android.widget.ArrayAdapter) {
            @SuppressWarnings("unchecked")
            android.widget.ArrayAdapter<String> adapter =
                    (android.widget.ArrayAdapter<String>) dialog.getListView().getAdapter();

            adapter.clear();
            adapter.addAll(currentItems);
            adapter.notifyDataSetChanged();

            if (lastSelectedIndex >= 0 && lastSelectedIndex < currentItems.length) {
                dialog.getListView().setItemChecked(lastSelectedIndex, true);
            } else {
                lastSelectedIndex = -1;
            }
        }
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
    }

    private String[] toItems(List<String> mountpoints) {
        List<String> safe = mountpoints != null ? mountpoints : new ArrayList<>();
        return safe.toArray(new String[0]);
    }
}