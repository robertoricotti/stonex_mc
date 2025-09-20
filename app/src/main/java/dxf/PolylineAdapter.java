package dxf;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import dxf.Polyline;
public class PolylineAdapter extends RecyclerView.Adapter<PolylineAdapter.PolylineViewHolder> {

    private List<Polyline> polylines;
    private Object selectedItem;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Polyline poly);
    }

    public PolylineAdapter(List<Polyline> polylines, OnItemClickListener listener) {
        this.polylines = polylines != null ? polylines : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PolylineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_polyline, parent, false); // item_polyline.xml layout
        return new PolylineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PolylineViewHolder holder, int position) {
        Polyline poly = polylines.get(position);

        String layerName = poly.getLayer() != null ? poly.getLayer().getLayerName() : "Nessun layer";
        holder.textView.setText("Layer: " + layerName + ", Vertici: " + poly.getVertexCount());

        // Evidenzia l’elemento selezionato
        if (poly.equals(selectedItem)) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedItem = poly;  // nel PointAdapter
            notifyDataSetChanged(); // evidenzia il selezionato
            listener.onItemClick(poly); // log e callback
        });
    }

    @Override
    public int getItemCount() {
        return polylines.size();
    }

    public void setSelectedItem(Object item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    static class PolylineViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public PolylineViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewPolyline);
        }
    }
}
