public class FIJICRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "FIJI__MAIN_ISLANDS__EAST_OF_180__FIJI_1956__UTM_ZONE_1S__3142.SP":
                MyData.push("crs", "3142");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FIJI__MAIN_ISLANDS__WEST_OF_180__FIJI_1956__UTM_ZONE_60S__3141.SP":
                MyData.push("crs", "3141");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FIJI__ONSHORE__FIJI_1986__FIJI_MAP_GRID__3460.SP":
                MyData.push("crs", "3460");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "FIJI__VITI_LEVU__VITI_LEVU_1912__VITI_LEVU_GRID__3140.SP":
                MyData.push("crs", "3140");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}