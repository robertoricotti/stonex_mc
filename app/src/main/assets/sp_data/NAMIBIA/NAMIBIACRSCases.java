public class NAMIBIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NAMIBIA__OFFSHORE__SCHWARZECK__UTM_ZONE_33S__29333.SP":
                MyData.push("crs", "29333");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NAMIBIA__WALVIS_BAY__CAPE__LO15__22275.SP":
                MyData.push("crs", "22275");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NAMIBIA__WALVIS_BAY__HARTEBEESTHOEK94__LO15__2046.SP":
                MyData.push("crs", "2046");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}