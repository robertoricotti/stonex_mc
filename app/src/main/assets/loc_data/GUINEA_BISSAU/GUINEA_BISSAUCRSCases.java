public class GUINEA_BISSAUCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUINEA_BISSAU__ONSHORE__BISSAU__UTM_ZONE_28N__2095.SP":
                MyData.push("SECONDO_S_CRS", "2095");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}