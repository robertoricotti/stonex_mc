public class FALKLAND_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FALKLAND_ISLANDS__OFFSHORE_63_W_TO_57_W__WGS_84__TM_60_SW__6703.SP":
                MyData.push("crs", "6703");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FALKLAND_ISLANDS__ONSHORE_EAST_OF_60_W__SAPPER_HILL_1943__UTM_ZONE_21S__29221.SP":
                MyData.push("crs", "29221");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FALKLAND_ISLANDS__ONSHORE_WEST_OF_60_W__SAPPER_HILL_1943__UTM_ZONE_20S__29220.SP":
                MyData.push("crs", "29220");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}