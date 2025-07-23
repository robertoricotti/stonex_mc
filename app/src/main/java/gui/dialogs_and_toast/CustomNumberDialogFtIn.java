package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;
import static utils.Utils.isNumeric;
import static utils.Utils.isNumericInch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.projects.Dialog_Edita_Punti3D;
import gui.projects.Punti3DAdapter;
import gui.tech_menu.SlideBoomActivity;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class CustomNumberDialogFtIn {
    String fraction = "7/8";
    Activity activity;
    EditText realValue, value_ft, value_in, value_fraction;

    public Dialog dialog;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bminus, bdel, bok;
    int dec;
    int mode;
    boolean c = true;
    int indexFtIn = 0;
    int flag;


    public CustomNumberDialogFtIn(Activity activity, int flag) {
        this.flag = flag;
        this.activity = activity;
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_number_ft_in);


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
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
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
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    /////////



    private void init() {
        indexFtIn = 0;
        c = true;
        value_ft.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_yellow));
        value_in.setBackgroundColor(Color.TRANSPARENT);
        String depth = realValue.getText().toString();
        if (depth.length() > 0) {
            value_ft.setText(depth.split("'")[0].trim());
            value_in.setText(depth.split("'")[1].trim());
            value_ft.setText(depth.split("'")[0].trim());
            value_in.setText(depth.split("'")[1].trim().substring(0, 2));
            int begin = depth.trim().length() - 4;
            int end = begin + 3;
            fraction = depth.substring(begin, end);
            value_fraction.setText(fraction);
        } else {
            value_ft.setText("");
            value_in.setText("");
        }


    }

    private void findView() {
        value_ft = dialog.findViewById(R.id.value);
        value_in = dialog.findViewById(R.id.value2);
        value_fraction = dialog.findViewById(R.id.value3);
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

    @SuppressLint("SetTextI18n")
    private void onClick() {
        value_fraction.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(activity, value_fraction);
            popupMenu.getMenu().add("0/0");
            popupMenu.getMenu().add("1/8");
            popupMenu.getMenu().add("1/4");
            popupMenu.getMenu().add("3/8");
            popupMenu.getMenu().add("1/2");
            popupMenu.getMenu().add("5/8");
            popupMenu.getMenu().add("3/4");
            popupMenu.getMenu().add("7/8");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getTitle().toString()) {
                        case "0/0":
                            fraction = "0/0";
                            value_fraction.setText(fraction);
                            return true;

                        case "1/8":
                            fraction = "1/8";
                            value_fraction.setText(fraction);
                            return true;
                        case "1/4":
                            fraction = "1/4";
                            value_fraction.setText(fraction);
                            return true;
                        case "3/8":
                            fraction = "3/8";
                            value_fraction.setText(fraction);
                            return true;
                        case "1/2":
                            fraction = "1/2";
                            value_fraction.setText(fraction);
                            return true;
                        case "5/8":
                            fraction = "5/8";
                            value_fraction.setText(fraction);
                            return true;
                        case "3/4":
                            fraction = "3/4";
                            value_fraction.setText(fraction);
                            return true;
                        case "7/8":
                            fraction = "7/8";
                            value_fraction.setText(fraction);
                            return true;

                        default:
                            return false;
                    }

                }
            });
            popupMenu.show();
        });

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


        b1.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("1"));
            } else {
                value_in.setText(value_in.getText().toString().concat("1"));
            }
        });

        b2.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("2"));
            } else {
                value_in.setText(value_in.getText().toString().concat("2"));
            }
        });

        b3.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("3"));
            } else {
                value_in.setText(value_in.getText().toString().concat("3"));
            }
        });

        b4.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("4"));
            } else {
                value_in.setText(value_in.getText().toString().concat("4"));
            }
        });

        b5.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("5"));
            } else {
                value_in.setText(value_in.getText().toString().concat("5"));
            }
        });

        b6.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("6"));
            } else {
                value_in.setText(value_in.getText().toString().concat("6"));
            }
        });

        b7.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("7"));
            } else {
                value_in.setText(value_in.getText().toString().concat("7"));
            }
        });

        b8.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("8"));
            } else {
                value_in.setText(value_in.getText().toString().concat("8"));
            }
        });

        b9.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("9"));
            } else {
                value_in.setText(value_in.getText().toString().concat("9"));
            }
        });

        b0.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("0"));
            } else {
                value_in.setText(value_in.getText().toString().concat("0"));
            }
        });

        bdot.setOnClickListener((View v) -> {
            if (c) {
                if (indexFtIn == 0) {
                    value_ft.setText("");
                } else {
                    value_in.setText("");
                }
                c = false;
            }
            if (indexFtIn == 0) {
                value_ft.setText(value_ft.getText().toString().concat("."));
            } else {
                value_in.setText(value_in.getText().toString().concat("."));
            }
        });

        bminus.setOnClickListener((View v) -> {
            if (!value_ft.getText().toString().contains("-")) {
                value_ft.setText(value_ft.getText().insert(0, "-"));
            } else {
                value_ft.setText(value_ft.getText().toString().replace("-", ""));
            }

        });

        bdel.setOnClickListener((View v) -> {
            if (indexFtIn == 0) {
                if (value_ft.getText().toString().length() > 0)
                    value_ft.setText(value_ft.getText().toString().substring(0, value_ft.getText().toString().length() - 1));
            } else {
                if (value_in.getText().toString().length() > 0)
                    value_in.setText(value_in.getText().toString().substring(0, value_in.getText().toString().length() - 1));
            }

        });

        bok.setOnClickListener((View v) -> {
            if (flag != 111) {
                if (isNumeric(value_ft.getText().toString()) && isNumericInch(value_in.getText().toString())) {

                    String mioValore = value_ft.getText().toString().trim() + "'" + value_in.getText().toString().trim() + " " + value_fraction.getText().toString().trim() + "\"".toString();

                    if(flag==0){

                            MyData.push("Unit_Of_Measure", Utils.writeMetri(mioValore));
                        realValue.setText(mioValore);
                        }else {
                            realValue.setText(mioValore);
                            if (flag == -1) {
                                DataSaved.line_Offset = Double.parseDouble(Utils.writeMetri(realValue.getText().toString()));
                            }
                        }


                    if (activity instanceof SlideBoomActivity) {
                        ((SlideBoomActivity) activity).save();
                    }
                    c = true;
                    dialog.dismiss();

                } else {
                    new CustomToast(activity, "Error Input!").show_error();
                }
            } else {
                if (isNumeric(value_ft.getText().toString()) && isNumericInch(value_in.getText().toString())) {
                    String mioValore = value_ft.getText().toString().trim() + "'" + value_in.getText().toString().trim() + " " + value_fraction.getText().toString().trim() + "\"".toString();

                    if(mode==0){
                        //Est
                        Punti3DAdapter.punti3DList.get(dec).setX(Double.parseDouble(Utils.writeMetri(mioValore)));
                    }
                    if(mode==1){
                        //Nord
                        Punti3DAdapter.punti3DList.get(dec).setY(Double.parseDouble(Utils.writeMetri(mioValore)));
                    }
                    if(mode==2){
                        //Quota
                        Punti3DAdapter.punti3DList.get(dec).setZ(Double.parseDouble(Utils.writeMetri(mioValore)));
                    }

                    Dialog_Edita_Punti3D.aggiornaLista();

                    c = true;
                    dec = 0;
                    mode=-1;
                    dialog.dismiss();

                } else {
                    new CustomToast(activity, "Error Input!").show_error();
                }
            }

        });
    }

    private void onLongClick() {
        bdel.setOnLongClickListener((View v) -> {
            if (indexFtIn == 0) {
                if (value_ft.getText().toString().length() > 0)
                    value_ft.setText("");
            } else {
                if (value_in.getText().toString().length() > 0)
                    value_in.setText("");
            }
            return true;
        });
    }

}
