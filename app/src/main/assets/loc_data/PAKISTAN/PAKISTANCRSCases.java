public class PAKISTANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "PAKISTAN__28_N_TO_35_35_N__KALIANPUR_1962__INDIA_ZONE_I__24376.SP":
                MyData.push("SECONDO_S_CRS", "24376");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__EAST_OF_72_E__KALIANPUR_1962__UTM_ZONE_43N__24313.SP":
                MyData.push("SECONDO_S_CRS", "24313");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_66_E_TO_72_E__KALIANPUR_1962__UTM_ZONE_42N__24312.SP":
                MyData.push("SECONDO_S_CRS", "24312");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_SOUTH_OF_28_N__KALIANPUR_1962__INDIA_ZONE_IIA__24377.SP":
                MyData.push("SECONDO_S_CRS", "24377");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "PAKISTAN__ONSHORE_WEST_OF_66_E__KALIANPUR_1962__UTM_ZONE_41N__24311.SP":
                MyData.push("SECONDO_S_CRS", "24311");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}