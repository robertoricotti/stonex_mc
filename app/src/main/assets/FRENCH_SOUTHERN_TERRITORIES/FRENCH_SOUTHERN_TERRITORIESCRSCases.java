public class FRENCH_SOUTHERN_TERRITORIESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FRENCH_SOUTHERN_TERRITORIES__KERGUELEN_ONSHORE__IGN_1962_KERGUELEN__UTM_ZONE_42S__3336.SP":
                MyData.push("crs", "3336");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_SOUTHERN_TERRITORIES__KERGUELEN_ONSHORE__K0_1949__UTM_ZONE_42S__2979.SP":
                MyData.push("crs", "2979");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}