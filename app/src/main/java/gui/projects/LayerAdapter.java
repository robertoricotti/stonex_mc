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
import dxf.Line;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import dxf.ZoomableImageView;
import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;

public class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {

    private final List<DisplayItem> displayItems;
    private final Map<String, String> layerInfoMap = new HashMap<>();
    public static boolean selectA;

    public LayerAdapter(List<DisplayItem> displayItems) {
        this.displayItems = displayItems != null ? displayItems : new ArrayList<>();

        printLayerList(DataSaved.dxfLayers_DTM, "DTM");
        printLayerList(DataSaved.dxfLayers_POLY, "POLY");
        printLayerList(DataSaved.dxfLayers_POINT, "POINT");

        syncDisplayItemsWithGlobalLayerState();
        popolaLayer();
        selectA = areAllLayersEnabled();
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
            bindFileRow(holder, item);
            return;
        }

        if (item.getType() == DisplayItem.TYPE_LAYER) {
            bindLayerRow(holder, item, position);
        }
    }

    private void bindFileRow(@NonNull ViewHolder holder, DisplayItem item) {
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

        String fileName = item.getFileName();
        if (fileName == null) fileName = "";

        holder.prjName.setText(fileName.replace(
                Environment.getExternalStorageDirectory().toString() + MyApp.folderPath + "/Projects", ""));

        if (holder.getAdapterPosition() == 0) {
            holder.selectAll.setEnabled(true);

            selectA = areAllLayersEnabled();
            tintSelectAll(holder.selectAll, selectA);

            holder.selectAll.setOnClickListener(view -> {
                boolean newState = !areAllLayersEnabled();
                selectA = newState;

                setAllLayersEnabled(newState);
                for (DisplayItem displayItem : displayItems) {
                    if (displayItem.getType() == DisplayItem.TYPE_LAYER) {
                        displayItem.setEnable(newState);
                    }
                }

                notifyDataSetChanged();
            });
        } else {
            holder.selectAll.setEnabled(false);
            holder.selectAll.setOnClickListener(null);
            holder.selectAll.setImageTintList(
                    ContextCompat.getColorStateList(MyApp.visibleActivity, R.color.black)
            );
        }
    }

    private void bindLayerRow(@NonNull ViewHolder holder, DisplayItem item, int position) {
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

        int swatchColor = myParseColor(item.getColor());
        holder.layerColor.setBackgroundColor(swatchColor);
        applySwatchIconTint(holder.layerColor, swatchColor);

        updateLayerEnableState(holder.layerEnable, item.isEnable());
        applyLayerVisualState(holder, item.isEnable());

        holder.layerEnable.setEnabled(true);
        holder.layerEnable.setOnClickListener(v -> {
            boolean newState = !item.isEnable();
            item.setEnable(newState);

            updateGlobalLayerState(item.getLayerName(), newState);
            updateLayerEnableState(holder.layerEnable, newState);
            applyLayerVisualState(holder, newState);

            selectA = areAllLayersEnabled();
            notifyItemChanged(position);
        });

        holder.layerColor.setOnClickListener(v -> {
            String layerName = item.getLayerName();
            if (layerName == null) {
                Toast.makeText(v.getContext(), "Layer name is null", Toast.LENGTH_SHORT).show();
                return;
            }

            if (layerInfoMap.containsKey(layerName)) {
                String info = layerInfoMap.get(layerName);

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Layer Info")
                        .setMessage("Layer: " + layerName + "\n\n" + info)
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                Toast.makeText(v.getContext(), "No Info for Layer: " + layerName, Toast.LENGTH_SHORT).show();
            }
        });

        holder.layerStatus.setOnClickListener(view -> {
            if (item.isEnable()) {
                String layerName = item.getLayerName();
                if (layerName != null) {
                    try {
                        mostra(layerName);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                new CustomToast(MyApp.visibleActivity, "Disabled Layer").show_alert();
            }
        });

        String[] testi = setTesti(item.getLayerName());
        holder.faceNumber.setText(" " + testi[0]);
        holder.polyNumber.setText(" " + testi[1]);
        holder.lineNumber.setText(" " + testi[2]);
        holder.pointNumber.setText(" " + testi[3]);

        updateCounterFrame(holder.frameFaces, holder.faceNumber);
        updateCounterFrame(holder.framePoly, holder.polyNumber);
        updateCounterFrame(holder.frameLines, holder.lineNumber);
        updateCounterFrame(holder.framePoints, holder.pointNumber);
    }

    private void applyLayerVisualState(@NonNull ViewHolder holder, boolean enabled) {
        holder.layerName.setAlpha(enabled ? 1f : 0.4f);
        holder.layerColor.setAlpha(enabled ? 1f : 0.4f);
        holder.layerStatus.setAlpha(enabled ? 1f : 0.5f);

        holder.frameFaces.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        holder.framePoly.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        holder.frameLines.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        holder.framePoints.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);

        if (enabled) {
            holder.layerStatus.setImageResource(R.drawable.baseline_panorama_96);
            holder.layerStatus.setImageTintList(ContextCompat.getColorStateList(
                    MyApp.visibleActivity.getApplicationContext(),
                    R.color._____cancel_text
            ));
        } else {
            holder.layerStatus.setImageResource(R.drawable.divieto_96);
            holder.layerStatus.setImageTintList(ContextCompat.getColorStateList(
                    MyApp.visibleActivity.getApplicationContext(),
                    R.color.red
            ));
        }
    }

    private void applySwatchIconTint(ImageView imageView, int color) {
        double luminance =
                (0.299 * Color.red(color)) +
                        (0.587 * Color.green(color)) +
                        (0.114 * Color.blue(color));

        int tintRes = luminance < 140 ? R.color.white : R.color.black;
        imageView.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, tintRes));
    }

    private void updateCounterFrame(FrameLayout frame, TextView countView) {
        try {
            if (Integer.parseInt(countView.getText().toString().trim()) > 0) {
                frame.setAlpha(1f);
                frame.setBackgroundColor(Color.TRANSPARENT);
            } else {
                frame.setAlpha(0.2f);
                frame.setBackgroundColor(Color.GRAY);
            }
        } catch (NumberFormatException e) {
            frame.setAlpha(0.2f);
            frame.setBackgroundColor(Color.GRAY);
        }
    }

    private void syncDisplayItemsWithGlobalLayerState() {
        for (DisplayItem item : displayItems) {
            if (item.getType() != DisplayItem.TYPE_LAYER) continue;

            Layer layer = findLayerByName(item.getLayerName());
            if (layer != null) {
                item.setEnable(layer.isEnable());
            }
        }
    }

    private Layer findLayerByName(String layerName) {
        if (layerName == null) return null;

        if (DataSaved.dxfLayers_DTM != null) {
            for (Layer layer : DataSaved.dxfLayers_DTM) {
                if (layer != null && layerName.equals(layer.getLayerName())) return layer;
            }
        }
        if (DataSaved.dxfLayers_POLY != null) {
            for (Layer layer : DataSaved.dxfLayers_POLY) {
                if (layer != null && layerName.equals(layer.getLayerName())) return layer;
            }
        }
        if (DataSaved.dxfLayers_POINT != null) {
            for (Layer layer : DataSaved.dxfLayers_POINT) {
                if (layer != null && layerName.equals(layer.getLayerName())) return layer;
            }
        }
        return null;
    }

    private void updateGlobalLayerState(String layerName, boolean newState) {
        if (layerName == null) return;

        if (DataSaved.dxfLayers_DTM != null) {
            for (Layer layer : DataSaved.dxfLayers_DTM) {
                if (layer != null && layerName.equals(layer.getLayerName())) {
                    layer.setEnable(newState);
                }
            }
        }

        if (DataSaved.dxfLayers_POLY != null) {
            for (Layer layer : DataSaved.dxfLayers_POLY) {
                if (layer != null && layerName.equals(layer.getLayerName())) {
                    layer.setEnable(newState);
                }
            }
        }

        if (DataSaved.dxfLayers_POINT != null) {
            for (Layer layer : DataSaved.dxfLayers_POINT) {
                if (layer != null && layerName.equals(layer.getLayerName())) {
                    layer.setEnable(newState);
                }
            }
        }
    }

    private boolean areAllLayersEnabled() {
        boolean foundAny = false;

        if (DataSaved.dxfLayers_DTM != null) {
            for (Layer layer : DataSaved.dxfLayers_DTM) {
                if (layer != null) {
                    foundAny = true;
                    if (!layer.isEnable()) return false;
                }
            }
        }

        if (DataSaved.dxfLayers_POLY != null) {
            for (Layer layer : DataSaved.dxfLayers_POLY) {
                if (layer != null) {
                    foundAny = true;
                    if (!layer.isEnable()) return false;
                }
            }
        }

        if (DataSaved.dxfLayers_POINT != null) {
            for (Layer layer : DataSaved.dxfLayers_POINT) {
                if (layer != null) {
                    foundAny = true;
                    if (!layer.isEnable()) return false;
                }
            }
        }

        return foundAny;
    }

    private void setAllLayersEnabled(boolean enabled) {
        if (DataSaved.dxfLayers_DTM != null) {
            for (Layer layer : DataSaved.dxfLayers_DTM) {
                if (layer != null) layer.setEnable(enabled);
            }
        }

        if (DataSaved.dxfLayers_POLY != null) {
            for (Layer layer : DataSaved.dxfLayers_POLY) {
                if (layer != null) layer.setEnable(enabled);
            }
        }

        if (DataSaved.dxfLayers_POINT != null) {
            for (Layer layer : DataSaved.dxfLayers_POINT) {
                if (layer != null) layer.setEnable(enabled);
            }
        }
    }

    private void tintSelectAll(ImageView imageView, boolean enabled) {
        int colorRes = enabled ? R.color.bg : R.color.gray;
        imageView.setImageTintList(ContextCompat.getColorStateList(MyApp.visibleActivity, colorRes));
    }

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
        Set<Layer> allUniqueLayers = new HashSet<>();

        if (DataSaved.dxfLayers_DTM != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_DTM);
        }
        if (DataSaved.dxfLayers_POLY != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_POLY);
        }
        if (DataSaved.dxfLayers_POINT != null) {
            allUniqueLayers.addAll(DataSaved.dxfLayers_POINT);
        }

        Map<String, Set<Layer>> fileToLayers = new LinkedHashMap<>();

        for (Layer layer : allUniqueLayers) {
            if (layer == null || layer.getProjName() == null) continue;

            String fileName = layer.getProjName();
            int lastIndex = fileName.lastIndexOf(".");
            if (lastIndex < 0) continue;

            String fileExtension = fileName.substring(lastIndex + 1).toLowerCase();
            if (fileExtension.equalsIgnoreCase("pstx")
                    || fileExtension.equalsIgnoreCase("dxf")
                    || fileExtension.equalsIgnoreCase("xml")) {
                fileToLayers.putIfAbsent(fileName, new HashSet<>());
                fileToLayers.get(fileName).add(layer);
            }
        }

        List<DisplayItem> displayItems = new ArrayList<>();
        for (Map.Entry<String, Set<Layer>> entry : fileToLayers.entrySet()) {
            String fileName = entry.getKey();
            displayItems.add(new DisplayItem(DisplayItem.TYPE_FILE, fileName, null));

            for (Layer layer : entry.getValue()) {
                DisplayItem layerItem = new DisplayItem(
                        DisplayItem.TYPE_LAYER,
                        fileName,
                        layer.getLayerName(),
                        layer.getColorState()
                );
                layerItem.setEnable(layer.isEnable());
                displayItems.add(layerItem);
            }
        }
        return displayItems;
    }

    private static void printLayerList(List<Layer> layerList, String listName) {
        try {
            if (layerList == null || layerList.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            Log.e("Layer_", e.toString());
        }
    }

    private int myParseColor(int color) {
        if (color == 0) {
            return Color.rgb(255, 255, 255);
        }
        return color;
    }

    private void popolaLayer() {
        populateLayerInfoFromList(DataSaved.dxfLayers_DTM, "dxfLayers_DTM");
        populateLayerInfoFromList(DataSaved.dxfLayers_POLY, "dxfLayers_POLY");
        populateLayerInfoFromList(DataSaved.dxfLayers_POINT, "dxfLayers_POINT");
    }

    private void populateLayerInfoFromList(List<Layer> layers, String logTag) {
        if (layers == null) return;

        for (Layer layer : layers) {
            if (layer == null || layer.getLayerName() == null) {
                Log.e("LayerInfo", "Layer o LayerName nullo in " + logTag);
                continue;
            }

            String layerName = layer.getLayerName();
            StringBuilder content = new StringBuilder();

            long faceCount = DataSaved.dxfFaces != null
                    ? DataSaved.dxfFaces.stream()
                    .filter(face -> face.getLayer() != null
                            && layerName.equals(face.getLayer().getLayerName()))
                    .count()
                    : 0;
            content.append("3DFACE: ").append(faceCount).append("\n");

            long polylineCount = DataSaved.polylines != null
                    ? DataSaved.polylines.stream()
                    .filter(polyline -> polyline.getLayer() != null
                            && layerName.equals(polyline.getLayer().getLayerName()))
                    .count()
                    : 0;
            content.append("3D Polyline: ").append(polylineCount).append("\n");

            long polyline2DCount = DataSaved.polylines_2D != null
                    ? DataSaved.polylines_2D.stream()
                    .filter(polyline2D -> polyline2D.getLayer() != null
                            && layerName.equals(polyline2D.getLayer().getLayerName()))
                    .count()
                    : 0;

            long lineCount = DataSaved.lines_2D != null
                    ? DataSaved.lines_2D.stream()
                    .filter(line -> line.getLayer() != null
                            && layerName.equals(line.getLayer().getLayerName()))
                    .count()
                    : 0;

            long circleCount = DataSaved.circles != null
                    ? DataSaved.circles.stream()
                    .filter(circle -> circle.getLayer() != null
                            && layerName.equals(circle.getLayer().getLayerName()))
                    .count()
                    : 0;

            long arcCount = DataSaved.arcs != null
                    ? DataSaved.arcs.stream()
                    .filter(arc -> arc.getLayer() != null
                            && layerName.equals(arc.getLayer().getLayerName()))
                    .count()
                    : 0;

            content.append("2D Objects: ")
                    .append(polyline2DCount + lineCount + circleCount + arcCount)
                    .append("\n");
            content.append("  - LWPolyline: ").append(polyline2DCount).append("\n");
            content.append("  - Line: ").append(lineCount).append("\n");
            content.append("  - Circle: ").append(circleCount).append("\n");
            content.append("  - Arc: ").append(arcCount).append("\n");

            long pointCount = DataSaved.points != null
                    ? DataSaved.points.stream()
                    .filter(point -> point.getLayer() != null
                            && layerName.equals(point.getLayer().getLayerName()))
                    .count()
                    : 0;
            content.append("Points: ").append(pointCount);

            layerInfoMap.put(layerName, content.toString());
        }
    }

    public static String[] setTesti(String layerName) {
        List<Point3D> filteredPoints = new ArrayList<>();
        List<Polyline> filteredPolylines = new ArrayList<>();
        List<Polyline_2D> filteredPolylines2D = new ArrayList<>();
        List<Face3D> filteredFaces = new ArrayList<>();
        List<Line> filteredLines = new ArrayList<>();
        List<Circle> filteredCircles = new ArrayList<>();
        List<Arc> filteredArcs = new ArrayList<>();

        try {
            filteredFaces = DataSaved.dxfFaces.stream()
                    .filter(face -> face.getLayer() != null
                            && face.getLayer().getLayerName().equals(layerName)
                            && face.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredPolylines = DataSaved.polylines.stream()
                    .filter(polyline -> polyline.getLayer() != null
                            && polyline.getLayer().getLayerName().equals(layerName)
                            && polyline.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredPolylines2D = DataSaved.polylines_2D.stream()
                    .filter(polyline2D -> polyline2D.getLayer() != null
                            && polyline2D.getLayer().getLayerName().equals(layerName)
                            && polyline2D.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredLines = DataSaved.lines_2D.stream()
                    .filter(line -> line.getLayer() != null
                            && line.getLayer().getLayerName().equals(layerName)
                            && line.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredCircles = DataSaved.circles.stream()
                    .filter(circle -> circle.getLayer() != null
                            && circle.getLayer().getLayerName().equals(layerName)
                            && circle.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredArcs = DataSaved.arcs.stream()
                    .filter(arc -> arc.getLayer() != null
                            && arc.getLayer().getLayerName().equals(layerName)
                            && arc.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredPoints = DataSaved.points.stream()
                    .filter(point -> point.getLayer() != null
                            && point.getLayer().getLayerName().equals(layerName)
                            && point.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        int twoDObjects = filteredPolylines2D.size()
                + filteredLines.size()
                + filteredCircles.size()
                + filteredArcs.size();

        return new String[]{
                String.valueOf(filteredFaces.size()),
                String.valueOf(filteredPolylines.size()),
                String.valueOf(twoDObjects),
                String.valueOf(filteredPoints.size())
        };
    }

    public static void mostra(String layerName) {
        List<Point3D> filteredPoints = new ArrayList<>();
        List<Polyline> filteredPolylines = new ArrayList<>();
        List<Polyline_2D> filteredPolylines2D = new ArrayList<>();
        List<Face3D> filteredFaces = new ArrayList<>();
        List<Line> filteredLines = new ArrayList<>();
        List<Arc> filteredArcs = new ArrayList<>();
        List<Circle> filteredCircles = new ArrayList<>();

        try {
            filteredPoints = DataSaved.points.stream()
                    .filter(point -> point.getLayer() != null
                            && point.getLayer().getLayerName().equals(layerName)
                            && point.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredPolylines = DataSaved.polylines.stream()
                    .filter(polyline -> polyline.getLayer() != null
                            && polyline.getLayer().getLayerName().equals(layerName)
                            && polyline.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredPolylines2D = DataSaved.polylines_2D.stream()
                    .filter(polyline2D -> polyline2D.getLayer() != null
                            && polyline2D.getLayer().getLayerName().equals(layerName)
                            && polyline2D.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredFaces = DataSaved.dxfFaces.stream()
                    .filter(face -> face.getLayer() != null
                            && face.getLayer().getLayerName().equals(layerName)
                            && face.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredLines = DataSaved.lines_2D.stream()
                    .filter(line -> line.getLayer() != null
                            && line.getLayer().getLayerName().equals(layerName)
                            && line.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredArcs = DataSaved.arcs.stream()
                    .filter(arc -> arc.getLayer() != null
                            && arc.getLayer().getLayerName().equals(layerName)
                            && arc.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        try {
            filteredCircles = DataSaved.circles.stream()
                    .filter(circle -> circle.getLayer() != null
                            && circle.getLayer().getLayerName().equals(layerName)
                            && circle.getLayer().isEnable())
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        if (filteredPoints.isEmpty()
                && filteredPolylines.isEmpty()
                && filteredPolylines2D.isEmpty()
                && filteredFaces.isEmpty()
                && filteredLines.isEmpty()
                && filteredCircles.isEmpty()
                && filteredArcs.isEmpty()) {
            new CustomToast(MyApp.visibleActivity, "No Data").show_alert();
            return;
        }

        createOverview(
                filteredPoints,
                filteredPolylines,
                filteredPolylines2D,
                filteredFaces,
                filteredLines,
                filteredCircles,
                filteredArcs,
                layerName
        );
    }

    private static void createOverview(
            List<Point3D> points,
            List<Polyline> polylines,
            List<Polyline_2D> polylines2D,
            List<Face3D> faces,
            List<Line> lines,
            List<Circle> circles,
            List<Arc> arcs,
            String layerName) {

        int width = 1000;
        int height = 1000;

        int lastIndex = DataSaved.progettoSelected.lastIndexOf(".");
        String fileExtension = DataSaved.progettoSelected.substring(lastIndex + 1).toLowerCase();
        boolean isXML = fileExtension.equalsIgnoreCase("xml");

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

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

                minX = Math.min(minX, current.getX());
                minY = Math.min(minY, current.getY());
                maxX = Math.max(maxX, current.getX());
                maxY = Math.max(maxY, current.getY());

                minX = Math.min(minX, next.getX());
                minY = Math.min(minY, next.getY());
                maxX = Math.max(maxX, next.getX());
                maxY = Math.max(maxY, next.getY());

                if (current.getBulge() != 0) {
                    double bulge = current.getBulge();
                    double chordLength = Math.sqrt(
                            Math.pow(next.getX() - current.getX(), 2)
                                    + Math.pow(next.getY() - current.getY(), 2)
                    );
                    double theta = 4 * Math.atan(Math.abs(bulge));
                    double radius = (chordLength / 2) / Math.abs(Math.sin(theta / 2));

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

        for (Line line : lines) {
            minX = Math.min(minX, Math.min(line.getStart().getX(), line.getEnd().getX()));
            minY = Math.min(minY, Math.min(line.getStart().getY(), line.getEnd().getY()));
            maxX = Math.max(maxX, Math.max(line.getStart().getX(), line.getEnd().getX()));
            maxY = Math.max(maxY, Math.max(line.getStart().getY(), line.getEnd().getY()));
        }

        for (Circle circle : circles) {
            double centerX = circle.getCenter().getX();
            double centerY = circle.getCenter().getY();
            double radius = circle.getRadius();

            minX = Math.min(minX, centerX - radius);
            minY = Math.min(minY, centerY - radius);
            maxX = Math.max(maxX, centerX + radius);
            maxY = Math.max(maxY, centerY + radius);
        }

        for (Arc arc : arcs) {
            double centerX = arc.getCenter().getX();
            double centerY = arc.getCenter().getY();
            double radius = arc.getRadius();

            double startAngle = Math.toRadians(arc.getStartAngle());
            double endAngle = Math.toRadians(arc.getEndAngle());

            double startX = centerX + radius * Math.cos(startAngle);
            double startY = centerY + radius * Math.sin(startAngle);
            double endX = centerX + radius * Math.cos(endAngle);
            double endY = centerY + radius * Math.sin(endAngle);

            minX = Math.min(minX, startX);
            minY = Math.min(minY, startY);
            maxX = Math.max(maxX, startX);
            maxY = Math.max(maxY, startY);

            minX = Math.min(minX, endX);
            minY = Math.min(minY, endY);
            maxX = Math.max(maxX, endX);
            maxY = Math.max(maxY, endY);

            double midAngle = (startAngle + endAngle) / 2;
            double midX = centerX + radius * Math.cos(midAngle);
            double midY = centerY + radius * Math.sin(midAngle);

            minX = Math.min(minX, midX);
            minY = Math.min(minY, midY);
            maxX = Math.max(maxX, midX);
            maxY = Math.max(maxY, midY);
        }

        if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(maxX) || !Double.isFinite(maxY)) {
            new CustomToast(MyApp.visibleActivity, "No Data").show_alert();
            return;
        }

        double marginX = Math.max((maxX - minX) * 0.05, 1e-6);
        double marginY = Math.max((maxY - minY) * 0.05, 1e-6);

        minX -= marginX;
        maxX += marginX;
        minY -= marginY;
        maxY += marginY;

        double rangeX = Math.max(maxX - minX, 1e-9);
        double rangeY = Math.max(maxY - minY, 1e-9);

        double scaleX = width / rangeX;
        double scaleY = height / rangeY;
        double scale = Math.min(scaleX, scaleY);

        double offsetX = (width - rangeX * scale) / 2;
        double offsetY = (height - rangeY * scale) / 2;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawColor(MyColorClass.colorSfondo);

        if (faces != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Face3D face : faces) {
                if (isXML) {
                    paint.setColor(myCheckColor(AutoCADColor.getColor(String.valueOf(face.getColor()))));
                } else {
                    paint.setColor(myCheckColor(face.getColor()));
                }

                Path path = new Path();
                path.moveTo(
                        (float) ((face.getP1().getX() - minX) * scale + offsetX),
                        (float) ((maxY - face.getP1().getY()) * scale + offsetY)
                );
                path.lineTo(
                        (float) ((face.getP2().getX() - minX) * scale + offsetX),
                        (float) ((maxY - face.getP2().getY()) * scale + offsetY)
                );
                path.lineTo(
                        (float) ((face.getP3().getX() - minX) * scale + offsetX),
                        (float) ((maxY - face.getP3().getY()) * scale + offsetY)
                );
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        if (polylines != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Polyline polyline : polylines) {
                paint.setColor(myCheckColor(polyline.getLineColor()));
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

        if (polylines2D != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Polyline_2D polyline2D : polylines2D) {
                paint.setColor(myCheckColor(polyline2D.getLineColor()));

                List<Point3D> vertices = polyline2D.getVertices();
                if (vertices.size() < 2) continue;

                for (int i = 0; i < vertices.size() - 1; i++) {
                    Point3D currentVertex = vertices.get(i);
                    Point3D nextVertex = vertices.get(i + 1);

                    float startX = (float) ((currentVertex.getX() - minX) * scale + offsetX);
                    float startY = (float) ((maxY - currentVertex.getY()) * scale + offsetY);
                    float endX = (float) ((nextVertex.getX() - minX) * scale + offsetX);
                    float endY = (float) ((maxY - nextVertex.getY()) * scale + offsetY);

                    float bulge = (float) currentVertex.getBulge();

                    if (bulge == 0) {
                        canvas.drawLine(startX, startY, endX, endY, paint);
                    } else {
                        if (Math.abs(bulge) > 1) {
                            drawArcBetweenPointsM(
                                    canvas,
                                    new PointF(startX, startY),
                                    new PointF(endX, endY),
                                    -bulge,
                                    paint
                            );
                        } else {
                            drawArcBetweenPoints(
                                    canvas,
                                    new PointF(startX, startY),
                                    new PointF(endX, endY),
                                    -bulge,
                                    paint
                            );
                        }
                    }
                }
            }
        }

        if (lines != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Line line : lines) {
                paint.setColor(myCheckColor(line.getColor()));

                float startX = (float) ((line.getStart().getX() - minX) * scale + offsetX);
                float startY = (float) ((maxY - line.getStart().getY()) * scale + offsetY);
                float endX = (float) ((line.getEnd().getX() - minX) * scale + offsetX);
                float endY = (float) ((maxY - line.getEnd().getY()) * scale + offsetY);

                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        }

        if (points != null) {
            paint.setStyle(Paint.Style.FILL);

            for (Point3D point : points) {
                paint.setColor(myCheckColor(MyColorClass.colorPoint));
                float x = (float) ((point.getX() - minX) * scale + offsetX);
                float y = (float) ((maxY - point.getY()) * scale + offsetY);
                canvas.drawCircle(x, y, 3, paint);
            }
        }

        if (circles != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Circle circle : circles) {
                paint.setColor(myCheckColor(circle.getColor()));
                float cx = (float) ((circle.getCenter().getX() - minX) * scale + offsetX);
                float cy = (float) ((maxY - circle.getCenter().getY()) * scale + offsetY);
                float radius = (float) (circle.getRadius() * scale);
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }

        if (arcs != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (Arc arc : arcs) {
                paint.setColor(myCheckColor(arc.getColor()));

                float cx = (float) ((arc.getCenter().getX() - minX) * scale + offsetX);
                float cy = (float) ((maxY - arc.getCenter().getY()) * scale + offsetY);
                float radius = (float) (arc.getRadius() * scale);

                RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

                float startAngle = (float) arc.getStartAngle();
                float sweepAngle = (float) (arc.getEndAngle() - arc.getStartAngle());

                startAngle = (360 - startAngle) % 360;
                sweepAngle = -sweepAngle;

                canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
            }
        }

        showOverview(bitmap, layerName);
    }

    private static void showOverview(Bitmap bitmap, String layerName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MyApp.visibleActivity,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );

        LinearLayout layout = new LinearLayout(MyApp.visibleActivity);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        layout.setBackgroundColor(Color.BLACK);
        layout.setOrientation(LinearLayout.VERTICAL);

        ZoomableImageView zoomableImageView = new ZoomableImageView(MyApp.visibleActivity);
        zoomableImageView.setImageBitmap(bitmap);
        zoomableImageView.setAdjustViewBounds(true);
        zoomableImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        layout.addView(zoomableImageView);

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
                if (s.equalsIgnoreCase("ff000000")) {
                    return Color.rgb(255, 255, 255);
                } else {
                    return color;
                }
            } else {
                if (s.equalsIgnoreCase("ffffffff")) {
                    return Color.rgb(0, 0, 0);
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
                case 2:
                    color = Color.BLACK;
                    break;
            }
        }
        return color;
    }

    private static void drawArcBetweenPointsM(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        double chordLength = Math.sqrt(
                Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2)
        );
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = Math.abs((chordLength / 2) / Math.sin(theta / 2));

        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        double sagitta = Math.sqrt(radius * radius - (chordLength / 2) * (chordLength / 2));

        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        float centerX = midX + perpX * (float) sagitta * (bulge > 0 ? -1 : 1);
        float centerY = midY + perpY * (float) sagitta * (bulge > 0 ? -1 : 1);

        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;
            }
        }

        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }

    private static void drawArcBetweenPoints(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        double distance = Math.sqrt(
                Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2)
        );
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = (distance / 2) / Math.abs(Math.sin(theta / 2));

        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        double height = Math.sqrt(radius * radius - (distance / 2) * (distance / 2));

        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        float centerX = midX + perpX * (float) height * (bulge > 0 ? 1 : -1);
        float centerY = midY + perpY * (float) height * (bulge > 0 ? 1 : -1);

        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;
            }
        }

        if (Math.abs(bulge) > 1 && Math.abs(sweepAngle) < 180) {
            sweepAngle = (bulge > 0)
                    ? 360 - Math.abs(sweepAngle)
                    : -(360 - Math.abs(sweepAngle));
        }

        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }
}