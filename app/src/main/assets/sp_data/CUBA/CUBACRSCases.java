public class CUBACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CUBA__ONSHORE_NORTH_OF_21_30_N__NAD27__CUBA_NORTE__2085.SP":
                MyData.push("crs", "2085");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_NORTH_OF_21_30_N__NAD27__CUBA_NORTE__3795.SP":
                MyData.push("crs", "3795");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_SOUTH_OF_21_30_N__NAD27__CUBA_SUR__2086.SP":
                MyData.push("crs", "2086");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "CUBA__ONSHORE_SOUTH_OF_21_30_N__NAD27__CUBA_SUR__3796.SP":
                MyData.push("crs", "3796");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}