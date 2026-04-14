public class VENEZUELACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "VENEZUELA__72_W_AND_66_W_ONSHORE__LA_CANOA__UTM_ZONE_19N__24719.SP":
                MyData.push("crs", "24719");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__72_W_AND_66_W__REGVEN__UTM_ZONE_19N__2202.SP":
                MyData.push("crs", "2202");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__EAST_OF_66_W_ONSHORE__LA_CANOA__UTM_ZONE_20N__24720.SP":
                MyData.push("crs", "24720");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__EAST_OF_66_W__REGVEN__UTM_ZONE_20N__2203.SP":
                MyData.push("crs", "2203");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__MARACAIBO_AREA__LAKE__MARACAIBO_GRID_M1__2101.SP":
                MyData.push("crs", "2101");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__MARACAIBO_AREA__LAKE__MARACAIBO_GRID_M3__2103.SP":
                MyData.push("crs", "2103");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__MARACAIBO_AREA__LAKE__MARACAIBO_GRID__2102.SP":
                MyData.push("crs", "2102");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__MARACAIBO__BLOCKS_I_II_AND_III__LAKE__MARACAIBO_LA_ROSA_GRID__2104.SP":
                MyData.push("crs", "2104");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__ONSHORE__PSAD56__ICN_REGIONAL__2317.SP":
                MyData.push("crs", "2317");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__REGVEN__4962.SP":
                MyData.push("crs", "4962");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__REGVEN__GEOCENTRIC__4368.SP":
                MyData.push("crs", "4368");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__WEST_OF_72_W__LA_CANOA__UTM_ZONE_18N__24718.SP":
                MyData.push("crs", "24718");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VENEZUELA__WEST_OF_72_W__REGVEN__UTM_ZONE_18N__2201.SP":
                MyData.push("crs", "2201");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}