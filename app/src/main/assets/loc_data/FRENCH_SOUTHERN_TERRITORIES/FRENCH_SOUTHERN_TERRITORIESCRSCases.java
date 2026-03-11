public class FRENCH_SOUTHERN_TERRITORIESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FRENCH_SOUTHERN_TERRITORIES__KERGUELEN_ONSHORE__IGN_1962_KERGUELEN__UTM_ZONE_42S__3336.SP":
                MyData.push("SECONDO_S_CRS", "3336");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FRENCH_SOUTHERN_TERRITORIES__KERGUELEN_ONSHORE__K0_1949__UTM_ZONE_42S__2979.SP":
                MyData.push("SECONDO_S_CRS", "2979");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}