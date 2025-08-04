package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import gui.projects.Dialog_Edita_Punti3D;
import gui.projects.Punti3DAdapter;
import gui.tech_menu.SlideBoomActivity;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.Utils;

public class CustomNumberDialog {
    Activity activity;
    EditText realValue;
    public Dialog dialog;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bminus, bdel, bok;
    EditText value;
    boolean c = true;
    int flag;
    int dec = 0;
    int mode = -1;


    public CustomNumberDialog(Activity activity, int flag) {
        this.flag = flag;
        this.activity = activity;
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_number);


    }

    public void show(EditText realValue) {
        dec = 0;
        this.realValue = realValue;
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(900, 650);
        } else {
            dialog.getWindow().setLayout(800, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }


    public void show(EditText realValue, int dec) {
        this.dec = dec;
        this.realValue = realValue;
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(900, 650);
        } else {
            dialog.getWindow().setLayout(800, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    public void show(EditText realValue, int dec, int mode) {
        this.dec = dec;
        this.mode = mode;
        this.realValue = realValue;
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(900, 650);
        } else {
            dialog.getWindow().setLayout(800, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        value.setText(realValue.getText().toString());
        onClick();
        onLongClick();
    }

    private void init() {
        c = true;

        if (dec > 0) {
            value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(dec)});
        }

        try {

            if (dec == 0) {
                value.setText(realValue.getText().toString());
            } else {
                value.setText(String.format("%0" + dec + "d", Integer.parseInt(realValue.getText().toString())));
            }
        } catch (NumberFormatException e) {
            value.setText("");
        }


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
        bdot = dialog.findViewById(R.id.bdot);
        bdel = dialog.findViewById(R.id.bdel);
        bminus = dialog.findViewById(R.id.bminus);
        bok = dialog.findViewById(R.id.bok);


    }

    private void onClick() {

        b1.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("1"));
        });

        b2.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("2"));
        });

        b3.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("3"));
        });

        b4.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("4"));
        });

        b5.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("5"));
        });

        b6.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("6"));
        });

        b7.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("7"));
        });

        b8.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("8"));
        });

        b9.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("9"));
        });

        b0.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("0"));
        });

        bdot.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("."));
        });

        bminus.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (!value.getText().toString().contains("-")) {
                value.setText(value.getText().insert(0, "-"));
            }
        });

        bdel.setOnClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
        });

        bok.setOnClickListener((View v) -> {
            if (flag != 111) {
                if (value.getText().toString().equals("") || value.getText().toString().equals("-") || value.getText().toString().equals("")) {
                    new CustomToast(activity, "Error Input!").show_error();
                } else {
                    if (dec == 0) {
                        realValue.setText(value.getText().toString());
                        try {
                            if (flag == -1) {
                                DataSaved.line_Offset = Double.parseDouble(Utils.writeMetri(value.getText().toString()));
                            }


                        } catch (NumberFormatException ignored) {

                        }

                    } else {

                        try {
                            realValue.setText(String.format("%0" + dec + "d", Integer.parseInt(value.getText().toString())));

                        } catch (NumberFormatException e) {
                            realValue.setText(value.getText().toString());
                            new CustomToast(activity, e.toString()).show_error();
                        }
                    }

                    if (activity instanceof SlideBoomActivity) {

                        ((SlideBoomActivity) activity).save();
                    }

                    c = true;
                    dec = 0;
                    dialog.dismiss();


                }

            } else {
                if (value.getText().toString().equals("") || value.getText().toString().equals("-") || value.getText().toString().equals("")) {
                    new CustomToast(activity, "Error Input!").show_error();
                } else {
                    if (mode == 0) {
                        //Est
                        Punti3DAdapter.punti3DList.get(dec).setX(Double.parseDouble(Utils.writeMetri(value.getText().toString())));
                    }
                    if (mode == 1) {
                        //Nord
                        Punti3DAdapter.punti3DList.get(dec).setY(Double.parseDouble(Utils.writeMetri(value.getText().toString())));
                    }
                    if (mode == 2) {
                        //Quota
                        Punti3DAdapter.punti3DList.get(dec).setZ(Double.parseDouble(Utils.writeMetri(value.getText().toString())));
                    }

                    Dialog_Edita_Punti3D.aggiornaLista();

                    c = true;
                    dec = 0;
                    mode = -1;
                    dialog.dismiss();

                }

            }
        });
    }


    private void onLongClick() {
        bdel.setOnLongClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText("");
            return true;
        });
    }


}
