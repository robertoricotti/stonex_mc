public class POLANDCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "POLAND__16_5_E_TO_19_5_E__ETRF2000_PL__CS2000_18__2177.SP":
                MyData.push("crs", "2177");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__19_5_E_TO_22_5_E__ETRF2000_PL__CS2000_21__2178.SP":
                MyData.push("crs", "2178");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__EAST_OF_22_5_E__ETRF2000_PL__CS2000_24__2179.SP":
                MyData.push("crs", "2179");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ETRF2000_PL__CS92__2180.SP":
                MyData.push("crs", "2180");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ONSHORE__PULKOVO_1942_58__GUGIK_80__3328.SP":
                MyData.push("crs", "3328");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__WEST_OF_16_5_E__ETRF2000_PL__CS2000_15__2176.SP":
                MyData.push("crs", "2176");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_III__PULKOVO_1942_58__POLAND_ZONE_III__2173.SP":
                MyData.push("crs", "2173");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_II__PULKOVO_1942_58__POLAND_ZONE_II__2172.SP":
                MyData.push("crs", "2172");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_IV__PULKOVO_1942_58__POLAND_ZONE_IV__2174.SP":
                MyData.push("crs", "2174");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_I__PULKOVO_1942_58__POLAND_ZONE_I__2171.SP":
                MyData.push("crs", "2171");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_I__PULKOVO_1942_58__POLAND_ZONE_I__3120.SP":
                MyData.push("crs", "3120");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "POLAND__ZONE_V__PULKOVO_1942_58__POLAND_ZONE_V__2175.SP":
                MyData.push("crs", "2175");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}