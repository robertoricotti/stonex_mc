package iredes;

import androidx.annotation.Nullable;

/**
 * Wrapper mutabile per contenere il punto selezionato tra schermate/dialog.
 * (passi questa reference alla dialog e lei la aggiorna)
 */
public class Selected_Point3D_Drill {

    @Nullable
    private Point3D_Drill selected;

    public Selected_Point3D_Drill() {
    }

    public Selected_Point3D_Drill(@Nullable Point3D_Drill initial) {
        this.selected = initial;
    }

    @Nullable
    public Point3D_Drill get() {
        return selected;
    }

    public void set(@Nullable Point3D_Drill p) {
        this.selected = p;
    }

    public boolean isSelected(Point3D_Drill p) {
        if (selected == null || p == null) return false;

        // match robusto: row+id se presenti
        String r1 = selected.getRowId();
        String i1 = selected.getId();
        String r2 = p.getRowId();
        String i2 = p.getId();

        if (r1 != null && i1 != null && r2 != null && i2 != null) {
            return r1.equals(r2) && i1.equals(i2);
        }
        // fallback: id solo
        if (i1 != null && i2 != null) return i1.equals(i2);

        return selected == p; // ultima spiaggia: stessa reference
    }
}
