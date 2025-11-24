package gui.projects;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dxf.Arc;
import dxf.AutoCADColor;
import dxf.Circle;
import dxf.DisplayItem;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import dxf.ZoomableImageView;
import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;

public class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {
    private List<DisplayItem> displayItems;
    private Map<String, String> layerInfoMap = new HashMap<>();
    public static boolean selectA;

    public LayerAdapter(List<DisplayItem> displayItems) {
        printLayerList(DataSaved.dxfLayers_DTM, "DTM");
        printLayerList(DataSaved.dxfLayers_POLY, "POLY");
        printLayerList(DataSaved.dxfLayers_POINT, "POINT");
        // Sincronizza lo stato dei layer
        for (DisplayItem item : displayItems) {
            if (item.getType() == DisplayItem.TYPE_LAYER) {
                for (Layer layer : DataSaved.dxfLayers_DTM) {
                    if (layer.getLayerName() != null) {
                        if (layer.getLayerName().equals(item.getLayerName())) {
                            item.setEnable(layer.isEnable());
                            break;
                        }
                    }
                }
                for (Layer layer : DataSaved.dxfLayers_POLY) {
                    if (layer.getLayerName() != null) {
                        if (layer.getLayerName().equals(item.getLayerName())) {
                            item.setEnable(layer.isEnable());
                            break;
                        }
                    }
                }
                for (Layer layer : DataSaved.dxfLayers_POINT) {
                    if (layer.getLayerName() != null) {
                        if (layer.getLayerName().equals(item.getLayerName())) {
                            item.setEnable(layer.isEnable());
                            break;
                        }
                    }
                }
            }
        }
        popolaLayer();
        this.displayItems = displayItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_layer_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayItem item = displayItems.get(position);

        if (item.getType() == DisplayItem.TYPE_FILE) {
            holder.prjName.setVisibility(View.VISIBLE);
            holder.selectAll.setVisibility(View.VISIBLE);
            holder.layerName.setVisibility(View.GONE);
            holder.layerStatus.setVisibility(View.GONE);
            holder.layerColor.setVisibility(View.GONE);
            holder.layerEnable.setVisibility(View.GONE);
            holder.frameFaces.setVisibility(View.GONE);
            holder.framePoly.setVisibility(View.GONE);
            holder.frameLines.setVisibility(View.GONE);
            holder.framePoints.setVisibility(View.GONE);
            holder.prjName.setText(item.getFileName().replace(
                    Environment.getExternalStorageDirectory().toString() + MyApp.folderPath + "/Projects", ""));


            if(holder.getAdapterPosition()==0){
                holder.selectAll.setEnabled(true);
                holder.selectAll.setOnClickListener(view -> {
                    selectA = !selectA;

                    // 1. Aggiorna tutti i layer delle liste globali
                    for (Layer layer : DataSaved.dxfLayers_DTM) {
                        layer.setEnable(selectA);
                    }
                    for (Layer layer : DataSaved.dxfLayers_POLY) {
                        layer.setEnable(selectA);
                    }
                    for (Layer layer : DataSaved.dxfLayers_POINT) {
                        layer.setEnable(selectA);
                    }

                    // 2. Aggiorna anche i DisplayItem (per aggiornare la UI)
                    for (DisplayItem displayItem : displayItems) {
                        if (displayItem.getType() == DisplayItem.TYPE_LAYER) {
                            displayItem.setEnable(selectA);
                        }
                    }

                    // 3. Notifica l'adapter per il refresh
                    notifyDataSetChanged();
                });
                if(selectA){
                    holder.selectAll.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.bg));
                }else {
                    holder.selectAll.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.gray));
                }
            }else {
                holder.selectAll.setEnabled(false);
                holder.selectAll.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.black));
            }

        } else if (item.getType() == DisplayItem.TYPE_LAYER) {
            holder.prjName.setVisibility(View.GONE);
            holder.selectAll.setVisibility(View.GONE);
            holder.layerName.setVisibility(View.VISIBLE);
            holder.layerStatus.setVisibility(View.VISIBLE);
            holder.layerColor.setVisibility(View.VISIBLE);
            holder.layerEnable.setVisibility(View.VISIBLE);
            holder.frameFaces.setVisibility(View.VISIBLE);
            holder.framePoly.setVisibility(View.VISIBLE);
            holder.frameLines.setVisibility(View.VISIBLE);
            holder.framePoints.setVisibility(View.VISIBLE);
            holder.layerName.setText(item.getLayerName());
            holder.layerColor.setBackgroundColor(myParseColor(AutoCADColor.getColor(String.valueOf(item.getColor()))));
            // Aggiorna il colore in base allo stato
            updateLayerEnableState(holder.layerEnable, item.isEnable());


            holder.layerEnable.setOnClickListener(v -> {
                // Cambia lo stato isEnable del layer
                boolean newState = !item.isEnable();
                item.setEnable(newState); // Aggiorna l'oggetto DisplayItem

                // Trova il layer corrispondente nella lista originale
                for (Layer layer : DataSaved.dxfLayers_DTM) {
                    if (layer.getLayerName().equals(item.getLayerName())) {
                        layer.setEnable(newState);
                        break;
                    }
                }
                for (Layer layer : DataSaved.dxfLayers_POLY) {
                    if (layer.getLayerName().equals(item.getLayerName())) {
                        layer.setEnable(newState);
                        break;
                    }
                }

                for (Layer layer : DataSaved.dxfLayers_POINT) {
                    if (layer.getLayerName().equals(item.getLayerName())) {
                        layer.setEnable(newState);
                        break;
                    }
                }
                // Aggiorna l'immagine in base al nuovo stato
                updateLayerEnableState(holder.layerEnable, newState);

                // Notifica il cambiamento
                notifyItemChanged(position);
            });
            holder.layerColor.setOnClickListener(v -> {
                String layerName = item.getLayerName();
                if (item.getColor() >= 0) {
                    if (layerInfoMap.containsKey(layerName)) {
                        String info = layerInfoMap.get(layerName);

                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Layer Info")
                                .setMessage("Layer: " + layerName + "\n\n" + info)
                                .setPositiveButton("OK", null)
                                .show();
                    } else {

                        Toast.makeText(v.getContext(), "No Info for Layer: " + layerName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    new CustomToast(MyApp.visibleActivity, "Disabled LAyer").show_alert();
                }
            });


            if (item.getColor() == 7 || item.getColor() == 0) {
                holder.layerColor.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.white));
            } else {
                holder.layerColor.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.black));
            }
            // Forza lo stato disabilitato se il colore è negativo
            if (item.getColor() < 0) {
                item.setEnable(false); // Forza il layer a disabilitato
                holder.layerEnable.setEnabled(false); // Disabilita il clic
                holder.layerName.setAlpha(0.4f);
                holder.layerColor.setAlpha(0.4f);

                holder.frameFaces.setVisibility(View.INVISIBLE);
                holder.framePoly.setVisibility(View.INVISIBLE);
                holder.frameLines.setVisibility(View.INVISIBLE);
                holder.framePoints.setVisibility(View.INVISIBLE);
                holder.layerStatus.setAlpha(0.5f);
                holder.layerEnable.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.gray));
                holder.layerStatus.setImageResource(R.drawable.divieto_96);
                holder.layerStatus.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity.getApplicationContext(), R.color.red));

            } else {
                holder.layerEnable.setEnabled(true); // Abilita il clic
                holder.layerName.setAlpha(1f);
                holder.layerColor.setAlpha(1f);

                holder.layerStatus.setAlpha(1f);

                holder.layerStatus.setImageResource(R.drawable.baseline_panorama_96);
                holder.layerStatus.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity.getApplicationContext(), R.color._____cancel_text));


                holder.layerStatus.setOnClickListener(view -> {
                    if (item.getColor() >= 0) {
                        String layerName = item.getLayerName();
                        if (layerName != null) {
                            try {
                                mostra(layerName); // Passa il nome del layer selezionato
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        new CustomToast(MyApp.visibleActivity, "Disabled Layer").show_alert();
                    }
                });


            }
            holder.faceNumber.setText(" " + setTesti(item.getLayerName())[0]);
            holder.polyNumber.setText(" " + setTesti(item.getLayerName())[1]);
            holder.lineNumber.setText(" " + setTesti(item.getLayerName())[2]);
            holder.pointNumber.setText(" " + setTesti(item.getLayerName())[3]);
            try {
                if (Integer.parseInt(holder.faceNumber.getText().toString().trim()) > 0) {
                    holder.frameFaces.setAlpha(1f);
                    holder.frameFaces.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    holder.frameFaces.setAlpha(0.2f);
                    holder.frameFaces.setBackgroundColor(Color.GRAY);
                }
            } catch (NumberFormatException e) {
                holder.frameFaces.setAlpha(0.2f);
                holder.frameFaces.setBackgroundColor(Color.GRAY);
            }

            try {
                if (Integer.parseInt(holder.polyNumber.getText().toString().trim()) > 0) {
                    holder.framePoly.setAlpha(1f);
                    holder.framePoly.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    holder.framePoly.setAlpha(0.2f);
                    holder.framePoly.setBackgroundColor(Color.GRAY);
                }
            } catch (NumberFormatException e) {
                holder.framePoly.setAlpha(0.2f);
                holder.framePoly.setBackgroundColor(Color.GRAY);
            }

            try {
                if (Integer.parseInt(holder.lineNumber.getText().toString().trim()) > 0) {
                    holder.frameLines.setAlpha(1f);
                    holder.frameLines.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    holder.frameLines.setAlpha(0.2f);
                    holder.frameLines.setBackgroundColor(Color.GRAY);
                }
            } catch (NumberFormatException e) {
                holder.frameLines.setAlpha(0.2f);
                holder.frameLines.setBackgroundColor(Color.GRAY);
            }
            try {
                if (Integer.parseInt(holder.pointNumber.getText().toString().trim()) > 0) {
                    holder.framePoints.setAlpha(1f);
                    holder.framePoints.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    holder.framePoints.setAlpha(0.2f);
                    holder.framePoints.setBackgroundColor(Color.GRAY);
                }
            } catch (NumberFormatException e) {
                holder.framePoints.setAlpha(0.2f);
                holder.framePoints.setBackgroundColor(Color.GRAY);
            }

        }

    }

    // Metodo per aggiornare lo stato visivo dell'immagine layerEnable
    private void updateLayerEnableState(ImageView layerEnable, boolean isEnabled) {
        if (isEnabled) {
            layerEnable.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.orange));
        } else {
            layerEnable.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, R.color._____cancel_text));
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).getType();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView prjName;
        public TextView layerName;
        public ImageView layerColor;
        public ImageView layerStatus;
        public ImageView layerEnable;
        public ImageView selectAll;
        public FrameLayout frameFaces;
        public FrameLayout framePoly;
        public FrameLayout frameLines;
        public FrameLayout framePoints;
        public TextView faceNumber;
        public TextView polyNumber;
        public TextView lineNumber;
        public TextView pointNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            prjName = itemView.findViewById(R.id.prjName);
            layerName = itemView.findViewById(R.id.path_tv);
            layerColor = itemView.findViewById(R.id.pointCk);
            layerStatus = itemView.findViewById(R.id.layerStatus);
            layerEnable = itemView.findViewById(R.id.layeren);
            selectAll = itemView.findViewById(R.id.selectAll);

            frameFaces = itemView.findViewById(R.id.frameFaces);
            framePoly = itemView.findViewById(R.id.framePoly3);
            frameLines = itemView.findViewById(R.id.framePoly2);
            framePoints = itemView.findViewById(R.id.framePoint);
            faceNumber = itemView.findViewById(R.id.textViewfaces);
            polyNumber = itemView.findViewById(R.id.textViewpoly3);
            lineNumber = itemView.findViewById(R.id.textViewpoly2);
            pointNumber = itemView.findViewById(R.id.textViewpoint);

        }

    }

    public static List<DisplayItem> getGroupedLayers() {
        // Usa un set per rimuovere i duplicati globali
        Set<Layer> allUniqueLayers = new HashSet<>();

        // Aggiungi i layer dalle tre liste
        if (DataSaved.dxfLayers_DTM != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_DTM);
        }
        if (DataSaved.dxfLayers_POLY != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_POLY);
        }
        if (DataSaved.dxfLayers_POINT != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_POINT);
        }

        // Mappa per raggruppare i layer per file
        Map<String, Set<Layer>> fileToLayers = new LinkedHashMap<>();

        // Raggruppa i layer per file
        for (Layer layer : allUniqueLayers) {
            String fileName = layer.getProjName();
            int lastIndex = fileName.lastIndexOf(".");
            String fileExtension = fileName.substring(lastIndex + 1).toLowerCase();
            if (fileExtension.equalsIgnoreCase("pstx") ||fileExtension.equalsIgnoreCase("dxf") || fileExtension.equalsIgnoreCase("xml")) {
                fileToLayers.putIfAbsent(fileName, new HashSet<>());
                fileToLayers.get(fileName).add(layer);
            }
        }

        // Genera la lista di DisplayItem
        List<DisplayItem> displayItems = new ArrayList<>();
        for (Map.Entry<String, Set<Layer>> entry : fileToLayers.entrySet()) {
            String fileName = entry.getKey();
            displayItems.add(new DisplayItem(DisplayItem.TYPE_FILE, fileName, null)); // Aggiungi il nome del file
            for (Layer layer : entry.getValue()) {
                displayItems.add(new DisplayItem(DisplayItem.TYPE_LAYER, fileName, layer.getLayerName(), layer.getColorState())); // Aggiungi i layer unici
            }
        }
        return displayItems;
    }


    private static void printLayerList(List<Layer> layerList, String listName) {
        try {
            if (layerList == null || layerList.isEmpty()) {
                //Log.d("Layer_", "La lista " + listName + " è vuota o non inizializzata.");
                return;
            }
            //Log.d("Layer_", "Contenuto della lista " + listName + ":");
           /* for (Layer layer : layerList) {
                Log.d("Layer_", "ProjName: " + layer.getProjName()
                        + ", LayerName: " + layer.getLayerName()
                        + ", ColorState: " + layer.getColorState()
                        + ", isEnaled: " + layer.isEnable());
            }*/
        } catch (Exception e) {
            Log.e("Layer_", e.toString());
        }
    }

    private int myParseColor(int color) {
        if (color == 0) {
            int[] rgb = new int[]{255, 255, 255};
            return Color.rgb(rgb[0], rgb[1], rgb[2]);
        } else {
            return color;
        }
    }

    private void popolaLayer() {
        for (Layer layer : DataSaved.dxfLayers_DTM) {
            if (layer == null || layer.getLayerName() == null) {
                Log.e("LayerInfo", "Layer o LayerName nullo in dxfLayers_DTM");
                continue;
            }

            StringBuilder content = new StringBuilder();

            // Verifica che la lista non sia null prima di accedervi
            long faceCount = DataSaved.dxfFaces != null ?
                    DataSaved.dxfFaces.stream()
                            .filter(face -> face.getLayer() != null && layer.getLayerName().equals(face.getLayer().getLayerName()))
                            .count() : 0;

            content.append("3DFACE: ").append(faceCount).append("\n");


            long polylineCount = DataSaved.polylines != null ?
                    DataSaved.polylines.stream()
                            .filter(polyline -> polyline.getLayer() != null && layer.getLayerName().equals(polyline.getLayer().getLayerName()))
                            .count() : 0;

            content.append("3D Polyline: ").append(polylineCount).append("\n");

            long polyline2DCount = DataSaved.polylines_2D != null ?
                    DataSaved.polylines_2D.stream()
                            .filter(polyline2D -> polyline2D.getLayer() != null && layer.getLayerName().equals(polyline2D.getLayer().getLayerName()))
                            .count() : 0;
            content.append("2D Objects: ").append(polyline2DCount).append("\n");

            long pointCount = DataSaved.points != null ?
                    DataSaved.points.stream()
                            .filter(point -> point.getLayer() != null && layer.getLayerName().equals(point.getLayer().getLayerName()))
                            .count() : 0;
            content.append("Points: ").append(pointCount);

            layerInfoMap.put(layer.getLayerName(), content.toString());
        }

        for (Layer layer : DataSaved.dxfLayers_POLY) {
            if (layer == null || layer.getLayerName() == null) {
                Log.e("LayerInfo", "Layer o LayerName nullo in dxfLayers_POLY");
                continue;
            }

            StringBuilder content = new StringBuilder();

            long faceCount = DataSaved.dxfFaces != null ?
                    DataSaved.dxfFaces.stream()
                            .filter(face -> face.getLayer() != null && layer.getLayerName().equals(face.getLayer().getLayerName()))
                            .count() : 0;
            content.append("3DFACE: ").append(faceCount).append("\n");

            long polylineCount = DataSaved.polylines != null ?
                    DataSaved.polylines.stream()
                            .filter(polyline -> polyline.getLayer() != null && layer.getLayerName().equals(polyline.getLayer().getLayerName()))
                            .count() : 0;
            content.append("3D Polyline: ").append(polylineCount).append("\n");

            long polyline2DCount = DataSaved.polylines_2D != null ?
                    DataSaved.polylines_2D.stream()
                            .filter(polyline2D -> polyline2D.getLayer() != null && layer.getLayerName().equals(polyline2D.getLayer().getLayerName()))
                            .count() : 0;
            content.append("2D Objects: ").append(polyline2DCount).append("\n");

            long pointCount = DataSaved.points != null ?
                    DataSaved.points.stream()
                            .filter(point -> point.getLayer() != null && layer.getLayerName().equals(point.getLayer().getLayerName()))
                            .count() : 0;
            content.append("Points: ").append(pointCount);

            layerInfoMap.put(layer.getLayerName(), content.toString());
        }

        for (Layer layer : DataSaved.dxfLayers_POINT) {
            if (layer == null || layer.getLayerName() == null) {
                Log.e("LayerInfo", "Layer o LayerName nullo in dxfLayers_POINT");
                continue;
            }

            StringBuilder content = new StringBuilder();

            long faceCount = DataSaved.dxfFaces != null ?
                    DataSaved.dxfFaces.stream()
                            .filter(face -> face.getLayer() != null && layer.getLayerName().equals(face.getLayer().getLayerName()))
                            .count() : 0;
            content.append("3DFACE: ").append(faceCount).append("\n");

            long polylineCount = DataSaved.polylines != null ?
                    DataSaved.polylines.stream()
                            .filter(polyline -> polyline.getLayer() != null && layer.getLayerName().equals(polyline.getLayer().getLayerName()))
                            .count() : 0;
            content.append("3D Polyline: ").append(polylineCount).append("\n");

            long polyline2DCount = DataSaved.polylines_2D != null ?
                    DataSaved.polylines_2D.stream()
                            .filter(polyline2D -> polyline2D.getLayer() != null && layer.getLayerName().equals(polyline2D.getLayer().getLayerName()))
                            .count() : 0;
            content.append("2D Objects: ").append(polyline2DCount).append("\n");

            long pointCount = DataSaved.points != null ?
                    DataSaved.points.stream()
                            .filter(point -> point.getLayer() != null && layer.getLayerName().equals(point.getLayer().getLayerName()))
                            .count() : 0;
            content.append("Points: ").append(pointCount);

            layerInfoMap.put(layer.getLayerName(), content.toString());
        }
    }

    public static String[] setTesti(String layerName) {
        List<Point3D> filteredPoints = new ArrayList<>();
        List<Polyline> filteredPolylines = new ArrayList<>();
        List<Polyline_2D> filteredPolylines2D = new ArrayList<>();
        List<Face3D> filteredFaces = new ArrayList<>();
        try {
            filteredFaces = DataSaved.dxfFaces.stream()
                    .filter(face -> face.getLayer() != null && face.getLayer().getLayerName().equals(layerName)&&
                            face.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        try {
            filteredPolylines = DataSaved.polylines.stream()
                    .filter(polyline -> polyline.getLayer() != null && polyline.getLayer().getLayerName().equals(layerName)&&polyline.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        try {
            filteredPolylines2D = DataSaved.polylines_2D.stream()
                    .filter(polyline2D -> polyline2D.getLayer() != null && polyline2D.getLayer().getLayerName().equals(layerName)&& polyline2D.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        try {
            filteredPoints = DataSaved.points.stream()
                    .filter(point -> point.getLayer() != null && point.getLayer().getLayerName().equals(layerName)&&point.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }

        return new String[]{String.valueOf(filteredFaces.size()), String.valueOf(filteredPolylines.size()), String.valueOf(filteredPolylines2D.size()), String.valueOf(filteredPoints.size())};
    }


    public static void mostra(String layerName) {
        List<Point3D> filteredPoints = new ArrayList<>();
        List<Polyline> filteredPolylines = new ArrayList<>();
        List<Polyline_2D> filteredPolylines2D = new ArrayList<>();
        List<Face3D> filteredFaces = new ArrayList<>();
        List<Arc> filteredArcs = new ArrayList<>();
        List<Circle> filteredCircles = new ArrayList<>();

        try {
            filteredPoints = DataSaved.points.stream()
                    .filter(point -> point.getLayer() != null && point.getLayer().getLayerName().equals(layerName)&&point.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }

        try {
            filteredPolylines = DataSaved.polylines.stream()
                    .filter(polyline -> polyline.getLayer() != null && polyline.getLayer().getLayerName().equals(layerName)&&polyline.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }


        try {
            filteredPolylines2D = DataSaved.polylines_2D.stream()
                    .filter(polyline2D -> polyline2D.getLayer() != null && polyline2D.getLayer().getLayerName().equals(layerName)&&polyline2D.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }

        try {
            filteredFaces = DataSaved.dxfFaces.stream()
                    .filter(face -> face.getLayer() != null && face.getLayer().getLayerName().equals(layerName)&&face.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        try {
            filteredArcs = DataSaved.arcs.stream()
                    .filter(arcs -> arcs.getLayer() != null && arcs.getLayer().getLayerName().equals(layerName)&&arcs.getLayer().isEnable())
                    .collect(Collectors.toList());

        } catch (Exception ignored) {

        }
        try {
            filteredCircles = DataSaved.circles.stream()
                    .filter(circle -> circle.getLayer() != null && circle.getLayer().getLayerName().equals(layerName)&&circle.getLayer().isEnable())
                    .collect(Collectors.toList());

        } catch (Exception ignored) {

        }


        if (filteredPoints.isEmpty() && filteredPolylines.isEmpty() && filteredPolylines2D.isEmpty() && filteredFaces.isEmpty()) {

            new CustomToast(MyApp.visibleActivity, "No Data").show_alert();
            return;
        }

        createOverview(filteredPoints, filteredPolylines, filteredPolylines2D, filteredFaces, filteredCircles, filteredArcs, layerName);
    }


    private static void createOverview(
            List<Point3D> points, List<Polyline> polylines, List<Polyline_2D> polylines2D, List<Face3D> faces, List<Circle> circles, List<Arc> arcs, String layerName) {

        int width = 1000; // Dimensioni del canvas??
        int height = 1000;
        int lastIndex = DataSaved.progettoSelected.lastIndexOf(".");
        String fileExtension = DataSaved.progettoSelected.substring(lastIndex + 1).toLowerCase();
        boolean isXML = fileExtension.equalsIgnoreCase("xml");

        // Trova i limiti del modello (bounding box)
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        // Itera su tutti i dati per determinare i limiti
        for (Point3D point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }
        for (Polyline polyline : polylines) {
            for (Point3D vertex : polyline.getVertices()) {
                minX = Math.min(minX, vertex.getX());
                minY = Math.min(minY, vertex.getY());
                maxX = Math.max(maxX, vertex.getX());
                maxY = Math.max(maxY, vertex.getY());
            }
        }
        for (Polyline_2D polyline2D : polylines2D) {
            for (int i = 0; i < polyline2D.getVertices().size() - 1; i++) {
                Point3D current = polyline2D.getVertices().get(i);
                Point3D next = polyline2D.getVertices().get(i + 1);

                // Aggiungi i vertici della polilinea 2D
                minX = Math.min(minX, current.getX());
                minY = Math.min(minY, current.getY());
                maxX = Math.max(maxX, current.getX());
                maxY = Math.max(maxY, current.getY());

                // Se il segmento ha un bulge, calcola i punti estremi dell'arco
                if (current.getBulge() != 0) {
                    double bulge = current.getBulge();
                    double chordLength = Math.sqrt(Math.pow(next.getX() - current.getX(), 2) + Math.pow(next.getY() - current.getY(), 2));
                    double theta = 4 * Math.atan(Math.abs(bulge));
                    double radius = (chordLength / 2) / Math.abs(Math.sin(theta / 2));

                    // Calcola il centro dell'arco
                    double midX = (current.getX() + next.getX()) / 2;
                    double midY = (current.getY() + next.getY()) / 2;
                    double sagitta = Math.sqrt(radius * radius - (chordLength / 2) * (chordLength / 2));
                    double dx = (next.getY() - current.getY());
                    double dy = (current.getX() - next.getX());
                    double norm = Math.sqrt(dx * dx + dy * dy);
                    dx /= norm;
                    dy /= norm;

                    double centerX = midX + dx * sagitta * (bulge > 0 ? -1 : 1);
                    double centerY = midY + dy * sagitta * (bulge > 0 ? -1 : 1);

                    // Calcola i punti estremi
                    minX = Math.min(minX, centerX - radius);
                    minY = Math.min(minY, centerY - radius);
                    maxX = Math.max(maxX, centerX + radius);
                    maxY = Math.max(maxY, centerY + radius);
                }
            }
        }
        for (Face3D face : faces) {
            minX = Math.min(minX, Math.min(face.getP1().getX(), Math.min(face.getP2().getX(), face.getP3().getX())));
            minY = Math.min(minY, Math.min(face.getP1().getY(), Math.min(face.getP2().getY(), face.getP3().getY())));
            maxX = Math.max(maxX, Math.max(face.getP1().getX(), Math.max(face.getP2().getX(), face.getP3().getX())));
            maxY = Math.max(maxY, Math.max(face.getP1().getY(), Math.max(face.getP2().getY(), face.getP3().getY())));
        }
        for (Circle circle : circles) {
            // Considera i limiti del cerchio
            double centerX = circle.getCenter().getX();
            double centerY = circle.getCenter().getY();
            double radius = circle.getRadius();

            minX = Math.min(minX, centerX - radius);
            minY = Math.min(minY, centerY - radius);
            maxX = Math.max(maxX, centerX + radius);
            maxY = Math.max(maxY, centerY + radius);
        }

        for (Arc arc : arcs) {
            // Considera i limiti dell'arco
            double centerX = arc.getCenter().getX();
            double centerY = arc.getCenter().getY();
            double radius = arc.getRadius();

            // Calcola i limiti degli angoli
            double startAngle = Math.toRadians(arc.getStartAngle());
            double endAngle = Math.toRadians(arc.getEndAngle());

            // Aggiungi punti estremi (inizio, fine e massimo)
            double startX = centerX + radius * Math.cos(startAngle);
            double startY = centerY + radius * Math.sin(startAngle);
            double endX = centerX + radius * Math.cos(endAngle);
            double endY = centerY + radius * Math.sin(endAngle);

            // Aggiorna min/max con i punti estremi
            minX = Math.min(minX, startX);
            minY = Math.min(minY, startY);
            maxX = Math.max(maxX, startX);
            maxY = Math.max(maxY, startY);

            minX = Math.min(minX, endX);
            minY = Math.min(minY, endY);
            maxX = Math.max(maxX, endX);
            maxY = Math.max(maxY, endY);

            // Aggiungi anche i punti massimi
            double midAngle = (startAngle + endAngle) / 2;
            double midX = centerX + radius * Math.cos(midAngle);
            double midY = centerY + radius * Math.sin(midAngle);

            minX = Math.min(minX, midX);
            minY = Math.min(minY, midY);
            maxX = Math.max(maxX, midX);
            maxY = Math.max(maxY, midY);
        }

        // Aggiungi un margine per evitare che i limiti esterni siano tagliati
        double marginX = (maxX - minX) * 0.05; // 5% del range in X
        double marginY = (maxY - minY) * 0.05; // 5% del range in Y

        minX -= marginX;
        maxX += marginX;
        minY -= marginY;
        maxY += marginY;

        // Calcola il fattore di scala uniformemente (il più piccolo tra scaleX e scaleY)
        double scaleX = width / (maxX - minX);
        double scaleY = height / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);

        // Centra il disegno nel canvas
        double offsetX = (width - (maxX - minX) * scale) / 2;
        double offsetY = (height - (maxY - minY) * scale) / 2;

        // Crea la bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawColor(MyColorClass.colorSfondo); // Sfondo


        // Disegna le facce
        if (faces != null) {
            paint.setStyle(Paint.Style.STROKE);
            for (Face3D face : faces) {
                if (isXML) {
                    paint.setColor(myCheckColor(AutoCADColor.getColor(String.valueOf(face.getColor()))));
                } else {
                    paint.setColor(myCheckColor(face.getColor()));
                }
                Path path = new Path();
                path.moveTo((float) ((face.getP1().getX() - minX) * scale + offsetX), (float) ((maxY - face.getP1().getY()) * scale + offsetY));
                path.lineTo((float) ((face.getP2().getX() - minX) * scale + offsetX), (float) ((maxY - face.getP2().getY()) * scale + offsetY));
                path.lineTo((float) ((face.getP3().getX() - minX) * scale + offsetX), (float) ((maxY - face.getP3().getY()) * scale + offsetY));
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        // Disegna le polilinee
        if (polylines != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            for (Polyline polyline : polylines) {
                paint.setColor(myCheckColor(polyline.getLayer().getColorState()));
                Path path = new Path();
                boolean first = true;
                for (Point3D vertex : polyline.getVertices()) {
                    float x = (float) ((vertex.getX() - minX) * scale + offsetX);
                    float y = (float) ((maxY - vertex.getY()) * scale + offsetY);
                    if (first) {
                        path.moveTo(x, y);
                        first = false;
                    } else {
                        path.lineTo(x, y);
                    }
                }
                canvas.drawPath(path, paint);
            }
        }

        // Disegna le polilinee 2D con bulge
        if (polylines2D != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Polyline_2D polyline2D : polylines2D) {
                paint.setColor(myCheckColor(polyline2D.getLayer().getColorState()));

                List<Point3D> vertices = polyline2D.getVertices();
                if (vertices.size() < 2) continue; // Una polilinea ha bisogno di almeno 2 punti

                for (int i = 0; i < vertices.size() - 1; i++) {
                    Point3D currentVertex = vertices.get(i);
                    Point3D nextVertex = vertices.get(i + 1);

                    float startX = (float) ((currentVertex.getX() - minX) * scale + offsetX);
                    float startY = (float) ((maxY - currentVertex.getY()) * scale + offsetY);
                    float endX = (float) ((nextVertex.getX() - minX) * scale + offsetX);
                    float endY = (float) ((maxY - nextVertex.getY()) * scale + offsetY);

                    float bulge = (float) currentVertex.getBulge();

                    if (bulge == 0) {
                        // Disegna una linea retta
                        canvas.drawLine(startX, startY, endX, endY, paint);
                    } else {
                        if (Math.abs(bulge) > 1) {
                            drawArcBetweenPointsM(canvas, new PointF(startX, startY), new PointF(endX, endY), -bulge, paint);

                        } else {
                            drawArcBetweenPoints(canvas, new PointF(startX, startY), new PointF(endX, endY), -bulge, paint);
                        }
                    }
                }
            }
        }


        // Disegna i punti
        if (points != null) {
            paint.setStyle(Paint.Style.FILL);
            for (Point3D point : points) {
                paint.setColor(myCheckColor(MyColorClass.colorPoint));
                float x = (float) ((point.getX() - minX) * scale + offsetX);
                float y = (float) ((maxY - point.getY()) * scale + offsetY);
                canvas.drawCircle(x, y, 3, paint);
            }
        }
        // Disegna i cerchi
        if (circles != null) {
            paint.setStyle(Paint.Style.STROKE);
            for (Circle circle : circles) {
                paint.setColor(myCheckColor(circle.getLayer().getColorState()));
                float cx = (float) ((circle.getCenter().getX() - minX) * scale + offsetX);
                float cy = (float) ((maxY - circle.getCenter().getY()) * scale + offsetY);
                float radius = (float) (circle.getRadius() * scale);
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
        // Disegna gli archi
        // Disegna gli archi
        if (arcs != null) {
            paint.setStyle(Paint.Style.STROKE);
            for (Arc arc : arcs) {
                paint.setColor(myCheckColor(arc.getLayer().getColorState()));

                // Centro dell'arco nel sistema di coordinate del canvas
                float cx = (float) ((arc.getCenter().getX() - minX) * scale + offsetX);
                float cy = (float) ((maxY - arc.getCenter().getY()) * scale + offsetY);

                // Raggio scalato
                float radius = (float) (arc.getRadius() * scale);

                // Rettangolo contenente l'arco
                RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

                // Calcolo degli angoli
                float startAngle = (float) arc.getStartAngle(); // Angolo iniziale
                float sweepAngle = (float) (arc.getEndAngle() - arc.getStartAngle()); // Sweep dell'arco

                // Normalizza startAngle per il sistema di coordinate Android
                startAngle = (360 - startAngle) % 360;

                // SweepAngle deve mantenere la direzione, ma essere coerente con il sistema Android
                sweepAngle = -sweepAngle;

                // Disegna l'arco
                canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
            }
        }


        // Mostra la bitmap
        showOverview(bitmap, layerName);
    }


    private static void showOverview(Bitmap bitmap, String layerName) {
        // Crea un AlertDialog.Builder per una finestra di dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(MyApp.visibleActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // Crea un layout personalizzato con ZoomableImageView
        LinearLayout layout = new LinearLayout(MyApp.visibleActivity);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundColor(Color.BLACK);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Configura la ZoomableImageView
        ZoomableImageView zoomableImageView = new ZoomableImageView(MyApp.visibleActivity);
        zoomableImageView.setImageBitmap(bitmap);
        zoomableImageView.setAdjustViewBounds(true);
        zoomableImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // Aggiungi la ZoomableImageView al layout
        layout.addView(zoomableImageView);

        // Configura il builder della dialog
        builder.setTitle("Layer Overview: " + layerName)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton("CLOSE", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private static int myCheckColor(int color) {
        try {


            String s = Integer.toHexString(color);
            if (DataSaved.temaSoftware == 0) {
                //sfondo nero
                if (s.equalsIgnoreCase("ff000000")) {
                    int[] rgb = new int[]{255, 255, 255};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }

            } else {
                if (s.equalsIgnoreCase("ffffffff")) {
                    int[] rgb = new int[]{0, 0, 0};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }
            }
        } catch (Exception e) {
            switch (DataSaved.temaSoftware) {
                case 0:
                    color = Color.WHITE;
                    break;
                case 1:
                    color = Color.BLACK;
                    break;
                case 2:
                    color = Color.BLACK;
                    break;
            }
        }
        return color;
    }

    private static void drawArcBetweenPointsM(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        // Step 1: Calcola la lunghezza della corda (distanza tra i punti di inizio e fine)
        double chordLength = Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));

        // Step 2: Calcola l'angolo di sweep usando il bulge
        double theta = 4 * Math.atan(Math.abs(bulge));

        // Step 3: Calcola il raggio dell'arco
        double radius = Math.abs((chordLength / 2) / Math.sin(theta / 2));

        // Step 4: Calcola il punto medio della corda
        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        // Step 5: Calcola la distanza perpendicolare dal punto medio al centro dell'arco
        double sagitta = Math.sqrt(radius * radius - (chordLength / 2) * (chordLength / 2));

        // Step 6: Determina la direzione perpendicolare alla corda
        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        // Normalizza la direzione perpendicolare
        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        // Step 7: Calcola il centro dell'arco (aggiustato in base al segno del bulge)
        float centerX = midX + perpX * (float) sagitta * (bulge > 0 ? -1 : 1);
        float centerY = midY + perpY * (float) sagitta * (bulge > 0 ? -1 : 1);

        // Step 8: Definisce il rettangolo di delimitazione dell'arco
        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        // Step 9: Calcola gli angoli di partenza e di sweep
        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        // Step 10: Calcola l'angolo di sweep
        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;  // Assicura che lo sweep sia in senso antiorario
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;  // Assicura che lo sweep sia in senso orario
            }
        }

        // Step 11: Disegna l'arco
        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }

    private static void drawArcBetweenPoints(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        // Step 1: Calcola la lunghezza della corda
        double distance = Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));

        // Step 2: Calcola l'angolo di sweep usando il bulge
        double theta = 4 * Math.atan(Math.abs(bulge));

        // Step 3: Calcola il raggio dell'arco
        double radius = (distance / 2) / Math.abs(Math.sin(theta / 2));

        // Step 4: Calcola il punto medio della corda
        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        // Step 5: Calcola la distanza perpendicolare dal punto medio al centro dell'arco
        double height = Math.sqrt(radius * radius - (distance / 2) * (distance / 2));

        // Step 6: Determina la direzione perpendicolare alla corda
        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        // Normalizza la direzione perpendicolare
        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        // Step 7: Calcola il centro dell'arco (aggiustato in base al segno del bulge)
        float centerX = midX + perpX * (float) height * (bulge > 0 ? 1 : -1);
        float centerY = midY + perpY * (float) height * (bulge > 0 ? 1 : -1);

        // Step 8: Definisce il rettangolo di delimitazione dell'arco
        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        // Step 9: Calcola gli angoli di partenza e di sweep
        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        // Calcola l'angolo di sweep
        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;  // Assicura che lo sweep sia in senso antiorario
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;  // Assicura che lo sweep sia in senso orario
            }
        }

        // Step 10: Correggi lo sweep dell'arco se necessario
        if (Math.abs(bulge) > 1 && Math.abs(sweepAngle) < 180) {
            sweepAngle = (bulge > 0) ? 360 - Math.abs(sweepAngle) : -(360 - Math.abs(sweepAngle));
        }

        // Step 11: Disegna l'arco
        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }


}
