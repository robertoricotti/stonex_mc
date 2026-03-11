public class SEYCHELLESCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SEYCHELLES__SEYCHELLES_BANK__SOUTH_EAST_ISLAND_1943__UTM_ZONE_40N__6915.SP":
                MyData.push("SECONDO_S_CRS", "6915");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}