package gui.debug_ecu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomToast;
import gui.hydro.Hydro_Activity_Entering;
import services.CanService;
import utils.MyDeviceManager;
import utils.Utils;

public class Hydraulic_Setup extends BaseClass {
    boolean isSendingTest = false;
    ImageView ecuStat, back, p_piu, p_meno, image;
    ImageButton btn_piu, btn_meno, btn_test, set_all_default;
    TextView pageNum, txtFunzione, valore;
    int pageCounter;
    final int maxPage = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydraulic_setup);
        findView();
        onClick();
        updateUI();

    }

    private void findView() {
        ecuStat = findViewById(R.id.ecustatus);
        back = findViewById(R.id.back);
        p_piu = findViewById(R.id.page_piu);
        p_meno = findViewById(R.id.page_meno);
        pageNum = findViewById(R.id.txt_page);
        image = findViewById(R.id.image_function);
        btn_piu = findViewById(R.id.btn_plu);
        btn_meno = findViewById(R.id.btn_min);
        txtFunzione = findViewById(R.id.txt_funzione);
        btn_test = findViewById(R.id.btn_testH);
        valore = findViewById(R.id.testo);
        set_all_default = findViewById(R.id.set_all_defaults);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        set_all_default.setOnLongClickListener(view -> {
            //dialog di conferma
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Hydraulic_Setup.this);
            builder.setTitle("RESET ECU TO DEFAULTS VALUE");
            builder.setMessage("Do You Want to Proceed ?");
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyDeviceManager.CanWrite(1, 888, 8, new byte[]{(byte) 251, 0, 0, 0, 0, 0, 0, (byte) 253});
                new CustomToast(Hydraulic_Setup.this, "ECU RESET DONE").show_alert();

            });
            builder.setNegativeButton("NO", (dialog, which) -> {

            });

            builder.show();

            return false;
        });
        back.setOnClickListener(view -> {
            startActivity(new Intent(this, Hydro_Activity_Entering.class));
            finish();
        });

        btn_piu.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 1, 0, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 254, 0, 0, 0, 0});

                        return false;
                }
            }

            return false;
        });
        btn_meno.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 1, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 254, 0, 0, 0, 0});

                        return false;
                }
            }

            return false;
        });

        p_piu.setOnClickListener(view -> {
            if (CanService.ECU_Connected) {
                pageCounter++;
                pageCounter = pageCounter % maxPage;
                MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 254, 0, 0, 0, 0});

            } else {
                MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 0, 0, 0, (byte) 254, 0, 0, 0, 0});
            }

        });
        p_meno.setOnClickListener(view -> {
            if (CanService.ECU_Connected) {
                if (pageCounter > 0) {
                    pageCounter--;
                    pageCounter = pageCounter % maxPage;
                }
                MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 254, 0, 0, 0, 0});

            } else {
                MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 0, 0, 0, (byte) 254, 0, 0, 0, 0});

            }
        });
        btn_test.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isSendingTest = true;

                        return false;

                    case MotionEvent.ACTION_UP:
                        isSendingTest = false;
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 0, 0, 0, 0, 0});

                        return false;
                }
            }

            return false;
        });

    }

    public void updateUI() {
        if (CanService.ECU_Connected) {
            ecuStat.setImageResource(R.drawable.ok_ecu_bosch);
            ecuStat.setImageTintList(getColorStateList(R.color.green));
            if (isSendingTest) {
                MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) pageCounter, 0, 0, (byte) 0, 0, 0, 0, 1});

            }
        } else {
            ecuStat.setImageResource(R.drawable.ko_ecu_bosch);
            ecuStat.setImageTintList(getColorStateList(R.color.white));
            pageCounter = 0;
        }
        pageNum.setText(" " + (pageCounter + 1) + " / " + maxPage + "\n" + definition(pageCounter));
        txtFunzione.setText(funzione(pageCounter));

    }

    private String definition(int page) {
        switch (page) {
            case 0:
                switch (CanService.hydraMachineType) {
                    case 0:
                        image.setImageResource(R.drawable.dozer_machines_btn);
                        break;
                    case 1:
                        image.setImageResource(R.drawable.cartoon_graderr);
                        break;


                }
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                switch (CanService.hydraMachineType) {
                    case 0:
                        valore.setText("DOZER MODE");
                        break;
                    case 1:
                        valore.setText("GRADER MODE");
                        break;
                }
                return "MACHINE MODE";

            case 1:

                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                switch (CanService.valveType) {
                    case 0:
                        valore.setText("DANFOSS");
                        image.setImageResource(R.drawable.danfoss_4_vie);
                        break;
                    case 1:
                        valore.setText("PROPOR. PWM");
                        image.setImageResource(R.drawable.cetop_pwm);
                        break;
                    case 2:
                        valore.setText("ON/OFF");
                        image.setImageResource(R.drawable.cetop_onoff);
                        break;

                  /*  case 3:
                        valore.setText("CAT JOY BRIDGE");
                        image.setImageResource(R.drawable.interface_joy_bridge);
                        break;
                    case 4:
                        valore.setText("KOMATSU CAN");
                        image.setImageResource(R.drawable.komatsucan);
                        break;
                    case 5:
                        valore.setText("JOHN DEERE CAN");
                        image.setImageResource(R.drawable.jdeere);
                        break;*/
                }
                return "HYDRAULIC INTERFACE TYPE";

            case 2:
                image.setImageResource(R.drawable.lama_doze_blank_left_rise_start);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.left_Rise_min));
                return "LEFT RISE START";

            case 3:
                image.setImageResource(R.drawable.lama_doze_blank_left_rise_max);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.left_Rise_Max));
                return "LEFT RISE MAX";

            case 4:
                image.setImageResource(R.drawable.lama_doze_blank_left_lower_start);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.left_Lower_min));
                return "LEFT LOWER START";

            case 5:
                image.setRotationY(0);
                image.setImageResource(R.drawable.lama_doze_blank_left_lower_max);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.left_Lower_Max));
                return "LEFT LOWER MAX";

            case 6:

                image.setImageResource(R.drawable.reverse_cylinder);
                image.setRotationY(180);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                switch (CanService.reverseLeft) {
                    case 0:
                        valore.setText("OFF");
                        break;
                    case 1:
                        valore.setText("REVERSED");
                        break;
                }

                return "REVERSE LEFT";


            case 7:
                image.setRotationY(0);
                image.setImageResource(R.drawable.lama_doze_blank_right_rise_start);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.right_Rise_min));
                return "RIGHT RISE START";

            case 8:
                image.setImageResource(R.drawable.lama_doze_blank_right_rise_max);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.right_Rise_Max));
                return "RIGHT RISE MAX";

            case 9:
                image.setImageResource(R.drawable.lama_doze_blank_right_lower_start);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.right_Lower_min));
                return "RIGHT LOWER START";

            case 10:
                image.setImageResource(R.drawable.lama_doze_blank_right_lower_max);
                btn_test.setVisibility(View.VISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.right_Lower_Max));
                return "RIGHT LOWER MAX";

            case 11:

                image.setImageResource(R.drawable.reverse_cylinder);

                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                switch (CanService.reverseRight) {
                    case 0:
                        valore.setText("OFF");
                        break;
                    case 1:
                        valore.setText("REVERSED");
                        break;
                }

                return "REVERSE RIGHT";


            case 12:
                image.setImageResource(R.drawable.window_blade);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.hydr_Window));
                return "WINDOW";
            case 13:
                image.setImageResource(R.drawable.lama_doze_blank_gain_left);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.left_Gain));
                return "LEFT GAIN BOOST";

            case 14:
                image.setImageResource(R.drawable.lama_doze_blank_gain_right);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(String.valueOf(CanService.right_Gain));
                return "RIGHT GAIN BOOST";
            case 15:
                image.setImageResource(R.drawable.tolleranza_metri_lama);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(Utils.readSensorCalibration(String.valueOf(CanService.elevationDB * 0.001)) + " " + Utils.getMetriSimbol());
                return "ELEVATION DEADBAND";
            case 16:
                image.setImageResource(R.drawable.tolleranza_angolo_lama);
                btn_test.setVisibility(View.INVISIBLE);
                set_all_default.setVisibility(View.GONE);
                valore.setText(Utils.readAngolo(String.valueOf(CanService.slopeDB * 0.01)) + " " + Utils.getGradiSimbol());
                return "SLOPE DEADBAND";

            case 17:
                image.setImageResource(R.drawable.baseline_download_done_96);
                btn_test.setVisibility(View.GONE);
                set_all_default.setVisibility(View.VISIBLE);
                valore.setText("DONE");
                return "COMPLETED";


        }
        return "";
    }

    private String funzione(int page) {
        switch (page) {
            case 0:
                return "THIS DEFINES THE HYDRAULIC MACHINE BEHAVIOR \nFOR DOZER LEFT MEANS BLADE ELEVATION AND RIGHT MEANS BLADE TILT \nFOR GRADER LEFT AND RIGHT ARE REFERRED TO BLADE'S EDGES";

            case 1:
                return "DEFINE THE HYDRAULIC VALVE TYPE\nOR THE CAN PROTOCOL TO USE";

            case 2:
                return "RANGE 1>>100 \nMINIMUM SPEED FOR LEFT CYLINDER \nRISE";

            case 3:
                return "RANGE 100>>255 \nMAXIMUM SPEED FOR LEFT CYLINDER \nRISE";

            case 4:
                return "RANGE 1>>100 \nMINIMUM SPEED FOR LEFT CYLINDER \nLOWER";

            case 5:
                return "RANGE 100>>255 \nMAXIMUM SPEED FOR LEFT CYLINDER \nLOWER";
            case 6:
                return "REVERSE LEFT MOVEMENT";

            case 7:
                return "RANGE 1>>100 \nMINIMUM SPEED FOR RIGHT CYLINDER \nRISE";

            case 8:
                return "RANGE 100>>255 \nMAXIMUM SPEED FOR RIGHT CYLINDER \nRISE";

            case 9:
                return "RANGE 1>>100 \nMINIMUM SPEED FOR RIGHT CYLINDER \nLOWER";

            case 10:
                return "RANGE 100>>255 \nMAXIMUM SPEED FOR RIGHT CYLINDER \nLOWER";
            case 11:
                return "REVERSE RIGHT MOVEMENT";
            case 12:
                return "DEFINES THE WINDOW WITHIN WHICH THE AUTOMATIC \nCAN OPERATES";

            case 13:
                return "AMPLIFIES THE LEFT SENSOR OUTPUT TO \nTHE HYDRAULIC SUBSYSTEM";

            case 14:
                return "AMPLIFIES THE RIGHT SENSOR OUTPUT TO \nTHE HYDRAULIC SUBSYSTEM";
            case 15:
                return "ELEVATION TOLERANCE WITHIN WHICH THE HYDRAULIC IS ON HOLD \n!!ATTENTION THIS IS NOT AFFECTED BY THE VALUE SET IN USER MENU!!";
            case 16:
                return "SLOPE TOLERANCE WITHIN WHICH THE HYDRAULIC IS ON HOLD \nNOT USED IN DUAL MAST CONFIGURATION";

            case 17:
                return "COMPLETED";


        }
        return "";
    }
}