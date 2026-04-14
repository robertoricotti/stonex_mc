public class TAIWANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "TAIWAN__118_E_TO_120_E__TWD97__TM2_ZONE_119__3825.SP":
                MyData.push("crs", "3825");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TAIWAN__120_E_TO_122_E__TWD97__TM2_ZONE_121__3826.SP":
                MyData.push("crs", "3826");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TAIWAN__ONSHORE__MAINLAND_AND_PENGHU__HU_TZU_SHAN_1950__UTM_ZONE_51N__3829.SP":
                MyData.push("crs", "3829");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TAIWAN__ONSHORE__MAINLAND__TWD67__TM2_ZONE_121__3828.SP":
                MyData.push("crs", "3828");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TAIWAN__ONSHORE__PENGHU__TWD67__TM2_ZONE_119__3827.SP":
                MyData.push("crs", "3827");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "TAIWAN__TWD97__3822.SP":
                MyData.push("crs", "3822");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}