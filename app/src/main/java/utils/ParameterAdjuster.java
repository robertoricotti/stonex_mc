package utils;

import static packexcalib.exca.DataSaved.offsetH;
import static packexcalib.exca.DataSaved.Off_Incr_Step;

import android.util.Log;

import packexcalib.exca.DataSaved;

public class ParameterAdjuster {

    //private static double value = 0.0;        // valore corrente
    //private static double step = 0.01;        // incremento/decremento per ogni fronte

    private static boolean lastInc = false;   // stato precedente incremento
    private static boolean lastDec = false;   // stato precedente decremento

    /**
     * Aggiorna lo stato con i due booleani
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
            MyData.push("shortcutOffset_"+DataSaved.shortcutIndex, Utils.writeMetri(String.valueOf(-offsetH)));
        }

        // Rileva la transizione negativa su dec
        if (lastDec && !dec) {
            MyData.push("shortcutOffset_"+DataSaved.shortcutIndex, Utils.writeMetri(String.valueOf(-offsetH)));
        }

        // aggiorna stati precedenti
        lastInc = inc;
        lastDec = dec;
    }

    /** Restituisce il valore corrente */
    public static double getValue() {
        return offsetH;
    }

    /** Imposta manualmente il valore */
    public static void setValue(double newValue) {
        offsetH = newValue;
    }

    /** Cambia lo step */
    public static void setStep(double newStep) {
        Off_Incr_Step = newStep;
    }
}
