public class SOMALIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SOMALIA__42_E_TO_48_E__N_HEMISPHERE_ONSHORE__AFGOOYE__UTM_ZONE_38N__20538.SP":
                MyData.push("crs", "20538");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SOMALIA__ONSHORE_EAST_OF_48_E__AFGOOYE__UTM_ZONE_39N__20539.SP":
                MyData.push("crs", "20539");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}