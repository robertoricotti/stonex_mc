package packexcalib.gnss;

import java.io.File;

/**
 * Factory comoda per creare il modello giusto in base all'estensione del file.
 */
public final class LocalizationFactory {
    private LocalizationFactory() {}

    public static LocalizationModel fromFile(File f) throws Exception {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".loc")) return CarlsonLocalization.fromLocFile(f);
        if (name.endsWith(".sp")) return SpLocalization.fromSpFile(f);
        if (name.endsWith(".lok")) return LeicaLokLocalization.fromLokFile(f);
        throw new IllegalArgumentException("Formato non supportato: " + f.getName());
    }
}