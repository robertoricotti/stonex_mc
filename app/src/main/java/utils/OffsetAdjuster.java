package utils;

import static packexcalib.exca.DataSaved.Off_Incr_Step;
import static packexcalib.exca.DataSaved.offsetH;

import packexcalib.exca.DataSaved;

public class OffsetAdjuster {

    //private static double value = 0.0;        // valore corrente
    //private static double step = 0.01;        // incremento/decremento per ogni fronte

    private static boolean lastInc = false;   // stato precedente incremento
    private static boolean lastDec = false;   // stato precedente decremento

    /**
     * Aggiorna lo stato con i due booleani
     *
     * @param inc true se il segnale "incrementa" è attivo
     * @param dec true se il segnale "decrementa" è attivo
     */
    public static void update(boolean inc, boolean dec) {
        // fronte di salita su incremento
        if (!lastInc && inc) {
            offsetH += Off_Incr_Step;

        }

        // fronte di salita su decremento
        if (!lastDec && dec) {
            offsetH -= Off_Incr_Step;

        }

        if (lastInc && !inc) {
            MyData.push("shortcutOffset_" + DataSaved.shortcutIndex, (String.valueOf(-offsetH)));

        }

        // Rileva la transizione negativa su dec
        if (lastDec && !dec) {
            MyData.push("shortcutOffset_" + DataSaved.shortcutIndex, (String.valueOf(-offsetH)));

        }

        // aggiorna stati precedenti
        lastInc = inc;
        lastDec = dec;

    }


}
