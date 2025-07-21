package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import gui.gps.Nuovo_Gps;
import gui.tech_menu.CanOpenTSM;
import gui.tech_menu.ExcavatorChooserActivity;
import gui.tech_menu.MachineSettings;
import gui.tech_menu.Nuova_Machine_Settings;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogPassword {
    int indexMachineSelected;
    Activity activity;
    public Dialog dialog;
    EditText value;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdel, bok;
    Button bq, bw, be, br, bt, by, bu, bi, bo, bp;
    Button ba, bs, bd, bf, bg, bh, bj, bk, bl;
    Button bz, bx, bc, bv, bb, bn, bm, space;
    String tmp = "";
    Button maiuscola;
    boolean capital=true;
    int whoCall=-1;



    public static boolean isTech ;
    public static boolean isTech2 ;

    public DialogPassword(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.setContentView(R.layout.dialog_qwerty_apollo8);

        } else if(Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("SRT7PROS")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("qti")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")){
            dialog.setContentView(R.layout.dialog_qwerty_apollo7);
        }
    }

    public void show(int whoCall) {
        this.whoCall=whoCall;
        FullscreenActivity.setFullScreen(dialog);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 650);
        } else {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 500);
        }


        findView();
        init();
        onClick();
        onLongClick();
    }

    private void findView() {
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


        bq = dialog.findViewById(R.id.bq);
        bw = dialog.findViewById(R.id.bw);
        be = dialog.findViewById(R.id.be);
        br = dialog.findViewById(R.id.br);
        bt = dialog.findViewById(R.id.bt);
        by = dialog.findViewById(R.id.by);
        bu = dialog.findViewById(R.id.bu);
        bi = dialog.findViewById(R.id.bi);
        bo = dialog.findViewById(R.id.bo);
        bp = dialog.findViewById(R.id.bp);

        ba = dialog.findViewById(R.id.ba);
        bs = dialog.findViewById(R.id.bs);
        bd = dialog.findViewById(R.id.bd);
        bf = dialog.findViewById(R.id.bf);
        bg = dialog.findViewById(R.id.bg);
        bh = dialog.findViewById(R.id.bh);
        bj = dialog.findViewById(R.id.bj);
        bk = dialog.findViewById(R.id.bk);
        bl = dialog.findViewById(R.id.bl);

        bz = dialog.findViewById(R.id.bz);
        bx = dialog.findViewById(R.id.bx);
        bc = dialog.findViewById(R.id.bc);
        bv = dialog.findViewById(R.id.bv);
        bb = dialog.findViewById(R.id.bb);
        bn = dialog.findViewById(R.id.bn);
        bm = dialog.findViewById(R.id.bm);

        bdel = dialog.findViewById(R.id.bdel);
        bok = dialog.findViewById(R.id.bok);
        space = dialog.findViewById(R.id.space);
        maiuscola=dialog.findViewById(R.id.maiuscolo);
        maiuscola.setVisibility(View.GONE);
    }

    private void init() {
        tmp="";
        indexMachineSelected = MyData.get_Int("MachineSelected");
        value.setText(tmp);
       /* if (isTech) {
            if (activity instanceof ExcavatorChooserActivity) {
                activity.startActivity(new Intent(activity, MachineSettings.class));
                activity.finish();
                tmp = "";
                dialog.dismiss();
            }
        }*/
        setupChar(true);


    }

    private void onClick() {
        bok.setOnClickListener((View v) -> {


            if (tmp.equals("000000") || tmp.equals("QWEDSAZXC") && !isTech) {
                isTech = true;
                if(whoCall==1){
                    activity.startActivity(new Intent(activity, Nuovo_Gps.class));
                    activity.finish();
                    tmp = "";
                    dialog.dismiss();
                }else if (whoCall==2){
                    activity.startActivity(new Intent(activity, Nuova_Machine_Settings.class));
                    activity.finish();
                    tmp = "";
                    dialog.dismiss();
                }else {
                    dialog.dismiss();
                }

                dialog.dismiss();
            } else if (tmp.equals("696969") || tmp.equals("1234567890") && !isTech2 && activity instanceof Nuova_Machine_Settings) {

                isTech2 = true;
                if (activity instanceof Nuova_Machine_Settings) {
                    activity.startActivity(new Intent(activity, CanOpenTSM.class));
                    activity.finish();

                }

                tmp = "";
                dialog.dismiss();
            } else {
                new CustomToast(activity, "PASSWORD ERROR!").show_error();
                isTech = false;
                isTech2 = false;
                tmp = "";
                dialog.dismiss();

            }
        });
        space.setOnClickListener(view -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "_";
        });



        b1.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "1";
        });

        b2.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "2";
        });

        b3.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "3";
        });

        b4.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "4";
        });

        b5.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "5";
        });

        b6.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "6";
        });

        b7.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "7";
        });

        b8.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "8";
        });

        b9.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "9";
        });

        b0.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "0";
        });
        ba.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "A";
        });
        bb.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "B";
        });
        bc.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "C";
        });
        bd.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "D";
        });
        be.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "E";
        });
        bf.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "F";
        });

        bg.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "G";
        });
        bh.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "H";
        });
        bi.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "I";
        });

        bj.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "J";
        });
        bk.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "K";
        });
        bl.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "L";
        });
        bm.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "M";
        });
        bn.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "N";
        });
        bo.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "O";
        });
        bp.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "P";
        });
        bq.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "Q";
        });
        br.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "R";
        });
        bs.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "S";
        });
        bt.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "T";
        });
        bu.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "U";
        });
        bv.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "V";
        });
        bw.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "W";
        });
        bx.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "X";
        });
        by.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "Y";
        });
        bz.setOnClickListener((View v) -> {
            value.setText(value.getText().toString().concat("*"));
            tmp += "Z";
        });


        bdel.setOnClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
            if (tmp != null && tmp.length() > 0) {
                tmp = tmp.substring(0, tmp.length() - 1);
            }

        });
    }

    public void setupChar(boolean capital){
        if(capital){
            bq.setText("Q");
            bw.setText("W");
            be.setText("E");
            br.setText("R");
            bt.setText("T");
            by.setText("Y");
            bu.setText("U");
            bi.setText("I");
            bo.setText("O");
            bp.setText("P");
            ba.setText("A");
            bs.setText("S");
            bd.setText("D");
            bf.setText("F");
            bg.setText("G");
            bh.setText("H");
            bj.setText("J");
            bk.setText("K");
            bl.setText("L");
            bz.setText("Z");
            bx.setText("X");
            bc.setText("C");
            bv.setText("V");
            bb.setText("B");
            bn.setText("N");
            bm.setText("M");
        }else {
            bq.setText("q");
            bw.setText("w");
            be.setText("e");
            br.setText("r");
            bt.setText("t");
            by.setText("y");
            bu.setText("u");
            bi.setText("i");
            bo.setText("o");
            bp.setText("p");
            ba.setText("a");
            bs.setText("s");
            bd.setText("d");
            bf.setText("f");
            bg.setText("g");
            bh.setText("h");
            bj.setText("j");
            bk.setText("k");
            bl.setText("l");
            bz.setText("z");
            bx.setText("x");
            bc.setText("cbool");
            bv.setText("v");
            bb.setText("b");
            bn.setText("n");
            bm.setText("m");
        }
    }
    private void onLongClick() {

    }

}