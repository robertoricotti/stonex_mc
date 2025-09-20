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


public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {

    private List<Point3D> points;
    private Object selectedItem;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Point3D point);
    }

    public PointAdapter(List<Point3D> points, OnItemClickListener listener) {
        this.points = points != null ? points : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point, parent, false); // item_point.xml layout
        return new PointViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PointViewHolder holder, int position) {
        Point3D point = points.get(position);
        holder.textView.setText(point.getName() != null ? point.getName() : point.getId());

        // Evidenzia l’elemento selezionato
        if (point.equals(selectedItem)) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedItem = point;  // nel PointAdapter
            notifyDataSetChanged(); // evidenzia il selezionato
            listener.onItemClick(point); // log e callback
        });
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    public void setSelectedItem(Object item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    static class PointViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public PointViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewPoint);
        }
    }
}
