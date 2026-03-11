public class MYANMARCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MYANMAR__BURMA__ONSHORE_WEST_OF_96_E__INDIAN_1954__UTM_ZONE_46N__23946.SP":
                MyData.push("SECONDO_S_CRS", "23946");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}