package gui.profiles;

import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class SetWidthAndSlope {
    Activity activity;
    public Dialog dialog;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bminus, bdel, bok;
    EditText value, value_ft, value_in;

    int indexPoint;
    int indexType;
    boolean c = true;

    int indexMeasure;

    int indexFtIn = 0;


    public SetWidthAndSlope(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
    }

    public void show(int indexLP, int indexType) {
        this.indexPoint = indexLP;
        this.indexType = indexType;
        if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
            dialog.setContentView(R.layout.dialog_number_ft_in);

        }
        else {

            dialog.setContentView(R.layout.dialog_number);

        }
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if(Build.BRAND.equals("SRT8PROS")){
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                dialog.getWindow().setLayout(1000, 650);
            }
            else {
                dialog.getWindow().setLayout(900, 650);
            }
        }
        else {
            dialog.getWindow().setLayout(800, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    private void init(){
        if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
            indexFtIn = 0;
            value_ft.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_yellow));
            value_in.setBackgroundColor(Color.TRANSPARENT);
            String depth = ((ProfileCalibManual)activity).width[indexPoint - 1];
            if(depth.length() > 0){
                value_ft.setText(depth.split("'")[0].trim());
                value_in.setText(depth.split("'")[1].trim());
            }
            else {
                value_ft.setText("");
                value_in.setText("");
            }
        }
        else {
            switch (indexType){
                case 1:
                    value.setText(((ProfileCalibManual)activity).width[indexPoint - 1]);
                    break;
                case 2:
                    value.setText(((ProfileCalibManual)activity).slope[indexPoint - 1]);
                    break;
            }
        }

    }

    @SuppressLint("CutPasteId")
    private void findView(){
        value = dialog.findViewById(R.id.value);
        value_ft = dialog.findViewById(R.id.value);
        value_in = dialog.findViewById(R.id.value2);
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
        bdot = dialog.findViewById(R.id.bdot);
        bdel = dialog.findViewById(R.id.bdel);
        bminus = dialog.findViewById(R.id.bminus);
        bok = dialog.findViewById(R.id.bok);
    }

    private void onClick(){
        if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
            value_ft.setOnClickListener((View v) -> {
                indexFtIn = 0;
                c = true;
                value_ft.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_yellow));
                value_in.setBackgroundColor(Color.TRANSPARENT);
            });

            value_in.setOnClickListener((View v) -> {
                indexFtIn = 1;
                c = true;
                value_in.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_yellow));
                value_ft.setBackgroundColor(Color.TRANSPARENT);
            });

        }

        b1.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("1"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("1"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("1"));
            }

        });

        b2.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("2"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("2"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("2"));
            }

        });

        b3.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("3"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("3"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("3"));
            }

        });

        b4.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("4"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("4"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("4"));
            }

        });

        b5.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("5"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("5"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("5"));
            }

        });

        b6.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("6"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("6"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("6"));
            }

        });

        b7.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("7"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("7"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("7"));
            }

        });

        b8.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("8"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("8"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("8"));
            }

        });

        b9.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("9"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("9"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("9"));
            }

        });

        b0.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("0"));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("0"));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("0"));
            }

        });

        bdot.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(indexFtIn == 0){
                    value_ft.setText(value_ft.getText().toString().concat("."));
                }
                else {
                    value_in.setText(value_in.getText().toString().concat("."));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("."));
            }

        });

        bminus.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(c){
                    if(indexFtIn == 0){
                        value_ft.setText("");
                    }
                    else {
                        value_in.setText("");
                    }
                    c = false;
                }
                if(!value_ft.getText().toString().contains("-")){
                    value_ft.setText(value_ft.getText().insert(0, "-"));
                }
                else {
                    value_ft.setText(value_ft.getText().toString().replace("-", ""));
                }
            }
            else {
                if(c){
                    value.setText("");
                    c = false;
                }
                if(!value.getText().toString().contains("-")){
                    value.setText(value.getText().insert(0, "-"));
                }
                else {
                    value.setText(value.getText().toString().replace("-", ""));
                }
            }
        });

        bdel.setOnClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(indexFtIn == 0){
                    if(value_ft.getText().toString().length() > 0)
                        value_ft.setText(value_ft.getText().toString().substring(0, value_ft.getText().toString().length() - 1));
                }
                else {
                    if(value_in.getText().toString().length() > 0)
                        value_in.setText(value_in.getText().toString().substring(0, value_in.getText().toString().length() - 1));
                }
            }
            else {
                if(value.getText().toString().length() > 0)
                    value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
            }

        });

        bok.setOnClickListener((View v) ->{
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(Utils.isNumeric(value_ft.getText().toString()) && Utils.isNumeric(value_in.getText().toString())){
                    ((ProfileCalibManual)activity).width[indexPoint - 1] = value_ft.getText().toString() + "' " + value_in.getText().toString();
                }
            }
            else {
                if(Utils.isNumeric(value.getText().toString())){
                    switch (indexType){
                        case 1:
                            ((ProfileCalibManual)activity).width[indexPoint - 1] = value.getText().toString();
                            break;
                        case 2:
                            ((ProfileCalibManual)activity).slope[indexPoint - 1] = value.getText().toString();
                            break;
                    }
                }
            }
            c = true;
            dialog.dismiss();

        });


    }

    private void onLongClick(){
        bdel.setOnLongClickListener((View v) -> {
            if((indexMeasure == 4 || indexMeasure == 5) && indexType == 1){
                if(indexFtIn == 0){
                    if(value_ft.getText().toString().length() > 0)
                        value_ft.setText("");
                }
                else {
                    if(value_in.getText().toString().length() > 0)
                        value_in.setText("");
                }
            }
            else {
                if(value.getText().toString().length() > 0)
                    value.setText("");
            }
            return true;
        });


    }

}
