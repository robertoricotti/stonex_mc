public class GUAMCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUAM__ONSHORE__NAD83_HARN__GUAM_MAP_GRID__4414.SP":
                MyData.push("crs", "4414");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUAM__ONSHORE__NAD83_MA11__GUAM_MAP_GRID__6637.SP":
                MyData.push("crs", "6637");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}