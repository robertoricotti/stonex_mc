public class KUWAITCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "KUWAIT__EAST_OF_48_E_ONSHORE__NGN__UTM_ZONE_39N__31839.SP":
                MyData.push("crs", "31839");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__KUWAIT_CITY__KUDAMS__KTM__31900.SP":
                MyData.push("crs", "31900");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__KUWAIT_CITY__KUDAMS__KTM__31901.SP":
                MyData.push("crs", "31901");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__ONSHORE__KOC_LAMBERT__24600.SP":
                MyData.push("crs", "24600");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "KUWAIT__WEST_OF_48_E_ONSHORE__NGN__UTM_ZONE_38N__31838.SP":
                MyData.push("crs", "31838");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}