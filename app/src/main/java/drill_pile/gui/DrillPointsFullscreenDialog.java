package drill_pile.gui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import gui.MyApp;
import gui.dialogs_and_toast.CustomQwertyDialog;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;

public class DrillPointsFullscreenDialog extends DialogFragment {

    @Nullable
    private OnHoleActionListener holeActionListener;
    CustomQwertyDialog customQwertyDialog;

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DISPLAY_FACTOR = "arg_display_factor";
    private boolean isFullScreen = true;
    private LinearLayout toolbar;
    private ImageView chiudi, espandi;
    private TextView titolo;
    private RecyclerView rvHeader, rvRows;
    private Button btnClose, btnCopyCsv;
    private EditText etSearch;

    private final List<Point3D_Drill> original = new ArrayList<>();
    private final List<Point3D_Drill> filtered = new ArrayList<>();

    private HeaderAdapter headerAdapter;
    private RowsAdapter rowsAdapter;

    // fattore di visualizzazione: metri->(metri/us-ft/survey-ft)
    private double displayFactor = 1.0;

    // Colonne dati (checkbox/status fisse a sinistra)
    // ✅ aggiunta colonna finale deltaZ
    private final List<String> columns = Arrays.asList(
            "Row", "Hole",
            "Start East", "Start North", "Start Z",
            "End East", "End North", "End Z",
            "Depth", "Length", "Bearing", "Tilt",
            "Delta Z", "Diameter", "Description"
    );


    /**
     * @param displayFactor moltiplicatore per mostrare coordinate/distanze:
     *                      metri->metri = 1
     *                      metri->us-ft = 3.280839895
     *                      metri->us survey ft = 3.280833333
     */
    public static DrillPointsFullscreenDialog newInstance(String title, double displayFactor) {
        DrillPointsFullscreenDialog d = new DrillPointsFullscreenDialog();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        b.putDouble(ARG_DISPLAY_FACTOR, displayFactor);
        d.setArguments(b);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FullscreenActivity.setFullScreen(dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_drillpoints, container, false);
        customQwertyDialog = new CustomQwertyDialog(MyApp.visibleActivity, "");

        toolbar = v.findViewById(R.id.toolbar);
        rvHeader = v.findViewById(R.id.rvHeader);
        rvRows = v.findViewById(R.id.rvRows);
        btnClose = v.findViewById(R.id.btnClose);
        btnCopyCsv = v.findViewById(R.id.btnCopyCsv);
        etSearch = v.findViewById(R.id.etSearch);
        chiudi = v.findViewById(R.id.chiudi);
        espandi = v.findViewById(R.id.espandi);
        titolo = v.findViewById(R.id.titolo);

        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "Drill Points";
        titolo.setText(title);

        // displayFactor passato da fuori (es ReadProjectService.conversionFactor)
        if (getArguments() != null)
            displayFactor = getArguments().getDouble(ARG_DISPLAY_FACTOR, 1.0);

        chiudi.setOnClickListener(view -> {
            dismiss();
        });

        btnClose.setOnClickListener(view -> dismiss());

        // ✅ carico i punti da DataSaved (sempre in metri)
        setPoints(DataSaved.drill_points);

        rvHeader.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        headerAdapter = new HeaderAdapter(columns);
        rvHeader.setAdapter(headerAdapter);

        rvRows.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        rvRows.addItemDecoration(new DividerItemDecoration1dp(requireContext()));

        rowsAdapter = new RowsAdapter(
                columns,
                filtered,
                displayFactor,
                () -> rowsAdapter.notifyDataSetChanged(),
                holeActionListener
        );

        rvRows.setAdapter(rowsAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etSearch.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(etSearch);
            }
        });
        espandi.setOnClickListener(view -> {
            isFullScreen = !isFullScreen;
            applyWindowMode();
            updateFullscreenIcon();
        });

        btnCopyCsv.setOnClickListener(view -> copyCsvToClipboard());

        rowsAdapter.notifyDataSetChanged();
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        isFullScreen = true;
        applyWindowMode();
        updateFullscreenIcon();

    }

    private void applyWindowMode() {
        Dialog dialog = getDialog();
        if (dialog == null) return;

        Window window = dialog.getWindow();
        if (window == null) return;

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        WindowManager.LayoutParams lp = window.getAttributes();

        if (isFullScreen) {
            // ✅ FULLSCREEN
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.x = 0;
            lp.y = 0;

            // Se hai una tua funzione fullscreen activity, applicala qui (opzionale)
            // FullscreenActivity.setFullScreen(dialog);

        } else {
            // ✅ SIDE PANEL (metà sinistra come ora)
            lp.width = screenWidth / 2;
            lp.height = screenHeight; // come stai facendo ora
            lp.gravity = Gravity.START | Gravity.BOTTOM;
            lp.x = 0;
            lp.y = 0;
        }

        window.setAttributes(lp);

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        FullscreenActivity.setFullScreen(dialog);
    }


    private void updateFullscreenIcon() {
        if (espandi == null) return;
        espandi.setImageResource(
                isFullScreen ? R.drawable.baseline_close_fullscreen_96
                        : R.drawable.baseline_open_in_full_96
        );
    }


    private void setPoints(List<Point3D_Drill> points) {
        original.clear();
        if (points != null) original.addAll(points);
        filtered.clear();
        filtered.addAll(original);
    }

    // -------------------------
    // Filter
    // -------------------------
    private void applyFilter(String q) {
        String query = q.trim().toLowerCase(Locale.ROOT);
        filtered.clear();

        if (query.isEmpty()) {
            filtered.addAll(original);
        } else {
            for (Point3D_Drill p : original) {
                String hay = (safe(p.getRowId()) + " " + safe(p.getId()) + " " + safe(p.getDescription()))
                        .toLowerCase(Locale.ROOT);
                if (hay.contains(query)) filtered.add(p);
            }
        }
        rowsAdapter.notifyDataSetChanged();
    }

    // -------------------------
    // CSV copy (in unità visualizzazione)
    // -------------------------
    private void copyCsvToClipboard() {
        StringBuilder sb = new StringBuilder();

        sb.append("selected,status,");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(columns.get(i));
        }
        sb.append("\n");

        for (Point3D_Drill p : filtered) {
            sb.append(isSelected(p) ? "1" : "0").append(",");
            sb.append(statusLabel(p)).append(",");

            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(escapeCsv(valueForColumn(p, columns.get(i), displayFactor)));
            }
            sb.append("\n");
        }

        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("DrillPoints CSV", sb.toString()));
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        if (needsQuotes) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    // -------------------------
    // Selection + Status
    // -------------------------
    private static boolean isSelected(Point3D_Drill p) {
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (sel == null || p == null) return false;

        if (sel.getRowId() != null && sel.getId() != null &&
                p.getRowId() != null && p.getId() != null) {
            return sel.getRowId().equals(p.getRowId()) && sel.getId().equals(p.getId());
        }
        if (sel.getId() != null && p.getId() != null) {
            return sel.getId().equals(p.getId());
        }
        return sel == p;
    }

    private static int statusValue(Point3D_Drill p) {
        Integer s = p.getStatus();
        return (s == null) ? 0 : s; // default: DA FARE
    }

    private static boolean isSelectable(Point3D_Drill p) {
        return statusValue(p) == 0;
    }

    private static String statusLabel(Point3D_Drill p) {
        int s = statusValue(p);
        if (s == 1) return "DONE";
        if (s == -1) return "ABORTED";
        return "TO DO";
    }

    // -------------------------
    // Column formatting
    // -------------------------
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String fmtD(Double d) {
        if (d == null) return "";
        return String.format(Locale.US, "%.3f", d);
    }

    private static String fmtDConv(Double metersVal, double displayFactor) {
        if (metersVal == null) return "";
        return String.format(Locale.US, "%.3f", metersVal * displayFactor);
    }

    // ✅ PoleId formatter: mostra solo il numero
    private static String formatPoleId(String idRaw) {
        if (idRaw == null) return "";

        String id = idRaw.trim();
        if (id.isEmpty()) return "";

        // caso "row-id" tipo "1-23" -> "23"
        int dash = id.lastIndexOf('-');
        if (dash >= 0 && dash < id.length() - 1) {
            String right = id.substring(dash + 1).trim();
            if (right.matches("\\d+")) return right;
        }

        // caso "row.id" tipo "1.23" -> "23"
        int dot = id.lastIndexOf('.');
        if (dot >= 0 && dot < id.length() - 1) {
            String right = id.substring(dot + 1).trim();
            if (right.matches("\\d+")) return right;
        }

        // se è solo numero, ok
        return id;
    }

    // ✅ delta quota: (headZ - endZ) convertito
    private static Double deltaZMeters(Point3D_Drill p) {
        if (p.getHeadZ() == null || p.getEndZ() == null) return null;
        return (p.getHeadZ() - p.getEndZ());
    }

    private static String valueForColumn(Point3D_Drill p, String col, double displayFactor) {
        switch (col) {
            case "Row":
                return p.getRowId();

            case "Hole":
                // ✅ se rowId è vuoto, mostra ID completo (consistente col parser)
                if (p.getRowId() == null || p.getRowId().trim().isEmpty()) {
                    return safe(p.getId());
                }
                // ✅ altrimenti puoi continuare a mostrare solo la parte "hole"
                return formatPoleId(p.getId());

            case "Description":
                return p.getDescription();

            // ✅ coordinate e distanze convertite al volo (salvate in metri)
            case "Start East":
                return fmtDConv(p.getHeadX(), displayFactor);
            case "Start North":
                return fmtDConv(p.getHeadY(), displayFactor);
            case "Start Z":
                return fmtDConv(p.getHeadZ(), displayFactor);

            case "End East":
                return fmtDConv(p.getEndX(), displayFactor);
            case "End North":
                return fmtDConv(p.getEndY(), displayFactor);
            case "End Z":
                return fmtDConv(p.getEndZ(), displayFactor);

            // Angoli: gradi sempre, nessuna conversione
            case "Bearing":
                return fmtD(p.getHeadingDeg());
            case "Tilt":
                return fmtD(p.getTilt());

            // Depth/Length: sono distanze → converti
            case "Depth":
                return fmtDConv(p.getDepth(), displayFactor);
            case "Length":
                return fmtDConv(p.getLength(), displayFactor);

            // diametro (se ti interessa in futuro: decidere unità)
            case "Diameter":
                return fmtD(p.getDiameter());

            // ✅ nuova colonna: delta quota
            case "Delta Z":
                return fmtDConv(deltaZMeters(p), displayFactor);
        }
        return "";
    }

    // -------------------------
    // Adapters
    // -------------------------
    private static class HeaderAdapter extends RecyclerView.Adapter<RowVH> {
        private final List<String> cols;

        HeaderAdapter(List<String> cols) {
            this.cols = cols;
        }

        @NonNull
        @Override
        public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drill_point_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RowVH holder, int position) {
            holder.bindHeader(cols);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }


    private static class RowsAdapter extends RecyclerView.Adapter<RowVH> {
        private final List<String> cols;
        private final List<Point3D_Drill> data;
        private final double displayFactor;
        private final Runnable onSelectionChanged;
        private final OnHoleActionListener actionListener;

        RowsAdapter(List<String> cols, List<Point3D_Drill> data, double displayFactor,
                    Runnable onSelectionChanged,
                    @Nullable OnHoleActionListener actionListener) {
            this.cols = cols;
            this.data = data;
            this.displayFactor = displayFactor;
            this.onSelectionChanged = onSelectionChanged;
            this.actionListener = actionListener;
        }

        @NonNull
        @Override
        public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drill_point_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RowVH holder, int position) {
            holder.bindData(cols, data.get(position), displayFactor, onSelectionChanged, actionListener);

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class RowVH extends RecyclerView.ViewHolder {
        private final LinearLayout rowRoot;
        private final CheckBox cbSelect;
        private final TextView tvStatus;
        private final LinearLayout cells;

        RowVH(@NonNull View itemView) {
            super(itemView);
            rowRoot = itemView.findViewById(R.id.rowRoot);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cells = itemView.findViewById(R.id.cellsContainer);
        }

        void bindHeader(List<String> cols) {
            cbSelect.setVisibility(View.INVISIBLE);
            tvStatus.setText("STATUS");
            tvStatus.setTypeface(tvStatus.getTypeface(), android.graphics.Typeface.BOLD);

            cells.removeAllViews();
            Context ctx = rowRoot.getContext();
            for (String c : cols) {
                // label più leggibile per deltaZ
                String label = c.equals("Delta Z") ? "Delta Z" : c;
                cells.addView(makeCell(ctx, label, true, c));
            }

            rowRoot.setBackgroundColor(Color.LTGRAY);//sfondo dell'Header
        }

        void bindData(List<String> cols, Point3D_Drill p, double displayFactor,
                      Runnable onSelectionChanged,
                      @Nullable OnHoleActionListener actionListener) {
            cbSelect.setVisibility(View.VISIBLE);

            tvStatus.setTypeface(tvStatus.getTypeface(), android.graphics.Typeface.NORMAL);
            tvStatus.setText(statusLabel(p));

            boolean selected = isSelected(p);
            boolean selectable = isSelectable(p);

            // highlight riga selezionata
            int s = statusValue(p);

            if (selected) {
                // Selected sempre giallo
                rowRoot.setBackgroundColor(Color.YELLOW);
            } else if (s == 1) {
                // DONE verde (soft)
                rowRoot.setBackgroundColor(Color.argb(100, 0, 200, 0));
            } else if (s == -1) {
                // ABORTED magenta (soft)
                rowRoot.setBackgroundColor(Color.argb(100, 200, 0, 0));
            } else {
                // TODO / default
                rowRoot.setBackgroundColor(Color.TRANSPARENT);
            }


            cbSelect.setOnCheckedChangeListener(null);
            cbSelect.setEnabled(selectable);
            cbSelect.setChecked(selected && selectable);
            cbSelect.setOnClickListener(v -> {
                if (!selectable) {
                    cbSelect.setChecked(false);
                    return;
                }

                Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;

                if (isSamePoint(p, sel)) {
                    // 🔁 era già selezionato → deseleziona
                    DataSaved.Selected_Point3D_Drill = null;
                    cbSelect.setChecked(false);
                } else {
                    // ✅ nuova selezione
                    DataSaved.Selected_Point3D_Drill = p;
                    cbSelect.setChecked(true);
                }

                if (onSelectionChanged != null) onSelectionChanged.run();
            });

          /*  cbSelect.setOnClickListener(v -> {
                if (!selectable) {
                    cbSelect.setChecked(false);
                    return;
                }
                DataSaved.Selected_Point3D_Drill = p;
                if (onSelectionChanged != null) onSelectionChanged.run();
            });*/

            rowRoot.setOnClickListener(v -> {
            /*    if (!selectable) return;
                DataSaved.Selected_Point3D_Drill = p;
                if (onSelectionChanged != null) onSelectionChanged.run();*/
            });


            cells.removeAllViews();
            Context ctx = rowRoot.getContext();
            for (String c : cols) {
                cells.addView(makeCell(ctx, valueForColumn(p, c, displayFactor), false, c));
            }


            // Long-press: allow RE-OPEN only for DONE/ABORTED
            rowRoot.setOnLongClickListener(v -> {
                int stt = statusValue(p);
                if (stt == 0) return false; // TODO -> nothing to do
                if (actionListener == null) return false;

                new android.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Re-open hole")
                        .setMessage("This hole will be set back to TODO and a RE-OPENED entry will be appended to the project report.\n\nContinue?")
                        .setPositiveButton("RE-OPEN", (d, which) -> {
                            actionListener.onReopenRequested(p);
                            if (onSelectionChanged != null) onSelectionChanged.run();
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();

                return true;
            });

        }

        //Definisce la larghezza delle celle/colonne
        private static TextView makeCell(Context ctx, String text, boolean bold, String column) {
            TextView tv = new TextView(ctx);

            int widthDp;
            if ("Description".equals(column)) {
                widthDp = 180;
            } else if ("Row".equals(column)) {
                widthDp = 60;
            } else if ("Tilt".equals(column)) {
                widthDp = 65;
            } else if ("Hole".equals(column)) {
                widthDp = 110;
            } else if ("Diameter".equals(column) || "Bearing".equals(column)) {
                widthDp = 90;
            } else {
                widthDp = 150;
            }
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(dp(ctx, widthDp), LinearLayout.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(lp);

            tv.setText(text == null ? "" : text);
            tv.setSingleLine(true);
            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);

            tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, bold ? 16 : 15);
            tv.setTextColor(android.graphics.Color.BLACK);

            tv.setPadding(dp(ctx, 8), dp(ctx, 6), dp(ctx, 8), dp(ctx, 6));
            if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);

            return tv;
        }


        private static int dp(Context ctx, int dp) {
            float d = ctx.getResources().getDisplayMetrics().density;
            return (int) (dp * d);
        }
    }

    private static boolean isSamePoint(Point3D_Drill a, Point3D_Drill b) {
        if (a == null || b == null) return false;

        // se rowId + id identificano univocamente il punto
        return safeEq(a.getRowId(), b.getRowId()) &&
                safeEq(a.getId(), b.getId());
    }

    private static boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

    public interface OnHoleActionListener {
        void onReopenRequested(@NonNull Point3D_Drill hole);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnHoleActionListener) {
            holeActionListener = (OnHoleActionListener) context;
        } else {
            holeActionListener = null; // dialog funziona lo stesso, ma senza azione
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        holeActionListener = null;
    }

}
