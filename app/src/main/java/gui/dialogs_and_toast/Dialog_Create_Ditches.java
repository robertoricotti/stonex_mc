package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stx_dig.R;

import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Create_Ditches {
    Activity activity;
    public Dialog dialog;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    DisplayMetrics displayMetrics;
    int larg = 1000, alt = 600;
    int units;
    Guideline toppla, bottla;

    TextView LW_DS, RW_DS;
    TextView p1p2L, p2p3L, p3p4L, p4p5L, p5p6L;

    public ImageView close;

    public EditText etLW, etLS;
    public EditText etRW, etRS;

    public EditText etp1p2L, etp1p2S;
    public EditText etp2p3L, etp2p3S;
    public EditText etp3p4L, etp3p4S;
    public EditText etp4p5L, etp4p5S;
    public EditText etp5p6L, etp5p6S;
    public Button btn_piu, btn_meno;
    public EditText etHDT;

    public Dialog_Create_Ditches(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        customNumberDialog = new CustomNumberDialog(activity, -1);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -1);
        displayMetrics = new DisplayMetrics();

    }
    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.35);
        alt = (int) (displayMetrics.heightPixels * 1);
        dialog.create();
        dialog.setContentView(R.layout.dialog_create_ditches);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.LEFT;
            wlp.dimAmount = 0.45f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        dialog.getWindow().setLayout(larg, WindowManager.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.LEFT;

        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();

    }
    private void findView() {
         units = MyData.get_Int("Unit_Of_Measure");

        toppla = dialog.findViewById(R.id.toppla);
        bottla = dialog.findViewById(R.id.bottla);

        close = dialog.findViewById(R.id.close);

        LW_DS = dialog.findViewById(R.id.LW_DS);
        etLW = dialog.findViewById(R.id.etLW);
        etLS = dialog.findViewById(R.id.etLS);

        RW_DS = dialog.findViewById(R.id.RW_DS);
        etRW = dialog.findViewById(R.id.etRW);
        etRS = dialog.findViewById(R.id.etRS);

        p1p2L = dialog.findViewById(R.id.p1p2L);
        etp1p2L = dialog.findViewById(R.id.etp1p2L);
        etp1p2S = dialog.findViewById(R.id.etp1p2S);

        p2p3L = dialog.findViewById(R.id.p2p3L);
        etp2p3L = dialog.findViewById(R.id.etp2p3L);
        etp2p3S = dialog.findViewById(R.id.etp2p3S);

        p3p4L = dialog.findViewById(R.id.p3p4L);
        etp3p4L = dialog.findViewById(R.id.etp3p4L);
        etp3p4S = dialog.findViewById(R.id.etp3p4S);

        p4p5L = dialog.findViewById(R.id.p4p5L);
        etp4p5L = dialog.findViewById(R.id.etp4p5L);
        etp4p5S = dialog.findViewById(R.id.etp4p5S);

        p5p6L = dialog.findViewById(R.id.p5p6L);
        etp5p6L = dialog.findViewById(R.id.etp5p6L);
        etp5p6S = dialog.findViewById(R.id.etp5p6S);
        btn_piu = dialog.findViewById(R.id.btn_piu);
        btn_meno = dialog.findViewById(R.id.btn_meno);
        etHDT = dialog.findViewById(R.id.etHDT);


    }
    private void onClick() {
      /*  close.setOnClickListener(v -> {
            dialog.dismiss();
        });*/

        setLengthEditTextClick(etLW);
        setLengthEditTextClick(etRW);

        setLengthEditTextClick(etp1p2L);
        setLengthEditTextClick(etp2p3L);
        setLengthEditTextClick(etp3p4L);
        setLengthEditTextClick(etp4p5L);
        setLengthEditTextClick(etp5p6L);

        setSimpleNumberEditTextClick(etLS);
        setSimpleNumberEditTextClick(etRS);

        setSimpleNumberEditTextClick(etp1p2S);
        setSimpleNumberEditTextClick(etp2p3S);
        setSimpleNumberEditTextClick(etp3p4S);
        setSimpleNumberEditTextClick(etp4p5S);
        setSimpleNumberEditTextClick(etp5p6S);

        setSimpleNumberEditTextClick(etHDT);
    }

    private void setLengthEditTextClick(EditText editText) {
        editText.setOnClickListener(view -> {
            if (units == 0 || units == 1 || units == 2 || units == 3 || units == 6 || units == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(editText);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(editText);
                }
            }
        });
    }

    private void setSimpleNumberEditTextClick(EditText editText) {
        editText.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(editText);
            }
        });
    }
}
