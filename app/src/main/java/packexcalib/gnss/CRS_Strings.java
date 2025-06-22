package packexcalib.gnss;

public class CRS_Strings {
    public static final String _21500 = "BE ON_SH BD50 BRUSSELS BELGE LAMBERT 50";
    public static final String _31300 = "BE ON_SH BD72 BELGE LAMBERT 72";
    public static final String _31370 = "BE LAMBERT 72";
    public static final String _2100 = "Greek Grid";
    public static final String _3447 = "BE ON_SH ETRS89 BELGIAN LAMBERT 2005";
    public static final String _3812 = "BE ON_SH ETRS89 BELGIAN LAMBERT 2008";
    public static final String _23095 = "NL OFF_SH ED50 TM 5 NE";
    public static final String _28992 = "RDNAPTRANS2018";
    public static final String _28991 = "NL ON_SH AMERSFOORT RD OLD";
    public static final String _UTM = "UTM";
    public static final String _NONE = "USE .SP FILE";

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
            case "21500":
                s = _21500;
                break;
            case "31300":
                s = _31300;
                break;
            case "31370":
                s = _31370;
                break;
            case "3447":
                s = _3447;
                break;
            case "3812":
                s = _3812;
                break;
            case "23095":
                s = _23095;
                break;
            case "28992":
                s = _28992;
                break;
            case "28991":
                s = _28991;
                break;
            case "2100":
                s =_2100;
                break;
            default:
                s = crs;
                break;

        }
        return s;

    }


}
