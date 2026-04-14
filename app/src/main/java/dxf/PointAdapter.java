package dxf;

import static gui.MyApp.folderPath;
import static gui.MyApp.visibleActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import utils.Utils;


public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {

    private List<Point3D> allPoints;   // lista completa
    private List<Point3D> points;      // lista filtrata
    private Point3D selectedPoint;

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Point3D point);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Point3D point);
    }

    public PointAdapter(List<Point3D> points) {
        this.allPoints = points != null ? new ArrayList<>(points) : new ArrayList<>();
        this.points = new ArrayList<>(this.allPoints); // inizialmente copia completa
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setSelectedItem(Point3D point) {
        if (selectedPoint != null && selectedPoint.equals(point)) {
            // se clicchi sul già selezionato → deseleziona
            selectedPoint = null;
        } else {
            selectedPoint = point;
        }
        notifyDataSetChanged();
    }

    /**
     * Metodo pubblico per filtrare i punti
     */
    public void filter(String query) {
        points.clear();
        if (query == null || query.trim().isEmpty()) {
            points.addAll(allPoints); // reset
        } else {
            String lowerQuery = query.toLowerCase();
            for (Point3D p : allPoints) {
                String idd = p.getId() != null ? p.getId().toString() : "";
                String name = p.getName() != null ? p.getName().toLowerCase() : "";
                String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                if (name.contains(lowerQuery) || desc.contains(lowerQuery) || idd.contains(lowerQuery)) {
                    points.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 🔄 Metodo per aggiornare la lista da fuori
     */
    public void updateData(List<Point3D> newPoints) {
        allPoints.clear();
        allPoints.addAll(newPoints);
        points.clear();
        points.addAll(newPoints);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point, parent, false);
        return new PointViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        Point3D point = points.get(position);

        // Fallback sicuri
        String filename = "";
        try {
            if (point.getFilename() != null) {
                filename = point.getFilename()
                        .replace(Environment.getExternalStorageDirectory().toString() + folderPath, "");
            }
        } catch (Exception ignored) {
        }

        String idStr = "";
        try {
            idStr = point.getId() != null ? point.getId().toString() : "";
        } catch (Exception ignored) {
        }

        double x = 0.0, y = 0.0, z = 0.0;
        try {
            x = point.getX();
        } catch (Exception ignored) {
        }
        try {
            y = point.getY();
        } catch (Exception ignored) {
        }
        try {
            z = point.getZ();
        } catch (Exception ignored) {
        }

        String desc = "";
        try {
            desc = point.getDescription() != null ? point.getDescription() : "";
        } catch (Exception ignored) {
        }

        int color;
        try {
            color = point.getColore();
            if (color == 0) {
                color = Color.GRAY; // fallback se mancante
            }
        } catch (Exception e) {
            color = Color.GRAY; // fallback sicuro
        }

        try {
            holder.textView.setText(
                    filename + "\n" +
                            idStr +
                            "    N:" + Utils.showCoords(String.valueOf(y)) +
                            "    E:" + Utils.showCoords(String.valueOf(x)) +
                            "  Z:" + Utils.showCoords(String.valueOf(z)) +
                            "    " + desc
            );
            holder.textView.setTextColor(Color.BLACK);
            holder.imageView.setImageTintList(ColorStateList.valueOf(color));
        } catch (Exception e) {
            // Se anche la renderizzazione fallisce, mostra errore
            holder.textView.setText("***********");
            holder.textView.setTextColor(Color.RED);
            holder.imageView.setImageTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }

        // Evidenzia selezionato
        if (point.equals(selectedPoint)) {
            holder.itemView.setBackgroundColor(
                    visibleActivity.getResources().getColor(R.color.bg_sfsgreen)
            );
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(point);
        });

        holder.itemView.setOnLongClickListener(v -> {
            setSelectedItem(point);
            if (longClickListener != null) longClickListener.onItemLongClick(point);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        PointViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewPoint);
            imageView = itemView.findViewById(R.id.imgPunto);
        }
    }
}
