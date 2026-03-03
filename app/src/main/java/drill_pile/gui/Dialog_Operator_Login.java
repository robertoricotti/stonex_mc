package drill_pile.gui;

import static drill_pile.gui.Drill_Activity.NOME_OPERATORE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomQwertyDialog;
import utils.FullscreenActivity;

public class Dialog_Operator_Login {
    Activity activity;
    public Dialog dialog;
    Button conferma,esci;
    EditText nomeOp;
    CustomQwertyDialog customQwertyDialog;

    public Dialog_Operator_Login(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_operator_login);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //  Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.85);
        int height = (int) (displayMetrics.heightPixels * 0.8);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();
        FullscreenActivity.setFullScreen(dialog);

    }
    private void findView(){
        customQwertyDialog=new CustomQwertyDialog(activity,"");
        conferma=dialog.findViewById(R.id.conferma);
        esci=dialog.findViewById(R.id.esci);
        nomeOp=dialog.findViewById(R.id.nomeOp);

    }
    private void onClick(){
        nomeOp.setOnClickListener(view -> {
            if(!customQwertyDialog.dialog.isShowing()){
                customQwertyDialog.show(nomeOp);
            }
        });

        esci.setOnClickListener(view -> {
            NOME_OPERATORE=null;
            dialog.dismiss();
            activity.startActivity(new Intent(activity, Activity_Home_Page.class));
            activity.finish();
        });
        conferma.setOnClickListener(view -> {
            String op = (nomeOp.getText() != null) ? nomeOp.getText().toString().trim() : "";

            if (op.isEmpty()) {
                nomeOp.setError("Insert Operator Name");
                nomeOp.requestFocus();
                return; // NON chiudere
            }

            NOME_OPERATORE = op;
            dialog.dismiss();
        });

    }
}
