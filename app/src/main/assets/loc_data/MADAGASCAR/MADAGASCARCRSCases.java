public class MADAGASCARCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MADAGASCAR__NEARSHORE__EAST_OF_48_E__TANANARIVE__UTM_ZONE_39S__29739.SP":
                MyData.push("SECONDO_S_CRS", "29739");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MADAGASCAR__NEARSHORE__WEST_OF_48_E__TANANARIVE__UTM_ZONE_38S__29738.SP":
                MyData.push("SECONDO_S_CRS", "29738");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MADAGASCAR__ONSHORE__TANANARIVE__PARIS__LABORDE_GRID_APPROXIMATION__29702.SP":
                MyData.push("SECONDO_S_CRS", "29702");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MADAGASCAR__ONSHORE__TANANARIVE__PARIS__LABORDE_GRID__29700.SP":
                MyData.push("SECONDO_S_CRS", "29700");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}