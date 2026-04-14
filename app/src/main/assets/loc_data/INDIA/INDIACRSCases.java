public class INDIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "INDIA__EAST_OF_96_E__KALIANPUR_1975__UTM_ZONE_47N__24347.SP":
                MyData.push("SECONDO_S_CRS", "24347");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__MAINLAND_72_E_TO_78_E__KALIANPUR_1975__UTM_ZONE_43N__24343.SP":
                MyData.push("SECONDO_S_CRS", "24343");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__MAINLAND_90_E_TO_96_E__KALIANPUR_1975__UTM_ZONE_46N__24346.SP":
                MyData.push("SECONDO_S_CRS", "24346");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__MAINLAND_SOUTH_OF_15_N__KALIANPUR_1975__INDIA_ZONE_IVA__24383.SP":
                MyData.push("SECONDO_S_CRS", "24383");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__NORTH_OF_28_N__KALIANPUR_1975__INDIA_ZONE_I__24378.SP":
                MyData.push("SECONDO_S_CRS", "24378");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_15_N_TO_21_N__KALIANPUR_1975__INDIA_ZONE_IIIA__24381.SP":
                MyData.push("SECONDO_S_CRS", "24381");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_21_N_TO_28_N_AND_WEST_OF_82_E__KALIANPUR_1975__INDIA_ZONE_IIA__24379.SP":
                MyData.push("SECONDO_S_CRS", "24379");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_78_E_TO_84_E__KALIANPUR_1975__UTM_ZONE_44N__24344.SP":
                MyData.push("SECONDO_S_CRS", "24344");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_84_E_TO_90_E__KALIANPUR_1975__UTM_ZONE_45N__24345.SP":
                MyData.push("SECONDO_S_CRS", "24345");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_NORTH_OF_21_N_AND_EAST_OF_82_E__KALIANPUR_1975__INDIA_ZONE_IIB__24380.SP":
                MyData.push("SECONDO_S_CRS", "24380");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "INDIA__ONSHORE_WEST_OF_72_E__KALIANPUR_1975__UTM_ZONE_42N__24342.SP":
                MyData.push("SECONDO_S_CRS", "24342");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}