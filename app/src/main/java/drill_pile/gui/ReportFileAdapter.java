package drill_pile.gui;
import static cloud.WebSocketPlugin.isAuthenticated;
import static gui.dialogs_and_toast.DialogPassword.isTech;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import gui.projects.Usb_Project_Nova;
import utils.MyData;
import utils.NetworkUtils;


public class ReportFileAdapter extends RecyclerView.Adapter<ReportFileAdapter.ViewHolder> {
    private OnCloudClickListener cloudListener;
    private ArrayList<FileItem> files;
    private int selectedItem = -1;
    private boolean isFold = false;
    private int filterType = 0; // 0 = tutti, 1 = solo cartelle, 2 = solo file
    private ArrayList<FileItem> originalFiles; // per conservare l'elenco completo

    public ReportFileAdapter(ArrayList<FileItem> filesName) {
        files = filesName;
    }
    public ReportFileAdapter(ArrayList<FileItem> filesName, int filterType) {
        this.originalFiles = new ArrayList<>(filesName);
        this.filterType = filterType;
        this.files = new ArrayList<>();
        applyFilter();
    }
    @NonNull
    @Override
    public ReportFileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.report_picker_row, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportFileAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        FileItem fileItem = files.get(position);
        String nameFile = fileItem.getName();
        boolean isFolder = fileItem.isFolder();
        long fileSize = fileItem.getSize();
        String filePath=fileItem.getPath();

        // Set item views based on your views and data model
        ConstraintLayout constraintLayout = holder.panel;
        TextView textView = holder.nameTextView;
        TextView sizeTextView = holder.sizeTextView;
        ImageView icon = holder.icon;
        ImageView usb = holder.usb;
        ImageView cloud = holder.cloud;
        ImageView delete = holder.delete;
        if (isFolder) {
            isFold = true;

            icon.setImageResource(R.drawable.folder_lilla); // Your folder icon resource

            sizeTextView.setText(formatSize(fileSize));
        }
        else {
            isFold = false;
            int lastIndex = nameFile.lastIndexOf(".");
            String fileExtension = nameFile.substring(lastIndex + 1).toLowerCase();
            icon.setImageResource(R.drawable.baseline_help_96);

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
            }else if (fileExtension.equalsIgnoreCase("lok")) {
                icon.setImageResource(R.drawable.accuracy);
            }else if (fileExtension.equalsIgnoreCase("csv")) {
                icon.setImageResource(R.drawable.mycsv);
            } else if (fileExtension.equalsIgnoreCase("txt")) {
                icon.setImageResource(R.drawable.mytxt);
            } else if (fileExtension.equalsIgnoreCase("ird")) {
                icon.setImageResource(R.drawable.ird_200);
            }else if (nameFile.startsWith("#1P_#")) {
                icon.setImageResource(R.drawable.image_1p);
            } else if (nameFile.startsWith("#AB_#")) {
                icon.setImageResource(R.drawable.image_ab);
            } else if (nameFile.startsWith("#CS_#")) {
                icon.setImageResource(R.drawable.image_cs);
            } else if (nameFile.startsWith("#AR_#")) {
                icon.setImageResource(R.drawable.area_image);
            } else if (nameFile.startsWith("pstx")) {
                icon.setImageResource(R.drawable.canale_benna);
            }
            sizeTextView.setText(formatSize(fileSize));
        }
        textView.setText(nameFile);
        constraintLayout.setBackgroundColor(selectedItem == position ? ContextCompat.getColor(constraintLayout.getContext(), R.color.bg_sfsgreen) : ContextCompat.getColor(constraintLayout.getContext(), R.color.transparent));
        boolean isSelected = (selectedItem == position);

        usb.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        cloud.setVisibility(isSelected&&isAuthenticated&& NetworkUtils.isInternetAvailable(MyApp.visibleActivity) ? View.VISIBLE : View.INVISIBLE);
        delete.setVisibility(isSelected&&isTech ? View.VISIBLE : View.INVISIBLE);
        holder.cloud.setOnClickListener(v -> {
            if (cloudListener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                cloudListener.onCloudClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setItem(int i) {
        selectedItem=i;
        notifyDataSetChanged();
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
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ConstraintLayout panel;
        public ImageView icon,usb,cloud,delete;
        public TextView sizeTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.pointCk);
            usb=itemView.findViewById(R.id.sendUSB);
            cloud=itemView.findViewById(R.id.sendCloud);
            delete=itemView.findViewById(R.id.deleteRep);
            nameTextView = itemView.findViewById(R.id.path_tv);
            panel = itemView.findViewById(R.id.panel);
            sizeTextView = itemView.findViewById(R.id.size_tv);
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

            });

            usb.setOnClickListener(v -> {
                if (usbClickListener != null) {
                    usbClickListener.onUsbClick(getAdapterPosition());
                }
            });
            cloud.setOnClickListener((View v) -> {


            });
            delete.setOnClickListener(v -> {

                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Context context = v.getContext();
                FileItem item = files.get(pos);

                File file = new File(item.getPath()); // 🔥 PATH ASSOLUTO

                new AlertDialog.Builder(context)
                        .setTitle("DELETING REPORT")
                        .setMessage("DELETE THE SELECTED REPORT?\n\n" + item.getName())
                        .setPositiveButton("YES", (dialog, which) -> {

                            boolean deleted;
                            if (file.isDirectory()) {
                                deleted = deleteRecursive(file);
                            } else {
                                deleted = file.delete();
                            }

                            if (deleted) {
                                files.remove(pos);
                                notifyItemRemoved(pos);
                                selectedItem = -1;
                            }
                        })
                        .setNegativeButton("NO", (dialog, which) -> {
                            selectedItem = -1;
                            notifyDataSetChanged();
                        })
                        .show();
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
    public long size(){
        return files.get(selectedItem).getSize();
    }


    public void renameSelectedFile( String pat,String newName) {
        if(pat==null&&newName==null){
            notifyDataSetChanged();
        }

        if (selectedItem != RecyclerView.NO_POSITION && selectedItem < files.size()) {
            File parentDirectory=new File(pat);
            FileItem selectedFileItem = files.get(selectedItem);
            File oldFile = new File(parentDirectory, selectedFileItem.getName());
            File newFile = new File(parentDirectory, newName);

            if (oldFile.exists()) {
                if (newFile.exists()) {
                } else if (oldFile.renameTo(newFile)) {
                    // Se il file è stato rinominato con successo, aggiorna l'adapter
                    files.set(selectedItem, new FileItem(newName, selectedFileItem.isFolder(), selectedFileItem.getSize(),selectedFileItem.getPath()));
                    notifyItemChanged(selectedItem);
                } else {
                }
            } else {
            }
        }
    }


    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }
    public interface OnUsbClickListener {
        void onUsbClick(int position);
    }

    private OnUsbClickListener usbClickListener;

    public void setOnUsbClickListener(OnUsbClickListener listener) {
        this.usbClickListener = listener;
    }

    private void copyRecursive(File source, File dest) throws IOException {

        if (source.isDirectory()) {
            if (!dest.exists()) dest.mkdirs();

            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyRecursive(child, new File(dest, child.getName()));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(dest)) {

                byte[] buffer = new byte[8192];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }

    public interface OnCloudClickListener {
        void onCloudClick(int position);
    }



    public void setOnCloudClickListener(OnCloudClickListener listener) {
        this.cloudListener = listener;
    }
    public FileItem getItem(int position) {
        if (position >= 0 && position < files.size()) {
            return files.get(position);
        }
        return null;
    }
}
