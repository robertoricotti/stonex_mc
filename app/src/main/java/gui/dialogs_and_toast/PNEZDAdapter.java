package gui.dialogs_and_toast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.List;

import dxf.PNEZDPoint;

public class PNEZDAdapter extends RecyclerView.Adapter<PNEZDAdapter.ViewHolder> {
    private List<PNEZDPoint> pointList;

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
        holder.pointColor.setBackgroundColor(point.getColor());
    }

    @Override
    public int getItemCount() {
        return pointList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pointNumberText, coordinatesText, descriptionText,pointColor;


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
}
