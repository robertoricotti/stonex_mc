public class TUNISIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "TUNISIA__NORTH_OF_34_39_N__CARTHAGE__NORD_TUNISIE__22391.SP":
                MyData.push("crs", "22391");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__OFFSHORE__CARTHAGE__TM_11_NE__2088.SP":
                MyData.push("crs", "2088");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__OFFSHORE__CARTHAGE__UTM_ZONE_32N__22332.SP":
                MyData.push("crs", "22332");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TUNISIA__SOUTH_OF_34_39_N__CARTHAGE__SUD_TUNISIE__22392.SP":
                MyData.push("crs", "22392");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}