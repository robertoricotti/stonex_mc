public class BOTSWANACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BOTSWANA__21_E_TO_27_E__CAPE__UTM_ZONE_36S__22236.SP":
                MyData.push("crs", "22236");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOTSWANA__EAST_OF_24_E__CAPE__UTM_ZONE_35S__22235.SP":
                MyData.push("crs", "22235");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BOTSWANA__WEST_OF_24_E__CAPE__UTM_ZONE_34S__22234.SP":
                MyData.push("crs", "22234");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}