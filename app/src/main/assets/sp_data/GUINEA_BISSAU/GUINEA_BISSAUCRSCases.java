public class GUINEA_BISSAUCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUINEA_BISSAU__ONSHORE__BISSAU__UTM_ZONE_28N__2095.SP":
                MyData.push("crs", "2095");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}