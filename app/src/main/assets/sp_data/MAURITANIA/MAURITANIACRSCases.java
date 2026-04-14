public class MAURITANIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MAURITANIA__12_W_TO_6_W__IGN_ASTRO_1960__UTM_ZONE_29N__3368.SP":
                MyData.push("crs", "3368");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__12_W_TO_6_W__MAURITANIA_1999__UTM_ZONE_29N__3104.SP":
                MyData.push("crs", "3104");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__12_W_TO_6_W__MAURITANIA_1999__UTM_ZONE_29N__3344.SP":
                MyData.push("crs", "3344");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__18_W_TO_12_W__MAURITANIA_1999__UTM_ZONE_28N__3343.SP":
                MyData.push("crs", "3343");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__EAST_OF_6_W__IGN_ASTRO_1960__UTM_ZONE_30N__3369.SP":
                MyData.push("crs", "3369");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__EAST_OF_6_W__MAURITANIA_1999__UTM_ZONE_30N__3105.SP":
                MyData.push("crs", "3105");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__EAST_OF_6_W__MAURITANIA_1999__UTM_ZONE_30N__3345.SP":
                MyData.push("crs", "3345");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__MAURITANIA_1999__4924.SP":
                MyData.push("crs", "4924");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__WEST_OF_12_W_ONSHORE__IGN_ASTRO_1960__UTM_ZONE_28N__3367.SP":
                MyData.push("crs", "3367");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MAURITANIA__WEST_OF_12_W_ONSHORE__MAURITANIA_1999__UTM_ZONE_28N__3103.SP":
                MyData.push("crs", "3103");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}