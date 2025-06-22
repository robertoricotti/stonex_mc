package dxf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.stx_dig.R;

import java.util.List;

import io.reactivex.annotations.Nullable;
import packexcalib.exca.DataSaved;

public class Poly3DAdapter extends ArrayAdapter<Polyline> {
    private Context context;
    private List<Polyline> polylines;
    public Poly3DAdapter(Context context, List<Polyline> polylines) {
        super(context, 0, polylines);
        this.context = context;
        this.polylines = polylines;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the custom layout for each list item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_point3d_item, parent, false);
        }


        Polyline polyline = getItem(position);




        TextView textView = convertView.findViewById(R.id.textView);


        if (polyline != null) {
            textView.setText( (position+1)+ ") Polyline:    "+(position+1)+"/"+polylines.size());

        }

        return convertView;
    }
}
