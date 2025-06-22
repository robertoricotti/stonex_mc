package packexcalib.gnss;

import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import gui.MyApp;
import utils.MyData;

public class MyGeoide {

    public static void setGeoid(String geoid) {
        if(MyData.get_String("geoidPath")==null){
            MyData.push("geoidPath","null");
        }else {
            MyData.push("geoidPath", geoid);
            String s = String.valueOf(geoid);
            if (s.equalsIgnoreCase("null")) {
                MyApp.GEOIDE_PATH = null;
            } else {
                MyApp.GEOIDE_PATH = geoid;
            }
        }
    }
}
