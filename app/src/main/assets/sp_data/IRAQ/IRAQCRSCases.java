public class IRAQCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "IRAQ__42_E_TO_48_E__IGRS__UTM_ZONE_38N__3891.SP":
                MyData.push("crs", "3891");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__42_E_TO_48_E__KARBALA_1979__UTM_ZONE_38N__3392.SP":
                MyData.push("crs", "3392");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__EAST_OF_48_E_ONSHORE__KARBALA_1979__UTM_ZONE_39N__3393.SP":
                MyData.push("crs", "3393");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__EAST_OF_48_E__IGRS__UTM_ZONE_39N__3892.SP":
                MyData.push("crs", "3892");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__IGRS__3887.SP":
                MyData.push("crs", "3887");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__ONSHORE__ED50__IRAQ_NATIONAL_GRID__3893.SP":
                MyData.push("crs", "3893");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__ONSHORE__KARBALA_1979__IRAQ_NATIONAL_GRID__6646.SP":
                MyData.push("crs", "6646");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__WEST_OF_42_E__IGRS__UTM_ZONE_37N__3890.SP":
                MyData.push("crs", "3890");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__WEST_OF_42_E__KARBALA_1979__UTM_ZONE_37N__3391.SP":
                MyData.push("crs", "3391");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAQ__WEST_OF_42_E__NAHRWAN_1967__UTM_ZONE_37N__27037.SP":
                MyData.push("crs", "27037");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}