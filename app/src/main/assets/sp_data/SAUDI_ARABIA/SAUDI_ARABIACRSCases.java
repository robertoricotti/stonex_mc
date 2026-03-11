public class SAUDI_ARABIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SAUDI_ARABIA__EAST_OF_54_E__AIN_EL_ABD__UTM_ZONE_40N__20440.SP":
                MyData.push("crs", "20440");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SAUDI_ARABIA__ONSHORE_36_E_TO_42_E__AIN_EL_ABD__UTM_ZONE_37N__20437.SP":
                MyData.push("crs", "20437");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SAUDI_ARABIA__ONSHORE_WEST_OF_36_E__AIN_EL_ABD__UTM_ZONE_36N__20436.SP":
                MyData.push("crs", "20436");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SAUDI_ARABIA__ONSHORE__AIN_EL_ABD__ARAMCO_LAMBERT__2318.SP":
                MyData.push("crs", "2318");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}