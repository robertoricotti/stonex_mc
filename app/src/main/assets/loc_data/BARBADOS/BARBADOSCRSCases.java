public class BARBADOSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BARBADOS__ONSHORE__BARBADOS_1938__BARBADOS_NATIONAL_GRID__21292.SP":
                MyData.push("SECONDO_S_CRS", "21292");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BARBADOS__ONSHORE__BARBADOS_1938__BRITISH_WEST_INDIES_GRID__21291.SP":
                MyData.push("SECONDO_S_CRS", "21291");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}