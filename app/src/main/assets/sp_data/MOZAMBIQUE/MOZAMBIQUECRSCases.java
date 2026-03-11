public class MOZAMBIQUECRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MOZAMBIQUE__36_E_TO_42_E__MOZNET__UTM_ZONE_37S__3037.SP":
                MyData.push("crs", "3037");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__MOZNET__4952.SP":
                MyData.push("crs", "4952");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__MOZNET__GEOCENTRIC__4358.SP":
                MyData.push("crs", "4358");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__OFFSHORE__WGS_84__TM_36_SE__32766.SP":
                MyData.push("crs", "32766");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__ONSHORE_EAST_OF_36_E__MOZNET__UTM_ZONE_38S__5629.SP":
                MyData.push("crs", "5629");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__ONSHORE_EAST_OF_36_E__TETE__UTM_ZONE_37S__2737.SP":
                MyData.push("crs", "2737");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__ONSHORE_WEST_OF_36_E__TETE__UTM_ZONE_36S__2736.SP":
                MyData.push("crs", "2736");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MOZAMBIQUE__WEST_OF_36_E__MOZNET__UTM_ZONE_36S__3036.SP":
                MyData.push("crs", "3036");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}