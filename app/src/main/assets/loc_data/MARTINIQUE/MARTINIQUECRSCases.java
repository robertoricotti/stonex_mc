public class MARTINIQUECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MARTINIQUE__ONSHORE__MARTINIQUE_1938__UTM_ZONE_20N__2973.SP":
                MyData.push("SECONDO_S_CRS", "2973");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}