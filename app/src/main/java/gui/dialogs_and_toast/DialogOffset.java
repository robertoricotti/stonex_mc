package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;
import static utils.Utils.isNumeric;
import static utils.Utils.isNumericInch;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
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

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class DialogOffset {
    Activity activity;
    public Dialog dialog;
    Button canc, save, zero, set, reverse;
    EditText value, value_ft, value_in, value_fraction;
    TextView title, measure;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bcanc, bdel;
    String fraction = "7/8";

    double offsetH;

    static boolean c = true;


    int indexMeasure;
    int indexFtIn;


    public DialogOffset(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_offset);
        dialog.setCancelable(false);
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
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.95);
        int height = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        c=true;
    }

    private void init() {
        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            bdot.setVisibility(View.GONE);
            measure.setVisibility(View.GONE);
            value.setVisibility(View.GONE);
            value_in.setVisibility(View.VISIBLE);
            value_ft.setVisibility(View.VISIBLE);
            indexFtIn = 0;
            value_ft.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_yellow));
            value_in.setBackgroundColor(Color.TRANSPARENT);
            String depth = Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.offsetH * -1));
            value_ft.setText(depth.split("'")[0].trim());
            value_in.setText(depth.split("'")[1].trim().substring(0, 2));
            int begin = depth.trim().length() - 4;
            int end = begin + 3;
            fraction = depth.substring(begin, end);
            value_fraction.setText(fraction);
        } else {
            indexFtIn = 0;
            measure.setVisibility(View.VISIBLE);
            value.setVisibility(View.VISIBLE);
            value_in.setVisibility(View.GONE);
            value_ft.setVisibility(View.GONE);
            value_fraction.setVisibility(View.GONE);
            title.setText(dialog.getContext().getResources().getString(R.string.offset));
            value.setText(Utils.readUnitOfMeasure(String.valueOf(DataSaved.offsetH * -1)));
            measure.setText(Utils.getMetriSimbol());
        }

    }

    private void findView() {
        title = dialog.findViewById(R.id.title);
        canc = dialog.findViewById(R.id.exit);
        save = dialog.findViewById(R.id.save);
        value = dialog.findViewById(R.id.value);
        set = dialog.findViewById(R.id.set);
        reverse = dialog.findViewById(R.id.inverted);
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
        bcanc = dialog.findViewById(R.id.bc);
        measure = dialog.findViewById(R.id.unitOfMeasure);
        zero = dialog.findViewById(R.id.zero);
        bdel = dialog.findViewById(R.id.bdel);
        value_ft = dialog.findViewById(R.id.value_ft);
        value_in = dialog.findViewById(R.id.value_in);
        value_fraction = dialog.findViewById(R.id.value_fraction);
    }

    private void onClick() {
        value_fraction.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(activity, title);
            popupMenu.getMenu().add("0/0");
            popupMenu.getMenu().add("1/8");
            popupMenu.getMenu().add("1/4");
            popupMenu.getMenu().add("3/8");
            popupMenu.getMenu().add("1/2");
            popupMenu.getMenu().add("5/8");
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
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {

                String mioValore =value_ft.getText().toString().trim() + "'" + value_in.getText().toString().trim() + " " + value_fraction.getText().toString().trim() + "\"".toString();

                if (isNumeric(value_ft.getText().toString()) && isNumericInch(value_in.getText().toString())) {
                    MyData.push("Operator_Offset", Utils.writeMetriLITE(mioValore.trim()));
                    MyData.push("shortcutOffset_" + DataSaved.shortcutIndex, Utils.writeMetriLITE(mioValore));
                    DataSaved.offsetH = MyData.get_Double("Operator_Offset") * -1;
                    dialog.cancel();
                } else {
                    new CustomToast(activity, "Error INPUT!").show_error();
                }
            } else {
                if (isNumeric(value.getText().toString())) {
                    MyData.push("Operator_Offset", Utils.writeMetri(value.getText().toString()));
                    MyData.push("shortcutOffset_" + DataSaved.shortcutIndex, Utils.writeMetri(value.getText().toString()));
                    DataSaved.offsetH = MyData.get_Double("Operator_Offset") * -1;
                    dialog.cancel();
                } else {
                    new CustomToast(activity, "Error INPUT!").show_error();
                }
            }
        });


        canc.setOnClickListener((View v) -> {
            dialog.cancel();
        });

        zero.setOnClickListener((View v) -> {
            offsetH = ExcavatorLib.bucketCoord[2];
            zero.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.blue));
        });

        set.setOnClickListener((View v) -> {
            if (zero.getBackgroundTintList() == ContextCompat.getColorStateList(activity, R.color.blue)) {
                c = true;
                set.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.orange));
                String depth = Utils.readUnitOfMeasure(String.valueOf((ExcavatorLib.bucketCoord[2] - offsetH)));
                if (indexMeasure == 4 || indexMeasure == 5) {
                    value_ft.setText(depth.split("'")[0].trim());
                    value_in.setText(depth.split("'")[1].trim());
                } else {
                    value.setText(depth);
                }

            } else {
                new CustomToast(activity, "SET PREVIOUS POINT!").show_alert();
            }

        });

        reverse.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!value_ft.getText().toString().equals("")) {
                    if (!value_ft.getText().toString().contains("-")) {
                        value_ft.setText(value_ft.getText().insert(0, "-"));
                    } else {
                        value_ft.setText(value_ft.getText().toString().replace("-", ""));
                    }
                }
            } else {
                if (!value.getText().toString().equals("")) {
                    if (!value.getText().toString().contains("-")) {
                        value.setText(value.getText().insert(0, "-"));
                    } else {
                        value.setText(value.getText().toString().replace("-", ""));
                    }
                }
            }
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
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("1"));
            }
            resetColor();
        });

        b2.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("2"));
            }
            resetColor();
        });

        b3.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("3"));
            }
            resetColor();
        });

        b4.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("4"));
            }
            resetColor();
        });

        b5.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("5"));
            }
            resetColor();
        });

        b6.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("6"));
            }
            resetColor();
        });

        b7.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("7"));
            }
            resetColor();

        });

        b8.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("8"));
            }
            resetColor();
        });

        b9.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("9"));
            }
            resetColor();
        });

        b0.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("0"));
            }
            resetColor();

        });

        bdot.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
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
            } else {
                if (c) {
                    value.setText("");
                    c = false;
                }
                value.setText(value.getText().toString().concat("."));
            }
            resetColor();
        });

        bcanc.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (indexFtIn == 0) {
                    value_ft.setText("0");
                } else {
                    value_in.setText("0");
                    fraction = "0/0";
                    value_fraction.setText(fraction);
                }

            } else {
                value.setText("0");
            }
            c = true;
            resetColor();
        });
        bcanc.setOnLongClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                value_ft.setText("0");
                value_in.setText("0");
                fraction = "0/0";
                value_fraction.setText(fraction);
            }

            return false;
        });

        bdel.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (indexFtIn == 0) {
                    if (value_ft.getText().toString().length() > 0)
                        value_ft.setText(value_ft.getText().toString().substring(0, value_ft.getText().toString().length() - 1));
                } else {
                    if (value_in.getText().toString().length() > 0)
                        value_in.setText(value_in.getText().toString().substring(0, value_in.getText().toString().length() - 1));
                }
            } else {
                if (value.getText().toString().length() > 0)
                    value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
            }
            resetColor();
        });
    }

    private void resetColor() {
        zero.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.dark_gray));
        set.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.dark_gray));
    }
}


