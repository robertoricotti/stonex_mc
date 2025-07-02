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
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class EasyConfigDialog {

    Activity activity;

    public Dialog dialog;
    ImageButton shortcut1, shortcut2, shortcut3, shortcut4, shortcut5, shortcut6;
    CustomQwertyDialog qwertyDialog;
    CustomNumberDialog numberDialog;
    int indexShortcut;
    Button ok, exit;
    EditText name1, name2, name3, name4, name5, name6;


    public EasyConfigDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        if(isApollo) {
            dialog.setContentView(R.layout.dialog_easy_config);
        }else {
            dialog.setContentView(R.layout.dialog_easy_config_s80);
        }
    }

    public void show() {
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        if(Build.BRAND.equals("SRT8PROS")){
            dialog.getWindow().setLayout(1000, 700);
        }
        else {
            dialog.getWindow().setLayout(700, 500);
        }

        findView();
        init();
        onClick();


    }

    private  void init(){
        numberDialog = new CustomNumberDialog(activity,-1);
        qwertyDialog = new CustomQwertyDialog(activity);

        indexShortcut = MyData.get_Int( "shortcutIndex");

        setShortcut(indexShortcut);





        name1.setText(MyData.get_String("shortcutName_1"));
        name2.setText(MyData.get_String("shortcutName_2"));
        name3.setText(MyData.get_String("shortcutName_3"));
        name4.setText(MyData.get_String("shortcutName_4"));
        name5.setText(MyData.get_String("shortcutName_5"));
        name6.setText(MyData.get_String("shortcutName_6"));
    }


    private void findView(){
        shortcut1 = dialog.findViewById(R.id.shortcut1);
        shortcut2 = dialog.findViewById(R.id.shortcut2);
        shortcut3 = dialog.findViewById(R.id.shortcut3);
        shortcut4 = dialog.findViewById(R.id.shortcut4);
        shortcut5 = dialog.findViewById(R.id.shortcut5);
        shortcut6 = dialog.findViewById(R.id.shortcut6);

        name1 = dialog.findViewById(R.id.name1);
        name2 = dialog.findViewById(R.id.name2);
        name3 = dialog.findViewById(R.id.name3);
        name4 = dialog.findViewById(R.id.name4);
        name5 = dialog.findViewById(R.id.name5);
        name6 = dialog.findViewById(R.id.name6);



        ok = dialog.findViewById(R.id.ok);
        exit = dialog.findViewById(R.id.exit);
    }

    private void onClick(){
        exit.setOnClickListener((View v) -> {
            dialog.dismiss();
        });

        shortcut1.setOnClickListener((View v) -> {
            indexShortcut = 1;
            setShortcut(indexShortcut);
        });

        shortcut2.setOnClickListener((View v) -> {
            indexShortcut = 2;
            setShortcut(indexShortcut);
        });

        shortcut3.setOnClickListener((View v) -> {
            indexShortcut = 3;
            setShortcut(indexShortcut);
        });

        shortcut4.setOnClickListener((View v) -> {
            indexShortcut = 4;
            setShortcut(indexShortcut);
        });

        shortcut5.setOnClickListener((View v) -> {
            indexShortcut = 5;
            setShortcut(indexShortcut);
        });

        shortcut6.setOnClickListener((View v) -> {
            indexShortcut = 6;
            setShortcut(indexShortcut);
        });

        ok.setOnClickListener((View v) -> {
            MyData.push("shortcutIndex", String.valueOf(indexShortcut));
            DataSaved.shortcutIndex = indexShortcut;

            String slopeY = MyData.get_String("shortcutSlopeY_" + indexShortcut);
            String slopeX = MyData.get_String("shortcutSlopeX_" + indexShortcut);
            String offset =MyData.get_String("shortcutOffset_" + indexShortcut);


            DataSaved.slopeY = Double.parseDouble(slopeY.replace(",", "."));

            DataSaved.slopeX = Double.parseDouble(slopeX.replace(",", "."));

            DataSaved.offsetH = Double.parseDouble(offset) * -1;


            if(name1.getText().toString().length() >= 0){
                MyData.push("shortcutName_1", name1.getText().toString());
            }
            if(name2.getText().toString().length() >= 0){
                MyData.push("shortcutName_2", name2.getText().toString());
            }
            if(name3.getText().toString().length() >= 0){
                MyData.push("shortcutName_3", name3.getText().toString());
            }
            if(name4.getText().toString().length() >= 0){
                MyData.push("shortcutName_4", name4.getText().toString());
            }
            if(name5.getText().toString().length() >= 0){
                MyData.push("shortcutName_5", name5.getText().toString());
            }
            if(name6.getText().toString().length() >= 0){
                MyData.push("shortcutName_6", name6.getText().toString());
            }

            dialog.dismiss();

        });

        name1.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name1);
            }
        });

        name2.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name2);
            }
        });

        name3.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name3);
            }
        });

        name4.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name4);
            }
        });

        name5.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name5);
            }
        });

        name6.setOnClickListener((View v) ->{
            if(!qwertyDialog.dialog.isShowing()){
                qwertyDialog.show(name6);
            }
        });

    }



    private void setShortcut(int index){
        shortcut1.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 1 ? R.color.orange : R.color.dark_gray));
        shortcut2.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 2 ? R.color.orange : R.color.dark_gray));
        shortcut3.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 3 ? R.color.orange : R.color.dark_gray));
        shortcut4.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 4 ? R.color.orange : R.color.dark_gray));
        shortcut5.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 5 ? R.color.orange : R.color.dark_gray));
        shortcut6.setBackgroundTintList(ContextCompat.getColorStateList(activity, index == 6 ? R.color.orange : R.color.dark_gray));
    }




}


