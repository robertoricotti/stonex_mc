public class KENYACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "KENYA__NORTH_OF_EQUATOR_AND_EAST_OF_36_E__ARC_1960__UTM_ZONE_37N__21097.SP":
                MyData.push("SECONDO_S_CRS", "21097");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}