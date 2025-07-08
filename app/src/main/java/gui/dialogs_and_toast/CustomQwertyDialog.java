package gui.dialogs_and_toast;

import static gui.MyApp.folderPath;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import java.io.File;

import gui.projects.Dialog_Edita_Punti3D;
import gui.projects.Dialog_PRJ_Folder;
import gui.projects.PickProject;
import gui.projects.ProjectFileAdapter;
import gui.projects.Punti3DAdapter;
import utils.FullscreenActivity;

public class CustomQwertyDialog {
    Activity activity;
    EditText realValue;
    public Dialog dialog;
    EditText value;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdel, bok;
    Button bq, bw, be, br, bt, by, bu, bi, bo, bp;
    Button ba, bs, bd, bf, bg, bh, bj, bk, bl;
    Button bz, bx, bc, bv, bb, bn, bm, space, chiocc, bpunto;
    Button maiuscola;
    int txtLen = 40;
    boolean capital = true;
    int flag,position;
    int index = -1;
    ProjectFileAdapter projectFileAdapter;
    Punti3DAdapter punti3DAdapter;
    String path = "";
    Dialog_PRJ_Folder dialogPrjFolder;

    boolean c = true;

    public CustomQwertyDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.setContentView(R.layout.dialog_qwerty_apollo8);//APOLLO2_10
        } else if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("qti") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            dialog.setContentView(R.layout.dialog_qwerty_apollo7);
        }
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
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 650);
        } else {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 500);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    public void show(EditText realValue, int index, int flag) {
        this.index = index;
        this.flag = flag;
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
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 650);
        } else {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 500);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        onLongClick();
    }

    public void show(int flag, ProjectFileAdapter projectFileAdapter,String path, String name) {
        this.path=path;
        this.flag = flag;
        this.projectFileAdapter = projectFileAdapter;
        dialogPrjFolder=new Dialog_PRJ_Folder(activity);
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
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init2(name);
        onClick();
        onLongClick();

    }
    public void showP(int flag,Punti3DAdapter punti3DAdapter,int position,String name) {
        this.punti3DAdapter=punti3DAdapter;
        this.position=position;
        this.flag = flag;
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
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
        onLongClick();
        init3(name);

    }
    private void init3(String name){
        c = true;
        value.setText(name);
    }

    private void init2(String name) {
        c = true;
        value.setText(name);
    }

    private void init() {
        c = true;
        value.setText(realValue.getText().toString());
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
        maiuscola = dialog.findViewById(R.id.maiuscolo);
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
        chiocc = dialog.findViewById(R.id.chiocc);
        bpunto = dialog.findViewById(R.id.bpunto);
        bdel = dialog.findViewById(R.id.bdel);
        bok = dialog.findViewById(R.id.bok);
        space = dialog.findViewById(R.id.space);
        if (capital) {
            maiuscola.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.light_yellow));
            space.setText("_");
        } else {
            maiuscola.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.bg_gray));
            space.setText("-");
        }
        setupChar(capital);
    }

    private void onClick() {
        maiuscola.setOnClickListener(view -> {
            capital = !capital;
            setupChar(capital);
            if (capital) {
                maiuscola.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.light_yellow));
                space.setText("_");
            } else {
                maiuscola.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.bg_gray));
                space.setText("-");
            }
        });
        space.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[26]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bdel.setOnClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
        });

        bok.setOnClickListener((View v) -> {
            if (flag == 999) {
                if (getName() != null) {
                    projectFileAdapter.renameSelectedFile(path,getName());
                    index = -1;
                    flag = -1;
                    dialog.dismiss();
                }

            } else if (flag==997) {

                if (getName() != null) {
                    boolean isOK=false;
                    String fullPath=Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath + "/Projects/"+getName();
                    File directory = new File(fullPath);
                    if (!directory.exists()) {
                        boolean created = directory.mkdirs();
                        if (created) {
                            isOK=true;
                            new CustomToast(activity,fullPath).show_alert();
                            Log.d("FolderCreation", "Cartella creata: " + fullPath);
                        } else {
                            isOK=false;
                            new CustomToast(activity,getName()+" Error").show_error();
                            Log.e("FolderCreation", "Errore nella creazione della cartella");
                        }
                    } else {
                        isOK=false;
                        new CustomToast(activity,getName()+" Already Exists").show_error();
                        Log.d("FolderCreation", "Cartella già esistente: " + fullPath);
                    }
                    index = -1;
                    flag = -1;
                    if(isOK){
                        try {

                            if (!dialogPrjFolder.dialog.isShowing()) {
                                dialogPrjFolder.show(fullPath);
                            }
                        } catch (Exception e) {
                            dialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                    dialog.dismiss();

                }

            } else if (flag==998) {
                punti3DAdapter.punti3DList.get(position).setName(value.getText().toString());
                index = -1;
                flag = -1;
                punti3DAdapter.notifyDataSetChanged();
                dialog.dismiss();


            } else {
                realValue.setText(value.getText().toString().trim());
                c = true;
                if (index > -1 && flag > -1) {
                    if (flag == 0) {
                        Punti3DAdapter.punti3DList.get(index).setId((value.getText().toString()));
                    }
                    if (flag == 1) {
                        Punti3DAdapter.punti3DList.get(index).setName((value.getText().toString()));
                    }
                    Dialog_Edita_Punti3D.aggiornaLista();
                }

                index = -1;
                flag = -1;
                dialog.dismiss();
            }
        });


        b1.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("1"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b2.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("2"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b3.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("3"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b4.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("4"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b5.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("5"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b6.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("6"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b7.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("7"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b8.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("8"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b9.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("9"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        b0.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("0"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });


        bq.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[0]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bw.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[1]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        be.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[2]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        br.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[3]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bt.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[4]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        by.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[5]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bu.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[6]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bi.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[7]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bo.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[8]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bp.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[9]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        ba.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[10]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bs.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[11]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bd.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[12]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bf.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[13]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bg.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[14]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bh.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[15]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bj.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[16]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bk.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[17]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bl.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[18]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bz.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[19]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bx.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[20]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bc.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[21]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bv.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[22]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bb.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[23]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bn.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[24]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });

        bm.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat(caratteri(capital)[25]));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });
        chiocc.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("@"));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });
        bpunto.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            if (value.getText().toString().length() < txtLen)
                value.setText(value.getText().toString().concat("."));
            else
                new CustomToast(activity, "STRING TOO LONG!").show();
        });


    }

    public void setupChar(boolean capital) {
        if (capital) {
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
        } else {
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
            bc.setText("c");
            bv.setText("v");
            bb.setText("b");
            bn.setText("n");
            bm.setText("m");
        }
    }

    public String[] caratteri(boolean capital) {
        String s[] = new String[27];
        if (capital) {
            s[0] = "Q";
            s[1] = "W";
            s[2] = "E";
            s[3] = "R";
            s[4] = "T";
            s[5] = "Y";
            s[6] = "U";
            s[7] = "I";
            s[8] = "O";
            s[9] = "P";
            s[10] = "A";
            s[11] = "S";
            s[12] = "D";
            s[13] = "F";
            s[14] = "G";
            s[15] = "H";
            s[16] = "J";
            s[17] = "K";
            s[18] = "L";
            s[19] = "Z";
            s[20] = "X";
            s[21] = "C";
            s[22] = "V";
            s[23] = "B";
            s[24] = "N";
            s[25] = "M";
            s[26] = "_";

        } else {
            s[0] = "q";
            s[1] = "w";
            s[2] = "e";
            s[3] = "r";
            s[4] = "t";
            s[5] = "y";
            s[6] = "u";
            s[7] = "i";
            s[8] = "o";
            s[9] = "p";
            s[10] = "a";
            s[11] = "s";
            s[12] = "d";
            s[13] = "f";
            s[14] = "g";
            s[15] = "h";
            s[16] = "j";
            s[17] = "k";
            s[18] = "l";
            s[19] = "z";
            s[20] = "x";
            s[21] = "c";
            s[22] = "v";
            s[23] = "b";
            s[24] = "n";
            s[25] = "m";
            s[26] = "-";


        }

        return s;
    }

    public String getName() {
        return value.getText().toString();
    }

    private void onLongClick() {
        bdel.setOnLongClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText("");
            return true;
        });
    }


}
