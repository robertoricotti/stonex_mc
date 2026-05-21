package drill_pile.gui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Drill_Z_Adjust {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    Activity activity;
    public Dialog dialog;
    ImageView close,btnPiu,btnMeno,btReset;
    TextView valore;
    DisplayMetrics displayMetrics;
    int larg = 1000, alt = 600;
    int units,machineSelected;
    double step=0.001;

//DataSaved.drill_Bit_Len
    //
    public Dialog_Drill_Z_Adjust(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        displayMetrics = new DisplayMetrics();

    }

    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.65);
        alt = (int) (displayMetrics.heightPixels * 0.45);
        dialog.create();
        dialog.setContentView(R.layout.dialog_drill_z_adjust);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.25f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
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
        onClick();

    }

    private void findView() {
        machineSelected=MyData.get_Int("MachineSelected");
        units = MyData.get_Int("Unit_Of_Measure");
        DataSaved.drill_Bit_Len=MyData.get_Double("M"+machineSelected+"drill_Bit_Len");
        close=dialog.findViewById(R.id.close);
        valore=dialog.findViewById(R.id.valore);
        btnPiu=dialog.findViewById(R.id.btPiu);
        btnMeno=dialog.findViewById(R.id.btMeno);
        btReset=dialog.findViewById(R.id.btReset);
    }
    private void init(){
        updateValore();


    }
    private void onClick(){
        btReset.setOnClickListener(v -> {
            new CustomToast(activity,"Long Press To Delete").show();
        });
        btReset.setOnLongClickListener(v -> {
            DataSaved.drill_Bit_Len=0;
            updateValore();
            return true;
        });
        close.setOnClickListener(v -> {
            MyData.push("M" + machineSelected + "drill_Bit_Len", Utils.writeMetri(String.valueOf(DataSaved.drill_Bit_Len).replace(",", ".")));
            dialog.dismiss();
        });
        if(btnPiu!=null) {
            setupAutoRepeat(btnPiu,() ->{
              DataSaved.drill_Bit_Len+=step;
              updateValore();

            });
        }
        if(btnMeno!=null) {
            setupAutoRepeat(btnMeno,() ->{
                DataSaved.drill_Bit_Len-=step;
                updateValore();

            });
        }
    }
    private void updateValore(){
        if(valore!=null){
            valore.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.drill_Bit_Len)));
        }
    }
    private void setupAutoRepeat(ImageView button, Runnable action) {
        button.setOnClickListener(v -> {

            action.run();
        });


        button.setOnLongClickListener(v -> {

                isRepeating = true;


                // Primo ritardo di 500ms prima di iniziare la ripetizione
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRepeating) {
                            action.run();
                            handler.postDelayed(this, 20); // ripeti ogni 50ms
                        }
                    }
                }, 500);

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
