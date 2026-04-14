public class UGANDACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "UGANDA__NORTH_OF_EQUATOR_AND_WEST_OF_30_E__ARC_1960__UTM_ZONE_35N__21095.SP":
                MyData.push("SECONDO_S_CRS", "21095");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}