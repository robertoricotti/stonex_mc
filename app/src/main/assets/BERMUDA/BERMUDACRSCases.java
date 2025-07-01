public class BERMUDACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BERMUDA__BDA2000__4886.SP":
                MyData.push("crs", "4886");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BERMUDA__BDA2000__BERMUDA_2000_NATIONAL_GRID__3770.SP":
                MyData.push("crs", "3770");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BERMUDA__ONSHORE__BERMUDA_1957__UTM_ZONE_20N__3769.SP":
                MyData.push("crs", "3769");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}