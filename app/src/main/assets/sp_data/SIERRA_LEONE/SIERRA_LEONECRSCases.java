public class SIERRA_LEONECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SIERRA_LEONE__EAST_OF_12_W__SIERRA_LEONE_1968__UTM_ZONE_29N__2162.SP":
                MyData.push("crs", "2162");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SIERRA_LEONE__WEST_OF_12_W__SIERRA_LEONE_1968__UTM_ZONE_28N__2161.SP":
                MyData.push("crs", "2161");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}