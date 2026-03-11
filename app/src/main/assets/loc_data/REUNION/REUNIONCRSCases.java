public class REUNIONCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "REUNION__EAST_OF_54_E__RGR92__UTM_ZONE_40S__2975.SP":
                MyData.push("SECONDO_S_CRS", "2975");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "REUNION__ONSHORE__REUNION_1947__TM_REUNION__2990.SP":
                MyData.push("SECONDO_S_CRS", "2990");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "REUNION__ONSHORE__REUNION_1947__TM_REUNION__3727.SP":
                MyData.push("SECONDO_S_CRS", "3727");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "REUNION__RGR92__4970.SP":
                MyData.push("SECONDO_S_CRS", "4970");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "REUNION__RGR92__GEOCENTRIC__4374.SP":
                MyData.push("SECONDO_S_CRS", "4374");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "REUNION__WEST_OF_54_E__RGR92__UTM_ZONE_39S__5644.SP":
                MyData.push("SECONDO_S_CRS", "5644");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}