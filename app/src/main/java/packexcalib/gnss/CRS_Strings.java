package packexcalib.gnss;

public class CRS_Strings {

   public static final String _150580 = "Greek Grid";
    public static final String _UTM = "UTM";
    public static final String _NONE = ".SP FILE";
    public static final String _LOCAL_COORDINATES_FROM_GNSS="LOCAL";

    public CRS_Strings() {

    }

    public static String description(String crs) {
        String s = "";
        switch (crs) {
            case _NONE:
                s = _NONE;
                break;
            case _UTM:
                s = _UTM;
                break;
            case _LOCAL_COORDINATES_FROM_GNSS:
                s = _LOCAL_COORDINATES_FROM_GNSS;
                break;
            case "150580":
                s = _150580;
                break;
            default:
                s = crs;
                break;

        }
        return s;

    }


}
