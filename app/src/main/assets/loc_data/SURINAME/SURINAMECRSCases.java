public class SURINAMECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SURINAME__OFFSHORE__ZANDERIJ__TM_54_NW__31154.SP":
                MyData.push("SECONDO_S_CRS", "31154");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SURINAME__ONSHORE__ZANDERIJ__SURINAME_OLD_TM__31170.SP":
                MyData.push("SECONDO_S_CRS", "31170");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SURINAME__ONSHORE__ZANDERIJ__SURINAME_TM__31171.SP":
                MyData.push("SECONDO_S_CRS", "31171");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SURINAME__ZANDERIJ__UTM_ZONE_21N__31121.SP":
                MyData.push("SECONDO_S_CRS", "31121");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}