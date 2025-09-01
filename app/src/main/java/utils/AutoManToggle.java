package utils;

public class AutoManToggle {

    // false = MAN, true = AUTO
    public static boolean Can_Toggled_Auto = false;  // stato iniziale MAN
    private static byte lastSignal = 0;    // stato precedente del byte

    /**
     * Deve essere chiamato ogni volta che leggi il byte dal CAN
     */
    public static void update(byte signal) {
        if (lastSignal == 0 && signal == 1) {
            // toggle dello stato
            Can_Toggled_Auto = !Can_Toggled_Auto;
        }
        lastSignal = signal;
    }

    /**
     * Restituisce lo stato corrente
     */
    public static boolean getState() {
        return Can_Toggled_Auto;
    }

    /**
     * Forza lo stato corrente
     */
    public static void setState(boolean value){
        Can_Toggled_Auto=value;
    }
}

