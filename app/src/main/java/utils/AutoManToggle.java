package utils;

public class AutoManToggle {

    // false = MAN, true = AUTO
    public static boolean Can_Toggled_Auto = false;  // stato iniziale MAN
    public static boolean Can_Toggled_Auto_L = false;  // stato iniziale MAN
    public static boolean Can_Toggled_Auto_R = false;  // stato iniziale MAN
    public static boolean Can_Toggled_Auto_SS = false;  // stato iniziale MAN
    private static byte lastSignal = 0;// stato precedente del byte
    private static byte lastSignal_L = 0;
    private static byte lastSignal_R = 0;
    private static byte lastSignal_SS = 0;

    /**
     * Deve essere chiamato ogni volta che leggi il byte dal CAN
     */
    public static void update(boolean signal) {
        if (lastSignal == 0 && signal) {
            // toggle dello stato
            Can_Toggled_Auto = !Can_Toggled_Auto;
        }
        if (signal) {
            lastSignal = 1;
        } else {
            lastSignal = 0;
        }

    }

    public static void updateLEFT(boolean signal) {
        if (lastSignal_L == 0 && signal) {
            // toggle dello stato
            Can_Toggled_Auto_L = !Can_Toggled_Auto_L;
        }
        if (signal) {
            lastSignal_L = 1;
        } else {
            lastSignal_L = 0;
        }

    }


    public static void updateRIGHT(boolean signal) {
        if (lastSignal_R == 0 && signal) {
            // toggle dello stato
            Can_Toggled_Auto_R = !Can_Toggled_Auto_R;
        }
        if (signal) {
            lastSignal_R = 1;
        } else {
            lastSignal_R = 0;
        }

    }

    public static void updateSS(boolean signal) {
        if (lastSignal_SS == 0 && signal) {
            // toggle dello stato
            Can_Toggled_Auto_SS = !Can_Toggled_Auto_SS;
        }
        if (signal) {
            lastSignal_SS = 1;
        } else {
            lastSignal_SS = 0;
        }

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
    public static void setState(boolean value) {
        Can_Toggled_Auto = value;
    }
}

