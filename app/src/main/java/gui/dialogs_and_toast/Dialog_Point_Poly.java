package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Point_Poly {

    String strDiRicerca = "";
    ListView listView;
    Activity activity;
    public Dialog dialog;

    ImageView save, exit;
    CheckBox ckBoxPOINT, ckBoxPOLY, ckNone;
    TextView projInfo;

    EditText editText;
    CustomQwertyDialog customQwertyDialog;
    int select = 0;
    int tmpAutoSnap = 0;

    Object selectedItem;
    String myS;
    LinearLayout layoff;
    TextView lineTit;
    EditText valore;
    Button piu, meno, clear, pm;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    int larg=1000,alt=600;
    DisplayMetrics displayMetrics;
    public Dialog_Point_Poly(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        customQwertyDialog = new CustomQwertyDialog(activity,null);
        customNumberDialog = new CustomNumberDialog(activity, -1);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -1);
        displayMetrics = new DisplayMetrics();


    }


    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.9);
        alt = (int) (displayMetrics.heightPixels * 0.8);
        dialog.create();
        dialog.setContentView(R.layout.dialog_point_poly);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        dialog.getWindow().setLayout(larg, alt);
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        update();
        onClick();


    }

    private void findView() {

        save = dialog.findViewById(R.id.salva);
        exit = dialog.findViewById(R.id.dismiss);
        ckBoxPOINT = dialog.findViewById(R.id.ckAutoSnap);
        ckBoxPOLY = dialog.findViewById(R.id.ckAutoSnapPoly);
        ckNone = dialog.findViewById(R.id.ckNone);
        editText = dialog.findViewById(R.id.et_ptly);
        listView = dialog.findViewById(R.id.listView);
        projInfo = dialog.findViewById(R.id.projInfo);
        layoff = dialog.findViewById(R.id.lineoff_lay);
        lineTit = dialog.findViewById(R.id.lineTit);
        valore = dialog.findViewById(R.id.off_valore);
        piu = dialog.findViewById(R.id.offs_piu);
        meno = dialog.findViewById(R.id.offs_men);
        clear = dialog.findViewById(R.id.off_clear);
        pm = dialog.findViewById(R.id.off_pm);

    }

    private void init() {

        strDiRicerca = editText.getText().toString();



    }

    private void update() {
        int f = 0, l = 0, p = 0, l2 = 0;
        try {
            f = DataSaved.dxfFaces.size();
        } catch (Exception e) {
            f = 0;
        }
        try {
            l = DataSaved.polylines.size();
        } catch (Exception e) {
            l = 0;
        }
        try {
            l2 = DataSaved.polylines_2D.size();
        } catch (Exception e) {
            l2 = 0;
        }

        try {
            p = DataSaved.points.size();
        } catch (Exception e) {
            p = 0;
        }

        String pt = "";
        try {
            pt = DataSaved.progettoSelected.replace("/storage/emulated/0/StonexMC_V4/Projects/", "");
        } catch (Exception e) {
            pt = "null";
        }
        String pl = "";
        try {
            pl = DataSaved.progettoSelected_POLY.replace("/storage/emulated/0/StonexMC_V4/Projects/", "");
        } catch (Exception e) {
            pl = "null";
        }
        String pp = "";
        try {
            pp = DataSaved.progettoSelected_POINT.replace("/storage/emulated/0/StonexMC_V4/Projects/", "");
        } catch (Exception e) {
            pp = "null";
        }
        String pj = "";

        Log.d("werty", DataSaved.dxfFaces.size() + "");
        myS = "PROJ. NAME: " + pt + "\t\t\t" + "3D FACES: " + f + "\n" + "PROJ. NAME: " + pl + "\t\t\t" + "3D POLYLINES: " + l + "   2D POLY: " + l2 + "\n" +
                "PROJ. NAME: " + pp + "\t\t\t" + "3D POINTS: " + p + "\n" + "PROJ. NAME: " + pj + "\t\t\t";
        switch (DataSaved.isAutoSnap) {
            case 0:
                ckNone.setChecked(true);
                ckBoxPOLY.setChecked(false);
                ckBoxPOINT.setChecked(false);
                layoff.setVisibility(View.INVISIBLE);
                break;
            case 1:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(false);
                ckBoxPOINT.setChecked(true);
                layoff.setVisibility(View.INVISIBLE);
                break;

            case 2:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(true);
                ckBoxPOINT.setChecked(false);
                layoff.setVisibility(View.VISIBLE);
                break;
        }

        editText.setEnabled(DataSaved.isAutoSnap == 0);

        projInfo.setText(myS);
        lineTit.setText("LINE OFFSET " + Utils.getMetriSimbol());
        valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));

    }


    private void onClick() {
        valore.setOnClickListener(view -> {
            if(MyData.get_Int("Unit_Of_Measure")==0|
                    MyData.get_Int("Unit_Of_Measure")==1|
                    MyData.get_Int("Unit_Of_Measure")==2|
                    MyData.get_Int("Unit_Of_Measure")==3|
                    MyData.get_Int("Unit_Of_Measure")==6|
                    MyData.get_Int("Unit_Of_Measure")==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(valore);
                }
            }else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(valore);
                }
            }

        });
        pm.setOnClickListener(view -> {
            if (DataSaved.line_Offset != 0) {
                //DataSaved.lockUnlock=0;
                DataSaved.line_Offset = DataSaved.line_Offset * -1;
                valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
            }
        });
        clear.setOnClickListener(view -> {
            if(DataSaved.line_Offset!=0){
                DataSaved.lockUnlock=0;
            }
            DataSaved.line_Offset = 0;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });
        piu.setOnClickListener(view -> {
            DataSaved.line_Offset += 0.01;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });
        meno.setOnClickListener(view -> {
            DataSaved.line_Offset -= 0.01;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });

        listView.setOnItemLongClickListener((myAdapter, myView, myItemInt, mylng) -> {

            for (int i = 0; i < listView.getChildCount(); i++) {
                listView.getChildAt(i).setBackgroundColor(activity.getColor(R.color.white));
            }

            myView.setBackgroundColor(activity.getColor(R.color.yellow));
            selectedItem = listView.getItemAtPosition(myItemInt);
            if (select == 0) {
                tmpAutoSnap = 3;
            } else {
                tmpAutoSnap = 4;
            }

            return false;
        });

        ckNone.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 0;
            DataSaved.lockUnlock = 0;
            update();

        });
        ckBoxPOINT.setOnClickListener(view -> {

            DataSaved.isAutoSnap = 1;
            DataSaved.lockUnlock = 0;
            update();

        });
        ckBoxPOLY.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 2;
            DataSaved.lockUnlock = 0;
            update();
        });

        save.setOnClickListener(view -> {
            if(DataSaved.isAutoSnap==2) {
                MyData.push("line_Offset", String.valueOf(DataSaved.line_Offset));
            }
            dialog.dismiss();
        });
        exit.setOnClickListener(view -> {
            dialog.dismiss();
        });
        editText.setOnClickListener((View v) -> {
            if (!customQwertyDialog.dialog.isShowing())
                customQwertyDialog.show(editText);
        });

    }


}
