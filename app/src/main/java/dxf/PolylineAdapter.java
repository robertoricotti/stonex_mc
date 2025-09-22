package dxf;

import static gui.MyApp.folderPath;

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

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PolylineAdapter extends RecyclerView.Adapter<PolylineAdapter.PolylineViewHolder> {

    private List<Polyline> polylines;
    private Polyline selectedPolyline;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Polyline poly);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Polyline poly);
    }

    public PolylineAdapter(List<Polyline> polylines) {
        this.polylines = polylines != null ? polylines : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setSelectedItem(Polyline polyline) {
        this.selectedPolyline = polyline;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PolylineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_polyline, parent, false); // assicurati che item_polyline.xml esista
        return new PolylineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PolylineViewHolder holder, int position) {
        Polyline poly = polylines.get(position);
        String s=poly.getFilename();
        try {
            s=s.replace(Environment.getExternalStorageDirectory().toString() + folderPath,"");
            String layerName = poly.getLayer() != null ? poly.getLayer().getLayerName() : "Nessun layer";
            holder.textView.setText(s+"\nLayer: " + layerName + ", Vertici: " + poly.getVertexCount());
            holder.textView.setTextColor(Color.BLACK);
            holder.imageView.setImageTintList(ColorStateList.valueOf(poly.getLineColor()));
        } catch (Exception e) {
            holder.textView.setText(e.getMessage());
            holder.textView.setTextColor(Color.RED);
            holder.imageView.setImageTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }


        if (poly.equals(selectedPolyline)) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(poly);
        });

        holder.itemView.setOnLongClickListener(v -> {
            setSelectedItem(poly);
            if (longClickListener != null) longClickListener.onItemLongClick(poly);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return polylines.size();
    }

    static class PolylineViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        PolylineViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewPolyline);
            imageView=itemView.findViewById(R.id.imgLine);
        }
    }
}