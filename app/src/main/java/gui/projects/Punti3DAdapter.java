package gui.projects;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dxf.Point3D;
import dxf.Point3DAdapter;
import gui.MyApp;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomQwertyDialog;
import utils.MyData;
import utils.Utils;


public class Punti3DAdapter extends RecyclerView.Adapter<Punti3DAdapter.ViewHolder> {
    public static List<Point3D> punti3DList;
    private int selectedItem = -1;
    private int selectedID = -1;
    private int selectedEast = -1;
    private int selectedNord = -1;
    private int selectedQuota = -1;
    private int selectedDes = -1;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    CustomQwertyDialog customQwertyDialog;

    public Punti3DAdapter(List<Point3D> punti3DList) {
        this.punti3DList = punti3DList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.my_point_row, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout constraintLayout = holder.panel;
        Point3D point = punti3DList.get(position);

        if (point != null) {
            holder.t_Name.setText(point.getId());
            holder.t_Est.setText(Utils.readSensorCalibration(String.valueOf(point.getX())));
            holder.t_Nord.setText(Utils.readSensorCalibration(String.valueOf(point.getY())));
            holder.t_Quota.setText(Utils.readSensorCalibration(String.valueOf(point.getZ())));
            holder.t_Des.setText(point.getName());
        }
        constraintLayout.setBackgroundColor(selectedItem == position ? ContextCompat.getColor(constraintLayout.getContext(), R.color.yellow) : ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
        holder.remove.setImageResource(selectedItem == position ? R.drawable.btn_delete_forever : R.drawable.bt_stop_search);
        holder.t_Name.setBackgroundColor(selectedID == position ? (Color.GREEN) : (Color.TRANSPARENT));
        holder.t_Est.setBackgroundColor(selectedEast == position ? (Color.GREEN) : (Color.TRANSPARENT));
        holder.t_Nord.setBackgroundColor(selectedNord == position ? (Color.GREEN) : (Color.TRANSPARENT));
        holder.t_Quota.setBackgroundColor(selectedQuota == position ? (Color.GREEN) : (Color.TRANSPARENT));
        holder.t_Des.setBackgroundColor(selectedDes == position ? (Color.GREEN) : (Color.TRANSPARENT));


    }

    @Override
    public int getItemCount() {
        return punti3DList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        EditText t_Name, t_Est, t_Nord, t_Quota, t_Des;
        ImageView remove;
        public ConstraintLayout panel;

        @SuppressLint("NotifyDataSetChanged")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            customNumberDialog = new CustomNumberDialog(MyApp.visibleActivity, 111);
            customNumberDialogFtIn = new CustomNumberDialogFtIn(MyApp.visibleActivity, 111);
            customQwertyDialog = new CustomQwertyDialog(MyApp.visibleActivity,null);
            remove = itemView.findViewById(R.id.img_del);
            t_Name = itemView.findViewById(R.id.t_Name);
            t_Est = itemView.findViewById(R.id.t_Est);
            t_Nord = itemView.findViewById(R.id.t_Nord);
            t_Quota = itemView.findViewById(R.id.t_Quota);
            t_Des = itemView.findViewById(R.id.t_Des);
            panel = itemView.findViewById(R.id.panel);
            remove.setOnClickListener(view -> {
                if (selectedItem != getAdapterPosition()) {
                    selectedItem = getAdapterPosition();
                    Activity_Crea_Superficie.indexSel=selectedItem;
                    selectedID = -1;
                    selectedDes = -1;
                    selectedEast = -1;
                    selectedNord = -1;
                    selectedQuota = -1;
                    notifyDataSetChanged();
                } else {
                    chiediRemove(getAdapterPosition());
                }

            });


            t_Name.setOnClickListener((View v) -> {
                selectedItem = getAdapterPosition();
                Activity_Crea_Superficie.indexSel=selectedItem;
                selectedID = getAdapterPosition();
                selectedDes = -1;
                selectedEast = -1;
                selectedNord = -1;
                selectedQuota = -1;
                notifyDataSetChanged();
                if (!customQwertyDialog.dialog.isShowing()) {
                    customQwertyDialog.show(t_Name, selectedItem, 0);
                }


            });
            t_Est.setOnClickListener((View v) -> {
                selectedItem = getAdapterPosition();
                Activity_Crea_Superficie.indexSel=selectedItem;
                selectedEast = getAdapterPosition();
                selectedID = -1;
                selectedDes = -1;
                selectedNord = -1;
                selectedQuota = -1;
                notifyDataSetChanged();
                if(MyData.get_Int("Unit_Of_Measure")==0|
                        MyData.get_Int("Unit_Of_Measure")==1|
                        MyData.get_Int("Unit_Of_Measure")==2|
                        MyData.get_Int("Unit_Of_Measure")==3|
                        MyData.get_Int("Unit_Of_Measure")==6|
                        MyData.get_Int("Unit_Of_Measure")==7) {
                    if (!customNumberDialog.dialog.isShowing()) {
                        customNumberDialog.show(t_Est, selectedItem, 0);
                    }
                }else {
                    if (!customNumberDialogFtIn.dialog.isShowing()) {
                        customNumberDialogFtIn.show(t_Est, selectedItem, 0);
                    }
                }


            });
            t_Nord.setOnClickListener((View v) -> {
                selectedItem = getAdapterPosition();
                Activity_Crea_Superficie.indexSel=selectedItem;
                selectedNord = getAdapterPosition();
                selectedID = -1;
                selectedEast = -1;
                selectedDes = -1;
                selectedQuota = -1;
                notifyDataSetChanged();
                if(MyData.get_Int("Unit_Of_Measure")==0|
                        MyData.get_Int("Unit_Of_Measure")==1|
                        MyData.get_Int("Unit_Of_Measure")==2|
                        MyData.get_Int("Unit_Of_Measure")==3|
                        MyData.get_Int("Unit_Of_Measure")==6|
                        MyData.get_Int("Unit_Of_Measure")==7) {
                    if (!customNumberDialog.dialog.isShowing()) {
                        customNumberDialog.show(t_Nord, selectedItem, 1);
                    }
                }else {
                    if (!customNumberDialogFtIn.dialog.isShowing()) {
                        customNumberDialogFtIn.show(t_Nord, selectedItem, 1);
                    }
                }

            });
            t_Quota.setOnClickListener((View v) -> {
                selectedItem = getAdapterPosition();
                Activity_Crea_Superficie.indexSel=selectedItem;
                selectedQuota = getAdapterPosition();
                selectedID = -1;
                selectedEast = -1;
                selectedNord = -1;
                selectedDes = -1;
                notifyDataSetChanged();
                if(MyData.get_Int("Unit_Of_Measure")==0|
                        MyData.get_Int("Unit_Of_Measure")==1|
                        MyData.get_Int("Unit_Of_Measure")==2|
                        MyData.get_Int("Unit_Of_Measure")==3|
                        MyData.get_Int("Unit_Of_Measure")==6|
                        MyData.get_Int("Unit_Of_Measure")==7){
                    if (!customNumberDialog.dialog.isShowing()) {
                        customNumberDialog.show(t_Quota, selectedItem, 2);
                    }
                }else {
                    if (!customNumberDialogFtIn.dialog.isShowing()) {
                        customNumberDialogFtIn.show(t_Quota, selectedItem, 2);
                    }
                }


            });
            t_Des.setOnClickListener((View v) -> {
                selectedItem = getAdapterPosition();
                Activity_Crea_Superficie.indexSel=selectedItem;
                selectedDes = getAdapterPosition();
                selectedID = -1;
                selectedEast = -1;
                selectedNord = -1;
                selectedQuota = -1;
                notifyDataSetChanged();
                if (!customQwertyDialog.dialog.isShowing()) {
                        customQwertyDialog.showP(998,Punti3DAdapter.this,selectedItem,punti3DList.get(selectedItem).getName());
                }
            });
        }
    }


    public int getSelectedItem() {
        return selectedItem;
    }


    public void chiediRemove(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyApp.visibleActivity);
        builder.setTitle("REMOVE POINT");
        builder.setMessage("Do You Want to Remove Point?");

        builder.setPositiveButton("YES", (dialog, which) -> {
            try {
                if (Activity_Crea_Superficie.point3DS != null && Activity_Crea_Superficie.point3DS.length > 0) {
                    List<Point3D> pointList = new ArrayList<>(Arrays.asList(Activity_Crea_Superficie.point3DS));

                    if (position >= 0 && position < pointList.size()) {
                        pointList.remove(position); // Rimuove il punto selezionato
                        Activity_Crea_Superficie.point3DS = pointList.toArray(new Point3D[0]); // Aggiorna l'array
                        Dialog_Edita_Punti3D.chiudi();

                    }
                }
            } catch (Exception ignored) {
            }
        });

        builder.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }






}