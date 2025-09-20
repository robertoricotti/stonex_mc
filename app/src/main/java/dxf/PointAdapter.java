package dxf;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;


import android.graphics.Color;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {

    private List<Point3D> points;
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
        this.points = points != null ? points : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setSelectedItem(Point3D point) {
        this.selectedPoint = point;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point, parent, false); // assicurati che item_point.xml esista
        return new PointViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        Point3D point = points.get(position);
        holder.textView.setText(point.getName() != null ? point.getName()+"   E:"+point.getX()+" N:"+point.getY()+" Z:"+point.getZ()
                : point.getId()+"   E:"+point.getX()+"  N:"+point.getY()+"  Z:"+point.getZ());

        // evidenzia selezionato in magenta
        if (point.equals(selectedPoint)) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(point);
        });

        holder.itemView.setOnLongClickListener(v -> {
            setSelectedItem(point); // l'adapter gestisce la selezione visuale
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
        PointViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewPoint); // assicurati che l'id corrisponda
        }
    }
}
