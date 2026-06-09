package drill_pile.gui;

import static packexcalib.exca.ExcavatorLib.toolEndCoord;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stx_dig.R;
import java.util.ArrayList;
import java.util.List;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.HeadingDialog;
import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import utils.Utils;

public class AddPattern_Activity extends AppCompatActivity {
    public static List<Point3D_Drill> creazione_punti = new ArrayList<>();
    ImageView digMenu,navigatorHDT,remove_point,add_point,salva,edit_point;
    TextView mostraCoor;
    HeadingDialog headingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pattern);
        findView();
        onClick();

    }

    private void findView(){
        headingDialog = new HeadingDialog(this);
        digMenu=findViewById(R.id.digMenu);
        mostraCoor=findViewById(R.id.mostraCoor);
        navigatorHDT=findViewById(R.id.navigatorHDT);
        remove_point=findViewById(R.id.remove_point);
        add_point=findViewById(R.id.add_point);
        edit_point=findViewById(R.id.edit_point);
        salva=findViewById(R.id.salva);
    }
    private void onClick(){
        remove_point.setOnClickListener(v -> {
            //TODO RIMUOVERE  punto PA
        });
        add_point.setOnClickListener(v -> {
            //TODO Aggiungere punto PA che è toolEndCoord[]{X,Y,Z}
        });
        edit_point.setOnClickListener(v -> {
            //TODO appare dialog per editare i parametri del progetto
        });
        salva.setOnClickListener(v -> saveCreateSurface());

        digMenu.setOnClickListener(v -> {
            setAct();
        });
    }
    public void updateUI(){
        //questa UI è aggiornata dal thread della myApp
        if(navigatorHDT!=null){
            navigatorHDT.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        }
        if (mostraCoor != null) {
            mostraCoor.setTextColor(MyColorClass.colorConstraint);
            try {
                mostraCoor.setText("TOOL:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(toolEndCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(toolEndCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(toolEndCoord[2])));
            } catch (Exception ignored) {
                mostraCoor.setText("UNKNOWN");
            }
        }

        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        navigatorHDT.setRotation(rotBus);
        if (creazione_punti != null) {
            if (creazione_punti.size() < 1) {
                remove_point.setVisibility(View.INVISIBLE);
            } else {
                remove_point.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setAct() {
        if(creazione_punti!=null){
            if(!creazione_punti.isEmpty()) {
                if (!headingDialog.dialog.isShowing()) {
                    headingDialog.show();
                }
            }else {
                startActivity(new Intent(this, Activity_Home_Page.class));
                finish();

            }
        }

    }
    private void saveCreateSurface() {
        //TODO se dati progetto completi chiede nome e salva
    }
}