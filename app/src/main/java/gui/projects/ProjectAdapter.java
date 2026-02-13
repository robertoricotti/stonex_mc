package gui.projects;

import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

import packexcalib.exca.DataSaved;
import utils.MyData;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    private ArrayList<FileItem> files;

    // Variables to track the selected position for each checkbox type
    private int selectedCkTrmPosition = -1;
    private int selectedCkPolyPosition = -1;
    private int selectedCkPoiPosition = -1;
    private int selectedItem = -1;

    View contactView;

    public ProjectAdapter(ArrayList<FileItem> filesName) {
        files = filesName;
        initializeCheckboxSelections();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        contactView = inflater.inflate(R.layout.layer_picker_row, parent, false);
        return new ViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = files.get(position);
        String nameFile = fileItem.getName();
        boolean isFolder = fileItem.isFolder();
        long fileSize = fileItem.getSize();

        // Stringhe di confronto per selezionare automaticamente le checkbox
        String sTrm = DataSaved.progettoSelected;
        String sPoly = DataSaved.progettoSelected_POLY;
        String sPoint = DataSaved.progettoSelected_POINT;

        ConstraintLayout constraintLayout = holder.panel;
        TextView textView = holder.nameTextView;
        TextView sizeTextView = holder.sizeTextView;
        ImageView icon = holder.icon;
        CheckBox ckTrm = holder.ckTrm;
        CheckBox ckPoly = holder.ckPolyl;
        CheckBox ckPoi = holder.ckPoi;
        CheckBox ckJson = holder.ckJson;

        // Configurazione visibilità e contenuti in base al tipo di file (cartella o file)
        if (isFolder) {
            ckTrm.setVisibility(View.GONE);
            ckPoly.setVisibility(View.GONE);
            ckPoi.setVisibility(View.GONE);
            ckJson.setVisibility(View.GONE);
            sizeTextView.setVisibility(View.VISIBLE);
            icon.setImageResource(R.drawable.filled_f);
            sizeTextView.setText(formatSize(fileSize));

        } else {

            int lastIndex = nameFile.lastIndexOf(".");
            String fileExtension = nameFile.substring(lastIndex + 1).toLowerCase();
            if (DataSaved.isWL == EXCAVATOR ||
                    DataSaved.isWL == WHEELLOADER ||
                    DataSaved.isWL == DOZER_SIX ||
                    DataSaved.isWL == DOZER ||
                    DataSaved.isWL == GRADER) {
                if (fileExtension.equalsIgnoreCase("geojson")) {
                    ckTrm.setVisibility(View.INVISIBLE);
                    ckPoly.setVisibility(View.INVISIBLE);
                    ckPoi.setVisibility(View.INVISIBLE);
                    ckJson.setVisibility(View.VISIBLE);
                    sizeTextView.setVisibility(View.VISIBLE);
                } else if (fileExtension.equalsIgnoreCase("csv")) {
                    ckTrm.setVisibility(View.INVISIBLE);
                    ckPoly.setVisibility(View.INVISIBLE);
                    ckPoi.setVisibility(View.VISIBLE);
                    ckJson.setVisibility(View.INVISIBLE);
                    sizeTextView.setVisibility(View.VISIBLE);
                } else {
                    ckTrm.setVisibility(View.VISIBLE);
                    ckPoly.setVisibility(View.VISIBLE);
                    ckPoi.setVisibility(View.VISIBLE);
                    ckJson.setVisibility(View.INVISIBLE);
                    sizeTextView.setVisibility(View.VISIBLE);
                }

            }else if(DataSaved.isWL==DRILL){
                ckTrm.setVisibility(View.INVISIBLE);
                if(fileExtension.equalsIgnoreCase("dxf")){
                ckPoly.setVisibility(View.VISIBLE);}else {
                    ckPoly.setVisibility(View.INVISIBLE);
                }
                if(fileExtension.equalsIgnoreCase("csv")||
                        fileExtension.equalsIgnoreCase("xml")||
                        fileExtension.equalsIgnoreCase("ird")||
                        fileExtension.equalsIgnoreCase("xls")||
                fileExtension.equalsIgnoreCase("xlsx")){
                ckPoi.setVisibility(View.VISIBLE);}
                else {
                    ckPoi.setVisibility(View.INVISIBLE);
                }
                ckJson.setVisibility(View.INVISIBLE);
                sizeTextView.setVisibility(View.VISIBLE);
            }
            setFileIcon(nameFile, icon);
            sizeTextView.setText(formatSize(fileSize));
            textView.setText(nameFile);
        }


        // Seleziona automaticamente le checkbox se i nomi dei file corrispondono alle stringhe salvate
        if (selectedCkTrmPosition == -1 && sTrm != null && sTrm.equals(nameFile)) {
            selectedCkTrmPosition = holder.getAdapterPosition();
            ckTrm.setChecked(true);  // Seleziona automaticamente ckTrm
        }

        if (selectedCkPolyPosition == -1 && sPoly != null && sPoly.equals(nameFile)) {
            selectedCkPolyPosition = holder.getAdapterPosition();
            ckPoly.setChecked(true);  // Seleziona automaticamente ckPoly
        }

        if (selectedCkPoiPosition == -1 && sPoint != null && sPoint.equals(nameFile)) {
            selectedCkPoiPosition = holder.getAdapterPosition();
            ckPoi.setChecked(true);  // Seleziona automaticamente ckPoi
        }


        // Evidenzia l'elemento se una qualsiasi delle checkbox è selezionata
        boolean isItemSelected = (holder.getAdapterPosition() == selectedCkTrmPosition) ||
                (holder.getAdapterPosition() == selectedCkPolyPosition) ||
                (holder.getAdapterPosition() == selectedCkPoiPosition);

        constraintLayout.setBackgroundColor(selectedItem == position ? ContextCompat.getColor(constraintLayout.getContext(), R.color.bg_sfsgreen) : ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));


        // Imposta gli stati delle checkbox in base alle posizioni selezionate
        ckTrm.setChecked(holder.getAdapterPosition() == selectedCkTrmPosition);
        if (ckTrm.isChecked()) {
            ckTrm.setBackgroundColor(ContextCompat.getColor(constraintLayout.getContext(), R.color.bg_sfsgreen));
        } else {
            ckTrm.setBackgroundColor(ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
        }
        ckPoly.setChecked(holder.getAdapterPosition() == selectedCkPolyPosition);
        if (ckPoly.isChecked()) {
            ckPoly.setBackgroundColor(ContextCompat.getColor(constraintLayout.getContext(), R.color.yellow));
        } else {
            ckPoly.setBackgroundColor(ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
        }
        ckPoi.setChecked(holder.getAdapterPosition() == selectedCkPoiPosition);
        if (ckPoi.isChecked()) {
            ckPoi.setBackgroundColor((Color.parseColor("#2196F3")));
        } else {
            ckPoi.setBackgroundColor(ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
        }


        // Imposta i listener di click per le checkbox
        holder.setCheckboxListeners();
    }


    private void setFileIcon(String nameFile, ImageView icon) {
        int lastIndex = nameFile.lastIndexOf(".");
        String fileExtension = nameFile.substring(lastIndex + 1).toLowerCase();
        icon.setImageResource(R.drawable.baseline_help_96);

        switch (fileExtension) {
            case "pstx":
                icon.setImageResource(R.drawable.canale_benna);
                break;
            case "geojson":
                icon.setImageResource(R.drawable.json_immag);
                break;
            case "pdf":
                icon.setImageResource(R.drawable.document_pdf);
                break;
            case "dxf":
                icon.setImageResource(R.drawable.file_type_dxf);
                break;
            case "xml":
                icon.setImageResource(R.drawable.land_xml_immagine);
                break;
            case "sp":
            case "loc":
            case "lok":
                icon.setImageResource(R.drawable.accuracy);
                break;
            case "csv":
                icon.setImageResource(R.drawable.mycsv);
                break;
            case "txt":
                icon.setImageResource(R.drawable.mytxt);
                break;
            case "ird":
                icon.setImageResource(R.drawable.ird_200);
                break;
            case "xls":
            case "xlsx":
                icon.setImageResource(R.drawable.my_xls);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int getSelectedCkTrmPosition() {
        return selectedCkTrmPosition;
    }

    public int getSelectedCkPolyPosition() {
        return selectedCkPolyPosition;
    }

    public int getSelectedCkPoiPosition() {
        return selectedCkPoiPosition;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox ckTrm, ckPolyl, ckPoi, ckJson;
        public TextView nameTextView;
        public ConstraintLayout panel;
        public ImageView icon;
        public TextView sizeTextView;

        @SuppressLint("NotifyDataSetChanged")
        public ViewHolder(View itemView) {
            super(itemView);
            ckTrm = itemView.findViewById(R.id.ckTrm);
            ckPolyl = itemView.findViewById(R.id.ckPolyl);
            ckPoi = itemView.findViewById(R.id.ckPoi);
            ckJson = itemView.findViewById(R.id.ckJSON);
            icon = itemView.findViewById(R.id.pointCk);
            nameTextView = itemView.findViewById(R.id.path_tv);
            panel = itemView.findViewById(R.id.panel);
            sizeTextView = itemView.findViewById(R.id.size_tv);
            nameTextView.setOnClickListener((View v) -> {
                if (selectedItem == -1) {
                    selectedItem = getAdapterPosition();
                } else {
                    selectedItem = -1;
                }
                notifyDataSetChanged();
            });
            icon.setOnClickListener((View v) -> {
                if (selectedItem == -1) {
                    selectedItem = getAdapterPosition();
                } else {
                    selectedItem = -1;
                }
                notifyDataSetChanged();
            });
        }

        // Method to set click listeners on checkboxes
        // Method to set click listeners on checkboxes inside the ViewHolder class
        private void setCheckboxListeners() {
            ckTrm.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION)
                    return; // Safeguard against invalid positions

                if (ckTrm.isChecked()) {
                    selectedCkTrmPosition = position; // Set selected position for ckTrm
                } else {
                    selectedCkTrmPosition = -1; // Deselect if unchecked
                }
                notifyDataSetChanged(); // Refresh the view to reflect changes
            });

            ckPolyl.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION)
                    return; // Safeguard against invalid positions

                if (ckPolyl.isChecked()) {
                    selectedCkPolyPosition = position; // Set selected position for ckPolyl
                } else {
                    selectedCkPolyPosition = -1; // Deselect if unchecked
                    MyData.push("progettoSelected_POLY", "");
                    DataSaved.lastProjectNamePOLY = "";
                }
                notifyDataSetChanged(); // Refresh the view to reflect changes
            });

            ckPoi.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION)
                    return; // Safeguard against invalid positions

                if (ckPoi.isChecked()) {
                    selectedCkPoiPosition = position; // Set  posselectedition for ckPoi
                } else {
                    selectedCkPoiPosition = -1; // Deselect if unchecked
                    MyData.push("progettoSelected_POINT", "");
                    DataSaved.lastProjectNamePOINT = "";
                }
                notifyDataSetChanged(); // Refresh the view to reflect changes
            });
            ckJson.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION)
                    return; // Safeguard against invalid positions

                notifyDataSetChanged(); // Refresh the view to reflect changes
            });
        }

    }


    public String getSelectedFilePath() {
        if (selectedCkTrmPosition != RecyclerView.NO_POSITION) {
            return files.get(selectedCkTrmPosition).getName();
        } else if (selectedCkPolyPosition != RecyclerView.NO_POSITION) {
            return files.get(selectedCkPolyPosition).getName();
        } else if (selectedCkPoiPosition != RecyclerView.NO_POSITION) {
            return files.get(selectedCkPoiPosition).getName();
        }
        return null;
    }

    public static class FileItem {
        private final String name;
        private final boolean isFolder;
        private final long size;

        public FileItem(String name, boolean isFolder, long size) {
            this.name = name;
            this.isFolder = isFolder;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public long getSize() {
            return size;
        }
    }

    private void initializeCheckboxSelections() {


        for (int i = 0; i < files.size(); i++) {
            FileItem fileItem = files.get(i);
            String nameFile = fileItem.getName();
            if (selectedCkTrmPosition == -1 && DataSaved.progettoSelected != null && DataSaved.progettoSelected.substring(DataSaved.progettoSelected.lastIndexOf("/") + 1, DataSaved.progettoSelected.length()).equals(nameFile)) {
                selectedCkTrmPosition = i;
            }
            if (selectedCkPolyPosition == -1 && DataSaved.progettoSelected_POLY != null && DataSaved.progettoSelected_POLY.substring(DataSaved.progettoSelected_POLY.lastIndexOf("/") + 1, DataSaved.progettoSelected_POLY.length()).equals(nameFile)) {
                selectedCkPolyPosition = i;
            }
            if (selectedCkPoiPosition == -1 && DataSaved.progettoSelected_POINT != null && DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf("/") + 1, DataSaved.progettoSelected_POINT.length()).equals(nameFile)) {
                selectedCkPoiPosition = i;
            }


        }
        if (selectedCkPolyPosition == -1) {
            MyData.push("progettoSelected_POLY", "");
            DataSaved.lastProjectNamePOLY = "";
        }
        if (selectedCkPoiPosition == -1) {
            MyData.push("progettoSelected_POINT", "");
            DataSaved.lastProjectNamePOINT = "";

        }


        // Notify the adapter to refresh and apply the initial checkbox selections
        notifyDataSetChanged();
    }



    public void setItem(int i) {
        selectedItem = i;
        notifyDataSetChanged();
    }

    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
