public class KENYACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "KENYA__NORTH_OF_EQUATOR_AND_EAST_OF_36_E__ARC_1960__UTM_ZONE_37N__21097.SP":
                MyData.push("crs", "21097");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}