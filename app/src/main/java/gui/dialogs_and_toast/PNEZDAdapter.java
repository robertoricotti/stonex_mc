package gui.dialogs_and_toast;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
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
        try {
            PNEZDPoint point = pointList.get(position);

            // Numero punto
            holder.pointNumberText.setText("P: " + point.getPointNumber());

            // Coordinate (se 0 → N/A)
            String northing = (point.getNorthing() == 0) ? "N/A" : String.valueOf(point.getNorthing());
            String easting  = (point.getEasting()  == 0) ? "N/A" : String.valueOf(point.getEasting());
            String elevation= (point.getElevation()== 0) ? "N/A" : String.valueOf(point.getElevation());

            holder.coordinatesText.setText("N: " + northing + " | E: " + easting + " | Z: " + elevation);

            // Descrizione
            String description = (point.getDescription() != null && !point.getDescription().isEmpty())
                    ? point.getDescription()
                    : "Nessuna descrizione";
            holder.descriptionText.setText(description);

            // Colore con fallback
            int color = point.getColor() != 0 ? point.getColor() : Color.GRAY;
            holder.pointColor.setImageTintList(ColorStateList.valueOf(color));

            // Evidenzia selezione
            holder.itemView.setBackgroundColor(
                    point.equals(selectedItem) ? Color.YELLOW : Color.TRANSPARENT
            );

            // Click selezione
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(point);
                setSelectedItem(point);
            });

            // Long click elimina
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onItemDelete(point);
                return true;
            });

        } catch (Exception e) {
            Log.e("PNEZDAdapter", "Errore in onBindViewHolder posizione " + position, e);
            holder.pointNumberText.setText("P: ERR");
            holder.coordinatesText.setText("Dati mancanti");
            holder.descriptionText.setText("Elemento corrotto");
            holder.pointColor.setImageTintList(ColorStateList.valueOf(Color.RED));
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
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
