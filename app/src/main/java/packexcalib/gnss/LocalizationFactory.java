package packexcalib.gnss;

import java.io.File;
import java.util.Locale;

/**
 * Factory comoda per creare il modello giusto in base all'estensione del file.
 */
public final class LocalizationFactory {
    private LocalizationFactory() {
    }

    public static LocalizationModel fromFile(File f,
                                             NativeProjTransformer geoToProj,
                                             NativeProjTransformer projToGeo) throws Exception {
        String name = f.getName().toLowerCase(Locale.ROOT);

        if (name.endsWith(".sp")) {
            return SpLocalization.fromSpFile(f);
        }

        if (name.endsWith(".loc")) {
            return ProjectedLocLocalization.fromLocFile(f, geoToProj, projToGeo);
        }

        throw new IllegalArgumentException("Formato non supportato: " + f.getName());
    }
}