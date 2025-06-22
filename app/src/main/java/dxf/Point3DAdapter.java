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

import gui.MyApp;
import io.reactivex.annotations.Nullable;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class Point3DAdapter extends ArrayAdapter<Point3D>  {

    private Context context;
    private List<Point3D> point3DList;


    public Point3DAdapter(Context context, List<Point3D> point3DList) {
        super(context, 0, point3DList);
        this.context = context;
        this.point3DList = point3DList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the custom layout for each list item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_point3d_item, parent, false);
        }

        // Get the Point3D object for this position
        Point3D point3D = getItem(position);



        // Find TextViews in your custom layout and set values from Point3D object
        TextView textView = convertView.findViewById(R.id.textView);


        if (point3D != null) {
            if(!DataSaved.dxfTexts.isEmpty()) {
                try {
                    int index = MyData.get_Int("Unit_Of_Measure");
                    switch (index){
                        case 0:
                        case 1:
                            textView.setText(" P Id: " + DataSaved.dxfTexts.get(position).getText() + " -  E: " + String.format("%.3f",point3D.getX()) + "   N: " + String.format("%.3f",point3D.getY()) + "   Z: " + String.format("%.3f",point3D.getZ()));
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            textView.setText(" P Id: " + DataSaved.dxfTexts.get(position).getText() + " -  E: " + String.format("%.4f",point3D.getX()/0.3048006096) + "   N: " + String.format("%.4f",point3D.getY()/0.3048006096) + "   Z: " + String.format("%.4f",point3D.getZ()/0.3048006096));
                            break;
                    }

                } catch (Exception e) {
                    textView.setText(" P Id: " + (position+1)+ " -  E: " + point3D.getX() + "   N: " + point3D.getY() + "   Z: " + point3D.getZ());
                }
            }else {
                try {
                    textView.setText(" P Id: " + (position+1)+ " -  E: " + String.format("%.3f",point3D.getX()) + "   N: " + String.format("%.3f",point3D.getY()) + "   Z: " + String.format("%.3f",point3D.getZ()));
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

        }

        return convertView;
    }
}
