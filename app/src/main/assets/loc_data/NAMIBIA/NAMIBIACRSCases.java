public class NAMIBIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NAMIBIA__OFFSHORE__SCHWARZECK__UTM_ZONE_33S__29333.SP":
                MyData.push("SECONDO_S_CRS", "29333");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NAMIBIA__WALVIS_BAY__CAPE__LO15__22275.SP":
                MyData.push("SECONDO_S_CRS", "22275");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NAMIBIA__WALVIS_BAY__HARTEBEESTHOEK94__LO15__2046.SP":
                MyData.push("SECONDO_S_CRS", "2046");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}