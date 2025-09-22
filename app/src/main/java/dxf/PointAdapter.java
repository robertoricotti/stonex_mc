package dxf;

import static gui.MyApp.folderPath;
import static gui.MyApp.visibleActivity;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;


import android.graphics.Color;


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

    /** Metodo pubblico per filtrare i punti */
    public void filter(String query) {
        points.clear();
        if (query == null || query.trim().isEmpty()) {
            points.addAll(allPoints); // reset
        } else {
            String lowerQuery = query.toLowerCase();
            for (Point3D p : allPoints) {
                String idd=p.getId()!=null?p.getId().toString():"";
                String name = p.getName() != null ? p.getName().toLowerCase() : "";
                String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                if (name.contains(lowerQuery) || desc.contains(lowerQuery)||idd.contains(lowerQuery)) {
                    points.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** 🔄 Metodo per aggiornare la lista da fuori */
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
        String s = point.getFilename();
        if (s == null) {
            s = "";
        } else {
            s = s.replace(Environment.getExternalStorageDirectory().toString() + folderPath, "");
        }

        try {
            holder.textView.setText(
                    s + "\n" +
                            point.getId() +
                            "    E:" + point.getX() +
                            "    N:" + point.getY() +
                            "  Z:" + point.getZ() +
                            "    " + point.getDescription()
            );
            holder.textView.setTextColor(Color.BLACK);
            holder.imageView.setImageTintList(ColorStateList.valueOf(point.getColore()));
        } catch (Exception e) {
            holder.textView.setText(e.getMessage());
            holder.textView.setTextColor(Color.RED);
            holder.imageView.setImageTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }

        // evidenzia selezionato
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
