public class BOLIVIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BOLIVIA__66_W_TO_60_W__MARGEN__UTM_ZONE_20S__5355.SP":
                MyData.push("crs", "5355");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__66_W_TO_60_W__PSAD56__UTM_ZONE_20S__24880.SP":
                MyData.push("crs", "24880");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__EAST_OF_60_W__MARGEN__UTM_ZONE_21S__5357.SP":
                MyData.push("crs", "5357");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__EAST_OF_60_W__PSAD56__UTM_ZONE_21S__24881.SP":
                MyData.push("crs", "24881");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__MARGEN__5352.SP":
                MyData.push("crs", "5352");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOLIVIA__WEST_OF_66_W__MARGEN__UTM_ZONE_19S__5356.SP":
                MyData.push("crs", "5356");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}