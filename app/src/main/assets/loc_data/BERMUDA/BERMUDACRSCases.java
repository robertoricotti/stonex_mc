public class BERMUDACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "BERMUDA__BDA2000__4886.SP":
                MyData.push("SECONDO_S_CRS", "4886");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BERMUDA__BDA2000__BERMUDA_2000_NATIONAL_GRID__3770.SP":
                MyData.push("SECONDO_S_CRS", "3770");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "BERMUDA__ONSHORE__BERMUDA_1957__UTM_ZONE_20N__3769.SP":
                MyData.push("SECONDO_S_CRS", "3769");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}