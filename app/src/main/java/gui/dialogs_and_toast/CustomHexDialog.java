package gui.dialogs_and_toast;



import static gui.MyApp.isApollo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import utils.FullscreenActivity;

public class CustomHexDialog {

    Activity activity;
    EditText realValue;
    public Dialog dialog;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdots, ba, bb, bc, bd, be, bf, bdel,bok;
    EditText value;

    boolean c = true;


    public CustomHexDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        if(isApollo){
        dialog.setContentView(R.layout.dialog_hex);}
        else {
            dialog.setContentView(R.layout.dialog_hex_s80);
        }
    }

    public void show(EditText realValue) {
        this.realValue = realValue;
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if(Build.BRAND.equals("SRT8PROS")){
            dialog.getWindow().setLayout(1100, 650);}
        else {
            dialog.getWindow().setLayout(1000, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    private void init(){
        value.setText(realValue.getText().toString());
    }

    private void findView(){
        value = dialog.findViewById(R.id.value);
        b1 = dialog.findViewById(R.id.b1);
        b2 = dialog.findViewById(R.id.b2);
        b3 = dialog.findViewById(R.id.b3);
        b4 = dialog.findViewById(R.id.b4);
        b5 = dialog.findViewById(R.id.b5);
        b6 = dialog.findViewById(R.id.b6);
        b7 = dialog.findViewById(R.id.b7);
        b8 = dialog.findViewById(R.id.b8);
        b9 = dialog.findViewById(R.id.b9);
        b0 = dialog.findViewById(R.id.b0);
        bdots = dialog.findViewById(R.id.bdots);
        ba = dialog.findViewById(R.id.ba);
        bb = dialog.findViewById(R.id.bb);
        bc = dialog.findViewById(R.id.bc);
        bd = dialog.findViewById(R.id.bd);
        be = dialog.findViewById(R.id.be);
        bf = dialog.findViewById(R.id.bf);
        bdel = dialog.findViewById(R.id.bdel);
        bok=dialog.findViewById(R.id.bok);
    }

    private void onClick(){
        bok.setOnClickListener(view -> {
            realValue.setText(value.getText().toString().toUpperCase());
            dialog.dismiss();
        });
        b1.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("1"));
        });

        b2.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("2"));
        });

        b3.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("3"));
        });

        b4.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("4"));
        });

        b5.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("5"));
        });

        b6.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("6"));
        });

        b7.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("7"));
        });

        b8.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("8"));
        });

        b9.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("9"));
        });

        b0.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("0"));
        });

        bdots.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat(":"));
        });

        ba.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("A"));
        });

        bb.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("B"));
        });

        bc.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("C"));
        });

        bd.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("D"));
        });

        be.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("E"));
        });

        bf.setOnClickListener((View v) -> {
            if(c){
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("F"));
        });

        bdel.setOnClickListener((View v) -> {
            if(value.getText().toString().length() > 0)
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
        });


    }

    private void onLongClick(){
        bdel.setOnLongClickListener((View v) -> {
            if(value.getText().toString().length() > 0)
                value.setText("");
            return true;
        });
    }

}
