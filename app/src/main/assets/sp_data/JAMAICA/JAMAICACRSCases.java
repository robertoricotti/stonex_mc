public class JAMAICACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "JAMAICA__EAST_OF_78_W__JAD2001__UTM_ZONE_18N__3450.SP":
                MyData.push("crs", "3450");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "JAMAICA__JAD2001__4894.SP":
                MyData.push("crs", "4894");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "JAMAICA__ONSHORE__JAD2001__JAMAICA_METRIC_GRID__3448.SP":
                MyData.push("crs", "3448");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "JAMAICA__ONSHORE__JAD69__JAMAICA_NATIONAL_GRID__24200.SP":
                MyData.push("crs", "24200");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "JAMAICA__WEST_OF_78_W__JAD2001__UTM_ZONE_17N__3449.SP":
                MyData.push("crs", "3449");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}