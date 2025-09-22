package gui.dialogs_and_toast;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.List;

import dxf.PNEZDPoint;

public class PNEZDAdapter extends RecyclerView.Adapter<PNEZDAdapter.ViewHolder> {
    private List<PNEZDPoint> pointList;
    private PNEZDPoint selectedItem = null;
    private OnPNEZDItemActionListener listener;
    public PNEZDAdapter(List<PNEZDPoint> pointList) {
        this.pointList = pointList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pnezd_point, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PNEZDPoint point = pointList.get(position);
        holder.pointNumberText.setText("P: " + point.getPointNumber());
        holder.coordinatesText.setText("N: " + point.getNorthing() + " | E: " + point.getEasting() + " | Z: " + point.getElevation());
        holder.descriptionText.setText(point.getDescription());
        holder.pointColor.setImageTintList(ColorStateList.valueOf(point.getColor()));

        // Evidenzia se è selezionato
        if (point.equals(selectedItem)) {
            holder.itemView.setBackgroundColor(Color.YELLOW); // <-- qui scegli il colore
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Click normale → selezione
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(point);

            setSelectedItem(point); // aggiorna evidenziazione
        });

        // Long click → elimina
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemDelete(point);
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return pointList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pointNumberText, coordinatesText, descriptionText;
        ImageView pointColor;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pointNumberText = itemView.findViewById(R.id.pointNumberText);
            coordinatesText = itemView.findViewById(R.id.coordinatesText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            pointColor=itemView.findViewById(R.id.descriptionTextColor);
        }
    }
    public void updateData(List<PNEZDPoint> newPoints) {
        this.pointList.clear();
        this.pointList.addAll(newPoints);
    }
    public void scrollToLast(RecyclerView recyclerView) {
        if (getItemCount() > 0) {
            recyclerView.scrollToPosition(getItemCount() - 1);
        }
    }

    // Metodo pubblico per aggiornare la selezione con toggle
    public void setSelectedItem(PNEZDPoint point) {
        if (this.selectedItem != null && this.selectedItem.equals(point)) {
            // Se il punto cliccato è già selezionato → deseleziona
            this.selectedItem = null;
        } else {
            // Altrimenti seleziona il nuovo
            this.selectedItem = point;
        }
        notifyDataSetChanged(); // forza il redraw della RecyclerView
    }
    public void removeSelectedItem() {
        if (selectedItem != null) {
            pointList.remove(selectedItem);
            selectedItem = null; // reset selezione
            notifyDataSetChanged(); // aggiorna RecyclerView
        }
    }
    public PNEZDPoint getSelectedItem() {
        return selectedItem;
    }
}
