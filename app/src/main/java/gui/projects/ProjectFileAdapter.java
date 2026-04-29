package gui.projects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import gui.MyApp;
import gui.dialogs_and_toast.Dialog_Set_Secondo_SP;
import packexcalib.exca.DataSaved;

public class ProjectFileAdapter extends RecyclerView.Adapter<ProjectFileAdapter.ViewHolder> {
    private ArrayList<FileItem> files;
    private int selectedItem = -1;
    private boolean isFold = false;
    private boolean isCloudFolder = false;


    private int filterType = 0; // 0 = tutti, 1 = solo cartelle, 2 = solo file
    private ArrayList<FileItem> originalFiles; // per conservare l'elenco completo

    public ProjectFileAdapter(ArrayList<FileItem> filesName) {
        files = filesName;
    }

    public ProjectFileAdapter(ArrayList<FileItem> filesName, int filterType) {
        this.originalFiles = new ArrayList<>(filesName);
        this.filterType = filterType;
        this.files = new ArrayList<>();
        applyFilter();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.file_picker_row, parent, false);
        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        FileItem fileItem = files.get(position);
        String nameFile = fileItem.getName();
        boolean isFolder = fileItem.isFolder();
        long fileSize = fileItem.getSize();
        String filePath = fileItem.getPath();

        // Set item views based on your views and data model
        ConstraintLayout constraintLayout = holder.panel;
        TextView textView = holder.nameTextView;
        TextView sizeTextView = holder.sizeTextView;
        TextView txtcrs2 = holder.txtcrs2;
        ImageView icon = holder.icon;


        if (isFolder) {
            isFold = true;
            //come definire con un booleano se è una cartella cloud o no per cambiare immagine
            if (isCloudFolder()) {
                icon.setImageResource(R.drawable.cloud_filled_f); // Your folder icon resource
            } else {
                icon.setImageResource(R.drawable.filled_f); // Your folder icon resource
            }

            sizeTextView.setText(formatSize(fileSize));
            txtcrs2.setVisibility(View.GONE);
        } else {
            isFold = false;
            int lastIndex = nameFile.lastIndexOf(".");
            String fileExtension = nameFile.substring(lastIndex + 1).toLowerCase();
            icon.setImageResource(R.drawable.baseline_help_96);
            if (fileExtension.equalsIgnoreCase("loc")) {
                txtcrs2.setVisibility(View.VISIBLE);
                txtcrs2.setText("Origin: " + DataSaved.SECONDO_S_CRS);
            } else {
                txtcrs2.setVisibility(View.GONE);
            }
            if (fileExtension.equalsIgnoreCase("pdf")) {
                icon.setImageResource(R.drawable.document_pdf);
            } else if (fileExtension.equalsIgnoreCase("dxf")) {
                icon.setImageResource(R.drawable.file_type_dxf);
            } else if (fileExtension.equalsIgnoreCase("xml")) {
                icon.setImageResource(R.drawable.land_xml_immagine);
            } else if (fileExtension.equalsIgnoreCase("sp")) {
                icon.setImageResource(R.drawable.accuracy);
            } else if (fileExtension.equalsIgnoreCase("loc")) {
                icon.setImageResource(R.drawable.accuracy);
            } else if (fileExtension.equalsIgnoreCase("lok")) {
                icon.setImageResource(R.drawable.accuracy);
            } else if (fileExtension.equalsIgnoreCase("csv")) {
                icon.setImageResource(R.drawable.mycsv);
            } else if (fileExtension.equalsIgnoreCase("txt")) {
                icon.setImageResource(R.drawable.mytxt);
            } else if (fileExtension.equalsIgnoreCase("ird")) {
                icon.setImageResource(R.drawable.ird_200);
            } else if (nameFile.startsWith("#1P_#")) {
                icon.setImageResource(R.drawable.image_1p);
            } else if (nameFile.startsWith("#AB_#")) {
                icon.setImageResource(R.drawable.image_ab);
            } else if (nameFile.startsWith("#CS_#")) {
                icon.setImageResource(R.drawable.image_cs);
            } else if (nameFile.startsWith("#AR_#")) {
                icon.setImageResource(R.drawable.area_image);
            } else if (nameFile.startsWith("pstx")) {
                icon.setImageResource(R.drawable.canale_benna);
            } else if (fileExtension.equalsIgnoreCase("xls") || fileExtension.equalsIgnoreCase("xlsx")) {
                icon.setImageResource(R.drawable.my_xls);
            }
            sizeTextView.setText(formatSize(fileSize));
        }

        textView.setText(nameFile.replace("#AB_#", "").replace("#1P_#", "").replace("#CS_#", "").replace("#AR_#", ""));

        constraintLayout.setBackgroundColor(selectedItem == position ? ContextCompat.getColor(constraintLayout.getContext(), R.color.bg_sfsgreen) : ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setItem(int i) {
        selectedItem = i;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ConstraintLayout panel;
        public ImageView icon;
        public TextView sizeTextView;  // Aggiungi questo campo
        public TextView txtcrs2;

        @SuppressLint("NotifyDataSetChanged")
        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.pointCk);
            nameTextView = itemView.findViewById(R.id.path_tv);
            panel = itemView.findViewById(R.id.panel);
            sizeTextView = itemView.findViewById(R.id.size_tv);
            txtcrs2 = itemView.findViewById(R.id.txtcrs2);

            icon.setOnClickListener((View v) -> {
                if (selectedItem == getAdapterPosition()) {
                    selectedItem = -1;
                    notifyDataSetChanged();

                } else {
                    selectedItem = getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
            nameTextView.setOnClickListener((View v) -> {
                if (selectedItem == getAdapterPosition()) {
                    selectedItem = -1;
                    notifyDataSetChanged();

                } else {
                    selectedItem = getAdapterPosition();
                    notifyDataSetChanged();
                }
                /*
                   selectedItem = getAdapterPosition();
                    notifyDataSetChanged();
                    if (MyApp.visibleActivity instanceof PickProject) {
                        Log.d("Selezzzione", String.valueOf(MyApp.visibleActivity));
                        new Dialog_PRJ_Folder(MyApp.visibleActivity).show(Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + getSelectedFilePath());
                    }
                 */
            });
            txtcrs2.setOnClickListener(view -> {
                Dialog_Set_Secondo_SP dialog = new Dialog_Set_Secondo_SP(MyApp.visibleActivity);
                dialog.show(() -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        notifyItemChanged(pos);
                    } else {
                        notifyDataSetChanged();
                    }
                });
            });
        }
    }

    public String getSelectedFilePath() {
        if (selectedItem != RecyclerView.NO_POSITION) {
            return files.get(selectedItem).getName();
        }
        return null;
    }

    public String getSelectedFilePathAbs() {
        if (selectedItem != RecyclerView.NO_POSITION) {
            return files.get(selectedItem).getPath();
        }
        return null;
    }


    public void setSelectedItem(int i) {
        selectedItem = i;
    }

    public boolean isFold() {

        return isFold;
    }

    public long size() {
        return files.get(selectedItem).getSize();
    }


    public void renameSelectedFile(String pat, String newName) {
        if (pat == null && newName == null) {
            notifyDataSetChanged();
        }

        if (selectedItem != RecyclerView.NO_POSITION && selectedItem < files.size()) {
            File parentDirectory = new File(pat);
            FileItem selectedFileItem = files.get(selectedItem);
            File oldFile = new File(parentDirectory, selectedFileItem.getName());
            File newFile = new File(parentDirectory, newName);

            if (oldFile.exists()) {
                if (newFile.exists()) {
                } else if (oldFile.renameTo(newFile)) {
                    // Se il file è stato rinominato con successo, aggiorna l'adapter
                    files.set(selectedItem, new FileItem(newName, selectedFileItem.isFolder(), selectedFileItem.getSize(), selectedFileItem.getPath()));
                    notifyItemChanged(selectedItem);
                } else {
                }
            } else {
            }
        }
    }


    public static class FileItem {

        private final String name;
        private final boolean isFolder;
        private final long size;
        private final String path; // Percorso completo del file

        public FileItem(String name, boolean isFolder, long size, String path) {
            this.name = name;
            this.isFolder = isFolder;
            this.size = size;
            this.path = path;
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

        public String getPath() {
            return path;
        }
    }

    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void setFilterType(int filter) {
        this.filterType = filter;
        applyFilter();
    }

    private void applyFilter() {
        files.clear();
        for (FileItem item : originalFiles) {
            if (filterType == 0) {
                files.add(item); // mostra tutto
            } else if (filterType == 1 && item.isFolder()) {
                files.add(item); // solo cartelle
            } else if (filterType == 2 && !item.isFolder()) {
                files.add(item); // solo file
            }
        }
        selectedItem = -1; // reset selezione
        notifyDataSetChanged();
    }

    public boolean isCloudFolder() {
        return isCloudFolder;
    }

    public void setCloudFolder(boolean cloudFolder) {
        this.isCloudFolder = cloudFolder;
    }

    public void updateItems(List<FileItem> newFiles) {
        if (newFiles == null) return;
        files.clear();
        files.addAll(newFiles);
        if (originalFiles != null) {
            originalFiles.clear();
            originalFiles.addAll(newFiles);
        }
        notifyDataSetChanged();
    }


}