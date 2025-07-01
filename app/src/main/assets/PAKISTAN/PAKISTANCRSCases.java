public class PAKISTANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PAKISTAN__28_N_TO_35_35_N__KALIANPUR_1962__INDIA_ZONE_I__24376.SP":
                MyData.push("crs", "24376");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__EAST_OF_72_E__KALIANPUR_1962__UTM_ZONE_43N__24313.SP":
                MyData.push("crs", "24313");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_66_E_TO_72_E__KALIANPUR_1962__UTM_ZONE_42N__24312.SP":
                MyData.push("crs", "24312");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_SOUTH_OF_28_N__KALIANPUR_1962__INDIA_ZONE_IIA__24377.SP":
                MyData.push("crs", "24377");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_WEST_OF_66_E__KALIANPUR_1962__UTM_ZONE_41N__24311.SP":
                MyData.push("crs", "24311");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}