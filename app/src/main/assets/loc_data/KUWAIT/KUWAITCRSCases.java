public class KUWAITCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "KUWAIT__EAST_OF_48_E_ONSHORE__NGN__UTM_ZONE_39N__31839.SP":
                MyData.push("SECONDO_S_CRS", "31839");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__KUWAIT_CITY__KUDAMS__KTM__31900.SP":
                MyData.push("SECONDO_S_CRS", "31900");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__KUWAIT_CITY__KUDAMS__KTM__31901.SP":
                MyData.push("SECONDO_S_CRS", "31901");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__ONSHORE__KOC_LAMBERT__24600.SP":
                MyData.push("SECONDO_S_CRS", "24600");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__WEST_OF_48_E_ONSHORE__NGN__UTM_ZONE_38N__31838.SP":
                MyData.push("SECONDO_S_CRS", "31838");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}